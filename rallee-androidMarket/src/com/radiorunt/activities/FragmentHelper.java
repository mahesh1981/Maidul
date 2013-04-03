package com.radiorunt.activities;

import android.content.Context;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.radiorunt.R;
import com.radiorunt.activities.HomeActivity.FlipGestureDetector;
import com.radiorunt.activities.fragments.AddUsersToConversationFragment;
import com.radiorunt.activities.fragments.FriendsDockFragment.FriendsDockActionsInterface;
import com.radiorunt.activities.fragments.CallCantrolsFragment;
import com.radiorunt.activities.fragments.OnlineFriendsFragment;
import com.radiorunt.activities.fragments.ParticipantsFragment;
import com.radiorunt.activities.fragments.PeopleFragment;
import com.radiorunt.activities.fragments.RightSidePanelFragment;

public class FragmentHelper implements FriendsDockActionsInterface {

	HomeActivity mHome;

	public FragmentHelper(HomeActivity home) {
		mHome = home;
	}

	@Override
	public void goToPeople() {

		Fragment pFrag = mHome.getSupportFragmentManager().findFragmentByTag(
				ParticipantsFragment.FRAGMENT_TAG);
		if (pFrag != null) {
			mHome.getSupportFragmentManager().popBackStackImmediate();
		}

		pFrag = mHome.getSupportFragmentManager().findFragmentByTag(
				AddUsersToConversationFragment.FRAGMENT_TAG);
		if (pFrag != null) {
			mHome.getSupportFragmentManager().popBackStackImmediate();
		}

		pFrag = mHome.getSupportFragmentManager().findFragmentByTag(
				OnlineFriendsFragment.FRAGMENT_TAG);
		if (pFrag != null) {
			mHome.getSupportFragmentManager().popBackStackImmediate();
		}

		PeopleFragment frag = new PeopleFragment();

		final FragmentTransaction ft = mHome.getSupportFragmentManager()
				.beginTransaction()
				.setCustomAnimations(R.anim.slide_down_in, 0)
				.replace(R.id.sideSlot, frag, PeopleFragment.FRAGMENT_TAG);
		ft.addToBackStack(null);

		mHome.overridePendingTransition(0, 0);

		int width = (int) TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_DIP, 30, mHome.getResources()
						.getDisplayMetrics());
		int displayWidth = mHome.getWindowManager().getDefaultDisplay()
				.getWidth();
		final int move = displayWidth - width;
		final TranslateAnimation slideMainPanelToRight = new TranslateAnimation(
				TranslateAnimation.RELATIVE_TO_PARENT, 0,
				TranslateAnimation.ABSOLUTE, move,
				TranslateAnimation.RELATIVE_TO_PARENT, 0,
				TranslateAnimation.RELATIVE_TO_PARENT, 0);

		slideMainPanelToRight.setDuration(800);
		slideMainPanelToRight.setFillAfter(true);
		slideMainPanelToRight.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				FrameLayout flview = (FrameLayout) mHome
						.findViewById(R.id.topPanelSlot);
				flview.setVisibility(View.GONE);

				mHome.swipeMode = FlipGestureDetector.SWIPE_MODE_PEOPLE;
			}
		});

		TranslateAnimation slideBottomPanelDown = new TranslateAnimation(
				TranslateAnimation.RELATIVE_TO_PARENT, 0,
				TranslateAnimation.RELATIVE_TO_PARENT, 0,
				TranslateAnimation.RELATIVE_TO_PARENT, 0,
				TranslateAnimation.RELATIVE_TO_PARENT, 1.0f);
		slideBottomPanelDown.setDuration(400);
		slideBottomPanelDown.setFillAfter(true);
		slideBottomPanelDown.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				// ft.commit();
				ft.commitAllowingStateLoss();
				((LinearLayout) mHome.findViewById(R.id.llRalleeMainPanel))
						.startAnimation(slideMainPanelToRight);

				((FrameLayout) mHome.findViewById(R.id.bottomPanelSlot))
						.setVisibility(View.GONE);
			}
		});
		((FrameLayout) mHome.findViewById(R.id.bottomPanelSlot))
				.startAnimation(slideBottomPanelDown);
		mHome.setScreenAnalytics("PeopleSearch");
	}

	@Override
	public void goToAlerts() {

		Fragment pFrag = mHome.getSupportFragmentManager().findFragmentByTag(
				ParticipantsFragment.FRAGMENT_TAG);
		if (pFrag != null) {
			if (mHome.mCCFrag != null) {
				mHome.mCCFrag.hideParticipants();
			} else {
				mHome.getSupportFragmentManager().popBackStackImmediate();
			}
		}

		pFrag = mHome.getSupportFragmentManager().findFragmentByTag(
				AddUsersToConversationFragment.FRAGMENT_TAG);
		if (pFrag != null) {
			if (mHome.mCCFrag != null) {
				mHome.mCCFrag.hideAddUsersToConversation();
			} else {
				mHome.getSupportFragmentManager().popBackStackImmediate();
			}
		}

		pFrag = mHome.getSupportFragmentManager().findFragmentByTag(
				OnlineFriendsFragment.FRAGMENT_TAG);
		if (pFrag != null) {
			mHome.getSupportFragmentManager().popBackStackImmediate();
		}

		Fragment frag = new RightSidePanelFragment();
		Bundle bundle = new Bundle();
		bundle.putInt(RightSidePanelFragment.MODE,
				RightSidePanelFragment.MODE_ALERTS);
		frag.setArguments(bundle);

		final FragmentTransaction ft = mHome
				.getSupportFragmentManager()
				.beginTransaction()
				.setCustomAnimations(R.anim.slide_down_in, 0)
				.replace(R.id.sideSlot, frag,
						RightSidePanelFragment.FRAGMENT_TAG);
		ft.addToBackStack(null);

		mHome.overridePendingTransition(0, 0);
		int width = (int) TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_DIP, 30, mHome.getResources()
						.getDisplayMetrics());
		int displayWidth = mHome.getWindowManager().getDefaultDisplay()
				.getWidth();

		final int move = width - displayWidth;
		final TranslateAnimation slideMainPanelToLeft = new TranslateAnimation(
				TranslateAnimation.RELATIVE_TO_PARENT, 0,
				TranslateAnimation.ABSOLUTE, move,
				TranslateAnimation.RELATIVE_TO_PARENT, 0,
				TranslateAnimation.RELATIVE_TO_PARENT, 0);
		slideMainPanelToLeft.setDuration(800);
		slideMainPanelToLeft.setFillAfter(true);
		slideMainPanelToLeft.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {

			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				FrameLayout view = (FrameLayout) mHome
						.findViewById(R.id.topPanelSlot);
				view.setVisibility(View.GONE);

				mHome.swipeMode = FlipGestureDetector.SWIPE_MODE_RIGHT_PANEL;
			}
		});

		TranslateAnimation slideBottomPanelDown = new TranslateAnimation(
				TranslateAnimation.RELATIVE_TO_PARENT, 0,
				TranslateAnimation.RELATIVE_TO_PARENT, 0,
				TranslateAnimation.RELATIVE_TO_PARENT, 0,
				TranslateAnimation.RELATIVE_TO_PARENT, 1.0f);
		slideBottomPanelDown.setDuration(400);
		slideBottomPanelDown.setFillAfter(true);
		slideBottomPanelDown.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				ft.commitAllowingStateLoss();
				// ft.commit();
				((LinearLayout) mHome.findViewById(R.id.llRalleeMainPanel))
						.startAnimation(slideMainPanelToLeft);
				((FrameLayout) mHome.findViewById(R.id.bottomPanelSlot))
						.setVisibility(View.GONE);
			}
		});

		((FrameLayout) mHome.findViewById(R.id.bottomPanelSlot))
				.startAnimation(slideBottomPanelDown);

		mHome.setScreenAnalytics("RightSidePanel");
	}

	@Override
	public void goToMainPanel() {

		int width = (int) TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_DIP, 30, mHome.getResources()
						.getDisplayMetrics());
		int displayWidth = mHome.getWindowManager().getDefaultDisplay()
				.getWidth();
		int startPosition = 0;
		switch (mHome.swipeMode) {
		case FlipGestureDetector.SWIPE_MODE_PEOPLE:
			startPosition = displayWidth - width;
			((InputMethodManager) mHome
					.getSystemService(Context.INPUT_METHOD_SERVICE))
					.hideSoftInputFromWindow(((EditText) mHome
							.findViewById(R.id.etFilter))
							.getApplicationWindowToken(), 0);
			break;
		case FlipGestureDetector.SWIPE_MODE_RIGHT_PANEL:
			startPosition = width - displayWidth;
			break;
		}
		mHome.swipeMode = FlipGestureDetector.SWIPE_MODE_DISABLED;

		TranslateAnimation slideMainPanelToPlace = new TranslateAnimation(
				TranslateAnimation.ABSOLUTE, startPosition,
				TranslateAnimation.RELATIVE_TO_PARENT, 0,
				TranslateAnimation.RELATIVE_TO_PARENT, 0,
				TranslateAnimation.RELATIVE_TO_PARENT, 0);
		slideMainPanelToPlace.setDuration(800);
		slideMainPanelToPlace.setFillAfter(true);
		slideMainPanelToPlace.setAnimationListener(null);
		AnimationListener al = new AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
				FrameLayout view = (FrameLayout) mHome
						.findViewById(R.id.topPanelSlot);
				view.setVisibility(View.VISIBLE);
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationEnd(Animation animation) {

				mHome.swipeMode = FlipGestureDetector.SWIPE_MODE_MAIN_PANEL;
				if (mHome.getSupportFragmentManager().findFragmentByTag(
						PeopleFragment.FRAGMENT_TAG) != null
						|| mHome.getSupportFragmentManager().findFragmentByTag(
								RightSidePanelFragment.FRAGMENT_TAG) != null) {
					mHome.getSupportFragmentManager().popBackStack();
				}

				TranslateAnimation slideBottomPanelUp = new TranslateAnimation(
						TranslateAnimation.RELATIVE_TO_PARENT, 0,
						TranslateAnimation.RELATIVE_TO_PARENT, 0,
						TranslateAnimation.RELATIVE_TO_PARENT, 1.0f,
						TranslateAnimation.RELATIVE_TO_PARENT, 0);
				slideBottomPanelUp.setDuration(400);
				slideBottomPanelUp.setFillAfter(true);
				((FrameLayout) mHome.findViewById(R.id.bottomPanelSlot))
						.setVisibility(View.VISIBLE);

				((FrameLayout) mHome.findViewById(R.id.bottomPanelSlot))
						.startAnimation(slideBottomPanelUp);
			}
		};
		slideMainPanelToPlace.setAnimationListener(al);

		LinearLayout llMain = ((LinearLayout) mHome
				.findViewById(R.id.llRalleeMainPanel));
		llMain.startAnimation(slideMainPanelToPlace);
		long time = SystemClock.uptimeMillis();
		MotionEvent motionEvent1 = MotionEvent.obtain(time, time + 100,
				MotionEvent.ACTION_DOWN, 200.0f, 200.0f, 0);
		MotionEvent motionEvent2 = MotionEvent.obtain(time + 100, time + 200,
				MotionEvent.ACTION_MOVE, 300.0f, 200.0f, 0);
		MotionEvent motionEvent3 = MotionEvent.obtain(time + 200, time + 300,
				MotionEvent.ACTION_UP, 200.0f, 200.0f, 0);

		// Dispatch touch event to view

		ListView lvRightPanel = (ListView) mHome
				.findViewById(R.id.lvRightPanel);
		if (lvRightPanel != null) {
			RightSidePanelFragment rsf = (RightSidePanelFragment) mHome
					.getSupportFragmentManager().findFragmentByTag(
							RightSidePanelFragment.FRAGMENT_TAG);
			if (rsf != null) {
				rsf.isFakeClick = true;
			}
			lvRightPanel.dispatchTouchEvent(motionEvent1);
			lvRightPanel.dispatchTouchEvent(motionEvent2);
			lvRightPanel.dispatchTouchEvent(motionEvent3);
		}
		EditText etFilter = (EditText) mHome.findViewById(R.id.etFilter);
		if (etFilter != null) {
			PeopleFragment pf = (PeopleFragment) mHome
					.getSupportFragmentManager().findFragmentByTag(
							PeopleFragment.FRAGMENT_TAG);
			if (pf != null) {
				pf.onClick(etFilter);
			}
		}
		if (mHome.convMode == HomeActivity.CONVERSATION_MODE_VOICE) {
			mHome.setScreenAnalytics("MainScreen-VoiceMode");
		} else if (mHome.convMode == HomeActivity.CONVERSATION_MODE_TEXT) {
			mHome.setScreenAnalytics("MainScreen-TextMode");
		}
	}
}
