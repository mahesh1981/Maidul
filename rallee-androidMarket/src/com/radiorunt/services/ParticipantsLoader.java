package com.radiorunt.services;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.List;

import com.radiorunt.activities.DbContentProvider;
import com.radiorunt.activities.HomeActivity;
import com.radiorunt.businessobjects.RRUser;
import com.radiorunt.facebook.FacebookUserDataLoader;
import com.radiorunt.facebook.Utility;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.AsyncTaskLoader;

public class ParticipantsLoader extends
		AsyncTaskLoader<List<HashMap<String, String>>> {

	List<HashMap<String, String>> mData;
	int mChannelId = -1;
	List<RRUser> mParticipants;

	public ParticipantsLoader(Context context, int channelId,
			List<RRUser> participants) {
		super(context);
		mChannelId = channelId;
		mParticipants = participants;
		// TODO Auto-generated constructor stub
	}

	@Override
	synchronized public List<HashMap<String, String>> loadInBackground() {

		List<HashMap<String, String>> participantsMap = new ArrayList<HashMap<String, String>>();
		try {
			for (RRUser participant : mParticipants) {
				if (participant.getChannel().id == mChannelId) {
					String fbIdOfUser = Utility.parseSNData(
							participant.userName).getAsString(
							Utility.SOCIAL_NETWORK_ID);
					RRUser usr = FacebookUserDataLoader
							.getFbUserById(fbIdOfUser);
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
		} catch (ConcurrentModificationException cme) {
			participantsMap = new ArrayList<HashMap<String, String>>();
		}
		return participantsMap;

	}

	UsersReceiver mUsersReceiver;

	/**
	 * Called when there is new data to deliver to the client. The super class
	 * will take care of delivering it; the implementation here just adds a
	 * little more logic.
	 */
	@Override
	public void deliverResult(List<HashMap<String, String>> data) {
		if (isReset()) {
			// An async query came in while the loader is stopped. We
			// don't need the result.
			if (data != null) {
				onReleaseResources(data);
			}
		}
		List<HashMap<String, String>> oldData = data;
		mData = data;

		if (isStarted()) {
			// If the Loader is currently started, we can immediately
			// deliver its results.
			super.deliverResult(data);
		}

		// At this point we can release the resources associated with
		// 'oldData' if needed; now that the new result is delivered we
		// know that it is no longer in use.
		if (oldData != null) {
			onReleaseResources(oldData);
		}
	}

	/**
	 * Handles a request to start the Loader.
	 */
	@Override
	protected void onStartLoading() {
		if (mData != null) {
			// If we currently have a result available, deliver it
			// immediately.
			deliverResult(mData);
		}

		// Start watching for changes in the app data.
		if (mUsersReceiver == null) {
			mUsersReceiver = new UsersReceiver(this);
		}

		// Has something interesting in the configuration changed since we
		// last built the app list?
		// boolean configChange = mLastConfig.applyNewConfig(getContext()
		// .getResources());

		if (takeContentChanged() || mData == null) {// || configChange) {
			// If the data has changed since the last time it was loaded
			// or is not currently available, start a load.
			forceLoad();
		}
	}

	/**
	 * Handles a request to stop the Loader.
	 */
	@Override
	protected void onStopLoading() {
		// Attempt to cancel the current load task if possible.
		cancelLoad();
	}

	/**
	 * Handles a request to cancel a load.
	 */
	@Override
	public void onCanceled(List<HashMap<String, String>> data) {
		super.onCanceled(data);

		// At this point we can release the resources associated with 'data'
		// if needed.
		onReleaseResources(data);
	}

	/**
	 * Handles a request to completely reset the Loader.
	 */
	@Override
	protected void onReset() {
		super.onReset();

		// Ensure the loader is stopped
		onStopLoading();

		// At this point we can release the resources associated with 'data'
		// if needed.
		if (mData != null) {
			onReleaseResources(mData);
			mData = null;
		}

		// Stop monitoring for changes.
		if (mUsersReceiver != null) {
			getContext().unregisterReceiver(mUsersReceiver);
			mUsersReceiver = null;
		}
	}

	/**
	 * Helper function to take care of releasing resources associated with an
	 * actively loaded data set.
	 */
	protected void onReleaseResources(List<HashMap<String, String>> data) {
		// For a simple List<> there is nothing to do. For something
		// like a Cursor, we would close it here.
	}

	public static class UsersReceiver extends BroadcastReceiver {
		final ParticipantsLoader mLoader;

		public UsersReceiver(ParticipantsLoader loader) {
			mLoader = loader;
			IntentFilter filter = new IntentFilter();
			filter.addAction("com.radiorunt.users.CHANGED");
			mLoader.getContext().registerReceiver(this, filter);
		}

		@Override
		public void onReceive(Context context, Intent intent) {
			// Tell the loader about the change.
			mLoader.onContentChanged();
		}
	}

}
