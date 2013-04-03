package com.radiorunt.activities.fragments;

import java.util.List;

import com.radiorunt.R;
import com.radiorunt.activities.DbContentProvider;
import com.radiorunt.activities.HomeActivity;
import com.radiorunt.businessobjects.QuickAction;
import com.radiorunt.businessobjects.RRUser;
import com.radiorunt.facebook.FacebookUserDataLoader;
import com.radiorunt.facebook.FriendsGetProfilePics;
import com.radiorunt.facebook.Utility;
import com.radiorunt.utilities.ImageHelper;
import com.radiorunt.utilities.RalleeApp;

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
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class RightSidePanelFragment extends Fragment implements
		LoaderCallbacks<Cursor>, OnClickListener {

	public static final String FRAGMENT_TAG = "rightSidePanelFragment";

	public static final int MODE_VOICEMAIL = 0;
	public static final int MODE_ALERTS = 1;
	public static final int MODE_HISTORY = 2;

	public static final String MODE = "mode";

	private static final int LOADER_VOICEMAIL = 0;
	private static final int LOADER_ALERTS = 1;
	private static final int LOADER_HISTORY = 2;

	private int mode;
	private ImageButton btnVoicemail;
	private ImageButton btnAlerts;
	private ImageButton btnHistory;
	private ListView lvList;

	SimpleCursorAdapter mListAdapterVoicemail;
	SimpleCursorAdapter mListAdapterAlerts;
	SimpleCursorAdapter mListAdapterHistory;

	private FriendsGetProfilePics voicemailPictureModel;
	private FriendsGetProfilePics alertsPictureModel;
	private FriendsGetProfilePics historyPictureModel;

	public boolean isFakeClick = false;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		mode = this.getArguments().getInt(MODE);

		View fragmentView = inflater.inflate(R.layout.right_side_panel,
				container, false);

		// fragmentView.setOnClickListener(this);
		return fragmentView;
	}

	@Override
	public void onStart() {
		super.onStart();

		initControls();
		initControlListeners();

		getLoaderManager().initLoader(LOADER_VOICEMAIL, null, this);
		getLoaderManager().initLoader(LOADER_ALERTS, null, this);
		getLoaderManager().initLoader(LOADER_HISTORY, null, this);
	}

	private void initControls() {

		btnVoicemail = (ImageButton) getView().findViewById(
				R.id.imgbtnRightVoicemail);
		btnVoicemail.setVisibility(View.GONE);

		btnAlerts = (ImageButton) getView()
				.findViewById(R.id.imgbtnRightAlerts);

		btnHistory = (ImageButton) getView().findViewById(
				R.id.imgbtnRightHistory);
		// btnHistory.setVisibility(View.INVISIBLE);
		lvList = (ListView) getView().findViewById(R.id.lvRightPanel);

		switch (mode) {
		case MODE_VOICEMAIL:
			btnVoicemail.setEnabled(false);
			break;
		case MODE_ALERTS:
			btnAlerts.setEnabled(false);
			break;
		case MODE_HISTORY:
			btnHistory.setEnabled(false);
			break;
		}
	}

	void initControlListeners() {

		mListAdapterVoicemail = createListAdapterVoicemail();
		mListAdapterVoicemail.setViewBinder(VoicemailViewBinder());
		mListAdapterAlerts = createListAdapterAlerts();
		mListAdapterAlerts.setViewBinder(AlertsViewBinder());
		mListAdapterHistory = createListAdapterHistory();
		mListAdapterHistory.setViewBinder(HistoryViewBinder());

		switch (mode) {
		case MODE_VOICEMAIL:
			lvList.setAdapter(mListAdapterVoicemail);
			break;
		case MODE_ALERTS:
			lvList.setAdapter(mListAdapterAlerts);
			break;
		case MODE_HISTORY:
			lvList.setAdapter(mListAdapterHistory);
			break;
		}

		btnVoicemail.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				mode = MODE_VOICEMAIL;
				btnVoicemail.setEnabled(false);
				btnAlerts.setEnabled(true);
				btnHistory.setEnabled(true);

				lvList.setAdapter(mListAdapterVoicemail);
			}
		});
		btnAlerts.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				mode = MODE_ALERTS;
				btnVoicemail.setEnabled(true);
				btnAlerts.setEnabled(false);
				btnHistory.setEnabled(true);

				lvList.setAdapter(mListAdapterAlerts);
			}
		});
		btnHistory.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				mode = MODE_HISTORY;
				btnVoicemail.setEnabled(true);
				btnAlerts.setEnabled(true);
				btnHistory.setEnabled(false);

				lvList.setAdapter(mListAdapterHistory);
			}
		});

		lvList.setOnItemClickListener(RightsideListItemClick());
		lvList.setOnItemLongClickListener(RightsideListItemLongClick());

		((ImageView) getView().findViewById(R.id.imgRightSideFakeSide))
				.setOnTouchListener((getHomeActivity()).getFlipDetector());

	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		// // first saving my state, so the bundle wont be empty.
		// // http://code.google.com/p/android/issues/detail?id=19917
		// outState.putString("WORKAROUND_FOR_BUG_19917_KEY",
		// "WORKAROUND_FOR_BUG_19917_VALUE");
		// super.onSaveInstanceState(outState);
	}

	private SimpleCursorAdapter createListAdapterVoicemail() {
		SimpleCursorAdapter adapter = new SimpleCursorAdapter(getActivity(),
				R.layout.right_side_panel_voicemail_list_item, null,
				new String[] { DbContentProvider.USER_COL_NAME,
						DbContentProvider.USER_COL_PIC_URL }, new int[] {
						R.id.txtVoicemailListItemName,
						R.id.imgVoicemailListItem }, 0);
		voicemailPictureModel = new FriendsGetProfilePics();
		voicemailPictureModel.setListener(adapter);
		return adapter;
	}

	private SimpleCursorAdapter createListAdapterAlerts() {
		SimpleCursorAdapter adapter = new SimpleCursorAdapter(getActivity(),
				R.layout.right_side_panel_alerts_list_item, null, new String[] {
						DbContentProvider.MISSED_CALLS_COL_SENDER,
						DbContentProvider.MISSED_CALLS_COL_SENDER,
						DbContentProvider.MISSED_CALLS_COL_TIMESTAMP },
				new int[] { R.id.txtAlertsListItem, R.id.imgAlertsListItem,
						R.id.txtAlertsListItemTime }, 0);

		alertsPictureModel = new FriendsGetProfilePics();
		alertsPictureModel.setListener(adapter);
		return adapter;
	}

	private SimpleCursorAdapter createListAdapterHistory() {
		SimpleCursorAdapter adapter = new SimpleCursorAdapter(getActivity(),
				R.layout.right_side_panel_history_list_item, null,
				new String[] { DbContentProvider.CALL_HISTORY_COL_TYPE,
						DbContentProvider.CALL_HISTORY_COL_TIMESTAMP },
				new int[] { R.id.txtHistoryListItemName,
						R.id.txtHistoryListItemTime }, 0) {
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				View view = super.getView(position, convertView, parent);
				TextView tvName = (TextView) view
						.findViewById(R.id.txtHistoryListItemName);
				int type = (Integer) tvName.getTag();

				TextView tvTime = (TextView) view
						.findViewById(R.id.txtHistoryListItemTime);
				String timestamp = (String) tvTime.getTag();

				ImageView ivPic = (ImageView) view
						.findViewById(R.id.imgHistoryListItem);
				ImageView ivPicEffect = (ImageView) view
						.findViewById(R.id.imgHistoryListItemEffect);
				ivPicEffect.setVisibility(View.INVISIBLE);
				ImageView ivIcon = (ImageView) view
						.findViewById(R.id.imgHistoryListItemSmall);
				ivIcon.setImageDrawable(null);

				Cursor c = null;
				String selection = DbContentProvider.CALL_HISTORY_COL_TIMESTAMP
						+ "=? AND " + DbContentProvider.CALL_HISTORY_COL_TYPE
						+ "=?";

				c = RalleeApp
						.getInstance()
						.getContentResolver()
						.query(DbContentProvider.CONTENT_URI_CALL_HISTORY_TABLE,
								null,
								selection,
								new String[] { String.valueOf(timestamp),
										String.valueOf(type) }, null);
				// Log.i(FRAGMENT_TAG, "c: " + c.getCount());
				if (c != null && c.moveToFirst()) {
					String text = "";
					switch (type) {
					case DbContentProvider.CALL_HISTORY_TYPE_PRIVATE_CALL_OUTGOING:
					case DbContentProvider.CALL_HISTORY_TYPE_PRIVATE_CALL_INCOMING:
						Cursor user = null;
						String userId = c
								.getString(c
										.getColumnIndex(DbContentProvider.CALL_HISTORY_COL_USER_ID));
						Uri userUri = Uri.withAppendedPath(
								DbContentProvider.CONTENT_URI_USER_TABLE,
								userId);
						user = RalleeApp
								.getInstance()
								.getContentResolver()
								.query(userUri,
										new String[] {
												DbContentProvider.USER_COL_NAME,
												DbContentProvider.USER_COL_PIC_URL },
										null, null, null);
						if (user != null && user.moveToFirst()) {
							text += RalleeApp.getInstance().getString(
									R.string.talk_with)
									+ user.getString(user
											.getColumnIndex(DbContentProvider.USER_COL_NAME));
							ivPic.setImageBitmap(historyPictureModel.getImage(
									userId,
									user.getString(user
											.getColumnIndex(DbContentProvider.USER_COL_PIC_URL)),
									RalleeApp.getInstance()));
							ivPicEffect.setVisibility(View.VISIBLE);
							// Small Incoming or outgoing icon
							// if (type ==
							// DbContentProvider.CALL_HISTORY_TYPE_PRIVATE_CALL_INCOMING)
							// {
							// ivIcon.setImageResource(R.color.cyan);
							// } else {
							// ivIcon.setImageResource(R.color.light_blue);
							// }
						} else { // user not in database
							text += RalleeApp.getInstance().getString(
									R.string.talk_with_unkown);
							ivPic.setImageResource(R.drawable.icon);
						}
						if (user != null && !user.isClosed()) {
							user.close();
						}
						break;
					case DbContentProvider.CALL_HISTORY_TYPE_RANDOM_CALL:
						Cursor cursorRandomUser = null;
						String randomId = c
								.getString(c
										.getColumnIndex(DbContentProvider.CALL_HISTORY_COL_USER_ID));

						if (randomId != null) {
							Uri randomUri = Uri.withAppendedPath(
									DbContentProvider.CONTENT_URI_USER_TABLE,
									randomId);
							cursorRandomUser = RalleeApp
									.getInstance()
									.getContentResolver()
									.query(randomUri,
											new String[] {
													DbContentProvider.USER_COL_NAME,
													DbContentProvider.USER_COL_PIC_URL },
											null, null, null);
							if (cursorRandomUser != null
									&& cursorRandomUser.moveToFirst()) {
								text += RalleeApp.getInstance().getString(
										R.string.random_talk_with)
										+ cursorRandomUser
												.getString(cursorRandomUser
														.getColumnIndex(DbContentProvider.USER_COL_NAME));
								ivPic.setImageBitmap(historyPictureModel.getImage(
										randomId,
										cursorRandomUser.getString(cursorRandomUser
												.getColumnIndex(DbContentProvider.USER_COL_PIC_URL)),
										RalleeApp.getInstance()));
								ivPicEffect.setVisibility(View.VISIBLE);
								ivIcon.setImageResource(R.drawable.main_ui_friends_well_random_icon);

							} else { // user not in database
								String fbIdOfRandomUser = Utility.parseSNData(
										randomId).getAsString(
										Utility.SOCIAL_NETWORK_ID);
								RRUser unknownUser = FacebookUserDataLoader
										.getFbUserById(fbIdOfRandomUser);
								text += RalleeApp.getInstance().getString(
										R.string.random_talk_with)
										+ unknownUser.FirstName;
								if (unknownUser.picUrl != null) {
									ivPic.setImageBitmap(ImageHelper.getRoundedCornerBitmap(
											(Utility.getBitmap(
													unknownUser.picUrl,
													RalleeApp.getInstance())),
											5));
									ivPicEffect.setVisibility(View.VISIBLE);
								} else {
									ivPic.setImageResource(R.drawable.main_ui_friends_well_random_icon);
								}
								ivIcon.setImageResource(R.drawable.main_ui_friends_well_random_icon);
							}
						} else {
							text += RalleeApp.getInstance().getString(
									R.string.random_talk);
							ivPic.setImageResource(R.drawable.main_ui_friends_well_random_icon);
						}

						if (cursorRandomUser != null
								&& !cursorRandomUser.isClosed()) {
							cursorRandomUser.close();
						}
						break;
					case DbContentProvider.CALL_HISTORY_TYPE_PRIVATE_GROUP_CALL:
						Cursor cursorPrivateG = null;
						ivPic.setImageResource(R.drawable.main_ui_btn_view_participants_on);
						String prvgId = c
								.getString(c
										.getColumnIndex(DbContentProvider.CALL_HISTORY_COL_PRIVATE_GROUP_ID));
						if (prvgId != null) {
							cursorPrivateG = RalleeApp
									.getInstance()
									.getContentResolver()
									.query(DbContentProvider.CONTENT_URI_PRIVATE_GROUP_TABLE,
											null,
											DbContentProvider.PRIVATE_GROUP_COL_ID
													+ "=?",
											new String[] { prvgId }, null);

							if (cursorPrivateG != null
									&& cursorPrivateG.moveToFirst()) {
								text += RalleeApp.getInstance().getString(
										R.string.entered_private_group)
										+ cursorPrivateG
												.getString(cursorPrivateG
														.getColumnIndex(DbContentProvider.PRIVATE_GROUP_COL_NAME));
							} else {
								text += RalleeApp.getInstance().getString(
										R.string.talk_private_group);
							}
							ivPic.setImageResource(R.drawable.main_ui_btn_view_participants_on);
						} else {
							Cursor userPg = null;
							String userIdpg = c
									.getString(c
											.getColumnIndex(DbContentProvider.CALL_HISTORY_COL_USER_ID));
							if (userIdpg != null) {
								Uri userUripg = Uri
										.withAppendedPath(
												DbContentProvider.CONTENT_URI_USER_TABLE,
												userIdpg);
								userPg = RalleeApp
										.getInstance()
										.getContentResolver()
										.query(userUripg,
												new String[] {
														DbContentProvider.USER_COL_NAME,
														DbContentProvider.USER_COL_PIC_URL },
												null, null, null);
								if (userPg != null && userPg.moveToFirst()) {
									text += RalleeApp.getInstance().getString(
											R.string.group_call_with)
											+ userPg.getString(userPg
													.getColumnIndex(DbContentProvider.USER_COL_NAME));
									ivPic.setImageBitmap(historyPictureModel.getImage(
											userIdpg,
											userPg.getString(userPg
													.getColumnIndex(DbContentProvider.USER_COL_PIC_URL)),
											RalleeApp.getInstance()));
									ivPicEffect.setVisibility(View.VISIBLE);
									ivIcon.setImageResource(R.drawable.main_ui_btn_view_participants_on);
								} else { // user not in database
									text += RalleeApp.getInstance().getString(
											R.string.group_call);
								}
								if (userPg != null && !userPg.isClosed()) {
									userPg.close();
								}
							} else {
								text += RalleeApp.getInstance().getString(
										R.string.group_call);
							}
						}

						if (cursorPrivateG != null
								&& !cursorPrivateG.isClosed()) {
							cursorPrivateG.close();
						}

						break;
					case DbContentProvider.CALL_HISTORY_TYPE_PUBLIC_GROUP_CALL:
						text += RalleeApp.getInstance().getString(
								R.string.entered_public_group);
						ivPic.setImageResource(R.drawable.main_ui_btn_view_participants_off);
						String publicGroupName = c
								.getString(c
										.getColumnIndex(DbContentProvider.CALL_HISTORY_COL_PUBLIC_GROUP_NAME));

						if (publicGroupName != null) {
							text += " "
									+ publicGroupName.substring(5,
											publicGroupName.length());

						}

						break;
					case DbContentProvider.CALL_HISTORY_TYPE_FB_GROUP_CALL:
						text += RalleeApp.getInstance().getString(
								R.string.entered_fb_group);
						ivPic.setImageResource(R.drawable.com_facebook_icon);
						String fbgId = c
								.getString(c
										.getColumnIndex(DbContentProvider.CALL_HISTORY_COL_FB_GROUP_ID));
						if (fbgId != null) {
							Cursor cursorFB = RalleeApp
									.getInstance()
									.getContentResolver()
									.query(DbContentProvider.CONTENT_URI_FB_GROUPS_TABLE,
											new String[] {
													DbContentProvider.FB_GROUP_COL_ID,
													DbContentProvider.FB_GROUP_COL_NAME },
											DbContentProvider.FB_GROUP_COL_ID
													+ "=?",
											new String[] { fbgId }, null);
							if (cursorFB != null && cursorFB.moveToFirst()) {
								String FBname = cursorFB
										.getString(cursorFB
												.getColumnIndex(DbContentProvider.FB_GROUP_COL_NAME));
								text += " " + FBname;
							}

							if (cursorFB != null && !cursorFB.isClosed()) {
								cursorFB.close();
							}
						}

						break;
					}

					tvName.setText(text);
				}

				if (c != null && !c.isClosed()) {
					c.close();
				}

				return view;
			}
		};
		historyPictureModel = new FriendsGetProfilePics();
		historyPictureModel.setListener(adapter);
		return adapter;
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle b) {
		Uri baseUri;
		String select;
		String additionalSelection;
		String order;
		switch (id) {
		case LOADER_VOICEMAIL:

			baseUri = DbContentProvider.CONTENT_URI_USER_TABLE;

			select = " ( " + DbContentProvider.USER_COL_INSTALLED + " = '1'";

			additionalSelection = "";
			if (b != null) {
				additionalSelection = b.getString("additionalSelection");
			}
			if (additionalSelection != null && !additionalSelection.equals("")) {
				select = select + " AND " + additionalSelection + " )";
			} else {
				select += " )";
			}

			order = DbContentProvider.USER_COL_NAME + " COLLATE LOCALIZED ASC";
			return new CursorLoader(RalleeApp.getInstance(), baseUri, null,
					select, null, order);
		case LOADER_ALERTS:

			baseUri = DbContentProvider.CONTENT_URI_MISSED_CALL_TABLE;
			order = DbContentProvider.MISSED_CALLS_COL_TIMESTAMP + " DESC";
			return new CursorLoader(RalleeApp.getInstance(), baseUri, null,
					null, null, order);

		case LOADER_HISTORY:

			baseUri = DbContentProvider.CONTENT_URI_CALL_HISTORY_TABLE;
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
		case LOADER_VOICEMAIL:
			mListAdapterVoicemail.swapCursor(data);
			break;
		case LOADER_ALERTS:
			mListAdapterAlerts.swapCursor(data);
			break;
		case LOADER_HISTORY:
			mListAdapterHistory.swapCursor(data);
			break;
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		int loaderId = loader.getId();
		switch (loaderId) {
		case LOADER_VOICEMAIL:
			mListAdapterVoicemail.swapCursor(null);
			break;
		case LOADER_ALERTS:
			mListAdapterAlerts.swapCursor(null);
			break;
		case LOADER_HISTORY:
			mListAdapterHistory.swapCursor(null);
			break;
		}

	}

	private ViewBinder VoicemailViewBinder() {
		return new ViewBinder() {
			@Override
			public boolean setViewValue(View view, final Cursor cursor,
					final int columnIndex) {
				if (view.getId() == R.id.imgVoicemailListItem) {
					((ImageView) view)
							.setImageBitmap(voicemailPictureModel.getImage(
									cursor.getString(cursor
											.getColumnIndex(DbContentProvider.USER_COL_ID)),
									cursor.getString(columnIndex), RalleeApp
											.getInstance()));
					return true;
				}
				if (view.getId() == R.id.txtVoicemailListItemName) {
					((TextView) view).setText(cursor.getString(columnIndex));
					return true;
				}
				return false;
			};
		};
	}

	private ViewBinder AlertsViewBinder() {
		return new ViewBinder() {
			// private FriendsGetProfilePics alertsPictureModel;

			@Override
			public boolean setViewValue(View view, final Cursor cursor,
					final int columnIndex) {

				if (view.getId() == R.id.txtAlertsListItem) {
					String message = getResources().getString(
							R.string.label_have_missed_call);
					Cursor c = RalleeApp
							.getInstance()
							.getApplicationContext()
							.getContentResolver()
							.query(Uri.parse(DbContentProvider.CONTENT_URI_USER_TABLE
									+ "/" + cursor.getString(columnIndex)),
									null, null, null, null);
					String userFirstName = "unknown";
					if (c != null && c.moveToFirst()) {
						userFirstName = c
								.getString(c
										.getColumnIndex(DbContentProvider.USER_COL_NAME));

					}
					c.close();
					message += " " + userFirstName;
					((TextView) view).setText(message);
					view.setTag(userFirstName);

					return true;
				} else if (view.getId() == R.id.imgAlertsListItem) {
					Cursor c = RalleeApp
							.getInstance()
							.getApplicationContext()
							.getContentResolver()
							.query(Uri.parse(DbContentProvider.CONTENT_URI_USER_TABLE
									+ "/" + cursor.getString(columnIndex)),
									null, null, null, null);
					if (c != null && c.moveToFirst()) {
						String userPicUri = c
								.getString(c
										.getColumnIndex(DbContentProvider.USER_COL_PIC_URL));
						String userId = c.getString(c
								.getColumnIndex(DbContentProvider.USER_COL_ID));

						((ImageView) view)
								.setImageBitmap(alertsPictureModel.getImage(
										userId, userPicUri, RalleeApp
												.getInstance()
												.getApplicationContext()));
					}

					c.close();
					return true;
				} else if (view.getId() == R.id.txtAlertsListItemTime) {
					String text = new java.sql.Timestamp(
							cursor.getLong(columnIndex)).toLocaleString();
					((TextView) view).setText(text);

					return true;
				}
				return false;
			};
		};
	}

	private ViewBinder HistoryViewBinder() {
		return new ViewBinder() {
			@Override
			public boolean setViewValue(View view, final Cursor cursor,
					final int columnIndex) {
				if (view.getId() == R.id.txtHistoryListItemName) {
					((TextView) view).setTag(cursor.getInt(columnIndex));
					return true;
				}
				if (view.getId() == R.id.txtHistoryListItemTime) {
					((TextView) view).setTag(cursor.getString(columnIndex));
					String text = "";
					long duration = cursor
							.getLong(cursor
									.getColumnIndex(DbContentProvider.CALL_HISTORY_COL_DURATION));
					if (duration != 0) {
						duration /= 1000;
						if (duration >= 60) {
							long sec = duration % 60;
							duration /= 60;
							if (duration > 0) {
								text += duration + " min ";
							}
							text += sec + " s, ";
						} else {
							text += duration + " s, ";
						}
					}
					text += new java.sql.Timestamp(cursor.getLong(columnIndex))
							.toLocaleString();
					((TextView) view).setText(text);
					return true;
				}
				return false;
			};
		};
	}

	private OnItemLongClickListener RightsideListItemLongClick() {
		return new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				if (isFakeClick) {
					isFakeClick = false;
					return true;
				}
				switch (mode) {
				case MODE_HISTORY:
					Cursor c = ((Cursor) parent.getItemAtPosition(position));
					if (c != null) {
						String type = c
								.getString(c
										.getColumnIndex(DbContentProvider.CALL_HISTORY_COL_TYPE));
						String timestamp = c
								.getString(c
										.getColumnIndex(DbContentProvider.CALL_HISTORY_COL_TIMESTAMP));
						RalleeApp
								.getInstance()
								.getContentResolver()
								.delete(DbContentProvider.CONTENT_URI_CALL_HISTORY_TABLE,
										DbContentProvider.CALL_HISTORY_COL_TYPE
												+ "=? AND "
												+ DbContentProvider.CALL_HISTORY_COL_TIMESTAMP
												+ "=?",
										new String[] { type, timestamp });
						return true;
						// RalleeApp.getInstance().getApplicationContext()
						// .getContentResolver()
						// .delete(uri_missedCall, null, null);
					}
					break;
				case MODE_ALERTS:
					c = ((Cursor) parent.getItemAtPosition(position));
					if (c != null) {
						String timestamp = c
								.getString(c
										.getColumnIndex(DbContentProvider.MISSED_CALLS_COL_TIMESTAMP));
						RalleeApp
								.getInstance()
								.getContentResolver()
								.delete(Uri.withAppendedPath(
										DbContentProvider.CONTENT_URI_MISSED_CALL_TABLE,
										timestamp), null, null);

						return true;
					}
					break;
				}
				return false;
			}

		};
	}

	private OnItemClickListener RightsideListItemClick() {
		return new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if (isFakeClick) {
					isFakeClick = false;
					return;
				}

				switch (mode) {
				case MODE_VOICEMAIL:

					break;
				case MODE_ALERTS:
					Cursor c = ((Cursor) parent.getItemAtPosition(position));
					if (c != null) {
						String userId = c
								.getString(c
										.getColumnIndex(DbContentProvider.MISSED_CALLS_COL_SENDER));
						Long timestamp = (c
								.getLong(c
										.getColumnIndex(DbContentProvider.MISSED_CALLS_COL_TIMESTAMP)));

						String user = (String) ((TextView) view
								.findViewById(R.id.txtAlertsListItem)).getTag();

						if (userId != null
								|| ((user != null) && (user.equals("unknown")))) {
							if (getHomeActivity() != null) {
								getHomeActivity().clickActionUser(
										QuickAction.TYPE_MISSED_CALL, userId,
										user, timestamp, view);
							}
						}
					}

					break;
				case MODE_HISTORY:
					Cursor cHistoiry = ((Cursor) parent
							.getItemAtPosition(position));
					if (cHistoiry != null) {
						int type = cHistoiry
								.getInt(cHistoiry
										.getColumnIndex(DbContentProvider.CALL_HISTORY_COL_TYPE));
						long timestamp = cHistoiry
								.getLong(cHistoiry
										.getColumnIndex(DbContentProvider.CALL_HISTORY_COL_TIMESTAMP));
						switch (type) {
						case DbContentProvider.CALL_HISTORY_TYPE_PRIVATE_CALL_INCOMING:
						case DbContentProvider.CALL_HISTORY_TYPE_PRIVATE_CALL_OUTGOING:
							String userId = cHistoiry
									.getString(cHistoiry
											.getColumnIndex(DbContentProvider.CALL_HISTORY_COL_USER_ID));
							String user = "unknown";
							Cursor cursorUser = RalleeApp
									.getInstance()
									.getContentResolver()
									.query(Uri.withAppendedPath(
											DbContentProvider.CONTENT_URI_USER_TABLE,
											userId), null, null, null, null);
							if (cursorUser != null && cursorUser.moveToFirst()) {
								user = cursorUser
										.getString(cursorUser
												.getColumnIndex(DbContentProvider.USER_COL_NAME));
							} else {
								return;
							}
							if (getHomeActivity() != null) {
								getHomeActivity().clickActionUser(type, userId,
										user, timestamp, view);
							}
							break;
						case DbContentProvider.CALL_HISTORY_TYPE_PRIVATE_GROUP_CALL:
							String privateGID = cHistoiry
									.getString(cHistoiry
											.getColumnIndex(DbContentProvider.CALL_HISTORY_COL_PRIVATE_GROUP_ID));
							if (privateGID != null) {
								String groupName = "";
								Cursor cursorPrivateG = RalleeApp
										.getInstance()
										.getContentResolver()
										.query(DbContentProvider.CONTENT_URI_PRIVATE_GROUP_TABLE,
												null,
												DbContentProvider.PRIVATE_GROUP_COL_ID
														+ "=?",
												new String[] { privateGID },
												null);
								if (cursorPrivateG != null
										&& cursorPrivateG.moveToFirst()) {
									groupName = cursorPrivateG
											.getString(cursorPrivateG
													.getColumnIndex(DbContentProvider.USER_COL_NAME));
									if (getHomeActivity() != null) {
										getHomeActivity().clickActionUser(type,
												privateGID, groupName,
												timestamp, view);
									}
								} else {
									return;
								}
							} else {
								String userIdPrvG = cHistoiry
										.getString(cHistoiry
												.getColumnIndex(DbContentProvider.CALL_HISTORY_COL_USER_ID));
								if (userIdPrvG != null) {
									String userPrvg = "unknown";
									Cursor cursorUserPrvG = RalleeApp
											.getInstance()
											.getContentResolver()
											.query(Uri.withAppendedPath(
													DbContentProvider.CONTENT_URI_USER_TABLE,
													userIdPrvG), null, null,
													null, null);
									if (cursorUserPrvG != null
											&& cursorUserPrvG.moveToFirst()) {
										userPrvg = cursorUserPrvG
												.getString(cursorUserPrvG
														.getColumnIndex(DbContentProvider.USER_COL_NAME));
									} else {
										return;
									}
									if (getHomeActivity() != null) {
										getHomeActivity()
												.clickActionUser(
														QuickAction.CALL_UNSAVED_PRIVATE_GROUP,
														userIdPrvG, userPrvg,
														timestamp, view);
									}
								}
							}
							break;
						case DbContentProvider.CALL_HISTORY_TYPE_PUBLIC_GROUP_CALL:
							String publicGname = cHistoiry
									.getString(cHistoiry
											.getColumnIndex(DbContentProvider.CALL_HISTORY_COL_PUBLIC_GROUP_NAME));
							if (publicGname != null
									&& getHomeActivity() != null) {
								getHomeActivity().clickActionUser(type, "",
										publicGname, timestamp, view);
							}
							break;
						case DbContentProvider.CALL_HISTORY_TYPE_FB_GROUP_CALL:
							String fbGid = cHistoiry
									.getString(cHistoiry
											.getColumnIndex(DbContentProvider.CALL_HISTORY_COL_FB_GROUP_ID));
							if (fbGid != null && getHomeActivity() != null) {
								getHomeActivity().clickActionUser(type, fbGid,
										"", timestamp, view);
							}
							break;
						case DbContentProvider.CALL_HISTORY_TYPE_RANDOM_CALL:
							if (getHomeActivity() != null) {
								getHomeActivity().clickActionUser(type, "", "",
										timestamp, view);
							}
							break;
						}
					}
					break;
				}
			}
		};

	}

	private HomeActivity getHomeActivity() {
		return (HomeActivity) getActivity();
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub

	}
}
