package com.radiorunt.facebook;

import com.facebook.android.Facebook;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

public class SessionStore {

	private static final String TOKEN = "access_token";
	private static final String EXPIRES = "expires_in";
	private static final String KEY = "facebook-session";

	/*
	 * Save the access token and expiry date so you don't have to fetch it each
	 * time
	 */

	public static boolean save(Facebook session, Context context) {
//		Log.d("FB", "SessionStore Save ");
		Editor editor = context.getSharedPreferences(KEY, Context.MODE_PRIVATE)
				.edit();
		editor.putString(TOKEN, session.getAccessToken());
		editor.putLong(EXPIRES, session.getAccessExpires());
		return editor.commit();
	}

	/*
	 * Restore the access token and the expiry date from the shared preferences.
	 */
	public static boolean restore(Facebook session, Context context) {
//		Log.d("FB", "SessionStore REStore ");
		SharedPreferences savedSession = context.getSharedPreferences(KEY,
				Context.MODE_PRIVATE);
		session.setAccessToken(savedSession.getString(TOKEN, null));
		session.setAccessExpires(savedSession.getLong(EXPIRES, 0));
		return session.isSessionValid();
	}

	public static void clear(Context context) {
//		Log.d("FB", "SessionStore Cleare ");
		Editor editor = context.getSharedPreferences(KEY, Context.MODE_PRIVATE)
				.edit();
		editor.clear();
		editor.commit();
	}

}
