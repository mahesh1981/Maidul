package com.radiorunt.activities.fragments;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.radiorunt.R;
import com.radiorunt.activities.DbContentProvider;
import com.radiorunt.activities.HomeActivity;
import com.radiorunt.businessobjects.RRUser;
import com.radiorunt.facebook.FriendsGetProfilePics;
import com.radiorunt.services.GetGroupsFromFBService;
import com.radiorunt.services.RadioRuntService;
import com.radiorunt.utilities.Globals;
import com.radiorunt.utilities.RRServerProxyHelper;
import com.radiorunt.utilities.MergeAdapter;
import com.radiorunt.utilities.RalleeApp;
import com.radiorunt.utilities.Settings;
import com.radiorunt.utilities.StringPair;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter.ViewBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.ScaleAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class FriendsDockFragment extends Fragment implements
		LoaderManager.LoaderCallbacks<Cursor>, OnClickListener {

	/**
	 * Tag that can be used to identify fragment within activity manager
	 */
	public static final String FRAGMENT_TAG = "friendsDockFragment";

	public static final String START_WITHOUT_LISTENERS = "startWithoutListeners";

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
	 * adapter of friends who are online
	 */
	// private SimpleCursorAdapter mOnlineFriendsAdapter;
	private MergeAdapter mGalleryMergeAdapter;
	private ScaleAnimation shrinkByYAxis;
	private ScaleAnimation expandByYAxis;
	private boolean galleryShrinked = true;
	private TextView tvNumOfGroups;
	private int numOfPrivateGroups = 0;
	private int numOfFBGroups = 0;
	private int numOfPublicGroups = 0;
	private TextView tvNumOfAlerts;
	private TextView tvNumOfVoicemails;
	private TextView tvNumOfFriends;
	private TextView tvNumOfRalleeFriends;
	private TextView tvNumOfOnlineFriends;
	private TextView tvNumOfFavs;

	private List<StringPair> randomFriends = new ArrayList<StringPair>(4);
	private List<StringPair> randomOnlineFriends = new ArrayList<StringPair>(4);
	private List<StringPair> randomRalleeFriends = new ArrayList<StringPair>(4);

	private FriendsGetProfilePics onlineFriendsPictureModel;
	private FriendsGetProfilePics dockPictureModel;
	private View alertItem;
	private View aftereAlertSeparatorItem;
	private FriendsDockActionsInterface mFriendsDockActions;

	private boolean createdWithouListeners = false;

	/**
	 * id of online contacts loader
	 */

	private static final int LOADER_FB_CHANNELS = 7;
	private static final int LOADER_PRIVATE_CHANNELS = 6;
	private static final int LOADER_ALL_FRIENDS = 5;
	private static final int LOADER_ALERTS = 4;
	private static final int LOADER_ONLINE_FRIENDS = 3;
	private static final int LOADER_ALL_RALLEE_FRIENDS = 2;
	private static final int LOADER_PUBLIC_CHANNELS = 1;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			mFriendsDockActions = (FriendsDockActionsInterface) ((HomeActivity) activity).mHelper;
		} catch (ClassCastException e) {
			throw new ClassCastException(
					((HomeActivity) activity).mHelper.toString()
							+ " must implement FriendsDockActionsInterface");
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Globals.logInfo(this, "onCreateView");
		View v = inflater.inflate(R.layout.friends_dock, container, false);
		v.setOnTouchListener(new View.OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				Toast.makeText(getActivity(),
						R.string.default_waiting_for_connection, 100).show();
				return true;
			}
		});
		return v;
	}

	@Override
	public void onStart() {
		super.onStart();
		Globals.logInfo(this, "onStart");

		if (getArguments() != null) {
			createdWithouListeners = getArguments().getBoolean(
					START_WITHOUT_LISTENERS, false);
		}

		initControls();
		if (!createdWithouListeners) {
			initControlListeners();
		}

		Globals.logInfo(this, "onStart ends");
	}

	@Override
	public void onResume() {
		Globals.logInfo(this, "onResume");
		super.onResume();
		gallery.setSelection(2);
		notifyFriendsAdapterDataSetChange();

		Globals.logInfo(this, "onResume ends");
	}

	@Override
	public void onDestroy() {
		createdWithouListeners = false;
		super.onDestroy();
	}

	public void initControls() {
		gallery = (Gallery) getView().findViewById(R.id.galleryOfFriends);

		randomFriends.clear();
		randomOnlineFriends.clear();
		randomRalleeFriends.clear();
		for (int i = 0; i < 4; i++) {
			randomFriends.add(new StringPair());
			randomOnlineFriends.add(new StringPair());
			randomRalleeFriends.add(new StringPair());
		}

		createAnimations();

		mGalleryMergeAdapter = new MergeAdapter() {
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				View v = super.getView(position, convertView, parent);
				if (this.getItemViewType(position) == 5) {// Friends
//					Log.i(FRAGMENT_TAG,
//							"View: "
//									+ ((TextView) v
//											.findViewById(R.id.tvGroupName))
//											.getText());
					if (dockPictureModel == null) {
						dockPictureModel = new FriendsGetProfilePics();
						dockPictureModel.setListener(this);
					}

					TextView tvGroupName = ((TextView) v
							.findViewById(R.id.tvGroupName));
					if (tvGroupName != null) {
						tvGroupName.setText(R.string.label_friends);
					}

					List<ImageView> ivs = new ArrayList<ImageView>();
					ivs.clear();
					ivs.add((ImageView) v.findViewById(R.id.pic1));
					ivs.add((ImageView) v.findViewById(R.id.pic2));
					ivs.add((ImageView) v.findViewById(R.id.pic3));
					ivs.add((ImageView) v.findViewById(R.id.pic4));
					for (int i = 0; i < randomRalleeFriends.size(); i++) {
						StringPair rf = randomRalleeFriends.get(i);
						ImageView iv = ivs.get(i);
						if (rf != null && rf.isInitialized() && iv != null) {
							iv.setImageBitmap(dockPictureModel.getImage(
									rf.getId(), rf.getUrl(),
									RalleeApp.getInstance()));
//							Log.i(FRAGMENT_TAG,
//									"setImageBitmap: " + rf.toString());
						}
					}

				} else if (this.getItemViewType(position) == 6) {// Online
//					Log.i(FRAGMENT_TAG,
//							"View: "
//									+ ((TextView) v
//											.findViewById(R.id.tvGroupName))
//											.getText());
					if (dockPictureModel == null) {
						dockPictureModel = new FriendsGetProfilePics();
						dockPictureModel.setListener(this);
					}

					TextView tvGroupName = ((TextView) v
							.findViewById(R.id.tvGroupName));
					if (tvGroupName != null) {
						tvGroupName.setText(R.string.label_online);
					}

					List<ImageView> ivs = new ArrayList<ImageView>();
					ivs.add((ImageView) v.findViewById(R.id.pic1));
					ivs.add((ImageView) v.findViewById(R.id.pic2));
					ivs.add((ImageView) v.findViewById(R.id.pic3));
					ivs.add((ImageView) v.findViewById(R.id.pic4));
					for (int i = 0; i < randomOnlineFriends.size(); i++) {
						StringPair rf = randomOnlineFriends.get(i);
						ImageView iv = ivs.get(i);
						if (rf != null && rf.isInitialized() && iv != null) {
							iv.setImageBitmap(dockPictureModel.getImage(
									rf.getId(), rf.getUrl(),
									RalleeApp.getInstance()));
//							Log.i(FRAGMENT_TAG,
//									"setImageBitmap: " + rf.toString());
						} else {
							if (iv != null) {
								iv.setImageResource(R.drawable.icon);
//								Log.i(FRAGMENT_TAG,
//										"setImageBitmap: R.drawable.icon");
							}
						}
					}

				} else if (this.getItemViewType(position) == 8) {// Invite, one
																	// skip for
																	// delimiter
//					Log.i(FRAGMENT_TAG,
//							"View: "
//									+ ((TextView) v
//											.findViewById(R.id.tvGroupName))
//											.getText());
					if (dockPictureModel == null) {
						dockPictureModel = new FriendsGetProfilePics();
					}
					dockPictureModel.setListener(this);

					TextView tvGroupName = ((TextView) v
							.findViewById(R.id.tvGroupName));
					if (tvGroupName != null) {
						tvGroupName.setText(R.string.label_invite);
					}

					List<ImageView> ivs = new ArrayList<ImageView>();
					ivs.add((ImageView) v.findViewById(R.id.pic1));
					ivs.add((ImageView) v.findViewById(R.id.pic2));
					ivs.add((ImageView) v.findViewById(R.id.pic3));
					ivs.add((ImageView) v.findViewById(R.id.pic4));
					for (int i = 0; i < randomFriends.size(); i++) {
						StringPair rf = randomFriends.get(i);
						ImageView iv = ivs.get(i);
						if (rf.isInitialized() && iv != null) {
							iv.setImageBitmap(dockPictureModel.getImage(
									rf.getId(), rf.getUrl(),
									RalleeApp.getInstance()));
//							Log.i(FRAGMENT_TAG,
//									"setImageBitmap: " + rf.toString());
						}
					}
				}
				return v;
			}
		};
		if (dockPictureModel == null) {
			dockPictureModel = new FriendsGetProfilePics();
		}
		dockPictureModel.setListener(mGalleryMergeAdapter);
		// mOnlineFriendsAdapter = createOnlineFriendsAdapter();
		// Utility.model = new FriendsGetProfilePics();
		// Utility.model.setListener(mOnlineFriendsAdapter);
		// Utility.model.setListener(mGalleryMergeAdapter);

		// mGalleryMergeAdapter.addView(createVoicemailItem());

		alertItem = createAlertsItem();
		mGalleryMergeAdapter.addView(alertItem);
		if (alertItem != null) {
			alertItem.setVisibility(View.GONE);
		}
		aftereAlertSeparatorItem = createSeparator();
		mGalleryMergeAdapter.addView(aftereAlertSeparatorItem, false);
		if (aftereAlertSeparatorItem != null) {
			aftereAlertSeparatorItem.setVisibility(View.GONE);
		}

		mGalleryMergeAdapter.addView(createRandomItem());
		mGalleryMergeAdapter.addView(createChannelsItem());
		mGalleryMergeAdapter.addView(createSeparator(), false);
		mGalleryMergeAdapter.addView(createFriendsItem());
		// mGalleryMergeAdapter.addView(createFavsItem());
		mGalleryMergeAdapter.addView(createOnlineFriendsItem());
		// mGalleryMergeAdapter.addView(createSeparator(), false);
		// mGalleryMergeAdapter.addAdapter(mOnlineFriendsAdapter);
		mGalleryMergeAdapter.addView(createSeparator(), false);
		mGalleryMergeAdapter.addView(createInviteFriendsItem());

		gallery.setAdapter(mGalleryMergeAdapter);
		gallery.setCallbackDuringFling(false);

	}

	public void initControlListeners() {

		createdWithouListeners = false;
		if (getView() != null) {
			getView().setOnTouchListener(null);
		}

		gallery.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				int clickedItemType = parent.getAdapter().getItemViewType(
						position);

				if (clickedItemType != 6) {// 8) {// Online
					hideOnlineFriends(); // Hide OnlineFragment on any action
											// that is not OnlineFriends
				}

				// if (clickedItemType == 0) {// Voicemail
				// goToVoicemail();
				// } else
				if (clickedItemType == 0) {// Alerts
					// goToAlerts();
					if (mFriendsDockActions != null) {
						mFriendsDockActions.goToAlerts();
					}
				} else if (clickedItemType == 2) {// 3) {// Random, skip one for
													// separator
					goToRandom();

				} else if (clickedItemType == 3) {// 4) {// Channels
					// goToChannels();
					goToGroups();
				} else if (clickedItemType == 5) {// 6) {// Friends, skip one
													// for
					// separator
					if (mFriendsDockActions != null) {
						mFriendsDockActions.goToPeople();
					}
				} else if (clickedItemType == 6) {// 7) {// Online
					// Toast.makeText(getActivity(), "Online", 500).show();
					showHideOnlineFriends();
				} else if (clickedItemType == 8) {// Invite, skip one for
													// separator
					getHomeActivity().showInviteFriendsDialog();
				}

			}
		});

		if (getRRService() != null) {
			initLoaders();
		}
	}

	private void initLoaders() {
		getLoaderManager().restartLoader(LOADER_PUBLIC_CHANNELS, null, this);
		getLoaderManager().restartLoader(LOADER_PRIVATE_CHANNELS, null, this);
		getLoaderManager().restartLoader(LOADER_FB_CHANNELS, null, this);
		getLoaderManager().restartLoader(LOADER_ONLINE_FRIENDS, null, this);
		getLoaderManager().restartLoader(LOADER_ALL_RALLEE_FRIENDS, null, this);
		getLoaderManager().restartLoader(LOADER_ALL_FRIENDS, null, this);
		getLoaderManager().restartLoader(LOADER_ALERTS, null, this);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		// // first saving my state, so the bundle wont be empty.
		// // http://code.google.com/p/android/issues/detail?id=19917
		// outState.putString("WORKAROUND_FOR_BUG_19917_KEY",
		// "WORKAROUND_FOR_BUG_19917_VALUE");
		// super.onSaveInstanceState(outState);
	}

	// Container Activity must implement this interface
	public interface FriendsDockActionsInterface {
		public void goToAlerts();

		public void goToPeople();

		public void goToMainPanel();
	}

	/**
	 * Function that is called when Random button is clicked
	 * 
	 * it first checks is over18 set, if not it opens age confirmation dialog,
	 * if user is over 18 we call goToRandomforOver18 function, if user is under
	 * 18 we show him info dialog
	 */

	public void goToRandom() {

		String over18 = getHomeActivity().settings.getOver18();

		if (over18.equals(Settings.RANDOM_PREF_DEFAULT)) { // show confirm age
															// dialog
			getHomeActivity().showCustomDialog(2, R.string.over_18_title,
					R.string.over_18_message, R.string.label_over18,
					onClickOver18(Settings.RANDOM_PREF_YES),
					R.string.label_under18,
					onClickOver18(Settings.RANDOM_PREF_NO));
		} else if (over18.equals(Settings.RANDOM_PREF_YES)) {
			goToRandomForOver18();
		} else if (over18.equals(Settings.RANDOM_PREF_NO)) {
			getHomeActivity()
					.showCustomDialog(1, R.string.over_18_title,
							R.string.under_18_message, R.string.label_ok, null,
							0, null);
			over18 = com.radiorunt.utilities.Settings.ARRAY_JITTER_NONE;

		}
		;
	}

	/**
	 * Function that checks if user has accepted Random call feature
	 * 
	 * if not Random accept dialog is shown if yes GoRandom dialog is shown
	 */

	private void goToRandomForOver18() {

		String gorandom = getHomeActivity().settings.getGoRandom();
		if (gorandom.equals(Settings.RANDOM_PREF_DEFAULT)) {
			getHomeActivity().showCustomDialog(2, R.string.accept_random_title,
					R.string.accept_random_message, R.string.label_accept,
					onClickAcceptRandom(), R.string.label_cancel, null);
		} else if (gorandom.equals(Settings.RANDOM_PREF_YES)) {
			getHomeActivity().showCustomDialog(2, true, false,
					R.string.random_calls_title, R.string.random_call_message,
					R.string.label_go_random, onClickGoRandom(),
					R.string.label_cancel, null);
		} else if (gorandom.equals(Settings.RANDOM_PREF_NO)) {

			getHomeActivity().showCustomDialog(2, R.string.accept_random_title,
					R.string.accept_random_message, R.string.label_accept,
					onClickAcceptRandom(), R.string.label_cancel, null);
		}
	}

	/**
	 * Called when users answers age confirmation dialog
	 * 
	 * @param answer
	 *            Setting.RANDOM_PREF_YES or Setting.RANDOM_PREF_NO
	 * 
	 */

	private OnClickListener onClickOver18(final String answer) {
		return new OnClickListener() {
			@Override
			public void onClick(View v) {

				if (RalleeApp.getInstance().getRalleeUID().equals("ralleeUID")) {
					return;
				}
				String jsonString = "";
				try {
					JSONObject json = new JSONObject();
					json.put("user_id", RalleeApp.getInstance().getRalleeUID());
					json.put("value", answer);
					// JSONArray jsonA = new JSONArray();
					// jsonA.put(json);
					jsonString = json.toString();
				} catch (JSONException e) {
					e.printStackTrace();
				}
				// getHomeActivity().mMwCommLogic.registerSetOver18ServiceReceiver();
				RRServerProxyHelper.startSetOver18Service(getActivity(),
						jsonString);
				// getHomeActivity().mMwCommLogic.unregisterSetOver18ServiceReceiver();

				getHomeActivity().settings.setOver18(answer);

				if (answer.equals(Settings.RANDOM_PREF_YES))
					goToRandomForOver18();
			}
		};
	}

	/**
	 * Called when user accepts Random feature
	 * 
	 */

	private OnClickListener onClickAcceptRandom() {
		return new OnClickListener() {
			@Override
			public void onClick(View v) {
				// getHomeActivity().

				if (RalleeApp.getInstance().getRalleeUID().equals("ralleeUID")) {
					return;
				}
				String jsonString = "";
				try {
					JSONObject json = new JSONObject();
					json.put("user_id", RalleeApp.getInstance().getRalleeUID());
					json.put("value", Settings.RANDOM_PREF_YES);
					// JSONArray jsonA = new JSONArray();
					// jsonA.put(json);
					jsonString = json.toString();
				} catch (JSONException e) {
					e.printStackTrace();
				}

				// getHomeActivity().mMwCommLogic.registerSetRandomServiceReceiver();
				RRServerProxyHelper.startSetRandomService(getActivity(),
						jsonString);
				// getHomeActivity().mMwCommLogic.unregisterSetOver18ServiceReceiver();

				getHomeActivity().settings
						.setGoRandom(Settings.RANDOM_PREF_YES);

				getHomeActivity().showCustomDialog(2, true, false,
						R.string.random_calls_title,
						R.string.random_call_message, R.string.label_go_random,
						onClickGoRandom(), R.string.label_cancel, null);
			}
		};
	}

	/**
	 * 
	 */

	private OnClickListener onClickGoRandom() {
		return new OnClickListener() {
			@Override
			public void onClick(View v) {
				Bundle tag = (Bundle) v.getTag();
//				Log.i("VLADA", tag.toString());
				int near = tag.getInt("near", 0);
				getHomeActivity().goRandom(near);
			}
		};
	}

	// protected void goToChannels() {
	// Intent channelsIntent = new Intent(getActivity(),
	// ChannelsActivity.class);
	// getHomeActivity().WAS_ON_CHANNEL_LIST = 1;
	// startActivity(channelsIntent);
	// }

	protected void goToGroups() {

		Fragment ofFrag = getFragmentManager().findFragmentByTag(
				GroupsFragment.FRAGMENT_TAG);
		if (ofFrag == null) {
			Intent service = new Intent(RalleeApp.getInstance(),
					GetGroupsFromFBService.class);
			RalleeApp.getInstance().startService(service);

			FragmentTransaction ft = getFragmentManager()
					.beginTransaction()
					.setCustomAnimations(R.anim.slide_up_in,
							R.anim.slide_down_out, R.anim.slide_down_in,
							R.anim.slide_down_out)
					.replace(R.id.topSlot, new GroupsFragment(),
							GroupsFragment.FRAGMENT_TAG);
			ft.addToBackStack(null);
			// ft.commit();
			ft.commitAllowingStateLoss();
			getActivity().overridePendingTransition(0, 0);
		} else {
			getFragmentManager().popBackStack();
		}

	}

	protected void showHideOnlineFriends() {

		Fragment ofFrag = getFragmentManager().findFragmentByTag(
				OnlineFriendsFragment.FRAGMENT_TAG);
		if (ofFrag == null) {

			FragmentTransaction ft = getFragmentManager().beginTransaction()
					.replace(R.id.bottomExtensionPanelSlot,
							new OnlineFriendsFragment(),
							OnlineFriendsFragment.FRAGMENT_TAG);
			ft.addToBackStack(null);
			// ft.commit();
			ft.commitAllowingStateLoss();
			getActivity().overridePendingTransition(0, 0);
		} else {
			getFragmentManager().popBackStack();
		}

	}

	protected void hideOnlineFriends() {
		Fragment ofFrag = getFragmentManager().findFragmentByTag(
				OnlineFriendsFragment.FRAGMENT_TAG);
		if (ofFrag != null) {
			getFragmentManager().popBackStack();
		}

	}

	// private SimpleCursorAdapter createOnlineFriendsAdapter() {
	// SimpleCursorAdapter adapter = new SimpleCursorAdapter(getActivity(),
	// R.layout.user_list_item, null, new String[] {
	// DbContentProvider.USER_COL_NAME,
	// DbContentProvider.USER_COL_PIC_URL }, new int[] {
	// R.id.title, R.id.profile_pic }, 0) {
	//
	// @Override
	// public View getView(int position, View convertView, ViewGroup parent) {
	// // TODO Auto-generated method stub
	// View v = super.getView(position, convertView, parent);
	// // changeItemsViews(v);
	// return v;
	// }
	// };
	//
	// onlineFriendsPictureModel = new FriendsGetProfilePics();
	// onlineFriendsPictureModel.setListener(adapter);
	//
	// adapter.setViewBinder(new ViewBinder() {
	//
	// @Override
	// public boolean setViewValue(View view, final Cursor cursor,
	// final int columnIndex) {
	// if (view.getId() == R.id.profile_pic) {
	// ((ImageView) view).setImageBitmap(onlineFriendsPictureModel.getImage(
	// cursor.getString(cursor
	// .getColumnIndex(DbContentProvider.USER_COL_ID)),
	// cursor.getString(columnIndex), RalleeApp
	// .getInstance()));
	// return true;
	// }
	// if (view.getId() == R.id.title) {
	// ((TextView) view).setText(trimToWholeFirstAndOneRest(cursor
	// .getString(columnIndex)));
	// ((TextView) view).setTag(cursor.getString(cursor
	// .getColumnIndex(DbContentProvider.USER_COL_ID)));
	// return true;
	// }
	// return false;
	// }
	// });
	//
	// return adapter;
	// }

	private View createSeparator() {
		LayoutInflater inflater = LayoutInflater.from(getActivity());
		View v = inflater.inflate(R.layout.galery_item_separator, null);
		return v;
	}

	// private View createVoicemailItem() {
	// LayoutInflater inflater = LayoutInflater.from(getActivity());
	// View v = inflater.inflate(R.layout.gallery_item_voicemail, null);
	// tvNumOfVoicemails = (TextView) v.findViewById(R.id.tvNumOfVoicemail);
	// return v;
	// }

	private View createAlertsItem() {
		LayoutInflater inflater = LayoutInflater.from(getActivity());
		View v = inflater.inflate(R.layout.gallery_item_alerts, null);
		tvNumOfAlerts = (TextView) v.findViewById(R.id.tvNumOfAlerts);
		return v;
	}

	private View createChannelsItem() {
		LayoutInflater inflater = LayoutInflater.from(getActivity());
		View v = inflater.inflate(R.layout.gallery_item_channels, null);
		tvNumOfGroups = (TextView) v.findViewById(R.id.tvNumOfChannels);
		// if (HomeActivity.permanentChannels != null) {
		// tvNumOfGroups.setText("" + HomeActivity.permanentChannels.size());
		// }
		return v;
	}

	private View createRandomItem() {
		LayoutInflater inflater = LayoutInflater.from(getActivity());
		View v = inflater.inflate(R.layout.gallery_item_random, null);
		return v;
	}

	private View createFriendsItem() {
		LayoutInflater inflater = LayoutInflater.from(getActivity());
		View v = inflater.inflate(R.layout.galery_item_user_group, null);
		tvNumOfRalleeFriends = (TextView) v
				.findViewById(R.id.tvNumberOfMembers);
		((TextView) v.findViewById(R.id.tvGroupName))
				.setText(R.string.label_friends);
		((ImageView) v.findViewById(R.id.imgGroupType))
				.setImageResource(R.drawable.dock_icon_friends_small);
		return v;
	}

	private View createFavsItem() {
		LayoutInflater inflater = LayoutInflater.from(getActivity());
		View v = inflater.inflate(R.layout.galery_item_user_group, null);
		tvNumOfFavs = (TextView) v.findViewById(R.id.tvNumberOfMembers);
		((TextView) v.findViewById(R.id.tvGroupName))
				.setText(R.string.label_favs);
		((ImageView) v.findViewById(R.id.imgGroupType))
				.setImageResource(R.drawable.dock_icon_favs_small);
		return v;
	}

	private View createOnlineFriendsItem() {
		LayoutInflater inflater = LayoutInflater.from(getActivity());
		View v = inflater.inflate(R.layout.galery_item_user_group, null);
		tvNumOfOnlineFriends = (TextView) v
				.findViewById(R.id.tvNumberOfMembers);
		((TextView) v.findViewById(R.id.tvGroupName))
				.setText(R.string.label_online);
		((ImageView) v.findViewById(R.id.imgGroupType))
				.setImageResource(R.drawable.main_ui_speaker_led);
		return v;
	}

	private View createInviteFriendsItem() {

		LayoutInflater inflater = LayoutInflater.from(getActivity());
		View v = inflater.inflate(R.layout.galery_item_user_group, null);
		tvNumOfFriends = (TextView) v.findViewById(R.id.tvNumberOfMembers);
		((TextView) v.findViewById(R.id.tvGroupName))
				.setText(R.string.label_invite);
		((ImageView) v.findViewById(R.id.imgGroupType))
				.setVisibility(View.GONE);
		return v;
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
		// mAdapter.notifyDataSetChanged();
		// gallery.startAnimation(shrinkByYAxis);
		// galleryShrinked = true;
		try {

			getLoaderManager().restartLoader(LOADER_ONLINE_FRIENDS, null, this);

			OnlineFriendsFragment ofFrag = (OnlineFriendsFragment) getFragmentManager()
					.findFragmentByTag(OnlineFriendsFragment.FRAGMENT_TAG);
			if (ofFrag != null) {
				ofFrag.notifyFriendsAdapterDataSetChange();
			}
		} catch (IllegalStateException ise) {

		}

	}

	// public void notifyChannelsAdapterDataSetChange() {
	//
	// getLoaderManager().restartLoader(LOADER_PRIVATE_CHANNELS, null, this);
	// getLoaderManager().restartLoader(LOADER_PUBLIC_CHANNELS, null, this);
	// getLoaderManager().restartLoader(LOADER_FB_CHANNELS, null, this);
	// }

	class ViewHolder {
		ImageView imgUserPicture;
		ImageView imgStatusLed;
		ImageView imgMicTalking;
		TextView title;
	}

	class ItemViewHolder {
		ImageView itemPicture;
	}

	class FriendlistViewHolder {
		ImageView profPic1;
		ImageView profPic2;
		ImageView profPic3;
		ImageView profPic4;
	}

	private void createAnimations() {

		shrinkByYAxis = new ScaleAnimation(1.0f, 1.0f, 1.0f, 0.0f,
				ScaleAnimation.RELATIVE_TO_PARENT, 0.5f,
				ScaleAnimation.RELATIVE_TO_PARENT, 0.5f);
		shrinkByYAxis.setDuration(500);

		expandByYAxis = new ScaleAnimation(1.0f, 1.0f, 0.0f, 1.0f,
				ScaleAnimation.RELATIVE_TO_PARENT, 0.5f,
				ScaleAnimation.RELATIVE_TO_PARENT, 0.5f);
		expandByYAxis.setDuration(500);

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
		if (id == LOADER_ONLINE_FRIENDS) {
			List<RRUser> rrUsers = null;
			if (getRRService() == null) {
				rrUsers = new ArrayList<RRUser>();
//				Log.i("nullPointer", "mService is NULL");
			} else {

//				Log.i("nullPointer", "mService is OK");
				rrUsers = getRRService().getUserList();
			}
			String selectionString = "_id IN (";
			for (int i = 0; i < rrUsers.size(); i++) {
				if (i == 0) {
					selectionString += "'" + rrUsers.get(i).userName + "'";
				} else {
					selectionString += ", '" + rrUsers.get(i).userName + "'";
				}
			}

			selectionString += ")";

			Uri baseUri = DbContentProvider.CONTENT_URI_USER_TABLE;
			String select = null;

			// String selectionWhereIn = bundle.getString("idsOfOnlineUsers");
			String selectionWhereIn = selectionString;

			if (selectionWhereIn != null && !selectionWhereIn.equals("")) {
				select = selectionWhereIn;
				// Log.i(FRAGMENT_TAG, "selection part:" + select);
			}

			String order = DbContentProvider.USER_COL_NAME
					+ " COLLATE LOCALIZED ASC";
			return new CursorLoader(RalleeApp.getInstance(), baseUri, null,
					select, null, order);
		} else if (id == LOADER_ALL_RALLEE_FRIENDS) {

			Uri baseUri = DbContentProvider.CONTENT_URI_USER_TABLE;
			String select = DbContentProvider.USER_COL_INSTALLED + "=?";
			String[] selectionArguments = new String[] { "1" };

			String order = DbContentProvider.USER_COL_NAME
					+ " COLLATE LOCALIZED ASC";
			return new CursorLoader(getActivity().getApplicationContext(),
					baseUri, null, select, selectionArguments, order);
		} else if (id == LOADER_ALL_FRIENDS) {

			Uri baseUri = DbContentProvider.CONTENT_URI_USER_TABLE;
			String select = null;
			String[] selectionArguments = null;

			String order = DbContentProvider.USER_COL_NAME
					+ " COLLATE LOCALIZED ASC";
			return new CursorLoader(getActivity().getApplicationContext(),
					baseUri, null, select, selectionArguments, order);
		} else if (id == LOADER_PRIVATE_CHANNELS || id == LOADER_FB_CHANNELS
				|| id == LOADER_PUBLIC_CHANNELS) {
			Uri baseUri = null;
			switch (id) {
			case LOADER_PRIVATE_CHANNELS:
				baseUri = DbContentProvider.CONTENT_URI_PRIVATE_GROUP_TABLE;
				break;
			case LOADER_FB_CHANNELS:
				baseUri = DbContentProvider.CONTENT_URI_FB_GROUPS_TABLE;
				break;
			case LOADER_PUBLIC_CHANNELS:
				baseUri = DbContentProvider.CONTENT_URI_CHANNEL_TABLE;
				break;
			}
			String select = null;

			String order = null;
			return new CursorLoader(getActivity().getApplicationContext(),
					baseUri, null, select, null, order);
		} else if (id == LOADER_ALERTS) {
			Uri baseUri = DbContentProvider.CONTENT_URI_MISSED_CALL_TABLE;
			String select = null;

			String order = null;
			return new CursorLoader(getActivity().getApplicationContext(),
					baseUri, null, select, null, order);
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
		if (loaderId == LOADER_ONLINE_FRIENDS) {
			// mOnlineFriendsAdapter.swapCursor(data);
			// if (galleryShrinked) {
			// galleryShrinked = false;
			// gallery.startAnimation(expandByYAxis);
			// }
			if (data != null) {
				if (tvNumOfOnlineFriends != null) {
					tvNumOfOnlineFriends.setText("" + data.getCount());
				}

				setThumbs(data, randomOnlineFriends, true);
				// mGalleryMergeAdapter.notifyDataSetChanged();
			}
		} else if (loaderId == LOADER_ALL_RALLEE_FRIENDS) {
			// mOnlineFriendsAdapter.swapCursor(data);
			// if (galleryShrinked) {
			// galleryShrinked = false;
			// gallery.startAnimation(expandByYAxis);
			// }
			if (data != null) {
				if (tvNumOfRalleeFriends != null) {
					tvNumOfRalleeFriends.setText("" + data.getCount());
				}
				setThumbs(data, randomRalleeFriends);
				// mGalleryMergeAdapter.notifyDataSetChanged();
			}

		} else if (loaderId == LOADER_ALL_FRIENDS) {
			// mOnlineFriendsAdapter.swapCursor(data);
			// if (galleryShrinked) {
			// galleryShrinked = false;
			// gallery.startAnimation(expandByYAxis);
			// }
			if (data != null) {
				if (tvNumOfFriends != null) {
					tvNumOfFriends.setText("" + data.getCount());
				}
				setThumbs(data, randomFriends);
				// mGalleryMergeAdapter.notifyDataSetChanged();
			}

		} else if (loaderId == LOADER_PRIVATE_CHANNELS
				|| loaderId == LOADER_FB_CHANNELS
				|| loaderId == LOADER_PUBLIC_CHANNELS) {
			switch (loaderId) {
			case LOADER_PRIVATE_CHANNELS:
				if (data != null) {
					numOfPrivateGroups = data.getCount();
				}
				break;
			case LOADER_FB_CHANNELS:
				if (data != null) {
					numOfFBGroups = data.getCount();
				}
				break;
			case LOADER_PUBLIC_CHANNELS:
				if (data != null) {
					numOfPublicGroups = data.getCount();
				}
				break;
			}
			// mOnlineFriendsAdapter.swapCursor(data);
			// if (galleryShrinked) {
			// galleryShrinked = false;
			// gallery.startAnimation(expandByYAxis);
			// }

			if (tvNumOfGroups != null) {
				tvNumOfGroups
						.setText(""
								+ (numOfPrivateGroups + numOfPublicGroups + numOfFBGroups));
			}

		} else if (loaderId == LOADER_ALERTS) {
			if (data != null) {
				int numOfAlerts = data.getCount();
				if (numOfAlerts > 0) {
					if (alertItem != null) {
						alertItem.setVisibility(View.VISIBLE);
						if (tvNumOfAlerts != null) {
							tvNumOfAlerts.setText("" + numOfAlerts);
						}

					}
					if (aftereAlertSeparatorItem != null) {
						aftereAlertSeparatorItem.setVisibility(View.VISIBLE);
					}
				} else {
					if (alertItem != null) {
						alertItem.setVisibility(View.GONE);
					}
					if (aftereAlertSeparatorItem != null) {
						aftereAlertSeparatorItem.setVisibility(View.GONE);
					}
					if (gallery != null) {
						gallery.setSelection(2);
					}
				}
				// mGalleryMergeAdapter.notifyDataSetChanged();
			}
		}
	}

	/**
	 * @param loader
	 *            which loading process is being reseted
	 */
	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		int loaderId = loader.getId();
		if (loaderId == LOADER_ONLINE_FRIENDS) {
			// mOnlineFriendsAdapter.swapCursor(null);

			// mGalleryMergeAdapter.notifyDataSetInvalidated();
		} else if (loaderId == LOADER_ALL_RALLEE_FRIENDS) {

			// mGalleryMergeAdapter.notifyDataSetInvalidated();

		} else if (loaderId == LOADER_ALL_FRIENDS) {

			// mGalleryMergeAdapter.notifyDataSetInvalidated();

			// } else if (loaderId == LOADER_CHANNELS) {
			//
			// // mGalleryMergeAdapter.notifyDataSetInvalidated();

		} else if (loaderId == LOADER_ALERTS) {

			// mGalleryMergeAdapter.notifyDataSetInvalidated();

		}

	}

	private void setThumbs(Cursor userCursor, List<StringPair> users) {
		setThumbs(userCursor, users, false);
	}

	private void setThumbs(Cursor userCursor, List<StringPair> users,
			boolean calculateOnline) {
		if (userCursor == null) {
			return;
		}

		Random randomGenerator = new Random();

		int size = userCursor.getCount();

		List<Integer> candidates = new ArrayList<Integer>();
		for (int i = 0; i < size; i++) {
			candidates.add(i);
		}

		List<RRUser> onlineUsers = null;
		if (getRRService() != null) {
			onlineUsers = getRRService().getUserList();
		}

		if (calculateOnline && onlineUsers != null) {
			for (int usersIndex = 0; usersIndex < users.size(); usersIndex++) {
				boolean hasLeft = true;
				if (users.get(usersIndex).isInitialized()) {
					for (RRUser onlineUser : onlineUsers) {
						String userId = onlineUser.userName;

						if (userId != null
								&& userId.equals(users.get(usersIndex).getId())) {
							hasLeft = false;
							break;
						}
					}

					if (hasLeft) {
						users.get(usersIndex).deinitailize();
						mGalleryMergeAdapter.notifyDataSetChanged();
					}
				}
			}
		}

		for (int j = 0; j < users.size(); j++) {
			StringPair user = users.get(j);
			while (!user.isInitialized() && candidates.size() != 0) {

				userCursor.moveToPosition(candidates.remove(randomGenerator
						.nextInt(candidates.size())));

				String id = userCursor.getString(userCursor
						.getColumnIndex(DbContentProvider.USER_COL_ID));
				int k = 3;
				boolean exists = false;
				while (k > -1) {
					if (id.equals(users.get(k).getId())) {
						exists = true;
					}
					k--;
				}

				if (!exists) {
					user.setId(userCursor.getString(userCursor
							.getColumnIndex(DbContentProvider.USER_COL_ID)));
					user.setUrl(userCursor.getString(userCursor
							.getColumnIndex(DbContentProvider.USER_COL_PIC_URL)));
//					Log.i(FRAGMENT_TAG, "user2 set: " + user.toString());

					mGalleryMergeAdapter.notifyDataSetChanged();
				}
			}
		}

	}
	// public void updateChannelsState() {
	// if (tvNumOfChannels != null) {
	// tvNumOfChannels.setText("" + HomeActivity.permanentChannels.size());
	// }
	//
	// }
}
