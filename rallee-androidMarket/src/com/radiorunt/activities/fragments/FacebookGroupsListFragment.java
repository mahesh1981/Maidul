package com.radiorunt.activities.fragments;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONException;

import com.facebook.FacebookException;
import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.model.GraphObject;
import com.radiorunt.R;
import com.radiorunt.activities.DbContentProvider;
import com.radiorunt.activities.HomeActivity;
import com.radiorunt.businessobjects.RRChannels;
import com.radiorunt.businessobjects.RRUser;
import com.radiorunt.facebook.FriendsGetProfilePics;
import com.radiorunt.facebook.Utility;
import com.radiorunt.services.RadioRuntService;
import com.radiorunt.utilities.Globals;
import com.radiorunt.utilities.RalleeApp;
import com.radiorunt.utilities.StringPair;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter.ViewBinder;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class FacebookGroupsListFragment extends ListFragment implements
		LoaderCallbacks<Cursor> {

	/**
	 * Tag that can be used to identify fragment within activity manager
	 */
	public static final String FRAGMENT_TAG = "facebookGroupsListFragment";

	public static final String URI = "uri";
	public static final String UIDS = "ids";
	public static final String URLS = "urls";

	private SimpleCursorAdapter mFacebookGroupsAdapter;
	protected FriendsGetProfilePics pictureModel;
	private HashMap<String, List<StringPair>> FBgroupUsersPics;
	private Handler mHandler;
	private OnClickListener listItemClick;

	@Override
	public void onStart() {
		Globals.logInfo(this, "onStart");
		super.onStart();
		initControls();
		initControlListeners();
		getLoaderManager().initLoader(0, null, this);
	}

	@Override
	public void onResume() {
		Globals.logInfo(this, "onResume");
		super.onResume();
	}

	private void initControls() {
		Globals.logInfo(this, "initControls");

		mHandler = new Handler();

		FBgroupUsersPics = new HashMap<String, List<StringPair>>();

	}

	private void initControlListeners() {
		Globals.logInfo(this, "initControlListeners");
		mFacebookGroupsAdapter = new SimpleCursorAdapter(getActivity(),
				R.layout.facebook_groups_list_item, null, new String[] {
						DbContentProvider.FB_GROUP_COL_NAME,
						DbContentProvider.FB_GROUP_COL_ID,
						DbContentProvider.FB_GROUP_COL_ID }, new int[] {
						R.id.tvGroupItemName, R.id.imgbtnGroupItem,
						R.id.profile_pic }, 0) {

			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				View rowView = super.getView(position, convertView, parent);

				loadGroupUserThumbs(rowView);
				changeGroupGlow(rowView);

				ImageButton ibEnter = (ImageButton) rowView
						.findViewById(R.id.imgbtnGroupItem);
				if (ibEnter != null) {
					ibEnter.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							if (getHomeActivity() != null) {
								String fbGroupId = (String) v.getTag();
								getHomeActivity()
										.enterFBGroupChannel(fbGroupId);
							}
						}
					});
					rowView.setTag(ibEnter.getTag());
					rowView.setOnClickListener(listItemClick);

				}
				;

				return rowView;
			}

		};
		listItemClick = new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				String channelName = (String) v.getTag();
//				Log.i(FRAGMENT_TAG, "Item " + v + " is clicked");
				if (channelName != null && !channelName.equals("")) {
//					Log.i(FRAGMENT_TAG, channelName);
					showGroupsDetails(channelName);
				}
			}
		};
		mFacebookGroupsAdapter.setViewBinder(vbFacebookGroups());
		setListAdapter(mFacebookGroupsAdapter);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		// // first saving my state, so the bundle wont be empty.
		// // http://code.google.com/p/android/issues/detail?id=19917
		// outState.putString("WORKAROUND_FOR_BUG_19917_KEY",
		// "WORKAROUND_FOR_BUG_19917_VALUE");
		// super.onSaveInstanceState(outState);
	}

	protected void loadGroupUserThumbs(View v) {

		if (pictureModel == null) {
			pictureModel = new FriendsGetProfilePics();
			pictureModel.setListener(mFacebookGroupsAdapter);
		}
		String FBgroupId = (String) (v.findViewById(R.id.imgbtnGroupItem))
				.getTag();
		List<StringPair> FBGroupMembers = FBgroupUsersPics.get(FBgroupId);

		List<ImageView> ivs = new ArrayList<ImageView>();
		ivs.add((ImageView) v.findViewById(R.id.pic1));
		ivs.add((ImageView) v.findViewById(R.id.pic2));
		ivs.add((ImageView) v.findViewById(R.id.pic3));
		ivs.add((ImageView) v.findViewById(R.id.pic4));
		if (FBGroupMembers != null) {
			for (int i = 0; i < FBGroupMembers.size(); i++) {
				StringPair rf = FBGroupMembers.get(i);
				if (rf != null && rf.isInitialized()) {
					ivs.get(i).setImageBitmap(
							pictureModel.getImage(rf.getId(), rf.getUrl(),
									RalleeApp.getInstance()));
//					Log.i(FRAGMENT_TAG, "setImageBitmap: " + rf.toString());
				} else {
					ivs.get(i).setImageResource(R.drawable.icon);
				}
			}
		} else {
			for (int i = 0; i < 4; i++) {
				ivs.get(i).setImageResource(R.drawable.icon);
			}
		}
	}

	protected void changeGroupGlow(View view) {

		if (getRRService() != null) {
			ImageView glow = (ImageView) view.findViewById(R.id.profile_pic);
			if (glow != null) {
				String groupname = (String) glow.getTag();
				boolean isOnline = false;

				for (RRChannels ch : getRRService().getChannelList()) {

					if (ch.name != null && ch.name.equals(groupname)) {
						isOnline = true;
						break;
					}
				}
				if (isOnline) {
					glow.setBackgroundResource(R.color.cyan_active_group);
				} else {
					glow.setBackgroundResource(0);
				}

			}
		}
	}

	private ViewBinder vbFacebookGroups() {
		return new ViewBinder() {

			@Override
			public boolean setViewValue(View view, Cursor cursor,
					int columnIndex) {
				switch (view.getId()) {
				case R.id.imgbtnGroupItem:
					view.setTag(cursor.getString(columnIndex));
					return true;

				case R.id.profile_pic:
					String groupid = "_fbgroup_"
							+ cursor.getString(columnIndex);
					((ImageView) view).setTag(groupid);
					return true;
				}
				return false;
			}
		};
	}

	@Override
	public Loader<Cursor> onCreateLoader(int loaderId, Bundle bundle) {
		Uri baseUri = DbContentProvider.CONTENT_URI_FB_GROUPS_TABLE;
		String sortOrder = DbContentProvider.FB_GROUP_COL_NAME + " ASC";
		return new CursorLoader(RalleeApp.getInstance(), baseUri, null, null,
				null, sortOrder);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, final Cursor data) {
		mFacebookGroupsAdapter.swapCursor(data);
		if (data.getCount() > 0) {
			final List<String> newData = new ArrayList<String>();
			data.moveToFirst();
			int columnIndex = data
					.getColumnIndex(DbContentProvider.FB_GROUP_COL_ID);
			do {
				String groupId = data.getString(columnIndex);
				if (!FBgroupUsersPics.containsKey(groupId)) {
					newData.add(groupId);
				}
			} while (data.moveToNext());

			Thread t = new Thread(new Runnable() {

				@Override
				public void run() {
					loadGroupUsersPics(newData);
				}
			});
			t.start();
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		mFacebookGroupsAdapter.swapCursor(null);
	}

	private void loadGroupUsersPics(List<String> newData) {

		for (int j = 0; j < newData.size(); j++) {
			final String groupId = newData.get(j);
			Cursor c = null;
			try {
				Bundle params = new Bundle();

				// Svi FB prijatelji koji su u grupi

				// String getFriendMembersQuery =
				// "SELECT uid FROM group_member WHERE gid = "
				// + data.getString(columnIndex)
				// +
				// " AND uid IN ( SELECT uid2 FROM friend WHERE uid1=me()) ORDER BY rand() LIMIT 4";

				// FB prijatelji koji su u odabranoj grupi i imaju instaliran
				// Rallee
				String getFriendMembersQuery = "SELECT uid FROM user WHERE is_app_user='true' AND uid IN "
						+ "( SELECT uid FROM group_member WHERE gid = "
						+ groupId
						+ " AND uid IN ( SELECT uid2 FROM friend WHERE uid1=me()) ) ORDER BY rand() LIMIT 4";

//				Log.i("FBGroupsList", getFriendMembersQuery);
				params.putString("q", getFriendMembersQuery);

				Session session = Session.getActiveSession();
				Request request = new Request(session, "/fql", params,
						HttpMethod.GET);
				Response response = null;

				response = Request.executeAndWait(request);
				if (response != null && response.getError() == null) {

					GraphObject data = response.getGraphObject();
					JSONArray membersIds = ((JSONArray) data.asMap()
							.get("data"));

//					Log.i("fbGroups", response.toString());
					String selection = "";
					if (membersIds.length() > 0) {
						selection = DbContentProvider.USER_COL_ID;
						for (int i = 0; i < membersIds.length(); i++) {
							if (i == 0) {
								selection += " IN ('"
										+ Utility.networkPrefix
										+ membersIds.getJSONObject(i)
												.getString("uid") + "'";
							} else {
								selection += ", '"
										+ Utility.networkPrefix
										+ membersIds.getJSONObject(i)
												.getString("uid") + "'";
							}
						}
						selection += ")";

					}
					final String select = selection;
					mHandler.post(new Runnable() {

						@Override
						public void run() {
							updateOneGroupPic(groupId, select);
						}
					});
				}

			} catch (JSONException e) {
				e.printStackTrace();
			} catch (FacebookException fbe) {
				fbe.printStackTrace();
			}

		}

		mHandler.post(new Runnable() {

			@Override
			public void run() {

				mFacebookGroupsAdapter.notifyDataSetChanged();

			}
		});
	}

	public void updateOneGroupPic(String groupId, String selection) {
		Cursor c = null;
		if (!selection.equals("")) {
			c = RalleeApp
					.getInstance()
					.getContentResolver()
					.query(DbContentProvider.CONTENT_URI_USER_TABLE,
							new String[] { DbContentProvider.USER_COL_ID,
									DbContentProvider.USER_COL_PIC_URL },
							selection, null, null);
		}

		List<StringPair> FBGroupMembers = new ArrayList<StringPair>(4);
		if (c != null) {
//			Log.i(FRAGMENT_TAG, c.toString());

			for (int i = 0; i < 4; i++) {
				StringPair sp = new StringPair();

				if (c.moveToPosition(i)) {
					sp.setId(c.getString(c
							.getColumnIndex(DbContentProvider.USER_COL_ID)));
					sp.setUrl(c.getString(c
							.getColumnIndex(DbContentProvider.USER_COL_PIC_URL)));
				}
				FBGroupMembers.add(sp);

			}

		} else {
			for (int i = 0; i < 4; i++) {
				StringPair sp = new StringPair();
				FBGroupMembers.add(sp);
			}
		}

		if (c != null && !c.isClosed()) {
			c.close();
		}

		FBgroupUsersPics.put(groupId, FBGroupMembers);
//		Log.i(FRAGMENT_TAG, groupId + " " + FBGroupMembers.toString());
	}

	public void notifyFBgroupsChange() {
		mFacebookGroupsAdapter.notifyDataSetChanged();
	}

	protected void showGroupsDetails(String channelName) {
		FacebookGroupDetailsFragment fgdfrag = new FacebookGroupDetailsFragment();

		Bundle bundle = new Bundle();
		bundle.putString(FacebookGroupsListFragment.URI,
				DbContentProvider.CONTENT_URI_FB_GROUPS_TABLE + "/"
						+ channelName);
		if (FBgroupUsersPics.containsKey(channelName)) {
			List<StringPair> channelPics = FBgroupUsersPics.get(channelName);

			ArrayList<String> ids = new ArrayList<String>(4);
			ArrayList<String> urls = new ArrayList<String>(4);

			for (int i = 0; i < 4; i++) {
				ids.add(i, channelPics.get(i).getId());
				urls.add(i, channelPics.get(i).getUrl());
			}
			bundle.putStringArrayList(UIDS, ids);
			bundle.putStringArrayList(URLS, urls);
		}
		if (getHomeActivity() != null) {
			getHomeActivity().googleTracker("groups", "fbGroup", "showDetails",
					0);
			getHomeActivity().setScreenAnalytics("GroupsDetails-FBgroups");
		}
		fgdfrag.setArguments(bundle);
		FragmentTransaction ft = getFragmentManager()
				.beginTransaction()
				.setCustomAnimations(R.anim.slide_right_in,
						R.anim.slide_left_out, R.anim.slide_left_in,
						R.anim.slide_right_out)
				.replace(R.id.topSlot, fgdfrag,
						FacebookGroupDetailsFragment.FRAGMENT_TAG);
		ft.addToBackStack(null);
		// ft.commit();
		ft.commitAllowingStateLoss();
	}

	private HomeActivity getHomeActivity() {
		return (HomeActivity) getActivity();
	}

	private RadioRuntService getRRService() {
		if (getHomeActivity() == null) {
			return null;
		} else {
			return getHomeActivity().getRRService();
		}
	}
}
