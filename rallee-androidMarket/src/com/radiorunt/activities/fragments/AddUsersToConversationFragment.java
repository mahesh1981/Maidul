package com.radiorunt.activities.fragments;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter.ViewBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.ScaleAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.TextView;

import com.radiorunt.R;
import com.radiorunt.activities.DbContentProvider;
import com.radiorunt.activities.HomeActivity;
import com.radiorunt.businessobjects.RRUser;
import com.radiorunt.facebook.FriendsGetProfilePics;
import com.radiorunt.services.GetGroupsFromFBService;
import com.radiorunt.services.RadioRuntService;
import com.radiorunt.utilities.CallState;
import com.radiorunt.utilities.Globals;
import com.radiorunt.utilities.RalleeApp;

public class AddUsersToConversationFragment extends Fragment implements
		LoaderManager.LoaderCallbacks<Cursor>, OnClickListener {

	/**
	 * Tag that can be used to identify fragment within activity manager
	 */
	public static final String FRAGMENT_TAG = "addUsersToConversationFragment";
	/**
	 * ui gallery which contains controls for presentation and manipulations
	 * with contatcs
	 */
	Gallery gallery;
	/**
* 
*/
	// private FriendsAdapter mAdapter;

	/**
	 * adapter of friends on channel
	 */
	private SimpleCursorAdapter mUsersAdapter;
	private ScaleAnimation shrinkByYAxis;
	private ScaleAnimation expandByYAxis;
	private boolean galleryShrinked = false;
	private FriendsGetProfilePics usersPictureModel;

	/**
	 * list of usernames of users that are on channel
	 */
	private List<String> friendsOnChannel = new ArrayList<String>();

	private static final int LOADER_FRIENDS = 1;

	private static final int RESET_USER_STATE = 1;
	private static final String USER_ID = "userId";
	private Handler mHandler;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Globals.logInfo(this, "onCreateView");
		View v = inflater.inflate(R.layout.online_friends, container, false);
		return v;
	}

	@Override
	public void onStart() {
		Globals.logInfo(this, "onStart");
		super.onStart();
		initControls();
		initControlListeners();

		getLoaderManager().restartLoader(LOADER_FRIENDS, null, this);
	}

	@Override
	public void onResume() {
		Globals.logInfo(this, "onResume");
		super.onResume();
		notifyFriendsAdapterDataSetChange();
	}

	private void initControls() {
		friendsOnChannel = new ArrayList<String>();

		gallery = (Gallery) getView().findViewById(R.id.galleryOfOnlineFriends);

		mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				if (msg.what == RESET_USER_STATE) {
					Bundle b = msg.getData();
					if (b != null) {
						String userId = b.getString(USER_ID);
						if (userId != null && CallState.calledFriends != null) {
							CallState.calledFriends.remove(userId);
						}
					}
				}
			}
		};

		// createAnimations();

		// mAdapter = new FriendsAdapter(getActivity(), /* 3 */4, 1);
		// gallery.setAdapter(mAdapter);
		mUsersAdapter = createOnlineParticipantsAdapter();
		// Utility.model = new FriendsGetProfilePics();
		// Utility.model.setListener(mOnlineFriendsAdapter);

		gallery.setAdapter(mUsersAdapter);
		gallery.setCallbackDuringFling(false);

	}

	private void initControlListeners() {

		gallery.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {

				// Toast.makeText(getActivity(), "Call user", 500).show();
				Cursor c = ((Cursor) parent.getItemAtPosition(position));
				if (c != null) {
					String username = c.getString(c
							.getColumnIndex(DbContentProvider.USER_COL_ID));
					String firstname = c.getString(c
							.getColumnIndex(DbContentProvider.USER_COL_NAME));

					if (username != null && firstname != null
							&& !username.equals("") && !firstname.equals("")) {

						for (String memberId : friendsOnChannel) {
							if (memberId.equals(username)) {
								// Toast.makeText(getActivity(), firstname +
								// getString(R.string.is_in_conversation),
								// 500).show();
								return;
							}
						}
						for (String memberId : CallState.calledFriends) {
							if (memberId.equals(username)) {
								// Toast.makeText(getActivity(), firstname +
								// getString(R.string.is_in_conversation),
								// 500).show();
								return;
							}
						}
						if (getHomeActivity() != null) {
							getHomeActivity().addFriendToConversation(username,
									firstname);
						}
					}
				}

			}
		});

	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		// // first saving my state, so the bundle wont be empty.
		// // http://code.google.com/p/android/issues/detail?id=19917
		// outState.putString("WORKAROUND_FOR_BUG_19917_KEY",
		// "WORKAROUND_FOR_BUG_19917_VALUE");
		// super.onSaveInstanceState(outState);
	}

	protected void showOnlineFriends() {
		// Intent service = new Intent(getActivity(),
		// GetGroupsFromFBService.class);
		// getActivity().startService(service);

		FragmentTransaction ft = getFragmentManager().beginTransaction()
				.replace(R.id.bottomExtensionPanelSlot,
						new OnlineFriendsFragment(),
						OnlineFriendsFragment.FRAGMENT_TAG);
		ft.addToBackStack(null);
		// ft.commit();
		ft.commitAllowingStateLoss();
		getActivity().overridePendingTransition(0, 0);
	}

	private SimpleCursorAdapter createOnlineParticipantsAdapter() {
		SimpleCursorAdapter adapter = new SimpleCursorAdapter(getActivity(),
				R.layout.user_list_item, null, new String[] {
						DbContentProvider.USER_COL_NAME,
						DbContentProvider.USER_COL_PIC_URL }, new int[] {
						R.id.title, R.id.profile_pic }, 0) {
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				// TODO Auto-generated method stub
				View v = super.getView(position, convertView, parent);
				changeItemsViews(v);
				return v;
			}
		};

		usersPictureModel = new FriendsGetProfilePics();
		usersPictureModel.setListener(adapter);

		adapter.setViewBinder(new ViewBinder() {

			@Override
			public boolean setViewValue(View view, final Cursor cursor,
					final int columnIndex) {
				if (view.getId() == R.id.profile_pic) {
					((ImageView) view).setImageBitmap(usersPictureModel.getImage(
							cursor.getString(cursor
									.getColumnIndex(DbContentProvider.USER_COL_ID)),
							cursor.getString(columnIndex), RalleeApp
									.getInstance()));
					return true;
				}
				if (view.getId() == R.id.title) {
					((TextView) view).setText(trimToWholeFirstAndOneRest(cursor
							.getString(columnIndex)));
					((TextView) view).setTag(cursor.getString(cursor
							.getColumnIndex(DbContentProvider.USER_COL_ID)));
					return true;
				}
				return false;
			}
		});

		return adapter;
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
			// mAdapter.notifyDataSetChanged();
			// gallery.startAnimation(shrinkByYAxis);
			// galleryShrinked = true;
//			Log.i(FRAGMENT_TAG, "notifyFriendsAdapterDataSetChange");
			if (getRRService() == null) {
				return;
			}
			if (getRRService().getCurrentChannel() == null) {
				return;
			}
			if (getRRService().getUserList() == null) {
				return;
			}
			List<RRUser> rrUsers = getRRService().getUserList();
			friendsOnChannel = new ArrayList<String>();

			getLoaderManager().restartLoader(LOADER_FRIENDS, null, this);

			int currentChannelId = getRRService().getCurrentChannel().id;
			friendsOnChannel.clear();
			for (int i = 0; i < rrUsers.size(); i++) {
				RRUser user = rrUsers.get(i);
				if (user.getChannel().id == currentChannelId) {
					if (addIfNotDuplicate(user.userName)) {
//						Log.i(FRAGMENT_TAG, "On channel: " + user.userName);
						mUsersAdapter.notifyDataSetChanged();
					}
				}
			}

		} catch (IllegalStateException ise) {

		}
	}

	// private void createAnimations() {
	//
	// shrinkByYAxis = new ScaleAnimation(1.0f, 1.0f, 1.0f, 0.0f,
	// ScaleAnimation.RELATIVE_TO_PARENT, 0.5f,
	// ScaleAnimation.RELATIVE_TO_PARENT, 0.5f);
	// shrinkByYAxis.setDuration(500);
	//
	// expandByYAxis = new ScaleAnimation(1.0f, 1.0f, 0.0f, 1.0f,
	// ScaleAnimation.RELATIVE_TO_PARENT, 0.5f,
	// ScaleAnimation.RELATIVE_TO_PARENT, 0.5f);
	// expandByYAxis.setDuration(500);
	//
	// }

	public CharSequence trimToWholeFirstAndOneRest(String firstName) {
		String result = "";
		boolean prevWasBlank = false;
		char[] charArray = firstName.toCharArray();
		for (char c : charArray) {
			if (prevWasBlank && (c != ' ')) {
				prevWasBlank = false;
				result += " " + c + ".";
				break;
			} else {
				if (c == ' ') {
					prevWasBlank = true;
				} else {
					result += c;
				}
			}

		}
		return result;
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub

	}

	/**
	 * @param id
	 *            id that is used for cursor loader selection
	 * @param bundle
	 *            parameters for filtering and setup of data cursor
	 * @return Cursor loader for data selected by id
	 */
	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
		if (id == LOADER_FRIENDS) {
			// mUsersAdapter.swapCursor(null);
			if (getRRService() == null) {
				return null;
			}
			List<RRUser> rrUsers = getRRService().getUserList();
			friendsOnChannel = new ArrayList<String>();

			String selectionString = "_id IN (";
			for (int i = 0; i < rrUsers.size(); i++) {
				RRUser user = rrUsers.get(i);
				if (i == 0) {
					selectionString += "'" + user.userName + "'";
				} else {
					selectionString += ", '" + user.userName + "'";
				}

				if (user.getChannel().id == getRRService().getCurrentChannel().id) {
					addIfNotDuplicate(user.userName);
				}
			}

			selectionString += ")";

			Uri baseUri = DbContentProvider.CONTENT_URI_USER_TABLE;
			String select = null;

			// String selectionWhereIn = bundle.getString("idsOfOnlineUsers");
			String selectionWhereIn = selectionString;

			if (selectionWhereIn != null && !selectionWhereIn.equals("")) {
				select = selectionWhereIn;
//				Log.i(FRAGMENT_TAG, "selection part:" + select);
			}

			String order = DbContentProvider.USER_COL_NAME
					+ " COLLATE LOCALIZED ASC";
			return new CursorLoader(RalleeApp.getInstance(), baseUri, null,
					select, null, order);
		} else {
			return null;
		}
	}

	/**
	 * @param loader
	 *            which loading process is being finished
	 * @param data
	 *            cursor loader that loading process is being finished
	 */
	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		int loaderId = loader.getId();
		if (loaderId == LOADER_FRIENDS) {
			mUsersAdapter.swapCursor(data);
			mUsersAdapter.notifyDataSetChanged();
			// if (galleryShrinked) {
			// galleryShrinked = false;
			// gallery.startAnimation(expandByYAxis);
			// }
		}

	}

	/**
	 * @param loader
	 *            which loading process is being reseted
	 */
	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		int loaderId = loader.getId();
		if (loaderId == LOADER_FRIENDS) {
			mUsersAdapter.swapCursor(null);
		} else {
		}

	}

	private void changeItemsViews(View view) {

		TextView tv = ((TextView) view.findViewById(R.id.title));
		ImageView imgStatus = ((ImageView) view
				.findViewById(R.id.img_led_status));
		FrameLayout darkenView = ((FrameLayout) view
				.findViewById(R.id.darkenView));
		if (tv != null) {
			String itemID = (String) tv.getTag();
			if (itemID != null) {
				for (String memberId : CallState.calledFriends) {
					if (memberId.equals(itemID)) {
						tv.setTextColor(Color.GRAY);
						if (darkenView != null) {
							darkenView.setVisibility(View.VISIBLE);
						}
					}
				}
				for (String memberId : friendsOnChannel) {
					if (memberId.equals(itemID)) {
						tv.setTextColor(Color.GRAY);
						if (darkenView != null) {
							darkenView.setVisibility(View.VISIBLE);
						}
						return;
					}
				}
			}
		}
		if (tv != null) {
			tv.setTextColor(Color.WHITE);
			if (darkenView != null) {
				darkenView.setVisibility(View.GONE);
			}
		}
	}

	private boolean addIfNotDuplicate(String userId) {
		if (friendsOnChannel != null && !friendsOnChannel.contains(userId)) {
			friendsOnChannel.add(userId);
			if (CallState.calledFriends != null) {
				CallState.calledFriends.remove(userId);
			}
			return true;
		} else {
			return false;
		}
	}

	public boolean addCalledFriendsIfNotDuplicate(String userId) {
		if (CallState.calledFriends != null) {

			Message msg = new Message();
			Bundle b = new Bundle();
			b.putString(USER_ID, userId);
			msg.setData(b);
			msg.what = RESET_USER_STATE;
			mHandler.sendMessageDelayed(msg, 60 * 1000);
			return true;

		} else {
			return false;
		}
	}
}
