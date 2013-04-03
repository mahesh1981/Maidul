package com.radiorunt.activities.fragments;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import com.facebook.FacebookException;
import com.facebook.FacebookOperationCanceledException;
import com.facebook.Session;
import com.facebook.widget.WebDialog;
import com.facebook.widget.WebDialog.OnCompleteListener;
import com.google.analytics.tracking.android.EasyTracker;
import com.radiorunt.R;
import com.radiorunt.activities.DbContentProvider;
import com.radiorunt.activities.HomeActivity;
import com.radiorunt.businessobjects.RRUser;
import com.radiorunt.facebook.BaseDialogListener;
import com.radiorunt.facebook.FriendsGetProfilePics;
import com.radiorunt.facebook.Utility;
import com.radiorunt.services.RequestFBData;
import com.radiorunt.utilities.RalleeApp;
//import com.radiorunt.utilities.DbAdapter;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SectionIndexer;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.LinearLayout.LayoutParams;

public class InviteToApplicationDialogFragment extends DialogFragment implements
		LoaderManager.LoaderCallbacks<Cursor> {

	public static final String FRAGMENT_TAG = "inviteToApplicationDialogFragment";

	// final ArrayList<RRUser> nonRRFriends;
	ArrayList<Boolean> checkedStates;
	ListView lvMembers;
	// FriendsAdapter mAdapter;
	// private static Activity mActivity;
	private static String mTitle = "";
	private Handler mHandler;
	int toSendNum = 0;
	private TextView infoLabel;
	private ProgressBar pbLoadingFriends;
	private int friendsNumber = 0;
	// DbAdapter dbAdapter;
	// Context context;
	Map<String, Boolean> selectedFriends;

	public LinkedList<String> mLinked = new LinkedList<String>();
	private SimpleCursorAdapter mSimpleCursorInvFriendsAdapter;
	// private Cursor invFriendsCursor;
	private Button btnCancel;
	private Button btnSendInvites;
	private Dialog mDialog;
	private FriendsGetProfilePics friendsPictureModel;

	// public InviteToApplicationDialog(final Context context, ArrayList<RRUser>
	// users, DbAdapter dbAdapter) {
	public static InviteToApplicationDialogFragment newInstance(String title,
			Activity act) {
		InviteToApplicationDialogFragment frag = new InviteToApplicationDialogFragment();
		Bundle args = new Bundle();
		mTitle = title;
		args.putString("title", title);
		frag.setArguments(args);
		// mActivity = act;

		return frag;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		// mActivity = getActivity();
		// ArrayList<RRUser>
		// users) {

		// this.context = context; // Kimi

		mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				if (msg.what == 1) {
					toSendNum--;
					if (toSendNum <= 0) {

					}
				}
			}
		};

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		// View dialogView = super.onCreateView(inflater, container,
		// savedInstanceState);

		View dialogView = inflater.inflate(R.layout.invite_friends_dialog,
				container, false);
		mDialog.getWindow().setLayout(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT);

		btnCancel = (Button) dialogView
				.findViewById(R.id.btnCancelInviteFriendsDialog);
		btnSendInvites = (Button) dialogView
				.findViewById(R.id.btnInviteInviteToAppDialog);
		lvMembers = (ListView) dialogView
				.findViewById(R.id.lvFriendsInviteToAppDialog);
		infoLabel = (TextView) dialogView.findViewById(R.id.tvNoFBFriends);
		infoLabel.setVisibility(View.GONE);
		pbLoadingFriends = (ProgressBar) dialogView
				.findViewById(R.id.pbLoadingFriends);

		return dialogView;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		mDialog = super.onCreateDialog(savedInstanceState);
		mDialog.setTitle(mTitle);
		return mDialog;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		btnCancel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dismiss();
			}
		});

		btnSendInvites.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				if (getActivity() == null) {
					return;
				}
				Set<String> toSendUserIds = selectedFriends.keySet();

				toSendNum = toSendUserIds.size();

				if (toSendNum > 0) {

					String ids = "";
					for (String userFBId : toSendUserIds) {

						if (((Boolean) selectedFriends.get(userFBId)) == true) {
							ids += userFBId + ",";
						}
					}

					Bundle parameters = new Bundle();
					parameters.putString("to", ids);
					parameters.putString("message", RalleeApp.getInstance()
							.getString(R.string.message_fb_invite));
					parameters.putString("title", "Go Rallee");

					WebDialog requestsDialog = (new WebDialog.RequestsDialogBuilder(
							getActivity(), Session.getActiveSession(),
							parameters)).setOnCompleteListener(
							new OnCompleteListener() {

								@Override
								public void onComplete(Bundle values,
										FacebookException error) {
									if (error != null) {
										if (error instanceof FacebookOperationCanceledException) {
											Toast.makeText(
													RalleeApp.getInstance(),
													"Request cancelled",
													Toast.LENGTH_SHORT).show();
										} else {
											Toast.makeText(
													RalleeApp.getInstance(),
													"Network Error",
													Toast.LENGTH_SHORT).show();
										}
									} else {

										final String requestId = values
												.getString("request");
										if (requestId != null) {

											if (toSendNum > 1) {
												Toast.makeText(
														RalleeApp.getInstance(),
														RalleeApp
																.getInstance()
																.getString(
																		R.string.message_invitations_send),
														Toast.LENGTH_SHORT)
														.show();

											} else if (toSendNum == 1) {
												Toast.makeText(
														RalleeApp.getInstance(),
														RalleeApp
																.getInstance()
																.getString(
																		R.string.message_invitation_send),
														Toast.LENGTH_SHORT)
														.show();
											}
											EasyTracker.getTracker()
													.trackEvent("invite",
															"fbFriends", "",
															(long) toSendNum);
										} else {
											Toast.makeText(
													RalleeApp.getInstance(),
													"Request cancelled",
													Toast.LENGTH_SHORT).show();
										}
									}

								}
							}).build();
					requestsDialog.show();
				}
				dismiss();

			}
		});

		// nonRRFriends = new ArrayList<RRUser>();
		checkedStates = new ArrayList<Boolean>();
		selectedFriends = new HashMap<String, Boolean>();
		// pbLoadingFriends.setMax(RequestFBData.numberToLoad);
//		Log.i("inviteDialog", "numberToLoad from RequestFBData: "
//				+ RequestFBData.numberToLoad);
		// mAdapter = new FriendsAdapter(context);
		// lvMembers.setAdapter(mAdapter);
		// this.dbAdapter = dbAdapter;
		// loadPeopleFromDB();
		// invFriendsCursor = loadFBFriendsFromDB();

		getLoaderManager().initLoader(0, null, this);
		createFriendsAdapter();

		lvMembers.setAdapter(mSimpleCursorInvFriendsAdapter);

		if (friendsNumber == 0) {
			infoLabel.setText(R.string.label_no_facebook_friends);
		} else {
			infoLabel.setVisibility(View.GONE);
			// pbLoadingFriends.setProgress(friendsNumber);
		}
		/*
		 * close progressbar change by maidul
		 */
		if (pbLoadingFriends.getProgress() >= pbLoadingFriends.getMax()) {
			pbLoadingFriends.setVisibility(View.GONE);
		}

	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		// // first saving my state, so the bundle wont be empty.
		// // http://code.google.com/p/android/issues/detail?id=19917
		// outState.putString("WORKAROUND_FOR_BUG_19917_KEY",
		// "WORKAROUND_FOR_BUG_19917_VALUE");
		// super.onSaveInstanceState(outState);
	}

	public void createFriendsAdapter() {
		// create the adapter using the cursor pointing to the desired data
		// as well as the layout information

		mSimpleCursorInvFriendsAdapter = new SimpleCursorAdapter(
				mDialog.getContext(), R.layout.invite_friends_list_item, null,
				new String[] { DbContentProvider.USER_COL_PIC_URL,
						DbContentProvider.USER_COL_NAME,
						DbContentProvider.USER_COL_LOCATION,
						DbContentProvider.USER_COL_ID,
						DbContentProvider.USER_COL_INSTALLED }, new int[] {
						R.id.profile_pic, R.id.name, R.id.info,
						R.id.cbMemberSelected, R.id.small_pic }, 0);

		if (friendsPictureModel == null) {
			friendsPictureModel = new FriendsGetProfilePics();
		}
		friendsPictureModel.setListener(mSimpleCursorInvFriendsAdapter);

		mSimpleCursorInvFriendsAdapter
				.setViewBinder(new SimpleCursorAdapter.ViewBinder() {

					@Override
					public boolean setViewValue(View view, final Cursor cursor,
							final int columnIndex) {
						// if (view.getId() == R.id.rl_friends_list_item) {
						// RelativeLayout rl = (RelativeLayout) view;
						// RRUser user = new RRUser();
						// ViewHolder holder = new ViewHolder();
						// user.FirstName = cursor.getString(
						// cursor.getColumnIndex(DbContentProvider.USER_COL_NAME));
						// user.picUrl = cursor.getString(
						// cursor.getColumnIndex(DbContentProvider.USER_COL_PIC_URL));
						// user.location = cursor.getString(
						// cursor.getColumnIndex(DbContentProvider.USER_COL_LOCATION));
						// user.FirstName = cursor.getString(
						// cursor.getColumnIndex(DbContentProvider.USER_COL_NAME));
						// holder.user = user;
						// holder.selected = selectedFriends.get(user.userName);
						// view.setTag(holder);
						// }
						if (view.getId() == R.id.profile_pic) {
							((ImageView) view).setImageBitmap(friendsPictureModel.getImage(
									cursor.getString(cursor
											.getColumnIndex(DbContentProvider.USER_COL_ID)),
									cursor.getString(columnIndex), RalleeApp
											.getInstance()));
							return true;
						}
						if (view.getId() == R.id.name) {
							((TextView) view).setText(cursor
									.getString(columnIndex));
							return true;
						}
						if (view.getId() == R.id.info) {
							((TextView) view).setText(cursor
									.getString(columnIndex));
							return true;
						}
						if (view.getId() == R.id.cbMemberSelected) {
							short installed = cursor.getShort(cursor
									.getColumnIndex(DbContentProvider.USER_COL_INSTALLED));
							if (installed == 0) { // 0 - false, 1 - true
								String userName = Utility.parseSNData(
										cursor.getString(columnIndex))
										.getAsString(Utility.SOCIAL_NETWORK_ID);
								;
								Boolean isChecked = (Boolean) selectedFriends
										.get(userName);
								if (isChecked == null) {
									isChecked = false;
								}
								((CheckBox) view).setVisibility(View.VISIBLE);
								((CheckBox) view).setChecked(isChecked);
								((CheckBox) view).setTag(userName);
								((CheckBox) view)
										.setOnClickListener(new View.OnClickListener() {

											@Override
											public void onClick(View v) {
												// TODO Auto-generated method
												// stub
												CheckBox cv = (CheckBox) v;
												selectedFriends.put(
														(String) v.getTag(),
														cv.isChecked());
											}
										});
							} else {
								((CheckBox) view).setVisibility(View.INVISIBLE);
							}

							return true;
						}
						if (view.getId() == R.id.small_pic) {
							short installed = cursor.getShort(columnIndex);
							if (installed == 0) { // 0 - false, 1 - true
								((ImageView) view)
										.setImageResource(R.drawable.com_facebook_icon);
							} else {
								((ImageView) view)
										.setImageResource(R.drawable.icon);
							}
							return true;
						}

						return false;
						// String firstName = u.FirstName;
						// // holder.letter.setText(firstName.substring(0, 1));
						// holder.cb.setOnCheckedChangeListener(new
						// OnCheckedChangeListener() {
						// @Override
						// public void onCheckedChanged(final CompoundButton
						// buttonView,
						// boolean isChecked) {
						// if(isChecked){
						// checkedStates.set(position, true);
						// }else{
						// checkedStates.set(position, false);
						// }
						// }
						// });
						// holder.cb.setChecked(selected.booleanValue());
						//
						// // if (view.getId() == R.id.endless_list_row_layout)
						// {
						// // RelativeLayout rl = (RelativeLayout) view;
						// // HDUser user = new HDUser();
						// // user.Name = cursor.getString(cursor
						// // .getColumnIndex(FriendsList.NAME));
						// // user.Phone = cursor.getString(cursor
						// // .getColumnIndex(FriendsList.PHONE));
						// // user.ProfileImageLocation =
						// cursor.getString(cursor
						// //
						// .getColumnIndex(FriendsList.PROFILE_IMAGE_LOCATION));
						// // user.LocalId = cursor.getInt(cursor
						// // .getColumnIndex(FriendsList._ID));
						// //
						// // int columnIndexConversationLocation = cursor
						// //
						// .getColumnIndex(FriendsList.CONVERSATION_IMAGE_LOCATION);
						// // if (columnIndexConversationLocation != -1) {
						// // user.conversationBackground = cursor
						// // .getString(columnIndexConversationLocation);
						// // }
						// //
						// // int columnIndexConversationBckColor = cursor
						// //
						// .getColumnIndex(FriendsList.CONVERSATION_BCK_COLOR);
						// // if (columnIndexConversationBckColor != -1) {
						// // user.conversationBackgroundColor = cursor
						// // .getInt(columnIndexConversationBckColor);
						// // }
						// //
						// // view.setTag(user);
						// // rl.setOnClickListener(new OnClickListener() {
						// // @Override
						// // public void onClick(View v) {
						// // try {
						// //
						// // HDUser u = (HDUser) v.getTag();
						// // Intent myIntent = new Intent(
						// // FriendListActivity.this,
						// // ChatConversationActivity.class);
						// // myIntent.putExtra("jid", u.Phone);
						// // if (u.conversationBackground != null) {
						// // myIntent.putExtra("conversationBackground",
						// // u.conversationBackground);
						// // }
						// // if (u.conversationBackgroundColor !=
						// Color.TRANSPARENT) {
						// // myIntent.putExtra(
						// // "conversationBackgroundColor",
						// // u.conversationBackgroundColor);
						// // }
						// // String uriString = "";
						// // if (u.ProfileImageLocation != null
						// // && !u.ProfileImageLocation.equals("")) {
						// // uriString = u.ProfileImageLocation;
						// // }
						// // myIntent.putExtra("imageLocationUri", uriString);
						// // myIntent.putExtra("name", u.Name);
						// // myIntent.putExtra("LocalId", u.LocalId);
						// // startActivity(myIntent);
						// // } catch (Exception e) {
						// // // TODO Auto-generated catch block
						// // e.printStackTrace();
						// // }
						// // }
						// // });
						// //
						// // return true;
						// // }
						// // if (view.getId() == R.id.avatarIcon) {
						// // ImageView iv = (ImageView) view;
						// // Bitmap bm = null;
						// //
						// // try {
						// // bm = loadContactPhoto(getContentResolver(),
						// // cursor.getString(columnIndex));
						// // } catch (Exception e) {
						// // // TODO Auto-generated catch block
						// // e.printStackTrace();
						// // }
						// // if (bm != null)
						// // iv.setImageBitmap(bm);
						// // else
						// // iv.setImageResource(R.drawable.picturemissing);
						// //
						// // return true;
						// // }
						// // if (view.getId() == R.id.btnFollow) {
						// // ImageButton btn = (ImageButton) view;
						// // btn.setVisibility(View.GONE);
						// // return true;
						// // }
						// // if (view.getId() == R.id.tvNewMessagesCount) {
						// // TextView tv = (TextView) view;
						// // String username = cursor.getString(cursor
						// // .getColumnIndex(FriendsList.PHONE))
						// // + "@jabber.liqmsg.com";
						// // if (unreadMessagesMap.containsKey(username)) {
						// // int count = unreadMessagesMap.get(username);
						// // Log.d("milan", username + " - " + count);
						// // tv.setText(Integer.toString(count));
						// // } else {
						// // tv.setText("");
						// // }
						// // return true;
						// // }
						// // return false;
						// }
						// });
						// // sets filter query provider for filtering friends
						// mSimpleCursorFriendsAdapter
						// .setFilterQueryProvider(new FilterQueryProvider() {
						// public Cursor runQuery(CharSequence constraint) {
						// return getFriends(constraint);
						// }
						// });
						// filterTerm.setText("");
						// filterTerm.addTextChangedListener(filterTextWatcher);
					}
				});
	}

	class ViewHolder {
		public RRUser user;
		public boolean selected;
	}

	// class FriendsSimpleCursorAdapter extends
	// android.support.v4.widget.SimpleCursorAdapter implements
	// SectionIndexer// ArrayAdapter<String>
	// // implements
	// // SectionIndexer
	// {
	//
	// HashMap<String, Integer> alphaIndexer;
	// String[] sections;
	//
	// public FriendsSimpleCursorAdapter(Context context, int layout,
	// Cursor cursor, String[] columns, int[] views) {
	// super(context, layout, cursor, columns, views);
	//
	// alphaIndexer = new HashMap<String, Integer>();
	//
	// if (cursor != null) {
	// int size = cursor.getCount();
	// while (cursor.moveToNext()) {
	// alphaIndexer
	// .put(cursor
	// .getString(
	// cursor.getColumnIndex(DbContentProvider.USER_COL_NAME))
	// .substring(0, 1).toUpperCase(), cursor
	// .getPosition());
	// }
	// }
	// // for (int x = 0; x < size; x++) {
	// // String s = items.get(x);
	// //
	// // // get the first letter of the store
	// // String ch = s.substring(0, 1);
	// // // convert to uppercase otherwise lowercase a -z will be sorted
	// // // after upper A-Z
	// // ch = ch.toUpperCase();
	// //
	// // // HashMap will prevent duplicates
	// // alphaIndexer.put(ch, x);
	// // }
	//
	// Set<String> sectionLetters = alphaIndexer.keySet();
	//
	// // create a list from the set to sort
	// ArrayList<String> sectionList = new ArrayList<String>(
	// sectionLetters);
	//
	// Collections.sort(sectionList);
	//
	// sections = new String[sectionList.size()];
	//
	// sectionList.toArray(sections);
	// }
	//
	// public int getPositionForSection(int section) {
	// return alphaIndexer.get(sections[section]);
	// }
	//
	// public int getSectionForPosition(int position) {
	// return 0;
	// }
	//
	// public Object[] getSections() {
	// return sections;
	// }
	// }

	// public void setFriendsToLoad(int num) {
	// Log.i("inviteDialog", "ITAPPDialog:setFriendsToLoad");
	// // pbLoadingFriends.setMax(num);
	// Log.i("inviteDialog", "friends to load: " + num);
	// }

	// public void freindSaved(int num) {
	// Log.i("inviteDialog", "ITAPPDialog:freindSaved");
	// // pbLoadingFriends.incrementProgressBy(num);
	//
	// getLoaderManager().restartLoader(0, null,
	// InviteToApplicationDialogFragment.this);
	// // mHandler.post(new Runnable() {
	// //
	// // @Override
	// // public void run() {
	// //// invFriendsCursor = loadFBFriendsFromDB();
	// //// mSimpleCursorInvFriendsAdapter.changeCursor(invFriendsCursor);
	// //
	// //// mSimpleCursorInvFriendsAdapter.notifyDataSetChanged();
	// //// if (friendsNumber == 0) {
	// //// infoLabel.setText(R.string.label_no_facebook_friends);
	// //// } else {
	// //// infoLabel.setVisibility(View.GONE);
	// //// pbLoadingFriends.setProgress(friendsNumber);
	// //// }
	// //// if (pbLoadingFriends.getProgress() >= pbLoadingFriends.getMax()) {
	// //// pbLoadingFriends.setVisibility(View.GONE);
	// //// }
	// //// Log.i("inviteDialog", "friends to loaded: " + friendsNumber);
	// //// Log.i("inviteDialog", "friends to load: " +
	// pbLoadingFriends.getMax());
	// // }
	// // });
	// }

	// public void loadingIsFinished() {
	// getLoaderManager().restartLoader(0, null,
	// InviteToApplicationDialogFragment.this);
	// pbLoadingFriends.setVisibility(View.GONE);
	// // Log.i("inviteDialog", "ITAPPDialog:loadingIsFinished");
	// // pbLoadingFriends.setProgress(pbLoadingFriends.getMax());
	// // mHandler.post(new Runnable() {
	// //
	// // @Override
	// // public void run() {
	// // invFriendsCursor = loadFBFriendsFromDB();
	// // mSimpleCursorInvFriendsAdapter.changeCursor(invFriendsCursor);
	// //
	// // mSimpleCursorInvFriendsAdapter.notifyDataSetChanged();
	// // if (invFriendsCursor == null
	// // || invFriendsCursor.getCount() == 0) {
	// // infoLabel.setText(R.string.label_no_facebook_friends);
	// // } else {
	// // infoLabel.setVisibility(View.GONE);
	// // pbLoadingFriends.setProgress(invFriendsCursor.getCount());
	// // }
	// // if (pbLoadingFriends.getProgress() >= pbLoadingFriends.getMax()) {
	// // pbLoadingFriends.setVisibility(View.GONE);
	// // }
	// // }
	// // });
	// }

	@Override
	public void dismiss() {
		// try {
		// invFriendsCursor.close();
		// } catch (Exception e) {
		// e.printStackTrace();
		// }
		super.dismiss();
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
//		Log.i(FRAGMENT_TAG, "onCreateLoader");
		String SELECT = null;
		String WHERE = null;// " ( "+DbContentProvider.USER_COL_INSTALLED +
							// " = '0' )" ;
		String ORDERBY = (DbContentProvider.USER_COL_NAME + " ASC , "
				+ DbContentProvider.USER_COL_INSTALLED + " DESC");
		return new CursorLoader(RalleeApp.getInstance(),
				DbContentProvider.CONTENT_URI_USER_TABLE, null, WHERE, null,
				ORDERBY);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
//		Log.i(FRAGMENT_TAG, "onLoadFinished");
		// Swap the new cursor in. (The framework will take care of closing the
		// old cursor once we return.)
		// mSimpleCursorInvFriendsAdapter.getCursor().close();
		mSimpleCursorInvFriendsAdapter.swapCursor(data);
		friendsNumber = data.getCount();
		if (friendsNumber == 0) {
			infoLabel.setText(R.string.label_no_facebook_friends);
			infoLabel.setVisibility(View.VISIBLE);
		} else {
			infoLabel.setVisibility(View.GONE);
			// pbLoadingFriends.setProgress(friendsNumber);
		}

		/*
		 * close progressbar by maidul
		 */
		pbLoadingFriends.setVisibility(View.GONE);
//		Log.i(FRAGMENT_TAG, "onLoadFinished progressbar ");
		if (pbLoadingFriends.getProgress() >= pbLoadingFriends.getMax()) {
			pbLoadingFriends.setVisibility(View.GONE);
//			Log.i(FRAGMENT_TAG, "onLoadFinished progressbar ");

		}
		// Log.i("inviteDialog", "friends to loaded: " + friendsNumber);
		// Log.i("inviteDialog", "friends to load: " +
		// pbLoadingFriends.getMax());
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
//		Log.i(FRAGMENT_TAG, "onLoaderReset");
		// TODO Auto-generated method stub
		mSimpleCursorInvFriendsAdapter.swapCursor(null);
	}

	private HomeActivity getHomeActivity() {
		return (HomeActivity) this.getActivity();

	}

}
