package com.radiorunt.utilities;

import com.radiorunt.services.RadioRuntService;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.preference.PreferenceManager;
import android.util.Log;

public class Settings {
	Context contx;
	public static String PREF_STREAM = "music";
	public static final String ARRAY_STREAM_VOICE = "voice";
	public static final String ARRAY_STREAM_MUSIC = "music";
	private static final String DEFAULT_STREAM = "music";

	public static final String PREF_JITTER = "buffering";
	public static final String ARRAY_JITTER_NONE = "none";
	public static final String ARRAY_JITTER_SPEEX = "speex";

	public static final String PREF_QUALITY = "qualityrate";
	private static final String DEFAULT_QUALITY = "3";

	public static final String PREF_PTT_KEY = "pttkey";

	public static final String PREF_EVENT_SOUNDS = "eventsounds";

	public static final String PREF_PROXIMITY = "proximity";

	public static final String PREF_TTS = "tts";
	public static final String PREF_FINE_LOCATING = "use_fine_locating";

	public static final String PREF_BACKGROUND_SERVICE = "bgservice";

	public static final int SPEAK_DURATION = 30;

	private static final int[] encodedSizes = { 10, 10, 25, 25, 42, 42, 60, 60,
			60, 106, 106 };// {6, 10, 15, 20, 20, 28, 28, 38, 38, 46, 62};

	public static final String HOST = "host";
	public static final int PORT = 0;
	public static final String USERNAME = "username";
	public static final String PASSWORD = "password";

	public static final String NOTIFCATION_VISIBILITY = "notification_visibility";

	private final String IS_FIRST_START = "isFirstStart";
	private final String IS_FIRST_START_FB_POST = "isFirstStartFBpost";

	private final String CALL_HISTORY_TIMESTAMP = "callHistoryTimestamp";
	private final String CALL_HISTORY_TYPE = "callHistoryType";

	/**
	 * random dialog constants
	 */

	// public static final String RANDOM_PREF_RANDOM = "random";
	public static final String RANDOM_PREF_GO_RANDOM = "gorandom";
	public static final String RANDOM_PREF_OVER18 = "over18";
	public static final String RANDOM_PREF_YES = "yes";
	public static final String RANDOM_PREF_NO = "no";
	public static final String RANDOM_PREF_DEFAULT = "default";
	public static final String EXIT_CODE = "exit";
	public static final String EXIT_AND_LOGIN = "exitAndLogin";

	private final SharedPreferences preferences;
	private final SharedPreferences.Editor editor;

	public Settings(final Context ctx) {
		contx = ctx.getApplicationContext();
		preferences = PreferenceManager.getDefaultSharedPreferences(contx);
		editor = preferences.edit();
	}

	public void setNotificationVisibility(boolean visibility) {
		editor.putBoolean(NOTIFCATION_VISIBILITY, visibility);
		editor.commit();
	}

	public boolean getNotificationVisibility() {
		return preferences.getBoolean("notification_visibility", false);
	}

	public void setConnectionIntent(String host, int port, String username,
			String password) {
		editor.putString("host", host);
		editor.putInt("port", port);
		editor.putString("username", username);
		editor.putString("password", password);
		editor.commit();
	}

	public Intent getConnectionIntent() {
		Intent connectionIntent = new Intent(contx, RadioRuntService.class);
		connectionIntent.setAction(RadioRuntService.ACTION_CONNECT);
		connectionIntent.putExtra(RadioRuntService.EXTRA_HOST,
				preferences.getString("host", "host"));
		connectionIntent.putExtra(RadioRuntService.EXTRA_PORT,
				preferences.getInt("port", 0));
		connectionIntent.putExtra(RadioRuntService.EXTRA_USERNAME,
				preferences.getString("username", "username"));
		connectionIntent.putExtra(RadioRuntService.EXTRA_PASSWORD,
				preferences.getString("password", "password"));
		return connectionIntent;
	}

	public void setConnectionIntentHost(String host) {
		editor.putString("host", host);
		editor.commit();
		Globals.logDebug(this, "Server ip address: " + host);
	}

	public String getConnectionIntentHost() {
		return preferences.getString("host", null);
	}

	public int getAudioQuality() {
		// return Integer.parseInt(preferences.getString(Settings.PREF_QUALITY,
		// DEFAULT_QUALITY));
		return Integer.parseInt(preferences.getString(PREF_QUALITY,
				DEFAULT_QUALITY));
		// return 10;
	}

	public int getEncodedSize() {
		return encodedSizes[getAudioQuality()];
	}

	public int getAudioStream() {
		return preferences.getString(PREF_STREAM, ARRAY_STREAM_MUSIC).equals(
				ARRAY_STREAM_MUSIC) ? AudioManager.STREAM_MUSIC
				: AudioManager.STREAM_VOICE_CALL;
	}

	public void setAudioStreamHandset(Context context) {
		addKey(context, PREF_STREAM, ARRAY_STREAM_VOICE);
	}

	public void setAudioStreamLaudSpeaker(Context context) {
		addKey(context, PREF_STREAM, ARRAY_STREAM_MUSIC);
	}

	public int getPttKey() {
		return Integer.parseInt(preferences.getString(PREF_PTT_KEY, "-1"));
	}

	public boolean isJitterBuffer() {
		return preferences.getString(PREF_JITTER, ARRAY_JITTER_NONE).equals(
				ARRAY_JITTER_SPEEX);
	}

	public boolean isEventSoundsEnabled() {
		return preferences.getBoolean(PREF_EVENT_SOUNDS, true);
	}

	public boolean isProximityEnabled() {
		return preferences.getBoolean(PREF_PROXIMITY, false);
	}

	public boolean isTtsEnabled() {
		return preferences.getBoolean(PREF_TTS, false);
	}

	public boolean isFineLocating() {
		return preferences.getBoolean(PREF_FINE_LOCATING, false);
	}

	public boolean isBackgroundServiceEnabled() {
		return preferences.getBoolean(PREF_BACKGROUND_SERVICE, true);
	}

	public void addKey(Context context, String key, String val) {
		editor.putString(key, val);
		editor.commit();
	}

	public void removeKey(Context context, String key) {
		editor.remove(key);
		editor.commit();
	}

	public void addNotificationCount(Context context, int val) {
		editor.putInt("NOTIFICATION_COUNT", val);
		editor.commit();
	}

	public void removeNotificationCount(Context context) {
		editor.remove("NOTIFICATION_COUNT");
		editor.commit();
	}

	public int getNotificationCount(Context context) {
		return preferences.getInt("NOTIFICATION_COUNT", 0);
	}

	public String getKey(Context context, String key) {
		return preferences.getString(key, null);
	}

	public void setIsFirstStart(Context context, boolean isFirst) {
		editor.putBoolean(IS_FIRST_START, isFirst);
		editor.commit();
	}

	public boolean getIsFirstStart() {
		return preferences.getBoolean(IS_FIRST_START, true);
	}

	public void setIsFirstStartFBpost(boolean isFirst) {
		editor.putBoolean(IS_FIRST_START_FB_POST, isFirst);
		editor.commit();
	}

	public boolean getIsFirstStartFBpost() {
		return preferences.getBoolean(IS_FIRST_START_FB_POST, true);
	}

	public void setOver18(String value) {
		editor.putString(RANDOM_PREF_OVER18, value);
		editor.commit();
	}

	public String getOver18() {
		return preferences.getString(RANDOM_PREF_OVER18, RANDOM_PREF_DEFAULT);
	}

	public void setGoRandom(String value) {
		editor.putString(RANDOM_PREF_GO_RANDOM, value);
		editor.commit();
	}

	public String getGoRandom() {
		return preferences
				.getString(RANDOM_PREF_GO_RANDOM, RANDOM_PREF_DEFAULT);
	}

	public void setCallHistoryTimestamp(long value) {
		editor.putLong(CALL_HISTORY_TIMESTAMP, value);
		editor.commit();
	}

	public long getCallHistoryTimestamp() {
		return preferences.getLong(CALL_HISTORY_TIMESTAMP, -1);
	}

	public void setCallHistoryTipe(int value) {
		editor.putInt(CALL_HISTORY_TYPE, value);
		editor.commit();
	}

	public int getCallHistoryTipe() {
		return preferences.getInt(CALL_HISTORY_TYPE, -1);
	}

	public String replaseUrlWhiteSpaces(String in) {
		String out = in.replaceAll(" ", "%20");
		return out;
	}

	/**
	 * @param Set
	 *            value for the shared pref. States: false - connected to
	 *            server; true - disconnected from server;
	 * 
	 */
	public void setExitCode(boolean value) {
		editor.putBoolean(EXIT_CODE, value);
		editor.commit();
	}

	/**
	 * @param return defaultV default value for the shared pref if it doesn't
	 *        exist. States: false - connected to server; true - disconnected
	 *        from server;
	 * 
	 */
	public boolean getExitCode(boolean defaultV) {
		return preferences.getBoolean(EXIT_CODE, defaultV);
	}

	/**
	 * @param set
	 *            checkBoxResult value for the shared pref. set "LogIn" string
	 *            if user has enable Exit and Login settings and "exitAndLogin"
	 *            if feature is not enabled
	 * 
	 */
	public void setExitAndLogInSettings(String checkBoxResult) {
		editor.putString(EXIT_AND_LOGIN, checkBoxResult);
		editor.commit();
	}

	/**
	 * @param return defaultV default value for the shared pref if it doesn't
	 *        exist. return string "LogIn" if user has enable Exit and Login
	 *        settings and "exitAndLogin" if feature is not enabled
	 * 
	 */
	public String getExitAndLogInSettings(String defaultV) {
		return preferences.getString(EXIT_AND_LOGIN, defaultV);
	}

	public void clearSettings() {
		editor.clear();
		editor.commit();
	}
}