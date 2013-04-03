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
import com.radiorunt.businessobjects.RRUser;
import com.radiorunt.facebook.FriendsGetProfilePics;
import com.radiorunt.facebook.Utility;
import com.radiorunt.services.RadioRuntService;
import com.radiorunt.utilities.Globals;
import com.radiorunt.utilities.MergeAdapter;
import com.radiorunt.utilities.RRServerProxyHelper;
import com.radiorunt.utilities.RalleeApp;
import com.radiorunt.utilities.StringPair;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
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
import android.widget.FrameLayout;
import android.widget.Gallery;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class FacebookGroupDetailsFragment extends Fragment implements
		LoaderCallbacks<Cursor> {

	/**
	 * Tag that can be used to identify fragment within activity manager
	 */
	public static final String FRAGMENT_TAG = "facebookGroupDetailsFragment";
	public static final String URI = "uri";
	public static final String MEMBERS = "members";

	private static final int FB_GROUP_DETAILS_LOADER = 0;
	private static final int FB_GROUP_DETAILS_FRIENDS = 1;

	private ImageButton imgBtnEditSave;

	private TextView tvViewTitle;

	private TextView tvGroupName;

	private ImageButton imgBtnBack;

	private ImageView imgGroupImage;

	private TextView tvGroupDescription;

	private TextView tvNumberOfMembers;

	private Gallery galleryOfMembers;

	private SimpleCursorAdapter mFriendsMembersAdapter;

	private MergeAdapter mMergeAdapter;

	private FriendsGetProfilePics channelDetailsPictureModel;

	String groupUriString = null;
	String fbGroupId = null;

	// private GetParticipantsServiceReceiver mGetParticipantsServiceReceiver;
	private FriendsGetProfilePics FriendsPictureModel;

	// private ArrayList<String> nonFriendMembersOnline = new
	// ArrayList<String>();
	private List<StringPair> nonFriendMembersStringPair = new ArrayList<StringPair>(
			4);
	// private ArrayList<String> allParticipants;
	private List<StringPair> GroupIconStringPair = new ArrayList<StringPair>(4);
	private View separator;
	private View nonFriendsIcon;
	private LinearLayout llButtonHolder;
	private ImageButton imgBtnReport;
	private ImageButton imgBtnEnter;
	private String sGroupId = "";

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Globals.logInfo(this, "onCreateView");
		View v = inflater.inflate(R.layout.group_details_facebook, container,
				false);
		getLoaderManager().initLoader(FB_GROUP_DETAILS_LOADER,
				this.getArguments(), this);
		// getLoaderManager().initLoader(FB_GROUP_DETAILS_FRIENDS, null, this);
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

	}

	@Override
	public void onPause() {
		super.onPause();
	}

	private void initControls() {
		Globals.logInfo(this, "initControls");

		imgBtnBack = (ImageButton) getView().findViewById(R.id.imgbtnBack);

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

		nonFriendMembersStringPair.clear();
		for (int i = 0; i < 4; i++) {
			nonFriendMembersStringPair.add(new StringPair());
		}

	}

	private void initControlListeners() {
		Globals.logInfo(this, "initControlListeners");

		imgBtnBack.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				getFragmentManager().popBackStack();
			}
		});

		mMergeAdapter = new MergeAdapter() {
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				View v = super.getView(position, convertView, parent);
				if (this.getItemViewType(position) == 0) {// Friend list item

					changeItemsViews(v);
				} else if (this.getItemViewType(position) == 2) {// Non Friends
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
					for (int i = 0; i < nonFriendMembersStringPair.size(); i++) {
						StringPair rf = nonFriendMembersStringPair.get(i);
						if (rf.isInitialized()) {
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
		mFriendsMembersAdapter = createOnlineFriendsMembersAdapter();

		mMergeAdapter.addAdapter(mFriendsMembersAdapter);
		separator = createSeparator();
		nonFriendsIcon = createNonFriendsMembersItem();
		mMergeAdapter.addView(separator, false);
		mMergeAdapter.addView(nonFriendsIcon);
		galleryOfMembers.setAdapter(mMergeAdapter);
		createFBGroupPicture();
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

	@Override
	public Loader<Cursor> onCreateLoader(int loaderId, Bundle bundle) {
		if (loaderId == FB_GROUP_DETAILS_LOADER) {

			groupUriString = bundle.getString(URI);
			Uri baseUri = Uri.parse(groupUriString);
			fbGroupId = baseUri.getLastPathSegment();
			ArrayList<String> uids = bundle
					.getStringArrayList(FacebookGroupsListFragment.UIDS);
			ArrayList<String> urls = bundle
					.getStringArrayList(FacebookGroupsListFragment.URLS);

			if (uids != null && urls != null && GroupIconStringPair.isEmpty()) {

				for (int i = 0; i < 4; i++) {
					StringPair sp = new StringPair();

					sp.setId(uids.get(i));
					sp.setUrl(urls.get(i));

					GroupIconStringPair.add(sp);
				}
			}

			return new CursorLoader(RalleeApp.getInstance(), baseUri, null,
					null, null, null);

		} else if (loaderId == FB_GROUP_DETAILS_FRIENDS) {

			Uri baseUri = DbContentProvider.CONTENT_URI_USER_TABLE;
			String select = null;
			if (bundle != null) {
				select = bundle.getString("select");
				if (select == null) {
					return null;
				}
				if (select.equals("")) {
					return null;
				}
			} else {
				return null;
			}

			return new CursorLoader(RalleeApp.getInstance(), baseUri, null,
					select, null, null);
		} else {
			return null;
		}
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		if (loader.getId() == FB_GROUP_DETAILS_LOADER) {
			if (data != null && data.moveToFirst()) {
				if (tvGroupName != null) {

					String name = data
							.getString(data
									.getColumnIndex(DbContentProvider.FB_GROUP_COL_NAME));
					if (name != null && !name.equals("")) {
						tvGroupName.setText(name);
					}
				}
				String groupId = data.getString(data
						.getColumnIndex(DbContentProvider.FB_GROUP_COL_ID));
				sGroupId = groupId;
				notifyFBgroupsChange();
				fetchFBgroupDetails(groupId);
				fetchFBgroupMembers(groupId);

				if (imgBtnEnter != null) {
					String name = data.getString(data
							.getColumnIndex(DbContentProvider.FB_GROUP_COL_ID));
					imgBtnEnter.setTag(name);
					imgBtnEnter.setOnClickListener(new View.OnClickListener() {

						@Override
						public void onClick(View v) {
							if (getActivity() != null) {
								getHomeActivity().enterFBGroupChannel(
										((String) v.getTag()));
							}
						}
					});
				}

			}
		} else if (loader.getId() == FB_GROUP_DETAILS_FRIENDS) {
			mFriendsMembersAdapter.swapCursor(data);
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {

	}

	private void fetchFBgroupDetails(String groupId) {

		try {

			Bundle params = new Bundle();
			String getFriendMembersQuery = "SELECT description FROM group WHERE gid = "
					+ groupId;

//			Log.i("FBGroupDetails", getFriendMembersQuery);
			params.putString("q", getFriendMembersQuery);
			Session session = Session.getActiveSession();
			Request request = new Request(session, "/fql", params,
					HttpMethod.GET);
			Response response = Request.executeAndWait(request);

//			Log.i("fbGroups", response.toString());

			if (response.getError() == null) {

				GraphObject data = response.getGraphObject();
				JSONArray friendUsersArray = ((JSONArray) data.asMap().get(
						"data"));

				if (friendUsersArray.length() > 0) {
					String description = friendUsersArray.getJSONObject(0)
							.getString("description");
					if (description == null || description.equals("")) {
						description = RalleeApp.getInstance().getString(
								R.string.no_description_group);
					}
					if (tvGroupDescription != null) {
						tvGroupDescription.setText(description);
					}
				}
			}
		} catch (JSONException e) {

			e.printStackTrace();
			// } catch (MalformedURLException e) {
			//
			// e.printStackTrace();
			// } catch (IOException e) {
			//
			// e.printStackTrace();
		}

	}

	private void fetchFBgroupMembers(String groupId) {

		try {
			Bundle params = new Bundle();

			// All FB friends in the group

			// String getFriendMembersQuery =
			// "SELECT uid FROM group_member WHERE gid = "
			// + data.getString(columnIndex)
			// +
			// " AND uid IN ( SELECT uid2 FROM friend WHERE uid1=me()) ORDER BY rand() LIMIT 4";

			// FB friends in selected group who have Rallee app installed

			String getFriendMembersQuery = "SELECT uid FROM user WHERE is_app_user='true' AND uid IN "
					+ "( SELECT uid FROM group_member WHERE gid = "
					+ groupId
					+ " AND uid IN ( SELECT uid2 FROM friend WHERE uid1=me()) )";
//			Log.i("FriendMembersQuery", getFriendMembersQuery);

			params.putString("q", getFriendMembersQuery);
			Session session = Session.getActiveSession();
			Request request = new Request(session, "/fql", params,
					HttpMethod.GET);
			Response response = Request.executeAndWait(request);
//			Log.i("fbGroups", response.toString());

			if (response.getError() == null) {

				GraphObject data = response.getGraphObject();
				JSONArray membersIds = ((JSONArray) data.asMap().get("data"));

				String selection = "";
				List<String> FBIds = new ArrayList<String>();

				if (membersIds.length() > 0) {
					selection = DbContentProvider.USER_COL_ID;
					for (int i = 0; i < membersIds.length(); i++) {
						FBIds.add(membersIds.getJSONObject(i).getString("uid"));
						if (i == 0) {
							selection += " IN ('"
									+ Utility.networkPrefix
									+ membersIds.getJSONObject(i).getString(
											"uid") + "'";
						} else {
							selection += ", '"
									+ Utility.networkPrefix
									+ membersIds.getJSONObject(i).getString(
											"uid") + "'";
						}
					}
					selection += ")";
				}

				Bundle bundle = new Bundle();
				bundle.putString("select", selection);
				getLoaderManager().restartLoader(FB_GROUP_DETAILS_FRIENDS,
						bundle, FacebookGroupDetailsFragment.this);
				int iNumToGet = FBIds.size() + 4;
				String getNonFriendMembersQuery = "SELECT uid, pic_square FROM user WHERE is_app_user='true' AND uid IN "
						+ "( SELECT uid FROM group_member WHERE gid = "
						+ groupId
						+ ") AND uid!=me() ORDER BY rand() LIMIT "
						+ iNumToGet;

//				Log.i("FBGroupsList", getNonFriendMembersQuery);
				params.clear();

				params.putString("q", getNonFriendMembersQuery);

				response = null;

				session = Session.getActiveSession();
				request = new Request(session, "/fql", params, HttpMethod.GET);
				response = Request.executeAndWait(request);

//				Log.i("fbGroups", response.toString());

				if (response.getError() == null) {

					data = response.getGraphObject();
					membersIds = ((JSONArray) data.asMap().get("data"));

					nonFriendMembersStringPair.clear();
					int j = 0;
					for (int i = 0; i < membersIds.length(); i++) {
						String uid = membersIds.getJSONObject(i).getString(
								"uid");
						boolean addNew = true;
						for (String toCmpr : FBIds) {
							if (toCmpr.equals(uid)) {
								addNew = false;

								break;
							}
						}

						if (addNew && j < 4) {
							StringPair sp = new StringPair();
							sp.setId(uid);
							sp.setUrl(membersIds.getJSONObject(i).getString(
									"pic_square"));
							nonFriendMembersStringPair.add(sp);
							j++;
						}
					}

					for (; j < 4; j++) {
						nonFriendMembersStringPair.add(new StringPair());
					}
				}
			}

		} catch (JSONException e) {

			e.printStackTrace();
			// } catch (MalformedURLException e) {
			//
			// e.printStackTrace();
			// } catch (IOException e) {
			//
			// e.printStackTrace();
		}

	}

	private SimpleCursorAdapter createOnlineFriendsMembersAdapter() {
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
				return v;
			}
		};

		FriendsPictureModel = new FriendsGetProfilePics();
		FriendsPictureModel.setListener(adapter);

		adapter.setViewBinder(new ViewBinder() {

			@Override
			public boolean setViewValue(View view, final Cursor cursor,
					final int columnIndex) {
				if (view.getId() == R.id.profile_pic) {
					((ImageView) view).setImageBitmap(FriendsPictureModel.getImage(
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
					view.setTag(cursor.getString(columnIndex));
					return true;
				}
				return false;
			}
		});

		return adapter;
	}

	private void createFBGroupPicture() {
		View v = getView().findViewById(R.id.rlGroupImage);

		if (v != null) {
			if (channelDetailsPictureModel == null) {
				channelDetailsPictureModel = new FriendsGetProfilePics();
				channelDetailsPictureModel.setListener(mMergeAdapter);
			}
			List<ImageView> ivs = new ArrayList<ImageView>();
			ivs.clear();
			ivs.add((ImageView) v.findViewById(R.id.pic1));
			ivs.add((ImageView) v.findViewById(R.id.pic2));
			ivs.add((ImageView) v.findViewById(R.id.pic3));
			ivs.add((ImageView) v.findViewById(R.id.pic4));
			for (int i = 0; i < GroupIconStringPair.size(); i++) {
				StringPair rf = GroupIconStringPair.get(i);
				if (rf.isInitialized()) {
					ivs.get(i).setImageBitmap(
							Utility.getBitmap(rf.getUrl(),
									RalleeApp.getInstance()));
//					Log.i(FRAGMENT_TAG, "setGroupIcon: " + rf.toString());
				} else {
					ivs.get(i).setImageResource(R.drawable.icon);
				}

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

		Session session = Session.getActiveSession();
		Request request = new Request(session, "/fql", params, HttpMethod.GET);
		try {

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

	public void notifyFBgroupsChange() {

		if (getRRService() != null) {
			int usercount = 0;
			String groupChannalName = "_fbgroup_" + sGroupId;
			for (RRChannels ch : getRRService().getChannelList()) {

				if (ch.name != null && ch.name.equals(groupChannalName)) {
					usercount = ch.userCount;
					break;
				}
			}
			String text = RalleeApp.getInstance().getString(
					R.string.label_in_group)
					+ usercount;
			tvNumberOfMembers.setText(text);
			mMergeAdapter.notifyDataSetChanged();
		}

	}

	private void changeItemsViews(View view) {
		if (getRRService() != null) {
			TextView tv = ((TextView) view.findViewById(R.id.title));
			ImageView imgStatus = ((ImageView) view
					.findViewById(R.id.img_led_status));
			FrameLayout darkenView = ((FrameLayout) view
					.findViewById(R.id.darkenView));
			RelativeLayout picLayout = ((RelativeLayout) view
					.findViewById(R.id.pic_layout));
			List<RRUser> onlineUserList = getRRService().getUserList();
			List<RRChannels> channels = getRRService().getChannelList();
			int fbChannelId = 0;
			for (RRChannels channel : channels) {
				if (channel.name.equals("_fbgroup_" + fbGroupId)) {
					fbChannelId = channel.id;
				}
			}
			boolean onFBChannel = false;
			boolean isOnline = false;
			if (tv != null) {
				String userId = (String) tv.getTag();
				if (userId != null && onlineUserList != null) {
					for (RRUser member : onlineUserList) {
						if (member != null && member.userName != null
								&& member.userName.equals(userId)) {
							isOnline = true;
							if (fbChannelId != 0 && member.getChannel() != null
									&& member.getChannel().id == fbChannelId) {
								onFBChannel = true;
							}
							break;
						}
					}
				}
			}
			if (onFBChannel) {
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
