package com.radiorunt.activities.fragments;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter.ViewBinder;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.radiorunt.R;
import com.radiorunt.activities.DbContentProvider;
import com.radiorunt.activities.HomeActivity;
import com.radiorunt.businessobjects.RRUser;
import com.radiorunt.facebook.FriendsGetProfilePics;
import com.radiorunt.services.GetGroupsFromFBService;
import com.radiorunt.services.RadioRuntService;
import com.radiorunt.utilities.CallState;
import com.radiorunt.utilities.ImageHelper;
import com.radiorunt.utilities.RalleeApp;

public class PeopleFragment extends Fragment implements
		LoaderManager.LoaderCallbacks<Cursor>, OnClickListener {

	/**
	 * Tag that can be used to identify fragment within activity manager
	 */
	public static final String FRAGMENT_TAG = "people";
	/**
	 * Position of static button Add Facebook group in Groups gallery
	 */
	public static final int GALLERY_STATIC_ITEM_ID_ADD_FB_GROUP = 0;
	// public static final int GALLERY_STATIC_ITEM_ID_MY_CONTACTS = 1;

	/**
	 * id of grid's contacts loader
	 */
	private static final int LOADER_GRID_CONTACTS = 0;
	/**
	 * contacts grid view
	 */
	private GridView gvContacts;
	/**
	 * text view that is used for filtering friends
	 */
	EditText etFilter;
	/**
	 * progress bar that shows loading of contacts into grid
	 */
	ProgressBar pbGridLoading;
	/**
	 * text view when there are no matchs
	 */
	TextView tvSearchNoMatch;
	/**
	 * data adapter for contacts grid
	 */
	SimpleCursorAdapter mGridAdapter;
	/**
	 * the handler for PeopleFragments UI thread
	 */
	Handler mHandler;
	/**
	 * the dialog for alerts
	 */
	private AlertDialog alertDialog;
	/**
	 * dialog alert builder
	 */
	private Builder builder;
	/**
	 * filtering string
	 */
	String mCurFilter = "";

	private FriendsGetProfilePics contactsPictureModel;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View fragmentView = inflater.inflate(R.layout.people, container, false);

		fragmentView.setOnClickListener(this);
		return fragmentView;
	}

	@Override
	public void onStart() {
		super.onStart();

		mHandler = new Handler();

		initControls();
		initControlListeners();

		// Prepare the loader. Either re-connect with an existing one,
		// or start a new one.
		getLoaderManager().initLoader(LOADER_GRID_CONTACTS, null, this);

		// Intent service = new Intent(getActivity(),
		// GetGroupsFromFBService.class);
		// getActivity().startService(service);
	}

	@Override
	public void onResume() {
		super.onResume();
		restoreMyContactsState();
	}

	/**
	 * method that initializes ui controls of this fragment
	 */
	private void initControls() {

		// set view variables
		gvContacts = (GridView) getView().findViewById(R.id.gvContacts);
		etFilter = (EditText) getView().findViewById(R.id.etFilter);
		tvSearchNoMatch = (TextView) getView().findViewById(
				R.id.tvSearchNoMatch);

		// set initial parameters
		pbGridLoading = (ProgressBar) getView()
				.findViewById(R.id.pbGridLoading);
		pbGridLoading.setVisibility(View.GONE);
		tvSearchNoMatch.setVisibility(View.GONE);
	}

	/**
	 * method that initializes listeners for controls
	 */
	private void initControlListeners() {
		// Search
		TextWatcher searchWatcher = new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				String oldFilter = mCurFilter;
				mCurFilter = etFilter.getText().toString();

				if (mCurFilter != null && !mCurFilter.equals(oldFilter)) {
					restoreMyContactsState();
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
			}
		};

		etFilter.addTextChangedListener(searchWatcher);

		OnEditorActionListener editorAction = new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId,
					KeyEvent event) {
				if (event != null
						&& (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
					hideSoftKeypad();
				}
				return false;
			}
		};

		etFilter.setOnEditorActionListener(editorAction);

		// Grid View
		mGridAdapter = createGridContactsAdapter();
		gvContacts.setAdapter(mGridAdapter);

		gvContacts.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				hideSoftKeypad();
				contactUser(parent, view, position, id);
			}
		});

		// swipe detector
		View.OnTouchListener mListener = getHomeActivity().getFlipDetector();
		((ImageView) getView().findViewById(R.id.imgPeopleFakeSide))
				.setOnTouchListener(mListener);

	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		// // first saving my state, so the bundle wont be empty.
		// // http://code.google.com/p/android/issues/detail?id=19917
		// outState.putString("WORKAROUND_FOR_BUG_19917_KEY",
		// "WORKAROUND_FOR_BUG_19917_VALUE");
		// super.onSaveInstanceState(outState);
	}

	/**
	 * @return adapter for contacts data
	 */
	private SimpleCursorAdapter createGridContactsAdapter() {
		SimpleCursorAdapter adapter = new SimpleCursorAdapter(getActivity(),
				R.layout.user_list_item, null, new String[] {
						DbContentProvider.USER_COL_NAME,
						DbContentProvider.USER_COL_PIC_URL,
						DbContentProvider.USER_COL_ID }, new int[] {
						R.id.title, R.id.profile_pic, R.id.img_led_status }, 0) {
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				// TODO Auto-generated method stub
				View v = super.getView(position, convertView, parent);
				changeItemsViews(v);
				return v;
			}
		};

		contactsPictureModel = new FriendsGetProfilePics();
		contactsPictureModel.setListener(adapter);

		adapter.setViewBinder(new ViewBinder() {

			@Override
			public boolean setViewValue(View view, final Cursor cursor,
					final int columnIndex) {
				if (view.getId() == R.id.profile_pic) {
					((ImageView) view).setImageBitmap(contactsPictureModel.getImage(
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

				if (view.getId() == R.id.img_led_status) {
					String userid = cursor.getString(columnIndex);
//					Log.i(FRAGMENT_TAG + "status", "setTag:" + userid);
					((ImageView) view).setTag(userid);
					return true;
				}
				return false;
			}
		});
		return adapter;
	}

	private void contactUser(AdapterView<?> parent, View view, int position,
			long id) {
		if (getHomeActivity() != null && getRRService() != null
				&& getRRService().getCurrentChannel() != null) {
			Cursor c = ((Cursor) parent.getItemAtPosition(position));
			if (c != null) {
				String userId = c.getString(c
						.getColumnIndex(DbContentProvider.USER_COL_ID));
				String user = c.getString(c
						.getColumnIndex(DbContentProvider.USER_COL_NAME));
				int currentChannelId = getRRService().getCurrentChannel().id;
				if (userId != null && user != null) {
					List<RRUser> users = getRRService().getUserList();
					boolean isOnline = false;
					boolean onMyChannel = false;
					if (users != null) {
						for (RRUser member : users) {
							if (member != null && member.userName != null
									&& member.userName.equals(userId)) {
								isOnline = true;
								if (currentChannelId != 0
										&& member.getChannel() != null
										&& member.getChannel().id == currentChannelId) {
									onMyChannel = true;
								}
								break;
							}
						}
					}

					if (isOnline) {
						if (onMyChannel) {
							// Toast.makeText(getActivity(),
							// user + getString(R.string.is_in_conversation),
							// 500).show();
						} else {
							if (CallState.sCurrent == CallState.INCALL) {
								getHomeActivity()./*
												 * clickActionUser(userId, user,
												 * view);
												 */addFriendToConversation(
										userId, user);
							} else if (CallState.sCurrent == CallState.NORMAL) {
								getHomeActivity()./*
												 * clickActionUser( userId,
												 * user, view);
												 */callFriend(userId, user);
							}
						}
					} else {
						getHomeActivity().callOfflineFriend(userId, user);
					}
				}
			}
		}
	}

	public void notifyFriendsAdapterDataSetChange() {
		// getLoaderManager().restartLoader(LOADER_GRID_CONTACTS, null, this);
		try {
			mGridAdapter.notifyDataSetChanged();
		} catch (IllegalStateException ise) {

		}
	}

	public Loader<Cursor> onCreateLoader(int id, Bundle b) {
		if (id == LOADER_GRID_CONTACTS) {

			// This is called when a new Loader needs to be created. This
			// sample only has one Loader, so we don't care about the ID.
			// First, pick the base URI to use depending on whether we are
			// currently filtering.

			pbGridLoading.setVisibility(View.VISIBLE);

			Uri baseUri = DbContentProvider.CONTENT_URI_USER_TABLE;

			// Now create and return a CursorLoader that will take care of
			// creating a Cursor for the data being displayed.
			String select = " ( " + DbContentProvider.USER_COL_INSTALLED
					+ " = '1'"; // 1 is for true, 0 for false
			String additionalSelection = "";
			if (b != null) {
				additionalSelection = b.getString("additionalSelection");
			}
			if (additionalSelection != null && !additionalSelection.equals("")) {
				select = select + " AND " + additionalSelection + " )";
			} else {
				select += " )";
			}

//			Log.i("fbGroups", "select: " + select);
			String order = DbContentProvider.USER_COL_NAME
					+ " COLLATE LOCALIZED ASC";
			return new CursorLoader(RalleeApp.getInstance(), baseUri, null,
					select, null, order);
		} else {
			return null;
		}
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		// Swap the new cursor in. (The framework will take care of closing the
		// old cursor once we return.)
		int loaderId = loader.getId();
		if (loaderId == LOADER_GRID_CONTACTS) {
			pbGridLoading.setVisibility(View.GONE);
			if (data.getCount() > 0) {
				tvSearchNoMatch.setVisibility(View.GONE);
			} else {
				tvSearchNoMatch.setVisibility(View.VISIBLE);
			}
			mGridAdapter.swapCursor(data);
		} else {
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		// This is called when the last Cursor provided to onLoadFinished()
		// above is about to be closed. We need to make sure we are no
		// longer using it.

		int loaderId = loader.getId();
		if (loaderId == LOADER_GRID_CONTACTS) {
			pbGridLoading.setVisibility(View.VISIBLE);
			tvSearchNoMatch.setVisibility(View.GONE);
			mGridAdapter.swapCursor(null);
		} else {
		}
	}

	// ==================Utilitiy Methods==========================
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

	void restoreMyContactsState() {

		String additionalSelection = DbContentProvider.USER_COL_NAME
				+ " like'%" + mCurFilter + "%'";
		Bundle b = new Bundle();

		b.putString("additionalSelection", additionalSelection);

		getLoaderManager().restartLoader(LOADER_GRID_CONTACTS, b,
				PeopleFragment.this);

	}

	@Override
	public void onClick(View v) {
		if (v.getId() != R.id.gvContacts) {

//			Log.i("People", "clicked: My Contacts");

			if (!hideSoftKeypad()) {
				etFilter.setText("");
			}
			// restoreMyContactsState();
		}
	}

	private void changeItemsViews(View view) {

		TextView tv = ((TextView) view.findViewById(R.id.title));
		ImageView imgStatus = ((ImageView) view
				.findViewById(R.id.img_led_status));
		FrameLayout darkenView = ((FrameLayout) view
				.findViewById(R.id.darkenView));

		RelativeLayout picLayout = ((RelativeLayout) view
				.findViewById(R.id.pic_layout));
		List<RRUser> onlineUserList;
		if (getRRService() != null && getRRService().getUserList() != null) {
			onlineUserList = getRRService().getUserList();
		} else {
			onlineUserList = new ArrayList<RRUser>();
		}
		int currentChannelId = 0;
		if (getRRService() != null
				&& getRRService().getCurrentChannel() != null) {
			currentChannelId = getRRService().getCurrentChannel().id;
		}
		boolean onMyChannel = false;
		boolean isOnline = false;
		if (tv != null) {
			String userId = (String) tv.getTag();
			if (userId != null && onlineUserList != null) {
				for (RRUser member : onlineUserList) {
					if (member != null && member.userName != null
							&& member.userName.equals(userId)) {
						isOnline = true;
						if (currentChannelId != 0
								&& member.getChannel() != null
								&& member.getChannel().id == currentChannelId) {
							onMyChannel = true;
						}
						break;
					}
				}
			}
		}
		if (onMyChannel) {
			if (tv != null) {
				tv.setTextColor(Color.GRAY);
				if (darkenView != null) {
					darkenView.setVisibility(View.VISIBLE);
				}
				if (picLayout != null) {
					picLayout.setBackgroundColor(Color.CYAN);
				}
			}
		} else {
			if (tv != null) {
				tv.setTextColor(Color.WHITE);
				if (darkenView != null) {
					darkenView.setVisibility(View.GONE);
				}
				if (picLayout != null) {
					picLayout.setBackgroundColor(Color.TRANSPARENT);
				}
			}
		}

		if (imgStatus != null) {
			String username = (String) imgStatus.getTag();
			if (isOnline) {
				imgStatus.setImageResource(R.drawable.main_ui_speaker_led);
			} else {
				imgStatus.setImageResource(R.drawable.main_ui_mic_led);
			}
		}
	}

	protected boolean hideSoftKeypad() {
		if (getActivity() == null) {
			return false;
		}
		InputMethodManager mImm = (InputMethodManager) getActivity()
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		if (mImm == null) {
			return false;
		}
		return mImm.hideSoftInputFromWindow(
				etFilter.getApplicationWindowToken(), 0);
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
