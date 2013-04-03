package com.radiorunt.services.audio;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import android.content.Context;
import android.util.Log;

import com.radiorunt.businessobjects.RRUser;
import com.radiorunt.services.MumbleProtocol;
import com.radiorunt.services.PacketDataStream;
import com.radiorunt.utilities.Globals;
import com.radiorunt.utilities.Settings;
import com.radiorunt.utilities.jni.Speex;
import com.radiorunt.utilities.jni.Speex.BufferPacket;

/**
 * Thread safe buffer for audio data. Implements audio queue and decoding.
 * 
 */
public class AudioUser {
	public interface PacketReadyHandler {
		public void packetReady(AudioUser user);
	}

	private final Queue<Speex.BufferPacket> normalBuffer;

	private final Queue<byte[]> dataArrayPool = new ConcurrentLinkedQueue<byte[]>();
	short[] lastFrame = new short[MumbleProtocol.FRAME_SIZE];
	private final RRUser user;

	private int missedFrames = 0;

	public AudioUser(final RRUser user, int speexQuality, Context c) {

		Settings settings = new Settings(c);
		Speex.open(settings.getAudioQuality());
		// Integer codec_status;
		// codec_status = Speex.getStatus();
		this.user = user;

		normalBuffer = new ConcurrentLinkedQueue<Speex.BufferPacket>();

		Globals.logInfo(this, "Created");
	}

	public boolean addFrameToBuffer(final PacketDataStream pds,
			final PacketReadyHandler readyHandler) {

		final int packetHeader = pds.next();

		// Make sure this is supported voice packet.
		//
		// (Yes this check is included in MumbleConnection as well but I believe
		// it should be made here since the decoding support is built into this
		// class anyway. In theory only this class needs to know what packets
		// can be decoded.)
		final int type = (packetHeader >> 5) & 0x7;
		if (type != MumbleProtocol.UDPMESSAGETYPE_UDPVOICESPEEX) {
			return false;
		}

		/* long session = */pds.readLong();
		final long sequence = pds.readLong();

		int dataHeader;
		int frameCount = 0;

		byte[] data = null;
		do {
			dataHeader = pds.next();
			final int dataLength = dataHeader & 0x7f;
			if (dataLength > 0) {

				// acquire data array for each packet.
				// They are released when dequeueing them fromt he buffer.
				data = acquireDataArray();
				pds.dataBlock(data, dataLength);

				final BufferPacket jbp = new BufferPacket();
				jbp.data = data;
				jbp.len = dataLength;

				normalBuffer.add(jbp);

				readyHandler.packetReady(this);
				frameCount++;

			}
		} while ((dataHeader & 0x80) > 0 && pds.isValid());

		return true;
	}

	public void freeDataArray(final byte[] data) {
		dataArrayPool.add(data);
	}

	public RRUser getUser() {
		return this.user;
	}

	/**
	 * Checks if this user has frames and sets lastFrame.
	 * 
	 * @return
	 */
	public boolean hasFrame() {
		byte[] data = null;
		int dataLength = 0;

		BufferPacket jbp;

		jbp = normalBuffer.poll();
		if (jbp != null) {
			data = jbp.data;
			dataLength = jbp.len;
			missedFrames = 0;
		} else {
			missedFrames++;
			return false;
		}

		Speex.decode(data, lastFrame, dataLength);

		if (data != null) {
			freeDataArray(data);
		}

		return (missedFrames < 10);
	}

	private byte[] acquireDataArray() {
		byte[] data = dataArrayPool.poll();

		if (data == null) {
			data = new byte[128];
		}

		return data;
	}

	@Override
	protected final void finalize() {

	}
}
