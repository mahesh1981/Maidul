package com.radiorunt.services;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.model.GraphObject;
import com.radiorunt.activities.DbContentProvider;
import com.radiorunt.activities.HomeActivity.UserFBFriendsReceiver;
import com.radiorunt.activities.LogInActivity;
import com.radiorunt.facebook.BaseRequestListener;
import com.radiorunt.facebook.Utility;
import com.radiorunt.utilities.Globals;
import com.radiorunt.utilities.RalleeApp;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class RequestFBData extends IntentService {

	public static boolean check_changes = false; // For RequestFBData.java
													// service to detect changes
	public static boolean finished_insertIntoDB = false; // For
															// RequestFBData.java
															// service
	public static final int NUMBER_TO_NOTIFY = 50;// will broadcast user is
													// saved after on every 50
													// friends saved
	private int numberSavedAfterNotify = NUMBER_TO_NOTIFY;
	public static int numberToLoad = 0;

	public RequestFBData() {
		super("RequestFBData");
	}

	@Override
	protected void onHandleIntent(Intent arg0) {
		// TODO Auto-generated method stub
		Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
		getFriendsFromFB();
	}

	public void getFriendsFromFB() {
		// if (RalleeApp.getInstance().getFacebook() == null) {
		// return;
		// }

		if (RalleeApp.getInstance().getFBSession() == null) {
			return;
		}

		boolean isChanged = false;
		try {

			Bundle params = new Bundle();
			String fqlQuery = "SELECT uid, name, is_app_user, pic_square FROM user WHERE uid IN ( SELECT uid2 FROM friend WHERE uid1=me()) ORDER BY name";
			params.putString("q", fqlQuery);
			Session session = Session.getActiveSession();
			Request request = new Request(session, "/fql", params,
					HttpMethod.GET);
			Response response = Request.executeAndWait(request);
			if (response.getError() == null) {

				GraphObject data = response.getGraphObject();
				JSONArray friendUsersArray = ((JSONArray) data.asMap().get(
						"data"));

				ContentValues values = new ContentValues();
				values.put(DbContentProvider.USER_COL_DELETED, true);
				int numUpdated = getApplicationContext().getContentResolver()
						.update(DbContentProvider.CONTENT_URI_USER_TABLE,
								values, null, null);

				Globals.logDebug(this, "Number of users marked as deleted: "
						+ numUpdated);
				Intent intent = new Intent();
				intent.setAction(UserFBFriendsReceiver.ACTION_NUMBER_OF_FRIENDS);
				numberToLoad = friendUsersArray.length();
				intent.putExtra(UserFBFriendsReceiver.NUM_OF_FRIENDS,
						numberToLoad);

				sendBroadcast(intent);
				numberSavedAfterNotify = NUMBER_TO_NOTIFY;

				for (int i = 0; i < friendUsersArray.length(); i++) {
					JSONObject o = friendUsersArray.getJSONObject(i);

					String userId = Utility.networkPrefix + o.getString("uid");

					Uri uri = Uri
							.parse(DbContentProvider.CONTENT_URI_USER_TABLE
									.toString() + "/" + userId);
					ContentValues val = new ContentValues();

					val.put(DbContentProvider.USER_COL_NAME,
							o.getString("name"));
					val.put(DbContentProvider.USER_COL_PIC_URL, o
							.has("pic_square") ? o.getString("pic_square")
							: null);
					val.put(DbContentProvider.USER_COL_INSTALLED, o
							.has("is_app_user") ? o.getBoolean("is_app_user")
							: false);
					val.put(DbContentProvider.USER_COL_DELETED, false);

					if (getApplicationContext().getContentResolver().update(
							uri, val, null, null) == 0) {

						val.put(DbContentProvider.USER_COL_ID, userId);
						uri = getApplicationContext()
								.getContentResolver()
								.insert(DbContentProvider.CONTENT_URI_USER_TABLE,
										val);

					}

					if (--numberSavedAfterNotify < 1) {
						intent = new Intent();
						intent.setAction(UserFBFriendsReceiver.ACTION_PART_OF_FRIENDS_SAVED);
						sendBroadcast(intent);
						numberSavedAfterNotify = NUMBER_TO_NOTIFY;
					}
				}

				getApplicationContext().getContentResolver().delete(
						DbContentProvider.CONTENT_URI_USER_TABLE,
						DbContentProvider.USER_COL_DELETED + "= 1", null);

				isChanged = true;

			}
		} catch (JSONException e) {
			// Toast.makeText(getApplicationContext(), "Error: " +
			// e.getMessage(), 1000).show();
			// showToast("Error: " + e.getMessage());
			return;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}

		finished_insertIntoDB = true;
		Intent i = new Intent();

		if (isChanged) {
			check_changes = true;
			i.setAction(UserFBFriendsReceiver.ACTION_DONE);
			sendBroadcast(i);
			/*
			 * loadPeopleFromDB(); if (mService != null) { countFriendsOnRR(); }
			 */
		} else {
			i.setAction(UserFBFriendsReceiver.ACTION_IS_NOT_CHANGED);
			sendBroadcast(i);
		}
	}
}
