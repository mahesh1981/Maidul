package com.radiorunt.utilities.jni;

public class Speexrec {

	static {
		System.loadLibrary("speexrec_jni");
	}

	// encoder functions
	public static native void open(int quality);

	public static native int encode(short[] in, byte[] out);

	public static native void close();

	public static native int getEncoderFrameSize();

	public static native int getStatus();

	// Aec functions

	// aec init status; 0 - destroyed; 1 - initialized
	public static native int getAecStatus();

	// initialization of aec
	public static native void initEcho(int frameSize, int filterLength);

	// methods for asynchronous threads
	public static native void speexEchoPlayback(short[] echoFrame);

	public static native void speexEchoCapture(short[] in, short[] out);

	public static native void speexEchoCaptureEncode(short[] in, byte[] out,
			int length);

	// method for synchronously input play and capture frames
	public static native void echoCancellation(short[] rec, short[] play,
			short[] out);

	// method for synchronously input play, capture frames, reduse noise and
	// encode voice
	public static native void echoCancellationEncode(short[] rec, short[] play,
			byte[] encoded);

	// noise reduction method
	public static native void speexPreprocess(short[] frame);

	// destroying aec
	public static native void destroyEcho();

}
