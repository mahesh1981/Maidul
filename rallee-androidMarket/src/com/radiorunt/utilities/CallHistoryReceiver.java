package com.radiorunt.utilities;

import java.util.Calendar;
import java.util.TimeZone;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.Tracker;
import com.radiorunt.activities.DbContentProvider;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;

public class CallHistoryReceiver extends BroadcastReceiver {
	public static final String ACTION_ADD_CALL = "com.radiorunt.CallHistory.ADD_CALL";
	public static final String ACTION_END_CALL = "com.radiorunt.CallHistory.END_CALL";
	public static final String ACTION_CHANGE_TO_GROUP = "com.radiorunt.CallHistory.CHANGE_GROUP";
	public static final String EXTRA_TYPE = "com.radiorunt.CallHistory.EXTRA_TYPE";
	public static final String EXTRA_ID = "com.radiorunt.CallHistory.EXTRA_ID";

	static final String TAG = "CallHistory";

	Context mContext;
	Settings settings;
	Uri currentCallUri = null;
	private long lTimestamp = -1;
	private int iType = -1;

	// private long oTimestamp = -1;

	public CallHistoryReceiver(Context ctx) {
		this.mContext = ctx;
		settings = new Settings(ctx);
		lTimestamp = settings.getCallHistoryTimestamp();
		iType = settings.getCallHistoryTipe();

		IntentFilter filter = new IntentFilter();
		filter.addAction(ACTION_ADD_CALL);
		this.mContext.registerReceiver(this, filter);

		filter = new IntentFilter();
		filter.addAction(ACTION_END_CALL);
		this.mContext.registerReceiver(this, filter);

		filter = new IntentFilter();
		filter.addAction(ACTION_CHANGE_TO_GROUP);
		this.mContext.registerReceiver(this, filter);

	}

	@Override
	public void onReceive(Context context, Intent intent) {

		if (intent.getAction().equals(ACTION_ADD_CALL)) {
			int type = intent.getIntExtra(EXTRA_TYPE,
					DbContentProvider.CALL_HISTORY_TYPE_PRIVATE_CALL_OUTGOING);
			String id = intent.getStringExtra(EXTRA_ID);

			createCallHistoryEntry(type, id);
		}
		if (intent.getAction().equals(ACTION_CHANGE_TO_GROUP)) {
			changeCallHistoryEntry();
		}
		if (intent.getAction().equals(ACTION_END_CALL)) {
			finishCallHistoryEntry();
		}
	}

	private void createCallHistoryEntry(int type, String id) {
		// if (lTimestamp != -1) {
		// finishCallHistoryEntry();
		// }
		ContentValues values = new ContentValues();
		lTimestamp = Calendar.getInstance(TimeZone.getTimeZone("GMT"))
				.getTimeInMillis();
		settings.setCallHistoryTimestamp(lTimestamp);
		iType = type;
		settings.setCallHistoryTipe(iType);
		values.put(DbContentProvider.CALL_HISTORY_COL_TIMESTAMP, lTimestamp);
		values.put(DbContentProvider.CALL_HISTORY_COL_TYPE, type);

		String col = "";
		switch (type) {
		case DbContentProvider.CALL_HISTORY_TYPE_PRIVATE_CALL_OUTGOING:
		case DbContentProvider.CALL_HISTORY_TYPE_PRIVATE_CALL_INCOMING:
		case DbContentProvider.CALL_HISTORY_TYPE_RANDOM_CALL:
			col = DbContentProvider.CALL_HISTORY_COL_USER_ID;
			break;
		case DbContentProvider.CALL_HISTORY_TYPE_PRIVATE_GROUP_CALL:
			col = DbContentProvider.CALL_HISTORY_COL_PRIVATE_GROUP_ID;
			break;
		case DbContentProvider.CALL_HISTORY_TYPE_PUBLIC_GROUP_CALL:
			col = DbContentProvider.CALL_HISTORY_COL_PUBLIC_GROUP_NAME;
			break;
		case DbContentProvider.CALL_HISTORY_TYPE_FB_GROUP_CALL:
			col = DbContentProvider.CALL_HISTORY_COL_FB_GROUP_ID;
			break;
		}
		values.put(col, id);

		mContext.getApplicationContext()
				.getContentResolver()
				.insert(DbContentProvider.CONTENT_URI_CALL_HISTORY_TABLE,
						values);
	}

	private void changeCallHistoryEntry() {// int groupId) {
		if (lTimestamp == -1) {
			return;
		}

		ContentValues values = new ContentValues();
		values.put(DbContentProvider.CALL_HISTORY_COL_TYPE,
				DbContentProvider.CALL_HISTORY_TYPE_PRIVATE_GROUP_CALL);
		mContext.getApplicationContext()
				.getContentResolver()
				.update(DbContentProvider.CONTENT_URI_CALL_HISTORY_TABLE,
						values,
						DbContentProvider.CALL_HISTORY_COL_TIMESTAMP
								+ "=? AND "
								+ DbContentProvider.CALL_HISTORY_COL_TYPE
								+ "=?",
						new String[] { String.valueOf(lTimestamp),
								String.valueOf(iType) });
		iType = DbContentProvider.CALL_HISTORY_TYPE_PRIVATE_GROUP_CALL;
		settings.setCallHistoryTipe(iType);
	}

	private void finishCallHistoryEntry() {
		if (lTimestamp == -1) {
			iType = -1;
			settings.setCallHistoryTipe(iType);
			settings.setCallHistoryTimestamp(-1);
			return;
		}

		long duration = Calendar.getInstance(TimeZone.getTimeZone("GMT"))
				.getTimeInMillis() - lTimestamp;
		ContentValues values = new ContentValues();
		values.put(DbContentProvider.CALL_HISTORY_COL_DURATION, duration);
		mContext.getApplicationContext()
				.getContentResolver()
				.update(DbContentProvider.CONTENT_URI_CALL_HISTORY_TABLE,
						values,
						DbContentProvider.CALL_HISTORY_COL_TYPE + "=? AND "
								+ DbContentProvider.CALL_HISTORY_COL_TIMESTAMP
								+ "=?",
						new String[] { String.valueOf(iType),
								String.valueOf(lTimestamp) });
		lTimestamp = -1;

		String action = "";
		boolean send = true;
		switch (iType) {
		case DbContentProvider.CALL_HISTORY_TYPE_PRIVATE_CALL_OUTGOING:
			action = "Private call - outgoing";
			break;
		case DbContentProvider.CALL_HISTORY_TYPE_PRIVATE_CALL_INCOMING:
			action = "Private call - incoming";
			send = false;
			break;
		case DbContentProvider.CALL_HISTORY_TYPE_RANDOM_CALL:
			action = "Random call";
			break;
		case DbContentProvider.CALL_HISTORY_TYPE_PRIVATE_GROUP_CALL:
			action = "Group call";
			break;
		case DbContentProvider.CALL_HISTORY_TYPE_PUBLIC_GROUP_CALL:
			action = "Entered public group";
			break;
		case DbContentProvider.CALL_HISTORY_TYPE_FB_GROUP_CALL:
			action = "Entered FB group";
		}

		Tracker tracker = EasyTracker.getTracker();
		tracker.setCustomMetric(1, duration / 1000);
		tracker.trackEvent("Call duration", action, "", (long) 0);
		if (send) {
			tracker.trackTiming("timings", duration, "call", action);
		}

		settings.setCallHistoryTimestamp(lTimestamp);
		iType = -1;
		settings.setCallHistoryTipe(iType);
	}
}
