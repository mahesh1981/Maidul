package com.radiorunt.activities;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import junit.framework.Assert;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.HttpMethod;
import com.facebook.LoggingBehavior;
import com.facebook.Request;
import com.facebook.Request.Callback;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.Session.OpenRequest;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphObject;
import com.google.android.c2dm.C2DMessaging;
import com.radiorunt.R;
import com.radiorunt.facebook.BaseRequestListener;
import com.radiorunt.facebook.SessionEvents;
import com.radiorunt.facebook.Utility;
import com.radiorunt.services.BaseServiceObserver;
import com.radiorunt.services.RadioRuntService;
import com.radiorunt.services.RequestFBData;
import com.radiorunt.services.TtsProvider;
import com.radiorunt.utilities.FaqActivity;
import com.radiorunt.utilities.Globals;
import com.radiorunt.utilities.RRServerProxyHelper;
import com.radiorunt.utilities.RalleeApp;
import com.radiorunt.utilities.Settings;
import com.radiorunt.utilities.TosAndPrivacyDialog;
//import com.radiorunt.facebook.LoginButton;
//import com.radiorunt.facebook.LoginButton.LogOutFBAccount;
//import com.radiorunt.utilities.DbAdapter;

public class LogInActivity extends ConnectedFragmentActivity {
	public static boolean firstConnectionToServerDone = false;
	public String serverIpAddress = null;
	JSONObject json_data = null;
	Settings settings;
	String Name,temp;
	Integer serverId = -1;
	// DbAdapter dbAdapter;
	public static final String SENDER_ID = "397536487190"/* "radioruntpushnotification@gmail.com" */;
	private static TextView tvStatus;
	private static ProgressBar pbStatus;
	// private LoginButton fbLoginButton;
	private ImageButton fbLoginButton;
	private TextView mText;
	private volatile Handler mHandler;
	private int handlerCounter = 0;
	private static ProgressDialog dialog;// Loading...
	String[] main_items = { "Update Status", "App Requests", "Get Friends",
			"Upload Photo", "Place Check-in", "Run FQL Query", "GraphUser",
			"Graph API Explorer" };
	List<String> readPermissions = Arrays.asList("user_groups",
			"user_location", "user_birthday");
	List<String> publishPermissions = Arrays.asList("publish_actions");
	final static int AUTHORIZE_ACTIVITY_RESULT_CODE = 1;
	private static final int ACTIVITY_HOME = 0;

	private static final String STATE_WAIT_CONNECTION = "com.radiorunt.activities.LogInActivity.WAIT_CONNECTION";
	protected static final int MSG_GETIPADDRESS_SERVICE = 1;
	public static final int MSG_REQ_USERDATA = 2;
	// private static final int PREF_CHANGED = 1;
	// private FbAPIsAuthListener fbAuthListener = new FbAPIsAuthListener();
	Intent connectionIntent;

	private ServerServiceObserver mServiceObserver;
	private GetServerIpAddressServiceReceiver mGetServerIpAddressServiceReceiver;
	private AlertDialog selectServerDialog = null;
	private AlertDialog selectGroupDialog = null;
	// public static Intent changeServerIntent;
	// private RRLocationServiceReceiver locationReceiver;
	// private SendPushMessageServiceReceiver mSendPushMessageServiceReceiver;
	// private GetServerIdServiceReceiver mGetServerIdServiceReceiver;
	private MwCommunicationLogic mMwCommLogic;
	protected TosAndPrivacyDialog mTosAndPrivacyDialog;

	private Session.StatusCallback fbStatusCallback = new SessionStatusCallback();

	// /////////////////////////////////////////////////////////////////////////////
	// //////////////////////// ANDROID METHODS
	// ///////////////////////////////////
	// /////////////////////////////////////////////////////////////////////////////

	private static final String AUTHORITY = "com.radiorunt.utilities";
	private static final String BASE_PATH = "todo";
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY
			+ "/" + BASE_PATH);

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {

		/*
		 * StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
		 * .detectDiskReads() .detectDiskWrites() .detectNetwork() // or
		 * .detectAll() for all detectable problems .penaltyLog() .build());
		 * StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
		 * .detectLeakedSqlLiteObjects() .detectLeakedSqlLiteObjects()
		 * .penaltyLog() .penaltyDeath() .build());
		 */

		super.onCreate(savedInstanceState);
		uiHelper = new UiLifecycleHelper(this, null);
		uiHelper.onCreate(savedInstanceState);

		com.facebook.Settings.setShouldAutoPublishInstall(true);
		com.facebook.Settings
				.addLoggingBehavior(LoggingBehavior.INCLUDE_ACCESS_TOKENS);
		// if (savedInstanceState == null) {
		// // Add the fragment on initial activity setup
		// fbFragment = new FacebookFragment();
		// getSupportFragmentManager()
		// .beginTransaction()
		// .add(android.R.id.content, fbFragment)
		// .commit();
		// } else {
		// // Or set the fragment from restored state info
		// fbFragment = (FacebookFragment) getSupportFragmentManager()
		// .findFragmentById(android.R.id.content);
		// }

		mMwCommLogic = new MwCommunicationLogic(this);
		settings = new Settings(this);
		setContentView(R.layout.main);
		Globals.logDebug(this, "LoginAC onCreate");
		C2DMessaging.register(this, SENDER_ID);
		fbLoginButton = (ImageButton) findViewById(R.id.btnLoginLogInActivity);
		fbLoginButton.setEnabled(false);
		// fbLoginButton.setVisibility(View.INVISIBLE);
		Session sessionToClose = Session.getActiveSession();
		if (sessionToClose != null && sessionToClose.isOpened()) {
			sessionToClose.close();
		}
		tvStatus = (TextView) findViewById(R.id.tvProgressStatusLoginActivity);
		pbStatus = (ProgressBar) findViewById(R.id.progressBarLoginActivity);
		pbStatus.setMinimumHeight(5);
		pbStatus.setMinimumWidth(5);
		mText = new TextView(this);
		mText.setTextSize(40);
		mText = (TextView) LogInActivity.this.findViewById(R.id.txt);
		// if (!RalleeApp.getInstance().getFullName().equals("default")) {
		// mText.setText(getString(R.string.label_welcome) + "\n"
		// + RalleeApp.getInstance().getFullName() + "!");
		// }

		TextView tos = (TextView) findViewById(R.id.tv_login_tos);
		tos.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mTosAndPrivacyDialog = TosAndPrivacyDialog.newInstance(
						LogInActivity.this, TosAndPrivacyDialog.MODE_TOS);
				mTosAndPrivacyDialog.show();
			}
		});
		TextView privacy = (TextView) findViewById(R.id.tv_login_privacy);
		privacy.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mTosAndPrivacyDialog = TosAndPrivacyDialog.newInstance(
						LogInActivity.this, TosAndPrivacyDialog.MODE_PRIVACY);
				mTosAndPrivacyDialog.show();
			}
		});

		// RalleeApp.getInstance().getFacebook() = new Facebook(APP_ID);
		// Utility.mAsyncRunner = new
		// AsyncFacebookRunner(RalleeApp.getInstance().getFacebook());
		// fbLoginButton.setVisibility(View.VISIBLE);

		// ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// /////////////////////////////// Connect to Intent Channel
		// ////////////////////////////////////////////////////////////

		// if (Utility.invocedChannelName != null) { FBTOBECHANGED
		// fbLoginButton.initIntent(this, AUTHORIZE_ACTIVITY_RESULT_CODE,
		// RalleeApp.getInstance().getFacebook(), permissions, this,
		// mHandler);
		// }

		// ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

		/*
		 * // TEST deo za upis u bazu i citanje
		 * 
		 * ContentValues values = new ContentValues();
		 * values.put(DbContentProvider.SERVER_COL_ID, 3);
		 * values.put(DbContentProvider.SERVER_COL_NAME, "server");
		 * values.put(DbContentProvider.SERVER_COL_HOST, "host");
		 * values.put(DbContentProvider.SERVER_COL_PORT, 8888);
		 * values.put(DbContentProvider.SERVER_COL_USERNAME, "milan");
		 * values.put(DbContentProvider.SERVER_COL_PASSWORD, "milan");
		 * 
		 * Uri uri =
		 * getApplicationContext().getContentResolver().insert(DbContentProvider
		 * .CONTENT_URI_SERVER_TABLE, values);
		 * 
		 * Cursor c = managedQuery(DbContentProvider.CONTENT_URI_SERVER_TABLE,
		 * null, null, null, "_id desc"); if (c.moveToFirst()) { do{
		 * Toast.makeText(this, c.getString(c.getColumnIndex(
		 * DbContentProvider.SERVER_COL_ID)) + ", " +
		 * c.getString(c.getColumnIndex( DbContentProvider.SERVER_COL_NAME)) +
		 * ", " + c.getString(c.getColumnIndex(
		 * DbContentProvider.SERVER_COL_HOST)), Toast.LENGTH_LONG).show(); }
		 * while (c.moveToNext()); }
		 * 
		 * Log.d("t", "t");
		 */

		mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				if (msg.what == MSG_GETIPADDRESS_SERVICE) {
					// if(handlerCounter++ < 5){
					try {
						tvStatus.setVisibility(View.VISIBLE);
						tvStatus.setText(R.string.waiting_for_internet);
						pbStatus.setVisibility(View.VISIBLE);
						RRServerProxyHelper
								.startGetServerIpAddressService(getApplicationContext());
						// RRServerProxyHelper.startGetServerIdService(getApplicationContext());
					} catch (Exception e) {
						e.printStackTrace();
					}
					// }else{
					// .setVisibility(View.VISIBLE);
					// .setText("Please check your internet connection.");
					// pbStatus.setVisibility(View.INVISIBLE);
					// }
				} else if (msg.what == MSG_REQ_USERDATA) {
					userDataRequest();
				}
			}
		};

		// restore session if one exists
		// FBTOBECHANGED
		// SessionStore.restore(RalleeApp.getInstance().getFacebook(), this);
		// SessionEvents.addAuthListener(fbAuthListener);
		// SessionEvents.addLogoutListener(fbAuthListener);

		// RalleeApp.getInstance().getFBSession();// FBTOBECHANGED

		/*
		 * Source Tag: login_tag
		 */
		// fbLoginButton.init(this, AUTHORIZE_ACTIVITY_RESULT_CODE, RalleeApp
		// FBTOBECHANGED
		// .getInstance().getFacebook(), permissions, this, mHandler);
		// Integer codec_status;
		// codec_status = Speex.getStatus();

		// Create the service observer. If such exists, onServiceBound will
		// register it.
		if (savedInstanceState != null) {
			mServiceObserver = new ServerServiceObserver();
		}

		// make sure the directory we plan to store the recording in exists
		File direct = new File(Environment.getExternalStorageDirectory()
				+ "/Rallee");

		if (!direct.exists()) {
			if (direct.mkdir())
				; // directory is created;

		}

		// deleteAllFbGroupsFromDb();
		// getAllUserFBGroups();
		// fetchAllFbNameGroups();
		// getIDFromFbGroup_table("Selo Vlasi");

		/**
		 * Finding that Mic available on kindle device or not.
		 * **/
		AudioManager am = (AudioManager) getSystemService(AUDIO_SERVICE);
		// Log.e("-----at LogInActivity--------","------Oncreate-----receiverFilter="+am.isWiredHeadsetOn());
		// //true
		PackageManager pm = this.getPackageManager();
		boolean micAvailable = pm
				.hasSystemFeature(PackageManager.FEATURE_MICROPHONE);
		// Log.e("-----at LogInActivity--------","------Oncreate-----micAvailable="+micAvailable);
		if (!micAvailable) {

			if (!am.isWiredHeadsetOn()) {
				AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
						LogInActivity.this);
				alertDialogBuilder.setTitle("Warning!");
				alertDialogBuilder
						.setMessage(
								"User needs to connect a microphone to the device to use")
						.setCancelable(false)
						.setPositiveButton("Ok",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int id) {
										dialog.cancel();
									}
								});

				AlertDialog alertDialog = alertDialogBuilder.create();
				alertDialog.show();
			} else {
				Log.e("check handsfree detected", "---------");
			}

		} else {
			Log.e("Microphone detected", "---------");
		}

	}

	@Override
	protected void onResume() {
		super.onResume();

		dismissDialog();

		// Speexrec.open(settings.getAudioQuality());

		// if(mService != null && RadioRuntService.SWITCH_SERVER != 99){
		// if(RadioRuntService.SWITCH_SERVER == 1 && Utility.switchChannel!=
		// null && RadioRuntService.tempNetState){
		// showDialog("Joining channel "+Utility.switchChannel.name.subSequence(5,
		// Utility.switchChannel.name.length())+"...", false);
		// registerConnectionReceiver();
		// }else if(RadioRuntService.SWITCH_SERVER == 0 &&
		// RadioRuntService.tempNetState){
		// showDialog("Resuming Rallee...", false);
		// registerConnectionReceiver();
		// }else if(!RadioRuntService.tempNetState){
		// showDialog("Waiting for internet connection...", true);
		// registerConnectionReceiver();
		// }else{
		// dismissDialog();
		// }
		// Globals.logInfo(this,"RadioRuntService.HOME_ACTIVITY_PAUSED " +
		// String.valueOf(RadioRuntService.HOME_ACTIVITY_PAUSED));
		// }
		RadioRuntService.HOME_ACTIVITY_PAUSED = 0;

		if (RadioRuntService.USER_IS_BANNED) {

			reportUserDialog();

			RadioRuntService.USER_IS_BANNED = false;
			// RadioRuntConnection.CONNECTION_ERROR = 0;
		}

		Globals.logDebug(this, "LoginAC onResume");
		// TODO Auto-generated method stub
		// RalleeApp.getInstance().extendFacebookTokenIfNeeded(); FBTOBECHANGED

		firstConnectionToServerDone = false;
		// fbLoginButton.setVisibility(View.VISIBLE);
		if (serverIpAddress == null && RadioRuntService.tempNetState) {
			fbLoginButton.setEnabled(false);
			// fbLoginButton.setVisibility(View.INVISIBLE);
			tvStatus.setVisibility(View.VISIBLE);
			tvStatus.setText(R.string.waiting_for_internet);
			pbStatus.setVisibility(View.VISIBLE);
		} else {
			fbLoginButton.setEnabled(true);
			// fbLoginButton.setVisibility(View.VISIBLE);
			tvStatus.setVisibility(View.INVISIBLE);
			pbStatus.setVisibility(View.INVISIBLE);
		}

		fbLoginButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				Session session = Session.getActiveSession();
				if (session == null) {
					session = new Session(LogInActivity.this);
					Session.setActiveSession(session);
				}
				if (session.isOpened()) {
					userDataRequest();
				} else {
					if (!session.isOpened() && !session.isClosed()) {

						OpenRequest openRequest = new Session.OpenRequest(
								LogInActivity.this)
								.setPermissions(readPermissions);
						List<String> permissions = session.getPermissions();
						if (!isSubsetOf(readPermissions, permissions)) {
							openRequest.setPermissions(readPermissions)
									.setCallback(fbStatusCallback);
						}
						session.openForRead(openRequest);
					} else {
						Session.openActiveSession(LogInActivity.this, true,
								fbStatusCallback);
					}
				}
			}
		});
		// fbLoginButton.setReadPermissions(readPermissions);
		// fbLoginButton.setSessionStatusCallback(this);
		// fbLoginButton.setLoginBehavior(SessionLoginBehavior.SSO_ONLY);

		handlerCounter = 0;
		mHandler.sendEmptyMessage(MSG_GETIPADDRESS_SERVICE);
		RegisterBroadcastReceivers();
		// Integer codec_status;
		// codec_status = Speex.getStatus();

		// FBTOBECHANGED
		// if (RalleeApp.getInstance().getFacebook() != null
		// && !RalleeApp.getInstance().getFacebook().isSessionValid()) {
		// mText.setText(R.string.label_loged_out);
		// }
		// Toast.makeText(
		// Nebojsa added 1 line
		if (settings.isTtsEnabled()) {
			TtsProvider.init(getApplicationContext());
		} else {
			TtsProvider.close();
		}
		// Ivan komentarisao
		// LogOutFBAccount logOutFb = fbLoginButton.new LogOutFBAccount();
		// logOutFb.logOut();
	}

	// public void getAllUserFBGroups() {
	// // Thread initBkgd = new Thread(new Runnable()
	// // {
	// // @Override
	// // public void run() {
	// try {
	//
	// JSONObject jObject;
	// jObject = new JSONObject(RalleeApp.getInstance().getFacebook()
	// .request("me"));
	// String fb_id = jObject.getString("id");
	//
	// JSONObject jObjectGroups = new JSONObject(RalleeApp.getInstance()
	// .getFacebook().request(fb_id + "/groups"));
	// JSONArray jsonGroupsArray = (JSONArray) jObjectGroups.get("data");
	// String id_groupe = null;
	// String name_groupe = null;
	// ContentValues values = new ContentValues();
	//
	// for (int i = 0; i < jsonGroupsArray.length(); i++) {
	// id_groupe = (String) ((JSONObject) jsonGroupsArray.get(i))
	// .get("id");
	// name_groupe = (String) ((JSONObject) jsonGroupsArray.get(i))
	// .get("name");
	// values.put(DbContentProvider.FB_GROUP_COL_ID, id_groupe);
	// values.put(DbContentProvider.FB_GROUP_COL_NAME, name_groupe);
	//
	// Uri uri = getApplicationContext().getContentResolver().insert(
	// DbContentProvider.CONTENT_URI_FB_GROUPS_TABLE, values);
	//
	// Globals.logDebug(this, "FB group id " + id_groupe);
	// Globals.logDebug(this, "FB group name " + name_groupe);
	// }
	// } catch (MalformedURLException e) {
	// e.printStackTrace();
	// } catch (IOException e) {
	// e.printStackTrace();
	// } catch (JSONException e) {
	// e.printStackTrace();
	// }
	// }

	// });
	// initBkgd.start();
	// }

	public void deleteAllFbGroupsFromDb() {
		getApplicationContext().getContentResolver().delete(
				DbContentProvider.CONTENT_URI_FB_GROUPS_TABLE, null, null);
	}

	public ArrayList<String> fetchAllFbNameGroups() {
		// Reading inserted data - table-> fb_groups [_id, name]
		ArrayList<String> fbGroup_name = new ArrayList<String>();

		Cursor c = getApplicationContext().getContentResolver().query(
				DbContentProvider.CONTENT_URI_FB_GROUPS_TABLE, null, null,
				null, null);

		if (c != null) {
			if (c.getCount() != 0) {
				do {
					fbGroup_name
							.add(c.getString(c
									.getColumnIndex(DbContentProvider.FB_GROUP_COL_NAME)));
				} while (c.moveToNext());
			}
		}
		c.close();

		Globals.logDebug(this, fbGroup_name.get(0));

		return fbGroup_name;
	}

	public ArrayList<String> fetchAllFbIDGroups() {
		// Reading inserted data - table-> fb_groups [_id]
		ArrayList<String> fbGroup_id = new ArrayList<String>();

		Cursor c = getApplicationContext().getContentResolver().query(
				DbContentProvider.CONTENT_URI_FB_GROUPS_TABLE, null, null,
				null, null);

		if (c != null) {
			if (c.getCount() != 0) {
				do {
					fbGroup_id
							.add(c.getString(c
									.getColumnIndex(DbContentProvider.FB_GROUP_COL_ID)));
				} while (c.moveToNext());
			}
		}
		c.close();

		Globals.logDebug(this, fbGroup_id.get(0));

		return fbGroup_id;
	}

	public String getIDFromFbGroup_table(String name_of_groupe) {
		String id = null;
		// Cursor c = getApplicationContext().getContentResolver().query(
		// DbContentProvider.CONTENT_URI_FB_GROUPS_TABLE,
		// new String[] { DbContentProvider.FB_GROUP_ID,
		// DbContentProvider.FB_GROUP_NAME},
		// DbContentProvider.FB_GROUP_NAME + " = " + name_of_groupe,
		// null, null);
		Cursor c = getApplicationContext().getContentResolver().query(
				DbContentProvider.CONTENT_URI_FB_GROUPS_TABLE, null, null,
				null, null);

		String name = null;
		if (c != null) {
			if (c.getCount() != 0) {
				do {
					name = c.getString(c
							.getColumnIndex(DbContentProvider.FB_GROUP_COL_NAME));
					if (name.compareTo(name_of_groupe) == 0) {
						id = c.getString(c
								.getColumnIndex(DbContentProvider.FB_GROUP_COL_ID));
						break;
					}
				} while (c.moveToNext());
			}
		}
		c.close();
		Globals.logDebug(this, "getIDFromFbGroup_table " + id);
		return id;
	}

	// FBTOBECHANGED
	// public void logOutFbAccount() {
	// LogOutFBAccount logOutFb = fbLoginButton.new LogOutFBAccount();
	// logOutFb.logOut();
	// }

	@Override
	protected void onPause() {
		Globals.logDebug(this, "LoginAC onPause");
		unregisterConnectionReceiver();
		UnregisterBroadcastReceivers();
		super.onPause();
	}

	// @Override
	// public void onActivityResult(int requestCode, int resultCode, Intent
	// data) {
	// switch (requestCode) {
	// case AUTHORIZE_ACTIVITY_RESULT_CODE: {
	// RalleeApp.getInstance().getFacebook()
	// .authorizeCallback(requestCode, resultCode, data);
	// break;
	// }
	//
	// }
	// }

	@Override
	protected final void onDestroy() {
		Globals.logDebug(this, "LoginAC onDestroy");
		TtsProvider.close();
		mMwCommLogic.unregisterSetInsertUserServiceReceiver();
		mMwCommLogic.unregisterGetOver18ServiceReceiver();
		mMwCommLogic.unregisterGetRandomServiceReceiver();

		// if (dbAdapter!=null) {
		// dbAdapter.close();
		// }
		// protection. Kill service if exist
		// if(mService != null){
		// mService.stopSelf();
		// // mService.setPauseMumProtocolState(true);
		// }
		super.onDestroy();
	}

	@Override
	protected void onDisconnected() {
		if (mService != null) {
			Globals.logDebug(this, "ON DISCONNECTED");
			// Toast.makeText(getApplicationContext(), "Hi Testing",
			// Toast.LENGTH_LONG).show();
			// // mService.backgroundServiceCheck();
			// final Intent intent = new Intent(Intent.ACTION_MAIN, null);
			// intent.addCategory(Intent.CATEGORY_LAUNCHER);
			// final ComponentName cn = new
			// ComponentName("com.android.settings",
			// "com.android.settings.wifi.WifiSettings");
			// intent.setComponent(cn);
			// intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			// startActivity( intent);
		}
	}

	@Override
	protected void onConnected() {
		// Toast.makeText(getApplicationContext(), "Hi Maidul",
		// Toast.LENGTH_LONG).show();
		// mService.sendListsForFiltering();

		/*
		 * set the connected or disconnected to the Rallee network state so the
		 * client can start from the LoginActivity or HomeActivity
		 */
		RadioRuntService.DISCONNECT_FROM_RALLEE = false;
		settings.setExitCode(false);

		// ///////// write my username to users table on db
		// ///////// at mw
		mMwCommLogic.registerSetInsertUserServiceReceiver();
		if (RalleeApp.getInstance().getRalleeUID().equals("ralleeUID")) {
			return;
		}
		String jsonString = "";
		try {
			JSONObject json = new JSONObject();
			json.put("user_id", RalleeApp.getInstance().getRalleeUID());
			json.put("name", RalleeApp.getInstance().getFullName());
			json.put("flag", Utility.networkPrefix);
			json.put("first_release", RalleeApp.getInstance()
					.getRalleeRelease());
			json.put("old_release", RalleeApp.getInstance().getRalleeRelease());
			json.put("new_release", RalleeApp.getInstance().getRalleeRelease());

			json.put("picture_url", RalleeApp.getInstance().getPicUrl());

			// JSONArray jsonA = new JSONArray();
			// jsonA.put(json);
			jsonString = json.toString();
		} catch (JSONException e) {
			e.printStackTrace();
		}

		RRServerProxyHelper.startSetInsertUserService(this, jsonString);

		Settings settings = new Settings(getApplicationContext());

		String over18 = settings.getOver18();
		if (over18.equals(Settings.RANDOM_PREF_DEFAULT)) {
			try {
				JSONObject json = new JSONObject();
				json.put("user_id", RalleeApp.getInstance().getRalleeUID());
				// JSONArray jsonA = new JSONArray();
				// jsonA.put(json);
				jsonString = json.toString();
			} catch (JSONException e) {
				e.printStackTrace();
			}

			mMwCommLogic.registerGetOver18ServiceReceiver();
			RRServerProxyHelper.startGetOver18Service(this, jsonString);

			mMwCommLogic.registerGetRandomServiceReceiver();
			RRServerProxyHelper.startGetRandomService(this, jsonString);

		} else if (over18.equals(Settings.RANDOM_PREF_YES)) {
			String gorandom = settings.getGoRandom();
			if (gorandom.equals(Settings.RANDOM_PREF_DEFAULT)) {
				mMwCommLogic.registerGetRandomServiceReceiver();
				RRServerProxyHelper.startGetRandomService(this, jsonString);
			}
		}

		// ///////////////////////////////////////////////////////

		final Intent i = new Intent(this, HomeActivity.class);
		i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		// if(!Utility.channelName.equals("")){
		// i.putExtra(HomeActivity.HAS_CALL, true);
		// }
		startActivity(i);
		Globals.logDebug(this, "LoginAC onConnected");
		dismissDialog();
		finish();
	}

	@Override
	protected void onReconnecting() {
	}

	// ///////////////////////////////////////////////////////////////////////////////
	// //////////////////////// END OF ANDROID METHODS
	// //////////////////////////////
	// ///////////////////////////////////////////////////////////////////////////////
	// ///////////////////////////////////////////////////////////////////////////////
	// /////////////////////////// GUI Methods
	// ///////////////////////////////////
	// ///////////////////////////////////////////////////////////////////////////////

	// ////////////////////////////////////////////////////////////////////////////////////
	// /////////////////////////// END OF GUI Methods
	// /////////////////////////////////
	// ////////////////////////////////////////////////////////////////////////////////////
	// ////////////////////////////////////////////////////////////////////////////////////
	// ///////////////////////////// FB METHODS
	// ///////////////////////////////////////////
	// ////////////////////////////////////////////////////////////////////////////////////

	/*
	 * The Callback for notifying the application when authorization succeeds or
	 * fails.
	 */
	// FBTOBECHANGED
	// public class FbAPIsAuthListener implements AuthListener, LogoutListener {
	//
	// @Override
	// public void onAuthSucceed() {
	// Globals.logDebug(this, "FbAPIsAuthListener ");
	// requestUserData();
	// SessionStore.save(RalleeApp.getInstance().getFacebook(),
	// getApplicationContext());
	// }
	//
	// @Override
	// public void onAuthFail(String error) {
	// mText.setText(getString(R.string.label_loging_failed) + error);
	// }
	//
	// @Override
	// public void onLogoutBegin() {
	// mText.setText(R.string.label_loging_out);
	// }
	//
	// @Override
	// public void onLogoutFinish() {
	// SessionStore.clear(getApplicationContext());
	// mText.setText(R.string.label_loged_out);
	// }
	// }

	/*
	 * Callback for fetching current user's name, picture, uid.
	 */
	public class UserDataCallback implements Request.Callback {

		int numOfRet = 0;
		final int MAX_NUM_OF_RET = 5;

		@Override
		public void onCompleted(Response response) {
			GraphObject data;
			Globals.logDebug(this, "FB  OnCompleteUSerRequestListener "
					+ response);
			if (response.getError() == null) {
				try {
					data = response.getGraphObject();
					Globals.logDebug(this, "FB Response: " + data.toString());
					JSONArray users = ((JSONArray) data.asMap().get("data"));
					JSONObject user = users.getJSONObject(0);

					if (user.has("uid")) {
						Utility.fbId = user.getString("uid");
					}
					//maidul change
					final String picSquareUrl = user.getString("pic_square");
					final String name = user.getString("name");
					Name=name;
					final String gender = user.getString("sex");
					final String birthday = user.getString("birthday_date");
					final String locale = user.getString("locale");

					if (user.has("current_location")
							&& !user.isNull("current_location")) {
						final JSONObject current_location = user
								.getJSONObject("current_location");
					}

					String[] groups = { Utility.testGroup };
					String userUID = Utility.buildUsername(
							Utility.networkPrefix, Utility.fbId, groups);

					RalleeApp.getInstance().setRalleeUID(
							Utility.parseSNData(userUID).getAsString(
									Utility.RALLEE_ID));

					RalleeApp.getInstance().setFullName(name);
					RalleeApp.getInstance().setPicUrl(picSquareUrl);
					RalleeApp.getInstance().setFBLocale(locale);

					mHandler.post(new Runnable() {
						@Override
						public void run() {
							mText.setText(getString(R.string.label_welcome)
									+ "\n" + name + "!");
						}
					});

					if (!firstConnectionToServerDone) {
						firstConnectionToServerDone = true;
						if (Utility.testServerAddress != null) {
							serverIpAddress = /*
											 * "107.21.101.95"
											 */Utility.testServerAddress;
						} else {
							serverIpAddress = /* "107.21.101.95" */Utility.bestServer;
						}
						// serverIpAddress = "107.21.101.95";
						while (serverIpAddress == null) {
							try {
								Thread.sleep(2000);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
						connectServer(1);

					} else {
					}

				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		void retry() {
			if (numOfRet < MAX_NUM_OF_RET) {
				numOfRet++;
				firstConnectionToServerDone = false;
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						mText.setText(R.string.label_retrying);
					}
				});
				mHandler.sendEmptyMessage(MSG_REQ_USERDATA);
			} else {
				numOfRet = 0;
				firstConnectionToServerDone = false;
				fbLoginButton.setEnabled(true);
				// fbLoginButton.setVisibility(View.VISIBLE);
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						mText.setText(R.string.label_try_again);
					}
				});
			}
		}

	}

	private class LogoutRequestListener extends BaseRequestListener {
		@Override
		public void onComplete(String response, final Object state) {
			/*
			 * callback should be run in the original thread, not the background
			 * thread
			 */
			mHandler.post(new Runnable() {
				@Override
				public void run() {
					SessionEvents.onLogoutFinish();
				}
			});
		}
	}

	/*
	 * Request user name, and picture to show on the main screen.
	 */
	public void userDataRequest() {
		Intent service = new Intent(this, RequestFBData.class);
		startService(service);

		mText.setText(R.string.label_fetching_name_and_pic);

		String fqlQuery = "SELECT uid, name, pic_square, sex, birthday_date, locale, current_location FROM user WHERE uid = me()";
		Bundle params = new Bundle();
		params.putString("q", fqlQuery);

		Callback userDataCallback = new UserDataCallback();
		Request r = new Request(Session.getActiveSession(), "/fql", params,
				HttpMethod.GET, userDataCallback);
		r.executeAsync();
		// RalleeApp.getInstance().getAsyncFacebookRunner()
		// .request("me", params, new UserRequestListener());

		fbLoginButton.setEnabled(false);
		// fbLoginButton.setVisibility(View.INVISIBLE);

		// fbLoginButton.setVisibility(View.INVISIBLE);
	}

	// ///////////////////////////////////////////////////////////////////////////////////////
	// ////////////////////////// END OF FB METHODS
	// /////////////////////////////////////////
	// ///////////////////////////////////////////////////////////////////////////////////////
	// ///////////////////////////////////////////////////////////////////////////////////////
	// ////////////////////////// MUMBLE CONNECTION
	// ////////////////////////////////////////
	// ///////////////////////////////////////////////////////////////////////////////////////

	private void RegisterBroadcastReceivers() {
		mGetServerIpAddressServiceReceiver = new GetServerIpAddressServiceReceiver();

		// locationReceiver = new RRLocationServiceReceiver();
		RRServerProxyHelper.registerGetServerIpAddressReceiver(this,
				mGetServerIpAddressServiceReceiver);
		mMwCommLogic.registerRegisterToAppServiceReceiver();
		mMwCommLogic.registerUnregisterToAppServiceReceiver();
		// IntentFilter filter = new
		// IntentFilter(RRLocationServiceReceiver.ACTION_RESP);
		// filter.addCategory(Intent.CATEGORY_DEFAULT);
		// registerReceiver(locationReceiver, filter);
	}

	private void UnregisterBroadcastReceivers() {
		unregisterReceiver(mGetServerIpAddressServiceReceiver);
		mMwCommLogic.unregisterRegisterToAppServiceReceiver();
		mMwCommLogic.unregisterUnregisterToAppServiceReceiver();
		// unregisterReceiver(locationReceiver);
	}

	private class ServerServiceObserver extends BaseServiceObserver {
		@Override
		public void onConnectionStateChanged(final int state)
				throws RemoteException {
			checkConnectionState();
		}
	}

	/**
	 * Monitors the connection state after clicking a server entry.
	 */
	private final boolean checkConnectionState() {
		switch (mService.getConnectionState()) {
		case RadioRuntService.CONNECTION_STATE_CONNECTING:
			Globals.logDebug(this, "RadioRuntService.STATE_CONNECTING");
			pbStatus.setVisibility(View.VISIBLE);
			tvStatus.setVisibility(View.VISIBLE);
			tvStatus.setText(R.string.label_connecting);
			Globals.logInfo(this, "Connecting");
		case RadioRuntService.CONNECTION_STATE_SYNCHRONIZING:
			pbStatus.setVisibility(View.VISIBLE);
			tvStatus.setVisibility(View.VISIBLE);
			tvStatus.setText(R.string.label_connecting);
			Globals.logInfo(this, "Synchronizing");
		case RadioRuntService.CONNECTION_STATE_CONNECTED:
			fbLoginButton.setEnabled(false);
			// fbLoginButton.setVisibility(View.INVISIBLE);
			tvStatus.setVisibility(View.VISIBLE);
			pbStatus.setVisibility(View.INVISIBLE);
			tvStatus.setText(R.string.label_connected);
			unregisterConnectionReceiver();
			Globals.logDebug(this, "RadioRuntService.STATE_CONNECTED 1");
			return true;
		case RadioRuntService.CONNECTION_STATE_DISCONNECTED:
			fbLoginButton.setEnabled(true);
			// fbLoginButton.setVisibility(View.VISIBLE);
			// fbLoginButton.setVisibility(View.VISIBLE);
			tvStatus.setVisibility(View.VISIBLE);
			pbStatus.setVisibility(View.INVISIBLE);
			tvStatus.setText(R.string.label_disconnected);
			Globals.logDebug(this, "RadioRuntService.STATE_DISCONNECTED 1");
			// TODO: Error message checks.
			// This can be reached if the user leaves ServerList after clicking
			// server but before the connection intent reaches the service.
			// In this case the service connects and can be disconnected before
			// the connection state is checked again.
			break;
		case RadioRuntService.CONNECTION_STATE_RECONNECTING:
			Globals.logDebug(this, "RadioRuntService.STATE_RECONNECTING 1");
			fbLoginButton.setEnabled(true);
			// fbLoginButton.setVisibility(View.VISIBLE);
			// fbLoginButton.setVisibility(View.VISIBLE);
			tvStatus.setVisibility(View.VISIBLE);
			pbStatus.setVisibility(View.INVISIBLE);
			tvStatus.setText(R.string.label_reconnecting);
			Globals.logInfo(this, "Reconnecting");
			break;
		default:
			Assert.fail("Unknown connection state");
		}
		return false;
	}

	private void registerConnectionReceiver() {
		if (mServiceObserver != null) {
			return;
		}
		mServiceObserver = new ServerServiceObserver();

		if (mService != null) {
			mService.registerObserver(mServiceObserver);
		}
	}

	private void unregisterConnectionReceiver() {
		if (mServiceObserver == null) {
			return;
		}

		if (mService != null) {
			mService.unregisterObserver(mServiceObserver);
			mServiceObserver = null;// maidul change
		}

		// mServiceObserver = null;
	}

	/**
	 * Starts connecting to a server.
	 * 
	 * @param id
	 */
	protected final void connectServer(final long id) {
		// final Cursor c = dbAdapter.fetchServer(id);
		final String host = /* "195.252.93.46"; */serverIpAddress;// c.getString(c.getColumnIndexOrThrow(DbAdapter.SERVER_COL_HOST));
		final int port = 64738;// c.getInt(c.getColumnIndexOrThrow(DbAdapter.SERVER_COL_PORT));

		final String username = RalleeApp.getInstance().getRalleeUID();// c.getString(c.getColumnIndexOrThrow(DbAdapter.SERVER_COL_USERNAME));
		final String password = RalleeApp.getInstance().getRalleeUID();// c.getString(c.getColumnIndexOrThrow(DbAdapter.SERVER_COL_PASSWORD));
		// c.close();

		Globals.logDebug(this, "" + username);

		settings.setConnectionIntent(host, port, username, password);

		registerConnectionReceiver();

		connectionIntent = new Intent(this, RadioRuntService.class);
		connectionIntent.setAction(RadioRuntService.ACTION_CONNECT);
		connectionIntent.putExtra(RadioRuntService.EXTRA_HOST, host);
		connectionIntent.putExtra(RadioRuntService.EXTRA_PORT, port);
		connectionIntent.putExtra(RadioRuntService.EXTRA_USERNAME, username);
		connectionIntent.putExtra(RadioRuntService.EXTRA_PASSWORD, password);

		startService(connectionIntent);
	}

	// protected final void switchServer(RRChannels channel) {
	// // final Cursor c = dbAdapter.fetchServer(id);
	// final String host =
	// channel.serverIpAdr;//c.getString(c.getColumnIndexOrThrow(DbAdapter.SERVER_COL_HOST));
	// final int port =
	// Integer.valueOf(channel.port);//c.getInt(c.getColumnIndexOrThrow(DbAdapter.SERVER_COL_PORT));
	// final String username =
	// Utility.fbId.toString();//c.getString(c.getColumnIndexOrThrow(DbAdapter.SERVER_COL_USERNAME));
	// final String password =
	// Utility.fbId.toString();//c.getString(c.getColumnIndexOrThrow(DbAdapter.SERVER_COL_PASSWORD));
	// // c.close();
	//
	// registerConnectionReceiver();
	// changeServerIntent = new Intent(this, RadioRuntService.class);
	// changeServerIntent.setAction(RadioRuntService.ACTION_CONNECT);
	// changeServerIntent.putExtra(RadioRuntService.EXTRA_HOST, host);
	// changeServerIntent.putExtra(RadioRuntService.EXTRA_PORT, port);
	// changeServerIntent.putExtra(RadioRuntService.EXTRA_USERNAME, username);
	// changeServerIntent.putExtra(RadioRuntService.EXTRA_PASSWORD, password);
	// mService.switchServer(changeServerIntent);
	// }

	public class GetServerIpAddressServiceReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {

			String errMsg = RRServerProxyHelper.getServerIpAddressError(
					context, intent);
			if (errMsg.equals("")) {
				Utility.bestServer = RRServerProxyHelper
						.getServerIpAddressResponse(context, intent);
				if (Utility.bestServer.equals("xxx")) {
					Intent market = new Intent(Intent.ACTION_VIEW);
					market.setData(Uri
							.parse("market://details?id=com.radiorunt"));
					startActivity(market);
					// tvStatus.setVisibility(View.VISIBLE);
					// tvStatus.setText(R.string.please_update);
					pbStatus.setVisibility(View.INVISIBLE);
				} else {
					// final long serverId =
					// this.getIntent().getLongExtra("serverId", -1);
					tvStatus.setVisibility(View.INVISIBLE);
					pbStatus.setVisibility(View.INVISIBLE);
					// if (serverId != -1 ){
					fbLoginButton.setEnabled(true);
					// fbLoginButton.setVisibility(View.VISIBLE);
					// }
				}
			} else if (errMsg.equals("xxx")) {
				tvStatus.setVisibility(View.VISIBLE);
				tvStatus.setText(R.string.please_update);
				pbStatus.setVisibility(View.INVISIBLE);
			} else {
				mHandler.sendEmptyMessageDelayed(MSG_GETIPADDRESS_SERVICE, 3000);
			}
		}
	}

	// public class RRLocationServiceReceiver extends BroadcastReceiver {
	// public static final String ACTION_RESP = "LOCATION_MESSAGE_PROCESSED";
	// public static final String ACTION_FAILED = "LOCATION_MESSAGE_FAILED";
	// private GeoFinder geoFind;
	//
	// @Override
	// public void onReceive(Context context, Intent intent) {
	// Log.i("msgLocation", "Usao u receiver");
	// if (intent.hasExtra(RRLocationService.PARAM_OUT_MSG)) {
	//
	// String location = intent
	// .getStringExtra(RRLocationService.PARAM_OUT_MSG);
	// String[] coordinates = location.split(",");
	// double lat = Double.parseDouble(coordinates[0]);
	// double lng = Double.parseDouble(coordinates[1]);
	//
	// geoFind = new GeoFinder();
	// List<String> address;
	// address = geoFind.geocode(context, lat, lng);
	// Log.i("msgLocation", address.toString());
	//
	// }
	// }
	//
	// }

	// public class SendPushMessageServiceReceiver extends BroadcastReceiver {
	//
	// @Override
	// public void onReceive(Context context, Intent intent) {
	// String errMsg = RRServerProxyHelper.sendPushMessageError(context,
	// intent);
	// if (errMsg.equals("")) {
	// String response = RRServerProxyHelper.sendPushMessageResponse(context,
	// intent);
	// Toast.makeText(getApplicationContext(), response,
	// Toast.LENGTH_SHORT).show();
	//
	// } else {
	// Toast.makeText(getApplicationContext(), errMsg,
	// Toast.LENGTH_SHORT).show();
	// }
	// }
	// }

	@Override
	protected void onSaveInstanceState(final Bundle outState) {
		super.onSaveInstanceState(outState);
		if (mServiceObserver != null) {
			outState.putBoolean(STATE_WAIT_CONNECTION, true);
		}
	}

	@Override
	protected void onServiceBound() {
		if (mServiceObserver != null) {
			if (!checkConnectionState()) {
				mService.registerObserver(mServiceObserver);
			}
		}
	}

	// /////////////////////////////////////////////////////////////////////////////////////
	// //////////////////////// MENU OPTIONS
	// ////////////////////////////////////////////
	// /////////////////////////////////////////////////////////////////////////////////////
	@Override
	public final boolean onCreateOptionsMenu(final Menu menu) {
		// super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.main_menu, menu);
		return true;
	}

	@Override
	public final boolean onMenuItemSelected(final int featureId,
			final MenuItem item) {
		Intent i;
		switch (item.getItemId()) {
		case R.id.signOutFromFb:
			C2DMessaging.unregister(this /* the application context */);
			/* Kimi */
			// DbAdapter adapter = new DbAdapter(getApplicationContext());
			// adapter.open();
			// adapter.clearAllTables();
			// adapter.close();
			// logOutFbAccount(); FBTOBECHANGED

			getApplicationContext().getContentResolver().delete(
					DbContentProvider.CONTENT_URI_SERVER_TABLE, null, null);
			getApplicationContext().getContentResolver().delete(
					DbContentProvider.CONTENT_URI_USER_TABLE, null, null);
			getApplicationContext().getContentResolver()
					.delete(DbContentProvider.CONTENT_URI_MISSED_CALL_TABLE,
							null, null);
			getApplicationContext().getContentResolver().delete(
					DbContentProvider.CONTENT_URI_CHANNEL_TABLE, null, null);
			getApplicationContext().getContentResolver().delete(
					DbContentProvider.CONTENT_URI_CHANNEL_CATEGORY_TABLE, null,
					null);
			getApplicationContext().getContentResolver().delete(
					DbContentProvider.CONTENT_URI_FB_GROUPS_TABLE, null, null);
			getApplicationContext().getContentResolver().delete(
					DbContentProvider.CONTENT_URI_GROUP_MEMBERS_TABLE, null,
					null);
			getApplicationContext().getContentResolver().delete(
					DbContentProvider.CONTENT_URI_PRIVATE_GROUP_TABLE, null,
					null);
			getApplicationContext().getContentResolver().delete(
					DbContentProvider.CONTENT_URI_CALL_HISTORY_TABLE, null,
					null);

			settings.clearSettings();

			SharedPreferences preferences = getSharedPreferences("Mypref", 0);
			preferences.edit().clear().commit();
			// maidul Add
			settings.setExitCode(true);
			mText.setText(R.string.label_loged_out);
			// Toast.makeText(this, "Prosao SignOut form DB", 1000).show();
			return true;
		case R.id.FAQ:
			Intent faq = new Intent(LogInActivity.this, FaqActivity.class);
			startActivity(faq);
			return true;
			// case R.id.selectServer:
			// showSelectServerDialog();
			// return true;
		default:
			return super.onMenuItemSelected(featureId, item);
		}
	}

	/*
	 * public class GetServerIdServiceReceiver extends BroadcastReceiver {
	 * 
	 * @Override public void onReceive(Context context, Intent intent) {
	 * 
	 * String errMsg = RRServerProxyHelper.getServerIdError(context, intent); if
	 * (errMsg.equals("")) { String serverIdString =
	 * RRServerProxyHelper.getServerIdResponse(context, intent);
	 * 
	 * serverId = Integer.valueOf(serverIdString);
	 * Toast.makeText(getApplicationContext(), "Got server id:" +
	 * serverId.toString(), Toast.LENGTH_SHORT).show();
	 * 
	 * if (serverIpAddress != null ){ buttonLogIn.setEnabled(true); } } else {
	 * Toast.makeText(getApplicationContext(), errMsg,
	 * Toast.LENGTH_SHORT).show(); } }
	 * 
	 * }
	 */
	public void showDialog(String message, boolean cancelable) {
		dialog = new ProgressDialog(this);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setCancelable(cancelable);
		dialog.setMessage(message);

		mHandler.post(new Runnable() {
			@Override
			public void run() {
				dialog.show();
			}
		});
	}

	public static void dismissDialog() {
		if (dialog != null) {
			dialog.dismiss();
			dialog = null;
		}
	}

	private void reportUserDialog() {
		final Dialog dialog = new Dialog(this,
				android.R.style.Theme_Translucent_NoTitleBar);

		dialog.setContentView(R.layout.confirm_random_dialog);

		TextView textMessage = (TextView) dialog
				.findViewById(R.id.dialog_random_text);
		TextView textTitle = (TextView) dialog
				.findViewById(R.id.dialog_random_title);

		textTitle.setVisibility(View.GONE);
		textMessage.setText(getResources()
				.getString(R.string.reported_feature1)
				+ "\n\n"
				+ getResources().getString(R.string.reported_feature2));

		textMessage.setLinksClickable(true);

		Button button1 = (Button) dialog
				.findViewById(R.id.dialog_random_btnYes);
		Button button2 = (Button) dialog.findViewById(R.id.dialog_random_btnNo);
		button1.setVisibility(View.GONE);
		button2.setVisibility(View.GONE);

		dialog.show();

		// Toast.makeText(this, "You are banned from Rallee", 1500).show();
	}

	private void newUsersClosed() {
		final Dialog dialog = new Dialog(this,
				android.R.style.Theme_Translucent_NoTitleBar);

		dialog.setContentView(R.layout.add_name_dialog);

		TextView textTitle = (TextView) dialog.findViewById(R.id.tvLabel);
		textTitle.setTypeface(null, Typeface.NORMAL);

		textTitle.setText(R.string.invite_closed);

		Button button1 = (Button) dialog.findViewById(R.id.btnCreate);
		button1.setText(R.string.label_send);
		button1.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Put send email address function here
				Toast.makeText(
						LogInActivity.this,
						"I want Rallee "
								+ ((EditText) dialog.findViewById(R.id.etName))
										.getText().toString(),
						Toast.LENGTH_SHORT).show();
				dialog.dismiss();

			}
		});
		((Button) dialog.findViewById(R.id.btnCancel)).setVisibility(View.GONE);
		EditText etEmail = (EditText) dialog.findViewById(R.id.etName);
		etEmail.setTypeface(null, Typeface.NORMAL);
		etEmail.setInputType(InputType.TYPE_CLASS_TEXT
				| InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);

		dialog.show();
	}

	private class SessionStatusCallback implements Session.StatusCallback {

		@Override
		public void call(Session session, SessionState state,
				Exception exception) {
			// Toast.makeText(this, "Session state: " + state, 1000).show();
			// RalleeApp.getInstance().getFBSession();
			if (session != null) {
				if (state.isOpened()) {
					List<String> permissions = session.getPermissions();
					if (!isSubsetOf(readPermissions, permissions)) {
						Session.NewPermissionsRequest newPermissionsRequest = new Session.NewPermissionsRequest(
								LogInActivity.this, readPermissions)
								.setCallback(fbStatusCallback);
						session.requestNewReadPermissions(newPermissionsRequest);
					} else if (!isSubsetOf(publishPermissions, permissions)) {
						Session.NewPermissionsRequest newPermissionsRequest = new Session.NewPermissionsRequest(
								LogInActivity.this, publishPermissions)
								.setCallback(fbStatusCallback);
						session.requestNewPublishPermissions(newPermissionsRequest);
					} else {
						userDataRequest();
					}
					// SessionStore.save(RalleeApp.getInstance().getFacebook(),
					// getApplicationContext());
				} else if (state.equals(SessionState.CLOSED_LOGIN_FAILED)) {
					session.closeAndClearTokenInformation();
				}
			}

		}
	}

	private boolean isSubsetOf(Collection<String> subset,
			Collection<String> superset) {
		for (String string : subset) {
			if (!superset.contains(string)) {
				return false;
			}
		}
		return true;
	}

	// public void showSelectServerDialog() {
	// if (selectServerDialog == null) {
	// final CharSequence[] names = getApplicationContext().getResources()
	// .getStringArray(R.array.server_names);
	// final CharSequence[] addresses = getApplicationContext()
	// .getResources().getStringArray(R.array.server_addresses);
	//
	// AlertDialog.Builder builder = new AlertDialog.Builder(
	// LogInActivity.this);
	// builder.setTitle("Pick a server");
	// builder.setItems(names, new DialogInterface.OnClickListener() {
	// public void onClick(DialogInterface dialog, int item) {
	// Toast.makeText(getApplicationContext(), names[item],
	// Toast.LENGTH_SHORT).show();
	// if (item != 0) {
	// Utility.testServerAddress = addresses[item].toString();
	// Utility.testServerName = names[item].toString();
	// } else {
	// Utility.testServerAddress = null;
	// Utility.testServerName = "";
	// }
	//
	// String groupString = "";
	// if (!Utility.testGroup.equals("")) {
	// groupString += " Group:" + Utility.testGroup;
	// }
	// setTitle("Rallee " + Utility.testServerName + groupString);
	//
	// showSelectGroupDialog();
	//
	// }
	// });
	// selectServerDialog = builder.create();
	// }
	// selectServerDialog.show();
	// }
	//
	// public void showSelectGroupDialog() {
	// if (selectGroupDialog == null) {
	// final CharSequence[] groups = getApplicationContext()
	// .getResources().getStringArray(R.array.test_groups);
	//
	// AlertDialog.Builder builder = new AlertDialog.Builder(
	// LogInActivity.this);
	// builder.setTitle("Pick a group");
	// builder.setItems(groups, new DialogInterface.OnClickListener() {
	// public void onClick(DialogInterface dialog, int item) {
	// Toast.makeText(getApplicationContext(), groups[item],
	// Toast.LENGTH_SHORT).show();
	// if (item != 0) {
	// Utility.testGroup = groups[item].toString();
	// } else {
	// Utility.testGroup = "";
	// }
	//
	// String groupString = "";
	// if (!Utility.testGroup.equals("")) {
	// groupString += " Group:" + Utility.testGroup;
	// }
	// setTitle("Rallee " + Utility.testServerName + groupString);
	// }
	// });
	// selectGroupDialog = builder.create();
	// }
	//
	// selectGroupDialog.show();
	// }
public void name(){
	temp=Name;
}
}
