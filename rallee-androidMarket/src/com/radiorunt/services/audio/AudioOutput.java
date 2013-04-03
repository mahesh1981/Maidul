package com.radiorunt.services.audio;

import java.nio.ShortBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.radiorunt.activities.HomeActivity;
import com.radiorunt.businessobjects.RRUser;
import com.radiorunt.services.MumbleProtocol;
import com.radiorunt.services.PacketDataStream;
import com.radiorunt.services.audio.AudioUser.PacketReadyHandler;
import com.radiorunt.utilities.Globals;
import com.radiorunt.utilities.Settings;
import com.radiorunt.utilities.jni.Speex;
import com.radiorunt.utilities.jni.Speexrec;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioTrack;
import android.util.Log;

/**
 * Audio output thread. Handles the playback of UDP packets added with
 * addFrameToBuffer.
 * 
 */
public class AudioOutput implements Runnable {
	private final PacketReadyHandler packetReadyHandler = new PacketReadyHandler() {
		public void packetReady(final AudioUser user) {
			synchronized (userPackets) {
				if (!userPackets.containsKey(user.getUser())) {
					host.setTalkState(user.getUser(),
							AudioOutputHost.STATE_TALKING);
					userPackets.put(user.getUser(), user);
					userPackets.notify();
				}
			}
		}
	};

	private final static int standbyTreshold = 5000;
	private final Settings settings;

	private boolean shouldRun;
	private final AudioTrack at;
	private final int bufferSize;
	private final int minBufferSize;
	Context ctx;

	final Map<RRUser, AudioUser> userPackets = new HashMap<RRUser, AudioUser>();
	private final Map<RRUser, AudioUser> users = new HashMap<RRUser, AudioUser>();

	/**
	 * Buffer used to hold temporary float values while mixing multiple inputs.
	 * Only for use in the audio thread.
	 */
	final short[] tempMix = new short[MumbleProtocol.FRAME_SIZE];

	private final AudioOutputHost host;

	public AudioOutput(final Context ctx, final AudioOutputHost host) {
		this.ctx = ctx;
		this.settings = new Settings(ctx);
		this.host = host;

		minBufferSize = AudioTrack.getMinBufferSize(MumbleProtocol.SAMPLE_RATE,
				AudioFormat.CHANNEL_CONFIGURATION_MONO,
				AudioFormat.ENCODING_PCM_16BIT);

		// Double the buffer size to reduce stuttering.
		final int desiredBufferSize = minBufferSize * 2;

		// Resolve the minimum frame count that fills the minBuffer requirement.
		final int frameCount = (int) Math.ceil((double) desiredBufferSize
				/ MumbleProtocol.FRAME_SIZE);

		bufferSize = frameCount * MumbleProtocol.FRAME_SIZE;

		at = new AudioTrack(settings.getAudioStream(),
				MumbleProtocol.SAMPLE_RATE,
				AudioFormat.CHANNEL_CONFIGURATION_MONO,
				AudioFormat.ENCODING_PCM_16BIT, bufferSize,
				AudioTrack.MODE_STREAM);

		// Set this here so this.start(); this.shouldRun = false; doesn't
		// result in run() setting shouldRun to true afterwards and continuing
		// running.
		shouldRun = true;
	}

	public void addFrameToBuffer(final RRUser u, final PacketDataStream pds,
			final int flags) {
		AudioUser user = users.get(u);
		if (user == null) {

			user = new AudioUser(u, settings.getAudioQuality(), ctx); // settings.isJitterBuffer());
			users.put(u, user);
			// Don't add the user to userPackets yet. The collection should
			// have only users with ready frames. Since this method is
			// called only from the TCP connection thread it will never
			// create a new AudioUser while a previous one is still decoding.
		}

		user.addFrameToBuffer(pds, packetReadyHandler);
	}

	public void run() {
		android.os.Process
				.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
		try {
			audioLoop();
		} catch (final InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void stop() {
		shouldRun = false;
		synchronized (userPackets) {
			userPackets.notify();
		}
	}

	@SuppressWarnings("static-access")
	private void audioLoop() throws InterruptedException {
		final short[] out = new short[MumbleProtocol.FRAME_SIZE];
		final List<AudioUser> mix = new LinkedList<AudioUser>();

		int buffered = 0;
		boolean playing = false;

		try {
			while (shouldRun) {
				mix.clear();

				// Get mix frames from the AudioUsers
				fillMixFrames(mix);

				// If there is output, play it now.
				if (mix.size() > 0) {
					// Mix all the frames into one array.
					mix(out, mix);

					at.write(out, 0, MumbleProtocol.FRAME_SIZE);

					// Make sure we are playing when there are enough samples
					// buffered.
					if (!playing) {
						buffered += out.length;
						// if(HomeActivity.shortBuf == null){
						// HomeActivity.shortBuf = ShortBuffer.allocate(240);
						// }
						// HomeActivity.shortBuf.wrap(out);
						// HomeActivity.shortQueue.add(out);
						if (buffered >= minBufferSize) {
							// Log.i("msg", "AEC INIT " +
							// Speexrec.getAecStatus());
							// if(Speexrec.getAecStatus() == 0){
							// Speexrec.initEcho(320, 1600);
							// }
							try {
								at.play();
							} catch (IllegalStateException e) {
								// TODO Auto-generated catch block
								Globals.logError(this, "erorr playing " + e);
								e.printStackTrace();
							}
							// Speexrec.speexEchoPlayback(out);
							playing = true;
							buffered = 0;

							Globals.logInfo(this,
									"Enough data buffered. Starting audio.");
						}
					}

					// Continue with playback since we know that there is at
					// least
					// one AudioUser in userPackets that wasn't removed as it
					// had
					// frames for mixing.
					continue;
				}

				// Wait for more input.
				playing &= !pauseForInput();
				if (!playing && buffered > 0) {
					Globals.logWarn(this,
							"AudioOutput: Stopped playing while buffered data present.");
				}
			}
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			Globals.logError(this, "erorr playing " + e1);
			e1.printStackTrace();
		}
		try {
			at.flush();
			at.stop();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			Globals.logError(this, "erorr stoping playback " + e);
			e.printStackTrace();
		}

	}

	private void fillMixFrames(final List<AudioUser> mix) {
		synchronized (userPackets) {
			final Iterator<AudioUser> i = userPackets.values().iterator();
			while (i.hasNext()) {
				final AudioUser user = i.next();
				if (user.hasFrame()) {
					mix.add(user);
				} else {
					i.remove();
					host.setTalkState(user.getUser(),
							AudioOutputHost.STATE_PASSIVE);
				}
			}
		}
	}

	private void mix(final short[] clipOut, final List<AudioUser> mix) {
		// Reset mix buffer.
		Arrays.fill(tempMix, (short) 0);

		// Sum the buffers.
		for (final AudioUser user : mix) {
			for (int i = 0; i < tempMix.length; i++) {
				tempMix[i] += user.lastFrame[i];
			}
		}

		// Clip buffer for real output.
		for (int i = 0; i < MumbleProtocol.FRAME_SIZE; i++) {
			clipOut[i] = tempMix[i];
		}
	}

	private boolean pauseForInput() throws InterruptedException {
		long silentTime;
		boolean paused = false;
		if (audioTrackState()) {
			synchronized (userPackets) {
				silentTime = System.currentTimeMillis();

				// Wait with the audio on
				while (shouldRun
						&& userPackets.isEmpty()
						&& (silentTime + standbyTreshold) > System
								.currentTimeMillis()) {

					userPackets.wait((silentTime + standbyTreshold)
							- System.currentTimeMillis() + 1);
				}

				// If conditions are still not filled, pause audio and wait
				// more.
				if (shouldRun && userPackets.isEmpty()) {
					at.pause();
					paused = true;
					Globals.logInfo(this,
							"Standby timeout reached. Audio paused.");

					while (shouldRun && userPackets.isEmpty()) {
						userPackets.wait();
					}
				}
			}
		}
		return paused;
	}

	public boolean audioTrackState() {
		if (at != null) {
			if (at.getState() == AudioTrack.STATE_INITIALIZED) {
				return true;
			} else {
				return false;
			}
		}
		return false;
	}

	public boolean setAudioVolume() {
		if (at != null) {
			at.setStereoVolume(0.1f, 0.1f);
		}
		return false;
	}
}
