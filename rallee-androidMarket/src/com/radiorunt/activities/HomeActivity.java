package com.radiorunt.activities;

//import com.crittercism.app.Crittercism;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.TimeZone;

import junit.framework.Assert;

import net.sf.mumble.MumbleProto.ChannelState;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemClock;
import android.provider.CallLog.Calls;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.ToggleButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.crittercism.app.Crittercism;
//import com.google.android.apps.analytics.GoogleAnalyticsTracker;

import com.facebook.FacebookRequestError;
import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.RequestAsyncTask;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.Tracker;
import com.google.android.c2dm.C2DMessaging;
import com.radiorunt.R;
import com.radiorunt.activities.fragments.AddUsersToConversationFragment;
import com.radiorunt.activities.fragments.CallCantrolsFragment;
import com.radiorunt.activities.fragments.FacebookGroupDetailsFragment;
import com.radiorunt.activities.fragments.FacebookGroupsListFragment;
import com.radiorunt.activities.fragments.FriendsDockFragment;
import com.radiorunt.activities.fragments.GroupsFragment;
import com.radiorunt.activities.fragments.InviteToApplicationDialogFragment;
import com.radiorunt.activities.fragments.NotificationFragment;
import com.radiorunt.activities.fragments.OnlineFriendsFragment;
import com.radiorunt.activities.fragments.ParticipantsFragment;
import com.radiorunt.activities.fragments.PeopleFragment;
import com.radiorunt.activities.fragments.PrivateGroupDetailsFragment;
import com.radiorunt.activities.fragments.PrivateGroupsListFragment;
import com.radiorunt.activities.fragments.PublicGroupDetailsFragment;
import com.radiorunt.activities.fragments.PublicGroupsListFragment;
import com.radiorunt.activities.fragments.RightSidePanelFragment;
import com.radiorunt.activities.fragments.TextModePanelFragment;
import com.radiorunt.activities.fragments.VoiceModePanelFragment;
import com.radiorunt.businessobjects.ActionItem;
import com.radiorunt.businessobjects.FBGroupsReceiver;
import com.radiorunt.businessobjects.ListOfCountries;
import com.radiorunt.businessobjects.ListOfCountries.CountryName;
import com.radiorunt.businessobjects.QuickAction;
import com.radiorunt.businessobjects.RRChannels;
import com.radiorunt.businessobjects.RRMessages;
import com.radiorunt.businessobjects.RRPushFilteringMessagePayload;
import com.radiorunt.businessobjects.RRPushMessage;
import com.radiorunt.businessobjects.RRPushMessagePayload;
import com.radiorunt.businessobjects.RRServer;
import com.radiorunt.businessobjects.RRUser;
import com.radiorunt.businessobjects.RandomUser;
import com.radiorunt.facebook.BaseDialogListener;
import com.radiorunt.facebook.FacebookUserDataLoader;
import com.radiorunt.facebook.Utility;
import com.radiorunt.services.BaseServiceObserver;
import com.radiorunt.services.GetGroupsFromFBService;
import com.radiorunt.services.IServiceObserver;
import com.radiorunt.services.RadioRuntService;
import com.radiorunt.services.RequestFBData;
import com.radiorunt.services.TtsProvider;
import com.radiorunt.utilities.CallHistoryReceiver;
import com.radiorunt.utilities.CallState;
import com.radiorunt.utilities.DeviceRegistrar;
import com.radiorunt.utilities.GeoFinder;
import com.radiorunt.utilities.Globals;
import com.radiorunt.utilities.ImageHelper;
import com.radiorunt.utilities.Preferences;
import com.radiorunt.utilities.RRLocationManager;
import com.radiorunt.utilities.RRServerProxyHelper;
import com.radiorunt.utilities.RalleeApp;
import com.radiorunt.utilities.Settings;
import com.radiorunt.utilities.jni.Speex;
import com.radiorunt.utilities.jni.Speexrec;
//import com.radiorunt.utilities.RRLocationServiceReceiver;

import crittercism.android.i;

//import com.radiorunt.utilities.RRLocationServiceReceiver;

public class HomeActivity extends ConnectedFragmentActivity implements
		OnTouchListener, Session.StatusCallback {

	// CONSTANTS
	boolean probe = false;
	UserFBFriendsReceiver userFbReceiver;
	FBGroupsReceiver groupsFbReceiver;
	CallHistoryReceiver mCallHistoryReceiver;
	URL img_value = null;
	private final float dimmedScreenBrightness = 0.01f;
	public static final String CHANNEL_TYPE = "CHANNEL_TYPE";
	public static final String PUSHED_CHANNEL = "PUSHED_CHANNEL";
	public static final String PUSHED_CHANNEL_ID = "PUSHED_CHANNEL_ID";
	public static final String SAVED_STATE_VISIBLE_CHANNEL = "visible_channel";
	private static final int PREF_CHANGED = 1;

	public static final String FB_LIST_ID = "FB_LIST_ID";

	public static final int CALLER_UNANSWERED = 0;
	public static final int CALLER_ACCEPTED = 1;
	// public static final int CALLER_DISMISSED = 2;
	public static final int CALLER_REJECTED = 2;
	public static final int CALLER_CANCELED = 3;
	public static final int CALLER_ENDED = 4;
	private static final CountryName[] COUNTRY_NAMES = CountryName.class
			.getEnumConstants();

	// private final int SET_MESSAGE = 0;
	// private final int DISMISS_MESSAGE = 1;
	// private final int UPDATE_MESSAGE = 2;

	// static constants for click on the user icon state
	private static final int TALK = 1;
	private static final int REPORT_USER = 2;
	private static final int CANCEL_USER_CLICK = 3;
	private static final int CLEAR_MISSED_CALL = 4;
	// //////////////////////////////////////////////////

	private static final int ACTIVITY_LOGIN = 0;

	protected static final long CALL_TIMEOUT = 60 * 1000;

	private static final int[] RANDOM_SEARCH_RADIUS_VALUES = { 1, 5, 10, 20,
			50, 0 };
	// final ArrayList<RRChannels> fbFriendListChannels = new
	// ArrayList<RRChannels>();

	// FLAGS
	// private RRLocationServiceReceiver locationReceiver;
	public static int PREF_CHANGED_ACTIVITY = 0;
	public static int CONNECTED = 0;
	public static int ROOT_ID = 0;
	public static String CURRENT_CH_NAME = "";

	public static int WAS_ON_CHANNEL_LIST = 0;
	public static int ENTER_CHANNEL_ON_CHANNEL_LIST = 0;
	public static int iWasOnFeedbackOrReportActivity = 0;

	// VARIABLES
	public Settings settings;
	RRChannels visibleChannel;
	RRChannels pushedChannel;
	private ToneGenerator tg;
	private ToneGenerator beepGenerator;
	TtsProvider mTts;
	public boolean manualRecord;
	private ProgressDialog mProgressDialog;
	private Dialog mCustomDialog;
	public Dialog mRandomCallDialog;
	public boolean isDialing = false;
	private boolean firstEnter = true;
	private boolean screenIsDimmed = false;
	String invocedChanelName;
	String trimedInvocedChannelName;
	public int swipeMode;
	// private GoogleAnalyticsTracker mGATracker;
	public final static boolean CONVERSATION_MODE_VOICE = true;
	public final static boolean CONVERSATION_MODE_TEXT = false;
	public boolean convMode = CONVERSATION_MODE_VOICE;
	// stopper

	private IServiceObserver protocolServiceObserver;
	// private ParticipantServiceObserver mParticipantceServiceObserver;
	private ConnectionServiceObserver mServiceObserver;
	public MwCommunicationLogic mMwCommLogic;
	private static final String STATE_WAIT_CONNECTION = "com.radiorunt.activities.LogInActivity.WAIT_CONNECTION";
	public final Boolean isThereNewVersion = false;
	ImageButton btnAcceptCall, btnRejectCall;ImageView btnCall;
	// ArrayList<RRUser> myOnlineUsers;
	// ArrayList<RRUser> myOfflineUsers;
	// ArrayList<RRUser> nonRRUsers;
	public List<RRChannels> chList;
	List<RRUser> users;
	public static final Object friendsLock = new Object();
	// private static ArrayList<RRFriendlist> myFriendlists;
	SpinnerAdapter nullAdapter;

	AlphaAnimation fadeOutAnimation;

	private boolean exitAndLogIn = false;
	// LayoutInflater adbInflater;
	// View checkboxLayout;

	static long msgsTimestamp;

	public Boolean deafOn = false;
	String picUrl;
	RRChannels tmpChannel;
	private boolean isChann;
	// private int currentChannelUserCount;
	private int currentFriendCount;
	boolean isThereChann;

	boolean isFirstStart = true;

	Handler mHandler;
	private Handler locationHandler;
	View viewNewFriendlist;
	EditText text;
	private Handler returnMissedCallHandler;
	// private Handler internalNotificationHandler;
	public FragmentHelper mHelper;
	private Handler loadRandomUserPic;
	public Handler callHandler;
	private RRLocationManager locationManager;
	private Location location = null;
	GeoFinder geofinder;String RanpicUrl=null;

	private static ProgressDialog dialog;

	RRServer serverToConnect = null;

	CharSequence[] permanentChannelsSeq = null;
	public static ArrayList<RRChannels> permanentChannels = new ArrayList<RRChannels>();

	ArrayList<String> usernames = new ArrayList<String>();

	private VoiceModePanelFragment mVMPFrag;
	public FriendsDockFragment mFDFrag;
	CallCantrolsFragment mCCFrag;
	public int  Width_metrics;
	// private PeopleFragment mPFrag;
	// private RightSidePanelFragment mRSPFrag;
	// private GroupsFragment mGFrag;
	int Height_metrics;

	public void cleanDialogs() {

		if (mProgressDialog != null) {
			mProgressDialog.dismiss();
			mProgressDialog = null;
		}

		if (mCustomDialog != null) {
			mCustomDialog.dismiss();
			mCustomDialog = null;
		}

		if (mRandomCallDialog != null) {
			mRandomCallDialog.dismiss();
			mRandomCallDialog = null;
		}

		mMwCommLogic.unregisterGetRandomUserServiceReceiver();

		setTitle(R.string.app_name);
		String groupString = "";
		if (!Utility.testGroup.equals("")) {
			groupString += " Group:" + Utility.testGroup;
		}
		setTitle("Rallee " + Utility.testServerName + groupString);
		setTitleColor(Color.WHITE);

		// internalNotificationHandler.removeMessages(DISMISS_MESSAGE);
	}

	/**
	 * Handles activity initialization when the Service has connected.
	 * 
	 * Should be called when there is a reason to believe that the connection
	 * might have became valid. The connection MUST be established but other
	 * validity criteria may still be unfilled such as server synchronization
	 * being complete.
	 * 
	 * The method implements the logic required for making sure that the
	 * Connected service is in such a state that it fills all the connection
	 * criteria for HomeActivity.
	 * 
	 * The method also takes care of making sure that its initialization code is
	 * executed only once so calling it several times doesn't cause problems.
	 */

	@Override
	protected void onConnected() {
		Globals.logDebug(this, "HOMEActivity onConnected");
		CONNECTED = 1;
		dismissDialog();

		// VoiceModePanelFragment vmpFrag = (VoiceModePanelFragment)
		// getSupportFragmentManager()
		// .findFragmentByTag(VoiceModePanelFragment.FRAGMENT_TAG);
		if (mVMPFrag != null) {
			mVMPFrag.initControlListeners();
		}

		if (mFDFrag != null) {
			mFDFrag.initControlListeners();
		}

		mService.startAudioThreads();

		RadioRuntService.SWITCH_CHANNEL = 0;
		// //////////////////////////////////////////////////////////////////
		mMwCommLogic.registerGetChannelsServiceReceiver();

		Intent service = new Intent(RalleeApp.getInstance(),
				GetGroupsFromFBService.class);
		RalleeApp.getInstance().startService(service);

		mMwCommLogic.registerGetRandomServiceReceiver();

		chList = mService.getChannelList();
		countUsersOnCurrentChannel();

		VoiceModePanelFragment frag = (VoiceModePanelFragment) getSupportFragmentManager()
				.findFragmentByTag(VoiceModePanelFragment.FRAGMENT_TAG);
		if (frag != null) {
			frag.serviceConnected();
		}

		// We are now connected!
		if (mProgressDialog != null) {
			mProgressDialog.dismiss();
			mProgressDialog = null;
		}

		manualRecord = mService.isRecording();

		// If we don't have visible channel selected, default to the current
		// channel.
		// Setting channel also synchronizes the UI so we don't need to do it
		// manually.
		//
		// TODO: Resync channel if current channel was visible on pause.
		// Currently if the user is moved to another channel while this activity
		// is paused the channel isn't updated when the activity resumes.
		if (visibleChannel == null) {
			setChannel(mService.getCurrentChannel());
		}
		if (firstEnter) {
			TtsProvider.speak(
					"connected to Rallee"/* + visibleChannel.name */, false);
			firstEnter = false;
		}
		C2DMessaging.register(this /* the application context */,
				DeviceRegistrar.SENDER_ID);

		// /////////////////////////////// Connect to Intent
		// /////////////////////////////// Channel
		// ////////////////////////////////////
		// /////////////////////////////////////////////////////////////////////////////////////////////
		if (Utility.invocedChannelName != null) {
			RadioRuntService.HOME_ACTIVITY_PAUSED = 1;
			RadioRuntService.SWITCH_CHANNEL = 1;
			mService.joinChannel(Utility.switchChannel.id);
			Utility.invocedChannelName = null;
			switchCallState(CallState.INCALL_NEARBY);
		}

		// ///////////////////////////////////////////////////////////////////////////////////////////////

		try {
			final List<RRMessages> messages = mService.getMessageList();
			int mSize = messages.size();
			for (RRMessages m : messages) {
				if (mSize > 0) {
					if (messages.get(0).timestamp != msgsTimestamp) {
						Globals.logDebug(this, "onConnencted msg process: "
								+ messages.get(0).message);
						processMessage(messages.get(0).message);
						return; //
					}
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (WAS_ON_CHANNEL_LIST == 1 && ENTER_CHANNEL_ON_CHANNEL_LIST == 1) {
			ENTER_CHANNEL_ON_CHANNEL_LIST = 0;
			switchCallState(CallState.INCALL_NEARBY);
		} else if (RadioRuntService.SWITCH_CHANNEL == 1
				|| (Utility.switchChannel != null && Utility.switchChannel.id != 0)) {
			mService.joinChannel(Utility.switchChannel.id);
			switchCallState(CallState.INCALL_NEARBY);
		}

		// if (mService != null && mService.getCurrentChannel() != null
		// && mService.getCurrentChannel().id == 0) {
		// switchCallState(CallState.NORMAL);
		// }
		WAS_ON_CHANNEL_LIST = 0;

		Globals.logDebug(this, "HOMEActivity onConnected ended");
		// if(mCCFrag != null){
		// mCCFrag.restoreToNormalCallStateIfNeeded();
		// }
	}

	/**
	 * Handles activity initialization when the Service is connecting.
	 */

	@Override
	protected void onConnecting() {
		Globals.logDebug(this, "HomeActivity onConnecting");
		// showProgressDialog(R.string.connectionProgressTitleEmptyMsg);
	}

	@Override
	protected void onReconnecting() {
		Globals.logDebug(this, "Home RECONNECTING");
		if (mService != null) {
			if (RadioRuntService.tempNetState) {
				dismissDialog();
				cleanDialogs();
				showDialog(getString(R.string.waiting_for_internet), true);
			} else {
				dismissDialog();
				cleanDialogs();
			}
			Globals.logInfo(this, "RadioRuntService.HOME_ACTIVITY_PAUSED "
					+ String.valueOf(RadioRuntService.HOME_ACTIVITY_PAUSED));
		} else {
			showDialog(getString(R.string.waiting_for_internet), true);
		}
	}

	@Override
	protected void onDisconnected() {

		if (!RadioRuntService.DISCONNECT_FROM_RALLEE) {
			final Intent i = new Intent(getApplicationContext(),
					LogInActivity.class);
			i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(i);
		}

		super.onDisconnected();
		Globals.logDebug(this, "HOMEActivity onDisconnected");
	}

	private void setChannel(final RRChannels channel) {
		visibleChannel = channel;
	}

	/**
	 * Dims the screen to set/normal brightness
	 * 
	 * @param dim
	 *            <code>true</code> to dim the screen or <code>false</code> to
	 *            return to normal brightness
	 */
	private void dimScreen(boolean dim) {
		final LayoutParams lp = getWindow().getAttributes();
		if (dim) {
			lp.screenBrightness = dimmedScreenBrightness;
			screenIsDimmed = true;
		} else {
			lp.screenBrightness = -1;
			screenIsDimmed = false;
		}
		getWindow().setAttributes(lp);
	}

	protected InviteToApplicationDialogFragment inviteFriendsDialogFragment = null;
	private LinearLayout mainLayout;
	private ToggleButton tbtnConvMode;
	private Integer randomContinuousNearby = 0;
	protected ArrayList<RandomUser> randomContinuousList;
	private boolean returnToRandomDialog = false;
	private int randomContinuousListIndex = 0;
	//ImageResolution IResolution=null;
	ImageHelper imageHelper=null;
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Globals.logDebug(this, "onCreateStart");

		uiHelper = new UiLifecycleHelper(this, this);
		uiHelper.onCreate(savedInstanceState);

		settings = new Settings(this);
		mHelper = new FragmentHelper(this);
		//IResolution=new ImageResolution(getApplicationContext());
		imageHelper=new ImageHelper(getApplicationContext());
		/*
		 * check is client connected or disconnected to the Rallee network so
		 * the client can start from the LoginActivity or proceed to the
		 * HomeActivity
		 */

		checkIfDisconnectedFromRallee();

		setContentView(R.layout.home);

		// /////////////////////////////////////////////////////////////////////
		// ///////////////////Set Crittercism and GoogleAnalyticsTracker for
		// Logging//////////////////////
		// /////////////////////////////////////////////////////////////////////

		setCrittercismForLogging();

		// create google analytics object

		// mGATracker = GoogleAnalyticsTracker.getInstance();
		// mGATracker.startNewSession("UA-35433409-1", this);
		// mGATracker.setProductVersion("Andorid", RalleeApp.getInstance()
		// .getRalleeRelease());
		// mGATracker.trackPageView("/HomeActivity");

		// /////////////////////////////////////////////////////////////////////
		// /////////////////////////////////////////////////////////////////////

		Intent service = new Intent(this, RequestFBData.class);
		startService(service);

		// /////////////////////////////////////////////////////////////////////

		// initControls();

		registerConnectionReceiver();
		mMwCommLogic = new MwCommunicationLogic(this);
		mMwCommLogic.registerReportUserReciver();

		String jsonString = "";
		try {
			JSONObject json = new JSONObject();
			json.put("user_id", RalleeApp.getInstance().getRalleeUID());
			jsonString = json.toString();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		RRServerProxyHelper.startCheckForNewReleaseService(this, jsonString);

		settings = new Settings(this);
		isFirstStart = settings.getIsFirstStart();

		mHandler = new Handler();

		if (RadioRuntService.tempNetState) {
			showDialog(getString(R.string.waiting_for_internet), true);
		} else {
			showDialog(getString(R.string.connecting_to_server), true);
		}

		Globals.logDebug(this, "onCreateLoadStart");

		locationHandler = new LocationHandler();
		// TODO Izmeniti uid.
		tmpChannel = new RRChannels();
		msgsTimestamp = 99999;

		settings.removeNotificationCount(this);

		if (savedInstanceState != null) {
			final RRChannels channel = (RRChannels) savedInstanceState
					.getParcelable(SAVED_STATE_VISIBLE_CHANNEL);

			// Channel might be null if we for example caused screen rotation
			// while still connecting.
			if (channel != null) {
				setChannel(channel);
			}
		}

		if (getIntent().getBooleanExtra(PUSHED_CHANNEL, false)) {
			mService.joinChannel(getIntent().getIntExtra(PUSHED_CHANNEL_ID, 0));
			// Toast.makeText(
			// this,
			// getString(R.string.label_joining)
			// + String.valueOf(getIntent().getIntExtra(
			// PUSHED_CHANNEL_ID, 0)), 1000).show();
		}

		// if(getIntent().getIntExtra(CHANNEL_TYPE, 0) == 1){
		// imgLogo.setVisibility(View.INVISIBLE);
		//

		// SharedPreferences checkFirstState = getSharedPreferences("check",
		// MODE_PRIVATE);
		// boolean stanje = false;
		// boolean check_state = checkFirstState.getBoolean("check", stanje);
		// if(check_state == false)
		// {
		// Editor mEditor = checkFirstState.edit();
		// mEditor.putBoolean("check", true);
		// mEditor.commit();
		// loadFriends();
		// InviteToApplicationDialog inviteDialog = new
		// InviteToApplicationDialog(HomeActivity.this);
		// inviteDialog.show();
		//
		// }

		// loadFriends();

		callHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				callHandler.removeMessages(CALLER_UNANSWERED);
				if (msg.what == CALLER_UNANSWERED) {
					Globals.logDebug(this,
							"toCALL_STATE_NORMAL CALLER_UNANSWERED");
					showRandomDialogIfNeeded();
					switchCallState(CallState.NORMAL);

				}
			}
		};

		returnMissedCallHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				String usnm = msg.getData()
						.getString("FRIEND_TO_CALL_USERNAME");
				boolean isCalled = false;
				// checkMissedCalls();

				Cursor c = getApplicationContext().getContentResolver().query(
						Uri.parse(DbContentProvider.CONTENT_URI_USER_TABLE
								+ "/" + usnm), null, null, null, null);
				if (c != null) {
					String username = c.getString(c
							.getColumnIndex(DbContentProvider.USER_COL_ID));
					String firstname = c.getString(c
							.getColumnIndex(DbContentProvider.USER_COL_NAME));
					if (username != null && firstname != null
							&& !username.equals("") && !firstname.equals("")) {
						isCalled = true;
						callFriend(username, firstname);
					}
				}
				if (!isCalled) {
					// for (RRUser u : myOfflineUsers) {
					// if (u.userName.equals(usnm)) {
					// isCalled = true;
					// callOfflineFriend(u);
					// break;
					// }
					// }
				}
			}
		};
		loadRandomUserPic = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);

				if (mRandomCallDialog == null || !mRandomCallDialog.isShowing()) {
					return;
				}
				String randomUserRalleeId=null;
				randomUserRalleeId = msg.getData().getString("id");
				
				TextView tvName = (TextView) mRandomCallDialog
						.findViewById(R.id.dialog_random_call_name);
				if (tvName == null) {
					return;
				}
				Object obj = tvName.getTag();
				if (obj == null) {
					return;
				}
				String randomUserRalleeIdShowing=null;
				randomUserRalleeIdShowing= (String) obj;
				if (randomUserRalleeIdShowing != null
						&& randomUserRalleeIdShowing.equals(randomUserRalleeId)) {
					String randomUserPicUrl=null;
					
					randomUserPicUrl = msg.getData().getString("pic");

					ImageView userPic = (ImageView) mRandomCallDialog
							.findViewById(R.id.dialog_random_call_user_pic);
					if (randomUserPicUrl != null
							&& !randomUserPicUrl.equals("")) {
						userPic.setImageBitmap(ImageHelper
								.getRoundedCornerBitmap(Utility.getBitmap(
										randomUserPicUrl,
										getApplicationContext()), 5));
						// userPic.setVisibility(View.VISIBLE);
						// ((ImageView) mCustomDialog
						// .findViewById(R.id.dialog_random_user_pic_effect))
						// .setVisibility(View.VISIBLE);
					}
				}
			}
		};

		// internalNotificationHandler = new Handler() {
		// @Override
		// public void handleMessage(Message msg) {
		// if (msg.what == SET_MESSAGE) {
		// setTitle(msg.getData().getString("message"));
		// setTitleColor(Color.RED);
		// } else if (msg.what == DISMISS_MESSAGE) {
		// setTitle(R.string.app_name);
		// String groupString = "";
		// if (!Utility.testGroup.equals("")) {
		// groupString += " Group:" + Utility.testGroup;
		// }
		// setTitle("Rallee " + Utility.testServerName + groupString);
		// setTitleColor(Color.WHITE);
		// } else if (msg.what == UPDATE_MESSAGE) {
		// checkMissedCalls();
		// }
		// }
		// };

		startClickUser();

		tbtnConvMode = (ToggleButton) findViewById(R.id.tbtnConvMode);
		tbtnConvMode.setChecked(true); // CONVERSATION_MODE_VOICE
		tbtnConvMode.setEnabled(false);
		tbtnConvMode.setVisibility(View.GONE);
		tbtnConvMode.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				if (isChecked == CONVERSATION_MODE_TEXT) {
					convMode = CONVERSATION_MODE_TEXT;
					// Toast.makeText(getActivity(),
					// "Text mode - Voice fragment",
					// 500).show();
					FragmentTransaction ft = getSupportFragmentManager()
							.beginTransaction();
					ft.replace(R.id.topPanelSlot, new TextModePanelFragment(),
							"textModePanel");
					ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
					// ft.commit();
					ft.commitAllowingStateLoss();
				} else {
					convMode = CONVERSATION_MODE_VOICE;

					FragmentTransaction ft = getSupportFragmentManager()
							.beginTransaction();

					ft.replace(R.id.topPanelSlot, new VoiceModePanelFragment(),
							VoiceModePanelFragment.FRAGMENT_TAG);
					ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
					// ft.commit();
					ft.commitAllowingStateLoss();
				}
			}
		});

		if (mVMPFrag == null) {
			Bundle vmb = new Bundle();
			vmb.putBoolean(VoiceModePanelFragment.START_WITHOUT_LISTENERS, true);
			startFragment(VoiceModePanelFragment.FRAGMENT_TAG,
					savedInstanceState, vmb);
		}

		if (CallState.sCurrent == CallState.NORMAL) {
			Bundle fdb = new Bundle();
			fdb.putBoolean(FriendsDockFragment.START_WITHOUT_LISTENERS, true);
			startFragment(FriendsDockFragment.FRAGMENT_TAG, savedInstanceState,
					fdb);
		} else {
			startFragment(CallCantrolsFragment.FRAGMENT_TAG,
					savedInstanceState, null);
		}
		EasyTracker.getTracker().setStartSession(true);
		Globals.logDebug(this, "HOMEActivity onCreate end");
	}

	public void callFriend(final String calledUserUsername,
			final String calledUserFirstName) {
		callFriend(calledUserUsername, calledUserFirstName, false, null);
		googleTracker("call", "private", "start", 0);
	}

	protected void showRandomUser(final RandomUser newRandomUser) {
		if (randomContinuousList == null) {
			randomContinuousList = new ArrayList<RandomUser>();
		}
		returnToRandomDialog = false;
		randomContinuousList.add(newRandomUser);
		randomContinuousListIndex = randomContinuousList.size() - 1;
		callFriend(newRandomUser.user_id, newRandomUser.user_name, true,
				newRandomUser);
	}

	protected void showRandomUser() {
		if (randomContinuousList == null || randomContinuousList.isEmpty()) {
			return;
		}
		returnToRandomDialog = false;
		RandomUser mRandomUser = randomContinuousList
				.get(randomContinuousListIndex);
		callFriend(mRandomUser.user_id, mRandomUser.user_name, true,
				mRandomUser);
	}

	private void showRandomDialogIfNeeded() {
		//mCCFrag.Invisible();
		if (CallState.sCallType == CallState.CALL_RANDOM
				&& returnToRandomDialog) {

			goRandom(-1);
		}
	}

	protected void callFriend(final String calledUserUsername,
			final String calledUserFirstName, final boolean isRandom,
			final RandomUser randomUser) {

		Utility.calledUserFirstName = calledUserFirstName;// u.FirstName;

		OnClickListener onCallConfirm = new OnClickListener() {

			@Override
			public void onClick(View v) {
				Fragment ofFrag = getSupportFragmentManager()
						.findFragmentByTag(OnlineFriendsFragment.FRAGMENT_TAG);
				if (ofFrag != null) {
					getSupportFragmentManager().popBackStack();
				}
				Fragment pFrag = getSupportFragmentManager().findFragmentByTag(
						PeopleFragment.FRAGMENT_TAG);
				if (pFrag != null) {
					// getSupportFragmentManager().popBackStack();
					mHelper.goToMainPanel();
				}

				Fragment rsFrag = getSupportFragmentManager()
						.findFragmentByTag(RightSidePanelFragment.FRAGMENT_TAG);
				if (rsFrag != null) {
					// getSupportFragmentManager().popBackStack();
					mHelper.goToMainPanel();
				}

				Fragment pgdf = getSupportFragmentManager().findFragmentByTag(
						PrivateGroupDetailsFragment.FRAGMENT_TAG);
				if (pgdf != null) {
					getSupportFragmentManager().popBackStackImmediate();
				}

				GroupsFragment gf = (GroupsFragment) getSupportFragmentManager()
						.findFragmentByTag(GroupsFragment.FRAGMENT_TAG);
				if (gf != null) {
					getSupportFragmentManager().popBackStackImmediate();
				}

				if (CallState.sCurrent != CallState.NORMAL) {
					Globals.logDebug(this,
							"toCALL_STATE_NORMAL callFriend, currentCallState != CALL_STATE_NORMAL");
					switchCallState(CallState.NORMAL);
				}
				Utility.calledUserUsername = calledUserUsername;// u.userName;
				Utility.calledUserFirstName = calledUserFirstName;// u.FirstName;

				tmpChannel.name = RalleeApp.getInstance().getRalleeUID()
						+ "_talkingto_" + calledUserUsername;// u.userName;
				tmpChannel.parent = 0;
				tmpChannel.temporary = true;
				tmpChannel.description = "Private Channel";

				isChann = false;
				for (int i = 0; i < chList.size(); i++) {
					if (chList.get(i).name.equals(tmpChannel.name)) {
						tmpChannel.id = chList.get(i).id;
						mService.joinChannel(tmpChannel.id);
						isChann = true;
					}
				}
				if (!isChann) {
					mService.createChannel(tmpChannel.name, true,
							tmpChannel.description);

				}

				CallState.sCallType = CallState.CALL_PRIVATE;
				switchCallState(CallState.OUTGOING);

				String[] recs = { calledUserUsername };// u.userName };
				final RRPushMessage pushMsg = new RRPushMessage(recs);
				final RRPushMessagePayload payload = new RRPushMessagePayload();
				payload.sender = RalleeApp.getInstance().getRalleeUID();
				if (isRandom) {
					payload.payloadType = "randomCall";
					CallState.sCallType = CallState.CALL_RANDOM;
					returnToRandomDialog = true;
				} else {
					payload.payloadType = "channelId";
				}
				payload.channelName = String.valueOf(tmpChannel.name);
				Utility.channelName = payload.channelName;
				long date = Calendar.getInstance(TimeZone.getTimeZone("GMT"))
						.getTimeInMillis();
				payload.timestamp = date;
				pushMsg.requestCode = RRPushMessage.INVITE_TO_PRIVATE_CHAT;

				ObjectMapper mapper = new ObjectMapper();
				try {
					int session = 0;
					List<RRUser> mUList = mService.getUserList();
					for (int i = 0; i < mUList.size(); i++) {
						if (mUList.get(i).userName.equals(calledUserUsername)) {// u.userName))
							// {
							session = mUList.get(i).session;
							break;
						}
					}
					if (session == 0) {

						Globals.logDebug(this, "random call session NOT found");
						mService.joinChannel(0);
						pushMsg.data = mapper.writeValueAsString(payload);
						String fullPushMessage = mapper
								.writeValueAsString(pushMsg);
						RRServerProxyHelper.startSendPushMessageService(
								getApplicationContext(), fullPushMessage);
					} else {
						Globals.logDebug(this, "random call session found "
								+ session);
						mService.sendUserTextMessage(
								mapper.writeValueAsString(payload), session);

						googleTracker("random", "Random call", "call", 0);
						googleTracker("call", "random", "start", 0);
					}
				} catch (JsonGenerationException e) {
					e.printStackTrace();
				} catch (JsonMappingException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				callHandler.sendEmptyMessageDelayed(CALLER_UNANSWERED,
						CALL_TIMEOUT);
			}
		};

		if (!isRandom) {
			// DialogInterface.OnClickListener onCallConfirm = new
			// DialogInterface.OnClickListener() {
			//
			// @Override
			// public void onClick(final DialogInterface dialog, final int
			// which) {
			Fragment ofFrag = getSupportFragmentManager().findFragmentByTag(
					OnlineFriendsFragment.FRAGMENT_TAG);
			if (ofFrag != null) {
				getSupportFragmentManager().popBackStack();
			}
			Fragment pFrag = getSupportFragmentManager().findFragmentByTag(
					PeopleFragment.FRAGMENT_TAG);
			if (pFrag != null) {
				// getSupportFragmentManager().popBackStack();
				mHelper.goToMainPanel();
			}
			Fragment rsFrag = getSupportFragmentManager().findFragmentByTag(
					RightSidePanelFragment.FRAGMENT_TAG);
			if (rsFrag != null) {
				// getSupportFragmentManager().popBackStack();
				mHelper.goToMainPanel();
			}

			if (CallState.sCurrent != CallState.NORMAL) {
				Globals.logDebug(this,
						"toCALL_STATE_NORMAL callFriend, currentCallState != CALL_STATE_NORMAL");
				switchCallState(CallState.NORMAL);
			}
			Utility.calledUserUsername = calledUserUsername;// u.userName;
			Utility.calledUserFirstName = calledUserFirstName;// u.FirstName;

			tmpChannel.name = RalleeApp.getInstance().getRalleeUID()
					+ "_talkingto_" + calledUserUsername;// u.userName;
			tmpChannel.parent = 0;
			tmpChannel.temporary = true;
			tmpChannel.description = "Private Channel";

			isChann = false;
			for (int i = 0; i < chList.size(); i++) {
				if (chList.get(i).name.equals(tmpChannel.name)) {
					tmpChannel.id = chList.get(i).id;
					mService.joinChannel(tmpChannel.id);
					isChann = true;
				}
			}
			if (!isChann) {
				mService.createChannel(tmpChannel.name, true,
						tmpChannel.description);

			}

			CallState.sCallType = CallState.CALL_PRIVATE;
			switchCallState(CallState.OUTGOING);

			String[] recs = { calledUserUsername };// u.userName };
			final RRPushMessage pushMsg = new RRPushMessage(recs);
			final RRPushMessagePayload payload = new RRPushMessagePayload();
			payload.sender = RalleeApp.getInstance().getRalleeUID();
			if (isRandom) {
				payload.payloadType = "randomCall";
			} else {
				payload.payloadType = "channelId";
			}
			payload.channelName = String.valueOf(tmpChannel.name);
			Utility.channelName = payload.channelName;
			long date = Calendar.getInstance(TimeZone.getTimeZone("GMT"))
					.getTimeInMillis();
			payload.timestamp = date;
			pushMsg.requestCode = RRPushMessage.INVITE_TO_PRIVATE_CHAT;

			ObjectMapper mapper = new ObjectMapper();
			try {
				int session = 0;
				for (int i = 0; i < users.size(); i++) {
					if (users.get(i).userName.equals(calledUserUsername)) {// u.userName))
						// {
						session = users.get(i).session;
						break;
					}
				}
				if (session == 0) {
					mService.joinChannel(0);
					pushMsg.data = mapper.writeValueAsString(payload);
					String fullPushMessage = mapper.writeValueAsString(pushMsg);
					RRServerProxyHelper.startSendPushMessageService(
							getApplicationContext(), fullPushMessage);
				} else {
					mService.sendUserTextMessage(
							mapper.writeValueAsString(payload), session);
				}
			} catch (JsonGenerationException e) {
				e.printStackTrace();
			} catch (JsonMappingException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			callHandler
					.sendEmptyMessageDelayed(CALLER_UNANSWERED, CALL_TIMEOUT);
		} else {

			showCustomDialogRandomContinuous(true, onCallConfirm, randomUser);
		}
	}

	public void callOfflineFriend(final String calledUserUsername,
			final String calledUserFirstName) {// final RRUser u) {
		Utility.calledUserFirstName = calledUserFirstName;// u.FirstName;
		OnClickListener onCallConfirm = new OnClickListener() {

			@Override
			public void onClick(View v) {
				googleTracker("call", "offline", "start", 0);
				Utility.calledUserUsername = calledUserUsername;// u.userName;
				Utility.calledUserFirstName = calledUserFirstName;// u.FirstName;

				String[] recs = { calledUserUsername };// u.userName };
				final RRPushMessage pushMsg = new RRPushMessage(recs);
				final RRPushMessagePayload payload = new RRPushMessagePayload();
				payload.sender = RalleeApp.getInstance().getRalleeUID();
				payload.payloadType = "channelId";
				payload.channelName = String.valueOf(tmpChannel.name);
				long date = Calendar.getInstance(TimeZone.getTimeZone("GMT"))
						.getTimeInMillis();
				payload.timestamp = date;
				pushMsg.requestCode = RRPushMessage.INVITE_TO_PRIVATE_CHAT;

				ObjectMapper mapper = new ObjectMapper();
				try {
					pushMsg.body = RalleeApp.getInstance().getFullName()
							+ " calling";
					pushMsg.data = mapper.writeValueAsString(payload);
					String fullPushMessage = mapper.writeValueAsString(pushMsg);
					RRServerProxyHelper.startSendPushMessageService(
							getApplicationContext(), fullPushMessage);
				} catch (JsonGenerationException e) {
					e.printStackTrace();
				} catch (JsonMappingException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		};

		showCustomDialog(2, R.string.label_call,
				getString(R.string.send_offline_message_part1)
						+ calledUserFirstName
						+ getString(R.string.send_offline_message_part2),
				android.R.string.yes, onCallConfirm, android.R.string.no, null);

		// showDialog(getString(R.string.label_call),
		// getString(R.string.send_offline_message_part1)
		// + calledUserFirstName
		// + getString(R.string.send_offline_message_part2),
		// android.R.drawable.ic_dialog_alert, null, android.R.string.yes,
		// onCallConfirm, null, android.R.string.no, null, null);
	}

	public void addFriendToConversation(String calledUserUsername,
			final String calledUserFirstName) {
		addFriendToConversation(calledUserUsername, calledUserFirstName, true);

	}

	public void addFriendToConversation(final String calledUserUsername,
			final String calledUserFirstName, final boolean needConfirmation) {// final
		// RRUser
		// u) {

		OnClickListener onCallConfirm = new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (needConfirmation) {
					// Toast.makeText(
					// HomeActivity.this,
					// getString(R.string.label_calling)
					// + calledUserFirstName + "...", 1000).show();
				}

				RRChannels groupChannel = new RRChannels();
				groupChannel.name = Utility.channelName;// u.userName;
				groupChannel.parent = 0;
				groupChannel.temporary = true;
				groupChannel.description = "Private Channel";

				String[] recs = { calledUserUsername };// u.userName };
				final RRPushMessage pushMsg = new RRPushMessage(recs);
				final RRPushMessagePayload payload = new RRPushMessagePayload();
				payload.sender = RalleeApp.getInstance().getRalleeUID();
				payload.payloadType = "groupConversationCall";
				payload.channelName = String.valueOf(groupChannel.name);
				Utility.channelName = payload.channelName;
				long date = Calendar.getInstance(TimeZone.getTimeZone("GMT"))
						.getTimeInMillis();
				payload.timestamp = date;
				pushMsg.requestCode = RRPushMessage.INVITE_TO_PRIVATE_CHAT;

				ObjectMapper mapper = new ObjectMapper();
				try {
					int session = 0;
					for (int i = 0; i < users.size(); i++) {
						if (users.get(i).userName.equals(calledUserUsername)) {// u.userName))
																				// {
							session = users.get(i).session;
							break;
						}
					}
					if (session == 0) {
						Globals.logDebug(this,
								"addFriendToConversation session of the user is 0");
						// mService.joinChannel(0);
						pushMsg.data = mapper.writeValueAsString(payload);
						String fullPushMessage = mapper
								.writeValueAsString(pushMsg);
						RRServerProxyHelper.startSendPushMessageService(
								getApplicationContext(), fullPushMessage);
					} else {
						Globals.logDebug(this,
								"addFriendToConversation session of the user is "
										+ session);
						mService.sendUserTextMessage(
								mapper.writeValueAsString(payload), session);

						if (!CallState.calledFriends
								.contains(calledUserUsername)) {
							CallState.calledFriends.add(calledUserUsername);
						}

						AddUsersToConversationFragment autcFrag = (AddUsersToConversationFragment) getSupportFragmentManager()
								.findFragmentByTag(
										AddUsersToConversationFragment.FRAGMENT_TAG);
						if (autcFrag != null) {
							autcFrag.addCalledFriendsIfNotDuplicate(calledUserUsername);
						}
					}
				} catch (JsonGenerationException e) {
					e.printStackTrace();
				} catch (JsonMappingException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}

			}
		};

		if (needConfirmation) {
			showCustomDialog(
					2,
					R.string.label_call,
					getString(R.string.add_user_to_conversation_part1)
							+ calledUserFirstName
							+ getString(R.string.add_user_to_conversation_part2),
					android.R.string.yes, onCallConfirm, android.R.string.no,
					null);

			// showDialog(
			// getString(R.string.label_call),
			// getString(R.string.add_user_to_conversation_part1)
			// + calledUserFirstName
			// + getString(R.string.add_user_to_conversation_part2),
			// 0, null, android.R.string.yes, onCallConfirm, null,
			// android.R.string.no, null, null);
		} else {
			onCallConfirm.onClick(null);
		}
	}

	/**
	 * @param nearby
	 *            0 - classic random; >0 - mile radius to search; -1 - random
	 *            next; -2 - random previous ; -3 - random continue, list
	 *            deleted
	 * 
	 */
	public void goRandom(Integer nearby) {// final RRUser u) {
		// showDialog(getString(R.string.label_random_call),
		// getString(R.string.searching_for_user), 0, null, 0, null, null,
		// 0, null, null);

		if (randomContinuousList == null) {
			randomContinuousList = new ArrayList<RandomUser>();
			randomContinuousListIndex = -1;
		}

		boolean startService = true;

		if (nearby != -1 && nearby != -2) {
			if (nearby >= 0) {
				randomContinuousNearby = nearby;
			}
			randomContinuousList.clear();
			randomContinuousListIndex = -1;
			if (nearby != -3) {
				googleTracker("random", "Random start", "start", 0);
			}
		} else {
			startService = false;
			if (nearby == -1) {
				if (randomContinuousList.size() - 1 <= randomContinuousListIndex) {
					randomContinuousListIndex = randomContinuousList.size() - 1;
					startService = true;
					googleTracker("random", "Random next", "next", 0);
				} else {
					randomContinuousListIndex++;
				}
			} else if (nearby == -2) {
				randomContinuousListIndex--;
				if (randomContinuousListIndex < 0) {
					randomContinuousListIndex = 0;
				}
			}
		}

		if (startService) {
			showCustomDialogRandomContinuous(false, null, null);

			mMwCommLogic.registerGetRandomUserServiceReceiver();

			if (RalleeApp.getInstance().getRalleeUID().equals("ralleeUID")) {
				return;
			}
			ArrayList<String> listFromRandomList = new ArrayList<String>();
			for (int i = 0; i < randomContinuousList.size(); i++) {
				listFromRandomList.add(randomContinuousList.get(i).user_id);
			}

			String jsonString = null;
			try {
				JSONObject json = new JSONObject();
				json.put("user_id", RalleeApp.getInstance().getRalleeUID());
				json.put("nearby", randomContinuousNearby);

				JSONArray excludeUserList = new JSONArray(listFromRandomList);
				json.put("excludeUserList", excludeUserList);

				jsonString = json.toString();
			} catch (JSONException e) {
				e.printStackTrace();
			}
			Globals.logDebug(this, "Json string sent to GetRandomUserService: "
					+ jsonString);
			RRServerProxyHelper.startGetRandomUserService(mMwCommLogic.ctx,
					jsonString);
		} else {
			showRandomUser();
		}
	}

	public void enterFBGroupChannel(String fbGroupId) {
		googleTracker("groups", "fbGroup", "startCall", 0);
		googleTracker("call", "fbGroup", "startCall", 0);
		FacebookGroupDetailsFragment fgd = (FacebookGroupDetailsFragment) getSupportFragmentManager()
				.findFragmentByTag(FacebookGroupDetailsFragment.FRAGMENT_TAG);
		if (fgd != null) {
			getSupportFragmentManager().popBackStackImmediate();
		}
		GroupsFragment gf = (GroupsFragment) getSupportFragmentManager()
				.findFragmentByTag(GroupsFragment.FRAGMENT_TAG);
		if (gf != null) {
			getSupportFragmentManager().popBackStackImmediate();
		}
		Fragment rsFrag = getSupportFragmentManager().findFragmentByTag(
				RightSidePanelFragment.FRAGMENT_TAG);
		if (rsFrag != null) {
			mHelper.goToMainPanel();
		}

		if (CallState.sCurrent != CallState.NORMAL) {
			Globals.logDebug(this,
					"toCALL_STATE_NORMAL callFriend, currentCallState != CALL_STATE_NORMAL");
			switchCallState(CallState.NORMAL);
		}

		tmpChannel.name = "_fbgroup_" + fbGroupId;

		tmpChannel.parent = 0;
		tmpChannel.temporary = true;
		tmpChannel.description = "FB Group Channel";

		isChann = false;
		for (int i = 0; i < chList.size(); i++) {
			if (chList.get(i).name.equals(tmpChannel.name)) {
				tmpChannel.id = chList.get(i).id;
				mService.joinChannel(tmpChannel.id);
				isChann = true;
			}
		}
		if (!isChann) {
			mService.createChannel(tmpChannel.name, true,
					tmpChannel.description);

		}
		Intent intent = new Intent();
		intent.setAction(CallHistoryReceiver.ACTION_ADD_CALL);
		intent.putExtra(CallHistoryReceiver.EXTRA_TYPE,
				DbContentProvider.CALL_HISTORY_TYPE_FB_GROUP_CALL);
		intent.putExtra(CallHistoryReceiver.EXTRA_ID, fbGroupId);
		sendBroadcast(intent);

		switchCallState(CallState.INCALL_NEARBY);

		Utility.channelName = String.valueOf(tmpChannel.name);
	}

	public void enterPrivateGroupChannel(String groupId, String channelName) {
		List<RRUser> usersToCall = new ArrayList<RRUser>();
		Cursor c = null;
		try {
			c = getContentResolver()
					.query(DbContentProvider.CONTENT_URI_GROUP_MEMBERS_TABLE,
							new String[] { DbContentProvider.GROUP_MEMBERS_COL_USER_ID },
							DbContentProvider.GROUP_MEMBERS_COL_GROUP_ID
									+ " ='" + groupId + "'", null, null);
			if (c != null && c.moveToFirst()) {
				List<RRUser> onlineUserList = getRRService().getUserList();
				Globals.logDebug(this,
						"CallPrivateGroup riends in group: " + c.getCount());
				if (onlineUserList != null) {
					do {
						String userId = c
								.getString(c
										.getColumnIndex(DbContentProvider.GROUP_MEMBERS_COL_USER_ID));
						if (userId != null) {
							for (RRUser member : onlineUserList) {
								if (member != null && member.userName != null
										&& member.userName.equals(userId)) {
									usersToCall.add(member);
									break;
								}
							}
						}
					} while (c.moveToNext());
				}
			}
		} finally {
			if (c != null && !c.isClosed()) {
				c.close();
			}
		}

		if (usersToCall.isEmpty()) {
			Toast.makeText(this, R.string.no_users_on_group, Toast.LENGTH_SHORT)
					.show();
		} else {

			googleTracker("groups", "privateGroup", "startCall", 0);
			googleTracker("call", "privateGroup", "startCall", 0);

			Fragment ofFrag = getSupportFragmentManager().findFragmentByTag(
					OnlineFriendsFragment.FRAGMENT_TAG);
			if (ofFrag != null) {
				getSupportFragmentManager().popBackStack();
			}

			Fragment pgdf = getSupportFragmentManager().findFragmentByTag(
					PrivateGroupDetailsFragment.FRAGMENT_TAG);
			if (pgdf != null) {
				getSupportFragmentManager().popBackStackImmediate();
			}

			GroupsFragment gf = (GroupsFragment) getSupportFragmentManager()
					.findFragmentByTag(GroupsFragment.FRAGMENT_TAG);
			if (gf != null) {
				getSupportFragmentManager().popBackStackImmediate();
			}

			Fragment rsFrag = getSupportFragmentManager().findFragmentByTag(
					RightSidePanelFragment.FRAGMENT_TAG);
			if (rsFrag != null) {
				mHelper.goToMainPanel();
			}

			if (CallState.sCurrent != CallState.NORMAL) {
				Globals.logDebug(
						this,
						"toCALL_STATE_NORMAL enterPrivateGroupChannel, currentCallState != CALL_STATE_NORMAL");
				switchCallState(CallState.NORMAL);
			}

			tmpChannel.name = RalleeApp.getInstance().getRalleeUID()
					+ "_privategroup_" + groupId;

			tmpChannel.parent = 0;
			tmpChannel.temporary = true;
			tmpChannel.description = "Private Group Channel";

			isChann = false;
			for (int i = 0; i < chList.size(); i++) {
				if (chList.get(i).name.equals(tmpChannel.name)) {
					tmpChannel.id = chList.get(i).id;
					mService.joinChannel(tmpChannel.id);
					isChann = true;
				}
			}
			if (!isChann) {
				mService.createChannel(tmpChannel.name, true,
						tmpChannel.description);
			}

			Utility.calledUserFirstName = channelName;

			Intent intent = new Intent();
			intent.setAction(CallHistoryReceiver.ACTION_ADD_CALL);
			intent.putExtra(CallHistoryReceiver.EXTRA_TYPE,
					DbContentProvider.CALL_HISTORY_TYPE_PRIVATE_GROUP_CALL);
			intent.putExtra(CallHistoryReceiver.EXTRA_ID, groupId);
			sendBroadcast(intent);

			CallState.sCallType = CallState.CALL_PRIVATE_GROUP;
			switchCallState(CallState.OUTGOING);

			Utility.channelName = String.valueOf(tmpChannel.name);

			for (RRUser toCall : usersToCall) {
				addFriendToConversation(toCall.userName, toCall.userName, false);
			}

		}

	}

	public void enterPublicChannel(final String channelName) {
		mHandler.post(new Runnable() {

			@Override
			public void run() {

				FragmentManager fm = getSupportFragmentManager();
				Fragment pgdf = fm
						.findFragmentByTag(PublicGroupDetailsFragment.FRAGMENT_TAG);
				Fragment gf = fm.findFragmentByTag(GroupsFragment.FRAGMENT_TAG);
				if (pgdf != null) {
					fm.popBackStackImmediate();
				}

				if (gf != null) {
					fm.popBackStackImmediate();
				}
				Fragment rsFrag = fm
						.findFragmentByTag(RightSidePanelFragment.FRAGMENT_TAG);
				if (rsFrag != null) {
					mHelper.goToMainPanel();
				}

				if (HomeActivity.permanentChannels != null) {
					for (RRChannels publicChannel : HomeActivity.permanentChannels) {
						if (publicChannel.name.equals(channelName)) {
							if (CallState.sCurrent != CallState.NORMAL) {
								Globals.logDebug(this,
										"toCALL_STATE_NORMAL callFriend, currentCallState != CALL_STATE_NORMAL");
								switchCallState(CallState.NORMAL);
							}

							Utility.channelName = publicChannel.name
									.subSequence(5, publicChannel.name.length())
									.toString();
							mService.joinChannel(publicChannel.id);
							Utility.switchChannel = publicChannel;

							googleTracker("call", "publicGroup",
									Utility.channelName, 0);
							googleTracker("groups", "publicGroup", "startCall",
									0);

							Intent intent = new Intent();
							intent.setAction(CallHistoryReceiver.ACTION_ADD_CALL);
							intent.putExtra(
									CallHistoryReceiver.EXTRA_TYPE,
									DbContentProvider.CALL_HISTORY_TYPE_PUBLIC_GROUP_CALL);
							intent.putExtra(CallHistoryReceiver.EXTRA_ID,
									String.valueOf(publicChannel.name));
							sendBroadcast(intent);

							switchCallState(CallState.INCALL_NEARBY);
						}
					}
				}

			}
		});
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onRestoreInstanceState(savedInstanceState);
		if (savedInstanceState != null) {

		}
	}

	@Override
	protected final void onPause() {
		// mGATracker.dispatch();
		EasyTracker.getInstance().dispatch();

		mMwCommLogic.unregisterGetChannelsServiceReceiver();
		mMwCommLogic.unregisterGetLeastParticipantsServerServiceReceiver();
		// mMwCommLogic.unregisterGetParticipantsServiceReceiver();
		mMwCommLogic.unregisterCheckForNewReleaseReceiver();
		mMwCommLogic.unregisterGetRandomServiceReceiver();
		mMwCommLogic.unregisterGetRandomUserServiceReceiver();
		mMwCommLogic.unregisterReportUserReciver();

		if (userFbReceiver != null) {
			unregisterReceiver(userFbReceiver);
		}
		if (groupsFbReceiver != null) {
			unregisterReceiver(groupsFbReceiver);
		}
		if (mCallHistoryReceiver != null) {
			unregisterReceiver(mCallHistoryReceiver);
		}

		// unregisterReceiver(locationReceiver);
		// mAdapter = null;
		locationManager.removeUpdates();
		Globals.logDebug(this, "HOMEActivity onPause");
		cleanDialogs();
		dismissDialog();
		Globals.logDebug(this, "onPAUSE");
		if (mService != null) {
			Globals.logDebug(this, "onPAUSE0");
			// try {
			// wl.release();
			// } catch (Exception e) {
			// // TODO Auto-generated catch block
			// e.printStackTrace();
			// }
			if (ROOT_ID == 0 && WAS_ON_CHANNEL_LIST == 0
					&& iWasOnFeedbackOrReportActivity == 0) {
				Globals.logDebug(this, "onPAUSE1");
				// mService.setPauseMumProtocolState(true);
				if (RadioRuntService.SWITCH_CHANNEL == 0) {
					mService.stopAudioThreads();
				}
				RadioRuntService.HOME_ACTIVITY_PAUSED = 1;

				if (beepGenerator != null) {
					beepGenerator.stopTone();
					beepGenerator.release();
					beepGenerator = null;
				}
			}
		}

		RadioRuntService.HOME_ACTIVITY_PAUSED = 1;
		// unregisterParticipantServiceObserver();
		super.onPause();
		// System.gc();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	protected void onResume() {
		Globals.logDebug(this, "HOMEActivity onResume start");
		super.onResume();
		checkIfDisconnectedFromRallee();
		Speexrec.open(settings.getAudioQuality());

		dismissDialog();
		cleanDialogs();
		if (mService != null) {
			mService.sendListsForFiltering();
		}
		showDialog(getString(R.string.lobel_loading), true);
		if (isFirstStart) {

			showInviteFriendsDialog();
			isFirstStart = false;
			settings.setIsFirstStart(this, isFirstStart);
		}
		registerBroadcastReceivers();
		locationManager = new RRLocationManager(HomeActivity.this,
				locationHandler);

		// Send that user has started HomeActivity so we can monitor app usage
		// in details
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

			jsonString = json.toString();
		} catch (JSONException e) {
			e.printStackTrace();
		}

		RRServerProxyHelper.startSetInsertUserService(this, jsonString);

		geofinder = new GeoFinder();
		Globals.logDebug(this, "onRESUME");
		if (mService != null) {
			Globals.logDebug(this, "onRESUME0");
			Globals.logDebug(this, "onRESUME1");
			if (PREF_CHANGED_ACTIVITY == 1) {
				PREF_CHANGED_ACTIVITY = 0;
				Globals.logDebug(this, "onRESUME2");
				mService.startAudioThreads();
				RadioRuntService.HOME_ACTIVITY_PAUSED = 1;

				Globals.logDebug(this, "HOME Pref");

				if (RadioRuntService.DISCONNECT_FROM_RALLEE) {
					Globals.logDebug(this, "HOME EXIT");

					// set shared preference and exit code for state of
					// connection
					RadioRuntService.DISCONNECT_FROM_RALLEE = false;
					// ////////////////////////////////////////////////////////////////
					// set shared preference and exit code for state of
					// connection

					settings.setExitCode(false);

					// ////////////////////////////////////////////////////////////////

					if (RadioRuntService.DELETE_ACCOUNT == 1) {
						RadioRuntService.DELETE_ACCOUNT = 0;

						// Create Alert dialog for confirming disconnection from
						// Rallee and deletion of account

						showCustomDialog(2, R.string.delete_account_title,
								R.string.delete_account_text,
								android.R.string.yes,
								onDeleteRalleeAccountConfirm,
								android.R.string.no, null);

					} else {

						// Create Alert dialog for confirming disconnection from
						// Rallee

						showCustomDialog(2, false, true,
								R.string.disconnect_title,
								R.string.disconnect_text, android.R.string.yes,
								onDisconnectConfirm, android.R.string.no, null);

					}

				}
			}
		}
		RadioRuntService.HOME_ACTIVITY_PAUSED = 0;
		iWasOnFeedbackOrReportActivity = 0;

		Globals.logDebug(this, "HOMEActivity onResume end");
		if (CONNECTED == 1) {
			mMwCommLogic.registerGetChannelsServiceReceiver();
		}

		// If user session in failed to keep opened, return to LogInActiviry to
		// connect again.
		Session session = Session.getActiveSession();
		if (session == null
				|| (session != null && (!session.isOpened() || session
						.isClosed()))) {
			// RadioRuntService.DISCONNECT_FROM_RALLEE = true;
			settings.setExitCode(true);
			// //////////////////////////////////////////////////////////////
			checkIfDisconnectedFromRallee();

			// CONNECTED = 0;
			// if (exitAndLogIn) {
			// settings.setExitAndLogInSettings("LogIn");
			// } else {
			// settings.setExitAndLogInSettings("NOT LogIn");
			// }
			//
			// RadioRuntService.SWITCH_CHANNEL = 99;
			// if (mService != null) {
			// mService.messagesClearAll();
			// mService.disconnect();
			// }
			// finish();
		}
	}

	@Override
	protected final void onDestroy() {
		super.onDestroy();
		Crittercism.sendAppLoadData();
		Globals.logDebug(this, "HOMEActivity onDestroy");
		restoreNormalState();
		RadioRuntService.ON_MY_CHANNEL = 0;
		Speex.close();
		Speexrec.close();
		Globals.logDebug(this, "AEC INIT " + Speexrec.getAecStatus());
	}

	@Override
	protected void onSaveInstanceState(final Bundle outState) {

		// Commented because of bug: java.lang.IllegalStateException: Can not
		// perform this action after onSaveInstanceState
		/*
		 * super.onSaveInstanceState(outState); if (mServiceObserver != null) {
		 * outState.putBoolean(STATE_WAIT_CONNECTION, true); } //
		 * CallCantrolsFragment frag = (CallCantrolsFragment) //
		 * getSupportFragmentManager() //
		 * .findFragmentByTag(CallCantrolsFragment.FRAGMENT_TAG); // if (frag !=
		 * null) { // CallCantrolsFragment.sDockMode = frag.getDockMode(); //
		 * outState.putInt("BAR_MODE", CallCantrolsFragment.sDockMode); // }
		 * outState.putParcelable(SAVED_STATE_VISIBLE_CHANNEL, visibleChannel);
		 */

		// Fix of bug: java.lang.IllegalStateException: Can not perform this
		// action after onSaveInstanceState
		// first saving my state, so the bundle wont be empty.
		// http://code.google.com/p/android/issues/detail?id=19917
		// outState.putString("WORKAROUND_FOR_BUG_19917_KEY",
		// "WORKAROUND_FOR_BUG_19917_VALUE");
		Globals.logDebug(this, "OnSaveInstanceState");
		if (mServiceObserver != null) {
			outState.putBoolean(STATE_WAIT_CONNECTION, true);
		}
		outState.putParcelable(SAVED_STATE_VISIBLE_CHANNEL, visibleChannel);
		super.onSaveInstanceState(outState);
	}

	/**
	 * Signals that the service has been bound and is available for use.
	 */
	@Override
	protected final void onServiceBound() {
		if (mServiceObserver != null) {
			if (!checkConnectionState()) {
				mService.registerObserver(mServiceObserver);
			}
		}
	}

	/**
	 * Return to normal brightness if a usertouch occurs and screen was dimmed
	 */
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		dimScreen(false);
		return false;
	}

	// ////////GUI Methods//////////////
	// ////////////////////////////////

	// //////////////////////////////////////////////////////////////////////////////////////////
	// ///////////////////////// Start OnClick Dialog Listeners
	// /////////////////////////////////
	// //////////////////////////////////////////////////////////////////////////////////////////

	public final OnClickListener onDisconnectConfirm = new OnClickListener() {
		@Override
		public void onClick(View v) {

			// set shared preference and disconnect state of connection
			RadioRuntService.DISCONNECT_FROM_RALLEE = true;
			settings.setExitCode(true);
			// //////////////////////////////////////////////////////////////

			CONNECTED = 0;
			if (exitAndLogIn) {
				settings.setExitAndLogInSettings("LogIn");
			} else {
				settings.setExitAndLogInSettings("NOT LogIn");
			}

			RadioRuntService.SWITCH_CHANNEL = 99;
			mService.messagesClearAll();
			mService.disconnect();
			finish();
		}
	};

	public final OnClickListener onDeleteRalleeAccountConfirm = new OnClickListener() {
		@Override
		public void onClick(View v) {

			// set shared preference and exit code for state of connection
			RadioRuntService.DISCONNECT_FROM_RALLEE = true;
			settings.setExitCode(true);
			// //////////////////////////////////////////////////////////////

			CONNECTED = 0;

			if (RalleeApp.getInstance().getRalleeUID().equals("ralleeUID")) {
				return;
			}
			String jsonString = "";
			try {
				JSONObject json = new JSONObject();
				json.put("user_id", RalleeApp.getInstance().getRalleeUID());
				// JSONArray jsonA = new JSONArray();
				// jsonA.put(json);
				jsonString = json.toString();
			} catch (JSONException e) {
				e.printStackTrace();
			}

			RRServerProxyHelper.startDeleteUserService(HomeActivity.this,
					jsonString);

			settings.setIsFirstStartFBpost(true);
			settings.setOver18(Settings.RANDOM_PREF_DEFAULT);
			settings.setGoRandom(Settings.RANDOM_PREF_DEFAULT);
			RadioRuntService.SWITCH_CHANNEL = 99;
			mService.messagesClearAll();
			mService.disconnect();
			finish();
		}
	};

	// public final View.OnClickListener

	// //////////////////////////////////////////////////////////////////////////////////////////
	// ///////////////////////// End OnClick Dialog Listeners
	// ///////////////////////////////////
	// //////////////////////////////////////////////////////////////////////////////////////////

	public void endCallFunction(String channelName) {
		if (channelName == null) {
			channelName = Utility.channelName;
		}
		RRChannels tempCh = new RRChannels();
		if (mService != null) {
			List<RRChannels> chList = mService.getChannelList();
			boolean isThereChann = false;
			Globals.logDebug(this, "ending channel: " + channelName);
			for (int i = 0; i < chList.size(); i++) {
				if (chList.get(i).name.equals(channelName)) {
					isThereChann = true;
					tempCh.id = chList.get(i).id;
					Globals.logDebug(this, "ending channel: " + channelName
							+ " channel found");
					break;
				} else {
					isThereChann = false;
				}
			}
			if (isThereChann) {
				RRPushMessagePayload payload = new RRPushMessagePayload();
				payload.sender = RalleeApp.getInstance().getRalleeUID();
				payload.payloadType = "callEnded";
				payload.channelName = channelName;
				ObjectMapper mapper = new ObjectMapper();
				try {
					mService.sendChannelTextMessage(
							mapper.writeValueAsString(payload), tempCh);
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
			} else {
				Globals.logDebug(this, "ending channel: " + channelName
						+ " channel NOT found");
			}
			showRandomDialogIfNeeded();
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, final KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {

			ParticipantsFragment partFrag = (ParticipantsFragment) getSupportFragmentManager()
					.findFragmentByTag(ParticipantsFragment.FRAGMENT_TAG);
			AddUsersToConversationFragment autcFrag = (AddUsersToConversationFragment) getSupportFragmentManager()
					.findFragmentByTag(
							AddUsersToConversationFragment.FRAGMENT_TAG);
			NotificationFragment tmpNotFrag = (NotificationFragment) getSupportFragmentManager()
					.findFragmentByTag(NotificationFragment.FRAGMENT_TAG);
			if (tmpNotFrag != null) {
				tmpNotFrag.rejectCall();
				return true;
			}

			if (partFrag != null) {
				if (mCCFrag != null) {
					mCCFrag.hideParticipants();
				} else {
					getSupportFragmentManager().popBackStack();
				}
				return true;
			} else if (autcFrag != null) {
				if (mCCFrag != null) {
					mCCFrag.hideAddUsersToConversation();
				} else {
					getSupportFragmentManager().popBackStack();
				}
				return true;
			}

			PeopleFragment peopleFrag = (PeopleFragment) getSupportFragmentManager()
					.findFragmentByTag(PeopleFragment.FRAGMENT_TAG);
			if (peopleFrag != null) {
				mHelper.goToMainPanel();
				return true;
			}
			RightSidePanelFragment rightSideFrag = (RightSidePanelFragment) getSupportFragmentManager()
					.findFragmentByTag(RightSidePanelFragment.FRAGMENT_TAG);
			if (rightSideFrag != null) {
				mHelper.goToMainPanel();
				return true;
			}
			GroupsFragment groupsFrag = (GroupsFragment) getSupportFragmentManager()
					.findFragmentByTag(GroupsFragment.FRAGMENT_TAG);
			if (groupsFrag != null) {
				if (convMode == CONVERSATION_MODE_VOICE) {
					setScreenAnalytics("MainScreen-VoiceMode");
				} else if (convMode == CONVERSATION_MODE_TEXT) {
					setScreenAnalytics("MainScreen-TextMode");
				}
			}

			if (CallState.sCurrent == CallState.OUTGOING) {

				Globals.logDebug(
						this,
						"toCALL_STATE_NORMAL "
								+ "onKeyDown, KEYCODE_BACK, currentCallState == CALL_STATE_OUTGOING");
				switchCallState(CallState.NORMAL);
				return true;
			} else if (CallState.sCurrent == CallState.INCALL_NEARBY) {
				Globals.logDebug(
						this,
						"toCALL_STATE_NORMAL"
								+ "onKeyDown, KEYCODE_BACK, currentCallState == CALL_STATE_INCALL_NEARBY");
				switchCallState(CallState.NORMAL);
				Intent intent = new Intent();
				intent.setAction(CallHistoryReceiver.ACTION_END_CALL);
				RalleeApp.getInstance().sendBroadcast(intent);
				return true;
			} else if (CallState.sCurrent == CallState.INCALL) {

				String endcallChannelName = new String(Utility.channelName);
				switchCallState(CallState.NORMAL);
				endCallFunction(endcallChannelName);
				Intent intent = new Intent();
				intent.setAction(CallHistoryReceiver.ACTION_END_CALL);
				RalleeApp.getInstance().sendBroadcast(intent);
				Globals.logDebug(
						this,
						"toCALL_STATE_NORMAL"
								+ "onKeyDown, KEYCODE_BACK, currentCallState == CALL_STATE_NORMAL");
				return true;
			}
		}

		else if (keyCode == settings.getPttKey() && event.getRepeatCount() == 0) {
			manualRecord = !mService.isRecording();
			mService.setRecording(manualRecord);
			return true;
		} else if (keyCode == KeyEvent.KEYCODE_SEARCH
				&& event.getRepeatCount() == 0) {
			PeopleFragment peopleFrag = (PeopleFragment) getSupportFragmentManager()
					.findFragmentByTag(PeopleFragment.FRAGMENT_TAG);
			if (peopleFrag != null) {
				EditText etFilter = (EditText) findViewById(R.id.etFilter);
				etFilter.requestFocus();
				((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
						.showSoftInput(etFilter,
								InputMethodManager.SHOW_IMPLICIT);
				return true;
			}
		}

		return super.onKeyDown(keyCode, event);
	}

	private int getNextInt(int i) {
		i = ++i % 10;
		return i;
	}

	// class SpeakStopper extends Handler {
	// @Override
	// public void handleMessage(Message msg) {
	// if(msg.what == speakCounter){
	// manualRecord = false;
	// mService.setRecording(manualRecord);
	// imgBtnMuteSelf.setImageResource(R.drawable.main_ui_super_grill_mute_me);
	// speak.setImageResource(R.drawable.main_ui_talk_button_down);
	// }
	// }
	// }

	//

	// ///////////////////////////////////
	// //////////////////////////////////

	// ////////////Gallery////////////////////

	public void showToast(final String msg) {
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				Toast toast = Toast.makeText(HomeActivity.this, msg,
						Toast.LENGTH_LONG);
				toast.show();
			}
		});
	}

	private class ParticipantServiceObserver extends BaseServiceObserver {
		@Override
		public void onUserAdded(final RRUser user) throws RemoteException {
			if (user == null) {
				return;
			}
			if (mService == null
					|| (mService != null && mService.getCurrentChannel() == null)) {
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			if (mService == null
					|| (mService != null && mService.getCurrentChannel() == null)) {
				return;
			}
			users = mService.getUserList();

			if (mFDFrag != null) {
				// fdFrag.galleryAnimationOnUserChanged();
				mFDFrag.notifyFriendsAdapterDataSetChange();
			}
			PeopleFragment pFrag = (PeopleFragment) getSupportFragmentManager()
					.findFragmentByTag(PeopleFragment.FRAGMENT_TAG);
			if (pFrag != null) {
				pFrag.notifyFriendsAdapterDataSetChange();
			}
			AddUsersToConversationFragment autcFrag = (AddUsersToConversationFragment) getSupportFragmentManager()
					.findFragmentByTag(
							AddUsersToConversationFragment.FRAGMENT_TAG);
			if (autcFrag != null) {
				autcFrag.notifyFriendsAdapterDataSetChange();
			}

			ParticipantsFragment partFrag = (ParticipantsFragment) getSupportFragmentManager()
					.findFragmentByTag(ParticipantsFragment.FRAGMENT_TAG);
			if (partFrag != null) {
				partFrag.notifyFriendsAdapterDataSetChange();
			}
			PrivateGroupsListFragment pglFrag = (PrivateGroupsListFragment) getSupportFragmentManager()
					.findFragmentByTag(PrivateGroupsListFragment.FRAGMENT_TAG);
			if (pglFrag != null) {
				pglFrag.notifyFriendsAdapterDataSetChange();
			}

			PrivateGroupDetailsFragment pgdFrag = (PrivateGroupDetailsFragment) getSupportFragmentManager()
					.findFragmentByTag(PrivateGroupDetailsFragment.FRAGMENT_TAG);
			if (pgdFrag != null) {
				pgdFrag.notifyFriendsAdapterDataSetChange();
			}

			Globals.logDebug(this, "onUserAdded: " + user.userName);
			// VoiceModePanelFragment frag = (VoiceModePanelFragment)
			// getSupportFragmentManager()
			// .findFragmentByTag(VoiceModePanelFragment.FRAGMENT_TAG);
			if (mVMPFrag != null) {
				mVMPFrag.refreshUserTalkState(user);
			}
			countUsersOnCurrentChannel();

			if (user.getChannel().id == mService.getCurrentChannel().id
					|| user.getChannel().id == 0) {
				Intent usersChangedIntent = new Intent();
				usersChangedIntent.setAction("com.radiorunt.users.CHANGED");
				HomeActivity.this.sendBroadcast(usersChangedIntent);

				if (user.getChannel().id != 0) {
					// Intent intent = new Intent();
					// intent.setAction(GroupConversationReceiver.ACTION_CHANGED);
					// intent.putExtra(GroupConversationReceiver.USER_ID_EXTRA,
					// user.userName);
					// HomeActivity.this.sendBroadcast(intent);
					if (mCCFrag != null) {
						mCCFrag.addParticipants();
					}
				}
			}
			showMessageNotificationUserIsOnline(user);
		}

		@Override
		public void onUserRemoved(final RRUser user) throws RemoteException {
			if (user == null) {
				return;
			}
			if (mService == null
					|| (mService != null && mService.getCurrentChannel() == null)) {
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			if (mService == null
					|| (mService != null && mService.getCurrentChannel() == null)) {
				return;
			}
			users = mService.getUserList();

			if (mFDFrag != null) {
				// fdFrag.galleryAnimationOnUserChanged();
				mFDFrag.notifyFriendsAdapterDataSetChange();
			}

			PeopleFragment pFrag = (PeopleFragment) getSupportFragmentManager()
					.findFragmentByTag(PeopleFragment.FRAGMENT_TAG);
			if (pFrag != null) {
				pFrag.notifyFriendsAdapterDataSetChange();
			}

			AddUsersToConversationFragment autcFrag = (AddUsersToConversationFragment) getSupportFragmentManager()
					.findFragmentByTag(
							AddUsersToConversationFragment.FRAGMENT_TAG);
			if (autcFrag != null) {
				autcFrag.notifyFriendsAdapterDataSetChange();
			}

			ParticipantsFragment partFrag = (ParticipantsFragment) getSupportFragmentManager()
					.findFragmentByTag(ParticipantsFragment.FRAGMENT_TAG);
			if (partFrag != null) {
				partFrag.notifyFriendsAdapterDataSetChange();
			}

			PrivateGroupsListFragment pglFrag = (PrivateGroupsListFragment) getSupportFragmentManager()
					.findFragmentByTag(PrivateGroupsListFragment.FRAGMENT_TAG);
			if (pglFrag != null) {
				pglFrag.notifyFriendsAdapterDataSetChange();
			}

			PrivateGroupDetailsFragment pgdFrag = (PrivateGroupDetailsFragment) getSupportFragmentManager()
					.findFragmentByTag(PrivateGroupDetailsFragment.FRAGMENT_TAG);
			if (pgdFrag != null) {
				pgdFrag.notifyFriendsAdapterDataSetChange();
			}

			Globals.logDebug(this, "onUserRemoved: " + user.userName);
			countUsersOnCurrentChannel();

			if (user.getChannel().id == mService.getCurrentChannel().id
					|| user.getChannel().id == 0) {
				Intent usersChangedIntent = new Intent();
				usersChangedIntent.setAction("com.radiorunt.users.CHANGED");
				HomeActivity.this.sendBroadcast(usersChangedIntent);
			}
		}

		@Override
		public void onUserUpdated(final RRUser user) throws RemoteException {
			if (user == null) {
				return;
			}

			if (mService == null
					|| (mService != null && mService.getCurrentChannel() == null)) {
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			if (mService == null
					|| (mService != null && mService.getCurrentChannel() == null)) {
				return;
			}

			users = mService.getUserList();
			// if (mFDFrag != null) {
			// mFDFrag.notifyFriendsAdapterDataSetChange();
			// }

			PeopleFragment pFrag = (PeopleFragment) getSupportFragmentManager()
					.findFragmentByTag(PeopleFragment.FRAGMENT_TAG);
			if (pFrag != null) {
				pFrag.notifyFriendsAdapterDataSetChange();
			}

			AddUsersToConversationFragment autcFrag = (AddUsersToConversationFragment) getSupportFragmentManager()
					.findFragmentByTag(
							AddUsersToConversationFragment.FRAGMENT_TAG);
			if (autcFrag != null) {
				autcFrag.notifyFriendsAdapterDataSetChange();
			}

			ParticipantsFragment partFrag = (ParticipantsFragment) getSupportFragmentManager()
					.findFragmentByTag(ParticipantsFragment.FRAGMENT_TAG);
			if (partFrag != null) {
				partFrag.notifyFriendsAdapterDataSetChange();
			}

			PrivateGroupsListFragment privglFrag = (PrivateGroupsListFragment) getSupportFragmentManager()
					.findFragmentByTag(PrivateGroupsListFragment.FRAGMENT_TAG);
			if (privglFrag != null) {
				privglFrag.notifyFriendsAdapterDataSetChange();
			}

			PrivateGroupDetailsFragment privgdFrag = (PrivateGroupDetailsFragment) getSupportFragmentManager()
					.findFragmentByTag(PrivateGroupDetailsFragment.FRAGMENT_TAG);
			if (privgdFrag != null) {
				privgdFrag.notifyFriendsAdapterDataSetChange();
			}

			// PublicGroupsListFragment pubglFrag = (PublicGroupsListFragment)
			// getSupportFragmentManager().findFragmentByTag(PublicGroupsListFragment.FRAGMENT_TAG);
			// if (pubglFrag != null) {
			// pubglFrag.notifyFriendsAdapterDataSetChange();
			// }

			// countFriendsOnRR();
			// mAdapter.notifyDataSetChanged();
			Globals.logDebug(this, "onUserUpdated: " + user.userName);
			countUsersOnCurrentChannel();

			if (user.getChannel().id == mService.getCurrentChannel().id
					|| user.getChannel().id == 0) {
				Intent usersChangedIntent = new Intent();
				usersChangedIntent.setAction("com.radiorunt.users.CHANGED");
				HomeActivity.this.sendBroadcast(usersChangedIntent);

				if (user.getChannel().id != 0) {
					if (mCCFrag != null) {
						mCCFrag.addParticipants();
					}
					if (CallState.sCallType != CallState.CALL_PRIVATE
							|| CallState.sCurrent != CallState.INCALL) {
						showMessageNotificationUserJoinedGroup(user);
					}
					// Intent intent = new Intent();
					// intent.setAction(GroupConversationReceiver.ACTION_CHANGED);
					// intent.putExtra(GroupConversationReceiver.USER_ID_EXTRA,
					// user.userName);
					// HomeActivity.this.sendBroadcast(intent);
				}
			}
		}

		@Override
		public void onUserTalkStateUpdated(final RRUser user)
				throws RemoteException {
			Globals.logDebug(this, "onUserUpdated: " + user.userName);
			// VoiceModePanelFragment frag = (VoiceModePanelFragment)
			// getSupportFragmentManager()
			// .findFragmentByTag(VoiceModePanelFragment.FRAGMENT_TAG);
			if (mVMPFrag != null) {
				mVMPFrag.refreshUserTalkState(user);
			}
		}

		@Override
		public void onMessageReceived(final RRMessages msg)
				throws RemoteException {

			// loadFriends();

			Globals.logDebug(this, "HOME MESSAGE RECEIVED");
			try {
				processMessage(msg.message);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			msgsTimestamp = msg.timestamp;
			// mAdapter.notifyDataSetChanged();
		}

		@Override
		public void onMessageSent(final RRMessages msg) throws RemoteException {
			// mAdapter.notifyDataSetChanged();
			Globals.logDebug(this, "HOME MESSAGE SENT");
			mService.messagesClearAll();
		}

		@Override
		public void onCurrentChannelChanged() throws RemoteException {
			if (mService.getCurrentChannel() == null) {
				return;
			} else {
				ROOT_ID = mService.getCurrentChannel().id;
			}
		}

		@Override
		public void onChannelUpdated(final RRChannels channel)
				throws RemoteException {
			chList = mService.getChannelList();
			if (mService.getCurrentChannel() == null || channel.id == 0) {
				return;
			} else {
				ROOT_ID = mService.getCurrentChannel().id;
				if (channel.id == ROOT_ID) {
					userJoinedChannelTone();
					int usersOnChannel = mService.getCurrentChannel().userCount;
					// Toast.makeText(HomeActivity.this,
					// "On channel: " + usersOnChannel, 1000).show();

					if (usersOnChannel > 2
							&& CallState.sCallType == CallState.CALL_PRIVATE) {
						// Intent intent = new Intent();
						// intent.setAction(GroupConversationReceiver.ACTION_STARTED_GROUP);
						// HomeActivity.this.sendBroadcast(intent);
						if (CallState.sCallType == CallState.CALL_PRIVATE) {
							if (mCCFrag != null) {
								mCCFrag.addParticipants();
							}
							// intent = new Intent();
							// intent.setAction(CallHistoryReceiver.ACTION_CHANGE_TO_GROUP);
							// HomeActivity.this.sendBroadcast(intent);
						}
					}
				}
			}

			if (channel.name.startsWith("_fbgroup_")) {
				FacebookGroupDetailsFragment fgdFragment = (FacebookGroupDetailsFragment) getSupportFragmentManager()
						.findFragmentByTag(
								FacebookGroupDetailsFragment.FRAGMENT_TAG);
				if (fgdFragment != null) {
					fgdFragment.notifyFBgroupsChange();
				}

			}

			if (channel.name.startsWith("_prm_")) {

				PublicGroupsListFragment pglFragment = (PublicGroupsListFragment) getSupportFragmentManager()
						.findFragmentByTag(
								PublicGroupsListFragment.FRAGMENT_TAG);
				if (pglFragment != null) {
					pglFragment.notifyChannelStateChanged();
				}

				PublicGroupDetailsFragment pgdFragment = (PublicGroupDetailsFragment) getSupportFragmentManager()
						.findFragmentByTag(
								PublicGroupDetailsFragment.FRAGMENT_TAG);
				if (pgdFragment != null) {
					pgdFragment.notifyFriendsAdapterDataSetChange();
				}

			}

		}

		@Override
		public void onChannelAdded(RRChannels channel) throws RemoteException {
			// TODO Auto-generated method stub
			super.onChannelAdded(channel);

			if (channel.name.startsWith("_fbgroup_")) {
				FacebookGroupsListFragment fglFragment = (FacebookGroupsListFragment) getSupportFragmentManager()
						.findFragmentByTag(
								FacebookGroupsListFragment.FRAGMENT_TAG);
				if (fglFragment != null) {
					fglFragment.notifyFBgroupsChange();
				}
				FacebookGroupDetailsFragment fgdFragment = (FacebookGroupDetailsFragment) getSupportFragmentManager()
						.findFragmentByTag(
								FacebookGroupDetailsFragment.FRAGMENT_TAG);
				if (fgdFragment != null) {
					fgdFragment.notifyFBgroupsChange();
				}

			}
		}

		@Override
		public void onChannelRemoved(RRChannels channel) throws RemoteException {
			// TODO Auto-generated method stub
			super.onChannelRemoved(channel);

			if (channel.name.startsWith("_fbgroup_")) {
				FacebookGroupsListFragment fglFragment = (FacebookGroupsListFragment) getSupportFragmentManager()
						.findFragmentByTag(
								FacebookGroupsListFragment.FRAGMENT_TAG);
				if (fglFragment != null) {
					fglFragment.notifyFBgroupsChange();
				}
				FacebookGroupDetailsFragment fgdFragment = (FacebookGroupDetailsFragment) getSupportFragmentManager()
						.findFragmentByTag(
								FacebookGroupDetailsFragment.FRAGMENT_TAG);
				if (fgdFragment != null) {
					fgdFragment.notifyFBgroupsChange();
				}
			}
		}

		@Override
		public void onCurrentUserUpdated() throws RemoteException {
			if (mService == null)
				return;
			if (mService.getCurrentChannel() == null)
				return;

			users = mService.getUserList();
			if (settings.getAudioStream() == AudioManager.STREAM_VOICE_CALL) {
				if (mService.getCurrentChannel().id != 0) {
					if (mService.isRecording() == false) {
						mService.setRecording(true);
					} else {
						mService.setRecording(true);
					}
				} else {
					mService.setRecording(false);
				}
			}
			if (mService.getCurrentChannel().id == 0) {
				// mService.restartPingAlarm(120);
				mService.muteSelf(false);
			} else {
				// mService.restartPingAlarm(1);
				Globals.logDebug(this, "msg CURRENT_CH_NAME " + CURRENT_CH_NAME);
				CURRENT_CH_NAME = mService.getCurrentChannel().name;
			}
			// if(mService.getCurrentUser().muted){
			// imgBtnMuteSelf.setImageResource(R.drawable.main_ui_super_grill_mute_me_on);
			// muteOn = false;
			// }else{
			// imgBtnMuteSelf.setImageResource(R.drawable.main_ui_super_grill_mute_me);
			// muteOn = true;
			// }
		}

	}

	@Override
	protected IServiceObserver createServiceObserver() {
		return new ParticipantServiceObserver();
	}

	int getNumberOfUsersOnCurrentChannel() {
		int num = 0;
		users = mService.getUserList();
		final Iterator<RRUser> i = users.iterator();
		if (mService.getCurrentChannel() == null) {
			return -1;
		}
		final int idOfCurrentChannel = mService.getCurrentChannel().id;
		while (i.hasNext()) {
			final RRUser user = i.next();
			if (user.getChannel().id == idOfCurrentChannel) {
				num++;
			}
		}
		return num;
	}

	protected void countUsersOnCurrentChannel() {
		synchronized (friendsLock) {
			users = mService.getUserList();
			final Iterator<RRUser> i = users.iterator();
			int currentChannelUserCount = 0;
			if (mService.getCurrentChannel() == null) {
				return;
			}
			final int idOfCurrentChannel = mService.getCurrentChannel().id;
			currentChannelUserCount = mService.getCurrentChannel().userCount;
			if (idOfCurrentChannel != 0) { // checking if outgoing call has been
											// accepted
				Globals.logDebug(this, "isThereChann NOT on ROOT");
				if (currentChannelUserCount > 1) { // ME+1

					if (CallState.sCurrent == CallState.OUTGOING) {
						callHandler.removeMessages(CALLER_UNANSWERED);
						if (CallState.sCallType == CallState.CALL_PRIVATE) {
							Intent intent = new Intent();
							intent.setAction(CallHistoryReceiver.ACTION_ADD_CALL);
							intent.putExtra(
									CallHistoryReceiver.EXTRA_TYPE,
									DbContentProvider.CALL_HISTORY_TYPE_PRIVATE_CALL_OUTGOING);
							intent.putExtra(CallHistoryReceiver.EXTRA_ID,
									Utility.calledUserUsername);
							sendBroadcast(intent);
						} else if (CallState.sCallType == CallState.CALL_RANDOM) {
							Intent intent = new Intent();
							intent.setAction(CallHistoryReceiver.ACTION_ADD_CALL);
							intent.putExtra(
									CallHistoryReceiver.EXTRA_TYPE,
									DbContentProvider.CALL_HISTORY_TYPE_RANDOM_CALL);
							intent.putExtra(CallHistoryReceiver.EXTRA_ID,
									Utility.calledUserUsername);
							sendBroadcast(intent);
						}

						switchCallState(CallState.INCALL);
					}
				}
			} else { // user is on ROOT
				Globals.logDebug(this, "on ROOT");
				if (CallState.sCurrent == CallState.INCOMING) {
					// checking if incoming call is canceled
					Globals.logDebug(this, "ringing");
					Handler ringingHandler = new Handler() {
						@Override
						public void handleMessage(Message msg) {
							// TODO Auto-generated method stub
							super.handleMessage(msg);
							if (CallState.sCurrent == CallState.INCOMING
									&& msg.what == 1
									&& !Utility.channelName.equals("")) {
								if (!isThereChann(Utility.channelName)) {
									this.removeMessages(1);
									Globals.logDebug(this,
											"there is no Channel named: "
													+ Utility.channelName);
									Globals.logDebug(this, "there is NOT: "
											+ Utility.channelName);
									callHandler
											.removeMessages(CALLER_UNANSWERED);

									Globals.logDebug(this,
											"toCALL_STATE_NORMAL countUsersOnCurrentChannel, CALLER HAS CANCELED CALL");
									NotificationFragment nFrag = (NotificationFragment) getSupportFragmentManager()
											.findFragmentByTag(
													NotificationFragment.FRAGMENT_TAG);
									if (nFrag != null) {
										nFrag.removeIncomingCallNotification();
									}
									switchCallState(CallState.NORMAL);
									Toast.makeText(getApplicationContext(),
											R.string.user_canceled_call, 500)
											.show();

								} else {
									this.sendEmptyMessageDelayed(1, 2000);
									Globals.logDebug(this, "there is : "
											+ Utility.channelName);
								}
							} else {
								this.removeMessages(1);
							}
						}
					};
					ringingHandler.removeMessages(1);
					ringingHandler.sendEmptyMessage(1);
				}
				// else if(BAR_MODE != BAR_MODE_GALLERY && BAR_MODE !=
				// BAR_MODE_LOADING_FRIENDS){
				// BAR_MODE = BAR_MODE_GALLERY;
				// viewFlipper.setDisplayedChild(BAR_MODE);
				//
				// Log.i("callBug", "ON ROOT switch to BAR_MODE_GALLERY");
				// }
				Globals.logDebug(this, "not ringing");
			}
		}
	}

	// protected void countFriendsOnRR() {
	// synchronized (friendsLock) {
	// ArrayList<RRUser> newOnlineUsers = new ArrayList<RRUser>();
	// ArrayList<RRUser> newOfflineUsers = new ArrayList<RRUser>();
	// for (RRUser onU : myOnlineUsers) {
	// final Iterator<RRUser> i = users.iterator();
	// boolean isOnline = false;
	// while (i.hasNext()) {
	// final RRUser user = i.next();
	// if (onU.userName.equals(user.userName)) {
	// isOnline = true;
	// insertInPlace(newOnlineUsers, onU);
	// break;
	// }
	// }
	// if (!isOnline) {
	// insertInPlace(newOfflineUsers, onU);
	// }
	// }
	//
	// for (RRUser offU : myOfflineUsers) {
	// final List<RRUser> users2 = mService.getUserList();
	// final Iterator<RRUser> i2 = users2.iterator();
	// boolean isOnline = false;
	// while (i2.hasNext()) {
	// final RRUser user = i2.next();
	// if (offU.userName.equals(user.userName)) {
	// insertInPlace(newOnlineUsers, offU);
	// isOnline = true;
	// break;
	// }
	// }
	// if (!isOnline) {
	// insertInPlace(newOfflineUsers, offU);
	// }
	// }
	// myOfflineUsers = newOfflineUsers;
	// myOnlineUsers = newOnlineUsers;
	// }
	// mHandler.post(new Runnable() {
	// @Override
	// public void run() {
	// FriendsDockFragment fdFrag = (FriendsDockFragment)
	// getSupportFragmentManager()
	// .findFragmentByTag(FriendsDockFragment.FRAGMENT_TAG);
	// if (fdFrag != null) {
	// fdFrag.notifyFriendsAdapterDataSetChange();
	// }
	// }
	// });
	// }

	synchronized public void processMessage(String msg) throws Exception {

		ObjectMapper mapper = new ObjectMapper();
		RRPushMessagePayload payload = null;
		try {

			Globals.logDebug(this, "processMessage: " + msg);
			payload = mapper.readValue(msg, RRPushMessagePayload.class);
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

		if (payload == null
				|| (payload != null && (payload.payloadType.equals("filter")
						|| payload.payloadType.equals("filteringOn")
						|| payload.payloadType.equals("")
						|| payload.payloadType.equals("transcriptingOff") || payload.payloadType
							.equals("transcriptingOn")))) {
			mService.messagesClearAll();
			return;
		}

		// countFriendsOnRR();
		mService.messagesClearAll();

		if (payload.payloadType == null || payload.sender == null) {
			return;
		}

		if (payload.payloadType.equals("") || payload.sender.equals("")) {
			return;
		}

		// Cursor c = dbAdapter.fetchUser(payload.sender);
		Uri uri_user = Uri.parse(DbContentProvider.CONTENT_URI_USER_TABLE
				.toString() + "/" + payload.sender);
		Cursor c = getApplicationContext().getContentResolver().query(uri_user,
				null, null, null, null);

		String senderName = "Unknown";
		String senderPicUrl = "";
		if (c != null) {
			if (c.getCount() != 0) {
				senderName = c.getString(c
						.getColumnIndex(DbContentProvider.USER_COL_NAME));
				senderPicUrl = c.getString(c
						.getColumnIndex(DbContentProvider.USER_COL_PIC_URL));
			}
		}
		c.close();

		boolean saveMissedCall = true;
		if (senderName.equals("Unknown")) {

			Globals.logDebug(this, "random Unknown user");
			saveMissedCall = false;
			String fbIdOfRandomUser = Utility.parseSNData(payload.sender)
					.getAsString(Utility.SOCIAL_NETWORK_ID);
			RRUser unknownUser = FacebookUserDataLoader
					.getFbUserById(fbIdOfRandomUser);
			senderName = unknownUser.FirstName;
			senderPicUrl = unknownUser.picUrl;

			Globals.logDebug(this, "random Unknown user: " + senderName
					+ " pic url:" + senderPicUrl);

			if (!payload.payloadType.equals("randomCall")) {
				// Toast.makeText(getApplicationContext(),
				// "Refreashing Facebook for new friends.", 2000).show();
				// loadFriends();
				Intent serviceGetFriendsFromFB = new Intent(this,
						RequestFBData.class);
				startService(serviceGetFriendsFromFB);
			}
		}

		if (payload.payloadType.equals("channelId")
				|| payload.payloadType.equals("randomCall")
				|| payload.payloadType.equals("groupConversationCall")) {
			if (payload.timestamp < 0) {
				return;
			}
			// dbAdapter.createMissedCall(payload.timestamp, payload.sender); //
			// Kimi

			if (saveMissedCall) {

				Globals.logDebug(this, "random saveMissedCall");
				ContentValues values = new ContentValues();
				values.put(DbContentProvider.MISSED_CALLS_COL_TIMESTAMP,
						payload.timestamp);
				values.put(DbContentProvider.MISSED_CALLS_COL_SENDER,
						payload.sender);
				Uri uri = getApplicationContext()
						.getContentResolver()
						.insert(DbContentProvider.CONTENT_URI_MISSED_CALL_TABLE,
								values);
			}

			boolean isThereChann = false;
			RRChannels tempCh = new RRChannels();
			if (mService != null) {

				for (int i = 0; i < chList.size(); i++) {
					if (chList.get(i).name.equals(payload.channelName)) {
						isThereChann = true;
						tempCh.id = chList.get(i).id;
						break;
					} else {
						isThereChann = false;
					}
				}

			}

			if ((CallState.sCurrent != CallState.NORMAL && isThereChann)
					|| senderName.equals("Unknown")) {
				RRPushMessagePayload payload2 = new RRPushMessagePayload();
				payload2.sender = RalleeApp.getInstance().getRalleeUID();
				payload2.payloadType = "userBusy";
				try {
					mService.sendChannelTextMessage(
							mapper.writeValueAsString(payload2), tempCh);
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
				return;
			}

			if (!isThereChann) {
				Toast.makeText(this,
						senderName + getString(R.string.has_canceled_call), 500)
						.show();
				return;
			}

			Utility.channelName = payload.channelName;
			Utility.senderName = senderName;
			Utility.senderPicUrl = senderPicUrl;
			ContentValues SNData = Utility.parseSNData(payload.sender);
			Utility.senderFBId = SNData.getAsString(Utility.SOCIAL_NETWORK_ID);
			Utility.callTimestamp = payload.timestamp;

			dismissDialog();
			cleanDialogs();
			if (payload.payloadType.equals("randomCall")) {

				showDialogIncomingRandom();
				CallState.sCallType = CallState.CALL_RANDOM;
			} else {
				CallState.sCallType = CallState.CALL_PRIVATE;
			}
			switchCallState(CallState.INCOMING);

			if (getSupportFragmentManager().findFragmentByTag(
					PeopleFragment.FRAGMENT_TAG) != null
					|| getSupportFragmentManager().findFragmentByTag(
							RightSidePanelFragment.FRAGMENT_TAG) != null
					|| getSupportFragmentManager().findFragmentByTag(
							GroupsFragment.FRAGMENT_TAG) != null
					|| getSupportFragmentManager().findFragmentByTag(
							PrivateGroupDetailsFragment.FRAGMENT_TAG) != null
					|| getSupportFragmentManager().findFragmentByTag(
							PublicGroupDetailsFragment.FRAGMENT_TAG) != null
					|| getSupportFragmentManager().findFragmentByTag(
							FacebookGroupDetailsFragment.FRAGMENT_TAG) != null) {
				if (payload.payloadType.equals("randomCall")) {
//					System.out.println("Maidul Testing ");

					closeFragmentDueToIncomingCall();
				} else {
					showMessageNotificationIncomingCall(payload.sender);
				}
				// showMessageNotificationIncomingCall(payload.sender);
			}

		} else if (payload.payloadType.equals("callDismissed")) {
			if (payload != null && payload.sender != null) {
				CallState.calledFriends.remove(payload.sender);
				AddUsersToConversationFragment autcFrag = (AddUsersToConversationFragment) getSupportFragmentManager()
						.findFragmentByTag(
								AddUsersToConversationFragment.FRAGMENT_TAG);
				if (autcFrag != null) {
					autcFrag.notifyFriendsAdapterDataSetChange();
				}

			}

			if (getNumberOfUsersOnCurrentChannel() > 1) {
				Toast.makeText(getApplicationContext(),
						senderName + getString(R.string.has_rejected_call), 500)
						.show();
			} else {

				if (CallState.sCallType == CallState.CALL_PRIVATE
						|| CallState.sCallType == CallState.CALL_RANDOM) {

					Toast.makeText(getApplicationContext(),
							R.string.user_rejected_call, 500).show();
					Globals.logDebug(this, "toCALL_STATE_NORMAL callDismissed");
					showRandomDialogIfNeeded();
					if (payload.payloadType.equals("randomCall")) {
//						System.out.println("Maidul Testing ");
						switchCallState(CallState.NORMAL);
						//mCCFrag.Invisible();
					} else {
						switchCallState(CallState.NORMAL);
					}

				} else if (CallState.sCallType == CallState.CALL_PRIVATE_GROUP) {
					Toast.makeText(getApplicationContext(),
							senderName + getString(R.string.has_rejected_call),
							500).show();
				}
			}
			mService.messagesClearAll();
		} else if (payload.payloadType.equals("callEnded")) {
			if (getNumberOfUsersOnCurrentChannel() > 1) { // Me + 1, do not
															// leave the channel
															// until you are
															// alone on channel
				Toast.makeText(this,
						senderName + getString(R.string.has_left_group), 500)
						.show();
			} else {
				if (!(CallState.sCallType == CallState.CALL_RANDOM && returnToRandomDialog)) {
					showCustomDialog(1, R.string.user_ended_call, "",
							R.string.label_ok, null, 0, null);
				}

				clearExtendedBottomPanel();
				showRandomDialogIfNeeded();
				switchCallState(CallState.NORMAL);
				Intent intent = new Intent();
				intent.setAction(CallHistoryReceiver.ACTION_END_CALL);
				RalleeApp.getInstance().sendBroadcast(intent);

			}

			mService.messagesClearAll();
		} else if (payload.payloadType.equals("userBusy")) {
			if (getNumberOfUsersOnCurrentChannel() > 1) { // Me + 1, do not
															// leave the channel
															// until you are
															// alone on channel
				Toast.makeText(this, senderName + getString(R.string.is_busy),
						500).show();
			} else {

				Toast.makeText(getApplicationContext(), R.string.user_is_busy,
						500).show();
				Globals.logDebug(this, "toCALL_STATE_NORMAL userBusy");
				showRandomDialogIfNeeded();
				switchCallState(CallState.NORMAL);

				mService.messagesClearAll();
			}
		}
	}

	private void clearExtendedBottomPanel() {
		ParticipantsFragment partFrag = (ParticipantsFragment) getSupportFragmentManager()
				.findFragmentByTag(ParticipantsFragment.FRAGMENT_TAG);
		if (partFrag != null) {
			getSupportFragmentManager().popBackStack();
		}

		AddUsersToConversationFragment autcFrag = (AddUsersToConversationFragment) getSupportFragmentManager()
				.findFragmentByTag(AddUsersToConversationFragment.FRAGMENT_TAG);
		if (autcFrag != null) {
			getSupportFragmentManager().popBackStack();
		}

	}

	private boolean isThereChann(String channelName) {
		// TODO Auto-generated method stub
		boolean isThereChann = false;

		RRChannels tempCh = new RRChannels();
		if (mService != null) {
			for (int i = 0; i < chList.size(); i++) {
				if (chList.get(i).name.equals(channelName)) {
					isThereChann = true;
					tempCh.id = chList.get(i).id;
					break;
				}
			}
		}
		return isThereChann;

	}

	// protected void connectionUserStatus() {
	// final Iterator<RRUser> i = users.iterator();
	// while (i.hasNext()) {
	// final RRUser user = i.next();
	// if (user.userName.equals(Utility.userName)) {
	// Utility.connectionStatus = true;
	// break;
	// } else {
	// Utility.connectionStatus = false;
	// }
	// }
	// }

	// protected List<RRUser> getUsersOnCurrentChannel() {
	// synchronized (friendsLock) {
	// final Iterator<RRUser> i = users.iterator();
	// List<RRUser> usersOnChannel = new ArrayList<RRUser>();
	//
	// while (i.hasNext()) {
	// final RRUser user = i.next();
	// if (user.getChannel().id == mService.getCurrentChannel().id) {
	// for (int j = 0; j < myOnlineUsers.size(); j++) {
	// if (myOnlineUsers.get(j).userName.equals(user.userName)) {
	// usersOnChannel.add(myOnlineUsers.get(j));
	// }
	// }
	// }
	// }
	// return usersOnChannel;
	// }
	// }

	private void insertInPlace(ArrayList<RRUser> list, RRUser u) {
		for (int i = 0; i < list.size(); i++) {
			int res = list.get(i).FirstName.compareToIgnoreCase(u.FirstName);
			if (res > 0) {
				list.add(i, u);
				return;
			}
		}
		list.add(u);
		return;
	}

	// private RRUser removeFromList(ArrayList<RRUser> list, RRUser u) {
	// for (int i = 0; i < list.size(); i++) {
	// if (list.get(i).userName.equals(u.userName)) {
	// FriendsDockFragment fdFrag = (FriendsDockFragment)
	// getSupportFragmentManager()
	// .findFragmentByTag(FriendsDockFragment.FRAGMENT_TAG);
	// if (fdFrag != null) {
	// fdFrag.galleryAnimationOnUserChanged();
	// }
	// return list.remove(i);
	// }
	// }
	// return null;
	// }

	// public void checkMissedCalls() {
	// Cursor c = getApplicationContext().getContentResolver().query(
	// DbContentProvider.CONTENT_URI_MISSED_CALL_TABLE, null, null,
	// null, null);
	//
	// try {
	// if (c != null) {
	// if (c.getCount() > 0) {
	// Message msg = new Message();
	// Bundle messageData = new Bundle();
	// String stringMessage = "";
	// if (c.getCount() > 1) {
	// stringMessage = c.getCount() + " missed calls.";
	// } else {
	// stringMessage = c.getCount() + " missed call.";
	// }
	// messageData.putString("message", stringMessage);
	// msg.setData(messageData);
	// msg.what = SET_MESSAGE;
	// internalNotificationHandler.removeMessages(DISMISS_MESSAGE);
	// internalNotificationHandler.sendMessage(msg);
	// internalNotificationHandler.sendEmptyMessageDelayed(
	// DISMISS_MESSAGE, 30000);
	// } else {
	// internalNotificationHandler.removeMessages(DISMISS_MESSAGE);
	// internalNotificationHandler
	// .sendEmptyMessage(DISMISS_MESSAGE);
	// }
	// } else {
	// internalNotificationHandler.removeMessages(DISMISS_MESSAGE);
	// internalNotificationHandler.sendEmptyMessage(DISMISS_MESSAGE);
	// }
	// } finally {
	// if (c != null && !c.isClosed()) {
	// c.close();
	// }
	// }
	// }

	protected void deleteOldPicFile() {
		// TODO Auto-generated method stub

	}

	public boolean dialTone(Boolean dial) {

		if (dial) {
			if (tg == null) {
				tg = new ToneGenerator(settings.getAudioStream(),
						ToneGenerator.MAX_VOLUME);
			}
			tg.startTone(ToneGenerator.TONE_SUP_RINGTONE);
			return true;
		} else {
			if (tg != null) {
				tg.stopTone();
				tg.release();
				tg = null;
			}
			return false;
		}
	}

	public void userJoinedChannelTone() {

		if (beepGenerator == null) {
			beepGenerator = new ToneGenerator(settings.getAudioStream(),
					ToneGenerator.MAX_VOLUME);
		}
		beepGenerator.startTone(ToneGenerator.TONE_PROP_BEEP);
	}

	public void chooseNearMeChannel() {
		RRChannels nearMeChann = new RRChannels();

		CountryName t = CountryName.World;

		for (int j = 0; j < COUNTRY_NAMES.length; j++) {
			if (ListOfCountries.HOME_COUNTRY.equals(COUNTRY_NAMES[j].name())) {
				t = COUNTRY_NAMES[j];
				break;
			}

		}

		switch (t) {
		case Serbia:
			for (int i = 0; i < chList.size(); i++) {
				if (chList.get(i).name.equals("EUROPE")) {
					nearMeChann = chList.get(i);
					Utility.channelName = "EUROPE";
					break;
				}
			}
			break;
		case Canada:
			for (int i = 0; i < chList.size(); i++) {
				if (chList.get(i).name.equals("CANADA")) {
					nearMeChann = chList.get(i);
					Utility.channelName = "CANADA";
					break;
				}
			}
			break;
		case Usa:
			for (int i = 0; i < chList.size(); i++) {
				if (chList.get(i).name.equals("USA")) {
					nearMeChann = chList.get(i);
					Utility.channelName = "USA";
					break;
				}
			}
			break;
		default:
			for (int i = 0; i < chList.size(); i++) {
				if (chList.get(i).name.equals("WORLD")) {
					nearMeChann = chList.get(i);
					Utility.channelName = "WORLD";
					break;
				}
			}
			break;
		}
		mService.joinChannel(nearMeChann.id);
	}

	public void restoreNormalState() {
		try {
			switchCallState(CallState.NORMAL);
//			Log.i("callState", "Call State restore: to: CALL_STATE_NORMAL");

			TranslateAnimation slideDownInTranslation = new TranslateAnimation(
					TranslateAnimation.RELATIVE_TO_PARENT, 0,
					TranslateAnimation.RELATIVE_TO_PARENT, 0,
					TranslateAnimation.RELATIVE_TO_PARENT, -1.0f,
					TranslateAnimation.RELATIVE_TO_PARENT, 0);

			TranslateAnimation slideDownOutTranslation = new TranslateAnimation(
					TranslateAnimation.RELATIVE_TO_PARENT, 0,
					TranslateAnimation.RELATIVE_TO_PARENT, 0,
					TranslateAnimation.RELATIVE_TO_PARENT, 0,
					TranslateAnimation.RELATIVE_TO_PARENT, 1.0f);
			slideDownInTranslation.setDuration(500);
			slideDownOutTranslation.setDuration(500);

			Utility.calledUserUsername = "";
			Utility.channelName = "";
			Utility.senderName = "";
			Utility.senderPicUrl = "";
			Utility.senderFBId = "";
			Utility.callTimestamp = 0;

			// if (mVMPFrag != null) {
			// mVMPFrag.updateFragment(R.id.tvCallerNameHomeActivity,
			// getString(R.string.choose_a_friend));
			// mVMPFrag.updateFragment(R.id.main_ui_phone_state,
			// R.drawable.main_ui_lcd_panel_phone_inactive);
			// mVMPFrag.updateFragment(R.id.chronometer, false); // STOP
			// mVMPFrag.updateFragment(R.id.chronometer, "0:00:00");
			// }
			// ringtone.stop();
			isDialing = dialTone(false);
			// checkMissedCalls();
		} catch (Exception e) {
//			Log.e("radiorunt", "failed to restore to normal state " + e);
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public final boolean onCreateOptionsMenu(final Menu menu) {
		// super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.home_menu, menu);
		return true;
	}

	@Override
	public final boolean onMenuItemSelected(final int featureId,
			final MenuItem item) {
		Intent i;

		switch (item.getItemId()) {
		case R.id.settings:
			if (mService == null) {
				return false;
			}
			if (mService.getCurrentChannel() == null) {
				return false;
			}
			if (mService.getCurrentChannel().id == 0) {
				mService.stopAudioThreads();
				Speex.close();
				Speexrec.close();
				Globals.logDebug(this, "AEC INIT " + Speexrec.getAecStatus());
				i = new Intent(this, Preferences.class);
				startActivityForResult(i, PREF_CHANGED);
				setScreenAnalytics("Settings");
			} else {
				Toast.makeText(this, R.string.settings_while_talking, 1000)
						.show();
			}
			return true;
		case R.id.feedback:
			iWasOnFeedbackOrReportActivity = 1;
			Intent emailIntent = new Intent(
					android.content.Intent.ACTION_SENDTO);
			String uriText;
			uriText = "mailto:feedback@rall.ee" + "?subject="
					+ getString(R.string.feedback_subject) + "&body="
					+ getString(R.string.feedback_body);
			uriText = uriText.replace(" ", "%20");
			Uri uri = Uri.parse(uriText);

			emailIntent.setData(uri);

			startActivity(Intent.createChooser(emailIntent,
					getString(R.string.feedback_chooser)));
			return true;
		case R.id.report:
			iWasOnFeedbackOrReportActivity = 1;
			Intent emailReportIntent = new Intent(
					android.content.Intent.ACTION_SENDTO);
			String uriReportText;
			uriReportText = "mailto:feedback@rall.ee" + "?subject="
					+ getString(R.string.report_subject) + "&body=";
			uriReportText = uriReportText.replace(" ", "%20");
			Uri uriReport = Uri.parse(uriReportText);

			emailReportIntent.setData(uriReport);

			startActivity(Intent.createChooser(emailReportIntent,
					getString(R.string.report_chooser)));
			return true;
		default:
			return false;
		}
	}

	public void showDialog(String message, boolean cancelable) {
		dialog = new ProgressDialog(this);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setCancelable(cancelable);
		dialog.setMessage(message);

		mHandler.post(new Runnable() {
			@Override
			public void run() {
				if (dialog != null) {
					dialog.show();
				}
			}
		});
	}

	public void dismissDialog() {
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				if (dialog != null) {
					dialog.dismiss();
					dialog = null;
				}
			}
		});
	}

	/**
	 * Custom dialog
	 * 
	 * @param numOfButtons
	 *            number of buttons on a dialog, 1 or 2
	 * @param showDistance
	 *            Does dialog show distance slider and advanced random button
	 * @param showCheckBox
	 *            Does dialog show login question CheckBox
	 * @param title
	 *            ResID of string title for a dialog
	 * @param message
	 *            ResID of string message to be displayed in dialog
	 * @param positiveBtnMsgResID
	 *            ResID of string on positive button
	 * @param positiveOnClick
	 *            onClickListener for a positive button click
	 * @param negativeBtnMsgResID
	 *            ResID of string on negative button
	 * @param negativeOnClick
	 *            onClickListener for a negative button click
	 */
	public void showCustomDialog(int numOfButtons, final boolean showDistance,
			final boolean showCheckBox, int title, int message,
			int positiveBtnMsgResID, final OnClickListener positiveOnClick,
			int negativeBtnMsgResID, final OnClickListener negativeOnClick) {
		showCustomDialog(numOfButtons, showDistance, showCheckBox, title,
				getString(message), positiveBtnMsgResID, positiveOnClick,
				negativeBtnMsgResID, negativeOnClick);
	}

	/**
	 * Custom dialog
	 * 
	 * @param numOfButtons
	 *            number of buttons on a dialog, 1 or 2
	 * @param showDistance
	 *            Does dialog show distance slider and advanced random button
	 * @param showCheckBox
	 *            Does dialog show login question CheckBox
	 * @param title
	 *            ResID of string title for a dialog
	 * @param message
	 *            String message to be displayed in dialog
	 * @param positiveBtnMsgResID
	 *            ResID of string on positive button
	 * @param positiveOnClick
	 *            onClickListener for a positive button click
	 * @param negativeBtnMsgResID
	 *            ResID of string on negative button
	 * @param negativeOnClick
	 *            onClickListener for a negative button click
	 */

	public void showCustomDialog(int numOfButtons, final boolean showDistance,
			final boolean showCheckBox, int title, String message,
			int positiveBtnMsgResID, final OnClickListener positiveOnClick,
			int negativeBtnMsgResID, final OnClickListener negativeOnClick) {
		if (mCustomDialog != null && mCustomDialog.isShowing()) {

			mCustomDialog.dismiss();
		}

		mCustomDialog = new Dialog(this,
				android.R.style.Theme_Translucent_NoTitleBar);

		mCustomDialog.setContentView(R.layout.confirm_random_dialog);

		TextView text = (TextView) mCustomDialog
				.findViewById(R.id.dialog_random_title);
		text.setText(title);
		text = (TextView) mCustomDialog.findViewById(R.id.dialog_random_text);
		text.setText(message);

		if (showDistance) {
			(mCustomDialog.findViewById(R.id.dialog_random_rlDistance))
					.setVisibility(View.VISIBLE);
			final SeekBar seekbar = (SeekBar) mCustomDialog
					.findViewById(R.id.dialog_random_sbDistance);
			final TextView tvMiles = (TextView) mCustomDialog
					.findViewById(R.id.dialog_random_tvDistanceRadiusValue);

			seekbar.setVisibility(View.VISIBLE);
			seekbar.setMax(RANDOM_SEARCH_RADIUS_VALUES.length - 1);
			seekbar.setProgress(seekbar.getMax());

			seekbar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

				@Override
				public void onStopTrackingTouch(SeekBar seekBar) {

				}

				@Override
				public void onStartTrackingTouch(SeekBar seekBar) {

				}

				@Override
				public void onProgressChanged(SeekBar seekBar, int progress,
						boolean fromUser) {
					String text;
					switch (RANDOM_SEARCH_RADIUS_VALUES[progress]) {
					case 0:
						text = getString(R.string.label_search_radius_entire_world);
						break;
					case 1:
						text = RANDOM_SEARCH_RADIUS_VALUES[progress] + " "
								+ getString(R.string.label_search_radius_mile);
						break;
					default:
						text = RANDOM_SEARCH_RADIUS_VALUES[progress] + " "
								+ getString(R.string.label_search_radius_miles);
					}
					tvMiles.setText(text);
				}
			});
			final Button btnAdvanced = (Button) mCustomDialog
					.findViewById(R.id.dialog_random_btnAdvanced);
			// This Button is a part of unfinished advanced Random search and is
			// hidden until a Middleware service is ready for it
			// btnAdvanced.setVisibility(View.VISIBLE);

			btnAdvanced.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					(mCustomDialog
							.findViewById(R.id.dialog_random_llSearchFiltersAdvanced))
							.setVisibility(View.VISIBLE);

					((TextView) mCustomDialog
							.findViewById(R.id.dialog_random_text))
							.setText(R.string.label_random_advanced_text);
					btnAdvanced.setVisibility(View.GONE);
					btnAdvanced.setEnabled(false);
				}
			});
			final Button btnMale = (Button) mCustomDialog
					.findViewById(R.id.dialog_random_btnMale);
			final Button btnFemale = (Button) mCustomDialog
					.findViewById(R.id.dialog_random_btnFemale);
			btnMale.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					btnMale.setEnabled(false);
					btnFemale.setEnabled(true);
				}
			});
			btnFemale.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					btnMale.setEnabled(true);
					btnFemale.setEnabled(false);
				}
			});
			if (RalleeApp.getInstance().getGender().equals("female")) {
				btnMale.performClick();
			} else {
				btnFemale.performClick();
			}
			// Spinner spinCountry = (Spinner) mCustomDialog
			// .findViewById(R.id.dialog_random_spinCountry);
			// ArrayAdapter<String> adapter = new ArrayAdapter<String>(
			// RalleeApp.getInstance(), R.layout.random_spinner_row,
			// R.id.spinner_text, RalleeApp.getInstance().getResources()
			// .getStringArray(R.array.country));
			// spinCountry.setAdapter(adapter);
		}

		CheckBox cbLogIn = (CheckBox) mCustomDialog
				.findViewById(R.id.dialog_random_cbLogIn);
		if (showCheckBox) {
			cbLogIn.setVisibility(View.VISIBLE);
			cbLogIn.setOnCheckedChangeListener(new OnCheckedChangeListener() {

				@Override
				public void onCheckedChanged(CompoundButton buttonView,
						boolean isChecked) {
					exitAndLogIn = isChecked;
				}
			});
			cbLogIn.setChecked(false);

		} else {
			cbLogIn.setVisibility(View.GONE);
		}

		Button button = (Button) mCustomDialog
				.findViewById(R.id.dialog_random_btnYes);

		button.setText(positiveBtnMsgResID);
		button.setVisibility(View.VISIBLE);
		button.setEnabled(true);
		button.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mCustomDialog != null && mCustomDialog.isShowing()) {
					mCustomDialog.dismiss();
				}
				if (positiveOnClick != null) {
					boolean doAclick = true;
					Bundle tag = new Bundle();
					if (showDistance) {
						int value = RANDOM_SEARCH_RADIUS_VALUES[((SeekBar) mCustomDialog
								.findViewById(R.id.dialog_random_sbDistance))
								.getProgress()];
						if (value > 0) {
							LocationManager locationManager = (LocationManager) getApplicationContext()
									.getSystemService(Context.LOCATION_SERVICE);
							String provider = locationManager.getBestProvider(
									new Criteria(), true);
							if (provider == null) {
								if (mCustomDialog != null
										&& mCustomDialog.isShowing()) {
									mCustomDialog.dismiss();
								}
								doAclick = false;
								OnClickListener onOpenSettings = new OnClickListener() {

									@Override
									public void onClick(View v) {
										Intent intent = new Intent(
												android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
										startActivity(intent);
									}
								};
								showCustomDialog(2,
										R.string.location_service_title,
										R.string.location_service_text,
										android.R.string.yes, onOpenSettings,
										android.R.string.no, null);
							}
						}
						String gender = "";
						String country = "";
						// if advanced random is shown
						if (!(mCustomDialog
								.findViewById(R.id.dialog_random_btnAdvanced))
								.isEnabled()) {
							if (((Button) mCustomDialog
									.findViewById(R.id.dialog_random_btnMale))
									.isEnabled()) {
								gender = "female";
							} else {
								gender = "male";
							}
							country = (String) ((Spinner) mCustomDialog
									.findViewById(R.id.dialog_random_spinCountry))
									.getSelectedItem();
						}
						tag.putInt("near", value);
						tag.putString("gender", gender);
						tag.putString("country", country);
					} else {
						tag.putInt("near", 0);
						tag.putString("gender", "male");
						v.setTag(tag);
					}
					v.setTag(tag);
					if (doAclick)
						positiveOnClick.onClick(v);
				}
			}
		});

		button = (Button) mCustomDialog.findViewById(R.id.dialog_random_btnNo);
		if (numOfButtons > 1) {
			button.setText(negativeBtnMsgResID);
			button.setVisibility(View.VISIBLE);
			button.setEnabled(true);
			button.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					if (mCustomDialog != null && mCustomDialog.isShowing()) {
						mCustomDialog.dismiss();
					}
					if (negativeOnClick != null) {
						negativeOnClick.onClick(v);
					}
				}
			});
		} else {
			button.setVisibility(View.GONE);
		}

		mCustomDialog.show();

	}

	/**
	 * Custom dialog
	 * 
	 * @param numOfButtons
	 *            number of buttons on a dialog, 1 or 2
	 * @param title
	 *            ResID of string title for a dialog
	 * @param message
	 *            ResID of string message to be displayed in dialog
	 * @param positiveBtnMsgResID
	 *            ResID of string on positive button
	 * @param positiveOnClick
	 *            onClickListener for a positive button click
	 * @param negativeBtnMsgResID
	 *            ResID of string on negative button
	 * @param negativeOnClick
	 *            onClickListener for a negative button click
	 */
	public void showCustomDialog(int numOfButtons, int title, int message,
			int positiveBtnMsgResID, final OnClickListener positiveOnClick,
			int negativeBtnMsgResID, final OnClickListener negativeOnClick) {
		showCustomDialog(numOfButtons, false, false, title, getString(message),
				positiveBtnMsgResID, positiveOnClick, negativeBtnMsgResID,
				negativeOnClick);
	}

	/**
	 * Custom dialog
	 * 
	 * @param numOfButtons
	 *            number of buttons on a dialog, 1 or 2
	 * @param title
	 *            ResID of string title for a dialog
	 * @param message
	 *            String message to be displayed in dialog
	 * @param positiveBtnMsgResID
	 *            ResID of string on positive button
	 * @param positiveOnClick
	 *            onClickListener for a positive button click
	 * @param negativeBtnMsgResID
	 *            ResID of string on negative button
	 * @param negativeOnClick
	 *            onClickListener for a negative button click
	 */
	public void showCustomDialog(int numOfButtons, int title, String message,
			int positiveBtnMsgResID, final OnClickListener positiveOnClick,
			int negativeBtnMsgResID, final OnClickListener negativeOnClick) {
		showCustomDialog(numOfButtons, false, false, title, message,
				positiveBtnMsgResID, positiveOnClick, negativeBtnMsgResID,
				negativeOnClick);
	}

	/**
	 * Random continuous 3 button dialog
	 * 
	 * @param toCall
	 *            true - it is a call dialog; false - progress loading dialog
	 * @param onClickCall
	 *            performed when call button is clicked
	 * @param randomUser
	 *            user profile
	 */
	public void showCustomDialogRandomContinuous(boolean toCall,
			final OnClickListener onClickCall, RandomUser randomUser) {
		if (mRandomCallDialog == null) {
			mRandomCallDialog = new Dialog(this,
					android.R.style.Theme_Translucent_NoTitleBar);

			mRandomCallDialog.setContentView(R.layout.random_call_dialog1);
		}
		TextView tvName = (TextView) mRandomCallDialog
				.findViewById(R.id.dialog_random_call_name);
		LinearLayout llPicAndName = (LinearLayout) mRandomCallDialog
				.findViewById(R.id.dialog_random_call_ll_pic_name_container);
//		TextView tvText = (TextView) mRandomCallDialog
//				.findViewById(R.id.dialog_random_call_profile_text);

		TextView tvSearch = (TextView) mRandomCallDialog
				.findViewById(R.id.dialog_random_call_searching);
		ProgressBar pbSearch = (ProgressBar) mRandomCallDialog
				.findViewById(R.id.dialog_random_call_progress);

		ImageView btnCancel = (ImageView) mRandomCallDialog
				.findViewById(R.id.dialog_random_call_btnCancel);
		ImageView btnNext = (ImageView) mRandomCallDialog
				.findViewById(R.id.dialog_random_call_btnNext);
		ImageView btnPrev = (ImageView) mRandomCallDialog
				.findViewById(R.id.dialog_random_call_btnPrev);
		btnCall = (ImageView) mRandomCallDialog
				.findViewById(R.id.dialog_random_call_btnCall);

		mRandomCallDialog.setOnCancelListener(new OnCancelListener() {

			@Override
			public void onCancel(DialogInterface dialog) {
				mMwCommLogic.unregisterGetRandomUserServiceReceiver();
			}
		});

		btnCancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mRandomCallDialog != null) {
					mRandomCallDialog.cancel();
				}
			}
		});

		if (toCall) {
			String calledUserUsername = randomUser.user_id;
			picUrl = randomUser.picture_url;
			
			

			pbSearch.setVisibility(View.INVISIBLE);
			tvSearch.setVisibility(View.INVISIBLE);

			llPicAndName.setVisibility(View.VISIBLE);

			tvName.setText(randomUser.user_name);
			tvName.setTag(calledUserUsername);
//			tvText.setText(String.format(
//					getString(R.string.dialog_random_call_profile_text),
//					randomUser.age, randomUser.gender, randomUser.country));
//			// next line is added to hide currently unavialable data about
//			// random user
//			tvText.setVisibility(View.GONE);
			btnCall.setVisibility(View.VISIBLE);
			btnNext.setEnabled(true);
			
			btnNext.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					//btnCall.setVisibility(View.INVISIBLE);
					goRandom(-1);
					
				}
			});
			if (randomContinuousListIndex <= 0) {
				btnPrev.setEnabled(false);
			} else {
				btnPrev.setEnabled(true);
			}
			btnPrev.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					//btnCall.setVisibility(View.INVISIBLE);
					goRandom(-2);
				}
			});

			btnCall.setEnabled(true);
			btnCall.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					if (mRandomCallDialog != null
							&& mRandomCallDialog.isShowing()) {
						mRandomCallDialog.dismiss();
					}

					onClickCall.onClick(v);
				}
			});

			ImageView userPic = (ImageView) mRandomCallDialog
					.findViewById(R.id.dialog_random_call_user_pic);
			
			class myRunnable1 implements Runnable {
				private String username;

				public myRunnable1(String id) {
					username = id;
				}

				@Override
				public void run() {
					String fbIdOfRandomUser = Utility.parseSNData(username)
							.getAsString(Utility.SOCIAL_NETWORK_ID);
					RRUser randomUser = FacebookUserDataLoader
							.getFbUserById(fbIdOfRandomUser);
					DisplayMetrics dm = new DisplayMetrics();
					getWindowManager().getDefaultDisplay().getMetrics(dm);
					int dm_width,dm_height;
					dm_width=dm.widthPixels;
					dm_height=dm.heightPixels;
					
					try {
							//img_value=new URL("http://graph.facebook.com/"+fbIdOfRandomUser+"/picture?width=550&height=900");
							img_value=new URL("http://graph.facebook.com/"+fbIdOfRandomUser+"/picture?width="+dm_width+"&height="+dm_height+"");
							picUrl=img_value.toString();
							RanpicUrl=img_value.toString();
							
//							Log.e("randomUser.picUrl", ".........."+randomUser.picUrl);
//							Log.e("randomUser.RanpicUrl", ".........."+RanpicUrl);
							//https://graph.facebook.com/shaverm/picture?width=dm_width&height=200 
//							img_value=null;
						
							
					} catch (MalformedURLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
//					if (randomUser != null) {
//						
//						String picUrlFB=null;
//						picUrlFB=picUrl;
//						picUrl=null;
//						Bundle b = new Bundle();
//						b.putString("id", username);
//						b.putString("pic", picUrlFB);
//						Message msg = new Message();
//						msg.setData(b);
//						loadRandomUserPic.dispatchMessage(msg);
//					}
				}
			}
			Thread t1 = new Thread(new myRunnable1(calledUserUsername));
			t1.run();

			if (picUrl != null && !picUrl.equals("") && !picUrl.equals("null")) {
				
				picUrl=RanpicUrl;
			
				Bitmap bmp = Utility.getBitmap(RanpicUrl, getApplicationContext());
				
				if (bmp == null) {
					bmp = BitmapFactory.decodeResource(RalleeApp.getInstance()
							.getResources(), R.drawable.fbdefault);
					
				}
				userPic.setImageBitmap(ImageHelper.getRoundedCornerBitmap(bmp,
						5));
				// userPic.setVisibility(View.VISIBLE);
				//bmp.recycle();

			} 
			else {

				userPic.setImageBitmap(ImageHelper.getRoundedCornerBitmap(
						BitmapFactory.decodeResource(getResources(),
								R.drawable.fbdefault), 5));
				// userPic.setVisibility(View.VISIBLE);
				// ((ImageView) mRandomCallDialog
				// .findViewById(R.id.dialog_random_user_pic_effect))
				// .setVisibility(View.VISIBLE);
				class myRunnable implements Runnable {
					private String username;

					public myRunnable(String id) {
						username = id;
					}

					@Override
					public void run() {
						String fbIdOfRandomUser = Utility.parseSNData(username)
								.getAsString(Utility.SOCIAL_NETWORK_ID);
						RRUser randomUser = FacebookUserDataLoader
								.getFbUserById(fbIdOfRandomUser);
						DisplayMetrics dm = new DisplayMetrics();
						getWindowManager().getDefaultDisplay().getMetrics(dm);
						int dm_width,dm_height;
						dm_width=dm.widthPixels;
						dm_height=dm.heightPixels;
						
						try {
								//img_value=new URL("http://graph.facebook.com/"+fbIdOfRandomUser+"/picture?width=550&height=900");
								img_value=new URL("http://graph.facebook.com/"+fbIdOfRandomUser+"/picture?width="+dm_width+"&height="+dm_height+"");
								picUrl=img_value.toString();
								RanpicUrl=img_value.toString();
//								Log.e("RanpicUrl", ".........."+RanpicUrl);
								//https://graph.facebook.com/shaverm/picture?width=dm_width&height=200 
//								img_value=null;
							
								
						} catch (MalformedURLException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						if (randomUser != null) {
//							System.out.println("test");
							String picUrlFB =RanpicUrl ;
//							String picUrlFB = picUrl;
							Bundle b = new Bundle();
							b.putString("id", username);
							b.putString("pic", picUrlFB);
							Message msg = new Message();
							msg.setData(b);
							loadRandomUserPic.dispatchMessage(msg);
							
						}
					}
				}
				Thread t = new Thread(new myRunnable(calledUserUsername));
				t.run();
			}
			

		} else {
			pbSearch.setVisibility(View.VISIBLE);
			tvSearch.setVisibility(View.VISIBLE);

			llPicAndName.setVisibility(View.INVISIBLE);

			tvName.setTag(null);

			tvName.setText("");
			//tvText.setText("");
			
			btnNext.setEnabled(false);
			btnNext.setOnClickListener(null);
			
			btnCall.setVisibility(View.INVISIBLE);
			
			btnCall.setEnabled(false);
			btnCall.setOnClickListener(null);
			

			btnPrev.setEnabled(false);
			btnPrev.setOnClickListener(null);

		}

		mRandomCallDialog.show();
	}

	public void showDialogIncomingRandom() {
		if (mCustomDialog != null && mCustomDialog.isShowing()) {

			mCustomDialog.dismiss();

		}

		mCustomDialog = new Dialog(this,
				android.R.style.Theme_Translucent_NoTitleBar);

		mCustomDialog.setContentView(R.layout.confirm_random_dialog1);
		btnAcceptCall = (ImageButton) mCustomDialog
				.findViewById(R.id.imgbtnAcceptCall1);
		btnRejectCall = (ImageButton) mCustomDialog
				.findViewById(R.id.imgbtnRejectCall1);
		TextView text = (TextView) mCustomDialog
				.findViewById(R.id.dialog_random_title);
		text.setText(R.string.random_calls_title);
		text = (TextView) mCustomDialog.findViewById(R.id.dialog_random_text);
		text.setText(getString(R.string.label_incoming_random_call_msg)
				+ Utility.senderName);

		CheckBox cbLogIn = (CheckBox) mCustomDialog
				.findViewById(R.id.dialog_random_cbLogIn);
		cbLogIn.setVisibility(View.GONE);
		//
		// Button button = (Button) mCustomDialog
		// .findViewById(R.id.dialog_random_btnYes);

		// button.setText(R.string.label_ok);
		// button.setVisibility(View.INVISIBLE);
		// button.setEnabled(true);
		// button.setOnClickListener(new View.OnClickListener() {
		// @Override
		// public void onClick(View v) {
		// if (mCustomDialog != null && mCustomDialog.isShowing()) {
		// mCustomDialog.dismiss();
		// }
		// }
		// });

		btnAcceptCall.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (mCustomDialog != null && mCustomDialog.isShowing()) {
					mCustomDialog.dismiss();
					acceptCall();
				}
			}
		});
		//
		btnRejectCall.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mCustomDialog != null && mCustomDialog.isShowing()) {
					mCustomDialog.dismiss();
					rejectCall();
				}

			}

		});
		// Button button = (Button)
		// mCustomDialog.findViewById(R.id.dialog_random_btnNo);
		// button.setVisibility(View.GONE);

		((RelativeLayout) mCustomDialog
				.findViewById(R.id.dialog_random_user_pic_container))
				.setVisibility(View.VISIBLE);
		((ProgressBar) mCustomDialog.findViewById(R.id.dialog_random_progress))
				.setVisibility(View.GONE);
		ImageView userPic = (ImageView) mCustomDialog
				.findViewById(R.id.dialog_random_user_pic);

		if (Utility.senderPicUrl != null && !Utility.senderPicUrl.equals("")) {
			userPic.setImageBitmap(ImageHelper.getRoundedCornerBitmap(Utility
					.getBitmap(Utility.senderPicUrl, getApplicationContext()),
					5));
			userPic.setVisibility(View.VISIBLE);
			((ImageView) mCustomDialog
					.findViewById(R.id.dialog_random_user_pic_effect))
					.setVisibility(View.VISIBLE);
		} else {
			userPic.setVisibility(View.INVISIBLE);
			((ImageView) mCustomDialog
					.findViewById(R.id.dialog_random_user_pic_effect))
					.setVisibility(View.INVISIBLE);
		}

		mCustomDialog.show();

	}

	private class ConnectionServiceObserver extends BaseServiceObserver {
		@Override
		public void onConnectionStateChanged(final int state)
				throws RemoteException {
			checkConnectionState();
		}
	}

	private final boolean checkConnectionState() {
		switch (mService.getConnectionState()) {
		case RadioRuntService.CONNECTION_STATE_CONNECTING:
			Globals.logDebug(this, "RadioRuntService.STATE_CONNECTING");
			return true;
		case RadioRuntService.CONNECTION_STATE_SYNCHRONIZING:
			Globals.logInfo(this, "Synchronizing");
			return true;
		case RadioRuntService.CONNECTION_STATE_CONNECTED:
			// fbLoginButton.setVisibility(View.INVISIBLE);
			unregisterConnectionReceiver();
			Globals.logDebug(this, "RadioRuntService.STATE_CONNECTED 1");
			return true;
		case RadioRuntService.CONNECTION_STATE_DISCONNECTED:
			Globals.logDebug(this, "RadioRuntService.STATE_DISCONNECTED 1");
			// TODO: Error message checks.
			// This can be reached if the user leaves ServerList after clicking
			// server but before the connection intent reaches the service.
			// In this case the service connects and can be disconnected before
			// the connection state is checked again.
			break;
		case RadioRuntService.CONNECTION_STATE_RECONNECTING:
			Globals.logDebug(this, "RadioRuntService.STATE_RECONNECTING");
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
		Globals.logDebug(this, "HOME CREATE CONNECTION OBSERVER");
		mServiceObserver = new ConnectionServiceObserver();

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
		}

		mServiceObserver = null;
	}

	private void registerBroadcastReceivers() // Ovo ide u OnResume, dok je u
												// 474 liniji koda deregistrovan
	{
		// Broadcast
		userFbReceiver = new UserFBFriendsReceiver();

		IntentFilter filter = new IntentFilter(
				UserFBFriendsReceiver.ACTION_DONE);
		filter.addCategory(Intent.CATEGORY_DEFAULT);

		registerReceiver(userFbReceiver, filter);

		IntentFilter failedFilter = new IntentFilter(
				UserFBFriendsReceiver.ACTION_IS_NOT_CHANGED);
		failedFilter.addCategory(Intent.CATEGORY_DEFAULT);

		registerReceiver(userFbReceiver, failedFilter);

		IntentFilter nofFilter = new IntentFilter(
				UserFBFriendsReceiver.ACTION_NUMBER_OF_FRIENDS);
		failedFilter.addCategory(Intent.CATEGORY_DEFAULT);

		registerReceiver(userFbReceiver, nofFilter);

		IntentFilter fsFilter = new IntentFilter(
				UserFBFriendsReceiver.ACTION_PART_OF_FRIENDS_SAVED);
		failedFilter.addCategory(Intent.CATEGORY_DEFAULT);

		registerReceiver(userFbReceiver, fsFilter);

		groupsFbReceiver = new FBGroupsReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				if (mService != null) {
//					Log.i("filtering", "OnReceive UserFBFriendsReceiver");
					mService.sendListsForFiltering();
				} else {
//					Log.i("filtering", "mService IS NULL");
				}
			}
		};

		IntentFilter fbgdFilter = new IntentFilter(FBGroupsReceiver.ACTION_DONE);
		failedFilter.addCategory(Intent.CATEGORY_DEFAULT);

		registerReceiver(groupsFbReceiver, fbgdFilter);

		mCallHistoryReceiver = new CallHistoryReceiver(HomeActivity.this);
	}

	public class UserFBFriendsReceiver extends BroadcastReceiver {
		public static final String ACTION_DONE = "com.radiorunt.fbFriendsLoad.done";
		public static final String ACTION_IS_NOT_CHANGED = "com.radiorunt.fbFriendsLoad.notChanged";
		public static final String ACTION_NUMBER_OF_FRIENDS = "com.radiorunt.fbFriendsLoad.numOfFriends";
		public static final String ACTION_PART_OF_FRIENDS_SAVED = "com.radiorunt.fbFriendsLoad.portOfFriendsSaved";
		public static final String NUM_OF_FRIENDS = "numOfFriendsToLoad";

		public void onReceive(Context ctx, Intent intent) {

			String action = intent.getAction();
			if (action.equals(ACTION_DONE)) {

				// send new list for filtering
				if (mService != null) {
					Globals.logDebug(this,
							"filtering OnReceive UserFBFriendsReceiver");
					mService.sendListsForFiltering();
				} else {
					Globals.logDebug(this, "filtering mService IS NULL");
				}

				// try {
				// // if (inviteFriendsDialogFragment != null
				// // && inviteFriendsDialogFragment.isVisible()) {
				// // inviteFriendsDialogFragment.loadingIsFinished();
				// // }
				// loadPeopleFromDB();
				// if (mService != null) {
				// countFriendsOnRR();
				// }
				// } catch (Exception e) {
				// e.printStackTrace();
				// }
			} else if (action.equals(ACTION_NUMBER_OF_FRIENDS)) {
				int numberOfFriendsToLoad = intent.getIntExtra(NUM_OF_FRIENDS,
						0);
				// if (inviteFriendsDialogFragment != null
				// && inviteFriendsDialogFragment.isVisible()) {
				// inviteFriendsDialogFragment
				// .setFriendsToLoad(numberOfFriendsToLoad);
				// }
			} else if (action.equals(ACTION_PART_OF_FRIENDS_SAVED)) {
				// if (inviteFriendsDialogFragment != null
				// && inviteFriendsDialogFragment.isVisible()) {
				// inviteFriendsDialogFragment
				// .freindSaved(RequestFBData.NUMBER_TO_NOTIFY);
				// loadPeopleFromDB();
				// if (mService != null) {
				// countFriendsOnRR();
				// }
				// }
			}
		}
	}

	public void showInviteFriendsDialog() {
		// mStackLevel++;

		// DialogFragment.show() will take care of adding the fragment
		// in a transaction. We also want to remove any currently showing
		// dialog, so make our own transaction and take care of that here.
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		Fragment prev = getSupportFragmentManager().findFragmentByTag("dialog");
		if (prev != null) {
			ft.remove(prev);
		}
		ft.addToBackStack(null);

		// Create and show the dialog.
		inviteFriendsDialogFragment = InviteToApplicationDialogFragment
				.newInstance(getString(R.string.label_invite_friends), this);
		inviteFriendsDialogFragment.show(ft, "dialog");
	}

	public RadioRuntService getRRService() {
		return mService;
	}

	void switchCallState(int toState) {
		Globals.logDebug(this, "callState switchCallState: from:"
				+ CallState.sCurrent + " to:" + toState);
		if (CallState.sCurrent == toState) {
			return;
		}

		if (mCCFrag != null) {
			FragmentTransaction ft = getSupportFragmentManager()
					.beginTransaction();
			if (mFDFrag != null && !mFDFrag.isHidden()) {
				ft.hide(mFDFrag);
			}

			if (mCCFrag.isHidden()) {
				ft.show(mCCFrag);
			}
			ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
			// ft.commit();
			ft.commitAllowingStateLoss();

			mCCFrag.switchCallState(toState);
			Globals.logDebug(this, "callState HomeActivity frag is not null");
		} else {
			Globals.logDebug(this, "callState HomeActivity frag is null");
			Bundle arguments = new Bundle();
			arguments.putInt("startWithState", toState);
			startFragment(CallCantrolsFragment.FRAGMENT_TAG, null, arguments);
		}
	}

	public void googleTracker(String arg0, String arg1, String arg2, long arg3) {
		EasyTracker.getTracker().trackEvent(arg0, arg1, arg2, arg3);
	}

	/**
	 * Set a screen that is opened
	 * 
	 * @param screen
	 *            Name of screen that is shown
	 */

	public void setScreenAnalytics(String screen) {
		// EasyTracker.getInstance().setContext(RalleeApp.getInstance());

		EasyTracker.getTracker().trackView(screen);
	}

	public View.OnTouchListener getFlipDetector() {
		return new OnTouchListener() {

			private GestureDetector gestureDetector = new GestureDetector(
					new FlipGestureDetector());

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return gestureDetector.onTouchEvent(event);
			}
		};
	}

	class LocationHandler extends Handler {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			if (msg.what == RRLocationManager.MSG_LOCATION_AQUIRED) {
				try {
					location = locationManager.getLastKnownLocation();

					if (RalleeApp.getInstance().getRalleeUID()
							.equals("ralleeUID")) {
						return;
					}

					String jsonString = "";
					try {
						JSONObject json = new JSONObject();
						json.put("user_id", RalleeApp.getInstance()
								.getRalleeUID());
						json.put("lat", location.getLatitude());
						json.put("lng", location.getLongitude());
						// JSONArray jsonA = new JSONArray();
						// jsonA.put(json);
						jsonString = json.toString();
					} catch (JSONException e) {
						e.printStackTrace();
					}
					RRServerProxyHelper.startSetUserLocationService(
							getApplicationContext(), jsonString);

					Globals.logDebug(this,
							"RRLocation coordinates: " + location.getLatitude()
									+ " , " + location.getLongitude());
					geofinder.geocode(HomeActivity.this,
							location.getLatitude(), location.getLongitude());
					// FriendsDockFragment fdFrag = (FriendsDockFragment)
					// getSupportFragmentManager()
					// .findFragmentByTag(
					// FriendsDockFragment.FRAGMENT_TAG);
					// if (fdFrag != null) {
					// fdFrag.notifyFriendsAdapterDataSetChange();
					// }
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else if (msg.what == RRLocationManager.MSG_NO_PROVIDER_AVAILABLE) {
				Toast.makeText(
						HomeActivity.this,
						"Please enable GPS or allow locating by networks in phone settings",
						3000).show();
			} else if (msg.what == RRLocationManager.MSG_ENABLE_GPS) {
				Toast.makeText(HomeActivity.this,
						"Enable GPS to get fine location.", 3000).show();
			}
		}

	}

	class FlipGestureDetector extends SimpleOnGestureListener {

		private static final int SWIPE_MIN_DISTANCE = 170;
		private static final int SWIPE_MAX_OFF_PATH = 150;
		private static final int SWIPE_THRESHOLD_VELOCITY = 200;

		public static final int SWIPE_MODE_DISABLED = -1;
		public static final int SWIPE_MODE_MAIN_PANEL = 0;
		public static final int SWIPE_MODE_PEOPLE = 1;
		public static final int SWIPE_MODE_RIGHT_PANEL = 2;

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {
			try {
				// if (swipe_mode==SWIPE_MODE_DISABLED){
				// return false;
				// }
				//
				if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH) {
					return false;
				}

				if (swipeMode == SWIPE_MODE_PEOPLE
						|| swipeMode == SWIPE_MODE_RIGHT_PANEL) {
					mHelper.goToMainPanel();
					return false;
				}

				if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE
						&& Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
					// right to left swipe
					if (swipeMode == SWIPE_MODE_MAIN_PANEL) {
						swipeMode = SWIPE_MODE_DISABLED;
						mHelper.goToAlerts();
					}

				} else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE
						&& Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
					// left to right swipe
					if (swipeMode == SWIPE_MODE_MAIN_PANEL) {
						swipeMode = SWIPE_MODE_DISABLED;
						mHelper.goToPeople();
					}

				}
			} catch (Exception e) {
				// nothing
			}
			return false;
		}

		@Override
		public boolean onDown(MotionEvent e) {
			return (swipeMode != SWIPE_MODE_DISABLED);// !(test==SWIPE_MODE_PEOPLE||test==SWIPE_MODE_RIGHT_PANEL);

		}

		@Override
		public boolean onSingleTapUp(MotionEvent e) {
			if (swipeMode == SWIPE_MODE_PEOPLE
					|| swipeMode == SWIPE_MODE_RIGHT_PANEL) {
				return this.onFling(e, e, 0, 0);
			} else
				return super.onSingleTapUp(e);
		}

	}

	/**
	 * Method for loging crashes and collecting logs
	 */
	private void setCrittercismForLogging() {
		JSONObject crittercismConfig = new JSONObject();
		String myCustomVersionName = RalleeApp.getInstance().getRalleeRelease();
		try {
			crittercismConfig.put("delaySendingAppLoad", true); // send app load
																// data with
																// Crittercism.sendAppLoadData()
			crittercismConfig.put("customVersionName", myCustomVersionName);
			crittercismConfig.put("shouldCollectLogcat", true); // send logcat
																// data for
																// devices with
																// API Level 16
																// and higher
		} catch (JSONException je) {
		}
		boolean didCrashOnLastAppLoad = Crittercism.init(
				getApplicationContext(), "507d40de01ed85267d000007",
				crittercismConfig);

		Crittercism.setUsername(RalleeApp.getInstance().getFullName());
	}

	QuickAction quickActionUser;
	private NotificationFragment notFrag;

	/**
	 * Create quick actions for click on the user
	 */
	private void startClickUser() {

		ActionItem talk = new ActionItem(TALK, getString(R.string.label_talk),
				getResources().getDrawable(
						R.drawable.main_ui_talk_button_normal_small));
		// ActionItem reportUser = new ActionItem(REPORT_USER,
		// getString(R.string.label_report_user),
		// getResources().getDrawable(R.drawable.report_user));
		ActionItem clearMissedCall = new ActionItem(CLEAR_MISSED_CALL,
				getString(R.string.label_clear), getResources().getDrawable(
						R.drawable.cancel_call));

		// create QuickAction. Use QuickAction.VERTICAL or
		// QuickAction.HORIZONTAL param to define layout
		// orientation
		quickActionUser = new QuickAction(this, QuickAction.VERTICAL);

		quickActionUser.addActionItem(talk);
		quickActionUser.addActionItem(clearMissedCall);
		// Set listener for action item clicked
		quickActionUser
				.setOnActionItemClickListener(new QuickAction.OnActionItemClickListener() {
					@Override
					public void onItemClick(QuickAction source, int pos,
							int actionId) {
						Long timestamp = QuickAction.TIMESTAMP;
						// here we can filter which action item was clicked with
						// pos or actionId parameter
						if (actionId == TALK) {

							if (CallState.sCurrent == CallState.NORMAL) {
								switch (QuickAction.TYPE) {
								case -1:
								case DbContentProvider.CALL_HISTORY_TYPE_PRIVATE_CALL_INCOMING:
								case DbContentProvider.CALL_HISTORY_TYPE_PRIVATE_CALL_OUTGOING:
								case QuickAction.CALL_UNSAVED_PRIVATE_GROUP:
									if (QuickAction.USERNAME != null
											&& ((QuickAction.FIRST_NAME != null) || (QuickAction.FIRST_NAME
													.equals("unknown")))) {
										if (mService == null) {
											Toast.makeText(
													HomeActivity.this,
													R.string.waiting_for_connection,
													500).show();
											return;
										}
										List<RRUser> users = mService
												.getUserList();
										boolean isOnline = false;
										for (RRUser usr : users) {
											if (usr.userName
													.equals(QuickAction.USERNAME)) {
												isOnline = true;
												break;
											}
										}

										if (isOnline) {
											callFriend(QuickAction.USERNAME,
													QuickAction.FIRST_NAME);
										} else {
											callOfflineFriend(
													QuickAction.USERNAME,
													QuickAction.FIRST_NAME);
										}
									}
									Uri uri_missedCall = Uri
											.withAppendedPath(
													DbContentProvider.CONTENT_URI_MISSED_CALL_TABLE,
													String.valueOf(timestamp));
									getApplicationContext()
											.getContentResolver().delete(
													uri_missedCall, null, null);
									break;
								case DbContentProvider.CALL_HISTORY_TYPE_PRIVATE_GROUP_CALL:
									enterPrivateGroupChannel(
											QuickAction.USERNAME,
											QuickAction.FIRST_NAME);
									break;
								case DbContentProvider.CALL_HISTORY_TYPE_PUBLIC_GROUP_CALL:
									enterPublicChannel(QuickAction.FIRST_NAME);
									break;
								case DbContentProvider.CALL_HISTORY_TYPE_FB_GROUP_CALL:
									enterFBGroupChannel(QuickAction.USERNAME);
									break;
								case DbContentProvider.CALL_HISTORY_TYPE_RANDOM_CALL:
									mHelper.goToMainPanel();
									if (mFDFrag != null) {
										mFDFrag.goToRandom();
									}
									break;
								}
							} else {
								Toast.makeText(RalleeApp.getInstance(),
										R.string.call_while_talking,
										Toast.LENGTH_SHORT).show();
							}
						} /*
						 * else if (actionId == REPORT_USER) {
						 * 
						 * String jsonString = ""; long date =
						 * Calendar.getInstance(TimeZone.getTimeZone("GMT"))
						 * .getTimeInMillis(); try { JSONObject json = new
						 * JSONObject(); json.put("reporting_user_id",
						 * RalleeApp.getInstance().getRalleeUID());
						 * json.put("user_id", QuickAction.USERNAME);
						 * json.put("date", date);
						 * 
						 * // JSONArray jsonA = new JSONArray(); //
						 * jsonA.put(json); jsonString = json.toString(); }
						 * catch (JSONException e) { e.printStackTrace(); } }
						 */
						else if (actionId == CLEAR_MISSED_CALL) {
							if (QuickAction.TYPE == QuickAction.TYPE_MISSED_CALL) {
								Uri uri_missedCall = Uri
										.parse(DbContentProvider.CONTENT_URI_MISSED_CALL_TABLE
												.toString() + "/" + timestamp);
								getApplicationContext().getContentResolver()
										.delete(uri_missedCall, null, null);
							} else {// Call history entry
								String toClearType = null;
								if (QuickAction.TYPE == QuickAction.CALL_UNSAVED_PRIVATE_GROUP) {
									toClearType = String
											.valueOf(DbContentProvider.CALL_HISTORY_TYPE_PRIVATE_GROUP_CALL);
								} else {
									toClearType = String
											.valueOf(QuickAction.TYPE);
								}
								RalleeApp
										.getInstance()
										.getContentResolver()
										.delete(DbContentProvider.CONTENT_URI_CALL_HISTORY_TABLE,
												DbContentProvider.CALL_HISTORY_COL_TYPE
														+ "=? AND "
														+ DbContentProvider.CALL_HISTORY_COL_TIMESTAMP
														+ "=?",
												new String[] {
														toClearType,
														String.valueOf(timestamp) });
							}
						}
					}
				});

		// set listnener for on dismiss event, this listener will be called only
		// if QuickAction dialog was dismissed
		// by clicking the area outside the dialog.
		quickActionUser
				.setOnDismissListener(new QuickAction.OnDismissListener() {
					@Override
					public void onDismiss() {
					}
				});
	}

	public void clickActionUser(int type, String username, String firstname,
			Long timestamp, View v) {
		quickActionUser.setType(type);
		quickActionUser.setUserName(username);
		quickActionUser.setFirstName(firstname);
		quickActionUser.setTimestamp(timestamp);
		quickActionUser.show(v);

	}

	public void showMessageNotification(String message, int duration) {
		showMessageNotification(message, duration,
				NotificationFragment.NOTIFICATION_TYPE_MESSAGE);
	}

	public void showMessageNotification(String message, int duration, int type) {
		if (notFrag == null) {
			notFrag = (NotificationFragment) getSupportFragmentManager()
					.findFragmentByTag(NotificationFragment.FRAGMENT_TAG);
		}
		if (notFrag != null) {
			Globals.logInfo(this, "NotificationFragment addMessage: " + message);
			notFrag.addMessage(message, duration);
		} else {
			notFrag = new NotificationFragment();
			Bundle bundle = new Bundle();
			bundle.putString(NotificationFragment.MESSAGE, message);
			bundle.putInt(NotificationFragment.DURATION_IN_SECONDS, duration);
			bundle.putInt(NotificationFragment.NOTIFICATION_TYPE, type);
			notFrag.setArguments(bundle);

			FragmentTransaction showNotificationTransaction = getSupportFragmentManager()
					.beginTransaction();
			showNotificationTransaction.replace(R.id.notificationSlot, notFrag,
					NotificationFragment.FRAGMENT_TAG);
			showNotificationTransaction.setCustomAnimations(R.anim.slide_up_in,
					R.anim.slide_up_in);
			// showNotificationTransaction.commit();
			showNotificationTransaction.commitAllowingStateLoss();
			Globals.logInfo(this, "NotificationFragment new: " + message);
		}
	}

	public void showMessageNotificationUserIsOnline(RRUser user) {
		String userId = user.userName;
		Cursor c = getApplicationContext().getContentResolver().query(
				Uri.parse(DbContentProvider.CONTENT_URI_USER_TABLE + "/"
						+ userId), null, null, null, null);
		try {
			if (c != null && c.moveToFirst()) {
				String userFirstName = c.getString(c
						.getColumnIndex(DbContentProvider.USER_COL_NAME));
				if (userFirstName != null && !userFirstName.equals("")) {
					String message = userFirstName
							+ getString(R.string.just_came_online);
					showMessageNotification(message, 3);
				}
			}
		} finally {
			if (c != null && !c.isClosed()) {
				c.close();
			}
		}

	}

	public void showMessageNotificationUserJoinedGroup(RRUser user) {
		String userId = user.userName;
		Cursor c = getApplicationContext().getContentResolver().query(
				Uri.parse(DbContentProvider.CONTENT_URI_USER_TABLE + "/"
						+ userId), null, null, null, null);
		try {
			if (c != null && c.moveToFirst()) {
				String userFirstName = c.getString(c
						.getColumnIndex(DbContentProvider.USER_COL_NAME));
				if (userFirstName != null && !userFirstName.equals("")) {
					String message = userFirstName
							+ getString(R.string.has_joined_group);
					showMessageNotification(message, 3);
				}
			}
		} finally {
			if (c != null && !c.isClosed()) {
				c.close();
			}
		}

	}

	public void showMessageNotificationIncomingCall(String userName) {

		String userId = userName;
		Cursor c = getApplicationContext().getContentResolver().query(
				Uri.parse(DbContentProvider.CONTENT_URI_USER_TABLE + "/"
						+ userId), null, null, null, null);
		try {
			if (c != null && c.moveToFirst()) {
				String userFirstName = c.getString(c
						.getColumnIndex(DbContentProvider.USER_COL_NAME));
				if (userFirstName != null && !userFirstName.equals("")) {
					String message = getString(R.string.incoming_call)
							+ userFirstName;
					showMessageNotification(
							message,
							30,
							NotificationFragment.NOTIFICATION_TYPE_INCOMING_CALL);
				}
			}
		} finally {
			if (c != null && !c.isClosed()) {
				c.close();
			}
		}

	}

	public void deinitializeNotificationFrag() {
		notFrag = null;
	}

	public void closeFragmentDueToIncomingCall() {
		PeopleFragment pFrag = (PeopleFragment) getSupportFragmentManager()
				.findFragmentByTag(PeopleFragment.FRAGMENT_TAG);
		if (pFrag != null) {
			mHelper.goToMainPanel();
		}

		RightSidePanelFragment rsFrag = (RightSidePanelFragment) getSupportFragmentManager()
				.findFragmentByTag(RightSidePanelFragment.FRAGMENT_TAG);
		if (rsFrag != null) {
			mHelper.goToMainPanel();
		}

		PrivateGroupDetailsFragment privgdFrag = (PrivateGroupDetailsFragment) getSupportFragmentManager()
				.findFragmentByTag(PrivateGroupDetailsFragment.FRAGMENT_TAG);
		if (privgdFrag != null) {
			getSupportFragmentManager().popBackStackImmediate();
		}

		PublicGroupDetailsFragment pubgdFrag = (PublicGroupDetailsFragment) getSupportFragmentManager()
				.findFragmentByTag(PublicGroupDetailsFragment.FRAGMENT_TAG);
		if (pubgdFrag != null) {
			getSupportFragmentManager().popBackStackImmediate();
		}

		FacebookGroupDetailsFragment fbgdFrag = (FacebookGroupDetailsFragment) getSupportFragmentManager()
				.findFragmentByTag(FacebookGroupDetailsFragment.FRAGMENT_TAG);
		if (fbgdFrag != null) {
			getSupportFragmentManager().popBackStackImmediate();
		}

		GroupsFragment gFrag = (GroupsFragment) getSupportFragmentManager()
				.findFragmentByTag(GroupsFragment.FRAGMENT_TAG);
		if (gFrag != null) {
			getSupportFragmentManager().popBackStackImmediate();
		}
	}

	public void acceptCall() {
		if (mCCFrag != null) {
			mCCFrag.acceptCall();

		}
	}

	public void rejectCall() {
		if (mCCFrag != null) {
			mCCFrag.rejectCall();
		}
	}

	public void silent() {
		if (mCCFrag != null) {
			mCCFrag.silent();
		}
	}

	public void startFragment(String fragmentTag, Bundle savedInstanceState,
			Bundle arguments) {

		if (fragmentTag.equals(CallCantrolsFragment.FRAGMENT_TAG)) {
			if (mCCFrag == null) {
				mCCFrag = (CallCantrolsFragment) getSupportFragmentManager()
						.findFragmentByTag(CallCantrolsFragment.FRAGMENT_TAG);
			}

			if (savedInstanceState != null && mCCFrag != null) {

				FragmentTransaction ft = getSupportFragmentManager()
						.beginTransaction();
				if (mFDFrag != null) {
					ft.hide(mFDFrag);
				}
				ft.show(mCCFrag);
				ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
				// ft.commit();
				ft.commitAllowingStateLoss();
			} else {
				mCCFrag = new CallCantrolsFragment();
				mCCFrag.setRetainInstance(true);
				mCCFrag.setArguments(arguments);

				FragmentTransaction ft = getSupportFragmentManager()
						.beginTransaction();
				if (mFDFrag != null) {
					ft.hide(mFDFrag);
				}
				ft.add(R.id.bottomPanelSlot, mCCFrag,
						CallCantrolsFragment.FRAGMENT_TAG);
				ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
				// ft.commit();
				ft.commitAllowingStateLoss();
			}
		} else if (fragmentTag.equals(FriendsDockFragment.FRAGMENT_TAG)) {
			if (mFDFrag == null) {
				mFDFrag = (FriendsDockFragment) getSupportFragmentManager()
						.findFragmentByTag(FriendsDockFragment.FRAGMENT_TAG);
			}

			if (savedInstanceState != null && mFDFrag != null) {
				FragmentTransaction ft = getSupportFragmentManager()
						.beginTransaction();
				if (mCCFrag != null) {
					ft.hide(mCCFrag);
				}
				ft.show(mFDFrag);
				ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
				// ft.commit();
				ft.commitAllowingStateLoss();
			} else {

				mFDFrag = new FriendsDockFragment();
				if (arguments != null) {
					mFDFrag.setArguments(arguments);
				}
				FragmentTransaction ft = getSupportFragmentManager()
						.beginTransaction();
				mFDFrag.setRetainInstance(true);
				if (mCCFrag != null) {
					ft.hide(mCCFrag);
				}
				ft.add(R.id.bottomPanelSlot, mFDFrag,
						FriendsDockFragment.FRAGMENT_TAG);
				ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
				// ft.commit();
				ft.commitAllowingStateLoss();
			}
		} else if (fragmentTag.equals(VoiceModePanelFragment.FRAGMENT_TAG)) {
			if (savedInstanceState != null) {
				mVMPFrag = (VoiceModePanelFragment) getSupportFragmentManager()
						.findFragmentByTag(VoiceModePanelFragment.FRAGMENT_TAG);
			} else {

				mVMPFrag = new VoiceModePanelFragment();
				mVMPFrag.setArguments(arguments);
				mVMPFrag.setRetainInstance(true);
				FragmentTransaction ft = getSupportFragmentManager()
						.beginTransaction();
				ft.replace(R.id.topPanelSlot, mVMPFrag,
						VoiceModePanelFragment.FRAGMENT_TAG);
				ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
				// ft.commit();
				ft.commitAllowingStateLoss();
			}
		}
	}

	private void checkIfDisconnectedFromRallee() {

		// RadioRuntService.DISCONNECT_FROM_RALLEE = settings.getExitCode(true);
		boolean exitCode = settings.getExitCode(true);

		// if (RadioRuntService.DISCONNECT_FROM_RALLEE) {
		if (exitCode) {
			RadioRuntService.DISCONNECT_FROM_RALLEE = exitCode;
			final Intent i = new Intent(getApplicationContext(),
					LogInActivity.class);
			i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(i);
			finish();
			return;
		}
	}

	@Override
	public void call(Session session, SessionState state, Exception exception) {
		// Toast.makeText(this, "Session state: " + state, 1000).show();
	}
	public int Width_metrics(){
		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay()
				.getMetrics(metrics);
//		Width_metrics=metrics.widthPixels;
//		Height_metrics=metrics.heightPixels;
		return metrics.widthPixels;

}
	public int Height_metrics(){
		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay()
				.getMetrics(metrics);
//		Width_metrics=metrics.widthPixels;
//		Height_metrics=metrics.heightPixels;
		return metrics.heightPixels;

}
}
