package com.radiorunt.activities.fragments;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.model.GraphObject;
import com.radiorunt.R;
import com.radiorunt.activities.DbContentProvider;
import com.radiorunt.activities.HomeActivity;
import com.radiorunt.businessobjects.RRChannels;
import com.radiorunt.facebook.FriendsGetProfilePics;
import com.radiorunt.facebook.Utility;
import com.radiorunt.utilities.Globals;
import com.radiorunt.utilities.MergeAdapter;
import com.radiorunt.utilities.RRServerProxyHelper;
import com.radiorunt.utilities.RalleeApp;
import com.radiorunt.utilities.StringPair;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
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
import android.widget.Gallery;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class PublicGroupDetailsFragment extends Fragment implements
		LoaderCallbacks<Cursor> {

	/**
	 * Tag that can be used to identify fragment within activity manager
	 */
	public static final String FRAGMENT_TAG = "publicGroupDetailsFragment";
	public static final String URI = "uri";
	public static final String MEMBERS = "members";

	private final int GROUP_DETAILS_LOADER = 0;
	private final int GROUP_MEMBERS_FRIENDS_LOADER = 1;
	private final int GROUP_MEMBERS_NON_FRIENDS_LOADER = 2;

	private final int MODE_VIEW = 0;
	private final int MODE_EDIT = 1;
	private final int MODE_ADD = 2;
	private int mode = MODE_VIEW;

	private ImageButton imgBtnEditSave;

	private TextView tvViewTitle;

	private TextView tvGroupName;

	private ImageButton imgBtnBack;

	private ImageView imgGroupImage;

	private TextView tvGroupDescription;

	private TextView tvNumberOfMembers;

	private Gallery galleryOfMembers;

	private SimpleCursorAdapter mOnlineFriendsMembersAdapter;

	private MergeAdapter mMergeAdapter;

	private FriendsGetProfilePics channelDetailsPictureModel;

	String groupUriString = null;
	private GetParticipantsServiceReceiver mGetParticipantsServiceReceiver;
	private FriendsGetProfilePics onlineFriendsPictureModel;

	private ArrayList<String> nonFriendMembersOnline = new ArrayList<String>();
	private List<StringPair> nonFriendMembersOnlineStringPair = new ArrayList<StringPair>(
			4);
	private ArrayList<String> allParticipants;
	private View separator;
	private View nonFriendsIcon;
	private LinearLayout llButtonHolder;
	private ImageButton imgBtnReport;
	private ImageButton imgBtnEnter;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Globals.logInfo(this, "onCreateView");
		View v = inflater.inflate(R.layout.group_details, container, false);
		getLoaderManager().initLoader(GROUP_DETAILS_LOADER,
				this.getArguments(), this);
		v.setOnTouchListener(new View.OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
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

		switchMode(MODE_VIEW);
		registerGetParticipantsServiceReceiver();
	}

	@Override
	public void onPause() {
		super.onPause();
		unregisterGetParticipantsServiceReceiver();
	}

	private void initControls() {
		Globals.logInfo(this, "initControls");
		imgBtnBack = (ImageButton) getView().findViewById(R.id.imgbtnBack);
		imgBtnEditSave = (ImageButton) getView().findViewById(
				R.id.imgbtnEditSave);
		imgBtnEditSave.setVisibility(View.INVISIBLE);
		tvViewTitle = (TextView) getView().findViewById(R.id.tvViewLabel);

		tvGroupName = (TextView) getView().findViewById(R.id.tvGroupName);
		imgGroupImage = (ImageView) getView().findViewById(R.id.imgGroupImage);
		tvGroupDescription = (TextView) getView().findViewById(
				R.id.tvGroupDescription);

		llButtonHolder = (LinearLayout) getView().findViewById(
				R.id.llButtonHolder);
		imgBtnReport = (ImageButton) getView().findViewById(R.id.imgbtnReport);
		imgBtnReport.setVisibility(View.INVISIBLE);
		imgBtnEnter = (ImageButton) getView().findViewById(R.id.imgbtnEnter);

		tvNumberOfMembers = (TextView) getView().findViewById(
				R.id.tvNumberOfMembers);

		galleryOfMembers = (Gallery) getView().findViewById(
				R.id.galleryOfMembers);

		nonFriendMembersOnlineStringPair.clear();
		for (int i = 0; i < 4; i++) {
			nonFriendMembersOnlineStringPair.add(new StringPair());
		}

	}

	private void initControlListeners() {
		Globals.logInfo(this, "initControlListeners");
		imgBtnEditSave.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mode == MODE_VIEW) {
					edit();
				} else if (mode == MODE_EDIT) {
					save();
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

		mMergeAdapter = new MergeAdapter() {
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				View v = super.getView(position, convertView, parent);
				if (this.getItemViewType(position) == 2) {// Non Friends
//					Log.i(FRAGMENT_TAG,
//							"View: "
//									+ ((TextView) v
//											.findViewById(R.id.tvGroupName))
//											.getText());
					if (channelDetailsPictureModel == null) {
						channelDetailsPictureModel = new FriendsGetProfilePics();
						channelDetailsPictureModel.setListener(this);
					}

					List<ImageView> ivs = new ArrayList<ImageView>();
					ivs.clear();
					ivs.add((ImageView) v.findViewById(R.id.pic1));
					ivs.add((ImageView) v.findViewById(R.id.pic2));
					ivs.add((ImageView) v.findViewById(R.id.pic3));
					ivs.add((ImageView) v.findViewById(R.id.pic4));
					boolean atLeastOneInitialized = false;
					for (int i = 0; i < nonFriendMembersOnlineStringPair.size(); i++) {
						StringPair rf = nonFriendMembersOnlineStringPair.get(i);
						if (rf != null && rf.isInitialized()) {
							atLeastOneInitialized = true;
							ivs.get(i).setImageBitmap(
									channelDetailsPictureModel.getImage(
											rf.getId(), rf.getUrl(),
											RalleeApp.getInstance()));
//							Log.i(FRAGMENT_TAG,
//									"setImageBitmap: " + rf.toString());
						}
					}

					if (atLeastOneInitialized) {
						separator.setVisibility(View.VISIBLE);
						nonFriendsIcon.setVisibility(View.VISIBLE);
					}

				}

				return v;
			}
		};
		mOnlineFriendsMembersAdapter = createOnlineFriendsMembersAdapter();

		mMergeAdapter.addAdapter(mOnlineFriendsMembersAdapter);
		separator = createSeparator();
		nonFriendsIcon = createNonFriendsMembersItem();
		mMergeAdapter.addView(separator, false);
		mMergeAdapter.addView(nonFriendsIcon);
		galleryOfMembers.setAdapter(mMergeAdapter);

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

	private void switchMode(int newMode) {
		switch (newMode) {
		case MODE_VIEW:
			if (tvViewTitle != null) {
				tvViewTitle.setText(R.string.label_group_details);
			}
			break;
		case MODE_EDIT:
			if (tvViewTitle != null) {
				tvViewTitle.setText(R.string.label_edit_group);
			}
			break;
		case MODE_ADD:
			if (tvViewTitle != null) {
				tvViewTitle.setText(R.string.label_add_group);
			}
			break;
		}
	}

	private void save() {
		switchMode(MODE_VIEW);
	}

	private void edit() {
		switchMode(MODE_EDIT);
	}

	public void notifyFriendsAdapterDataSetChange() {
		// getLoaderManager().restartLoader(LOADER_GRID_CONTACTS, null, this);
		try {
			getLoaderManager().restartLoader(GROUP_DETAILS_LOADER, null, this);
		} catch (IllegalStateException ise) {

		}
	}

	@Override
	public Loader<Cursor> onCreateLoader(int loaderId, Bundle bundle) {
		if (loaderId == GROUP_DETAILS_LOADER) {

			if (groupUriString == null) {
				groupUriString = bundle.getString(URI);
			}
			Uri baseUri = Uri.parse(groupUriString);
			return new CursorLoader(RalleeApp.getInstance(), baseUri, null,
					null, null, null);

		} else if (loaderId == GROUP_MEMBERS_FRIENDS_LOADER
				|| loaderId == GROUP_MEMBERS_NON_FRIENDS_LOADER) {
			ArrayList<String> mebers = bundle.getStringArrayList(MEMBERS);
			Uri baseUri = DbContentProvider.CONTENT_URI_USER_TABLE;
			String selectionString = null;

			if (mebers != null) {

				tvNumberOfMembers.setText(RalleeApp.getInstance().getString(
						R.string.label_in_group)
						+ mebers.size());

				selectionString = DbContentProvider.USER_COL_ID + " IN (";
				for (int i = 0; i < mebers.size(); i++) {
					if (i == 0) {
						selectionString += "'" + mebers.get(i) + "'";
					} else {
						selectionString += ", '" + mebers.get(i) + "'";
					}
				}
				selectionString += ")";
			}

			if (loaderId == GROUP_MEMBERS_NON_FRIENDS_LOADER) {
				allParticipants = mebers;
			}

			return new CursorLoader(RalleeApp.getInstance(), baseUri, null,
					selectionString, null, null);
		} else {
			return null;
		}
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		if (loader.getId() == GROUP_DETAILS_LOADER) {
			if (data != null && data.moveToFirst()) {
				if (tvGroupName != null) {

					String name = data
							.getString(data
									.getColumnIndex(DbContentProvider.CHANNEL_COL_NAME));
					if (name != null && !name.equals("")) {
						tvGroupName.setText(name.substring(5, name.length()));
					}
				}

				if (tvGroupDescription != null) {
					String description = data
							.getString(data
									.getColumnIndex(DbContentProvider.CHANNEL_COL_DESCRIPTION));
					if (description != null && !description.equals("")) {
						tvGroupDescription.setText(description);
					}
				}

				if (imgBtnEnter != null) {
					String name = data
							.getString(data
									.getColumnIndex(DbContentProvider.CHANNEL_COL_NAME));
					imgBtnEnter.setTag(name);
					imgBtnEnter.setOnClickListener(new View.OnClickListener() {

						@Override
						public void onClick(View v) {
							if (getHomeActivity() != null) {
								getHomeActivity().enterPublicChannel(
										((String) v.getTag()));
							}
						}
					});
				}

				startGetParticipantsServiceReceiver(data);
			}
		} else if (loader.getId() == GROUP_MEMBERS_FRIENDS_LOADER) {

			mOnlineFriendsMembersAdapter.swapCursor(data);
		} else if (loader.getId() == GROUP_MEMBERS_NON_FRIENDS_LOADER) {

			if (data != null) {
				if (data.moveToFirst()) {
					do {
						if (allParticipants != null) {
							Bundle b = new Bundle();
							allParticipants
									.remove(data.getString(data
											.getColumnIndex(DbContentProvider.USER_COL_ID)));
						}
					} while (data.moveToNext());
				}

				setThumbs(allParticipants, nonFriendMembersOnlineStringPair);

			}
		}
	}

	protected HomeActivity getHomeActivity() {
		return (HomeActivity) getActivity();
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		if (loader.getId() == GROUP_MEMBERS_FRIENDS_LOADER) {

			mOnlineFriendsMembersAdapter.swapCursor(null);
		} else if (loader.getId() == GROUP_MEMBERS_NON_FRIENDS_LOADER) {
			nonFriendMembersOnlineStringPair.clear();
			for (int i = 0; i < 4; i++) {
				nonFriendMembersOnlineStringPair.add(new StringPair());
			}
		}
	}

	private SimpleCursorAdapter createOnlineFriendsMembersAdapter() {
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
				return false;
			}
		});

		return adapter;
	}

	/**
	 * Register Participants on the Channel Service receiver
	 */
	public void registerGetParticipantsServiceReceiver() {
		if (mGetParticipantsServiceReceiver != null) {
			return;
		}
		mGetParticipantsServiceReceiver = new GetParticipantsServiceReceiver();
		RRServerProxyHelper.registerGetParticipantsReceiver(getActivity(),
				mGetParticipantsServiceReceiver);

	}

	/**
	 * Unregister Participants on the Channel Service receiver
	 */
	public void unregisterGetParticipantsServiceReceiver() {
		if (mGetParticipantsServiceReceiver == null) {
			return;
		}
		getActivity().unregisterReceiver(mGetParticipantsServiceReceiver);
		mGetParticipantsServiceReceiver = null;
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

	public class GetParticipantsServiceReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {

			String errMsg = RRServerProxyHelper.getChannelsError(context,
					intent);
			if (errMsg.equals("")) {
				String jsonResponseString = RRServerProxyHelper
						.getParticipantsResponse(context, intent);

				if (jsonResponseString != null) {
//					Log.i(FRAGMENT_TAG, jsonResponseString);
					ObjectMapper mapper = new ObjectMapper();
					ArrayList<String> array = null;
					try {
						array = mapper.readValue(jsonResponseString,
								new TypeReference<ArrayList<String>>() {
								});
					} catch (JsonParseException e) {
//						Log.i(FRAGMENT_TAG, e.toString());
						e.printStackTrace();

					} catch (JsonMappingException e) {
//						Log.i(FRAGMENT_TAG, e.toString());
						e.printStackTrace();

					} catch (IOException e) {
//						Log.i(FRAGMENT_TAG, e.toString());
						e.printStackTrace();
					}
					if (array != null) {
						Bundle b = new Bundle();
						b.putStringArrayList(MEMBERS, array);
						getLoaderManager().restartLoader(
								GROUP_MEMBERS_FRIENDS_LOADER, b,
								PublicGroupDetailsFragment.this);
						getLoaderManager().restartLoader(
								GROUP_MEMBERS_NON_FRIENDS_LOADER, b,
								PublicGroupDetailsFragment.this);
					}
				} else {
//					Log.i(FRAGMENT_TAG, "array is null");
				}
			} else {
//				Log.i(FRAGMENT_TAG, "No participants on this channel");
			}
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

	private void setThumbs(List<String> ralleeIds, List<StringPair> users) {
		if (ralleeIds == null) {
			return;
		}

		Random randomGenerator = new Random();

		int size = ralleeIds.size();

		List<String> randomFBIds = new ArrayList<String>();
		String whareString = " IN ( ";
		while (ralleeIds.size() != 0 && randomFBIds.size() < 4) {
			String candidate = Utility
					.parseSNData(
							ralleeIds.remove(randomGenerator.nextInt(ralleeIds
									.size()))).getAsString(
							Utility.SOCIAL_NETWORK_ID);
			if (candidate == null)
				continue;
			if (candidate.equals(""))
				continue;
			if (candidate.equals("Unknown"))
				continue;
			randomFBIds.add(candidate);
			whareString += candidate + ",";

		}
		whareString = whareString.substring(0, whareString.length() - 1);
		whareString += ")";

		String query = "SELECT uid, pic_square FROM user WHERE uid"
				+ whareString + " ORDER BY rand() LIMIT 4";
		Bundle params = new Bundle();
		params.putString("q", query);
		Response response = null;

		try {
			Session session = Session.getActiveSession();
			Request request = new Request(session, "/fql", params,
					HttpMethod.GET);
			response = Request.executeAndWait(request);

			if (response == null) {
				return;
			}

			if (response.getError() == null) {

				GraphObject data = response.getGraphObject();
				JSONArray friendUsersArray = ((JSONArray) data.asMap().get(
						"data"));

				for (int j = 0; j < friendUsersArray.length(); j++) {
					JSONObject o = friendUsersArray.getJSONObject(j);
					users.get(j).setId(o.getString("uid"));
					users.get(j).setUrl(o.getString("pic_square"));
				}
				mMergeAdapter.notifyDataSetChanged();
			}

			// } catch (MalformedURLException e) {
			// // TODO Auto-generated catch block
			// e.printStackTrace();
			// } catch (IOException e) {
			// // TODO Auto-generated catch block
			// e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
