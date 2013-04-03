package com.radiorunt.activities.fragments;

import com.radiorunt.R;
import com.radiorunt.activities.HomeActivity;
import com.radiorunt.utilities.Globals;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

public class NotificationFragment extends Fragment {
	/**
	 * Tag that can be used to identify fragment within activity manager
	 */
	public static final String FRAGMENT_TAG = "notificationFragment";

	public static final String MESSAGE = "message";
	public static final String DURATION_IN_SECONDS = "durationInSeconds";
	public static final String NOTIFICATION_TYPE = "notificationType";

	public static final int NOTIFICATION_TYPE_MESSAGE = 0;
	public static final int NOTIFICATION_TYPE_INCOMING_CALL = 1;

	public static final int MSG_CLOSE_FRAGMENT = 0;

	TextView tvMessage;
	String message = "";
	Handler mHandler;
	int mDuration;
	int notificationType = NOTIFICATION_TYPE_MESSAGE;

	ImageButton imgbtnAnswer;
	ImageButton imgbtnReject;
	ImageButton imgbtnSilent;

	LinearLayout llIncomingCallContainer;

	LinearLayout llRootNotification;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View v = inflater.inflate(R.layout.notification, container, false);
		v.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return true;
			}
		});
		return v;
	}

	@Override
	public void onStart() {
		Globals.logInfo(this, "onStart");
		super.onStart();
		initControls();
		initControlListeners();
	}

	private void initControls() {
		tvMessage = (TextView) getView().findViewById(R.id.tvMessage);
		imgbtnAnswer = (ImageButton) getView().findViewById(
				R.id.imgbtnNotifAnswer);
		imgbtnReject = (ImageButton) getView().findViewById(
				R.id.imgbtnNotifReject);
		imgbtnSilent = (ImageButton) getView().findViewById(
				R.id.imgbtnNotifSilent);
		llIncomingCallContainer = (LinearLayout) getView().findViewById(
				R.id.llIncomingCallContainer);
		llIncomingCallContainer.setVisibility(View.GONE);
		llRootNotification = (LinearLayout) getView().findViewById(
				R.id.llRootNotification);

		if (message != null && !message.equals("")) {
			message += "\n" + getArguments().getString(MESSAGE);
		} else {
			message = getArguments().getString(MESSAGE);
		}

		if (message != null && !message.equals("")) {
			tvMessage.setText(message);
		} else {
			tvMessage.setVisibility(View.GONE);
		}

		mDuration = getArguments().getInt(DURATION_IN_SECONDS) * 1000;

		notificationType = getArguments().getInt(NOTIFICATION_TYPE); // Retruns
																		// NOTIFICATION_TYPE_MESSAGE
																		// (0)
																		// as
																		// defaul
																		// value

		if (notificationType == NOTIFICATION_TYPE_INCOMING_CALL) {
			imgbtnAnswer.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					mHandler.removeMessages(MSG_CLOSE_FRAGMENT);
					mHandler.sendEmptyMessage(MSG_CLOSE_FRAGMENT);
					((HomeActivity) getActivity())
							.closeFragmentDueToIncomingCall();
					((HomeActivity) getActivity()).acceptCall();
				}
			});
			imgbtnReject.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					rejectCall();
				}
			});
			imgbtnSilent.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					mHandler.removeMessages(MSG_CLOSE_FRAGMENT);
					mHandler.sendEmptyMessage(MSG_CLOSE_FRAGMENT);
					((HomeActivity) getActivity()).silent();
				}
			});

			llIncomingCallContainer.setVisibility(View.VISIBLE);

			llRootNotification.setOnTouchListener(new View.OnTouchListener() {

				@Override
				public boolean onTouch(View v, MotionEvent event) {
					return true;
				}
			});
		}
	}

	public void rejectCall() {
		mHandler.removeMessages(MSG_CLOSE_FRAGMENT);
		mHandler.sendEmptyMessage(MSG_CLOSE_FRAGMENT);
		((HomeActivity) getActivity()).rejectCall();

	}

	private void initControlListeners() {
		mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				if (msg.what == MSG_CLOSE_FRAGMENT) {
					if (getActivity() != null) {

						message = "";
						((HomeActivity) getActivity())
								.deinitializeNotificationFrag();

						Fragment frag = getFragmentManager().findFragmentByTag(
								NotificationFragment.FRAGMENT_TAG);
						if (frag != null) {
							FragmentTransaction ft = getFragmentManager()
									.beginTransaction();
							ft.remove(frag);
							ft.commitAllowingStateLoss();
						}
					}
				}
			}
		};

		mHandler.removeMessages(MSG_CLOSE_FRAGMENT);
		mHandler.sendEmptyMessageDelayed(MSG_CLOSE_FRAGMENT, mDuration);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		// // first saving my state, so the bundle wont be empty.
		// // http://code.google.com/p/android/issues/detail?id=19917
		// outState.putString("WORKAROUND_FOR_BUG_19917_KEY",
		// "WORKAROUND_FOR_BUG_19917_VALUE");
		// super.onSaveInstanceState(outState);
	}

	public void addMessage(String newMessage, int newDurationInSeconds) {
		if (notificationType == NOTIFICATION_TYPE_MESSAGE) {
			if (mHandler != null) {
				mHandler.removeMessages(MSG_CLOSE_FRAGMENT);
				mHandler.sendEmptyMessageDelayed(MSG_CLOSE_FRAGMENT,
						newDurationInSeconds * 1000);
			}

			message += "\n" + newMessage;

			if (tvMessage != null) {
				if (message != null && !message.equals("")) {
					tvMessage.setText(message);
				} else {
					tvMessage.setVisibility(View.GONE);
				}
			}
		} else {
			if (getActivity() != null) {

				((HomeActivity) getActivity()).deinitializeNotificationFrag();
			}
		}

	}

	public void removeIncomingCallNotification() {
		if (notificationType == NOTIFICATION_TYPE_INCOMING_CALL) {
			mHandler.removeMessages(MSG_CLOSE_FRAGMENT);
			mHandler.sendEmptyMessage(MSG_CLOSE_FRAGMENT);
		}

	}
}
