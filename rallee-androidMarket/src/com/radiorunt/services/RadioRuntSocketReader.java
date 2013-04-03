package com.radiorunt.services;

import java.io.IOException;

import android.content.Context;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;

import com.radiorunt.utilities.Globals;

/**
 * Provides the general structure for the socket readers.
 */
public abstract class RadioRuntSocketReader {
	private final Object monitor;
	private boolean running;
	private final Thread thread;
	public boolean suspendSocketReaderFlag;
	// WakeLock wakelockSocket;
	// PowerManager powermSocket;

	protected Runnable runnable = new Runnable() {

		@Override
		public void run() {

			try {
				while (isRunning()) {
					try {
						// wakelockSocket.acquire();
						// Log.i("radiorunt",
						// "RadioRuntSocketReader:run/acquire 1000");
						// Log.i("radiorunt",
						// "RadioRuntSocketReader:run/acquire");
					} catch (Exception e) {
						e.printStackTrace();
					}
					process();
					// if (wakelockSocket != null && wakelockSocket.isHeld() ==
					// true) {
					// try {
					// //wakelockSocket.release();
					// //Log.i("radiorunt",
					// "RadioRuntSocketReader:run/release");
					// } catch (Exception e) {
					// e.printStackTrace();
					// }
					// }
				}
			} catch (final IOException ex) {
				// if (!RadioRuntService.tempNetState) {
				// return;
				// }
				// If we aren't running, exception is expected.
				if (isRunning()) {
					RadioRuntService.tcpTimeOuted = true;

					// RadioRuntService.tempNetState = false;
					Globals.logError(this, "Error reading socket ", ex);

				}
			} finally {
				running = false;
				synchronized (monitor) {
					monitor.notifyAll();
				}
			}
		}
	};

	/**
	 * Constructs a new Reader instance
	 * 
	 * @param monitor
	 *            The monitor that should be signaled when the thread is
	 *            quitting.
	 */
	public RadioRuntSocketReader(final Object monitor, final String name,
			Context ctx) {
		this.monitor = monitor;
		this.running = true;
		this.thread = new Thread(runnable, name);
		// powermSocket = (PowerManager) ctx
		// .getSystemService(Context.POWER_SERVICE);
		// wakelockSocket =
		// powermSocket.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "cpulock");
		// Log.i("radiorunt", "RadioRuntSocketReader:created");
	}

	/**
	 * The condition that must be fulfilled for the reader to continue running.
	 * 
	 * @return True while the reader should keep processing the socket.
	 */
	public boolean isRunning() {
		return running && thread.isAlive();
	}

	public void start() {
		this.thread.start();
	}

	public void stop() {
		this.thread.interrupt();
		try {
			this.thread.join();
		} catch (final InterruptedException e) {
			Globals.logWarn(this, "Socket thread join interrupted", e);
		}
		Globals.logDebug(this, "RadioRuntSocketReader:stoped/release");
		// if (wakelockSocket != null && wakelockSocket.isHeld() == true) {
		// try {
		// wakelockSocket.release();
		// } catch (Exception e) {
		// e.printStackTrace();
		// }
		// }
	}

	/**
	 * A single processing step that reads and processes a message from the
	 * socket.
	 * 
	 * @throws IOException
	 */
	protected abstract void process() throws IOException;
}
