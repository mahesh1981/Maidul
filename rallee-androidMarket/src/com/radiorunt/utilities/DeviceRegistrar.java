package com.radiorunt.utilities;

import java.io.IOException;
import java.net.URI;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import android.content.Context;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.radiorunt.businessobjects.RegInfo;
import com.radiorunt.facebook.Utility;
import com.radiorunt.utilities.Settings;

public class DeviceRegistrar {
	public static final String SENDER_ID = "397536487190"/* "radioruntpushnotification@gmail.com" */;
	public static final String KEY_DEVICE_REGISTRATION_ID = "deviceRegistrationID";
	private static final String REGISTER_URI = "/register";
	private static final String UNREGISTER_URI = "/unregister";
	private static final String TAG = "C2DM";

	public static void registerWithServer(Context context, String registrationId) {
		if (RalleeApp.getInstance().getRalleeUID().equals("ralleeUID")) {
			return;
		}
		String deviceId = RalleeApp.getInstance().getRalleeUID();
		if (deviceId == null)
			return;
		// connect with 3rd party server and register the device
		// TODO: do this on a thread
		// try {
		// Log.d(TAG, "attempting to register with 3rd party app server...");
		// HttpClient client = new DefaultHttpClient();
		// HttpGet request = new HttpGet();
		// request.setURI(new URI(APP_SERVER_URL + REGISTER_URI + "?deviceId=" +
		// deviceId + "registrationId=" + registrationId));
		// HttpResponse response = client.execute(request);
		// StatusLine status = response.getStatusLine();
		// if (status == null)
		// throw new IllegalStateException("no status from request");
		// if (status.getStatusCode() != 200)
		// throw new IllegalStateException(status.getReasonPhrase());
		// } catch (Exception e) {
		// Log.e(TAG, "unable to register: " + e.getMessage());
		// //TODO: notify the user
		// return;
		// }

		String jsonString = "";
		ObjectMapper mapper = new ObjectMapper();
		RegInfo regInfo = new RegInfo();
		regInfo.deviceId = deviceId;
		regInfo.registrationId = registrationId;
		try {
			jsonString = mapper.writeValueAsString(regInfo);
		} catch (JsonGenerationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		RRServerProxyHelper.startRegisterToAppService(context, jsonString);

		// store for later
		Settings settings = new Settings(context);
		settings.addKey(context, KEY_DEVICE_REGISTRATION_ID, registrationId);
//		Log.d(TAG, "successfully registered device with 3rd party app server");
	}

	public static void unregisterWithServer(Context context,
			String registrationId) {
		if (RalleeApp.getInstance().getRalleeUID().equals("ralleeUID")) {
			return;
		}
		String deviceId = RalleeApp.getInstance().getRalleeUID();
		if (deviceId == null)
			return;
		// connect with 3rd party server and unregister the device
		// TODO: do this on a thread
		// try {
		// Log.d(TAG, "attempting to unregister with 3rd party app server...");
		// HttpClient client = new DefaultHttpClient();
		// HttpGet request = new HttpGet();
		// request.setURI(new URI(APP_SERVER_URL + UNREGISTER_URI + "?deviceId="
		// + deviceId));
		// HttpResponse response = client.execute(request);
		// StatusLine status = response.getStatusLine();
		// if (status == null)
		// throw new IllegalStateException("no status from request");
		// if (status.getStatusCode() != 200)
		// throw new IllegalStateException(status.getReasonPhrase());
		// } catch (Exception e) {
		// Log.e(TAG, "unable to unregister: " + e.getMessage());
		// //TODO: notify the user
		// return;
		// }
		String jsonString = "";
		ObjectMapper mapper = new ObjectMapper();
		RegInfo regInfo = new RegInfo();
		regInfo.deviceId = deviceId;
		regInfo.registrationId = "";
		try {
			jsonString = mapper.writeValueAsString(regInfo);
		} catch (JsonGenerationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		RRServerProxyHelper.startUnregisterToAppService(context, jsonString);

		// remove local key so app doesn't try to accidentally use it
		Settings settings = new Settings(context);
		settings.removeKey(context, KEY_DEVICE_REGISTRATION_ID);
//		Log.d(TAG, "succesfully unregistered with 3rd party app server");
	}
}
