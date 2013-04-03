package com.radiorunt.utilities;

import java.util.Iterator;

import android.content.Context;
import android.location.Criteria;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

public class UserLocation {

	/**
	 * If GPS is enabled. Use minimal connected satellites count.
	 */
	private static final int min_gps_sat_count = 5;

	/**
	 * Iteration step time.
	 */
	private static final int iteration_timeout_step = 500;

	LocationResult locationResult;
	private Location bestLocation = null;
	private Handler handler = new Handler();
	private LocationManager myLocationManager;
	public Context context;

	private boolean gps_enabled = false;

	private int counts = 0;
	private int sat_count = 0;

	private Runnable showTime = new Runnable() {

		@Override
		public void run() {
			boolean stop = false;
			counts++;
//			System.out.println("counts=" + counts);

			// if timeout (30 sec) exceeded, stop trying
			if (counts > 60) {
				stop = true;
			}

			// update last best location
			bestLocation = getLocation(context);

			// if location is not ready or don`t exists, try again
			if (bestLocation == null) {
//				System.out.println("BestLocation not ready, continue to wait");
				handler.postDelayed(this, iteration_timeout_step);
			} else {
				// if best location is known, calculate if we need to continue
				// to look for better location
				// if gps is enabled and min satellites count has not been
				// connected or min check count is smaller then 4 (2 sec)
				if (stop == false && !needToStop()) {
//					System.out.println("Connected " + sat_count
//							+ " sattelites. continue waiting..");
					handler.postDelayed(this, iteration_timeout_step);
				} else {
					System.out
							.println("#########################################");
					System.out
							.println("BestLocation finded return result to main. sat_count="
									+ sat_count);
					System.out
							.println("#########################################");

					// removing all updates and listeners
					myLocationManager.removeUpdates(gpsLocationListener);
					myLocationManager.removeUpdates(networkLocationListener);
					myLocationManager
							.removeGpsStatusListener(gpsStatusListener);
					sat_count = 0;

					// send best location to locationResult
					locationResult.gotLocation(bestLocation);
				}
			}
		}
	};

	/**
	 * Determine if continue to try to find best location
	 */
	private Boolean needToStop() {
		if (gps_enabled) {
			if (counts <= 4) {
				return false;
			}
			if (sat_count < min_gps_sat_count) {
				// if 20-25 sec and 3 satellites found then stop
				if (counts >= 20 && sat_count >= 3) {
					return true;
				}
				return false;
			}
		}
		return true;
	}

	/**
	 * Best location abstract result class
	 */
	public static abstract class LocationResult {
		public abstract void gotLocation(Location location);
	}

	/**
	 * Initialize starting values and starting best location listeners
	 * 
	 * @param Context
	 *            ctx
	 * @param LocationResult
	 *            result
	 */
	public void init(Context ctx, LocationResult result) {
		context = ctx;
		locationResult = result;

		myLocationManager = (LocationManager) context
				.getSystemService(Context.LOCATION_SERVICE);

		gps_enabled = myLocationManager
				.isProviderEnabled(LocationManager.GPS_PROVIDER);

		bestLocation = null;
		counts = 0;

		// turning on location updates
		myLocationManager.requestLocationUpdates("network", 0, 0,
				networkLocationListener);
		myLocationManager.requestLocationUpdates("gps", 0, 0,
				gpsLocationListener);
		myLocationManager.addGpsStatusListener(gpsStatusListener);

		// starting best location finder loop
		handler.postDelayed(showTime, iteration_timeout_step);
	}

	/**
	 * GpsStatus listener. OnChainged counts connected satellites count.
	 */
	public final GpsStatus.Listener gpsStatusListener = new GpsStatus.Listener() {
		@Override
		public void onGpsStatusChanged(int event) {

			if (event == GpsStatus.GPS_EVENT_SATELLITE_STATUS) {
				try {
					// Check number of satellites in list to determine fix state
					GpsStatus status = myLocationManager.getGpsStatus(null);
					Iterable<GpsSatellite> satellites = status.getSatellites();

					sat_count = 0;

					Iterator<GpsSatellite> satI = satellites.iterator();
					while (satI.hasNext()) {
						GpsSatellite satellite = satI.next();
//						System.out.println("Satellite: snr="
//								+ satellite.getSnr() + ", elevation="
//								+ satellite.getElevation());
						sat_count++;
					}
				} catch (Exception e) {
					e.printStackTrace();
					sat_count = min_gps_sat_count + 1;
				}

//				System.out.println("#### sat_count = " + sat_count);
			}
		}
	};

	/**
	 * Gps location listener.
	 */
	public final LocationListener gpsLocationListener = new LocationListener() {
		@Override
		public void onLocationChanged(Location location) {
			bestLocation = location;
			Globals.logDebug(this,
					"location changed - " + location.getLatitude() + ":"
							+ location.getLongitude());
			if (context != null) {
				LocationManager lm = (LocationManager) context
						.getSystemService(Context.LOCATION_SERVICE);
				lm.removeUpdates(this);
				lm.removeGpsStatusListener(gpsStatusListener);
			}
		}

		@Override
		public void onProviderDisabled(String provider) {
		}

		@Override
		public void onProviderEnabled(String provider) {
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
		}
	};

	/**
	 * Network location listener.
	 */
	public final LocationListener networkLocationListener = new LocationListener() {
		@Override
		public void onLocationChanged(Location location) {
			bestLocation = location;
			Globals.logDebug(this,
					"network location changed - " + location.getLatitude()
							+ ":" + location.getLongitude());
			if (context != null) {
				LocationManager lm = (LocationManager) context
						.getSystemService(Context.LOCATION_SERVICE);
				lm.removeUpdates(this);
			}
		}

		@Override
		public void onProviderDisabled(String provider) {
		}

		@Override
		public void onProviderEnabled(String provider) {
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
		}
	};

	/**
	 * Returns best location using LocationManager.getBestProvider()
	 * 
	 * @param context
	 * @return Location|null
	 */
	public static Location getLocation(Context context) {
//		System.out.println("getLocation()");

		// fetch last known location and update it
		try {
			LocationManager lm = (LocationManager) context
					.getSystemService(Context.LOCATION_SERVICE);

			Criteria criteria = new Criteria();
			criteria.setPowerRequirement(Criteria.NO_REQUIREMENT);
			criteria.setSpeedRequired(false);
			criteria.setAccuracy(Criteria.ACCURACY_COARSE);
			criteria.setAltitudeRequired(false);
			criteria.setBearingRequired(false);
			criteria.setCostAllowed(true);
			String strLocationProvider = lm.getBestProvider(criteria, true);

//			System.out.println("strLocationProvider=" + strLocationProvider);
			Location location = lm.getLastKnownLocation(strLocationProvider);
			if (location != null) {
				return location;
			}
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	// Toast.make
}
