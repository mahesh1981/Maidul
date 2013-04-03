package com.radiorunt.activities.fragments;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import android.content.ContentValues;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter.ViewBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Gallery;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.radiorunt.R;
import com.radiorunt.activities.DbContentProvider;
import com.radiorunt.activities.HomeActivity;
import com.radiorunt.businessobjects.RRChannels;
import com.radiorunt.businessobjects.RRUser;
import com.radiorunt.facebook.FriendsGetProfilePics;
import com.radiorunt.services.RadioRuntService;
import com.radiorunt.utilities.Globals;
import com.radiorunt.utilities.RRServerProxyHelper;
import com.radiorunt.utilities.RalleeApp;

public class PrivateGroupDetailsFragment extends Fragment implements
		LoaderCallbacks<Cursor> {

	/**
	 * Tag that can be used to identify fragment within activity manager
	 */
	public static final String FRAGMENT_TAG = "privateGroupDetailsFragment";
	public static final String URI = "uri";
	public static final String MEMBERS = "members";
	public static final String MODE = "mode";

	private final int GROUP_DETAILS_LOADER = 0;
	private final int GROUP_MEMBERS_LOADER = 1;
	private final int USERS_LOADER = 2;

	public static final int MODE_VIEW = 0;
	public static final int MODE_EDIT = 1;
	public static final int MODE_ADD = 2;
	private int mode = MODE_VIEW;

	private ImageButton imgBtnEditSave;

	private TextView tvViewTitle;

	private TextView tvGroupName;

	private ImageButton imgBtnBack;

	private ImageView imgGroupImage;

	private TextView tvGroupDescription;

	private TextView tvNumberOfMembers;

	private Gallery galleryOfMembers;

	private SimpleCursorAdapter mUsersAdapter;

	// private MergeAdapter mMergeAdapter;

	private FriendsGetProfilePics channelDetailsPictureModel;

	String groupUriString = null;
	private FriendsGetProfilePics onlineFriendsPictureModel;
	private FriendsGetProfilePics usersPictureModel;

	// private ArrayList<String> nonFriendMembersOnline = new
	// ArrayList<String>();
	// private List<StringPair> nonFriendMembersOnlineStringPair = new
	// ArrayList<StringPair>(
	// 4);
	// private ArrayList<String> allParticipants;
	// private View separator;
	// private View nonFriendsIcon;
	private LinearLayout llButtonHolder;
	private ImageButton imgBtnReport;
	private ImageButton imgBtnEnter;
	private ScrollView descriptionContainer;
	private EditText etGroupName;
	private EditText etGroupDescription;

	ArrayList<String> selectedUsers = new ArrayList<String>();

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Globals.logInfo(this, "onCreateView");
		View v = inflater.inflate(R.layout.group_details, container, false);

		this.mode = this.getArguments().getInt(MODE, MODE_VIEW);

		v.setOnTouchListener(new View.OnTouchListener() {

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

	@Override
	public void onResume() {
		Globals.logInfo(this, "onResume");
		super.onResume();
		switchMode();
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	private void initControls() {
		Globals.logInfo(this, "initControls");
		imgBtnBack = (ImageButton) getView().findViewById(R.id.imgbtnBack);
		imgBtnEditSave = (ImageButton) getView().findViewById(
				R.id.imgbtnEditSave);
		tvViewTitle = (TextView) getView().findViewById(R.id.tvViewLabel);

		tvGroupName = (TextView) getView().findViewById(R.id.tvGroupName);
		etGroupName = (EditText) getView().findViewById(R.id.etGroupName);
		imgGroupImage = (ImageView) getView().findViewById(R.id.imgGroupImage);
		tvGroupDescription = (TextView) getView().findViewById(
				R.id.tvGroupDescription);
		etGroupDescription = (EditText) getView().findViewById(
				R.id.etGroupDescription);
		descriptionContainer = (ScrollView) getView().findViewById(
				R.id.descriptionContainer);

		llButtonHolder = (LinearLayout) getView().findViewById(
				R.id.llButtonHolder);
		imgBtnReport = (ImageButton) getView().findViewById(R.id.imgbtnReport);
		imgBtnReport.setVisibility(View.INVISIBLE);
		imgBtnEnter = (ImageButton) getView().findViewById(R.id.imgbtnEnter);

		tvNumberOfMembers = (TextView) getView().findViewById(
				R.id.tvNumberOfMembers);

		galleryOfMembers = (Gallery) getView().findViewById(
				R.id.galleryOfMembers);

		// nonFriendMembersOnlineStringPair.clear();
		// for (int i = 0; i < 4; i++) {
		// nonFriendMembersOnlineStringPair.add(new StringPair());
		// }

	}

	private void initControlListeners() {
		Globals.logInfo(this, "initControlListeners");
		imgBtnEditSave.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mode == MODE_VIEW) {
					edit();
				} else if (mode == MODE_ADD) {
					save();
				} else if (mode == MODE_EDIT) {
					update();
				}
			}
		});

		imgBtnBack.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				getFragmentManager().popBackStack();
				// FragmentTransaction ft = getFragmentManager()
				// .beginTransaction()
				// .setCustomAnimations(R.anim.slide_up_in,
				// R.anim.slide_down_out, R.anim.slide_down_in,
				// R.anim.slide_down_out)
				// .replace(R.id.topSlot, new GroupsFragment(),
				// GroupsFragment.FRAGMENT_TAG);
				// ft.addToBackStack(null);
				// ft.commit();
				// getActivity().overridePendingTransition(0, 0);
			}
		});

		mUsersAdapter = createMembersAdapterAdapter();
		galleryOfMembers.setAdapter(mUsersAdapter);

		galleryOfMembers.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if (mode == MODE_ADD || mode == MODE_EDIT) {
					// Toast.makeText(getActivity(), "selectUserForAdd", 500)
					// .show();
					selectUserForAdd(parent, view, position, id);
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

	private View createNonFriendsMembersItem() {
		LayoutInflater inflater = LayoutInflater.from(getActivity());
		View v = inflater.inflate(R.layout.galery_item_user_group, null);
		View numView = v.findViewById(R.id.tvNumberOfMembers);
		if (numView != null) {
			numView.setVisibility(View.INVISIBLE);
		}
		((TextView) v.findViewById(R.id.tvGroupName))
				.setText(R.string.label_non_friends);
		((ImageView) v.findViewById(R.id.imgGroupType))
				.setVisibility(View.GONE);
		v.setVisibility(View.INVISIBLE);
		return v;
	}

	private View createSeparator() {
		LayoutInflater inflater = LayoutInflater.from(getActivity());
		View v = inflater.inflate(R.layout.galery_item_separator, null);
		v.setVisibility(View.INVISIBLE);
		return v;
	}

	private void switchMode() {
		switch (mode) {
		case MODE_VIEW:
			if (tvViewTitle != null) {
				tvViewTitle.setText(R.string.label_group_details);
			}

			tvGroupName.setVisibility(View.VISIBLE);
			descriptionContainer.setVisibility(View.VISIBLE);

			etGroupName.setVisibility(View.GONE);
			etGroupDescription.setVisibility(View.GONE);

			imgBtnEditSave
					.setImageResource(R.drawable.channels_ui_moving_function_bar_btn_edit);

			llButtonHolder.setVisibility(View.VISIBLE);

			selectedUsers.clear();

			getLoaderManager().destroyLoader(USERS_LOADER);
			getLoaderManager().restartLoader(GROUP_DETAILS_LOADER,
					this.getArguments(), this);
			getLoaderManager().restartLoader(GROUP_MEMBERS_LOADER,
					this.getArguments(), this);
			break;
		case MODE_EDIT:
			if (tvViewTitle != null) {
				tvViewTitle.setText(R.string.label_edit_group);
			}

			tvGroupName.setVisibility(View.GONE);
			descriptionContainer.setVisibility(View.GONE);

			etGroupName.setVisibility(View.VISIBLE);
			etGroupDescription.setVisibility(View.VISIBLE);

			imgBtnEditSave
					.setImageResource(R.drawable.channels_ui_moving_function_bar_btn_save);

			llButtonHolder.setVisibility(View.GONE);

			tvViewTitle.setText(R.string.label_add_new_group);

			getLoaderManager().destroyLoader(USERS_LOADER);
			getLoaderManager().restartLoader(GROUP_DETAILS_LOADER,
					this.getArguments(), this);
			getLoaderManager().restartLoader(GROUP_MEMBERS_LOADER,
					this.getArguments(), this);
			break;
		case MODE_ADD:
			if (tvViewTitle != null) {
				tvViewTitle.setText(R.string.label_add_group);
			}

			tvGroupName.setVisibility(View.GONE);
			descriptionContainer.setVisibility(View.GONE);

			etGroupName.setVisibility(View.VISIBLE);
			etGroupDescription.setVisibility(View.VISIBLE);

			imgBtnEditSave
					.setImageResource(R.drawable.channels_ui_moving_function_bar_btn_save);

			llButtonHolder.setVisibility(View.GONE);

			tvViewTitle.setText(R.string.label_add_new_group);

			getLoaderManager().restartLoader(USERS_LOADER, null, this);
			getLoaderManager().destroyLoader(GROUP_DETAILS_LOADER);
			getLoaderManager().destroyLoader(GROUP_MEMBERS_LOADER);
			break;
		}
	}

	private void save() {

		ContentValues values = new ContentValues();
		String groupName = etGroupName.getText().toString();
		String groupDescription = etGroupDescription.getText().toString();

		if (groupName != null && groupDescription != null
				&& !groupName.equals("") && !groupDescription.equals("")) {

			values.put(DbContentProvider.PRIVATE_GROUP_COL_NAME, groupName);
			values.put(DbContentProvider.PRIVATE_GROUP_COL_DESCRIPTION,
					groupDescription);

			int updated = 0;
			Uri uri = null;
			if (mode == MODE_EDIT && groupUriString != null
					&& !groupUriString.equals("")) {
				uri = Uri.parse(groupUriString);
				updated = RalleeApp.getInstance().getApplicationContext()
						.getContentResolver().update(uri, values, null, null);
				getHomeActivity().googleTracker("groups", "privateGroup",
						"edit", 0);
			}
			if (updated == 0) {
				uri = RalleeApp
						.getInstance()
						.getApplicationContext()
						.getContentResolver()
						.insert(DbContentProvider.CONTENT_URI_PRIVATE_GROUP_TABLE,
								values);
				getHomeActivity().googleTracker("groups", "privateGroup",
						"addNew", 0);
			}

			if (uri != null) {
//				Log.i("fbGroups", "URI: " + uri.toString());
				// Toast.makeText(getActivity(),
				// "Group " + groupName + " is created", 500).show();
				int newGroupId = Integer.parseInt(uri.getLastPathSegment());
				addUsersToTheGroup(selectedUsers, newGroupId);

				groupUriString = uri.toString();

				mode = MODE_VIEW;
				this.getArguments().putString(URI, groupUriString);

				switchMode();
			}
		} else {
			// Toast.makeText(getActivity(),
			// R.string.please_enter_group_name, 500)
			// .show();
		}
	}

	private void edit() {
		mode = MODE_EDIT;
		this.getArguments().putString(URI, groupUriString);
		getLoaderManager().restartLoader(GROUP_DETAILS_LOADER,
				this.getArguments(), this);
		switchMode();
	}

	private void update() {
		save();
	}

	@Override
	public Loader<Cursor> onCreateLoader(int loaderId, Bundle bundle) {
		if (loaderId == GROUP_DETAILS_LOADER) {

			groupUriString = bundle.getString(URI);
			String groupId = Uri.parse(groupUriString).getLastPathSegment();
			Uri baseUri = DbContentProvider.CONTENT_URI_PRIVATE_GROUP_TABLE;
			String selection = DbContentProvider.PRIVATE_GROUP_COL_ID + "=?";
			String[] selectionArgs = { groupId };
			return new CursorLoader(RalleeApp.getInstance(), baseUri, null,
					selection, selectionArgs, null);

		} else if (loaderId == GROUP_MEMBERS_LOADER) {
			groupUriString = bundle.getString(URI);
			String groupId = Uri.parse(groupUriString).getLastPathSegment();
			Uri baseUri = DbContentProvider.CONTENT_URI_GROUP_MEMBERS_TABLE;
			String selection = DbContentProvider.GROUP_MEMBERS_COL_GROUP_ID
					+ "=?";
			String[] selectionArgs = { groupId };

			return new CursorLoader(RalleeApp.getInstance(), baseUri, null,
					selection, selectionArgs, null);
		} else if (loaderId == USERS_LOADER) {
			String selectionString = "";
			if (bundle != null) {
				ArrayList<String> members = bundle.getStringArrayList(MEMBERS);
				if (members != null) {
					if (members != null && tvNumberOfMembers != null) {
						tvNumberOfMembers.setText(RalleeApp.getInstance()
								.getString(R.string.label_total_prefix)
								+ members.size());
					}

					selectionString = " AND " + DbContentProvider.USER_COL_ID
							+ " IN ( ";

					if (members != null) {
						for (String m : members) {
							selectionString += "'" + m + "',";
						}
					}

					selectionString = selectionString.substring(0,
							selectionString.length() - 1);
					selectionString += ")";
				}
			}

			String basicSelectrionString = DbContentProvider.USER_COL_INSTALLED
					+ "=1";

			if (mode == MODE_VIEW) {
				basicSelectrionString += selectionString;
			}

			String sortOrder = DbContentProvider.USER_COL_NAME + " ASC";

			Uri baseUri = DbContentProvider.CONTENT_URI_USER_TABLE;
			return new CursorLoader(RalleeApp.getInstance(), baseUri, null,
					basicSelectrionString, null, sortOrder);
		} else {
			return null;
		}
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		if (loader.getId() == GROUP_DETAILS_LOADER) {
			if (data != null && data.moveToFirst()) {

				Integer groupId = data
						.getInt(data
								.getColumnIndex(DbContentProvider.PRIVATE_GROUP_COL_ID));
				String name = data
						.getString(data
								.getColumnIndex(DbContentProvider.PRIVATE_GROUP_COL_NAME));
				String description = data
						.getString(data
								.getColumnIndex(DbContentProvider.PRIVATE_GROUP_COL_DESCRIPTION));

				if (mode == MODE_VIEW) {
					if (tvGroupName != null) {

						if (name != null && !name.equals("")) {
							tvGroupName.setText(name);
						}
					}

					if (tvViewTitle != null) {

						if (name != null && !name.equals("")) {
							tvViewTitle.setText(name
									+ RalleeApp.getInstance().getString(
											R.string.label_details_sufix));
						}
					}

					if (tvGroupDescription != null) {

						if (description != null && !description.equals("")) {
							tvGroupDescription.setText(description);
						}
					}
				} else {
					if (etGroupName != null) {

						if (name != null && !name.equals("")) {
							etGroupName.setText(name);
						}
					}

					if (etGroupDescription != null) {
						if (description != null && !description.equals("")) {
							etGroupDescription.setText(description);
						}
					}

					if (mode == MODE_EDIT && tvViewTitle != null) {
						if (description != null && !description.equals("")) {
							tvViewTitle.setText(RalleeApp.getInstance()
									.getString(R.string.label_edit_prefix)
									+ name);
						}
					}
				}

				if (imgBtnEnter != null) {
					imgBtnEnter.setTag("" + groupId);
					imgBtnEnter.setOnClickListener(new View.OnClickListener() {

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
						}
					});
				}
			}
		} else if (loader.getId() == GROUP_MEMBERS_LOADER) {

			if (data != null && tvNumberOfMembers != null) {
				tvNumberOfMembers.setText(RalleeApp.getInstance().getString(
						R.string.label_total_prefix)
						+ data.getCount());
			}

			ArrayList<String> memberIds = new ArrayList<String>();
			if (data != null) {
				if (data.moveToFirst()) {
					do {
						memberIds
								.add(data.getString(data
										.getColumnIndex(DbContentProvider.GROUP_MEMBERS_COL_USER_ID)));
					} while (data.moveToNext());
				}

				if (mode == MODE_EDIT) {
					selectedUsers.clear();
					for (String member : memberIds) {
						selectedUsers.add(member);
					}
					getLoaderManager().restartLoader(USERS_LOADER, null, this);
				} else {
					Bundle usersBundle = new Bundle();
					usersBundle.putStringArrayList(MEMBERS, memberIds);
					getLoaderManager().restartLoader(USERS_LOADER, usersBundle,
							this);
				}
			}

		} else if (loader.getId() == USERS_LOADER) {

			// if (data != null && tvNumberOfMembers != null) {
			// tvNumberOfMembers.setText("Members users table finished: "
			// + data.getCount());
			// }

			mUsersAdapter.swapCursor(data);
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		if (loader.getId() == USERS_LOADER) {
			mUsersAdapter.swapCursor(null);
		}
		if (loader.getId() == GROUP_DETAILS_LOADER) {

		}
		if (loader.getId() == GROUP_MEMBERS_LOADER) {
			selectedUsers.clear();
			getLoaderManager().restartLoader(USERS_LOADER, null,
					PrivateGroupDetailsFragment.this);
		}

	}

	private SimpleCursorAdapter createMembersAdapterAdapter() {

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

		onlineFriendsPictureModel = new FriendsGetProfilePics();
		onlineFriendsPictureModel.setListener(adapter);

		adapter.setViewBinder(new ViewBinder() {

			@Override
			public boolean setViewValue(View view, final Cursor cursor,
					final int columnIndex) {
				if (view.getId() == R.id.profile_pic) {
					((ImageView) view).setImageBitmap(onlineFriendsPictureModel.getImage(
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

	private SimpleCursorAdapter createAllUsersAdapter() {
		SimpleCursorAdapter adapter = new SimpleCursorAdapter(getActivity(),
				R.layout.user_list_item, null, new String[] {
						DbContentProvider.USER_COL_NAME,
						DbContentProvider.USER_COL_PIC_URL }, new int[] {
						R.id.title, R.id.profile_pic }, 0) {
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				// TODO Auto-generated method stub
				View v = super.getView(position, convertView, parent);
				// changeItemsViews(v);
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

	public void startGetParticipantsServiceReceiver(Cursor c) {
		RRChannels channel = new RRChannels();
		channel.name = c.getString(c
				.getColumnIndex(DbContentProvider.CHANNEL_COL_NAME));
		channel.id = c.getInt(c
				.getColumnIndex(DbContentProvider.CHANNEL_COL_ID));
		channel.parent = c.getInt(c
				.getColumnIndex(DbContentProvider.CHANNEL_COL_PARENT));
		channel.description = c.getString(c
				.getColumnIndex(DbContentProvider.CHANNEL_COL_DESCRIPTION));
		channel.temporary = (c.getInt(c
				.getColumnIndex(DbContentProvider.CHANNEL_TEMPORARY)) == 0) ? false
				: true;
		channel.userCount = c.getInt(c
				.getColumnIndex(DbContentProvider.CHANNEL_COL_USER_COUNT));
		channel.serverIpAdr = c.getString(c
				.getColumnIndex(DbContentProvider.CHANNEL_COL_SERVER_IP_ADR));
		channel.port = c.getString(c
				.getColumnIndex(DbContentProvider.CHANNEL_COL_PORT));
		startGetParticipantsServiceReceiver(channel);
	}

	/**
	 * Start Participants on the Channel Service receiver
	 */
	public void startGetParticipantsServiceReceiver(RRChannels channel) {
		ObjectMapper mapper = new ObjectMapper();
		try {
			String chanStr = mapper.writeValueAsString(channel);
//			Log.i(FRAGMENT_TAG, "startGetParticipantsService('" + chanStr
//					+ "')");
			RRServerProxyHelper.startGetParticipantsService(
					RalleeApp.getInstance(), chanStr);
		} catch (JsonGenerationException e) {
			// TODO Auto-generated catch block no users on this channel
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

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

	private void addUsersToTheGroup(ArrayList<String> userIds, int newGroupId) {
		if (mode == MODE_ADD || mode == MODE_EDIT) {

			RalleeApp
					.getInstance()
					.getApplicationContext()
					.getContentResolver()
					.delete(DbContentProvider.CONTENT_URI_GROUP_MEMBERS_TABLE,
							DbContentProvider.GROUP_MEMBERS_COL_GROUP_ID + "=?",
							new String[] { "" + newGroupId });

			for (String userId : userIds) {
				ContentValues cv = new ContentValues();
				cv.put(DbContentProvider.GROUP_MEMBERS_COL_GROUP_ID, newGroupId);
				cv.put(DbContentProvider.GROUP_MEMBERS_COL_USER_ID, userId);
				Uri uri = RalleeApp
						.getInstance()
						.getApplicationContext()
						.getContentResolver()
						.insert(DbContentProvider.CONTENT_URI_GROUP_MEMBERS_TABLE,
								cv);
				if (uri != null) {
//					Log.i("fbGroups", "uri:" + uri.toString());
				}

			}
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
		boolean selected = false;
		if (mode == MODE_ADD || mode == MODE_EDIT) {
			if (tv != null) {
				String itemID = (String) tv.getTag();
				if (itemID != null) {
					for (String memberId : selectedUsers) {
						if (memberId.equals(itemID)) {
							selected = true;
							break;
						}
					}
				}
			}
		}
		if (selected) {
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
			boolean isOnline = false;
			if (getRRService() != null) {
				for (RRUser usnm : getRRService().getUserList()) {
//					Log.i(FRAGMENT_TAG + "status", "Is " + usnm.userName
//							+ " equal " + username);
					if (usnm.userName != null && usnm.userName.equals(username)) {
//						Log.i(FRAGMENT_TAG + "status", "YES");
						isOnline = true;
						break;
					} else {
//						Log.i(FRAGMENT_TAG + "status", "NO");
					}
				}
			}
			if (isOnline) {
				imgStatus.setImageResource(R.drawable.main_ui_speaker_led);
			} else {
				imgStatus.setImageResource(R.drawable.main_ui_mic_led);
			}
		}
	}

	private void contactUser(AdapterView<?> parent, View view, int position,
			long id) {
		Cursor c = ((Cursor) parent.getItemAtPosition(position));
		if (c != null) {
			String userId = c.getString(c
					.getColumnIndex(DbContentProvider.USER_COL_ID));
			String user = c.getString(c
					.getColumnIndex(DbContentProvider.USER_COL_NAME));
			if (userId != null && user != null) {
				List<RRUser> users = getRRService().getUserList();
				boolean isOnline = false;
				for (RRUser usr : users) {
					if (usr.userName.equals(userId)) {
						isOnline = true;
						break;
					}

				}
				if (isOnline) {
					getHomeActivity().callFriend(userId, user);
				} else {
					getHomeActivity().callOfflineFriend(userId, user);
				}
			}
		}
	}

	private void selectUserForAdd(AdapterView<?> parent, View view,
			int position, long id) {
		if (mode == MODE_ADD || mode == MODE_EDIT) {
			if (selectedUsers != null) {
				Cursor c = ((Cursor) parent.getItemAtPosition(position));
				if (c != null) {
					String userId = c.getString(c
							.getColumnIndex(DbContentProvider.USER_COL_ID));
					if (!selectedUsers.remove(userId)) {
						selectedUsers.add(userId);
					}

					if (selectedUsers != null && tvNumberOfMembers != null) {
						tvNumberOfMembers.setText(RalleeApp.getInstance()
								.getString(R.string.label_total_prefix)
								+ selectedUsers.size());
					}

					getLoaderManager().restartLoader(USERS_LOADER, null,
							PrivateGroupDetailsFragment.this);
				}

			}
		}
	}

	public void notifyFriendsAdapterDataSetChange() {
		mUsersAdapter.notifyDataSetChanged();
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
