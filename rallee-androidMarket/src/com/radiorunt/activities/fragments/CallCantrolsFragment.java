package com.radiorunt.activities.fragments;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import com.radiorunt.R;
import com.radiorunt.activities.DbContentProvider;
import com.radiorunt.activities.HomeActivity;
import com.radiorunt.activities.LogInActivity;
import com.radiorunt.businessobjects.RRChannels;
import com.radiorunt.businessobjects.RRPushFilteringMessagePayload;
import com.radiorunt.businessobjects.RRPushMessagePayload;
import com.radiorunt.businessobjects.RRUser;
import com.radiorunt.facebook.Utility;
import com.radiorunt.services.RadioRuntService;
import com.radiorunt.utilities.CallHistoryReceiver;
import com.radiorunt.utilities.CallState;
import com.radiorunt.utilities.Globals;
import com.radiorunt.utilities.RalleeApp;

import android.app.Dialog;
import android.content.ContentValues;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

public class CallCantrolsFragment extends Fragment {

	public static final String FRAGMENT_TAG = "callCantrolsFragment";

	// public static final int DOCK_MODE_GALLERY = 0;
	public static final int DOCK_MODE_END_CALL = 1;
	public static final int DOCK_MODE_CANCEL_CALL = 2;
	public static final int DOCK_MODE_ACC_REJECT_CALL = 3;
	public static final int DOCK_MODE_LOADING_FRIENDS = 4;
	public static final int DOCK_MODE_IN_CALL = 5;
	public static int sDockMode = -1;

	private Object lockCurrentCallState = new Object();

	ImageButton btnEndCall;
	ImageButton btnEndCallSmall;
	ImageButton btnAcceptCall;
	ImageButton btnRejectCall;
	ImageButton btnCancelCall;
	ImageButton btnParticipants;
	ImageButton btnAddGroup;
	LinearLayout llIncomingCallBtnHolder;
	LinearLayout llInCallBtnHolder;
	ViewFlipper viewFlipper;
	Ringtone ringtone;
	private Uri alert;

	private int startWithState = -1;

	List<String> usersParticipated = new ArrayList<String>();

	private Dialog alertDialog;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
			startWithState = getArguments().getInt("startWithState", -1);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		Globals.logInfo(this, "onCreateView");
		View v = inflater.inflate(R.layout.call_controls, container, false);
		return v;
	}

	@Override
	public void onStart() {
		super.onStart();
		Globals.logInfo(this, "onStart");
		initControls();
		initControlListeners();

		// CallState.sCurrent = CallState.NORMAL;

		if (startWithState != -1) {
			switchCallState(startWithState);
			startWithState = -1;
			getArguments().putInt("startWithState", -1);
		}

		setDockMode(sDockMode);
	}

	@Override
	public void onResume() {
		super.onResume();
		Globals.logInfo(this, "onResume");
	}

	@Override
	public void onPause() {
		super.onPause();
		Globals.logInfo(this, "onPause");
		if (ringtone != null) {
			ringtone.stop();
		}
	}

	private void initControls() {
		btnEndCall = (ImageButton) getView().findViewById(
				R.id.imgbtnEndCallHomeActivity);
		btnAcceptCall = (ImageButton) getView().findViewById(
				R.id.imgbtnAcceptCall);
		btnCancelCall = (ImageButton) getView().findViewById(
				R.id.imgbtnCancelCallHomeActivity);
		btnRejectCall = (ImageButton) getView().findViewById(
				R.id.imgbtnRejectCall);
		llIncomingCallBtnHolder = (LinearLayout) getView().findViewById(
				R.id.llIncomingCallBtnHolder);
		llInCallBtnHolder = (LinearLayout) getView().findViewById(
				R.id.llIncallButtonHolder);
		btnEndCallSmall = (ImageButton) getView().findViewById(
				R.id.imgbtnEndCallSmall);
		btnParticipants = (ImageButton) getView().findViewById(
				R.id.imgbtnParticipants);
		btnAddGroup = (ImageButton) getView().findViewById(R.id.imgbtnAddUser);

		viewFlipper = (ViewFlipper) getView().findViewById(R.id.viewFlipper);

		alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
		ringtone = RingtoneManager.getRingtone(RalleeApp.getInstance(), alert);

	}

	private void initControlListeners() {
		btnEndCall.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				endCall();
			}
		});

		btnEndCallSmall.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				endCall();
			}
		});

		btnParticipants.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// Toast.makeText(getActivity(), "Show participants",
				// 1000).show();
				showHideParticipants();
			}
		});

		btnAddGroup.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// Toast.makeText(getActivity(), "Add group", 1000).show();
				showHideAddUsersToConversation();
			}
		});

		btnAcceptCall.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				acceptCall();
			}
		});

		btnCancelCall.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Globals.logDebug(this, "toCALL_STATE_NORMAL btnCancelCall");

				if (getHomeActivity() != null) {
					getHomeActivity().endCallFunction(null);
					switchCallState(CallState.NORMAL);
				}

			}
		});

		btnRejectCall.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				rejectCall();
			}

		});

	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		// first saving my state, so the bundle wont be empty.
		// http://code.google.com/p/android/issues/detail?id=19917
		outState.putString("WORKAROUND_FOR_BUG_19917_KEY",
				"WORKAROUND_FOR_BUG_19917_VALUE");
		super.onSaveInstanceState(outState);
	}

	public void acceptCall() {
		if (ringtone != null) {
			ringtone.stop();
		}
		RRChannels tempCh = new RRChannels();
		if (getRRService() != null) {
			boolean isThereChann = false;

			for (int i = 0; i < getHomeActivity().chList.size(); i++) {
				if (getHomeActivity().chList.get(i).name
						.equals(Utility.channelName)) {
					isThereChann = true;
					tempCh.id = getHomeActivity().chList.get(i).id;
					break;
				} else {
					isThereChann = false;
				}
			}
			if (isThereChann) {
				getRRService().createChannel(Utility.channelName, true, "");
				getRRService().joinChannel(tempCh.id);
				switchCallState(CallState.INCALL);
				if (CallState.sCallType == CallState.CALL_RANDOM) {
					Intent intent = new Intent();
					intent.setAction(CallHistoryReceiver.ACTION_ADD_CALL);
					intent.putExtra(CallHistoryReceiver.EXTRA_TYPE,
							DbContentProvider.CALL_HISTORY_TYPE_RANDOM_CALL);
					intent.putExtra(CallHistoryReceiver.EXTRA_ID,
							Utility.networkPrefix + Utility.senderFBId);
					RalleeApp.getInstance().sendBroadcast(intent);
				} else {
					Intent intent = new Intent();
					intent.setAction(CallHistoryReceiver.ACTION_ADD_CALL);
					intent.putExtra(
							CallHistoryReceiver.EXTRA_TYPE,
							DbContentProvider.CALL_HISTORY_TYPE_PRIVATE_CALL_INCOMING);
					intent.putExtra(CallHistoryReceiver.EXTRA_ID,
							Utility.networkPrefix + Utility.senderFBId);
					RalleeApp.getInstance().sendBroadcast(intent);
				}

			} else {
				Globals.logDebug(this, "toCALL_STATE_NORMAL  btnAcceptCall");
				switchCallState(CallState.NORMAL);
			}
		}
	}

	public void silent() {
		if (ringtone != null && ringtone.isPlaying()) {
			ringtone.stop();
		}
	}

	public void rejectCall() {

		RRChannels tempCh = new RRChannels();
		if (getRRService() != null) {

			List<RRChannels> channelList = getHomeActivity().chList;
			if (channelList != null) {
				boolean isThereChann = false;
				Globals.logDebug(this, "isThereChann rejecting channel: "
						+ Utility.channelName);
				for (int i = 0; i < channelList.size(); i++) {
					if (channelList.get(i).name.equals(Utility.channelName)) {
						isThereChann = true;
						tempCh.id = channelList.get(i).id;
						Globals.logDebug(this,
								"isThereChann rejecting channel: "
										+ Utility.channelName
										+ " channel found");
						break;
					} else {
						isThereChann = false;
					}
				}
				if (isThereChann) {
					RRPushMessagePayload payload = new RRPushMessagePayload();
					payload.sender = RalleeApp.getInstance().getRalleeUID();
					payload.payloadType = "callDismissed";
					payload.channelName = Utility.channelName;
					ObjectMapper mapper = new ObjectMapper();
					try {
						getRRService().sendChannelTextMessage(
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

					Globals.logDebug(this, "isThereChann rejecting channel: "
							+ Utility.channelName + " channel NOT found");
				}
			}
		}
		Globals.logDebug(this, "toCALL_STATE_NORMAL btnRejectCall");
		switchCallState(CallState.NORMAL);

		getRRService().messagesClearAll();

	}

	public void switchCallState(int toState) {
		switchCallState(CallState.sCurrent, toState);
	}

	private HomeActivity getHomeActivity() {
		return (HomeActivity) this.getActivity();

	}

	private RadioRuntService getRRService() {
		if (getHomeActivity() == null) {
			return null;
		} else {
			return getHomeActivity().getRRService();
		}
	}

	private void switchCallState(int fromState, int toState) {
		synchronized (lockCurrentCallState) {
			if (fromState == toState) {
				return;
			}

			Globals.logDebug(this,
					"callState CallControlsFragment switchCallState: from:"
							+ CallState.sCurrent + " to:" + toState);
			TranslateAnimation slideUpOutTranslation = new TranslateAnimation(
					TranslateAnimation.RELATIVE_TO_PARENT, 0,
					TranslateAnimation.RELATIVE_TO_PARENT, 0,
					TranslateAnimation.RELATIVE_TO_PARENT, 0,
					TranslateAnimation.RELATIVE_TO_PARENT, -1.0f);

			TranslateAnimation slideUpInTranslation = new TranslateAnimation(
					TranslateAnimation.RELATIVE_TO_PARENT, 0,
					TranslateAnimation.RELATIVE_TO_PARENT, 0,
					TranslateAnimation.RELATIVE_TO_PARENT, 1.0f,
					TranslateAnimation.RELATIVE_TO_PARENT, 0);

			if (CallState.sCurrent == CallState.INCALL
					&& toState == CallState.NORMAL) {
				Globals.logDebug(this, "callState should end Group call");
				// Intent intent = new Intent();
				// intent.setAction(GroupConversationReceiver.ACTION_ENDED_GROUP);
				// RalleeApp.getInstance().sendBroadcast(intent);s

				savePrivateGroup();
			}
			if (toState == CallState.NORMAL) {
				if (getHomeActivity() != null) {
					getHomeActivity().googleTracker("call", "allCalls", "end",
							0);
				}
				Intent intent = new Intent();
				intent.setAction(CallHistoryReceiver.ACTION_END_CALL);
				RalleeApp.getInstance().sendBroadcast(intent);
			}

			if (toState == CallState.INCALL) {
				if (usersParticipated != null) {
					usersParticipated.clear();
				}
			}

			FragmentManager fm = getFragmentManager();
			Fragment fragToRemove = fm
					.findFragmentByTag(OnlineFriendsFragment.FRAGMENT_TAG);
			if (fragToRemove != null) {
				fm.popBackStack();
			}
			fragToRemove = fm
					.findFragmentByTag(ParticipantsFragment.FRAGMENT_TAG);
			if (fragToRemove != null) {
				fm.popBackStack();
			}
			fragToRemove = fm
					.findFragmentByTag(AddUsersToConversationFragment.FRAGMENT_TAG);
			if (fragToRemove != null) {
				fm.popBackStack();
			}

			switch (fromState) {
			case CallState.NORMAL:
				switch (toState) {
				case CallState.INCALL_NEARBY:
					CallState.sCurrent = CallState.INCALL_NEARBY;
					Globals.logDebug(
							this,
							"callState"
									+ "Call State switch: from: CallState.NORMAL to: CallState.INCALL_NEARBY");

					slideUpOutTranslation.setDuration(500);
					slideUpInTranslation.setDuration(500);

					// gallery.startAnimation(slideUpOutTranslation);
					// btnEndCall.startAnimation(slideUpInTranslation);

					sDockMode = DOCK_MODE_END_CALL;
					setDockMode(sDockMode);

					VoiceModePanelFragment frag = (VoiceModePanelFragment) getFragmentManager()
							.findFragmentByTag(
									VoiceModePanelFragment.FRAGMENT_TAG);
					if (frag != null) {
						frag.updateFragment(
								R.id.tvCallerNameHomeActivity,
								RalleeApp.getInstance().getString(
										R.string.joined)
										+ Utility.channelName);
						frag.updateFragment(R.id.chronometer, true); // START
						frag.updateFragment(R.id.chronometer,
								SystemClock.elapsedRealtime());

					}
					// checkIfCallCanceled(joined);

					break;
				case CallState.INCOMING:
					CallState.sCurrent = CallState.INCOMING;
					Globals.logDebug(
							this,
							"callState"
									+ "Call State switch: from: CallState.NORMAL to: CALL_STATE_INCOMING");

					slideUpOutTranslation.setDuration(500);
					slideUpInTranslation.setDuration(500);
					// gallery.startAnimation(slideUpOutTranslation);
					// llIncomingCallBtnHolder
					// .startAnimation(slideUpInTranslation);

					sDockMode = DOCK_MODE_ACC_REJECT_CALL;
					setDockMode(sDockMode);

					frag = (VoiceModePanelFragment) getFragmentManager()
							.findFragmentByTag(
									VoiceModePanelFragment.FRAGMENT_TAG);
					if (frag != null) {
						frag.updateFragment(
								R.id.tvCallerNameHomeActivity,
								Utility.senderName
										+ RalleeApp.getInstance().getString(
												R.string.is_calling));
						frag.updateFragment(R.id.main_ui_phone_state,
								R.drawable.main_ui_lcd_panel_phone_active);
					}
					if (ringtone != null) {
						ringtone.play();
					}
					break;
				case CallState.OUTGOING:
					CallState.sCurrent = CallState.OUTGOING;
					Globals.logDebug(
							this,
							"callState"
									+ "Call State switch: from: CallState.NORMAL to: CallState.OUTGOING");

					slideUpOutTranslation.setDuration(500);
					slideUpInTranslation.setDuration(500);
					// gallery.startAnimation(slideUpOutTranslation);
					// btnCancelCall.startAnimation(slideUpInTranslation);
					frag = (VoiceModePanelFragment) getFragmentManager()
							.findFragmentByTag(
									VoiceModePanelFragment.FRAGMENT_TAG);
					if (frag != null) {
						frag.updateFragment(
								R.id.tvCallerNameHomeActivity,
								RalleeApp.getInstance().getString(
										R.string.calling)
										+ Utility.calledUserFirstName);
						frag.updateFragment(R.id.main_ui_phone_state,
								R.drawable.main_ui_lcd_panel_phone_active);
					}

					sDockMode = DOCK_MODE_CANCEL_CALL;
					setDockMode(sDockMode);
					getHomeActivity().isDialing = getHomeActivity().dialTone(
							true);
					break;
				default:
					CallState.sCurrent = CallState.NORMAL;
					Globals.logError(this,
							"callState  Ilegal Call State switch: from:"
									+ fromState + " to: " + toState);
				}
				break;
			case CallState.INCOMING:
				switch (toState) {
				case CallState.NORMAL:
					CallState.sCurrent = CallState.NORMAL;
					CallState.sCallType = CallState.CALL_IDLE;
					Globals.logDebug(
							this,
							"callState "
									+ "Call State switch: from: CALL_STATE_INCOMING to: CallState.NORMAL");

					getRRService().joinChannel(0);

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

					// BAR_MODE = BAR_MODE_GALLERY;
					// setDockMode(BAR_MODE);

					// gallery.startAnimation(slideDownInTranslation);
					// llIncomingCallBtnHolder
					// .startAnimation(slideDownOutTranslation);

					// sDockMode = DOCK_MODE_GALLERY;
					sDockMode = -1;
					setDockMode(sDockMode);// sDockMode);

					VoiceModePanelFragment frag = (VoiceModePanelFragment) getFragmentManager()
							.findFragmentByTag(
									VoiceModePanelFragment.FRAGMENT_TAG);
					if (frag != null) {
						frag.updateFragment(
								R.id.tvCallerNameHomeActivity,
								RalleeApp.getInstance().getString(
										R.string.choose_a_friend));
						frag.updateFragment(R.id.main_ui_phone_state,
								R.drawable.main_ui_lcd_panel_phone_inactive);
						frag.updateFragment(R.id.chronometer, false); // STOP
						frag.updateFragment(R.id.chronometer, "0:00:00");
					}
					if (ringtone != null) {
						ringtone.stop();
					}
					getHomeActivity().isDialing = getHomeActivity().dialTone(
							false);
					getHomeActivity().cleanDialogs();
					// getHomeActivity().checkMissedCalls();

					break;
				case CallState.INCALL:
					CallState.sCurrent = CallState.INCALL;
					Globals.logDebug(
							this,
							"callState "
									+ "Call State switch: from: CALL_STATE_INCOMING to: CallState.INCALL");

					if (Utility.callTimestamp != 0) {
						// dbAdapter.clearMissedCall(Utility.callTimestamp); //
						// Kimi
						RalleeApp
								.getInstance()
								.getApplicationContext()
								.getContentResolver()
								.delete(Uri.parse(DbContentProvider.CONTENT_URI_MISSED_CALL_TABLE
										.toString()
										+ "/"
										+ Utility.callTimestamp), null, null);
					}

					TranslateAnimation slideRightOutTranslation = new TranslateAnimation(
							TranslateAnimation.RELATIVE_TO_PARENT, 0,
							TranslateAnimation.RELATIVE_TO_PARENT, 1.0f,
							TranslateAnimation.RELATIVE_TO_PARENT, 0,
							TranslateAnimation.RELATIVE_TO_PARENT, 0);

					TranslateAnimation slideLeftOutTranslation = new TranslateAnimation(
							TranslateAnimation.RELATIVE_TO_PARENT, 0,
							TranslateAnimation.RELATIVE_TO_PARENT, -1.0f,
							TranslateAnimation.RELATIVE_TO_PARENT, 0,
							TranslateAnimation.RELATIVE_TO_PARENT, 0);

					final ScaleAnimation zoomOutInScale = new ScaleAnimation(
							2.0f, 1.0f, 2.0f, 1.0f,
							ScaleAnimation.RELATIVE_TO_PARENT, 0.5f,
							ScaleAnimation.RELATIVE_TO_PARENT, 0.5f);
					slideRightOutTranslation.setDuration(500);
					slideLeftOutTranslation.setDuration(500);
					zoomOutInScale.setStartOffset(500);
					zoomOutInScale.setDuration(500);

					btnRejectCall.startAnimation(slideLeftOutTranslation);
					btnAcceptCall.startAnimation(slideRightOutTranslation);

					try {
						Thread.sleep(500);
					} catch (InterruptedException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}

					btnEndCall.startAnimation(zoomOutInScale);

					sDockMode = DOCK_MODE_END_CALL;
					setDockMode(sDockMode);

					orderEndCallShrink();

					frag = (VoiceModePanelFragment) getFragmentManager()
							.findFragmentByTag(
									VoiceModePanelFragment.FRAGMENT_TAG);
					if (frag != null) {
						frag.updateFragment(
								R.id.tvCallerNameHomeActivity,
								RalleeApp.getInstance().getString(
										R.string.talking_to)
										+ Utility.senderName);
						frag.updateFragment(R.id.chronometer, true); // START
						frag.updateFragment(R.id.chronometer,
								SystemClock.elapsedRealtime());
					}
					// checkIfCallCanceled(joined);

					// getHomeActivity().checkMissedCalls();

					// Utility.sendFBpost(
					// Utility.fbId,
					// "post",
					// Utility.firstName
					// + " "
					// +
					// getString(R.string.message_fb_connected_via_rallee_with)
					// + " " + Utility.senderName, "", "", "", "",
					// "");
					Utility.sendFBpost(Utility.fbId, "talk", "", "", "", "", "");

					// Utility.channelName="";
					// Utility.senderName = "";
					// Utility.senderPicUrl = "";
					// Utility.senderFBId = "";
					// Utility.calledUserUsername = "";
					// Utility.callTimestamp = 0;

					break;
				default:
					CallState.sCurrent = CallState.NORMAL;
					Globals.logError(this,
							"callState Ilegal Call State switch: from:"
									+ fromState + " to: " + toState);
				}
				break;
			case CallState.OUTGOING:
				switch (toState) {
				case CallState.NORMAL:
					CallState.sCurrent = CallState.NORMAL;
					CallState.sCallType = CallState.CALL_IDLE;
					Globals.logDebug(
							this,
							"callState "
									+ "Call State switch: from: CallState.OUTGOING to: CallState.NORMAL");

					getRRService().joinChannel(0);

					Utility.channelName = "";
					// Utility.senderName = "";
					Utility.senderPicUrl = "";
					Utility.senderFBId = "";
					Utility.calledUserUsername = "";
					Utility.callTimestamp = 0;

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

					// gallery.startAnimation(slideDownInTranslation);
					// btnCancelCall.startAnimation(slideDownOutTranslation);

					getHomeActivity().callHandler
							.removeMessages(HomeActivity.CALLER_UNANSWERED);
					// sDockMode = DOCK_MODE_GALLERY;
					sDockMode = -1;
					setDockMode(sDockMode);// sDockMode);

					VoiceModePanelFragment frag = (VoiceModePanelFragment) getFragmentManager()
							.findFragmentByTag(
									VoiceModePanelFragment.FRAGMENT_TAG);
					if (frag != null) {
						frag.updateFragment(
								R.id.tvCallerNameHomeActivity,
								RalleeApp.getInstance().getString(
										R.string.choose_a_friend));
						frag.updateFragment(R.id.main_ui_phone_state,
								R.drawable.main_ui_lcd_panel_phone_inactive);
						frag.updateFragment(R.id.chronometer, false); // STOP
						frag.updateFragment(R.id.chronometer, "0:00:00");
					}
					getHomeActivity().isDialing = getHomeActivity().dialTone(
							false);
					// getHomeActivity().checkMissedCalls();
					break;
				case CallState.INCALL:
					CallState.sCurrent = CallState.INCALL;
					Globals.logDebug(
							this,
							"callState "
									+ "Call State switch: from: CallState.OUTGOING to: CallState.INCALL");

					final ScaleAnimation zoomOutInScale = new ScaleAnimation(
							2.0f, 1.0f, 2.0f, 1.0f,
							ScaleAnimation.RELATIVE_TO_PARENT, 0.5f,
							ScaleAnimation.RELATIVE_TO_PARENT, 0.5f);
					ScaleAnimation zoomOutOutScale = new ScaleAnimation(1.0f,
							0, 1.0f, 0, ScaleAnimation.RELATIVE_TO_PARENT,
							0.5f, ScaleAnimation.RELATIVE_TO_PARENT, 0.5f);

					zoomOutOutScale.setDuration(500);
					zoomOutInScale.setDuration(500);
					// zoomOutOutScale.setAnimationListener(new
					// AnimationListener() {
					//
					// @Override
					// public void onAnimationStart(Animation animation) {
					// // TODO Auto-generated method stub
					//
					// }
					//
					// @Override
					// public void onAnimationRepeat(Animation animation) {
					// // TODO Auto-generated method stub
					//
					// }
					//
					// @Override
					// public void onAnimationEnd(Animation animation) {
					// // TODO Auto-generated method stub
					//
					// }
					// });

					btnCancelCall.startAnimation(zoomOutOutScale);
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					btnEndCall.startAnimation(zoomOutInScale);

					sDockMode = DOCK_MODE_END_CALL;
					setDockMode(sDockMode);

					orderEndCallShrink();

					// Toast.makeText(getActivity().getApplicationContext(),
					// R.string.user_has_joined_group, 500).show();
					frag = (VoiceModePanelFragment) getFragmentManager()
							.findFragmentByTag(
									VoiceModePanelFragment.FRAGMENT_TAG);
					if (frag != null) {
						frag.updateFragment(
								R.id.tvCallerNameHomeActivity,
								RalleeApp.getInstance().getString(
										R.string.talking_to)
										+ Utility.calledUserFirstName);
						frag.updateFragment(R.id.main_ui_phone_state,
								R.drawable.main_ui_lcd_panel_phone_active);
						frag.updateFragment(R.id.chronometer, true); // START
						frag.updateFragment(R.id.chronometer,
								SystemClock.elapsedRealtime());
					}
					getHomeActivity().isDialing = getHomeActivity().dialTone(
							false);
					// String fbIdOfCalledUser = Utility.parseSNData(
					// Utility.calledUserUsername).getAsString(
					// Utility.SOCIAL_NETWORK_ID);
					Utility.sendFBpost(Utility.fbId, "talk", "", "", "", "", "");
					break;
				default:
					CallState.sCurrent = CallState.NORMAL;

				}
				break;
			case CallState.INCALL:
				switch (toState) {
				case CallState.NORMAL:
					CallState.sCurrent = CallState.NORMAL;
					CallState.sCallType = CallState.CALL_IDLE;
					Globals.logDebug(
							this,
							"callState "
									+ "Call State switch: from: CallState.INCALL to: CallState.NORMAL");

					Utility.calledUserUsername = "";
					Utility.channelName = "";
					Utility.senderName = "";
					Utility.senderPicUrl = "";
					Utility.senderFBId = "";
					Utility.callTimestamp = 0;

					getRRService().joinChannel(0);

					getHomeActivity().callHandler
							.removeMessages(getHomeActivity().CALLER_UNANSWERED);
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

					// gallery.startAnimation(slideDownInTranslation);
					// btnEndCall.startAnimation(slideDownOutTranslation);

					// sDockMode = DOCK_MODE_GALLERY;
					sDockMode = -1;
					setDockMode(sDockMode);// sDockMode);

					VoiceModePanelFragment frag = (VoiceModePanelFragment) getFragmentManager()
							.findFragmentByTag(
									VoiceModePanelFragment.FRAGMENT_TAG);
					if (frag != null) {
						frag.updateFragment(
								R.id.tvCallerNameHomeActivity,
								RalleeApp.getInstance().getString(
										R.string.choose_a_friend));
						frag.updateFragment(R.id.main_ui_phone_state,
								R.drawable.main_ui_lcd_panel_phone_inactive);
						frag.updateFragment(R.id.chronometer, false); // STOP
						frag.updateFragment(R.id.chronometer, "0:00:00");
					}

					// getHomeActivity().checkMissedCalls();
					break;
				default:
					CallState.sCurrent = CallState.NORMAL;
					Globals.logDebug(this,
							"callState Ilegal Call State switch: from:"
									+ fromState + " to: " + toState);
				}
				break;
			case CallState.INCALL_NEARBY:
				switch (toState) {
				case CallState.NORMAL:
					CallState.sCurrent = CallState.NORMAL;

					Globals.logDebug(
							this,
							"callState "
									+ "Call State switch: from: CallState.INCALL to: CallState.NORMAL");

					Utility.calledUserUsername = "";
					Utility.channelName = "";
					Utility.senderName = "";
					Utility.senderPicUrl = "";
					Utility.senderFBId = "";
					Utility.callTimestamp = 0;
					Utility.switchChannel = null;

					getRRService().joinChannel(0);

					getHomeActivity().callHandler
							.removeMessages(getHomeActivity().CALLER_UNANSWERED);
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

					// gallery.startAnimation(slideDownInTranslation);
					// FragmentTransaction ft =
					// getFragmentManager().beginTransaction();
					// ft.replace(R.id.bottomPanelSlot, new
					// FriendsDockFragment(),
					// FriendsDockFragment.FRAGMENT_TAG);
					// ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
					// ft.commitAllowingStateLoss();
					btnEndCall.startAnimation(slideDownOutTranslation);

					// sDockMode = DOCK_MODE_GALLERY;
					sDockMode = -1;
					setDockMode(sDockMode);// sDockMode);

					VoiceModePanelFragment frag = (VoiceModePanelFragment) getFragmentManager()
							.findFragmentByTag(
									VoiceModePanelFragment.FRAGMENT_TAG);
					if (frag != null) {
						frag.updateFragment(
								R.id.tvCallerNameHomeActivity,
								RalleeApp.getInstance().getString(
										R.string.choose_a_friend));
						frag.updateFragment(R.id.main_ui_phone_state,
								R.drawable.main_ui_lcd_panel_phone_inactive);
						frag.updateFragment(R.id.chronometer, false); // STOP
						frag.updateFragment(R.id.chronometer, "0:00:00");
					}
					// getHomeActivity().checkMissedCalls();
					break;
				default:
					CallState.sCurrent = CallState.NORMAL;
					Globals.logError(this,
							"callState Ilegal Call State switch: from:"
									+ fromState + " to: " + toState);
				}
				break;
			default:
				CallState.sCurrent = CallState.NORMAL;
				Globals.logError(this, "callState Ilegal CURRENT Call State:"
						+ fromState);
			}

		}
	}

	public void restoreToNormalCallStateIfNeeded() {
		if (CallState.sCurrent == CallState.INCALL
				|| CallState.sCurrent == CallState.INCALL_NEARBY) {
			if (getRRService() != null
					&& getRRService().getCurrentChannel() != null
					&& getRRService().getCurrentChannel().id == 0) {
				switchCallState(CallState.NORMAL);
			}
		}
	}

	public void setDockMode(int dockMode) {
		if (dockMode == -1) {
			if (getHomeActivity().mFDFrag != null) {
				FragmentTransaction ft = getFragmentManager()
						.beginTransaction();
				ft.hide(this);
				ft.show(getHomeActivity().mFDFrag);
				// ft.replace(R.id.bottomPanelSlot, getHomeActivity().mFDFrag,
				// FriendsDockFragment.FRAGMENT_TAG);
				ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
				// ft.commit();
				ft.commitAllowingStateLoss();
				// btnEndCall.startAnimation(slideDownOutTranslation);
			} else {
				Globals.logDebug(this, "callState mFDFrag is null");
				getHomeActivity().startFragment(
						FriendsDockFragment.FRAGMENT_TAG, null, null);
			}
		} else {
			if (viewFlipper == null) {
				// viewFlipper = (ViewFlipper)
				// getView().findViewById(R.id.viewFlipper);
				initControls();
			}
			viewFlipper.setDisplayedChild(dockMode);
		}
	}

	public int getDockMode() {
		return viewFlipper.getDisplayedChild();
	}

	public void orderEndCallShrink() {
		Handler endCallShrink = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				if (CallState.sCurrent == CallState.INCALL && msg.what == 1) {
					shrinkEndCallButton();
				}
				this.removeMessages(1);
			}
		};
		endCallShrink.removeMessages(1);
		endCallShrink.sendEmptyMessageDelayed(1, 4 * 1000);
	}

	public void shrinkEndCallButton() {
		sDockMode = DOCK_MODE_IN_CALL;
		setDockMode(sDockMode);
		if (CallState.calledFriends != null) {
			CallState.calledFriends.clear();
		} else {
			CallState.calledFriends = new ArrayList<String>();
		}
	}

	private void endCall() {

		// RadioRuntService.SWITCH_SERVER = 0;
		boolean switchChannel = false;
		if (CallState.sCurrent == CallState.INCALL_NEARBY) {
			// switchChannel = true;
		}
		// if (CallState.sCurrent == CallState.INCALL) {
		// getHomeActivity().endCallFunction();
		// }
		String endcallChannelName = new String(Utility.channelName);
		switchCallState(CallState.NORMAL);

		if (getHomeActivity() != null) {
			getHomeActivity().endCallFunction(endcallChannelName);
		}

		Globals.logDebug(this, "btnEndCall");
		switchCallState(CallState.NORMAL);
		// CURRENT_CH_ID is set with observer but with this
		// we insure that we will be back to root channel
		getRRService().messagesClearAll();
		// wait for update of users list from the server
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// if(switchChannel){
		// unregisterParticipantServiceObserver();
		// getRRService().reconnectServer();
		if (getRRService() != null) {
			getRRService().joinChannel(0);
		}
		// RadioRuntService.HOME_ACTIVITY_PAUSED = 1;
		Intent intent = new Intent(getHomeActivity(), LogInActivity.class);

		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP).addFlags(
				Intent.FLAG_ACTIVITY_NEW_TASK);
		// startActivity(intent);

	}

	protected void showHideParticipants() {

		if (getActivity() != null) {
			Fragment pFrag = getFragmentManager().findFragmentByTag(
					ParticipantsFragment.FRAGMENT_TAG);
			if (pFrag == null) {
				hideAddUsersToConversation();
				btnParticipants
						.setImageResource(R.drawable.main_ui_btn_view_participants_on);
				FragmentTransaction ft = getFragmentManager()
						.beginTransaction().replace(
								R.id.bottomExtensionPanelSlot,
								new ParticipantsFragment(),
								ParticipantsFragment.FRAGMENT_TAG);
				ft.addToBackStack(null);
				// ft.commit();
				ft.commitAllowingStateLoss();
				getActivity().overridePendingTransition(0, 0);
			} else {
				btnParticipants
						.setImageResource(R.drawable.button_participants);
				getFragmentManager().popBackStack();
			}
		}

	}

	public void hideParticipants() {

		Fragment pFrag = getFragmentManager().findFragmentByTag(
				ParticipantsFragment.FRAGMENT_TAG);
		if (pFrag != null) {
			btnParticipants.setImageResource(R.drawable.button_participants);
			getFragmentManager().popBackStack();
		}
	}

	protected void showHideAddUsersToConversation() {
		if (getActivity() != null) {
			Fragment autcFrag = getFragmentManager().findFragmentByTag(
					AddUsersToConversationFragment.FRAGMENT_TAG);
			if (autcFrag == null) {
				hideParticipants();
				btnAddGroup
						.setImageResource(R.drawable.main_ui_btn_make_group_on);
				FragmentTransaction ft = getFragmentManager()
						.beginTransaction().replace(
								R.id.bottomExtensionPanelSlot,
								new AddUsersToConversationFragment(),
								AddUsersToConversationFragment.FRAGMENT_TAG);
				ft.addToBackStack(null);
				// ft.commit();
				ft.commitAllowingStateLoss();
				getActivity().overridePendingTransition(0, 0);
			} else {
				btnAddGroup
						.setImageResource(R.drawable.button_add_user_to_conversation);
				getFragmentManager().popBackStack();
			}
		}
	}

	public void hideAddUsersToConversation() {
		Fragment autcFrag = getFragmentManager().findFragmentByTag(
				AddUsersToConversationFragment.FRAGMENT_TAG);
		if (autcFrag != null) {
			btnAddGroup
					.setImageResource(R.drawable.button_add_user_to_conversation);
			getFragmentManager().popBackStack();
		}

	}

	public void addParticipants() {

		if (getHomeActivity() == null) {
			return;
		}

		if (getHomeActivity().getRRService() == null) {
			return;
		}

		List<RRUser> users = getHomeActivity().getRRService().getUserList();
		int currentChannelId = getHomeActivity().getRRService()
				.getCurrentChannel().id;
		if (users == null) {
			return;
		}
		for (RRUser u : users) {
			if (currentChannelId == u.getChannel().id) {
				addIfNotDuplicate(u.userName);
			}
		}

	}

	public boolean addIfNotDuplicate(String userId) {
		if (userId != null && !usersParticipated.contains(userId)
				&& !userId.equals(RalleeApp.getInstance().getRalleeUID())) {
			usersParticipated.add(userId);
			if (usersParticipated.size() > 1) {
				CallState.sCallType = CallState.CALL_PRIVATE_GROUP;
			}
			return true;
		} else {
			return false;
		}
	}

	private void savePrivateGroup() {
		if (usersParticipated == null) {
			return;
		}
		if (usersParticipated.size() < 2) {
			return;
		}

		((HomeActivity) getActivity()).showCustomDialog(2,
				R.string.label_private_group,
				R.string.label_save_this_conversation_as_group,
				R.string.label_ok, new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						// Toast.makeText(mContext, "Group Saved !!! ", 500)
						// .show();
						renamePrivateGroup();
					}
				}, R.string.label_cancel, new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						usersParticipated.clear();
					}

				});

		// Toast.makeText(mContext, "Group Saved !!! ", 500).show();
	}

	private void renamePrivateGroup() {

		final String groupName = "Private Group";

		Globals.logDebug(this, "fbGroups Rename Group");
		// Toast.makeText(((HomeActivity) mContext), "Rename Group",
		// 500).show();

		// LayoutInflater inflater = (LayoutInflater) ((HomeActivity) mContext)
		// .getApplicationContext().getSystemService(
		// ((HomeActivity) mContext).LAYOUT_INFLATER_SERVICE);
		// View layout = inflater.inflate(R.layout.add_name_dialog, null);
		alertDialog = new Dialog(((HomeActivity) getActivity()),
				android.R.style.Theme_Translucent_NoTitleBar);

		alertDialog.setContentView(R.layout.add_name_dialog);
		// layout.setLayoutParams(new LayoutParams(250, 150));
		// layout.setLayoutParams(params)
		TextView nameLabel = (TextView) alertDialog.findViewById(R.id.tvLabel);
		nameLabel.setText(R.string.enter_group_name);
		final EditText etName = (EditText) alertDialog
				.findViewById(R.id.etName);
		etName.setText(groupName);
		etName.setSelection(0, etName.getText().length());
		Button btnCreate = (Button) alertDialog.findViewById(R.id.btnCreate);
		Button btnCancel = (Button) alertDialog.findViewById(R.id.btnCancel);
		btnCancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				alertDialog.dismiss();
				usersParticipated.clear();
			}
		});
		btnCreate.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				Uri currentPrivateGroupUri = null;
				ContentValues values = new ContentValues();

				String newGroupName = etName.getText().toString();

				values.put(DbContentProvider.PRIVATE_GROUP_COL_NAME, groupName);
				values.put(DbContentProvider.PRIVATE_GROUP_COL_NAME,
						newGroupName);
				if (newGroupName != null && !newGroupName.equals("")) {
					currentPrivateGroupUri = RalleeApp
							.getInstance()
							.getContentResolver()
							.insert(DbContentProvider.CONTENT_URI_PRIVATE_GROUP_TABLE,
									values);

					if (currentPrivateGroupUri == null) {
						return;
					}
				}

				final String groupId = currentPrivateGroupUri
						.getLastPathSegment();

				if (groupId != null && !groupId.equals("")) {
					Globals.logDebug(this, "fbGroups Group renamed to: "
							+ newGroupName);
					if (usersParticipated != null) {
						for (String uId : usersParticipated) {
							ContentValues cv = new ContentValues();
							cv.put(DbContentProvider.GROUP_MEMBERS_COL_GROUP_ID,
									groupId);
							cv.put(DbContentProvider.GROUP_MEMBERS_COL_USER_ID,
									uId);
							Uri uri = null;

							uri = RalleeApp
									.getInstance()
									.getApplicationContext()
									.getContentResolver()
									.insert(DbContentProvider.CONTENT_URI_GROUP_MEMBERS_TABLE,
											cv);
						}
					}

					((HomeActivity) getActivity()).googleTracker("groups",
							"privateGroup", "addNewAutomatic", 0);
				}
				alertDialog.dismiss();
			}
		});
		alertDialog.show();
	}

	public void Invisible() {
		btnAcceptCall.setVisibility(View.GONE);
		btnRejectCall.setVisibility(View.GONE);

	}
}
