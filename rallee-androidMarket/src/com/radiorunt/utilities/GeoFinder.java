package com.radiorunt.utilities;

import java.util.List;
import java.util.ArrayList;

import com.radiorunt.businessobjects.ListOfCountries;

import android.content.Context;
import android.location.Geocoder;
import android.location.Address;

/**
 * Geo codes the input Location and provides an address associated with the
 * location
 * 
 */
public class GeoFinder {

	public List<String> geocode(Context context, double lat, double lng)
			throws Exception {

		// Get the android Geocoder
		Geocoder geocoder = new Geocoder(context);
		try {
			List<String> addresses = new ArrayList<String>();

			// Get the addresses corresponding to the input location
			List<Address> addr = geocoder.getFromLocation(lat, lng, 3);

			if (addr != null) {
				for (Address address : addr) {
					String placeName = address.getLocality();
					String featureName = address.getFeatureName();
					String country = address.getCountryName();
					String road = address.getThoroughfare();
					String locInfo = String.format("\n[%s] [%s] [%s] [%s]",
							placeName, featureName, road, country);
					addresses.add(locInfo);
					ListOfCountries.HOME_COUNTRY = country;
				}
			}

			return addresses;
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	public static String getZipCode(Context context, double lat, double lng)
			throws Exception {

		// Get the android Geocoder
		Geocoder geocoder = new Geocoder(context);
		try {
			List<String> addresses = new ArrayList<String>();

			// Get the addresses corresponding to the input location
			List<Address> addr = geocoder.getFromLocation(lat, lng, 3);

			if (addr != null) {
				for (Address address : addr) {
					String postal = address.getPostalCode();
					if (postal != null) {
						return postal;
					}
				}
			}
			return "";
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}

	}

}
