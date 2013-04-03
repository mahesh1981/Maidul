package com.radiorunt.services;

import com.radiorunt.utilities.Globals;

import net.sf.mumble.MumbleProto.Ping;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.os.SystemClock;
import android.util.Log;

public class PingAlarm extends BroadcastReceiver {

	private static RadioRuntConnection mc = null;
	private static boolean start = true;
	private final byte[] udpBuffer = new byte[9];
	private static WifiLock wifiLock;
	private static WifiManager wm;

	public PingAlarm() {

	}

	public PingAlarm(Context context, RadioRuntConnection mc,
			int timeoutInMiliSeconds) {
		Globals.logDebug(this, "PingAlarm started");
		this.mc = mc;
		Globals.logDebug(this, "PingAlarm start mc:" + mc);
		start = true;
		AlarmManager alarmMgr = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent(context, PingAlarm.class);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0,
				intent, 0);

		Globals.logDebug(this, "PingAlarm pending intent:" + pendingIntent);

		alarmMgr.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
				SystemClock.elapsedRealtime(), timeoutInMiliSeconds,
				pendingIntent);

		Globals.logDebug(this, "PingAlarm set");

		// Type: Ping
		udpBuffer[0] = MumbleProtocol.UDPMESSAGETYPE_UDPPING << 5;
	}

	public void setStopPingALarm(boolean start) {
		PingAlarm.start = start;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		if (mc == null) {
			return;
		}
		if (!start) {
			return;
		}
		WakeLockManager.acquire(context);

		// /////////////////////////////////////////////////////////////////
		// /////////////// Wifi wakelock acquire //////////////////////
		// /////////////////////////////////////////////////////////////////
		if (wifiLock != null && wifiLock.isHeld()) {
			wifiLock.release();
			wifiLock = null;
		}

		wm = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

		wifiLock = wm.createWifiLock(WifiManager.WIFI_MODE_FULL, "r Wifi");

		if (wm.isWifiEnabled()) {
			// if(wm.getWifiState() == WifiManager.WIFI_STATE_DISABLED){
			// wm.setWifiEnabled(true);
			// }
			wifiLock.acquire();
			Globals.logDebug(this, "WIFI LockManager acquire");
		}

		// /////////////////////////////////////////////////////////////////
		// /////////////// End Wifi wakelock acquire ///////////////////
		// /////////////////////////////////////////////////////////////////

		// here you can get the extras you passed in when creating the alarm
		// Bundle bundle = intent.getBundleExtra(ALARM_BUNDLE);
		Globals.logDebug(this, "=========Ping Alarm called========");
		final long timestamp = System.currentTimeMillis();

		// TCP
		final Ping.Builder p = Ping.newBuilder();
		p.setTimestamp(timestamp);
		Globals.logDebug(this, "send timestamp " + timestamp);
		Globals.logDebug(this, "onReceive mc:" + mc);
		mc.sendTcpMessage(MumbleProtocol.MessageType.Ping, p);
		Globals.logDebug(this, "PingAlarm TCP");

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
		Globals.logDebug(this, "PingAlarm UDP");

		// /////////////////////////////////////////////////////////////////
		// /////////////// Wifi wakelock release ////////////////////
		// /////////////////////////////////////////////////////////////////

		if (wm.isWifiEnabled()) {
			if (wifiLock != null && wifiLock.isHeld()) {
				wifiLock.release();
				Globals.logDebug(this, "WIFI LockManager release");
			}
			wifiLock = null;
		}

		// /////////////////////////////////////////////////////////////////
		// /////////////// End Wifi wakelock release/////////////////////
		// /////////////////////////////////////////////////////////////////

		WakeLockManager.release();
	}
}
