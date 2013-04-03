package com.radiorunt.utilities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.radiorunt.R;
import com.radiorunt.services.TtsProvider;
import com.radiorunt.services.audio.AudioOutputHost;
import com.radiorunt.businessobjects.RRUser;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class UserListAdapter extends BaseAdapter {
	Comparator<RRUser> userComparator = new Comparator<RRUser>() {
		@Override
		public int compare(final RRUser object1, final RRUser object2) {
			return object1.userName.compareTo(object2.userName);
		}
	};

	/**
	 * Tracks the current row elements of a user.
	 */
	private final Context context;
	private final Map<Integer, RRUser> users = new HashMap<Integer, RRUser>();
	private final Map<Integer, String> visibleUserNames = new HashMap<Integer, String>();
	private final List<RRUser> visibleUserList = new ArrayList<RRUser>();
	private final ListView stupidList;
	private int visibleChannel = -1;

	private final Runnable visibleUsersChangedCallback;

	int totalViews = 0;

	public UserListAdapter(final Context context, final ListView stupidList,
			final Runnable visibleUsersChangedCallback) {
		this.context = context;
		this.stupidList = stupidList; // Stupid list is stupid.
		this.visibleUsersChangedCallback = visibleUsersChangedCallback;
	}

	@Override
	public int getCount() {
		return this.visibleUserList.size();
	}

	@Override
	public Object getItem(final int arg0) {
		return this.visibleUserList.get(arg0);
	}

	@Override
	public long getItemId(final int arg0) {
		return this.visibleUserList.get(arg0).session;
	}

	/**
	 * Get a View for displaying data in the specified position.
	 * 
	 * Since UserListAdapter can refresh individual users it is important that
	 * it maintains a 1:1 relationship between user, View and RowElement.
	 * <p>
	 * The logical trivial relationships are listed below. These relationships
	 * cannot be 1:* relationships in any case.
	 * <ul>
	 * <li>User id maps to one RowElement through userElements.
	 * <li>RowElement has one View as its member.
	 * <li>View has a single tag that is set to the user id.
	 * </ul>
	 * The relationships that must be enforced are listed below. These
	 * relationships may become 1:* relationships.
	 * <ul>
	 * <li>Several users may own a single RowElement in userElements. (I)
	 * <li>Several RowElements may refer to the same View. (II)
	 * <li>Several Views may be tagged with same user id. (III)
	 * </ul>
	 * The enforcing for the latter three rules is made either when using an old
	 * view that is currently tied to a user (a) or when creating a new view for
	 * a user that is currently tied to another view (b).
	 */
	// Ivan
	// @Override
	// public final View getView(final int position, View v, final ViewGroup
	// parent) {
	// // All views are the same.
	// if (v == null) {
	// final LayoutInflater inflater = (LayoutInflater)
	// this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	// v = inflater.inflate(R.layout.channel_user_row, null);
	// }
	//
	// // Tie the view to the current user.
	// final RRUser u = this.visibleUserList.get(position);
	// v.setTag(u.session);

	// refreshElements(v, u);
	// return v;
	// }

	@Override
	public boolean hasStableIds() {
		return true;
	}

	public final boolean hasUser(final RRUser user) {
		return visibleUserNames.containsValue(user.session);
	}

	@Override
	public void notifyDataSetChanged() {
		repopulateUsers();
		super.notifyDataSetChanged();
	}

	public final void refreshUser(final RRUser user, boolean initialConnect) {
		final boolean oldVisible = visibleUserNames.get(user.session) != null;
		final boolean newVisible = user.getChannel().id == visibleChannel;

		users.put(user.session, user);

		int oldLocation = -1;
		if (oldVisible) {
			for (int i = 0; i < visibleUserList.size(); i++) {
				if (visibleUserList.get(i).session == user.session) {
					oldLocation = i;
					break;
				}
			}
		}

		int newLocation = 0;
		if (newVisible) {
			newLocation = Collections.binarySearch(visibleUserList, user,
					userComparator);
		}

		int newInsertion = (newLocation < 0) ? (-newLocation - 1) : newLocation;

		if (oldVisible && newVisible) {
			// If the new would be inserted next to the old one, replace the old
			// as it should be removed anyway.
			if (oldLocation == newInsertion || oldLocation == newInsertion + 1) {
				setVisibleUser(oldLocation, user);

				// Since we just replaced a user we can update view without
				// full refresh.
				// Ivan refreshUserAtPosition(oldLocation, user);
			} else {
				removeVisibleUser(oldLocation);

				// If the old one was removed before the new one, move the
				// new index to the left
				if (oldLocation < newInsertion) {
					newInsertion--;
				}

				addVisibleUser(newInsertion, user);

				// Perform full refresh as order changed.
				super.notifyDataSetChanged();
			}
		} else if (oldVisible) {
			removeVisibleUser(oldLocation);
			// TtsProvider.speak(user.userName + " left channel", false);
			super.notifyDataSetChanged();
		} else if (newVisible) {
			addVisibleUser(newInsertion, user);
			if (!initialConnect) /*
								 * TtsProvider.speak(user.userName +
								 * " joined channel", false);
								 */
				super.notifyDataSetChanged();
		}

		if ((oldVisible || newVisible) && visibleUsersChangedCallback != null) {
			visibleUsersChangedCallback.run();
		}
	}

	public void removeUser(final int id) {
		final RRUser user = users.remove(id);

		if (user.getChannel().id == visibleChannel) {
			final int userLocation = Collections.binarySearch(visibleUserList,
					user, userComparator);

			removeVisibleUser(userLocation);
			super.notifyDataSetChanged();

			if (visibleUsersChangedCallback != null) {
				visibleUsersChangedCallback.run();
			}
		}
	}

	public void setUsers(final List<RRUser> users) {
		this.users.clear();
		for (final RRUser user : users) {
			this.users.put(user.session, user);
		}
		repopulateUsers();
	}

	public void setVisibleChannel(final int channelId) {
		visibleChannel = channelId;
		repopulateUsers();
	}

	private void addVisibleUser(final int position, final RRUser user) {
		visibleUserList.add(position, user);
		visibleUserNames.put(user.session, user.userName);
	}

	private void addVisibleUser(final RRUser user) {
		visibleUserList.add(user);
		visibleUserNames.put(user.session, user.userName);
	}

	// Ivan Commented
	// private final void refreshElements(final View view, final RRUser user) {
	// // If this view has been used for another user already, don't update
	// // it with the information from this user.
	// if ((Integer) view.getTag() != user.session) {
	// return;
	// }
	//
	// final TextView name = (TextView) view.findViewById(R.id.userRowName);
	// final ImageView state = (ImageView) view.findViewById(R.id.userRowState);
	//
	// name.setText(user.userName);
	//
	// switch (user.userState) {
	// case RRUser.USERSTATE_DEAFENED:
	// state.setImageResource(R.drawable.deafened);
	// break;
	// case RRUser.USERSTATE_MUTED:
	// state.setImageResource(R.drawable.muted);
	// break;
	// default:
	// if (user.talkingState == AudioOutputHost.STATE_TALKING) {
	// state.setImageResource(R.drawable.talking_on);
	// } else {
	// state.setImageResource(R.drawable.talking_off);
	// }
	// }
	//
	// view.invalidate();
	// }
	//
	// private final void refreshUserAtPosition(final int position, final RRUser
	// user) {
	// final int firstPosition = stupidList.getFirstVisiblePosition();
	// if (firstPosition <= position) {
	// // getChildAt gets the Nth visible item inside the stupidList.
	// // This is documented in Google I/O. :P
	// // http://www.youtube.com/watch?v=wDBM6wVEO70#t=52m30s
	// final View v = stupidList.getChildAt(position - firstPosition);
	//
	// if (v != null) {
	// refreshElements(v, user);
	// }
	// }
	// }

	private void removeVisibleUser(final int position) {
		final RRUser u = visibleUserList.remove(position);
		visibleUserNames.remove(u.session);
	}

	private void repopulateUsers() {
		visibleUserList.clear();
		visibleUserNames.clear();
		for (final RRUser user : users.values()) {
			if (user.getChannel().id == visibleChannel) {
				addVisibleUser(user);
			}
		}

		Collections.sort(visibleUserList, userComparator);

		if (visibleUsersChangedCallback != null) {
			visibleUsersChangedCallback.run();
		}

		super.notifyDataSetChanged();
	}

	private void setVisibleUser(final int position, final RRUser user) {
		visibleUserList.set(position, user);
		visibleUserNames.put(user.session, user.userName);
	}

	// Ivan added instead "View getView" up in the class
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		return null;
	}
}
