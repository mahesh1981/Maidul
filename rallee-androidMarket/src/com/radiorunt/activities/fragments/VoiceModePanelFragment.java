package com.radiorunt.activities.fragments;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.os.Vibrator;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.CheckBox;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.radiorunt.R;
import com.radiorunt.activities.DbContentProvider;
import com.radiorunt.activities.HomeActivity;
import com.radiorunt.businessobjects.RRUser;
import com.radiorunt.facebook.FacebookUserDataLoader;
import com.radiorunt.facebook.FriendsGetProfilePics;
import com.radiorunt.facebook.Utility;
import com.radiorunt.services.RadioRuntService;
import com.radiorunt.services.audio.AudioOutputHost;
import com.radiorunt.utilities.CallState;
import com.radiorunt.utilities.Globals;
import com.radiorunt.utilities.ImageHelper;
import com.radiorunt.utilities.RalleeApp;

public class VoiceModePanelFragment extends Fragment {

	public static final String FRAGMENT_TAG = "voiceModePanel";
	public static final String START_WITHOUT_LISTENERS = "startWithoutListeners";

	private static final int MSG_CLEAR_TALKING_USER_PIC = 0;

	// Views

	ImageButton imgBtnMuteSelf;
	ImageButton imgBtnDeafenSelf;
	ImageButton speak;
	ImageButton speakGlow;

	AlphaAnimation fadeOutAnimation;
	ImageView imgSpeakStatusLed;
	ImageView imgListenStatusLed;

	ImageButton btnEndCall;
	ImageButton btnAcceptCall;
	ImageButton btnRejectCall;
	ImageButton btnCancelCall;
	LinearLayout llIncomingCallBtnHolder;
	Chronometer chronometer;
	LinearLayout mainLayout;

	public CheckBox exitAndLogIn;
	LayoutInflater adbInflater;
	View checkboxLayout;

	TextView tvLCDInfo;
	ImageView phoneState;
	static long msgsTimestamp;
	Uri alert;
	private ImageView imgLogo;
	protected boolean hadsetOrLoudSpeaker = false;

	protected boolean someoneIsTalking = false;

	FriendsGetProfilePics talkingStatePictureModel;

	Handler mHandler;

	String lcdInfo;

	long chronometerBase = 0;
	boolean chronometerRunning = false;

	protected boolean createdWithouListeners = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Globals.logInfo(this, "onCreate");
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		Globals.logInfo(this, "onCreateView");
		View v = inflater.inflate(R.layout.voice_mode_panel, container, false);

		// int displayWidth = ((WindowManager) this.getActivity()
		// .getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay()
		// .getWidth();
		// int displayHeight = ((WindowManager) this.getActivity()
		// .getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay()
		// .getHeight();

		// v.setLayoutParams(new LayoutParams(displayWidth, displayHeight * 4 /
		// 5));
		v.setOnTouchListener(new View.OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getActionMasked() == MotionEvent.ACTION_DOWN
						&& getActivity() != null) {
					Toast.makeText(getActivity(),
							R.string.default_waiting_for_connection, 100)
							.show();
				}
				return true;
			}
		});
		return v;
	}

	@Override
	public void onStart() {
		super.onStart();
		Globals.logInfo(this, "onStart");

		// setRetainInstance(true);
		initControls();

		if (getArguments() != null) {
			createdWithouListeners = getArguments().getBoolean(
					START_WITHOUT_LISTENERS, false);
		}

		if (!createdWithouListeners) {
			initControlListeners();
		}

		Globals.logInfo(this, "onStart ends");
	}

	@Override
	public void onResume() {
		super.onResume();
		Globals.logInfo(this, "onResume");

		if (mHandler != null) {
			mHandler.removeMessages(MSG_CLEAR_TALKING_USER_PIC);
			mHandler.sendEmptyMessageDelayed(MSG_CLEAR_TALKING_USER_PIC, 3000);
		}

		if (getHomeActivity() != null) {
			getHomeActivity().setScreenAnalytics("MainScreen-VoiceMode");
		}

		Globals.logInfo(this, "onResume ends");
	};

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		Globals.logInfo(this, "onDestroyView");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Globals.logInfo(this, "onDestroy");
		createdWithouListeners = false;
	}

	public void initControls() {

		// mainLayout = (LinearLayout)
		// getView().findViewById(R.id.llVoiceModePanel);

		phoneState = (ImageView) getView().findViewById(
				R.id.main_ui_phone_state);
		phoneState
				.setImageResource(R.drawable.main_ui_lcd_panel_phone_inactive);
		phoneState.setTag(null);
		imgListenStatusLed = (ImageView) getView().findViewById(
				R.id.imgListenStatus);
		imgSpeakStatusLed = (ImageView) getView().findViewById(
				R.id.imgSpeakStatus);

		fadeOutAnimation = new AlphaAnimation(1.0f, 0.0f);
		fadeOutAnimation.setDuration(1000);
		fadeOutAnimation.setRepeatCount(Animation.INFINITE);
		fadeOutAnimation.setRepeatMode(Animation.REVERSE);

		Typeface sucabaFont = Typeface.createFromAsset(RalleeApp.getInstance()
				.getAssets(), "fonts/sucaba.ttf");
		// Typeface lcdPlainFont = Typeface.createFromAsset(getAssets(),
		// "fonts/lcdPlain.otf");

		chronometer = (Chronometer) getView().findViewById(R.id.chronometer);
		if (chronometerRunning) {
			chronometer.setBase(chronometerBase);
			chronometer.start();
		} else {
			chronometer.stop();
			chronometer.setBase(SystemClock.elapsedRealtime());
		}
		// chronometer.setText("0:00:00");

		Typeface tf = Typeface.createFromAsset(RalleeApp.getInstance()
				.getAssets(), "fonts/lcdPlain.otf");
		chronometer.setTypeface(tf);
		chronometer.setTextColor(getResources().getColor(R.color.light_blue));

		// chronometer.setTypeface(lcdPlainFont);

		imgLogo = (ImageView) getView().findViewById(R.id.imgRRLogo);

		speak = (ImageButton) getView().findViewById(
				R.id.imgbtnTalkHomeActivity);
		speakGlow = (ImageButton) getView().findViewById(
				R.id.imgbtnTalkHomeActivity);
		imgBtnMuteSelf = (ImageButton) getView().findViewById(
				R.id.imgBtnMuteSelfOnOffHomeActivity);
		imgBtnMuteSelf.setVisibility(View.INVISIBLE);
		imgBtnDeafenSelf = (ImageButton) getView().findViewById(
				R.id.imgBtnDeafenSelfOnOffHomeActivity);
		tvLCDInfo = (TextView) getView().findViewById(
				R.id.tvCallerNameHomeActivity);
		tvLCDInfo.setTypeface(sucabaFont);

		tvLCDInfo.setSelected(true);
		tvLCDInfo.setTextColor(getResources().getColor(R.color.light_blue));
		if (lcdInfo != null) {
			tvLCDInfo.setText(lcdInfo);
		}
		// TranslateAnimation circulateTranslation = new
		// TranslateAnimation(TranslateAnimation.RELATIVE_TO_PARENT, 1.0f,
		// TranslateAnimation.RELATIVE_TO_PARENT, - 1.0f,
		// TranslateAnimation.RELATIVE_TO_PARENT, 0,
		// TranslateAnimation.RELATIVE_TO_PARENT, 0);
		// circulateTranslation.setDuration(10000);
		// circulateTranslation.setRepeatCount(Animation.INFINITE);
		// tvCallerName.startAnimation(circulateTranslation);

		// imgBtnMuteSelf.setOnClickListener(muteSelfClickListener);
		// imgBtnDeafenSelf.setOnClickListener(handsetOrLaudSpeakerTouchListener);

		imgLogo.setVisibility(View.VISIBLE);

		// imgBtnPeople.setVisibility(View.INVISIBLE);
		// imgBtnVoicemail.setVisibility(View.INVISIBLE);
		// tbtnConvMode.setVisibility(View.VISIBLE);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		// first saving my state, so the bundle wont be empty.
		// http://code.google.com/p/android/issues/detail?id=19917
		outState.putString("WORKAROUND_FOR_BUG_19917_KEY",
				"WORKAROUND_FOR_BUG_19917_VALUE");

		if (chronometerRunning) {
			chronometerBase = chronometer.getBase();
		} else {
			chronometerBase = 0;
		}

		super.onSaveInstanceState(outState);

	}

	public void initControlListeners() {
		if (getHomeActivity() == null) {
			return;
		}

		createdWithouListeners = false;
		getView().setOnTouchListener(null);

		View.OnTouchListener gestureListener = getHomeActivity()
				.getFlipDetector();

		((LinearLayout) getView().findViewById(R.id.llVoiceModePanel))
				.setOnTouchListener(gestureListener);
		imgLogo.setOnTouchListener(gestureListener);

		mHandler = new Handler() {
			public void handleMessage(android.os.Message msg) {
				if (msg.what == MSG_CLEAR_TALKING_USER_PIC) {
					if (!someoneIsTalking) {
						phoneState.setTag(null);
						if (CallState.sCurrent == CallState.NORMAL) {
							updateFragment(R.id.main_ui_phone_state,
									R.drawable.main_ui_lcd_panel_phone_inactive);
						} else {
							updateFragment(R.id.main_ui_phone_state,
									R.drawable.main_ui_lcd_panel_phone_active);
						}
					}
				}
			};
		};

		imgBtnDeafenSelf.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// if(getRRService().getCurrentChannel().id != 0){
				// if(settings.getAudioStream() ==
				// AudioManager.STREAM_VOICE_CALL){
				// if (muteOn){
				// getRRService().muteSelf(true);
				// }else{
				// getRRService().muteSelf(false);
				// if(getRRService().isRecording() == false){
				// getRRService().setRecording(true);
				// }
				// }
				// }else{
				if (getRRService() != null) {
					if (getHomeActivity().deafOn) {
						getHomeActivity().deafOn = false;
						imgBtnDeafenSelf
								.setImageResource(R.drawable.main_ui_super_grill_mute_others);
						speak.setEnabled(true);
						getRRService().deafenSelf(false);
					} else {
						getHomeActivity().deafOn = true;
						imgBtnDeafenSelf
								.setImageResource(R.drawable.main_ui_super_grill_mute_others_on);
						speak.setEnabled(false);
						getRRService().deafenSelf(true);
					}
					// }
					// }
				}
			}
		});
		imgBtnMuteSelf.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (getRRService() != null) {
					if (hadsetOrLoudSpeaker) {
						// laudspeaker
						if (getRRService().audioOutputState()) {
							getRRService().stopAudioThreads();
						} else {
							return;
						}

						imgBtnMuteSelf
								.setImageResource(R.drawable.main_ui_super_grill_mute_me);
						getHomeActivity().settings
								.setAudioStreamLaudSpeaker(getHomeActivity());
						getRRService().startAudioThreads();
						getRRService().setRecording(false);

						// when is muted muteOn change state
						// if(muteOn){
						// if(deafOn){
						// speak.setEnabled(false);
						// }else{
						speak.setEnabled(true);
						// }
						speak.setOnTouchListener(speakButtonClickEvent);
						speak.setImageResource(R.drawable.main_ui_talk_button_normal);
						speak.clearAnimation();
						hadsetOrLoudSpeaker = false;
						if (getHomeActivity().isDialing) {
							getHomeActivity().dialTone(false);
							getHomeActivity().isDialing = getHomeActivity()
									.dialTone(true);
						}
						try {
							Thread.sleep(500);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					} else {
						// handset
						if (getRRService().audioOutputState()) {
							getRRService().stopAudioThreads();
						} else {
							return;
						}
						imgBtnMuteSelf
								.setImageResource(R.drawable.main_ui_super_grill_mute_me_on);
						getHomeActivity().settings
								.setAudioStreamHandset(getActivity());
						getRRService().startAudioThreads();
						if (getRRService().getCurrentChannel().id != 0) {
							if (getRRService().isRecording() == false) {
								getRRService().setRecording(true);
							}
						} else {
							getRRService().setRecording(false);
						}
						getRRService().setAudioVolume();
						speak.setEnabled(false);
						speak.setImageResource(R.drawable.main_ui_talk_button_down);

						Vibrator vb = (Vibrator) RalleeApp.getInstance()
								.getSystemService(Context.VIBRATOR_SERVICE);
						if (vb != null) {
							vb.vibrate(300);
						}
						hadsetOrLoudSpeaker = true;

						if (getHomeActivity().isDialing) {
							getHomeActivity().dialTone(false);
							getHomeActivity().isDialing = getHomeActivity()
									.dialTone(true);
						}
						try {
							Thread.sleep(500);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			}
		});

		if (getHomeActivity().settings.getAudioStream() == AudioManager.STREAM_MUSIC) {
			// laudspeaker
			hadsetOrLoudSpeaker = true;
			imgBtnMuteSelf
					.setImageResource(R.drawable.main_ui_super_grill_mute_me);
			speak.setOnTouchListener(speakButtonClickEvent);
			speak.setEnabled(true);
		} else {
			// handset
			imgBtnMuteSelf
					.setImageResource(R.drawable.main_ui_super_grill_mute_me_on);
			hadsetOrLoudSpeaker = false;
			getRRService().setAudioVolume();
			speak.setEnabled(false);
			speak.setImageResource(R.drawable.main_ui_talk_button_down);
		}

	}

	RadioRuntService getRRService() {
		if (getHomeActivity() == null) {
			return null;
		} else {
			return getHomeActivity().getRRService();
		}
	}

	// int getCurrentCallState() {
	// return ((HomeActivity) getActivity()).getCurrentCallState();
	// }

	private HomeActivity getHomeActivity() {
		return (HomeActivity) this.getActivity();

	}

	private final OnTouchListener speakButtonClickEvent = new OnTouchListener() {
		public boolean onTouch(final View v, MotionEvent e) {
			if (getHomeActivity() != null && !getHomeActivity().manualRecord
					&& e.getActionMasked() == MotionEvent.ACTION_DOWN) {
				getHomeActivity().manualRecord = true;
				getRRService().stopAudioThreads();
				getRRService().setRecording(getHomeActivity().manualRecord);
				((ImageButton) v)
						.setImageResource(R.drawable.main_ui_talk_button_down);
				((ImageButton) v).startAnimation(fadeOutAnimation);

				Vibrator vb = (Vibrator) RalleeApp.getInstance()
						.getSystemService(Context.VIBRATOR_SERVICE);
				if (vb != null) {
					vb.vibrate(100);
				}
				return true;
			} else if (getHomeActivity() != null
					&& e.getActionMasked() == MotionEvent.ACTION_UP) {
				getHomeActivity().manualRecord = false;
				getRRService().startAudioThreads();
				getRRService().setRecording(getHomeActivity().manualRecord);
				((ImageButton) v).clearAnimation();
				((ImageButton) v)
						.setImageResource(R.drawable.main_ui_talk_button_normal);
				return true;
			}
			return false;
		}
	};

	public void refreshUserTalkState(final RRUser user) {
		if (user == null) {
			return;
		}
		if (user.userName == null) {
			return;
		}
		if (user.userName.equals(RalleeApp.getInstance().getRalleeUID())) {
			if (user.talkingState == AudioOutputHost.STATE_TALKING) {
				imgSpeakStatusLed
						.setImageResource(R.drawable.main_ui_mic_led_on);
			} else {
				imgSpeakStatusLed.setImageResource(R.drawable.main_ui_mic_led);
			}
		} else {// if(user.userName.equals(Utility.senderFBId) || filteringOn
				// user.userName.equals(Utility.calledUserUsername) ){
			if (user.talkingState == AudioOutputHost.STATE_TALKING) {

				someoneIsTalking = true;

				// shows picture of user who is talking
				updateFragment(R.id.main_ui_phone_state, user);

				imgListenStatusLed
						.setImageResource(R.drawable.main_ui_speaker_led_on);
			} else {

				someoneIsTalking = false;

				if (phoneState.getTag() == null) {
					if (CallState.sCurrent == CallState.NORMAL) {
						updateFragment(R.id.main_ui_phone_state,
								R.drawable.main_ui_lcd_panel_phone_inactive);
					} else {
						updateFragment(R.id.main_ui_phone_state,
								R.drawable.main_ui_lcd_panel_phone_active);
					}
				}

				imgListenStatusLed
						.setImageResource(R.drawable.main_ui_speaker_led);
			}
		}

	}

	public void serviceConnected() {
		speak.setEnabled(true);
	}

	public void updateFragment(int viewId, String string) {
		switch (viewId) {
		case R.id.tvCallerNameHomeActivity:
			lcdInfo = string;
			tvLCDInfo.setText(string);
			break;
		case R.id.chronometer:
			chronometer.setText(string);
			break;
		}
	}

	public void updateFragment(int viewId, int intValue) {
		switch (viewId) {
		case R.id.main_ui_phone_state:
			phoneState.setImageResource(intValue);
			break;
		}

	}

	public void updateFragment(int viewId, Bitmap bitmap) {
		switch (viewId) {
		case R.id.main_ui_phone_state:
			phoneState.setImageBitmap(bitmap);
			break;
		}

	}

	public void updateFragment(int viewId, boolean boolValue) {
		switch (viewId) {
		case R.id.chronometer:
			chronometerRunning = boolValue;
			if (boolValue) {
				chronometer.start();
			} else {
				chronometer.stop();
			}
			break;
		}
	}

	public void updateFragment(int viewId, long longValue) {
		switch (viewId) {
		case R.id.chronometer:
			chronometer.setBase(longValue);
			break;
		}

	}

	public void updateFragment(int viewId, RRUser user) {
		switch (viewId) {
		case R.id.main_ui_phone_state:
			if (!user.userName.equals((String) phoneState.getTag())) {
//				Log.i(FRAGMENT_TAG, "User " + user.userName + " image is sat");
				phoneState.setTag(user.userName);
				phoneState.setImageBitmap(getUserBitmap(user));
				phoneState.setAdjustViewBounds(true);
			} else {
//				Log.i(FRAGMENT_TAG, "User " + user.userName
//						+ " image is the same");
			}

			mHandler.removeMessages(MSG_CLEAR_TALKING_USER_PIC);
			mHandler.sendEmptyMessageDelayed(MSG_CLEAR_TALKING_USER_PIC, 3000);

			break;
		}

	}

	/**
	 * @param rrUser
	 *            rrUser fetshed from server
	 * @return bitmap pic of that user
	 */
	private Bitmap getUserBitmap(final RRUser rrUser) {
		Bitmap bm = null;

		String id = rrUser.userName;
		String picture = "";

		Cursor c = null;
		try {
			c = RalleeApp
					.getInstance()
					.getContentResolver()
					.query(DbContentProvider.CONTENT_URI_USER_TABLE,
							new String[] { DbContentProvider.USER_COL_ID,
									DbContentProvider.USER_COL_PIC_URL },
							DbContentProvider.USER_COL_ID + " IN ('" + id
									+ "')", null, null);
			if (c != null && c.moveToFirst()) {
				picture = c.getString(c
						.getColumnIndex(DbContentProvider.USER_COL_PIC_URL));
			}

			if (picture.equals("")) {
				String fbIdOfUser = Utility.parseSNData(id).getAsString(
						Utility.SOCIAL_NETWORK_ID);
				RRUser usr = FacebookUserDataLoader.getFbUserById(fbIdOfUser);
				picture = usr.picUrl;
			}

			bm = ImageHelper.getRoundedCornerBitmap(
					Utility.getBitmap(picture, RalleeApp.getInstance()), 5);
			// bm = talkingStatePictureModel.getImage(id, picture, getActivity()
			// .getApplicationContext());
		} finally {
			if (c != null && !c.isClosed()) {
				c.close();
			}
		}

		return bm;
	}

}
