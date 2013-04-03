package com.radiorunt.activities;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.radiorunt.R;
import com.radiorunt.businessobjects.RRChannels;
import com.radiorunt.businessobjects.RRPushMessagePayload;
import com.radiorunt.businessobjects.RRServer;
import com.radiorunt.businessobjects.RRUser;
import com.radiorunt.businessobjects.RandomUser;
import com.radiorunt.facebook.FacebookUserDataLoader;
import com.radiorunt.facebook.Utility;
import com.radiorunt.utilities.Globals;
import com.radiorunt.utilities.CallState;
import com.radiorunt.utilities.RRServerProxyHelper;
import com.radiorunt.utilities.RalleeApp;
import com.radiorunt.utilities.Settings;

/**
 * @author stivan
 * 
 */

public class MwCommunicationLogic extends Activity {

	Context ctx;
	RRServer serverToConnect = null;
	CharSequence[] permanentChannelsSeq = null;

	GetParticipantsServiceReceiver mGetParticipantsServiceReceiver;
	GetLeastParticipantsServerServiceReceiver mGetLeastParticipantsServerServiceReceiver;
	GetChannelsServiceReceiver mGetChannelsServiceReceiver;
	GetRandomUserServiceReceiver mGetRandomUserServiceReceiver;
	RegisterToAppServiceReceiver mRegisterToAppServiceReceiver;
	UnregisterToAppServiceReceiver mUnregisterToAppServiceReceiver;
	RegisterSetInsertUserServiceReceiver mInsertUserReceiver;
	GetUserServiceReceiver mGetUserReceiver;
	RegisterSetOver18Receiver mSetOver18Receiver;
	RegisterGetOver18Receiver mGetOver18Receiver;
	RegisterSetRandomReceiver mSetRandomReceiver;
	RegisterGetRandomReceiver mGetRandomReceiver;
	RegisterCheckForNewReleaseReceiver mCheckForNewReleaseReceiver;
	RegisterSetUserLocationReceiver mSetUserLocationReciver;
	RegisterReportUserReceiver mRegisterReportUserReceiver;
	GetServerIpAddressServiceReceiver mGetServerIpAddressServiceReceiver;

	public MwCommunicationLogic(Context ctx) {
		this.ctx = ctx;
	}

	// ////////////////////////////////////////////////////////////////////////////////////////////////////
	// ///////////////////Start of registration and unregistration of receivers
	// ///////////////////////////
	// ////////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * Register Participants on the Channel Service receiver
	 */
	public void registerGetParticipantsServiceReceiver() {
		if (mGetParticipantsServiceReceiver != null) {
			return;
		}
		mGetParticipantsServiceReceiver = new GetParticipantsServiceReceiver();
		RRServerProxyHelper.registerGetParticipantsReceiver(ctx,
				mGetParticipantsServiceReceiver);
	}

	/**
	 * Unregister Participants on the Channel Service receiver
	 */
	public void unregisterGetParticipantsServiceReceiver() {
		if (mGetParticipantsServiceReceiver == null) {
			return;
		}
		ctx.unregisterReceiver(mGetParticipantsServiceReceiver);
		mGetParticipantsServiceReceiver = null;
	}

	/**
	 * Register Service receiver of the service which returns the least number
	 * of users on the servers
	 */
	public void registerGetLeastParticipantsServerServiceReceiver() {
		if (mGetLeastParticipantsServerServiceReceiver != null) {
			return;
		}
		mGetLeastParticipantsServerServiceReceiver = new GetLeastParticipantsServerServiceReceiver();
		RRServerProxyHelper.registerGetLeastParticipantsServerReceiver(ctx,
				mGetLeastParticipantsServerServiceReceiver);
		RRServerProxyHelper.startGetLeastParticipantsServer(ctx);
	}

	/**
	 * Unregister Service receiver of the service which returns the least number
	 * of users on the servers
	 */
	public void unregisterGetLeastParticipantsServerServiceReceiver() {
		if (mGetLeastParticipantsServerServiceReceiver == null) {
			return;
		}
		ctx.unregisterReceiver(mGetLeastParticipantsServerServiceReceiver);
		mGetLeastParticipantsServerServiceReceiver = null;
	}

	/**
	 * Register Service receiver of the service which returns all permanent
	 * channels on from the server in the database
	 */
	public void registerGetChannelsServiceReceiver() {
		if (mGetChannelsServiceReceiver != null) {
			return;
		}

		String locale = Locale.getDefault().getLanguage();

		mGetChannelsServiceReceiver = new GetChannelsServiceReceiver();
		RRServerProxyHelper.registerGetChannelsReceiver(ctx,
				mGetChannelsServiceReceiver);
		RRServerProxyHelper.startGetChannelsService(ctx, locale);
	}

	/**
	 * Unregister Service receiver of the service which returns all permanent
	 * channels on from the server in the database
	 */
	public void unregisterGetChannelsServiceReceiver() {
		if (mGetChannelsServiceReceiver == null) {
			return;
		}
		ctx.unregisterReceiver(mGetChannelsServiceReceiver);
		mGetChannelsServiceReceiver = null;
	}

	/**
	 * Register Service receiver of the service which returns one random user
	 * chosen by MW Service
	 */
	public void registerGetRandomUserServiceReceiver() {
		if (mGetRandomUserServiceReceiver != null) {
			return;
		}

		mGetRandomUserServiceReceiver = new GetRandomUserServiceReceiver();
		RRServerProxyHelper.registerGetRandomUserReceiver(ctx,
				mGetRandomUserServiceReceiver);
	}

	/**
	 * Unregister Service receiver of the service which returns one random user
	 * chosen by MW Service
	 */
	public void unregisterGetRandomUserServiceReceiver() {
		if (mGetRandomUserServiceReceiver == null) {
			return;
		}
		ctx.unregisterReceiver(mGetRandomUserServiceReceiver);
		mGetRandomUserServiceReceiver = null;
	}

	/**
	 * 
	 */
	public void registerRegisterToAppServiceReceiver() {
		if (mRegisterToAppServiceReceiver != null) {
			return;
		}
		mRegisterToAppServiceReceiver = new RegisterToAppServiceReceiver();
		RRServerProxyHelper.registerRegisterToAppReceiver(ctx,
				mRegisterToAppServiceReceiver);
	}

	/**
	 * 
	 */
	public void unregisterRegisterToAppServiceReceiver() {
		if (mRegisterToAppServiceReceiver == null) {
			return;
		}
		ctx.unregisterReceiver(mRegisterToAppServiceReceiver);
		mRegisterToAppServiceReceiver = null;

	}

	/**
	 * 
	 */
	public void registerUnregisterToAppServiceReceiver() {
		if (mUnregisterToAppServiceReceiver != null) {
			return;
		}
		mUnregisterToAppServiceReceiver = new UnregisterToAppServiceReceiver();
		RRServerProxyHelper.registerUnregisterToAppReceiver(ctx,
				mUnregisterToAppServiceReceiver);
	}

	/**
	 * 
	 */
	public void unregisterUnregisterToAppServiceReceiver() {
		if (mUnregisterToAppServiceReceiver == null) {
			return;
		}
		ctx.unregisterReceiver(mUnregisterToAppServiceReceiver);
		mUnregisterToAppServiceReceiver = null;

	}

	/**
	 * Register service receiver for inserting user in the db
	 */
	public void registerSetInsertUserServiceReceiver() {
		if (mInsertUserReceiver != null) {
			return;
		}

		mInsertUserReceiver = new RegisterSetInsertUserServiceReceiver();
		RRServerProxyHelper.registerSetInsertUserReceiver(ctx,
				mInsertUserReceiver);
	}

	/**
	 * Unregister Service receiver for inserting user in the db
	 */
	public void unregisterSetInsertUserServiceReceiver() {
		if (mInsertUserReceiver == null) {
			return;
		}
		ctx.unregisterReceiver(mInsertUserReceiver);
		mInsertUserReceiver = null;
	}

	/**
	 * Register service receiver for geting user info from mw
	 */
	public void registerGetUserServiceReceiver() {
		if (mGetUserReceiver != null) {
			return;
		}

		mGetUserReceiver = new GetUserServiceReceiver();
		RRServerProxyHelper.registerGetUserReceiver(ctx, mGetUserReceiver);
	}

	/**
	 * Unregister service receiver for geting user info from mw
	 */
	public void unregisterGetUserServiceReceiver() {
		if (mGetUserReceiver == null) {
			return;
		}
		ctx.unregisterReceiver(mGetUserReceiver);
		mGetUserReceiver = null;
	}

	/**
	 * Register service receiver for inserting Over18 user state in the db
	 */
	public void registerSetOver18ServiceReceiver() {
		if (mSetOver18Receiver != null) {
			return;
		}

		mSetOver18Receiver = new RegisterSetOver18Receiver();
		RRServerProxyHelper.registerSetOver18Receiver(ctx, mSetOver18Receiver);
	}

	/**
	 * UnRegister service receiver for inserting Over18 user state in the db
	 */
	public void unregisterSetOver18ServiceReceiver() {
		if (mSetOver18Receiver == null) {
			return;
		}
		ctx.unregisterReceiver(mSetOver18Receiver);
		mSetOver18Receiver = null;
	}

	/**
	 * Register service receiver for getting Over18 user state from the db
	 */

	public void registerGetOver18ServiceReceiver() {
		if (mGetOver18Receiver != null) {
			return;
		}

		mGetOver18Receiver = new RegisterGetOver18Receiver();
		RRServerProxyHelper.registerGetOver18Receiver(ctx, mGetOver18Receiver);
	}

	/**
	 * UnRegister service receiver for Over18 user state from the db
	 */
	public void unregisterGetOver18ServiceReceiver() {
		if (mGetOver18Receiver == null) {
			return;
		}
		ctx.unregisterReceiver(mGetOver18Receiver);
		mGetOver18Receiver = null;
	}

	/**
	 * Register service receiver for inserting Random user state in the db
	 */
	public void registerSetRandomServiceReceiver() {
		if (mSetRandomReceiver != null) {
			return;
		}

		mSetRandomReceiver = new RegisterSetRandomReceiver();
		RRServerProxyHelper.registerSetRandomsReceiver(ctx, mSetRandomReceiver);
	}

	/**
	 * UnRegister service receiver for inserting Random user state in the db
	 */
	public void unregisterSetRandomServiceReceiver() {
		if (mSetRandomReceiver == null) {
			return;
		}
		ctx.unregisterReceiver(mSetRandomReceiver);
		mSetRandomReceiver = null;
	}

	/**
	 * Register service receiver for getting Random user state from the db
	 */
	public void registerGetRandomServiceReceiver() {
		if (mGetRandomReceiver != null) {
			return;
		}

		mGetRandomReceiver = new RegisterGetRandomReceiver();
		RRServerProxyHelper.registerGetRandomReceiver(ctx, mGetRandomReceiver);
	}

	/**
	 * UnRegister service receiver for Random user state from the db
	 */
	public void unregisterGetRandomServiceReceiver() {
		if (mGetRandomReceiver == null) {
			return;
		}
		ctx.unregisterReceiver(mGetRandomReceiver);
		mGetRandomReceiver = null;
	}

	/**
	 * Register service receiver for getting new Release state from the db
	 */
	public void registerCheckForNewReleaseReceiver() {
		if (mCheckForNewReleaseReceiver != null) {
			return;
		}

		mCheckForNewReleaseReceiver = new RegisterCheckForNewReleaseReceiver();
		RRServerProxyHelper.registerCheckForNewReleaseReceiver(ctx,
				mCheckForNewReleaseReceiver);
	}

	/**
	 * UnRegister service receiver for getting new Release state from the db
	 */
	public void unregisterCheckForNewReleaseReceiver() {
		if (mCheckForNewReleaseReceiver == null) {
			return;
		}
		ctx.unregisterReceiver(mCheckForNewReleaseReceiver);
		mCheckForNewReleaseReceiver = null;
	}

	/**
	 * Register service receiver for inserting user location
	 */

	public void registerSetUserLocationReciver() {
		if (mSetUserLocationReciver != null) {
			return;
		}

		mSetUserLocationReciver = new RegisterSetUserLocationReceiver();
		RRServerProxyHelper.registerSetUserLocationReceiver(ctx,
				mSetUserLocationReciver);
	}

	/**
	 * UnRegister service receiver for Random user state from the db
	 */
	public void unregisterSetUserLocationReciver() {
		if (mSetUserLocationReciver == null) {
			return;
		}
		ctx.unregisterReceiver(mSetUserLocationReciver);
		mSetUserLocationReciver = null;
	}

	/**
	 * Register service receiver for getting server ip address
	 */

	public void registerGetServerIpAddressServiceReceiver() {
		if (mGetServerIpAddressServiceReceiver != null) {
			return;
		}

		mGetServerIpAddressServiceReceiver = new GetServerIpAddressServiceReceiver();
		RRServerProxyHelper.registerGetServerIpAddressReceiver(ctx,
				mGetServerIpAddressServiceReceiver);
		RRServerProxyHelper.startGetServerIpAddressService(ctx);
	}

	/**
	 * UnRegister service receiver for getting server ip address
	 */
	public void unregisterGetServerIpAddressServiceReceiver() {
		if (mGetServerIpAddressServiceReceiver == null) {
			return;
		}
		ctx.unregisterReceiver(mGetServerIpAddressServiceReceiver);
		mGetServerIpAddressServiceReceiver = null;
	}

	/**
	 * Register report service receiver for reporting user
	 */

	public void registerReportUserReciver() {
		if (mRegisterReportUserReceiver != null) {
			return;
		}

		mRegisterReportUserReceiver = new RegisterReportUserReceiver();
		RRServerProxyHelper.registerReportUserReceiver(ctx,
				mRegisterReportUserReceiver);
	}

	/**
	 * UnRegister report service receiver for Reporting user
	 */
	public void unregisterReportUserReciver() {
		if (mRegisterReportUserReceiver == null) {
			return;
		}
		if (mRegisterReportUserReceiver != null) {
			ctx.unregisterReceiver(mRegisterReportUserReceiver);
			mRegisterReportUserReceiver = null;
		}

	}

	// ////////////////////////////////////////////////////////////////////////////////////////////////////
	// /////////////////////// End of registration and unregistration of
	// Receivers ///////////////////////
	// ////////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * Channel Participants receiver. Shows how many people is on channel
	 * 
	 * @author Ivan
	 */
	public class GetParticipantsServiceReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {

			String errMsg = RRServerProxyHelper.getChannelsError(context,
					intent);
			if (errMsg.equals("")) {
				String jsonResponseString = RRServerProxyHelper
						.getParticipantsResponse(context, intent);

				if (jsonResponseString != null) {
//					Log.i("channParticipants", jsonResponseString);
					ObjectMapper mapper = new ObjectMapper();
					ArrayList<String> array = null;
					try {
						array = mapper.readValue(jsonResponseString,
								new TypeReference<ArrayList<String>>() {
								});
					} catch (JsonParseException e) {
						// TODO Auto-generated catch block
//						Log.i("channParticipants", e.toString());
						e.printStackTrace();

					} catch (JsonMappingException e) {
						// TODO Auto-generated catch block
//						Log.i("channParticipants", e.toString());
						e.printStackTrace();

					} catch (IOException e) {
						// TODO Auto-generated catch block
//						Log.i("channParticipants", e.toString());
						e.printStackTrace();

					}
					if (array != null) {
						if (array.size() > 0) {
							showParticipants(array);
						} else {
							Toast.makeText(ctx, "No users on this channel.",
									1000).show();
						}
					} else {

//						Log.i("channParticipants", "array is null");
					}
				} else {
					Toast.makeText(ctx, "No participants on this channel", 1000)
							.show();
				}
			} else {
				Toast.makeText(ctx, errMsg, 3000).show();
			}
		}
	}

	private void showParticipants(ArrayList<String> participantsArrayList) {
		// ParticipantsDialog partDialog = new
		// ParticipantsDialog(HomeActivity.this, participantsArrayList,
		// dbAdapter);
		// ParticipantsDialog partDialog = new ParticipantsDialog(ctx,
		// participantsArrayList);
		// partDialog.show();
	}

	/**
	 * Returns RRServer Object with least number of participants on it
	 * 
	 * @author stivan
	 */
	public class GetLeastParticipantsServerServiceReceiver extends
			BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {

			String errMsg = RRServerProxyHelper
					.getLeastParticipantsServerError(context, intent);
			if (errMsg.equals("")) {
				String jsonResponseString = RRServerProxyHelper
						.getLeastParticipantsServerResponse(context, intent);

				if (jsonResponseString != null) {
					Globals.logDebug(this, "channParticipants "
							+ jsonResponseString);
					ObjectMapper mapper = new ObjectMapper();
					serverToConnect = null;
					try {
						serverToConnect = mapper.readValue(jsonResponseString,
								RRServer.class);
					} catch (JsonParseException e) {
						// TODO Auto-generated catch block
						Globals.logError(this,
								"channParticipants " + e.toString());
						e.printStackTrace();

					} catch (JsonMappingException e) {
						// TODO Auto-generated catch block
						Globals.logError(this,
								"channParticipants " + e.toString());
						e.printStackTrace();

					} catch (IOException e) {
						// TODO Auto-generated catch block
						Globals.logError(this,
								"channParticipants " + e.toString());
						e.printStackTrace();

					}
				} else {
//					Log.i("channParticipants", "array is null");
				}
				if (serverToConnect != null) {
					// Toast.makeText(HomeActivity.this,
					// "Best server is: "+serverToConnect.name +
					// " with ip: "+serverToConnect.ipAddress, 1000).show();
				} else {
					// Toast.makeText(HomeActivity.this,
					// "There is no free server", 1000).show();
				}
			} else {
				// Toast.makeText(HomeActivity.this, "There is no free server",
				// 1000).show();
			}
		}
	}

	/**
	 * @author stivan Return Array list of RRChannels Objects which are
	 *         permanent channels on the server register in the db
	 */
	public class GetChannelsServiceReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {

			String errMsg = RRServerProxyHelper.getChannelsError(context,
					intent);
			if (errMsg.equals("")) {
				String jsonResponseString = RRServerProxyHelper
						.getChannelsResponse(context, intent);

				Globals.logDebug(this, "Channels " + jsonResponseString);

				ObjectMapper mapper = new ObjectMapper();
				RRChannels[] array = null;

				try {
					HomeActivity.permanentChannels = mapper.readValue(
							jsonResponseString,
							new TypeReference<List<RRChannels>>() {
							});
					array = mapper.readValue(jsonResponseString,
							RRChannels[].class);
				} catch (JsonParseException e) {

					e.printStackTrace();
				} catch (JsonMappingException e) {

					e.printStackTrace();
				} catch (IOException e) {

					e.printStackTrace();
				}
				if (HomeActivity.permanentChannels != null) {
					ArrayList<RRChannels> publicGroups = HomeActivity.permanentChannels;

					if (publicGroups.size() > 0) {
						context.getApplicationContext()
								.getContentResolver()
								.delete(DbContentProvider.CONTENT_URI_CHANNEL_TABLE,
										null, null);

						context.getApplicationContext()
								.getContentResolver()
								.delete(DbContentProvider.CONTENT_URI_CHANNEL_CATEGORY_TABLE,
										null, null);
					}

					ContentValues generalCategoryValues = new ContentValues();
					generalCategoryValues.put(
							DbContentProvider.CHANNEL_CATEGORY_COL_NAME,
							"General");
					if (context
							.getApplicationContext()
							.getContentResolver()
							.update(DbContentProvider.CONTENT_URI_CHANNEL_CATEGORY_TABLE,
									generalCategoryValues,
									DbContentProvider.CHANNEL_CATEGORY_COL_ID
											+ "=?", new String[] { "1" }) == 0) {

						context.getApplicationContext()
								.getContentResolver()
								.insert(DbContentProvider.CONTENT_URI_CHANNEL_CATEGORY_TABLE,
										generalCategoryValues);
					}

					for (int i = 0; i < publicGroups.size(); i++) {
						RRChannels chan = publicGroups.get(i);

						ContentValues values = new ContentValues();
						values.put(DbContentProvider.CHANNEL_COL_CHANNEL_ID,
								chan.id);
						values.put(DbContentProvider.CHANNEL_COL_PARENT, 0);
						values.put(DbContentProvider.CHANNEL_COL_DESCRIPTION,
								"");
						values.put(DbContentProvider.CHANNEL_TEMPORARY, false);
						values.put(DbContentProvider.CHANNEL_COL_USER_COUNT,
								chan.userCount);
						values.put(DbContentProvider.CHANNEL_COL_SERVER_IP_ADR,
								chan.serverIpAdr);
						values.put(DbContentProvider.CHANNEL_COL_PORT,
								chan.port);

						String categoryName;
						categoryName = chan.category;

						if (categoryName == null) {
							categoryName = "General";
						}

						if (categoryName.equals("")) {
							categoryName = "General";
						}

						Cursor categoryCursor = context
								.getApplicationContext()
								.getContentResolver()
								.query(DbContentProvider.CONTENT_URI_CHANNEL_CATEGORY_TABLE,
										null,
										DbContentProvider.CHANNEL_CATEGORY_COL_NAME
												+ "=?",
										new String[] { categoryName }, null);
						Uri categoryUri = null;
						if (categoryCursor != null) {
							try {

								if (categoryCursor.getCount() > 0) {
									if (categoryCursor.moveToFirst()) {
										categoryUri = Uri
												.parse(DbContentProvider.CONTENT_URI_CHANNEL_CATEGORY_TABLE
														.toString()
														+ "/"
														+ categoryCursor
																.getLong(categoryCursor
																		.getColumnIndex(DbContentProvider.CHANNEL_CATEGORY_COL_ID)));
									} else {
										categoryUri = null;
									}
								} else {
									ContentValues categoryValues = new ContentValues();
									categoryValues
											.put(DbContentProvider.CHANNEL_CATEGORY_COL_NAME,
													categoryName);
									categoryUri = context
											.getApplicationContext()
											.getContentResolver()
											.insert(DbContentProvider.CONTENT_URI_CHANNEL_CATEGORY_TABLE,
													categoryValues);
								}
							} catch (Exception e) {
								// TODO: handle exception
							} finally {
								if (categoryCursor != null
										&& !categoryCursor.isClosed()) {
									categoryCursor.close();
								}
							}
						}

						if (categoryUri != null) {
							if (categoryUri.getLastPathSegment() != null) {
								Long categoryId = Long.parseLong(categoryUri
										.getLastPathSegment());
								values.put(
										DbContentProvider.CHANNEL_COL_CATEGORY,
										categoryId);
							}
						}

						Uri uri = Uri
								.parse(DbContentProvider.CONTENT_URI_CHANNEL_TABLE
										.toString() + "/" + chan.name);

						if (context.getApplicationContext()
								.getContentResolver()
								.update(uri, values, null, null) == 0) {
							values.put(DbContentProvider.CHANNEL_COL_NAME,
									chan.name);
							uri = context
									.getApplicationContext()
									.getContentResolver()
									.insert(DbContentProvider.CONTENT_URI_CHANNEL_TABLE,
											values);
						}
					}

					// FriendsDockFragment frag = (FriendsDockFragment)
					// ((HomeActivity) context)
					// .getSupportFragmentManager().findFragmentByTag(
					// FriendsDockFragment.FRAGMENT_TAG);
					// if (frag != null) {
					// frag.updateChannelsState();
					// }
					permanentChannelsSeq = new CharSequence[HomeActivity.permanentChannels
							.size()];
					for (int i = 0; i < HomeActivity.permanentChannels.size(); i++) {
						String name = new String(
								HomeActivity.permanentChannels.get(i).name);
						permanentChannelsSeq[i] = name.subSequence(5,
								name.length());
					}
				}

			} else {

				// Toast.makeText(ctx, errMsg, 3000).show();
			}
		}
	}

	/**
	 * @author nebojsa Return ralleeId of random user chosen by MWService
	 */
	public class GetRandomUserServiceReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {

			String errMsg = RRServerProxyHelper.getRandomUserError(context,
					intent);
			if (errMsg.equals("")) {
				String jsonResponseString = RRServerProxyHelper
						.getRandomUserResponse(context, intent);
				if (jsonResponseString != null
						&& !jsonResponseString.equals("")
						&& !jsonResponseString.equals("default")) {
					String randomUserRalleeId = null;

					ObjectMapper mapper = new ObjectMapper();
					RandomUser mRandomUser = null;

					try {
						JSONObject jsonMessage = new JSONObject(
								jsonResponseString);
						randomUserRalleeId = jsonMessage.getString("user_id");

						mRandomUser = mapper.readValue(jsonResponseString,
								RandomUser.class);
					} catch (JSONException e) {
						e.printStackTrace();
					} catch (JsonParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (JsonMappingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					if (randomUserRalleeId != null) {
						if (CallState.sCurrent == CallState.NORMAL) {
							((HomeActivity) ctx).showRandomUser(mRandomUser);
						} else {
//							Log.i("random", "randomUser: null");
							((HomeActivity) ctx).cleanDialogs();
						}
					} else {
						if (((HomeActivity) ctx).randomContinuousList == null
								|| !((HomeActivity) ctx).randomContinuousList
										.isEmpty()) {
							((HomeActivity) ctx).goRandom(-3);
							return;
						} else {
							Toast.makeText(ctx, R.string.label_no_random,
									Toast.LENGTH_LONG).show();
							((HomeActivity) ctx).cleanDialogs();
						}
					}
				}

				unregisterGetRandomUserServiceReceiver();
			}
		}
	}

	public class RegisterToAppServiceReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			String errMsg = RRServerProxyHelper.registerToAppError(context,
					intent);
			if (errMsg.equals("")) {
				String response = RRServerProxyHelper.registerToAppResponse(
						context, intent);
			} else {
			}
		}
	}

	public class UnregisterToAppServiceReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			String errMsg = RRServerProxyHelper.unregisterToAppError(context,
					intent);
			if (errMsg.equals("")) {
				String response = RRServerProxyHelper.unregisterToAppResponse(
						context, intent);
			} else {
			}
		}
	}

	/**
	 * @author stivan Observe service which Insert user in db
	 */
	public class RegisterSetInsertUserServiceReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {

			String errMsg = RRServerProxyHelper.setInsertUserError(context,
					intent);
			if (errMsg.equals("")) {
				String jsonResponseString = RRServerProxyHelper
						.setInsertUserResponse(context, intent);

				Settings settings = new Settings(context);
				boolean isFirstStartFBpost = settings.getIsFirstStartFBpost();

				if (isFirstStartFBpost) {
					String title = RalleeApp.getInstance().getFullName();
					if (jsonResponseString.equals("Inserted")) {
						title = title
								+ " "
								+ context
										.getString(R.string.message_fb_new_to_rallee);
						Utility.sendFBpost(
								Utility.fbId,
								"link",
								context.getString(R.string.message_fb_rallee_description),
								context.getString(R.string.app_name),
								Utility.APP_ICON_URL,
								title,
								context.getString(R.string.message_fb_rallee_link));

					}

					isFirstStartFBpost = false;
					settings.setIsFirstStartFBpost(isFirstStartFBpost);
				}

//				Log.i("IVAN", "setInsertUserResponse: " + jsonResponseString);

			} else {

			}

		}
	}

	public class GetUserServiceReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			String errMsg = RRServerProxyHelper.getUserError(context, intent);
			if (errMsg.equals("")) {
				String jsonResponseString = RRServerProxyHelper
						.getUserResponse(context, intent);

//				Log.i("MwCommunication", "Get user: " + jsonResponseString);

			} else {

//				Log.i("MwCommunication", "Get user error: " + errMsg);
			}
		}
	}

	/**
	 * @author stivan Observe service which set Over18 user state in db
	 */
	public class RegisterSetOver18Receiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {

			String errMsg = RRServerProxyHelper.setOver18Error(context, intent);
			if (errMsg.equals("")) {
				String jsonResponseString = RRServerProxyHelper
						.setOver18Response(context, intent);

//				Log.i("IVAN", "Set Over18 user state: " + jsonResponseString);

			} else {

//				Log.i("IVAN", "Set Over18 user state error: " + errMsg);
			}

		}
	}

	/**
	 * @author stivan Observer service which get Over18 user state in db
	 */
	public class RegisterGetOver18Receiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {

			String errMsg = RRServerProxyHelper.getOver18Error(context, intent);
			if (errMsg.equals("")) {
				String jsonResponseString = RRServerProxyHelper
						.getOver18Response(context, intent);

//				Log.i("IVAN", "Get Over18 user state: " + jsonResponseString);

				if (jsonResponseString == null || jsonResponseString.equals("")) {
					jsonResponseString = Settings.RANDOM_PREF_DEFAULT;
				}

				Settings settings = new Settings(context);
				settings.setOver18(jsonResponseString);

//				Log.i("IVAN", "Get Over18 user state:" + jsonResponseString);

			} else {

			}

		}
	}

	/**
	 * @author stivan Observe service which set random user state in db
	 */
	public class RegisterSetRandomReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {

			String errMsg = RRServerProxyHelper.setRandomError(context, intent);
			if (errMsg.equals("")) {
				String jsonResponseString = RRServerProxyHelper
						.setRandomResponse(context, intent);

//				Log.i("IVAN", "Set random user state Response: "
//						+ jsonResponseString);

			} else {
//				Log.i("IVAN", "Set random user state error: " + errMsg);
			}

		}
	}

	/**
	 * @author stivan Observe service which get random user state in db
	 */
	public class RegisterGetRandomReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {

			String errMsg = RRServerProxyHelper.getRandomError(context, intent);
			if (errMsg.equals("")) {
				String jsonResponseString = RRServerProxyHelper
						.getRandomResponse(context, intent);
				if (jsonResponseString == null || jsonResponseString.equals("")) {
					jsonResponseString = Settings.RANDOM_PREF_DEFAULT;
				}

				Settings settings = new Settings(context);
				settings.setGoRandom(jsonResponseString);

//				Log.i("IVAN", " Get random user state Response: "
//						+ jsonResponseString);

			} else {
				// Toast.makeText(ctx, errMsg, 3000).show();
			}

		}
	}

	/**
	 * @author stivan Observe service which get new state of new release
	 */
	public class RegisterCheckForNewReleaseReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {

			String errMsg = RRServerProxyHelper.checkForNewReleaseError(
					context, intent);
			if (errMsg.equals("")) {
				String jsonResponseString = RRServerProxyHelper
						.checkForNewReleaseResponse(context, intent);

//				Log.i("IVAN", " Get new release state Response: "
//						+ jsonResponseString);
				if (jsonResponseString.equals("yes")) {
					Toast.makeText(ctx, ctx.getString(R.string.update), 25000)
							.show();
				}

			} else {
				Toast.makeText(ctx, errMsg, 3000).show();
			}

		}
	}

	public class RegisterSetUserLocationReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {

			String errMsg = RRServerProxyHelper.setUserLocationError(context,
					intent);
			if (errMsg.equals("")) {
				String jsonResponseString = RRServerProxyHelper
						.setUserLocationResponse(context, intent);
//
//				Log.i("MwCommunLogic", "Set user location response: "
//						+ jsonResponseString);

			} else {
//				Log.i("MwCommunLogic", "Set user location error: " + errMsg);
			}

		}
	}

	public class GetServerIpAddressServiceReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {

			String errMsg = RRServerProxyHelper.getServerIpAddressError(
					context, intent);
			if (errMsg.equals("")) {
				Settings settings = new Settings(context);
				String host = RRServerProxyHelper.getServerIpAddressResponse(
						context, intent);
				settings.setConnectionIntentHost(host);
			}
			unregisterGetServerIpAddressServiceReceiver();
		}
	}

	public class RegisterReportUserReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {

			String errMsg = RRServerProxyHelper.setReportUserError(context,
					intent);
			if (errMsg.equals("")) {
				String jsonResponseString = RRServerProxyHelper
						.setReportUserResponse(context, intent);

//				Log.i("MwCommunLogic", "Report user response: "
//						+ jsonResponseString);

			} else {
//				Log.i("MwCommunLogic", "Report user error: " + errMsg);
			}

		}
	}
}
