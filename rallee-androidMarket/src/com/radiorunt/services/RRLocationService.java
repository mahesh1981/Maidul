package com.radiorunt.services;

import com.radiorunt.utilities.RRLocationServiceReceiver;
import com.radiorunt.utilities.UserLocation;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.Toast;

public class RRLocationService extends IntentService {
	public static final String PARAM_OUT_MSG = "statuses_stream_out";
	boolean actionCompleted = false;

	protected LocationManager locationManager;
	private static Location bestLocation = null;

	public String coordMsg;

	UserLocation userLoc = new UserLocation();

	public RRLocationService() {
		super("RRLocationService");
	}

	public class BestLocationResult extends UserLocation.LocationResult {

		public void gotLocation(Location location) {
			bestLocation = location;
			if (bestLocation != null) {
				double lat = bestLocation.getLatitude();
				double lng = bestLocation.getLongitude();
				RRLocationService.this.notifyIntent(lat, lng);
			}
		}

	}

	public void notifyIntent(double lat, double lng) {
		String LatLng = Double.toString(lat) + "," + Double.toString(lng);
		Intent broadcastIntent = new Intent();
		broadcastIntent.setAction(RRLocationServiceReceiver.ACTION_RESP);
		broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
		broadcastIntent.putExtra(PARAM_OUT_MSG, LatLng);
		sendBroadcast(broadcastIntent);
		// Toast.makeText(RRLocationService.this.getApplicationContext(),
		// LatLng,
		// Toast.LENGTH_LONG);
	}

	@Override
	protected void onHandleIntent(Intent arg0) {

		String location_context = Context.LOCATION_SERVICE;
		locationManager = (LocationManager) getSystemService(location_context);

		Bundle extras = arg0.getExtras();
		boolean isNecessaryLocationUpdate = extras
				.getBoolean("isNecessaryLocationUpdate");
		Criteria criteria = extras.getParcelable("criteria");

		coordMsg = getCurrentLocation(isNecessaryLocationUpdate, criteria);

		Intent broadcastIntent = new Intent();
		if (actionCompleted)
			broadcastIntent.setAction(RRLocationServiceReceiver.ACTION_RESP);
		else
			broadcastIntent.setAction(RRLocationServiceReceiver.ACTION_FAILED);
		broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
		broadcastIntent.putExtra(PARAM_OUT_MSG, coordMsg);
		sendBroadcast(broadcastIntent);

	}

	protected String getCurrentLocation(boolean locationUpdate, Criteria crit) {

		String LatLng = null;

		if (!locationUpdate) {
			String bestProvider = locationManager.getBestProvider(crit, true);
			if (bestProvider != null) {
				bestLocation = locationManager
						.getLastKnownLocation(bestProvider);
			}
		} else {
			userLoc.init(this, new BestLocationResult());
		}

		if (bestLocation != null) {
			double lat = bestLocation.getLatitude();
			double lng = bestLocation.getLongitude();
			LatLng = Double.toString(lat) + "," + Double.toString(lng);
			actionCompleted = true;
		} else {

			LatLng = "No Location";
		}

		return LatLng;
	}

}
