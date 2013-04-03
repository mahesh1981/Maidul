package com.radiorunt.activities.fragments;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.ScaleAnimation;
import android.widget.FrameLayout;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.SimpleAdapter.ViewBinder;
import android.widget.TextView;

import com.radiorunt.R;
import com.radiorunt.activities.DbContentProvider;
import com.radiorunt.activities.HomeActivity;
import com.radiorunt.businessobjects.RRUser;
import com.radiorunt.facebook.FacebookUserDataLoader;
import com.radiorunt.facebook.FriendsGetProfilePics;
import com.radiorunt.facebook.Utility;
import com.radiorunt.services.GetGroupsFromFBService;
import com.radiorunt.services.ParticipantsLoader;
import com.radiorunt.services.RadioRuntService;
import com.radiorunt.utilities.Globals;
import com.radiorunt.utilities.RalleeApp;

public class ParticipantsFragment extends Fragment implements OnClickListener,
		LoaderManager.LoaderCallbacks<List<HashMap<String, String>>> {

	/**
	 * Tag that can be used to identify fragment within activity manager
	 */
	public static final String FRAGMENT_TAG = "participantsFragment";
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
	 * adapter of participants on channel
	 */
	private SimpleAdapter mParticipantsAdapter;
	private FriendsGetProfilePics participantsPictureModel;

	/**
	 * list of usernames of users that are on channel
	 */
	private List<String> participants;

	List<HashMap<String, String>> participantsFillMaps;

	private static final int LOADER_PARTICIPANTS = 1;

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
		// initControlListeners();
	}

	@Override
	public void onResume() {
		Globals.logInfo(this, "onResume");
		super.onResume();
		getLoaderManager().initLoader(LOADER_PARTICIPANTS, null, this);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	private void initControls() {
		participantsFillMaps = new ArrayList<HashMap<String, String>>();

		participants = new ArrayList<String>();
		gallery = (Gallery) getView().findViewById(R.id.galleryOfOnlineFriends);

		mParticipantsAdapter = createParticipantsAdapter(participantsFillMaps);

		gallery.setAdapter(mParticipantsAdapter);
		gallery.setCallbackDuringFling(false);

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

	private SimpleAdapter createParticipantsAdapter(
			List<HashMap<String, String>> dataList) {
		SimpleAdapter adapter = new SimpleAdapter(getActivity(), dataList,
				R.layout.user_list_item, new String[] { "rowid", "rowid" },
				new int[] { R.id.title, R.id.profile_pic }) {
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				// TODO Auto-generated method stub
				View v = super.getView(position, convertView, parent);
				changeItemsViews(v);
				return v;
			}
		};

		participantsPictureModel = new FriendsGetProfilePics();
		participantsPictureModel.setListener(adapter);

		adapter.setViewBinder(new ViewBinder() {
			@Override
			public boolean setViewValue(View view, Object data,
					String textRepresentation) {
				if (view.getId() == R.id.profile_pic) {
					Integer location = Integer.parseInt(textRepresentation);
					Map<String, String> user = participantsFillMaps
							.get(location);
					String picture = user
							.get(DbContentProvider.USER_COL_PIC_URL);
					String id = user.get(DbContentProvider.USER_COL_ID);

					((ImageView) view).setImageBitmap(participantsPictureModel
							.getImage(id, picture, RalleeApp.getInstance()));
					return true;
				}
				if (view.getId() == R.id.title) {
					Integer location = Integer.parseInt(textRepresentation);
					Map<String, String> user = participantsFillMaps
							.get(location);
					String name = user.get(DbContentProvider.USER_COL_NAME);
					String id = user.get(DbContentProvider.USER_COL_ID);
					((TextView) view).setText(trimToWholeFirstAndOneRest(name));
					((TextView) view).setTag(id);
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
			getLoaderManager().restartLoader(LOADER_PARTICIPANTS, null, this);
		} catch (IllegalStateException ise) {

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

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub

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
				for (String memberId : participants) {
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

	private List<HashMap<String, String>> getParticipants() {
		List<RRUser> participants = getRRService().getUserList();
		int currentChannelId = getRRService().getCurrentChannel().id;
		List<HashMap<String, String>> participantsMap = new ArrayList<HashMap<String, String>>();
		for (RRUser participant : participants) {
			if (participant.getChannel().id == currentChannelId) {
				String fbIdOfUser = Utility.parseSNData(participant.userName)
						.getAsString(Utility.SOCIAL_NETWORK_ID);
				RRUser usr = FacebookUserDataLoader.getFbUserById(fbIdOfUser);
				if (usr != null) {
					HashMap<String, String> map = new HashMap<String, String>();
					map.put("rowid", "" + participantsMap.size());
					map.put(DbContentProvider.USER_COL_ID, usr.userName);
					map.put(DbContentProvider.USER_COL_NAME, usr.FirstName);
					map.put(DbContentProvider.USER_COL_PIC_URL, usr.picUrl);
					participantsMap.add(map);
				}
			}
		}
		return participantsMap;
	}

	@Override
	public Loader<List<HashMap<String, String>>> onCreateLoader(int id,
			Bundle bundle) {
		if (id == LOADER_PARTICIPANTS) {
			if (getRRService() == null) {
				return null;
			}
			if (getRRService() != null
					&& getRRService().getCurrentChannel() == null) {
				return null;
			}

			if (getRRService() != null && getRRService().getUserList() == null) {
				return null;
			}

			if (getRRService() != null
					&& getRRService().getCurrentChannel() == null) {
				return null;
			}

			if (getRRService() != null
					&& getRRService().getCurrentChannel() != null
					&& getRRService().getCurrentChannel().id == 0) {
				return null;
			}

			Globals.logInfo(this, getRRService().getUserList().toString());
			return new ParticipantsLoader(getActivity(), getRRService()
					.getCurrentChannel().id, getRRService().getUserList());
		} else {
			return null;
		}
	}

	@Override
	public void onLoadFinished(Loader<List<HashMap<String, String>>> loader,
			List<HashMap<String, String>> data) {
		if (loader.getId() == LOADER_PARTICIPANTS) {
			participantsFillMaps.clear();
			if (data != null) {
			}
			for (HashMap<String, String> item : data) {
				Globals.logInfo(this, data.toString());
				participantsFillMaps.add(item);
				// addUserToCurrentGroup(item.get(DbContentProvider.USER_COL_ID));
			}
			mParticipantsAdapter.notifyDataSetChanged();
		}
	}

	@Override
	public void onLoaderReset(Loader<List<HashMap<String, String>>> loader) {
		if (loader.getId() == LOADER_PARTICIPANTS) {
			mParticipantsAdapter.notifyDataSetInvalidated();
		}
	}
}
