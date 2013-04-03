package com.radiorunt.activities.fragments;

import com.radiorunt.R;
import com.radiorunt.activities.HomeActivity;
import com.radiorunt.utilities.Globals;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class GroupsFragment extends Fragment {

	/**
	 * Tag that can be used to identify fragment within activity manager
	 */
	public static final String FRAGMENT_TAG = "groupsFragment";
	private LinearLayout llPrivateSection;
	private LinearLayout llFacebookSection;
	private LinearLayout llPublicSection;
	private OnClickListener sectionClickLIstener;

	private FrameLayout privateGroupsSlot;
	private FrameLayout facebookGroupsSlot;
	private FrameLayout publicGroupsSlot;

	private ImageView imgArrowPrivate;
	private ImageView imgArrowFacebook;
	private ImageView imgArrowPublic;

	private ImageButton imgbtnClose;

	private int lastSectionOpened = -1;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Globals.logInfo(this, "onCreateView");
		View v = inflater.inflate(R.layout.groups, container, false);
		return v;
	}

	@Override
	public void onStart() {
		Globals.logInfo(this, "onStart");
		super.onStart();
		initControls();
		initControlListeners();
	}

	@Override
	public void onResume() {
		Globals.logInfo(this, "onResume");
		super.onResume();
		if (getHomeActivity() != null) {
			getHomeActivity().setScreenAnalytics("GroupsList");
		}
		switch (lastSectionOpened) {
		case 0:
			openSection(R.id.llPrivateGroupsSection);
			break;
		case 1:
			openSection(R.id.llFacebookGroupsSection);
			break;
		case 2:
			openSection(R.id.llPublicGroupsSection);
			break;
		default:
			openSection(R.id.llPublicGroupsSection);
		}
	}

	private void initControls() {
		Globals.logInfo(this, "initControls");

		imgbtnClose = (ImageButton) getView().findViewById(R.id.imgbtnClose);
		llPrivateSection = (LinearLayout) getView().findViewById(
				R.id.llPrivateGroupsSection);
		llFacebookSection = (LinearLayout) getView().findViewById(
				R.id.llFacebookGroupsSection);
		llPublicSection = (LinearLayout) getView().findViewById(
				R.id.llPublicGroupsSection);

		privateGroupsSlot = (FrameLayout) getView().findViewById(
				R.id.privateGroupsSlot);
		facebookGroupsSlot = (FrameLayout) getView().findViewById(
				R.id.facebookGroupsSlot);
		publicGroupsSlot = (FrameLayout) getView().findViewById(
				R.id.publicGroupsSlot);

		imgArrowPrivate = (ImageView) getView().findViewById(
				R.id.imgArrowPrivate);
		imgArrowFacebook = (ImageView) getView().findViewById(
				R.id.imgArrowFacebook);
		imgArrowPublic = (ImageView) getView()
				.findViewById(R.id.imgArrowPublic);
	}

	private void initControlListeners() {
		Globals.logInfo(this, "initControlListeners");
		imgbtnClose.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				getFragmentManager().popBackStack();
			}
		});

		sectionClickLIstener = new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				openSection(v.getId());
			}
		};
		llPrivateSection.setOnClickListener(sectionClickLIstener);
		llFacebookSection.setOnClickListener(sectionClickLIstener);
		llPublicSection.setOnClickListener(sectionClickLIstener);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		// // first saving my state, so the bundle wont be empty.
		// // http://code.google.com/p/android/issues/detail?id=19917
		// outState.putString("WORKAROUND_FOR_BUG_19917_KEY",
		// "WORKAROUND_FOR_BUG_19917_VALUE");
		// super.onSaveInstanceState(outState);
	}

	private void openSection(int viewId) {
		Globals.logInfo(this, "openSection");
		if (privateGroupsSlot != null && publicGroupsSlot != null
				&& facebookGroupsSlot != null) {
			switch (viewId) {
			case R.id.llPrivateGroupsSection:

				openPrivateSection();

				break;
			case R.id.llFacebookGroupsSection:

				openFacebookSection();

				break;
			case R.id.llPublicGroupsSection:

				openPublicSection();

				break;
			default:

				privateGroupsSlot.setVisibility(View.GONE);
				facebookGroupsSlot.setVisibility(View.GONE);
				publicGroupsSlot.setVisibility(View.GONE);

				imgArrowPrivate
						.setImageResource(R.drawable.channels_ui_moving_function_bar_btn_close_browse);
				imgArrowFacebook
						.setImageResource(R.drawable.channels_ui_moving_function_bar_btn_close_browse);
				imgArrowPublic
						.setImageResource(R.drawable.channels_ui_moving_function_bar_btn_close_browse);
				break;
			}
		}
	}

	private void openPrivateSection() {
		privateGroupsSlot.setVisibility(View.VISIBLE);
		facebookGroupsSlot.setVisibility(View.GONE);
		publicGroupsSlot.setVisibility(View.GONE);

		imgArrowPrivate
				.setImageResource(R.drawable.channels_ui_moving_function_bar_btn_open_browse);
		imgArrowFacebook
				.setImageResource(R.drawable.channels_ui_moving_function_bar_btn_close_browse);
		imgArrowPublic
				.setImageResource(R.drawable.channels_ui_moving_function_bar_btn_close_browse);

		FragmentTransaction ft = getFragmentManager().beginTransaction();
		Fragment puglf = getFragmentManager().findFragmentByTag(
				PublicGroupsListFragment.FRAGMENT_TAG);
		if (puglf != null) {
			ft.remove(puglf);
		}
		Fragment fglf = getFragmentManager().findFragmentByTag(
				FacebookGroupsListFragment.FRAGMENT_TAG);
		if (fglf != null) {
			ft.remove(fglf);
		}
		ft.replace(R.id.privateGroupsSlot, new PrivateGroupsListFragment(),
				PrivateGroupsListFragment.FRAGMENT_TAG);
		// ft.commit();
		ft.commitAllowingStateLoss();
		getActivity().overridePendingTransition(0, 0);

		Globals.logInfo(this,
				"privateGroupsSlot replace PrivateGroupsListFragment");
		getHomeActivity()
				.googleTracker("groups", "privateGroup", "showList", 0);
		lastSectionOpened = 0;
	}

	private void openFacebookSection() {
		privateGroupsSlot.setVisibility(View.GONE);
		facebookGroupsSlot.setVisibility(View.VISIBLE);
		publicGroupsSlot.setVisibility(View.GONE);

		imgArrowPrivate
				.setImageResource(R.drawable.channels_ui_moving_function_bar_btn_close_browse);
		imgArrowFacebook
				.setImageResource(R.drawable.channels_ui_moving_function_bar_btn_open_browse);
		imgArrowPublic
				.setImageResource(R.drawable.channels_ui_moving_function_bar_btn_close_browse);

		FragmentTransaction ft = getFragmentManager().beginTransaction();
		Fragment puglf = getFragmentManager().findFragmentByTag(
				PublicGroupsListFragment.FRAGMENT_TAG);
		if (puglf != null) {
			ft.remove(puglf);
		}
		Fragment prglf = getFragmentManager().findFragmentByTag(
				PrivateGroupsListFragment.FRAGMENT_TAG);
		if (prglf != null) {
			ft.remove(prglf);
		}
		ft.replace(R.id.facebookGroupsSlot, new FacebookGroupsListFragment(),
				FacebookGroupsListFragment.FRAGMENT_TAG);
		// ft.commit();
		ft.commitAllowingStateLoss();
		getActivity().overridePendingTransition(0, 0);

		Globals.logInfo(this,
				"facebookGroupsSlot replace FacebookGroupsListFragment");
		getHomeActivity().googleTracker("groups", "fbGroup", "showList", 0);
		lastSectionOpened = 1;
	}

	private void openPublicSection() {
		privateGroupsSlot.setVisibility(View.GONE);
		facebookGroupsSlot.setVisibility(View.GONE);
		publicGroupsSlot.setVisibility(View.VISIBLE);

		imgArrowPrivate
				.setImageResource(R.drawable.channels_ui_moving_function_bar_btn_close_browse);
		imgArrowFacebook
				.setImageResource(R.drawable.channels_ui_moving_function_bar_btn_close_browse);
		imgArrowPublic
				.setImageResource(R.drawable.channels_ui_moving_function_bar_btn_open_browse);

		FragmentTransaction ft = getFragmentManager().beginTransaction();
		Fragment fglf = getFragmentManager().findFragmentByTag(
				FacebookGroupsListFragment.FRAGMENT_TAG);
		if (fglf != null) {
			ft.remove(fglf);
		}
		Fragment prglf = getFragmentManager().findFragmentByTag(
				PrivateGroupsListFragment.FRAGMENT_TAG);
		if (prglf != null) {
			ft.remove(prglf);
		}
		ft.replace(R.id.publicGroupsSlot, new PublicGroupsListFragment(),
				PublicGroupsListFragment.FRAGMENT_TAG);
		// ft.commit();
		ft.commitAllowingStateLoss();
		getActivity().overridePendingTransition(0, 0);

		Globals.logInfo(this,
				"publicGroupsSlot replace PublicGroupsListFragment");
		getHomeActivity().googleTracker("groups", "publicGroup", "showList", 0);
		lastSectionOpened = 2;

	}

	private HomeActivity getHomeActivity() {
		return (HomeActivity) getActivity();
	}

}
