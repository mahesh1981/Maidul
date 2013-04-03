package com.radiorunt.utilities;

import java.util.ArrayList;
import java.util.List;

import com.facebook.AccessToken;
import com.facebook.Request;
import com.facebook.Session;
import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.Facebook;
import com.facebook.android.FacebookError;
import com.google.analytics.tracking.android.EasyTracker;
import com.radiorunt.R;
import com.radiorunt.R.string;
import com.radiorunt.activities.LogInActivity;
import com.radiorunt.facebook.Utility;

import android.R.bool;
import android.app.Application;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

public class RalleeApp extends Application {

	@SuppressWarnings("null")
	@Override
	public void onCreate() {
		super.onCreate();
		instance = (RalleeApp) getApplicationContext();
		EasyTracker.getInstance().setContext(instance);

		getFBSession();
	}

	@Override
	public void onTerminate() {
		// TODO Auto-generated method stub
		super.onTerminate();
	}

	private static RalleeApp instance = RalleeApp.getInstance();

	private static final String RALLEE_UID = "ralleeUID";
	// public static final String APP_ID = "422592304447150"/*
	// * radiorunt appid
	// * "336310619730871"
	// */;
	private static final String FULL_NAME = "fullName";
	private static final String PIC_URL = "picUrl";
	private static final String FB_LOCALE = "fbLocale";

	private static final String BIRTHDAY = "birthday";
	private static final String GENDER = "gender";
	/**
	 * First Name and Last Name of FB User
	 */
	private String fullName = null;
	private String picUrl = null;
	private String ralleeUID = null;
	private String fbLocale = null;

	private String birthday = null;
	private String gender = null;

	// private Uri currentPrivateGroupUri = null;
	// private boolean isInGroupCall = false;
	// private Facebook mFacebook;
	// private AsyncFacebookRunner mAsyncFacebookRunner;
	private Session fbSession;

	public static RalleeApp getInstance() {
		return instance;
	}

	public String getRalleeUID() {
		if (ralleeUID == null || "ralleeUID".equals(ralleeUID)) {
			ralleeUID = PreferenceManager.getDefaultSharedPreferences(instance)
					.getString(RALLEE_UID, "ralleeUID");
		}
		return ralleeUID;
	}

	public boolean setRalleeUID(String uid) {
		this.ralleeUID = uid;
		return PreferenceManager.getDefaultSharedPreferences(instance).edit()
				.putString(RALLEE_UID, uid).commit();
	}

	/**
	 * Get First Name and Last Name of FB User
	 */
	public String getFullName() {
		if (fullName == null) {
			fullName = PreferenceManager.getDefaultSharedPreferences(instance)
					.getString(FULL_NAME, "default");
		}
		return fullName;
	}

	public String getPicUrl() {
		if (picUrl == null) {
			picUrl = PreferenceManager.getDefaultSharedPreferences(instance)
					.getString(PIC_URL, "default");
		}
		return picUrl;
	}

	public String getFBLocale() {
		if (fbLocale == null) {
			fbLocale = PreferenceManager.getDefaultSharedPreferences(instance)
					.getString(FB_LOCALE, "en-US");
		}
		return fbLocale;
	}

	/**
	 * Set First Name and Last Name of FB User
	 */
	public boolean setFullName(String fullName) {
		this.fullName = fullName;
		return PreferenceManager.getDefaultSharedPreferences(instance).edit()
				.putString(FULL_NAME, fullName).commit();
	}

	/**
	 * Sets URL of users picture
	 * 
	 * @param picUrl
	 *            Url of users picture that needs to be stored
	 * @return Returns true if the new value was successfully written to shared
	 *         preferences.
	 */
	public boolean setPicUrl(String picUrl) {
		this.picUrl = picUrl;
		return PreferenceManager.getDefaultSharedPreferences(instance).edit()
				.putString(PIC_URL, picUrl).commit();
	}

	public boolean setFBLocale(String locale) {
		this.fbLocale = locale;
		return PreferenceManager.getDefaultSharedPreferences(instance).edit()
				.putString(FB_LOCALE, this.fbLocale).commit();
	}

	// public Facebook getFacebook() {
	// if (mFacebook == null) {
	// Globals.logDebug(this, "random getFacebook, mFacebook is NULL");
	// mFacebook = new Facebook(APP_ID);
	// }
	//
	// if (mFacebook != null) {
	// Globals.logDebug(this, "random getFacebook, mFacebook initialized");
	// } else {
	// Globals.logDebug(this,
	// "random getFacebook, mFacebook is still NULL");
	// }
	// return mFacebook;
	// }
	public Session getFBSession() {
		SharedPreferences mPrefs = PreferenceManager
				.getDefaultSharedPreferences(instance);
		String access_token = mPrefs.getString("access_token", null);

		fbSession = Session.getActiveSession();
		if (fbSession != null) {
			// Check if there is an existing token to be migrated
			if (access_token != null) {
				// Clear the token info
				SharedPreferences.Editor editor = mPrefs.edit();
				editor.putString("access_token", null);
				editor.commit();
				// Create an AccessToken object for importing
				// just pass in the access token and take the
				// defaults on other values
				AccessToken accessToken = AccessToken
						.createFromExistingAccessToken(access_token, null,
								null, null, null);
				// statusCallback: Session.StatusCallback implementation
				fbSession.open(accessToken, null);
				Session.setActiveSession(fbSession);
			}
		}
		return fbSession;
	}

	// public AsyncFacebookRunner getAsyncFacebookRunner() {
	// if (mAsyncFacebookRunner == null) {
	// mAsyncFacebookRunner = new AsyncFacebookRunner(getFacebook());
	// }
	// return mAsyncFacebookRunner;
	// }

	// public void extendFacebookTokenIfNeeded() {
	// getInstance().getFacebook().extendAccessTokenIfNeeded(this,
	// new Facebook.ServiceListener() {
	//
	// @Override
	// public void onFacebookError(FacebookError e) {
	// // TODO Auto-generated method stub
	// Globals.logDebug(this, "fbAccessToken FacebookError: "
	// + e.toString());
	// }
	//
	// @Override
	// public void onError(Error e) {
	// // TODO Auto-generated method stub
	//
	// Globals.logError(this,
	// "fbAccessToken Error: " + e.toString());
	// }
	//
	// @Override
	// public void onComplete(Bundle values) {
	// // TODO Auto-generated method stub
	// Globals.logDebug(this, "fbAccessToken Complete");
	// }
	// });
	//
	// }

	// public void extendFacebookToken() {
	// getInstance().getFacebook().extendAccessToken(this,
	// new Facebook.ServiceListener() {
	//
	// @Override
	// public void onFacebookError(FacebookError e) {
	// // TODO Auto-generated method stub
	// Globals.logDebug(this, "fbAccessToken FacebookError: "
	// + e.toString());
	// }
	//
	// @Override
	// public void onError(Error e) {
	// // TODO Auto-generated method stub
	//
	// Globals.logError(this,
	// "fbAccessToken Error: " + e.toString());
	// }
	//
	// @Override
	// public void onComplete(Bundle values) {
	// // TODO Auto-generated method stub
	// Globals.logDebug(this, "fbAccessToken Complete");
	// }
	// });
	//
	// }

	public String getRalleeRelease() {
		String ralleeRelease = "Rallee ";// getString(R.string.app_name) + " ";
		try {
			ralleeRelease += getInstance().getPackageManager().getPackageInfo(
					getInstance().getPackageName(), 0).versionName;
		} catch (NameNotFoundException e) {

			e.printStackTrace();
		}
		return ralleeRelease;
	}

	public boolean setBirthday(String birthday) {
		this.birthday = birthday;
		return PreferenceManager.getDefaultSharedPreferences(instance).edit()
				.putString(BIRTHDAY, birthday).commit();
	}

	public String getBirthday() {
		if (birthday == null) {
			birthday = PreferenceManager.getDefaultSharedPreferences(instance)
					.getString(BIRTHDAY, "default");
		}
		return birthday;
	}

	public boolean setGender(String gender) {
		this.gender = gender;
		return PreferenceManager.getDefaultSharedPreferences(instance).edit()
				.putString(BIRTHDAY, birthday).commit();
	}

	public String getGender() {
		if (gender == null) {
			gender = PreferenceManager.getDefaultSharedPreferences(instance)
					.getString(GENDER, "default");
		}
		return gender;
	}

	// public Uri getCurrentPrivateGroupUri() {
	// // TODO Auto-generated method stub
	// return currentPrivateGroupUri;
	// }
	//
	// public void setCurrentPrivateGroupUri(Uri uri) {
	// currentPrivateGroupUri = uri;
	// }

	// public boolean isInGroupCall() {
	// return isInGroupCall;
	// }

	// public void setIsInGroupCall(boolean is) {
	// isInGroupCall = is;
	// }

}
