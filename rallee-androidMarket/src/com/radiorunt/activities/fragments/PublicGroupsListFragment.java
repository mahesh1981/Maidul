package com.radiorunt.activities.fragments;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SimpleCursorTreeAdapter;
import android.widget.SimpleCursorTreeAdapter.ViewBinder;
import android.widget.TextView;

import com.radiorunt.R;
import com.radiorunt.activities.DbContentProvider;
import com.radiorunt.activities.HomeActivity;
import com.radiorunt.activities.MwCommunicationLogic.GetParticipantsServiceReceiver;
import com.radiorunt.businessobjects.RRChannels;
import com.radiorunt.services.RadioRuntService;
import com.radiorunt.utilities.Globals;
import com.radiorunt.utilities.RalleeApp;

public class PublicGroupsListFragment extends Fragment implements
		LoaderCallbacks<Cursor> {

	/**
	 * Tag that can be used to identify fragment within activity manager
	 */
	public static final String FRAGMENT_TAG = "publicGroupsListFragment";

	private static final int CATEGORIES_LOADER = 0;
	private static final int CHANNELS_LOADER = 1;

	private SimpleCursorTreeAdapter mPublicGroupsAdapter;
	private ViewBinder viewBinderPublicGroupsAdapter;
	private ExpandableListView mExpandableListView;

	private OnClickListener childClickLIstener;

	private GetParticipantsServiceReceiver mGetParticipantsServiceReceiver;

	private Map<String, Cursor> childrenCursors = new HashMap<String, Cursor>();

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Globals.logInfo(this, "onCreateView");
		View v = inflater.inflate(R.layout.expandable_list, container, false);
		return v;
	}

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

	@Override
	public void onDestroy() {
		super.onDestroy();

		Set<Map.Entry<String, Cursor>> entries = childrenCursors.entrySet();
		for (Map.Entry<String, Cursor> entry : entries) {
			Cursor cursorToClose = entry.getValue();
			if (cursorToClose != null && !cursorToClose.isClosed()) {
				cursorToClose.close();
			}
		}
		childrenCursors.clear();
	}

	private void initControls() {
		Globals.logInfo(this, "initControls");
		mExpandableListView = (ExpandableListView) getView().findViewById(
				R.id.expadableListview);

		DisplayMetrics metrics = new DisplayMetrics();
		getActivity().getWindowManager().getDefaultDisplay()
				.getMetrics(metrics);
		int width = metrics.widthPixels;
		// this code for adjusting the group indicator into right side of the
		// view
		// mExpandableListView.setIndicatorBounds(width -
		// GetDipsFromPixel(40),width - GetDipsFromPixel(10));
		DisplayMetrics dm = new DisplayMetrics();
		getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);

		/*
		 * Code block for tab and kindle *
		 */
		if ((dm.widthPixels >= 550) && (dm.heightPixels >= 900)) {
			mExpandableListView.setIndicatorBounds(
					width - GetDipsFromPixel(58), width - GetDipsFromPixel(5));
		}

		/*
		 * End of code *
		 */
		else {
			mExpandableListView.setIndicatorBounds(
					width - GetDipsFromPixel(40), width - GetDipsFromPixel(10));
		}
	}

	private void initControlListeners() {
		Globals.logInfo(this, "initControlListeners");

		viewBinderPublicGroupsAdapter = new ViewBinder() {

			@Override
			public boolean setViewValue(View view, Cursor cursor,
					int columnIndex) {
				// TODO Auto-generated method stub
				switch (view.getId()) {
				case R.id.tvGroupItemName:
					String channelUntrimmedName = cursor.getString(columnIndex);
					((TextView) view).setText(channelUntrimmedName.subSequence(
							5, channelUntrimmedName.length()));
					return true;

				case R.id.imgbtnGroupItem:
					view.setTag(cursor.getString(columnIndex));
					return true;

				case R.id.imgGroupItem:
					view.setTag(cursor.getString(columnIndex));
					return true;
				default:
					return false;
				}

			}
		};

		registerForContextMenu(mExpandableListView);
		// mExpandableListView.setOnChildClickListener(new
		// OnChildClickListener() {
		// @Override
		// public boolean onChildClick(ExpandableListView parent, View v,
		// int groupPosition, int childPosition, long id) {
		//
		// Log.i(FRAGMENT_TAG, "Child " + groupPosition + ", "
		// + childPosition + " is clicked");
		//
		// showGroupsDetails("_prm_Animals");
		//
		// return false;
		// }
		// });

		childClickLIstener = new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				String channelName = (String) v.getTag();
//				Log.i(FRAGMENT_TAG, "Child " + v + " is clicked");
				if (channelName != null && !channelName.equals("")) {
//					Log.i(FRAGMENT_TAG, channelName);
					showGroupsDetails(channelName);
				}
			}
		};

		if (mPublicGroupsAdapter != null) {
			mPublicGroupsAdapter.setViewBinder(viewBinderPublicGroupsAdapter);
			mExpandableListView.setAdapter(mPublicGroupsAdapter);
		}

	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		// // first saving my state, so the bundle wont be empty.
		// // http://code.google.com/p/android/issues/detail?id=19917
		// outState.putString("WORKAROUND_FOR_BUG_19917_KEY",
		// "WORKAROUND_FOR_BUG_19917_VALUE");
		// super.onSaveInstanceState(outState);
	}

	private SimpleCursorTreeAdapter createPublicGroupsAdapter(Cursor c) {
		return new SimpleCursorTreeAdapter(getActivity(), c,
				R.layout.category_groups_list_item,
				new String[] { DbContentProvider.CHANNEL_CATEGORY_COL_NAME },
				new int[] { R.id.tvCategoryName },
				R.layout.public_groups_list_item, new String[] {
						DbContentProvider.CHANNEL_COL_NAME,
						DbContentProvider.CHANNEL_COL_NAME,
						DbContentProvider.CHANNEL_COL_NAME }, new int[] {
						R.id.tvGroupItemName, R.id.imgbtnGroupItem,
						R.id.imgGroupItem }) {

			@Override
			protected Cursor getChildrenCursor(Cursor groupCursor) {
				String groupCategory = groupCursor
						.getString(groupCursor
								.getColumnIndex(DbContentProvider.CHANNEL_CATEGORY_COL_ID));
				Cursor c = childrenCursors.get(groupCategory);
				if (c == null || (c != null && c.isClosed())) {
					c = RalleeApp
							.getInstance()
							.getApplicationContext()
							.getContentResolver()
							.query(DbContentProvider.CONTENT_URI_CHANNEL_TABLE,
									null,
									DbContentProvider.CHANNEL_COL_CATEGORY
											+ "=?",
									new String[] { groupCategory }, null);

					childrenCursors.put(groupCategory, c);
				}
				return c;
			}

			@Override
			public View getChildView(final int groupPosition,
					final int childPosition, boolean isLastChild,
					View convertView, ViewGroup parent) {
				View rowView = super.getChildView(groupPosition, childPosition,
						isLastChild, convertView, parent);
				changeGroupGlow(rowView);

				ImageButton ibEnter = (ImageButton) rowView
						.findViewById(R.id.imgbtnGroupItem);
				if (ibEnter != null) {
					ibEnter.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							String channelName = (String) v.getTag();
							if (getHomeActivity() != null) {
								getHomeActivity().enterPublicChannel(
										channelName);
							}
						}
					});
				}
				;

				Cursor child = getChild(groupPosition, childPosition);
				rowView.setTag(child.getString(child
						.getColumnIndex(DbContentProvider.CHANNEL_COL_NAME)));
				rowView.setOnClickListener(childClickLIstener);

				return rowView;
			}

			@Override
			public View getGroupView(int groupPosition, boolean isExpanded,
					View convertView, ViewGroup parent) {
				View groupView = super.getGroupView(groupPosition, isExpanded,
						convertView, parent);

				ImageView glow = (ImageView) groupView
						.findViewById(R.id.imgCategory);
				if (glow != null) {

					boolean isOnline = false;
					Cursor groupCursor = super.getGroup(groupPosition);

					if (groupCursor != null && !groupCursor.isClosed()
							&& groupCursor.getCount() > 0) {
						String groupCategory = groupCursor
								.getString(groupCursor
										.getColumnIndex(DbContentProvider.CHANNEL_CATEGORY_COL_ID));
						Cursor childCursor = null;
						try {
							childCursor = RalleeApp
									.getInstance()
									.getContentResolver()
									.query(DbContentProvider.CONTENT_URI_CHANNEL_TABLE,
											null,
											DbContentProvider.CHANNEL_COL_CATEGORY
													+ "=?",
											new String[] { groupCategory },
											null);

							if (childCursor != null
									&& childCursor.moveToFirst()) {
								do {
									String groupname = childCursor
											.getString(childCursor
													.getColumnIndex(DbContentProvider.CHANNEL_COL_NAME));

									if (getRRService() != null) {
										for (RRChannels ch : getRRService()
												.getChannelList()) {

											if (ch.name != null
													&& ch.name
															.equals(groupname)) {
												if (ch.userCount > 0) {
													isOnline = true;
												} else {
													isOnline = false;
												}
												break;
											}
										}
									}
									if (isOnline) {
										break;
									}
								} while (childCursor.moveToNext());
							}
						} finally {
							if (childCursor != null && !childCursor.isClosed()) {
								childCursor.close();
							}
						}
					}

					if (isOnline) {
						glow.setImageResource(R.drawable.main_ui_friends_well_channels_icon_on_glow);
					} else {
						glow.setImageResource(R.drawable.main_ui_friends_well_channels_icon_on);
					}
				}

				return groupView;
			}

			@Override
			public boolean isChildSelectable(int groupPosition,
					int childPosition) {
				boolean selectable = super.isChildSelectable(groupPosition,
						childPosition);
				if (selectable) {
//					Log.i(FRAGMENT_TAG, "Child " + groupPosition + ", "
//							+ childPosition + " is selectable");
					// Cursor c = mPublicGroupsAdapter.getChild(groupPosition,
					// childPosition);
					// showGroupsDetails(c.getString(c.getColumnIndex(DbContentProvider.CHANNEL_COL_NAME)));
				} else {
//					Log.i(FRAGMENT_TAG, "Child " + groupPosition + ", "
//							+ childPosition + " is NOT selectable");
				}
				return selectable;
			}
		};

	}

	@Override
	public Loader<Cursor> onCreateLoader(int loaderId, Bundle bundle) {
		if (loaderId == CATEGORIES_LOADER) {
			Uri baseUri = DbContentProvider.CONTENT_URI_CHANNEL_CATEGORY_TABLE;
			String sortOrder = null;
			return new CursorLoader(RalleeApp.getInstance(), baseUri, null,
					null, null, sortOrder) {

			};

		} else if (loaderId == CHANNELS_LOADER) {
			return null;
		} else {
			return null;
		}

	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

		if (loader.getId() == CATEGORIES_LOADER) {

			mPublicGroupsAdapter = createPublicGroupsAdapter(data);
			mPublicGroupsAdapter.setViewBinder(new ViewBinder() {

				@Override
				public boolean setViewValue(View view, Cursor cursor,
						int columnIndex) {
					// TODO Auto-generated method stub
					return false;
				}
			});
			if (mPublicGroupsAdapter != null) {
				mPublicGroupsAdapter
						.setViewBinder(viewBinderPublicGroupsAdapter);
				mExpandableListView.setAdapter(mPublicGroupsAdapter);
			}

		} else if (loader.getId() == CHANNELS_LOADER) {

		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		// TODO Auto-generated method stub
		mPublicGroupsAdapter.setGroupCursor(null);
	}

	public int GetDipsFromPixel(float pixels) {
		// Get the screen's density scale
		final float scale = getResources().getDisplayMetrics().density;
		// Convert the dps to pixels, based on density scale
		return (int) (pixels * scale + 0.5f);
	}

	protected void changeGroupGlow(View view) {

		ImageView glow = (ImageView) view.findViewById(R.id.imgGroupItem);
		if (glow != null) {
			String groupname = (String) glow.getTag();
			boolean isOnline = false;

			if (getRRService() != null) {
				for (RRChannels ch : getRRService().getChannelList()) {

					if (ch.name != null && ch.name.equals(groupname)) {

						if (ch.userCount > 0) {
							isOnline = true;
						} else {
							isOnline = false;
						}
						break;
					}
				}
			}
			if (isOnline) {
				glow.setImageResource(R.drawable.main_ui_friends_well_channels_icon_on_glow);
			} else {
				glow.setImageResource(R.drawable.main_ui_friends_well_channels_icon_on);
			}
		}
	}

	public void notifyChannelStateChanged() {
		if (mPublicGroupsAdapter != null) {
			mPublicGroupsAdapter.notifyDataSetChanged();
		}
	}

	protected void showGroupsDetails(String channelName) {
		if (getHomeActivity() != null) {
			getHomeActivity().googleTracker("groups", "publicGroup",
					"showDetails", 0);
			getHomeActivity().setScreenAnalytics("GroupsDetails-PublicGroup");
		}
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

	private HomeActivity getHomeActivity() {
		return ((HomeActivity) getActivity());
	}

	private RadioRuntService getRRService() {
		if (getHomeActivity() == null) {
			return null;
		} else {
			return getHomeActivity().getRRService();
		}

	}

}
