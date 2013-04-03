package com.radiorunt.services.audio;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import java.util.LinkedList;

import com.radiorunt.R;
import com.radiorunt.activities.HomeActivity;
import com.radiorunt.services.MumbleProtocol;
import com.radiorunt.services.RadioRuntService;
import com.radiorunt.services.PacketDataStream;
import com.radiorunt.services.audio.WavReader.WavException;
import com.radiorunt.services.audio.WavReader.WavInfo;
import com.radiorunt.utilities.Globals;
import com.radiorunt.utilities.Settings;
import com.radiorunt.utilities.jni.Speexrec;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.util.Log;

/**
 * Thread responsible for recording voice and sending it over to server.
 * 
 */
public class RecordThread implements Runnable {
	private static int frameSize;
	private static int recordingSampleRate;
	private final short[] buffer;
	private final int framesPerPacket = 6;
	private final LinkedList<byte[]> outputQueue = new LinkedList<byte[]>();
	private int seq;
	private final RadioRuntService mService;
	private final Settings settings;
	private Context ctx;
	AudioRecord ar = null;

	public RecordThread(final RadioRuntService service, Context ctx) {
		settings = new Settings(ctx);
		mService = service;
		recordingSampleRate = MumbleProtocol.SAMPLE_RATE;
		frameSize = MumbleProtocol.FRAME_SIZE;
		buffer = new short[frameSize];
		this.ctx = ctx;
		// Speexrec.open(settings.getAudioQuality());
		// Log.i("msg", "AEC INIT " + Speexrec.getAecStatus());
		// if (Speexrec.getAecStatus() == 0) {
		// Speexrec.initEcho(240, 1600);
		// }
		// Log.i("msg", "AEC INIT " + Speexrec.getAecStatus());
		// if (Speexrec.getAecStatus() == 0) {
		// Speexrec.initEcho(320, 1600);
		// }
	}

	public final void run() {

		final boolean running = true;
		android.os.Process
				.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);

		try {
			ar = new AudioRecord(MediaRecorder.AudioSource.MIC,
					recordingSampleRate,
					AudioFormat.CHANNEL_CONFIGURATION_MONO,
					AudioFormat.ENCODING_PCM_16BIT, 64 * 1024); // AudioTrack.getMinBufferSize
			// (8000, AudioFormat.CHANNEL_CONFIGURATION_MONO,
			// AudioFormat.ENCODING_PCM_16BIT));//64 * 1024);

			if (ar.getState() != AudioRecord.STATE_INITIALIZED) {
				return;
			}

			try {
				ar.startRecording();
			} catch (IllegalStateException e) {
				// TODO Auto-generated catch block
				Globals.logError(this, "erorr recording " + e);
				e.printStackTrace();
			}
			while (running && !Thread.interrupted()) {
				final int read = ar.read(buffer, 0, frameSize);

				if (read == AudioRecord.ERROR_BAD_VALUE
						|| read == AudioRecord.ERROR_INVALID_OPERATION) {
					throw new RuntimeException("" + read);
				}
				short[] out;
				out = buffer;

				short[] outNoAec = new short[320];
				final int compressedSize = settings.getEncodedSize();
				// short[] outAec = new short[compressedSize];
				final byte[] compressed = new byte[compressedSize];

				// Speexrec.speexEchoCapture(out, outAec);
				// Speexrec.echoCancellation(out, play, outAec);
				// if(HomeActivity.shortBuf != null){
				// // Speexrec.echoCancellation(out,
				// HomeActivity.shortBuf.array(), outAec);
				// // Speexrec.encode(outAec, compressed);
				//
				// Speexrec.echoCancellationEncode(out,
				// HomeActivity.shortBuf.array(), compressed);
				// }else{
				try {
					synchronized (Speexrec.class) {
						Speexrec.encode(out, compressed);
					}

				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				// }

				// if(HomeActivity.shortQueue.poll() != null){
				// if(HomeActivity.shortBuf != null){
				// // Speexrec.echoCancellation(out,
				// HomeActivity.shortBuf.array(), outAec);
				// // Speexrec.encode(outAec, compressed);
				// outAec = HomeActivity.shortQueue.poll();
				// outAec = HomeActivity.shortBuf.array();
				// outAec = getSamples(240, HomeActivity.shortBuf);
				// Speexrec.speexEchoPlayback(outAec);
				// Speexrec.speexEchoCapture(out, outNoAec);
				// Speexrec.echoCancellation(out, outAec, outNoAec);
				// Speexrec.encode(outNoAec, compressed);
				// }
				// Speexrec.echoCancellationEncode(out,outAec, compressed);
				// }

				// Speexrec.speexEchoCaptureEncode(out, compressed,
				// compressedSize);

				outputQueue.add(compressed);

				if (outputQueue.size() < framesPerPacket) {
					continue;
				}

				final byte[] outputBuffer = new byte[1024];

				final PacketDataStream pds = new PacketDataStream(outputBuffer);

				while (!outputQueue.isEmpty()) {
					int flags = 0;
					flags |= mService.getCodec() << 5;
					outputBuffer[0] = (byte) flags;

					pds.rewind();
					// skip flags
					pds.next();
					seq += framesPerPacket;
					pds.writeLong(seq);
					for (int i = 0; i < framesPerPacket; ++i) {
						final byte[] tmp = outputQueue.poll();
						if (tmp == null) {
							break;
						}
						int head = (short) tmp.length;
						if (i < framesPerPacket - 1) {
							head |= 0x80;
						}

						pds.append(head);
						pds.append(tmp);
					}

					mService.sendUdpMessage(outputBuffer, pds.size());
				}
			}
		} finally {
			try {
				sendBeep();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				Globals.logError(this, "sendBeep" + e);
				e.printStackTrace();
				finalize();
			}
			if (ar != null) {
				ar.release();
			}
		}
	}

	public static byte[] toBytes(short s) {
		return new byte[] { (byte) (s & 0x00FF), (byte) ((s & 0xFF00) >> 8) };
	}

	public boolean isInitialised() {
		if (ar != null) {
			if (ar.getState() == AudioRecord.STATE_INITIALIZED) {
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

	@Override
	protected final void finalize() {
		if (ar != null) {
			ar.release();
		}
		// Speexrec.close();
	}

	private void sendBeep() {

		short[] out = null;
		InputStream is_beep = null;

		is_beep = ctx.getResources().openRawResource(R.raw.beep8);

		byte[] wavePcm = null;
		try {
			WavInfo wi = WavReader.readHeader(is_beep);
			wavePcm = WavReader.readWavPcm(wi, is_beep);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (WavException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		;

		Globals.logDebug(this, " " + wavePcm.length);
		out = byteToShort(wavePcm, wavePcm.length);

		short[] buffer = new short[320];
		int cycles = out.length / 320 + 1;
		int lastCyc = out.length - (cycles - 1) * 320;
		int buffPosition = 0;
		Globals.logDebug(this, "wavPCM cycles " + cycles + "lastCycles "
				+ lastCyc);

		for (int i = 0; i < cycles; i++) {

			if (i != cycles - 1) {
				Globals.logDebug(this, "wavPCM " + i);
				for (int bp = 0; bp < 320; bp++) {
					buffer[bp] = out[buffPosition + bp];
				}
			} else {
				for (int bp = 0; bp < lastCyc; bp++) {
					buffer[bp] = out[buffPosition + bp];
				}
			}
			buffPosition = buffPosition + 320;

			final int compressedSize = settings.getEncodedSize();
			final byte[] compressed = new byte[compressedSize];

			try {
				synchronized (Speexrec.class) {
					Speexrec.encode(buffer, compressed);
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				Globals.logError(this, "sendBeepEncode" + e);
				e.printStackTrace();
				finalize();
			}
			//
			//
			outputQueue.add(compressed);

			if (outputQueue.size() < framesPerPacket) {
				continue;
			}

			final byte[] outputBuffer = new byte[1024];

			final PacketDataStream pds = new PacketDataStream(outputBuffer);

			while (!outputQueue.isEmpty()) {
				int flags = 0;
				flags |= mService.getCodec() << 5;
				outputBuffer[0] = (byte) flags;

				pds.rewind();
				// skip flags
				pds.next();
				seq += framesPerPacket;
				pds.writeLong(seq);
				for (int j = 0; j < framesPerPacket; ++j) {
					final byte[] tmp = outputQueue.poll();
					if (tmp == null) {
						break;
					}
					int head = (short) tmp.length;
					if (j < framesPerPacket - 1) {
						head |= 0x80;
					}

					pds.append(head);
					pds.append(tmp);
				}

				mService.sendUdpMessage(outputBuffer, pds.size());
			}
		}
		WavReader.play(wavePcm);
	}

	public short[] byteToShort(byte[] data, int length) {
		ByteBuffer byteBuffer = ByteBuffer.wrap(data).order(
				ByteOrder.LITTLE_ENDIAN)/* .order(ByteOrder.BIG_ENDIAN) */;
		ShortBuffer shortBuffer = byteBuffer.asShortBuffer();
		// shortBuffer.reset();
		short[] lastFrame = new short[length / 2];
		int i = 0;
		while (shortBuffer.hasRemaining()) {
			lastFrame[i] = shortBuffer.get();
			i++;
		}
		return lastFrame;
	}

	public byte[] shortToByte(short[] data) {
		byte[] byteData = new byte[data.length * 2];
		// for(int i = 0; i<data.length; i++){
		// byteData = toBytes(data[i]);
		// }
		ByteBuffer.wrap(byteData).order(ByteOrder.LITTLE_ENDIAN)
				.asShortBuffer().put(data);
		return byteData;
	}
}
