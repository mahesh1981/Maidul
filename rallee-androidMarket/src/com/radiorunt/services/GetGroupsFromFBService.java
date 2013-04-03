package com.radiorunt.services;

import java.io.IOException;
import java.net.MalformedURLException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.android.FacebookError;
import com.facebook.model.GraphObject;
import com.radiorunt.activities.DbContentProvider;
import com.radiorunt.businessobjects.FBGroupsReceiver;
import com.radiorunt.facebook.BaseRequestListener;
import com.radiorunt.facebook.Utility;
import com.radiorunt.utilities.Globals;
import com.radiorunt.utilities.RalleeApp;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

public class GetGroupsFromFBService extends IntentService {

	public GetGroupsFromFBService() {
		super("GetGroupsFromFBService");
	}

	@Override
	protected void onHandleIntent(Intent arg0) {
		// TODO Auto-generated method stub
		Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
		getGroupsFromFB();
	}

	synchronized public void getGroupsFromFB() {
		Globals.logDebug(this, "Start getGroupsFromFB");
		try {
			if (RalleeApp.getInstance().getFBSession() == null) {
				return;
			}
			Bundle params = new Bundle();
			Session session = Session.getActiveSession();
			Request request = new Request(session, "me/groups", null,
					HttpMethod.GET);

			Response response = Request.executeAndWait(request);
			if (response.getError() == null) {

				GraphObject data = response.getGraphObject();
				JSONArray jsonGroupsArray = ((JSONArray) data.asMap().get(
						"data"));

				String id_group = null;
				String name_group = null;

				if (jsonGroupsArray != null) {
					getApplicationContext().getContentResolver().delete(
							DbContentProvider.CONTENT_URI_FB_GROUPS_TABLE,
							null, null);
				}

				for (int i = 0; i < jsonGroupsArray.length(); i++) {
					ContentValues values = new ContentValues();
					id_group = (String) ((JSONObject) jsonGroupsArray.get(i))
							.get("id");
					name_group = (String) ((JSONObject) jsonGroupsArray.get(i))
							.get("name");
					values.put(DbContentProvider.FB_GROUP_COL_NAME, name_group);

					Uri uri = Uri
							.parse(DbContentProvider.CONTENT_URI_FB_GROUPS_TABLE
									.toString() + "/" + id_group);

					if (getApplicationContext().getContentResolver().update(
							uri, values, null, null) == 0) {
						values.put(DbContentProvider.FB_GROUP_COL_ID, id_group);
						uri = getApplicationContext()
								.getContentResolver()
								.insert(DbContentProvider.CONTENT_URI_FB_GROUPS_TABLE,
										values);
					}
				}

				Intent intent = new Intent();
				intent.setAction(FBGroupsReceiver.ACTION_DONE);
				sendBroadcast(intent);
			}

		} catch (JSONException e) {
			e.printStackTrace();
			Intent intent = new Intent();
			intent.setAction(FBGroupsReceiver.ACTION_FAILED);
			sendBroadcast(intent);
			// } catch (MalformedURLException e) {
			// // TODO Auto-generated catch block
			// e.printStackTrace();
			// } catch (IOException e) {
			// // TODO Auto-generated catch block
			// e.printStackTrace();
		}
	}
}
