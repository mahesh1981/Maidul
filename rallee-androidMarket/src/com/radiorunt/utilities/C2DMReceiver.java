/***
	Copyright (c) 2010 CommonsWare, LLC
	
	Licensed under the Apache License, Version 2.0 (the "License"); you may
	not use this file except in compliance with the License. You may obtain
	a copy of the License at
		http://www.apache.org/licenses/LICENSE-2.0
	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.
 */

package com.radiorunt.utilities;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;

import com.google.android.c2dm.C2DMBaseReceiver;
import com.radiorunt.R;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.radiorunt.activities.DbContentProvider;
import com.radiorunt.activities.HomeActivity;
import com.radiorunt.activities.LogInActivity;
import com.radiorunt.businessobjects.RRMissedCall;
import com.radiorunt.businessobjects.RRPushMessage;
import com.radiorunt.businessobjects.RRPushMessagePayload;
import com.radiorunt.businessobjects.RRUser;
import com.radiorunt.facebook.Utility;
import com.radiorunt.utilities.DeviceRegistrar;

public class C2DMReceiver extends C2DMBaseReceiver {
	private static final String TAG = "C2DM";
	private Settings settings;

	public C2DMReceiver() {
		// send the email address you set up earlier
		super(DeviceRegistrar.SENDER_ID);
	}

	@Override
	public void onRegistered(Context context, String registrationId)
			throws IOException {
//		Log.d(TAG, "successfully registered with C2DM server; registrationId: "
//				+ registrationId);

		DeviceRegistrar.registerWithServer(context, registrationId);
	}

	@Override
	public void onUnregistered(Context context) {
//		Log.d(TAG, "successfully unregistered with C2DM server");
		Settings settings = new Settings(context);
		// String deviceRegistrationID = settings.getKey(context,
		// DeviceRegistrar.KEY_DEVICE_REGISTRATION_ID);
		if (!RalleeApp.getInstance().getRalleeUID().equals("ralleeUID")) {
			DeviceRegistrar.unregisterWithServer(context, RalleeApp
					.getInstance().getRalleeUID());
		}
	}

	@Override
	public void onError(Context context, String errorId) {
		// notify the user
//		Log.e(TAG, "error with C2DM receiver: " + errorId);

		if ("ACCOUNT_MISSING".equals(errorId)) {
			// no Google account on the phone; ask the user to open the account
			// manager and add a google account and then try again
			// TODO

		} else if ("AUTHENTICATION_FAILED".equals(errorId)) {
			// bad password (ask the user to enter password and try. Q: what
			// password - their google password or the sender_id password? ...)
			// i _think_ this goes hand in hand with google account; have them
			// re-try their google account on the phone to ensure it's working
			// and then try again
			// TODO

		} else if ("TOO_MANY_REGISTRATIONS".equals(errorId)) {
			// user has too many apps registered; ask user to uninstall other
			// apps and try again
			// TODO

		} else if ("INVALID_SENDER".equals(errorId)) {
			// this shouldn't happen in a properly configured system
			// TODO: send a message to app publisher?, inform user that service
			// is down

		} else if ("PHONE_REGISTRATION_ERROR".equals(errorId)) {
			// the phone doesn't support C2DM; inform the user
			// TODO

		} // else: SERVICE_NOT_AVAILABLE is handled by the super class and does
			// exponential backoff retries

	}

	@Override
	protected void onMessage(Context context, Intent intent) {
		Bundle extras = intent.getExtras();
		if (extras != null) {
			// parse the message and do something with it.
			// For example, if the server sent the payload as
			// "data.message=xxx", here you would have an extra called "message"
			String message = extras.getString("message");
			// // MainActivity.setMessage(message);
			// Toast.makeText(context, "Message from server: "+message,
			// 1000).show();
			// Intent pushedIntent = new Intent(context, HomeActivity.class);
			// pushedIntent.putExtra(HomeActivity.PUSHED_CHANNEL, true);
			// pushedIntent.putExtra(HomeActivity.PUSHED_CHANNEL_ID,
			// Integer.valueOf(message));
			// //notification
			// startActivity(pushedIntent);

			ObjectMapper mapper = new ObjectMapper();
			RRPushMessagePayload payload = null;
			settings = new Settings(context);
			try {
//				Log.e("push", "push message: " + message);
				payload = mapper.readValue(message, RRPushMessagePayload.class);
			} catch (JsonParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JsonMappingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			// DbAdapter dbAdapter = new DbAdapter(getApplicationContext());
			// dbAdapter.open();
			// Cursor c = dbAdapter.fetchUser(payload.sender);
			if (payload == null) {
				return;
			}

//			Log.e("push", "push payload.sender: " + payload.sender);
			Uri uri_user = Uri.parse(DbContentProvider.CONTENT_URI_USER_TABLE
					.toString() + "/" + payload.sender);
			Cursor c = null;
			String senderName = "Unknown";
			String senderPicUrl = "";
			try {

				c = getApplicationContext().getContentResolver().query(
						uri_user, null, null, null, null);
				if (c != null && c.moveToFirst()) {
					senderName = c.getString(c
							.getColumnIndex(DbContentProvider.USER_COL_NAME));
					senderPicUrl = c
							.getString(c
									.getColumnIndex(DbContentProvider.USER_COL_PIC_URL));
				}
			} finally {
				if (c != null && !c.isClosed()) {
					c.close();
				}
			}
			// dbAdapter.close();
			if (payload.payloadType.equals("channelId")
					&& !senderName.equals("Unknown")) {
				NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
				int icon = R.drawable.icon; // icon from resources
				CharSequence tickerText = getString(R.string.message_notification_missed_call_from)
						+ senderName; // ticker-text
				long when = System.currentTimeMillis(); // notification time
				Context ctx = getApplicationContext(); // application Context
				CharSequence contentTitle = getString(R.string.message_notification_missed_call_from)
						+ senderName; // expanded
				// message
				// title
				// message
				// text

				Intent notificationIntent = new Intent(
						context.getApplicationContext(), LogInActivity.class);

				PendingIntent contentIntent = PendingIntent.getActivity(this,
						0, notificationIntent, 0);
				// the next two lines initialize the Notification, using the
				// configurations above
				Notification notification = new Notification(icon, tickerText,
						when);
				notification.defaults |= Notification.DEFAULT_SOUND
						| Notification.DEFAULT_LIGHTS;
				notification.flags |= Notification.FLAG_SHOW_LIGHTS;
				notification.flags |= Notification.FLAG_AUTO_CANCEL;
				notification.setLatestEventInfo(
						context,
						contentTitle,
						getResources().getString(
								R.string.message_notification_time_sent)
								+ new Timestamp(payload.timestamp),
						contentIntent);
				int num = settings.getNotificationCount(context);
				notification.number = ++num;
				settings.addNotificationCount(context, num);

				ContentValues value = new ContentValues();
				value.put(DbContentProvider.MISSED_CALLS_COL_TIMESTAMP,
						payload.timestamp);
				value.put(DbContentProvider.MISSED_CALLS_COL_SENDER,
						payload.sender);
				getApplicationContext().getContentResolver().insert(
						DbContentProvider.CONTENT_URI_MISSED_CALL_TABLE, value);

				notificationManager.notify(10001, notification);
			}

//			Log.w("C2DMReceiver", "finish");

		} else {
		}
	}

}