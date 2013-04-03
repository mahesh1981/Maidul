package com.radiorunt.utilities;

import java.util.List;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

public class RRLocationManager implements LocationListener {
	private static final int MINIMUM_DISTANCE_CHANGE_FOR_UPDATES = 1000 * 5; // in
																				// meters
	private static final int MINIMUM_GPS_DISTANCE_CHANGE_FOR_UPDATES = 100; // in
																			// meters
	public static final int MSG_LOCATION_AQUIRED = 0;
	public static final int MSG_NO_PROVIDER_AVAILABLE = 1;
	public static final int MSG_ENABLE_GPS = 2;
	final int TWO_MINUTES = 1000 * 60 * 2;

	Context context;
	LocationManager locationManager;
	Location location;
	String provider = null;
	Criteria criteria;
	Handler mHandler;

	public RRLocationManager(Context ctx, Handler handler/*
														 * , LocationManager lm,
														 * Location loc
														 */) {
		super();
		this.context = ctx;
		// this.locationManager = lm;
		this.locationManager = (LocationManager) context
				.getSystemService(Context.LOCATION_SERVICE);
		this.location = null;
		mHandler = handler;

		List<String> providers = locationManager.getProviders(true);
		/*
		 * Code for enable any provider for gettings loaction *
		 */
		boolean isGPSAvailable = false;
		boolean isFineLocating = (new Settings(context.getApplicationContext()))
				.isFineLocating();
		for (String p : providers) {
			Globals.logDebug(this, "provider available: " + p);
			if (p.equals(LocationManager.GPS_PROVIDER)) {
				isGPSAvailable = true;
			}
			/*
			 * Code for enable any provider for gettings loaction add this by
			 * Maidul Islam *
			 */
			else if (p.equals(LocationManager.PASSIVE_PROVIDER)) {
				isGPSAvailable = true;
			} else if (p.equals(LocationManager.NETWORK_PROVIDER)) {
				isGPSAvailable = true;
			}
			/*
			 * End the code
			 * 
			 * *
			 */

		}

		criteria = new Criteria();
		criteria.setPowerRequirement(Criteria.NO_REQUIREMENT);
		criteria.setSpeedRequired(false);
		if (isFineLocating) {
			criteria.setAccuracy(Criteria.ACCURACY_FINE);
			// Toast.makeText(context, "Accuracy fine", 500).show();
		} else {
			criteria.setAccuracy(Criteria.ACCURACY_COARSE);
			// Toast.makeText(context, "Accuracy coarse", 500).show();
		}
		criteria.setAltitudeRequired(false);
		criteria.setBearingRequired(false);
		criteria.setCostAllowed(true);

		if (!isGPSAvailable && isFineLocating) {
			mHandler.sendEmptyMessage(MSG_ENABLE_GPS);
		}

		provider = locationManager.getBestProvider(criteria, true);
		if (provider != null) {
			location = locationManager.getLastKnownLocation(provider);
			if (location != null) {
				mHandler.sendEmptyMessage(MSG_LOCATION_AQUIRED);
			}
		} else {
			Globals.logDebug(this, "no provider ");
			mHandler.sendEmptyMessage(MSG_NO_PROVIDER_AVAILABLE);
		}
		// ah I see; this is what is causing the GPS to be turned on
		for (String p : providers) {
			if (p.equals(LocationManager.GPS_PROVIDER)) {
				if (isFineLocating) {
					locationManager.requestLocationUpdates(p, 0,
							MINIMUM_GPS_DISTANCE_CHANGE_FOR_UPDATES, this);
				}
			}
			/*
			 * Add by Maidul Islam
			 * 
			 * *
			 */

			else if (p.equals(LocationManager.PASSIVE_PROVIDER)) {
				locationManager.requestLocationUpdates(p, 0,
						MINIMUM_DISTANCE_CHANGE_FOR_UPDATES, this);
			} else if (p.equals(LocationManager.NETWORK_PROVIDER)) {
				locationManager.requestLocationUpdates(p, 0,
						MINIMUM_DISTANCE_CHANGE_FOR_UPDATES, this);
			} else {
				locationManager.requestLocationUpdates(p, 0,
						MINIMUM_DISTANCE_CHANGE_FOR_UPDATES, this);
			}
			/*
			 * close by Maidul Islam
			 * 
			 * *
			 */
		}
	}

	public Location getLastKnownLocation() {
		return this.location;
	}

	public void removeUpdates() {
		this.locationManager.removeUpdates(this);
	}

	@Override
	public void onLocationChanged(Location newLoc) {
		// TODO Auto-generated method stub
		if (isBetterLocation(newLoc, location)) {
			location = newLoc;

			// Toast.makeText(context, "Better location from: " +
			// location.getProvider() +"\n accuracy: "+location.getAccuracy(),
			// 500).show();
			mHandler.sendEmptyMessage(MSG_LOCATION_AQUIRED);
		}

		if (newLoc != null) {
			// Toast.makeText(context, "Worse location from: " +
			// newLoc.getProvider() +"\n accuracy: "+newLoc.getAccuracy(),
			// 500).show();
		}

	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
		this.provider = this.locationManager.getBestProvider(criteria, true);
	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
		this.provider = this.locationManager.getBestProvider(criteria, true);
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
		this.provider = this.locationManager.getBestProvider(criteria, true);
	}

	protected boolean isBetterLocation(Location newLocation,
			Location currentBestLocation) {
		if (currentBestLocation == null) {
			// A new location is always better than no location
			return true;
		}

		// Check whether the new location fix is newer or older
		long timeDelta = newLocation.getTime() - currentBestLocation.getTime();
		boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
		boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
		boolean isNewer = timeDelta > 0;

		// If it's been more than two minutes since the current location, use
		// the new location
		// because the user has likely moved
		if (isSignificantlyNewer) {
			return true;
			// If the new location is more than two minutes older, it must be
			// worse
		} else if (isSignificantlyOlder) {
			return false;
		}

		// Check whether the new location fix is more or less accurate
		int accuracyDelta = (int) (newLocation.getAccuracy() - currentBestLocation
				.getAccuracy());
		boolean isLessAccurate = accuracyDelta > 0;
		boolean isMoreAccurate = accuracyDelta < 0;
		boolean isSignificantlyLessAccurate = accuracyDelta > 200;

		// Check if the old and new location are from the same provider
		boolean isFromSameProvider = isSameProvider(newLocation.getProvider(),
				currentBestLocation.getProvider());

		// Determine location quality using a combination of timeliness and
		// accuracy
		if (isMoreAccurate) {
			return true;
		} else if (isNewer && !isLessAccurate) {
			return true;
		} else if (isNewer && !isSignificantlyLessAccurate
				&& isFromSameProvider) {
			return true;
		}
		return false;
	}

	/** Checks whether two providers are the same */
	private boolean isSameProvider(String provider1, String provider2) {
		if (provider1 == null) {
			return provider2 == null;
		}
		return provider1.equals(provider2);
	}

}
