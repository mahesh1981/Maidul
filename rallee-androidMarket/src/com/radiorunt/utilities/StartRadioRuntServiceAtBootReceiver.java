package com.radiorunt.utilities;

import com.radiorunt.services.RadioRuntService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class StartRadioRuntServiceAtBootReceiver extends BroadcastReceiver {

	Settings settings;

	@Override
	public void onReceive(Context context, Intent intent) {
		settings = new Settings(context);
		Globals.logDebug(this, "AtBootReceiver");

		if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {

			Intent serviceIntent = new Intent();
			serviceIntent = settings.getConnectionIntent();
			Globals.logDebug(this, "" + serviceIntent);
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			context.startService(serviceIntent);
		}
	}
}