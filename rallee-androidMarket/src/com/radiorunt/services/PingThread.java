package com.radiorunt.services;

import com.radiorunt.utilities.Globals;

import android.content.Context;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import net.sf.mumble.MumbleProto.Ping;

class PingThread implements Runnable {
	private boolean running = true;
	private final RadioRuntConnection mc;
	private final byte[] udpBuffer = new byte[9];
	String name; // name of thread
	public boolean suspendFlag = false;
	Thread t;
	WakeLock wakelockPing;
	PowerManager powermPing;
	Context ctx;

	public PingThread(String threadname, final RadioRuntConnection mc_,
			Context ctx) {
		this.mc = mc_;
		// Type: Ping
		udpBuffer[0] = MumbleProtocol.UDPMESSAGETYPE_UDPPING << 5;
		name = threadname;
		t = new Thread(this, name);
//		System.out.println("New thread: " + t);
		suspendFlag = false;
		t.start(); // Start the thread
		this.ctx = ctx;
		// powermPing = (PowerManager)
		// ctx.getSystemService(Context.POWER_SERVICE);
		// wakelockPing = powermPing.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
		// "cpulock");
	}

	@Override
	public final void run() {
		while (running && mc.isConnectionAlive()) {
			try {
				try {
					wakelockPing.acquire();
					Globals.logDebug(this, "ping acquire");
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				final long timestamp = System.currentTimeMillis();

				// TCP
				final Ping.Builder p = Ping.newBuilder();
				p.setTimestamp(timestamp);
				mc.sendTcpMessage(MumbleProtocol.MessageType.Ping, p);
				Globals.logDebug(this, "Ping TCP");

				// UDP
				udpBuffer[1] = (byte) ((timestamp >> 56) & 0xFF);
				udpBuffer[2] = (byte) ((timestamp >> 48) & 0xFF);
				udpBuffer[3] = (byte) ((timestamp >> 40) & 0xFF);
				udpBuffer[4] = (byte) ((timestamp >> 32) & 0xFF);
				udpBuffer[5] = (byte) ((timestamp >> 24) & 0xFF);
				udpBuffer[6] = (byte) ((timestamp >> 16) & 0xFF);
				udpBuffer[7] = (byte) ((timestamp >> 8) & 0xFF);
				udpBuffer[8] = (byte) ((timestamp) & 0xFF);

				mc.sendUdpMessage(udpBuffer, udpBuffer.length, true);
				Globals.logDebug(this, "Ping UDP");

				// if (wakelockPing != null && wakelockPing.isHeld() == true) {
				// try {
				// wakelockPing.release();
				// Log.i("radiorunt", "ping release");
				// } catch (Exception e) {
				// // TODO Auto-generated catch block
				// //e.printStackTrace();
				// }
				// }
				Thread.sleep(120000);
				synchronized (this) {
					while (suspendFlag) {
						wait();
					}
				}
			} catch (final InterruptedException e) {
				e.printStackTrace();
				running = false;
			}
		}
	}

	void mysuspend() {
		suspendFlag = true;
	}

	synchronized void myresume() {
		suspendFlag = false;
		notify();
	}
}
