package com.radiorunt.utilities;

import java.util.List;

import com.radiorunt.services.RRLocationService;
import com.radiorunt.services.RadioRuntService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

public class RRLocationServiceReceiver extends BroadcastReceiver {
	public static final String ACTION_RESP = "LOCATION_MESSAGE_PROCESSED";
	public static final String ACTION_FAILED = "LOCATION_MESSAGE_FAILED";
	private GeoFinder geoFind;
	private Handler mHandler = new Handler();

	@Override
	public void onReceive(Context context, final Intent intent) {
		if (intent.hasExtra(RRLocationService.PARAM_OUT_MSG)) {

			String location = intent
					.getStringExtra(RRLocationService.PARAM_OUT_MSG);
			Globals.logDebug(this, "Location " + location);
			String[] coordinates = location.split(",");
			double lat = Double.parseDouble(coordinates[0]);
			double lng = Double.parseDouble(coordinates[1]);

			geoFind = new GeoFinder();
			List<String> address;
			if (location != null && RadioRuntService.tempNetState) {
				// address = geoFind.geocode(context, lat, lng);
				// Log.i("msgLocation", address.toString());
			} else {
				Globals.logDebug(this, "Location is NULL");
				final Context mContext = context;
				mHandler.post(new Runnable() {

					@Override
					public void run() {
						try {
							Thread.sleep(2000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						mContext.sendBroadcast(intent);
					}
				});

			}

		}
	}

}
