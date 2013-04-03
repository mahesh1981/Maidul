package com.radiorunt.utilities.jni;

public class Speex {

	static {
		System.loadLibrary("speex_jni");
	}

	public static class BufferPacket {
		public byte[] data;
		public int len;
		public int timestamp;
		public int span;
		public short sequence;
		public int user_data;
	}

	public static native void open(int quality);

	public static native int decode(byte[] in, short[] out, int length);

	public static native void close();

	public static native int getDecoderFrameSize();

	public static native int getStatus();
}
