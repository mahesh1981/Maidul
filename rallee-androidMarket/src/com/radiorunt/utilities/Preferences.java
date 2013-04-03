package com.radiorunt.utilities;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.Request.Callback;
import com.facebook.android.AsyncFacebookRunner.RequestListener;
import com.facebook.android.FacebookError;
import com.facebook.model.GraphObject;
import com.radiorunt.R;
import com.radiorunt.activities.HomeActivity;
import com.radiorunt.activities.MwCommunicationLogic;
import com.radiorunt.activities.LogInActivity.UserDataCallback;
import com.radiorunt.activities.fragments.InviteToApplicationDialogFragment;
import com.radiorunt.facebook.Utility;
import com.radiorunt.services.GetGroupsFromFBService;
import com.radiorunt.services.RadioRuntService;
import com.radiorunt.services.RequestFBData;
import com.radiorunt.utilities.jni.Speexrec;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.widget.Toast;

public class Preferences extends PreferenceActivity {
	protected static final int MY_DATA_CHECK_CODE = 0;
	// private static final int TTS_CHECK_FOR_CHECKBOX = 1;
	// private static final int NOTIFICATION_VISIBILITY = 2;
	// CheckBoxPreference cbTtsPref;
	CheckBoxPreference cbNotifVisibility;
	CheckBoxPreference cbRandomPref;
	Preference syncButton;
	Handler mHandler;
	MwCommunicationLogic mMwCommLogic;
	// EditTextPreference etTtsPref;
	Settings settings;
	protected AboutDialog mAboutDialog;

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Speexrec.close();
		addPreferencesFromResource(R.xml.preferences);

		HomeActivity.PREF_CHANGED_ACTIVITY = 1;
		settings = new Settings(getApplicationContext());
		mHandler = new Handler();
		mMwCommLogic = new MwCommunicationLogic(Preferences.this);
		// b = new AlertDialog.Builder(this);
		// cbTtsPref = (CheckBoxPreference) findPreference("tts");
		cbRandomPref = (CheckBoxPreference) findPreference("random_feature");
		cbNotifVisibility = (CheckBoxPreference) findPreference("notification_visibility");
		// etTtsPref = (EditTextPreference) findPreference("ttsbtn");
		// etTtsPref.setEnabled(false);

		Preference customPref = (Preference) findPreference("disconnect");
		customPref
				.setOnPreferenceClickListener(new OnPreferenceClickListener() {

					public boolean onPreferenceClick(Preference preference) {
						// set shared preference and exit code for state of
						// connection
						RadioRuntService.DISCONNECT_FROM_RALLEE = true;
						settings.setExitCode(false);

						// ////////////////////////////////////////////////////////////////

						startHomeActivity();
						return true;
					}

				});

		Preference customDeleteAccountPref = (Preference) findPreference("delete_rallee_account");
		customDeleteAccountPref
				.setOnPreferenceClickListener(new OnPreferenceClickListener() {

					public boolean onPreferenceClick(Preference preference) {
						RadioRuntService.DISCONNECT_FROM_RALLEE = true;
						settings.setExitCode(false);

						RadioRuntService.DELETE_ACCOUNT = 1;

						startHomeActivity();
						return true;
					}

				});

		OnPreferenceChangeListener randomPrefChangeListener = new OnPreferenceChangeListener() {

			@Override
			public boolean onPreferenceChange(Preference preference,
					Object newValue) {
				if (cbRandomPref.isChecked()) {

					String jsonString = "";
					try {
						JSONObject json = new JSONObject();
						json.put("user_id", RalleeApp.getInstance()
								.getRalleeUID());
						json.put("value", Settings.RANDOM_PREF_NO);
						jsonString = json.toString();
					} catch (JSONException e) {
						e.printStackTrace();
					}

					RRServerProxyHelper.startSetRandomService(Preferences.this,
							jsonString);

					settings.setGoRandom(Settings.RANDOM_PREF_NO);

					Toast.makeText(getApplicationContext(),
							R.string.random_disabled, 1000).show();
				} else {

					String jsonString = "";
					try {
						JSONObject json = new JSONObject();
						json.put("user_id", RalleeApp.getInstance()
								.getRalleeUID());
						json.put("value", Settings.RANDOM_PREF_YES);
						jsonString = json.toString();
					} catch (JSONException e) {
						e.printStackTrace();
					}

					RRServerProxyHelper.startSetRandomService(Preferences.this,
							jsonString);

					settings.setGoRandom(Settings.RANDOM_PREF_YES);

					Toast.makeText(getApplicationContext(),
							R.string.random_enabled, 1000).show();
				}
				return true;
			}
		};

		Preference showAboutDialog = (Preference) findPreference("about_dialog");
		showAboutDialog
				.setOnPreferenceClickListener(new OnPreferenceClickListener() {

					@Override
					public boolean onPreferenceClick(Preference preference) {

						mAboutDialog = AboutDialog
								.newInstance(Preferences.this);
						mAboutDialog.show();

						// FragmentTransaction ft =
						// getSupportFragmentManager().beginTransaction();
						// Fragment prev =
						// getSupportFragmentManager().findFragmentByTag("dialog");
						// if (prev != null) {
						// ft.remove(prev);
						// }
						// ft.addToBackStack(null);
						//
						// // Create and show the dialog.
						// inviteFriendsDialogFragment =
						// InviteToApplicationDialogFragment
						// .newInstance(getString(R.string.label_invite_friends),
						// this);
						// inviteFriendsDialogFragment.show(ft, "dialog");

						return true;
					}
				});

		syncButton = (Preference) findPreference("button_sync");
		syncButton
				.setOnPreferenceClickListener(new OnPreferenceClickListener() {

					@Override
					public boolean onPreferenceClick(Preference preference) {
						syncButton.setTitle(R.string.label_sync_title);
						syncButton.setEnabled(false);

						Intent service = new Intent(RalleeApp.getInstance(),
								RequestFBData.class);
						RalleeApp.getInstance().startService(service);

						Intent serviceGroups = new Intent(RalleeApp
								.getInstance(), GetGroupsFromFBService.class);
						RalleeApp.getInstance().startService(serviceGroups);

						String fqlQuery = "SELECT uid, name, pic_square, sex, birthday_date, locale, current_location FROM user WHERE uid = me()";
						Bundle params = new Bundle();
						params.putString("q", fqlQuery);

						Request r = new Request(Session.getActiveSession(),
								"/fql", params, HttpMethod.GET,
								new Request.Callback() {

									@Override
									public void onCompleted(Response response) {

										GraphObject data;
										Globals.logDebug(this,
												"FB OnCompleteUSerRequestListener "
														+ response);
										if (response.getError() == null) {
											try {
												data = response
														.getGraphObject();
												Globals.logDebug(
														this,
														"FB Response: "
																+ data.toString());
												JSONArray users = ((JSONArray) data
														.asMap().get("data"));
												JSONObject user = users
														.getJSONObject(0);

												if (user.has("uid")) {
													Utility.fbId = user
															.getString("uid");
												}
												final String picSquareUrl = user
														.getString("pic_square");
												final String name = user
														.getString("name");
												final String gender = user
														.getString("sex");
												final String birthday = user
														.getString("birthday_date");
												final String locale = user
														.getString("locale");

												if (user.has("current_location")
														&& !user.isNull("current_location")) {
													final JSONObject current_location = user
															.getJSONObject("current_location");
												}

												RalleeApp.getInstance()
														.setFullName(name);
												RalleeApp
														.getInstance()
														.setPicUrl(picSquareUrl);
												RalleeApp.getInstance()
														.setFBLocale(locale);

												JSONObject json = new JSONObject();
												json.put("user_id", RalleeApp
														.getInstance()
														.getRalleeUID());
												json.put("name", name);
												json.put("flag",
														Utility.networkPrefix);
												json.put(
														"first_release",
														RalleeApp
																.getInstance()
																.getRalleeRelease());
												json.put(
														"old_release",
														RalleeApp
																.getInstance()
																.getRalleeRelease());
												json.put(
														"new_release",
														RalleeApp
																.getInstance()
																.getRalleeRelease());
												json.put("picture_url",
														picSquareUrl);
												mMwCommLogic
														.registerSetInsertUserServiceReceiver();
												RRServerProxyHelper
														.startSetInsertUserService(
																RalleeApp
																		.getInstance(),
																json.toString());

												mHandler.post(new Runnable() {

													@Override
													public void run() {
														syncButton
																.setTitle(getString(R.string.label_sync_title)
																		+ " - "
																		+ getString(R.string.label_synced));
														syncButton
																.setEnabled(true);
													}
												});

											} catch (JSONException e) {
												e.printStackTrace();
												mHandler.post(new Runnable() {

													@Override
													public void run() {
														syncButton
																.setEnabled(true);
													}
												});
											}

										} else {
											mHandler.post(new Runnable() {

												@Override
												public void run() {
													syncButton.setEnabled(true);
												}
											});
										}
									}

								});
						r.executeAsync();

						// RalleeApp.getInstance().getAsyncFacebookRunner()
						// .request();

						return true;
					}
				});

		// OnPreferenceChangeListener ttsPrefChangeListener = new
		// OnPreferenceChangeListener() {
		//
		// @Override
		// public boolean onPreferenceChange(Preference preference,
		// Object newValue) {
		// if (preference.getKey() == cbTtsPref.getKey()) {
		// boolean checked = (Boolean) newValue;
		// cbTtsPref.setChecked(checked);
		// Intent checkIntent = new Intent();
		// checkIntent
		// .setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
		// startActivityForResult(checkIntent, MY_DATA_CHECK_CODE);
		// return true;
		// }
		// return false;
		// }
		// };
		//
		// OnPreferenceClickListener ttsbtnPreferenceClickListener = new
		// OnPreferenceClickListener() {
		//
		// @Override
		// public boolean onPreferenceClick(Preference preference) {
		// Intent installIntent = new Intent();
		// installIntent
		// .setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
		// startActivity(installIntent);
		// return true;
		// }
		// };

		// OnPreferenceChangeListener notificVisibilityPrefChangeListener = new
		// OnPreferenceChangeListener() {
		//
		// @Override
		// public boolean onPreferenceChange(Preference preference, Object
		// newValue) {
		// if (preference.getKey() == cbNotifVisibility.getKey()){
		// boolean checked = (Boolean) newValue;
		//
		// cbNotifVisibility.setChecked(checked);
		//
		// if(cbNotifVisibility.isChecked()){
		// settings.setNotificationVisibility(true);
		// }else{
		// settings.setNotificationVisibility(false);
		// }
		// return true;
		// }
		// return false;
		// }
		// };

		// cbTtsPref.setOnPreferenceChangeListener(ttsPrefChangeListener);

		cbRandomPref.setOnPreferenceChangeListener(randomPrefChangeListener);
		// cbNotifVisibility.setOnPreferenceChangeListener(notificVisibilityPrefChangeListener);
		// etTtsPref.setOnPreferenceClickListener(ttsbtnPreferenceClickListener);

		// Intent checkIntent = new Intent();
		// checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
		// startActivityForResult(checkIntent, TTS_CHECK_FOR_CHECKBOX);
	}

	@Override
	protected void onPause() {
		super.onPause();
		// Speexrec.open(settings.getAudioQuality());
		mMwCommLogic.unregisterSetInsertUserServiceReceiver();
		finish();
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (settings.getGoRandom().equals(Settings.RANDOM_PREF_YES)) {
			Globals.logDebug(this, "RANDOM_PREF_YES");
			cbRandomPref.setChecked(true);
		} else {
			Globals.logDebug(this, "RANDOM_PREF_NO");
			cbRandomPref.setChecked(false);
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		// Speexrec.open(settings.getAudioQuality());
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == MY_DATA_CHECK_CODE) {
			// if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
			// if (cbTtsPref.isChecked()) {
			// Toast.makeText(getApplicationContext(),
			// R.string.text_to_speech_available, 500).show();
			// }
			// etTtsPref.setEnabled(false);
			// } else {
			// Toast.makeText(getApplicationContext(),
			// R.string.text_to_speech_install, 1000)
			// .show();
			// etTtsPref.setEnabled(true);
			// cbTtsPref.setChecked(false);
			// }
			// }
			// if (requestCode == TTS_CHECK_FOR_CHECKBOX) {
			// if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
			// cbTtsPref.setSummary(R.string.text_to_speech_available);
			// etTtsPref.setEnabled(false);
			// } else {
			// cbTtsPref.setSummary(R.string.text_to_speech_not);
			// etTtsPref.setEnabled(true);
			// }
		}

	}

	private void startHomeActivity() {
		final Intent i = new Intent(this, HomeActivity.class);
		i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(i);
	}
}