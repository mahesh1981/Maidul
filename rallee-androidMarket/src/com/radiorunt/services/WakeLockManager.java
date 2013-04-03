package com.radiorunt.services;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;

public abstract class WakeLockManager {
	private static WakeLock wakeLock;

	public static void acquire(Context ctx) {
		if (wakeLock != null) {
			wakeLock.release();
		}

		PowerManager pm = (PowerManager) ctx
				.getSystemService(Context.POWER_SERVICE);

		wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "radiorunt");

		try {
			wakeLock.acquire();
//			Log.i("radiorunt", "WakeLockManager acquire");

		} catch (Exception e) {
//			Log.e("WakeLockManager", "Error getting Lock: " + e.getMessage());

		}

	}

	public static void release() {
		if (wakeLock != null && wakeLock.isHeld()) {
			wakeLock.release();
		}
		wakeLock = null;

//		Log.i("pingRallee", "WakeLockManager release");
	}
}