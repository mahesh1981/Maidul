package com.radiorunt.activities.fragments;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
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
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.radiorunt.R;
import com.radiorunt.activities.DbContentProvider;
import com.radiorunt.activities.HomeActivity;
import com.radiorunt.businessobjects.RRUser;
import com.radiorunt.facebook.FacebookUserDataLoader;
import com.radiorunt.facebook.FriendsGetProfilePics;
import com.radiorunt.facebook.Utility;
import com.radiorunt.services.RadioRuntService;
import com.radiorunt.utilities.RalleeApp;

public class TextModePanelFragment extends Fragment implements
		LoaderCallbacks<Cursor> {

	public static final String FRAGMENT_TAG = "textModePanelFragment";

	private static final int LOADER_TRANSCRIPT = 0;

	// Views

	private EditText etMessage;
	private ListView lvConversation;
	private Button btnSend;
	SimpleCursorAdapter mListAdapterMessages;

	private FriendsGetProfilePics messagePictureModel;
	private Map<String, RRUser> participantsMap = new HashMap<String, RRUser>();

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.text_mode_panel, container, false);

		int displayWidth = ((WindowManager) this.getActivity()
				.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay()
				.getWidth();
		int displayHeight = ((WindowManager) this.getActivity()
				.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay()
				.getHeight();

		v.setLayoutParams(new LayoutParams(displayWidth, displayHeight * 4 / 5));
		return v;
	}

	@Override
	public void onStart() {
		super.onStart();
		initControls();
		setControlsListeners();
		getLoaderManager().initLoader(LOADER_TRANSCRIPT, null, this);
	}

	@Override
	public void onResume() {
		super.onResume();

		if (getHomeActivity() != null) {
			getHomeActivity().setScreenAnalytics("MainScreen-TextMode");
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
	}

	private void initControls() {

		lvConversation = (ListView) getView().findViewById(R.id.lvConversation);
		btnSend = (Button) getView().findViewById(R.id.btnSend);
		etMessage = (EditText) getView().findViewById(R.id.etMessage);
	}

	void setControlsListeners() {

		mListAdapterMessages = createListAdapterMessages();
		btnSend.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				sendMessage();
			}
		});

		lvConversation.setStackFromBottom(true);
		lvConversation.setAdapter(mListAdapterMessages);
	}

	private SimpleCursorAdapter createListAdapterMessages() {
		SimpleCursorAdapter adapter = new SimpleCursorAdapter(getActivity(),
				R.layout.message, null, new String[] {
						DbContentProvider.TRANSCRIPT_COL_FROM,
						DbContentProvider.TRANSCRIPT_COL_FROM,
						DbContentProvider.TRANSCRIPT_COL_TIMESTAMP,
						DbContentProvider.TRANSCRIPT_COL_FROM,
						DbContentProvider.TRANSCRIPT_COL_FROM,
						DbContentProvider.TRANSCRIPT_COL_TIMESTAMP,
						DbContentProvider.TRANSCRIPT_COL_MESSAGE }, new int[] {
						R.id.imgUserPicLeft, R.id.tvUserSaidLabelLeft,
						R.id.tvTimeAgoLeft, R.id.imgUserPicRight,
						R.id.tvUserSaidLabelRight, R.id.tvTimeAgoRight,
						R.id.tvMessage }, 0) {
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				View v = super.getView(position, convertView, parent);

				View llMsgStatusLeft = v.findViewById(R.id.llMsgStatusLeft);
				View llMsgStatusRight = v.findViewById(R.id.llMsgStatusRight);

				View imgUserPicLeft = v.findViewById(R.id.imgUserPicLeft);
				View imgUserPicRight = v.findViewById(R.id.imgUserPicRight);

				Cursor c = getCursor();

				String userFrom = "";
				userFrom = c.getString(c
						.getColumnIndex(DbContentProvider.TRANSCRIPT_COL_FROM));

				RRUser user = participantsMap.get(userFrom);

				if ((position % 2) == 0) {
					llMsgStatusLeft.setVisibility(View.VISIBLE);
					imgUserPicLeft.setVisibility(View.VISIBLE);
					if (user != null) {
						if (user.FirstName != null) {
							((TextView) llMsgStatusLeft
									.findViewById(R.id.tvUserSaidLabelLeft))
									.setText(user.FirstName + " said:");
						}
						if (user.picUrl != null) {
							((ImageView) imgUserPicLeft)
									.setImageBitmap(messagePictureModel
											.getImage(userFrom, user.picUrl,
													RalleeApp.getInstance()));
						}
					}

					llMsgStatusRight.setVisibility(View.GONE);
					imgUserPicRight.setVisibility(View.GONE);
				} else {
					llMsgStatusLeft.setVisibility(View.GONE);
					imgUserPicLeft.setVisibility(View.GONE);

					llMsgStatusRight.setVisibility(View.VISIBLE);
					imgUserPicRight.setVisibility(View.VISIBLE);

					if (user.FirstName != null) {
						((TextView) llMsgStatusRight
								.findViewById(R.id.tvUserSaidLabelRight))
								.setText(user.FirstName + " said:");
					}
					if (user.picUrl != null) {
						((ImageView) imgUserPicRight)
								.setImageBitmap(messagePictureModel.getImage(
										userFrom, user.picUrl,
										RalleeApp.getInstance()));
					}
				}
				return v;
			}
		};
		messagePictureModel = new FriendsGetProfilePics();
		messagePictureModel.setListener(adapter);
		adapter.setViewBinder(new ViewBinder() {

			@Override
			public boolean setViewValue(View view, final Cursor cursor,
					final int columnIndex) {
				if (view.getId() == R.id.imgUserPicLeft
						|| view.getId() == R.id.imgUserPicRight) {

					view.setTag(cursor.getString(columnIndex));
					return true;
				}
				if (view.getId() == R.id.tvUserSaidLabelLeft
						|| view.getId() == R.id.tvUserSaidLabelRight) {

					view.setTag(cursor.getString(columnIndex));
					return true;
				}
				if (view.getId() == R.id.tvTimeAgoLeft
						|| view.getId() == R.id.tvTimeAgoRight) {
					((TextView) view).setText(getTimeAgo(cursor
							.getLong(columnIndex)));
					return true;
				}
				if (view.getId() == R.id.tvMessage) {
					((TextView) view).setText(cursor.getString(columnIndex));
					return true;
				}
				return false;
			}
		});
		return adapter;
	}

	RadioRuntService getRRService() {
		if (getActivity() == null) {
			return null;
		} else {
			return ((HomeActivity) getActivity()).getRRService();
		}

	}

	private void sendMessage() {
		if (etMessage != null) {
			String composedMessage = "";
			composedMessage = etMessage.getText().toString();

			if (!composedMessage.equals("")) {

				ContentValues val = new ContentValues();
				long date = Calendar.getInstance(TimeZone.getTimeZone("GMT"))
						.getTimeInMillis();
				long timestamp = date;

				val.put(DbContentProvider.TRANSCRIPT_COL_FROM, RalleeApp
						.getInstance().getRalleeUID());
				val.put(DbContentProvider.TRANSCRIPT_COL_MESSAGE,
						composedMessage);
				val.put(DbContentProvider.TRANSCRIPT_COL_TIMESTAMP, timestamp);
				val.put(DbContentProvider.TRANSCRIPT_COL_HISTORY_ID_TIMESTAMP,
						1);
				val.put(DbContentProvider.TRANSCRIPT_COL_HISTORY_ID_TYPE, 1);

				Uri uri = RalleeApp
						.getInstance()
						.getApplicationContext()
						.getContentResolver()
						.insert(DbContentProvider.CONTENT_URI_TRANSCRIPT_TABLE,
								val);
				if (uri != null) {
//					Log.i(FRAGMENT_TAG, "inserted: " + uri.toString());
				} else {
//					Log.e(FRAGMENT_TAG, "message was not inserted");
				}
			}

			etMessage.setText("");

		}
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {

		Uri baseUri;
		switch (id) {

		case LOADER_TRANSCRIPT:

			baseUri = DbContentProvider.CONTENT_URI_TRANSCRIPT_TABLE;
			return new CursorLoader(RalleeApp.getInstance(), baseUri, null,
					null, null, null);
		default:
			return null;
		}

	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		int loaderId = loader.getId();
		switch (loaderId) {
		case LOADER_TRANSCRIPT:
			prepareParticipants(data);
			mListAdapterMessages.swapCursor(data);
			break;
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		int loaderId = loader.getId();
		switch (loaderId) {
		case LOADER_TRANSCRIPT:
			break;
		}
	}

	private void prepareParticipants(Cursor data) {
		if (data != null && data.moveToFirst()) {
			do {
				String userName = data.getString(data
						.getColumnIndex(DbContentProvider.TRANSCRIPT_COL_FROM));

				if (userName != null && !userName.equals("")
						&& participantsMap.containsKey(userName)) {
					continue;
				}

				RRUser user = new RRUser();
				user.userName = userName;
				user.FirstName = "";
				user.picUrl = "";

				Cursor userCursor = null;
				try {
					userCursor = RalleeApp
							.getInstance()
							.getContentResolver()
							.query(Uri.parse(DbContentProvider.CONTENT_URI_USER_TABLE
									.toString() + "/" + user.userName), null,
									null, null, null);

					if (userCursor != null && userCursor.moveToFirst()) {
						user.FirstName = userCursor.getString(userCursor
								.getColumnIndex(DbContentProvider.USER_COL_ID));
						user.picUrl = userCursor
								.getString(userCursor
										.getColumnIndex(DbContentProvider.USER_COL_PIC_URL));
					} else {
						RRUser unknownUser = FacebookUserDataLoader
								.getFbUserById(Utility.parseSNData(
										user.userName).getAsString(
										Utility.SOCIAL_NETWORK_ID));
						user.FirstName = unknownUser.FirstName;
						user.picUrl = unknownUser.picUrl;
					}

					participantsMap.put(userName, user);

				} finally {
					if (userCursor != null && !userCursor.isClosed()) {
						userCursor.close();
					}
				}

			} while (data.moveToNext());

			mListAdapterMessages.notifyDataSetChanged();
		}
	}

	private String getTimeAgo(long timestamp) {
		String agoString = "a few moments ago";
		long now = Calendar.getInstance(TimeZone.getTimeZone("GMT"))
				.getTimeInMillis();
		long difference = now - timestamp;

//		Log.i(FRAGMENT_TAG, "now: " + now + ", timestamp: " + timestamp
//				+ ", difference: " + difference);

		if (difference < 0) {
			return agoString;
		}

		long seconds = difference / 1000;
		long minutes = seconds / 60;
		long hours = minutes / 60;
		long days = hours / 24;
		long months = days / 30;
		long years = days / 365;

//		Log.i(FRAGMENT_TAG, "seconds: " + seconds + ", minutes: " + minutes
//				+ ", hours: " + hours + ", days: " + days + ", months: "
//				+ months + ", years: " + years);

		if (years > 0) {
			agoString = String.valueOf(years) + " years ago";
		} else if (months > 0) {
			agoString = String.valueOf(months) + " months ago";
		} else if (days > 0) {
			agoString = String.valueOf(days) + " days ago";
		} else if (hours > 0) {
			agoString = String.valueOf(hours) + " hours ago";
		} else if (minutes > 0) {
			agoString = String.valueOf(minutes) + " minutes ago";
		} else if (seconds > 0) {
			agoString = String.valueOf(seconds) + " seconds ago";
		}

		return agoString;
	}

	private HomeActivity getHomeActivity() {
		return (HomeActivity) this.getActivity();
	}
}
