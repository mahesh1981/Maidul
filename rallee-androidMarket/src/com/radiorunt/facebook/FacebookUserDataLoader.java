package com.radiorunt.facebook;

import java.io.IOException;
import java.net.MalformedURLException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.pm.ApplicationInfo;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.model.GraphObject;
import com.radiorunt.activities.HomeActivity;
import com.radiorunt.businessobjects.RRUser;
import com.radiorunt.utilities.Globals;
import com.radiorunt.utilities.RalleeApp;

public class FacebookUserDataLoader {

	public static RRUser getFbUserById(String fbID) {
//		Log.i("random", "getFbUserById, fbID: " + fbID);

		if (!isFBIdValid(fbID)) {
			return null;
		}
		RRUser part = new RRUser();
		part.userName = Utility
				.buildUsername(Utility.networkPrefix, fbID, null);

//		Log.i("random", "getFbUserById, part.userName: " + part.userName);

		Bundle params = new Bundle();
		String fqlQuery = "SELECT uid, name, pic_square FROM user WHERE uid = "
				+ fbID;
//		Log.i("random", "getFbUserById, query: " + fqlQuery);
		params.putString("q", fqlQuery);
		Session session = Session.getActiveSession();
		Response response = null;
		boolean extendTokenStarted = false;
		int numOfTrys = 3;
		do {
			numOfTrys--;
			try {

				Request request = new Request(session, "/fql", params,
						HttpMethod.GET);
				response = Request.executeAndWait(request);
				if (response.getError() == null) {

					GraphObject data = response.getGraphObject();

					Globals.logInfo(RalleeApp.getInstance(),
							"getFbUserById, response: " + response);

					JSONArray users = ((JSONArray) data.asMap().get("data"));

					if (users != null && users.length() > 0) {
						JSONObject userObject = users.getJSONObject(0);
						part.FirstName = userObject.has("name") ? userObject
								.getString("name") : "Unknown name";
								
						part.picUrl = userObject.has("pic_square") ? userObject
								.getString("pic_square") : null;
						return part;
					}
				}
				// } catch (MalformedURLException e) {
				// // if (extendTokenStarted && numOfTrys <= 1) {
				// // extendTokenStarted = true;
				// // RalleeApp.getInstance().extendFacebookToken();
				// // }
				// // TODO Auto-generated catch block
				// e.printStackTrace();
				// } catch (IOException e) {
				// // if (extendTokenStarted && numOfTrys <= 1) {
				// // extendTokenStarted = true;
				// // RalleeApp.getInstance().extendFacebookToken();
				// } // TODO Auto-generated catch block
				// e.printStackTrace();
			} catch (JSONException e) {
				// if (extendTokenStarted) {
				// extendTokenStarted = true;
				// RalleeApp.getInstance().extendFacebookToken();
				// } // TODO Auto-generated catch block
				e.printStackTrace();
			}
		} while (response == null || numOfTrys < 0
				|| (response != null && response.equals("")));

		if (part.FirstName == null) {
			part.FirstName = "Unknown name";
		}

//		Log.i("random", "getFbUserById, part.FirstName: " + part.FirstName);
		return part;

	}

	private static boolean isDebugable() {
		boolean DEBUGGABLE = (RalleeApp.getInstance().getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
		return DEBUGGABLE;
	}

	private static boolean isFBIdValid(String fbID) {
		CharSequence cs = fbID;
		for (int i = 0; i < cs.length(); i++) {
			char c = cs.charAt(i);
			if (c < '0' || c > '9') {
				return false;
			}
		}
		return true;
	}

}
