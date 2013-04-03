package com.radiorunt.activities.fragments;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import com.radiorunt.R;
import com.radiorunt.activities.DbContentProvider;
import com.radiorunt.activities.HomeActivity;
import com.radiorunt.businessobjects.RRUser;
import com.radiorunt.facebook.FriendsGetProfilePics;
import com.radiorunt.services.RadioRuntService;
import com.radiorunt.utilities.Globals;
import com.radiorunt.utilities.MergeAdapter;
import com.radiorunt.utilities.RalleeApp;
import com.radiorunt.utilities.StringPair;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.location.Criteria;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter.ViewBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class PrivateGroupsListFragment extends ListFragment implements
		LoaderCallbacks<Cursor> {

	/**
	 * Tag that can be used to identify fragment within activity manager
	 */
	public static final String FRAGMENT_TAG = "privateGroupsListFragment";
	private static final int LOADER_PRIVATE_GROUPS = 0;
	private static final int LOADER_ONLINE_FRIENDS = 1;
	private MergeAdapter mMergeAdapter;
	private SimpleCursorAdapter mPrivateGroupsAdapter;
	protected FriendsGetProfilePics pictureModel;
	private HashMap<String, Integer> hmGroupsOnlineUsersCount;
	private HashMap<String, List<StringPair>> hmGroupUsersPics;
	private HashMap<String, Integer> hmGroupsUsersCount;

	@Override
	public void onStart() {
		Globals.logInfo(this, "onStart");
		super.onStart();
		initControls();
		initControlListeners();
		getLoaderManager().initLoader(LOADER_PRIVATE_GROUPS, null, this);
		getLoaderManager().initLoader(LOADER_ONLINE_FRIENDS, null, this);
	}

	@Override
	public void onResume() {
		Globals.logInfo(this, "onResume");
		super.onResume();
	}

	private void initControls() {
		Globals.logInfo(this, "initControls");

		hmGroupsUsersCount = new HashMap<String, Integer>();
		hmGroupsOnlineUsersCount = new HashMap<String, Integer>();
		hmGroupUsersPics = new HashMap<String, List<StringPair>>();

		mMergeAdapter = new MergeAdapter() {
		};

		mMergeAdapter.addView(createNewGroupItem());
		mPrivateGroupsAdapter = new SimpleCursorAdapter(getActivity(),
				R.layout.private_groups_list_item, null, new String[] {
						DbContentProvider.PRIVATE_GROUP_COL_NAME,
						DbContentProvider.PRIVATE_GROUP_COL_ID,
						DbContentProvider.PRIVATE_GROUP_COL_ID }, new int[] {
						R.id.tvGroupItemName, R.id.tvNumberOfMembers,
						R.id.imgbtnGroupItem }, 0) {

			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				View v = super.getView(position, convertView, parent);
				updateViewsCount(v);
				loadGroupUserThumbs(v);
				Cursor c = super.getCursor();
				v.setTag(c.getInt(c
						.getColumnIndex(DbContentProvider.PRIVATE_GROUP_COL_ID)));
				v.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						Integer groupId = (Integer) v.getTag();
						if (getHomeActivity() != null) {
							getHomeActivity().googleTracker("groups",
									"privateGroup", "showDetails", 0);
							getHomeActivity().setScreenAnalytics(
									"GroupsDetails-PrivateGroup");
						}
						showPrivateGroupsDetails(groupId,
								PrivateGroupDetailsFragment.MODE_VIEW);

					}
				});
				v.setOnLongClickListener(new OnLongClickListener() {

					@Override
					public boolean onLongClick(View v) {
						Integer groupId = (Integer) v.getTag();
						// Toast.makeText(getActivity(),
						// "Private group " + groupId, 500).show();
						deletePrivateGroup(groupId);
						return true;
					}
				});
				return v;
			}

		};

		mMergeAdapter.addAdapter(mPrivateGroupsAdapter);

		setListAdapter(mMergeAdapter);

		mPrivateGroupsAdapter.setViewBinder(new ViewBinder() {

			@Override
			public boolean setViewValue(View view, Cursor cursor,
					int columnIndex) {

				switch (view.getId()) {
				case R.id.tvNumberOfMembers:
					view.setTag(cursor.getString(columnIndex));
					return true;
				case R.id.imgbtnGroupItem:
					view.setTag(cursor.getString(columnIndex));
					view.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {

							String groupId = (String) v.getTag();

							String groupName = null;
							Cursor c = null;
							try {
								c = RalleeApp
										.getInstance()
										.getApplicationContext()
										.getContentResolver()
										.query(DbContentProvider.CONTENT_URI_PRIVATE_GROUP_TABLE,
												null,
												DbContentProvider.PRIVATE_GROUP_COL_ID
														+ "=?",
												new String[] { "" + groupId },
												null);

								if (c != null && c.moveToFirst()) {
									groupName = c.getString(c
											.getColumnIndex(DbContentProvider.PRIVATE_GROUP_COL_NAME));
								}
							} finally {
								if (c != null && !c.isClosed()) {
									c.close();
								}
							}
							if (getHomeActivity() != null) {
								getHomeActivity().enterPrivateGroupChannel(
										groupId, groupName);
							}
							// GroupsFragment gf = (GroupsFragment)
							// getFragmentManager()
							// .findFragmentByTag(
							// GroupsFragment.FRAGMENT_TAG);
							// if (gf != null) {
							// getFragmentManager().popBackStack();
							// }
						}
					});

					return true;
				}
				return false;
			}
		});
	}

	private void initControlListeners() {
		Globals.logInfo(this, "initControlListeners");
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		// // first saving my state, so the bundle wont be empty.
		// // http://code.google.com/p/android/issues/detail?id=19917
		// outState.putString("WORKAROUND_FOR_BUG_19917_KEY",
		// "WORKAROUND_FOR_BUG_19917_VALUE");
		// super.onSaveInstanceState(outState);
	}

	protected void showGroupsDetails(String channelName) {
		PublicGroupDetailsFragment gdfrag = new PublicGroupDetailsFragment();
		Bundle bundle = new Bundle();
		bundle.putString(PublicGroupDetailsFragment.URI,
				DbContentProvider.CONTENT_URI_CHANNEL_TABLE + "/" + channelName);
		gdfrag.setArguments(bundle);
		FragmentTransaction ft = getFragmentManager()
				.beginTransaction()
				.setCustomAnimations(R.anim.slide_right_in,
						R.anim.slide_left_out, R.anim.slide_left_in,
						R.anim.slide_right_out)
				.replace(R.id.topSlot, gdfrag,
						PublicGroupDetailsFragment.FRAGMENT_TAG);
		ft.addToBackStack(null);
		// ft.commit();
		ft.commitAllowingStateLoss();
	}

	@Override
	public Loader<Cursor> onCreateLoader(int loaderId, Bundle bundle) {
		Uri baseUri;
		switch (loaderId) {
		case LOADER_PRIVATE_GROUPS:
			baseUri = DbContentProvider.CONTENT_URI_PRIVATE_GROUP_TABLE;
			return new CursorLoader(RalleeApp.getInstance(), baseUri, null,
					null, null, null);
		case LOADER_ONLINE_FRIENDS:
			List<RRUser> rrUsers = null;
			if (getRRService() == null) {
				rrUsers = new ArrayList<RRUser>();
//				Log.i("nullPointer", "mService is NULL");
			} else {

//				Log.i("nullPointer", "mService is OK");
				rrUsers = getRRService().getUserList();
			}

			String selectionString = DbContentProvider.GROUP_MEMBERS_COL_USER_ID
					+ " IN (";
			for (int i = 0; i < rrUsers.size(); i++) {
				if (i == 0) {
					selectionString += "'" + rrUsers.get(i).userName + "'";
				} else {
					selectionString += ", '" + rrUsers.get(i).userName + "'";
				}
			}

			selectionString += ")";

			baseUri = DbContentProvider.CONTENT_URI_GROUP_MEMBERS_TABLE;
			String select = null;
			String selectionWhereIn = selectionString;

			if (selectionWhereIn != null && !selectionWhereIn.equals("")) {
				select = selectionWhereIn;
			}

			String order = DbContentProvider.GROUP_MEMBERS_COL_USER_ID
					+ " COLLATE LOCALIZED ASC";
			return new CursorLoader(RalleeApp.getInstance(), baseUri, null,
					select, null, order);
		}
		return null;

	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		switch (loader.getId()) {
		case LOADER_PRIVATE_GROUPS:
			mPrivateGroupsAdapter.swapCursor(data);
			updateGroupsUserPics(data);
			break;
		case LOADER_ONLINE_FRIENDS:
			updateGroupsCount(data);
			mPrivateGroupsAdapter.notifyDataSetChanged();
			break;
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		switch (loader.getId()) {
		case LOADER_PRIVATE_GROUPS:
			mPrivateGroupsAdapter.swapCursor(null);
			break;
		case LOADER_ONLINE_FRIENDS:
			break;
		}
	}

	private View createNewGroupItem() {
		LayoutInflater inflater = LayoutInflater.from(getActivity());
		View v = inflater.inflate(R.layout.private_new_groups_list_item, null);
		v.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// Toast.makeText(getActivity(), "New Private Group",
				// 500).show();
				showPrivateGroupsDetails(-1,
						PrivateGroupDetailsFragment.MODE_ADD);
			}
		});
		return v;
	}

	private void updateGroupsCount(Cursor data) {

		if (data != null && data.moveToFirst()) {
			hmGroupsOnlineUsersCount.clear();
			do {
				String groupId = data
						.getString(data
								.getColumnIndex(DbContentProvider.GROUP_MEMBERS_COL_GROUP_ID));
				Integer curValue = hmGroupsOnlineUsersCount.get(groupId);
				if (curValue == null) {
					curValue = 1;
				} else {
					curValue++;
				}
				hmGroupsOnlineUsersCount.put(groupId, curValue);

			} while (data.moveToNext());
		}
//		Log.i(FRAGMENT_TAG, "After online users count update: "
//				+ hmGroupsOnlineUsersCount.toString());
	}

	synchronized protected void updateViewsCount(View v) {
		TextView tv = (TextView) v.findViewById(R.id.tvNumberOfMembers);
		ImageView glow = (ImageView) v.findViewById(R.id.profile_pic);
		ImageButton talk = (ImageButton) v.findViewById(R.id.imgbtnGroupItem);

		if (tv != null && glow != null && talk != null) {
			int resid = 0;
			String groupName = (String) tv.getTag();
			Integer count = hmGroupsOnlineUsersCount.get(groupName);
			if (count != null) {
				tv.setText(count.toString());
				resid = R.color.cyan_active_group;
			} else {
				tv.setText("0");
			}
			glow.setBackgroundResource(resid);
			count = hmGroupsUsersCount.get(groupName);
			if (count != null) {
				if (count == 0) {
					talk.setVisibility(View.INVISIBLE);
				} else {
					talk.setVisibility(View.VISIBLE);
				}

			}
		}
	}

	private void updateGroupsUserPics(Cursor data) {
		if (data != null && data.moveToFirst()) {
//			Log.i(FRAGMENT_TAG, "Private groups in memory: " + data.getCount());
			do {
				String groupId = data
						.getString(data
								.getColumnIndex(DbContentProvider.PRIVATE_GROUP_COL_ID));
				if (!hmGroupUsersPics.containsKey(groupId)) {
					String selection = DbContentProvider.GROUP_MEMBERS_COL_GROUP_ID
							+ " = '" + groupId + "'";
					String order = "RANDOM() LIMIT 4";
					Cursor c = null;
					try {
						c = RalleeApp
								.getInstance()
								.getContentResolver()
								.query(DbContentProvider.CONTENT_URI_GROUP_MEMBERS_TABLE,
										new String[] { DbContentProvider.GROUP_MEMBERS_COL_USER_ID },
										selection, null, order);
						if (c != null) {
//							Log.i(FRAGMENT_TAG, "Number of users in group "
//									+ groupId + " : " + c.getCount());
							hmGroupsUsersCount.put(groupId, c.getCount());
							List<StringPair> groupMembers = new ArrayList<StringPair>(
									4);
							if (c.getCount() > 0) {
								selection = DbContentProvider.USER_COL_ID
										+ " IN (";
								do {
									if (c.isLast()) {
										selection += "'"
												+ c.getString(c
														.getColumnIndex(DbContentProvider.GROUP_MEMBERS_COL_USER_ID))
												+ "')";
									} else {
										selection += "'"
												+ c.getString(c
														.getColumnIndex(DbContentProvider.GROUP_MEMBERS_COL_USER_ID))
												+ "', ";
									}

								} while (c.moveToNext());
								if (c != null && !c.isClosed()) {
									c.close();
								}
								c = RalleeApp
										.getInstance()
										.getContentResolver()
										.query(DbContentProvider.CONTENT_URI_USER_TABLE,
												new String[] {
														DbContentProvider.USER_COL_ID,
														DbContentProvider.USER_COL_PIC_URL },
												selection, null, null);

//								Log.i(FRAGMENT_TAG, c.toString());

								groupMembers.clear();
								for (int i = 0; i < 4; i++) {
									StringPair sp = new StringPair();
									if (c != null) {
										if (c.moveToPosition(i)) {
											sp.setId(c.getString(c
													.getColumnIndex(DbContentProvider.USER_COL_ID)));
											sp.setUrl(c.getString(c
													.getColumnIndex(DbContentProvider.USER_COL_PIC_URL)));
										}
									}
									groupMembers.add(sp);

								}

							} else {
								groupMembers.clear();
								for (int i = 0; i < 4; i++) {
									StringPair sp = new StringPair();
									groupMembers.add(sp);
								}
							}
							hmGroupUsersPics.put(groupId, groupMembers);
						}
					} finally {
						if (c != null && !c.isClosed()) {
							c.close();
						}
					}
				}
			} while (data.moveToNext());
		}

	}

	protected void loadGroupUserThumbs(View v) {

		if (pictureModel == null) {
			pictureModel = new FriendsGetProfilePics();
			pictureModel.setListener(mPrivateGroupsAdapter);
		}
		String groupId = (String) (v.findViewById(R.id.tvNumberOfMembers))
				.getTag();
		List<StringPair> groupMembers = hmGroupUsersPics.get(groupId);
		List<ImageView> ivs = new ArrayList<ImageView>();
		ivs.add((ImageView) v.findViewById(R.id.pic1));
		ivs.add((ImageView) v.findViewById(R.id.pic2));
		ivs.add((ImageView) v.findViewById(R.id.pic3));
		ivs.add((ImageView) v.findViewById(R.id.pic4));

		for (int i = 0; i < 4; i++) {
			ivs.get(i).setImageResource(R.drawable.icon);
			if (groupMembers != null) {
				StringPair rf = groupMembers.get(i);
				if (rf != null && rf.isInitialized()) {
					ivs.get(i).setImageBitmap(
							pictureModel.getImage(rf.getId(), rf.getUrl(),
									RalleeApp.getInstance()));
//					Log.i(FRAGMENT_TAG, "setImageBitmap: " + rf.toString());
				}
			}
		}

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

	public void notifyFriendsAdapterDataSetChange() {
		try {
			getLoaderManager().restartLoader(LOADER_ONLINE_FRIENDS, null, this);
		} catch (IllegalStateException ise) {

		}

	}

	protected void showPrivateGroupsDetails(int groupId, int mode) {
		PrivateGroupDetailsFragment pgfrag = new PrivateGroupDetailsFragment();
		Bundle bundle = new Bundle();
		bundle.putString(PrivateGroupDetailsFragment.URI,
				DbContentProvider.CONTENT_URI_PRIVATE_GROUP_TABLE + "/"
						+ groupId);
		bundle.putInt(PrivateGroupDetailsFragment.MODE, mode);
		pgfrag.setArguments(bundle);
		FragmentTransaction ft = getFragmentManager()
				.beginTransaction()
				.setCustomAnimations(R.anim.slide_right_in,
						R.anim.slide_left_out, R.anim.slide_left_in,
						R.anim.slide_right_out)
				.replace(R.id.topSlot, pgfrag,
						PrivateGroupDetailsFragment.FRAGMENT_TAG);
		ft.addToBackStack(null);
		// ft.commit();
		ft.commitAllowingStateLoss();
	}

	protected void deletePrivateGroup(int groupId) {

		Cursor c = null;
		String groupName = null;
		try {
			c = RalleeApp
					.getInstance()
					.getContentResolver()
					.query(DbContentProvider.CONTENT_URI_PRIVATE_GROUP_TABLE,
							null,
							DbContentProvider.PRIVATE_GROUP_COL_ID + "=?",
							new String[] { "" + groupId }, null);

			if (c != null && c.moveToFirst()) {
				groupName = c
						.getString(c
								.getColumnIndex(DbContentProvider.PRIVATE_GROUP_COL_NAME));
			}
		} finally {
			if (c != null && !c.isClosed()) {
				c.close();
			}
		}

		if (groupName != null) {
			final Dialog dialog = new Dialog(getActivity(),
					android.R.style.Theme_Translucent_NoTitleBar);

			dialog.setContentView(R.layout.confirm_random_dialog);

			TextView text = (TextView) dialog
					.findViewById(R.id.dialog_random_title);
			text.setText(R.string.label_delete);
			text = (TextView) dialog.findViewById(R.id.dialog_random_text);
			text.setText(RalleeApp.getInstance().getString(
					R.string.delete_group)
					+ groupName);

			Button button = (Button) dialog
					.findViewById(R.id.dialog_random_btnYes);

			button.setText(R.string.label_delete);
			button.setTag(String.valueOf(groupId));
			button.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					RalleeApp
							.getInstance()
							.getContentResolver()
							.delete(DbContentProvider.CONTENT_URI_GROUP_MEMBERS_TABLE,
									DbContentProvider.GROUP_MEMBERS_COL_GROUP_ID
											+ "=?",
									new String[] { (String) v.getTag() });

					dialog.dismiss();

					RalleeApp
							.getInstance()
							.getContentResolver()
							.delete(DbContentProvider.CONTENT_URI_PRIVATE_GROUP_TABLE,
									DbContentProvider.PRIVATE_GROUP_COL_ID
											+ "=?",
									new String[] { (String) v.getTag() });

					dialog.dismiss();

					getHomeActivity().googleTracker("groups", "privateGroup",
							"delete", 0);
				}
			});

			button = (Button) dialog.findViewById(R.id.dialog_random_btnNo);
			button.setText(R.string.label_cancel);

			button.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					dialog.dismiss();
				}
			});

			dialog.show();
		}

	}
}
