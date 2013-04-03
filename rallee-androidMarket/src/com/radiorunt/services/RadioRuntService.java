package com.radiorunt.services;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URL;
import java.net.UnknownHostException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;

import net.sf.mumble.MumbleProto.Ping;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import com.radiorunt.utilities.Globals;
import com.radiorunt.utilities.RalleeApp;
import com.radiorunt.R;
import com.radiorunt.utilities.Settings;
import com.radiorunt.activities.DbContentProvider;
import com.radiorunt.activities.HomeActivity;
import com.radiorunt.activities.LogInActivity;
import com.radiorunt.activities.MwCommunicationLogic;
import com.radiorunt.businessobjects.RRChannels;
import com.radiorunt.businessobjects.RRMessages;
import com.radiorunt.businessobjects.RRPushFilteringMessagePayload;
import com.radiorunt.businessobjects.RRPushMessagePayload;
import com.radiorunt.businessobjects.RRUser;
import com.radiorunt.facebook.Utility;
import com.radiorunt.services.audio.AudioOutputHost;
import com.radiorunt.services.audio.RecordThread;

import junit.framework.Assert;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Resources.Theme;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.SystemClock;
import android.os.PowerManager.WakeLock;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * Service for providing the client an access to the connection.
 * 
 * RadioRuntService manages the Rallee connection and provides access to it for
 * binding activities.
 * 
 */
public class RadioRuntService extends Service {
	public class LocalBinder extends Binder {
		public RadioRuntService getService() {
			return RadioRuntService.this;
		}
	}

	WakeLock wakelock;
	PowerManager powerm;

	class ServiceAudioOutputHost extends AbstractHost implements
			AudioOutputHost {
		abstract class ServiceProtocolMessage extends ProtocolMessage {
			@Override
			protected Iterable<IServiceObserver> getObservers() {
				return observers.values();
			}
		}

		@Override
		public void setTalkState(final RRUser user, final int talkState) {
			handler.post(new ServiceProtocolMessage() {
				@Override
				public void process() {
					if (user != null) {
						user.talkingState = talkState;
					}
				}

				@Override
				protected void broadcast(final IServiceObserver observer)
						throws RemoteException {
					if (user != null) {
						observer.onUserTalkStateUpdated(user);
					}
				}
			});
		}
	}

	class ServiceConnectionHost extends AbstractHost implements
			RadioRuntConnectionHost {
		abstract class ServiceProtocolMessage extends ProtocolMessage {
			@Override
			protected Iterable<IServiceObserver> getObservers() {
				return observers.values();
			}
		}

		public void setConnectionState(final int state) {
			handler.post(new ServiceProtocolMessage() {
				@Override
				public void process() {
					if (RadioRuntService.this.state == state) {
						return;
					}

					RadioRuntService.this.state = state;

					// Handle foreground stuff
					if (state == RadioRuntConnectionHost.STATE_CONNECTED) {
						Globals.logDebug(this,
								"RadioRuntConnectionHost.STATE_CONNECTED 1");
						sendListsForFiltering();
						updateConnectionState();
						if (reconnAlarmReceiver != null) {
							stopReconnectionAlarm();
							Globals.logDebug(this, "setStopReconnectionALarm");
						}
					} else if (state == RadioRuntConnectionHost.STATE_DISCONNECTED) {
						Globals.logDebug(this,
								"RadioRuntConnectionHost.STATE_DISCONNECTED 1");
						// if(tempNetState && HomeActivity.CONNECTED == 1){
						// Log.i("conn", "updateNewConn");
						// handleCommandInternal(connectionIntent); SSL
						// }else{
						doConnectionDisconnect();
						// }
					} else if (state == RadioRuntConnectionHost.STATE_RECONNECTING) {
						mProtocol.stopPingAlarm();
						// doConnectionDisconnectInternal();
						updateConnectionState();
						reconnectServer();

					} else {
						updateConnectionState();
					}
				}

				@Override
				protected void broadcast(final IServiceObserver observer) {
				}
			});
		}

		@Override
		public void setError(final String error) {
			handler.post(new Runnable() {
				@Override
				public void run() {
					Globals.logError(this, " 1 " + error);
					errorString = error;

					if (error.equals("Connection closed by peer")) {
						Globals.logDebug(this, "Connection closed by peer");
						// set shared preference and disconnect state of
						// connection
						// RadioRuntService.DISCONNECT_FROM_RALLEE = true;
						// settings.setExitCode(true);
						// disconnect();
						// stopReconnectionAlarm();
						// Intent intent = new Intent(RadioRuntService.this,
						// LogInActivity.class);
						// intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
						// .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						// startActivity(intent);
						// //////////////////////////////////////////////////////////////
					}
				}
			});
		}
	}

	/**
	 * Connection host for RadioRuntConnection.
	 * 
	 * RadioRuntConnection uses this interface to communicate back to
	 * RadioRuntService. Since RadioRuntConnection processes the data packets in
	 * a background thread these methods will be called from that thread.
	 * RadioRuntService should expose itself as a single threaded Service so its
	 * consumers don't need to bother with synchronizing. For this reason these
	 * handlers should take care of the required synchronization.
	 * 
	 * Also it is worth noting that in case a certain handler doesn't need
	 * synchronizing for its own purposes it might need it to maintain the order
	 * of events. Forwarding the CURRENT_USER_UPDATED event shouldn't be done
	 * before the USER_ADDED event has been processed for that user. For this
	 * reason even events like the CURRENT_USER_UPDATED are posted to the
	 * RadioRuntService handler.
	 */
	class ServiceProtocolHost extends AbstractHost implements
			RadioRuntProtocolHost {
		abstract class ServiceProtocolMessage extends ProtocolMessage {
			@Override
			protected Iterable<IServiceObserver> getObservers() {
				return observers.values();
			}
		}

		@Override
		public void channelAdded(final RRChannels channel) {
			handler.post(new ServiceProtocolMessage() {
				@Override
				public void process() {
					channels.add(channel);
				}

				@Override
				protected void broadcast(final IServiceObserver observer)
						throws RemoteException {
					observer.onChannelAdded(channel);
				}
			});
		}

		@Override
		public void channelRemoved(final int channelId) {
			handler.post(new ServiceProtocolMessage() {
				RRChannels channel;

				@Override
				public void process() {
					for (int i = 0; i < channels.size(); i++) {
						if (channels.get(i).id == channelId) {
							channel = channels.remove(i);
							break;
						}
					}
				}

				@Override
				protected void broadcast(final IServiceObserver observer)
						throws RemoteException {
					observer.onChannelRemoved(channel);
				}
			});
		}

		@Override
		public void channelUpdated(final RRChannels channel) {
			handler.post(new ServiceProtocolMessage() {
				@Override
				public void process() {
					for (int i = 0; i < channels.size(); i++) {
						if (channels.get(i).id == channel.id) {
							channels.set(i, channel);
							break;
						}
					}
				}

				@Override
				protected void broadcast(final IServiceObserver observer)
						throws RemoteException {
					observer.onChannelUpdated(channel);
				}
			});
		}

		public void currentChannelChanged() {
			handler.post(new ServiceProtocolMessage() {
				@Override
				public void process() {
				}

				@Override
				protected void broadcast(final IServiceObserver observer)
						throws RemoteException {
					observer.onCurrentChannelChanged();
				}
			});
		}

		@Override
		public void currentUserUpdated() {
			Globals.logDebug(this, "RRSRV CurrentUser updated; ChId: "
					+ getCurrentChannel().id);
			if (getCurrentChannel().id != 0) {
				ON_MY_CHANNEL = getCurrentChannel().id;
			}
			handler.post(new ServiceProtocolMessage() {
				@Override
				public void process() {
					if (!canSpeak() && isRecording()) {
						setRecording(false);
					}
				}

				@Override
				protected void broadcast(final IServiceObserver observer)
						throws RemoteException {
					observer.onCurrentUserUpdated();
				}
			});
		}

		public void messageReceived(final RRMessages msg) {
			Globals.logDebug(this, "SRVC MESSAGE RECEIVED");

			String msgBody = msg.message;
			ObjectMapper mapper = new ObjectMapper();
			RRPushMessagePayload payload = null;
			try {

				Globals.logDebug(this, "RRSRV  processMessage:" + msgBody);
				payload = mapper.readValue(msgBody, RRPushMessagePayload.class);
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
			if (payload == null
					|| (payload != null && (payload.payloadType
							.equals("filter")
							|| payload.payloadType.equals("filteringOn")
							|| payload.payloadType.equals("")
							|| payload.payloadType.equals("transcriptingOff") || payload.payloadType
								.equals("transcriptingOn")))) {
				messagesClearAll();
				return;
			}

			if (payload.payloadType.equals("banned")) {
				Globals.logWarn(this, "User is banned");
				USER_IS_BANNED = true;

				DISCONNECT_FROM_RALLEE = true;
				settings.setExitCode(true);

				stopReconnectionAlarm();
				Intent intent = new Intent(RadioRuntService.this,
						LogInActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP).addFlags(
						Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(intent);

				SWITCH_CHANNEL = 99;

				messagesClearAll();
				disconnect();
				return;
			}

			if (payload.payloadType.equals("kick")) {
				Globals.logWarn(this, "User is kicked");

				DISCONNECT_FROM_RALLEE = true;
				settings.setExitCode(true);

				stopReconnectionAlarm();

				SWITCH_CHANNEL = 99;

				messagesClearAll();
				disconnect();
				return;
			}

			if (msg.message.equals("1001")) {
				final long timestamp = System.currentTimeMillis();
				final byte[] udpBuffer = new byte[9];
				// UDP
				udpBuffer[1] = (byte) ((timestamp >> 56) & 0xFF);
				udpBuffer[2] = (byte) ((timestamp >> 48) & 0xFF);
				udpBuffer[3] = (byte) ((timestamp >> 40) & 0xFF);
				udpBuffer[4] = (byte) ((timestamp >> 32) & 0xFF);
				udpBuffer[5] = (byte) ((timestamp >> 24) & 0xFF);
				udpBuffer[6] = (byte) ((timestamp >> 16) & 0xFF);
				udpBuffer[7] = (byte) ((timestamp >> 8) & 0xFF);
				udpBuffer[8] = (byte) ((timestamp) & 0xFF);

				mClient.sendUdpMessage(udpBuffer, udpBuffer.length, true);
				Globals.logDebug(this,
						"Received 1001 msg - Ping sent to server");
			} else {
				if (HOME_ACTIVITY_PAUSED == 1) {

					// mProtocol.setPauseMumProtocolState(false);
					powerm = (PowerManager) getSystemService(Context.POWER_SERVICE);
					wakelock = powerm.newWakeLock(
							PowerManager.ACQUIRE_CAUSES_WAKEUP
									| PowerManager.FULL_WAKE_LOCK
									| PowerManager.SCREEN_BRIGHT_WAKE_LOCK,
							"bbbb");
					try {
						wakelock.acquire();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					// reconnectServer();
					// Intent intent = new
					// Intent(RadioRuntService.this,LogInActivity.class);
					// intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					// startActivity(intent);
					Intent intent = new Intent(RadioRuntService.this,
							HomeActivity.class);
					intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP).addFlags(
							Intent.FLAG_ACTIVITY_NEW_TASK);
					startActivity(intent);

				}
				handler.post(new ServiceProtocolMessage() {

					@Override
					public void process() {
						messages.add(msg);
					}

					@Override
					protected void broadcast(final IServiceObserver observer)
							throws RemoteException {
						observer.onMessageReceived(msg);
					}
				});
				killWakeLock();
			}
		}

		public void messageSent(final RRMessages msg) {
			Globals.logDebug(this, "SRVC MESSAGE SENT");
			handler.post(new ServiceProtocolMessage() {
				@Override
				public void process() {
					// we don't need to keep messages which we have sent
					// main purpose of keeping these messages was to show txt
					// conversation
					// which we are not using in Rallee client
					// messages.add(msg);
				}

				@Override
				protected void broadcast(final IServiceObserver observer)
						throws RemoteException {
					observer.onMessageSent(msg);
				}
			});
		}

		@Override
		public void setError(final String error) {
			if (!tempNetState) {
				return;
			}
			// handleCommandInternal(connectionIntent);
			handler.post(new ServiceProtocolMessage() {
				@Override
				protected void broadcast(final IServiceObserver observer) {
				}

				@Override
				protected void process() {
					Globals.logError(this, " 2 " + error);
					errorString = error;
				}
			});
		}

		@Override
		public void setSynchronized(final boolean synced) {
			handler.post(new ServiceProtocolMessage() {
				@Override
				public void process() {
					RadioRuntService.this.synced = synced;
					if (synced) {
						Globals.logInfo(this, "Synchronized");
					} else {
						Globals.logInfo(this, "Synchronization reset");
					}
					updateConnectionState();
				}

				@Override
				protected void broadcast(final IServiceObserver observer) {
				}
			});
		}

		@Override
		public void userAdded(final RRUser user) {
			handler.post(new ServiceProtocolMessage() {
				@Override
				public void process() {
					users.remove(user);
					users.add(user);
				}

				@Override
				protected void broadcast(final IServiceObserver observer)
						throws RemoteException {
					observer.onUserAdded(user);
				}
			});
		}

		@Override
		public void userRemoved(final int userId) {
			handler.post(new ServiceProtocolMessage() {
				private RRUser user;

				@Override
				public void process() {
					for (int i = 0; i < users.size(); i++) {
						if (users.get(i).session == userId) {
							try {
								this.user = users.remove(i);
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							return;
						}
					}

					// try {
					// //Assert.fail("Non-existant user was removed");
					// } catch (Exception e) {
					// // TODO Auto-generated catch block
					// e.printStackTrace();
					// }
				}

				@Override
				protected void broadcast(final IServiceObserver observer)
						throws RemoteException {
					observer.onUserRemoved(user);
				}
			});
		}

		@Override
		public void userUpdated(final RRUser user) {
			handler.post(new ServiceProtocolMessage() {
				@Override
				public void process() {
					for (int i = 0; i < users.size(); i++) {
						if (users.get(i).session == user.session) {
							users.set(i, user);

							return;
						}
					}
					try {
						// Assert.fail("Non-existant user was updated");
						Globals.logError(this, "Non-existant user was updated");
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

				@Override
				protected void broadcast(final IServiceObserver observer)
						throws RemoteException {
					observer.onUserUpdated(user);
				}
			});
		}

		@Override
		public void userStateUpdated(final RRUser user) {
			handler.post(new ServiceProtocolMessage() {
				@Override
				public void process() {
					for (int i = 0; i < users.size(); i++) {
						if (users.get(i).session == user.session) {
							users.set(i, user);

							return;
						}
					}
					try {
						// Assert.fail("Non-existant user was updated");
						Globals.logError(this, "Non-existant user was updated");
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

				@Override
				protected void broadcast(final IServiceObserver observer)
						throws RemoteException {
					// observer.onUserStateUpdated(user);
				}
			});
		}
	}

	public static final int CONNECTION_STATE_DISCONNECTED = 0;
	public static final int CONNECTION_STATE_CONNECTING = 1;
	public static final int CONNECTION_STATE_SYNCHRONIZING = 2;
	public static final int CONNECTION_STATE_CONNECTED = 3;
	public static final int CONNECTION_STATE_RECONNECTING = 4;

	/**
	 * Values: 0 - connected and on the Root; 1 - connected and on some channel;
	 * 99 - disconnected from the server
	 */
	public static int SWITCH_CHANNEL = 99;
	public static int ON_MY_CHANNEL = 0;

	private static final String[] CONNECTION_STATE_NAMES = { "Disconnected",
			"Connecting", "Connected", "Reconnecting" };
	private static final String[] SERVICE_STATE_NAMES = { "Disconnected",
			"Connecting", "Synchronizing", "Connected", "Reconnecting" };

	public static final String ACTION_CONNECT = "radiorunt.action.CONNECT";

	public static final String EXTRA_MESSAGE = "radiorunt.extra.MESSAGE";
	public static final String EXTRA_CONNECTION_STATE = "radiorunt.extra.CONNECTION_STATE";
	public static final String EXTRA_HOST = "radiorunt.extra.HOST";
	public static final String EXTRA_PORT = "radiorunt.extra.PORT";
	public static final String EXTRA_USERNAME = "radiorunt.extra.USERNAME";
	public static final String EXTRA_PASSWORD = "radiorunt.extra.PASSWORD";
	public static final String EXTRA_USER = "radiorunt.extra.USER";

	// use to start home activity when it is on pause and we receive call
	public static int HOME_ACTIVITY_PAUSED = 1;
	public static int LOGIN_ACTIVITY_PAUSED = 0;

	/**
	 * True if the server has banned user
	 */
	public static Boolean USER_IS_BANNED = false;

	/**
	 * States: false - connected to server; true - disconnected from server;
	 */
	public static boolean DISCONNECT_FROM_RALLEE = true;

	/**
	 * States: 0 - Account is not for deletion; 1 - Account is ready to
	 * deletion;
	 */
	// public static int DELETE_ACCOUNT = 0;
	/*
	 * delete account by maidul
	 */
	public static int DELETE_ACCOUNT = 0;
	private Settings settings;

	private RadioRuntConnection mClient;
	private MumbleProtocol mProtocol;
	private ReconnectionAlarmReceiver reconnAlarmReceiver;

	private Thread mClientThread;
	private Thread mRecordThread;
	Boolean disconnectInternal = false;
	Notification mNotification;
	boolean mHasConnections;
	Intent changeServerIntent;

	// SharedPreferences shPref;

	private ConnectivityReceiver connectivityReceiver = null;
	private MwCommunicationLogic mMwCommLogic;

	private final LocalBinder mBinder = new LocalBinder();
	final Handler handler = new Handler();

	int state;
	boolean synced;
	int serviceState;
	String errorString;
	final List<RRMessages> messages = new LinkedList<RRMessages>();
	final List<RRChannels> channels = new ArrayList<RRChannels>();
	final List<RRUser> users = new ArrayList<RRUser>();

	// Use concurrent hash map so we can modify the collection while iterating.
	private final Map<Object, IServiceObserver> observers = new ConcurrentHashMap<Object, IServiceObserver>();

	private static final Class<?>[] mStartForegroundSignature = new Class[] {
			int.class, Notification.class };
	private static final Class<?>[] mStopForegroundSignature = new Class[] { boolean.class };

	private Method mStartForeground;
	private Method mStopForeground;

	private final Object[] mStartForegroundArgs = new Object[2];
	private final Object[] mStopForegroundArgs = new Object[1];

	private ServiceProtocolHost mProtocolHost;
	private ServiceConnectionHost mConnectionHost;
	private ServiceAudioOutputHost mAudioHost;
	private String connectedServerString;
	Intent connectionIntent = new Intent();

	public boolean canSpeak() {
		return mProtocol != null && mProtocol.canSpeak;
	}

	public void reconnectServer() {
		// mProtocol.stopThreads();
		// disconnect();
		Globals.logDebug(this, "reconnectServer");
		MumbleProtocol.notOnGlobalServer = false;
		sendOnePing();

		// start reconnecting if the tcp connection is crashed and we have an
		// active internet connection
		// reconnection is started on every 10 sec.
		if (!isConnected()/* state == RadioRuntConnectionHost.STATE_RECONNECTING */) {
			Globals.logDebug(this, "reconnectServer ReconnectingState");
			if (reconnAlarmReceiver == null) {

				reconnAlarmReceiver = new ReconnectionAlarmReceiver();

				Globals.logDebug(this, "start Reconnection Alarm");
				IntentFilter filter = new IntentFilter();
				filter.addAction(ReconnectionAlarmReceiver.ACTION_RECONNECT_START);
				filter.addCategory(Intent.CATEGORY_DEFAULT);
				registerReceiver(reconnAlarmReceiver, filter);

				reconnectionAlarm(this, 10 * 1000);
			}
		}
	}

	// public void switchServerIntent(Intent serverIntent){
	// MumbleProtocol.notOnGlobalServer = true;
	// Log.d("msg", "switchServerIntent");
	// handleCommandInternal(serverIntent);
	// }

	// public final void switchServer(RRChannels channel) {
	// // final Cursor c = dbAdapter.fetchServer(id);
	// final String host =
	// channel.serverIpAdr;//c.getString(c.getColumnIndexOrThrow(DbAdapter.SERVER_COL_HOST));
	// final int port =
	// Integer.valueOf(channel.port);//c.getInt(c.getColumnIndexOrThrow(DbAdapter.SERVER_COL_PORT));
	// final String username =
	// Utility.userUID;//c.getString(c.getColumnIndexOrThrow(DbAdapter.SERVER_COL_USERNAME));
	// final String password =
	// Utility.userUID;//c.getString(c.getColumnIndexOrThrow(DbAdapter.SERVER_COL_PASSWORD));
	// // c.close();
	// changeServerIntent = new Intent(this, RadioRuntService.class);
	// changeServerIntent.setAction(RadioRuntService.ACTION_CONNECT);
	// changeServerIntent.putExtra(RadioRuntService.EXTRA_HOST, host);
	// changeServerIntent.putExtra(RadioRuntService.EXTRA_PORT, port);
	// changeServerIntent.putExtra(RadioRuntService.EXTRA_USERNAME, username);
	// changeServerIntent.putExtra(RadioRuntService.EXTRA_PASSWORD, password);
	// Log.d("msg", "switchServer");
	// switchServerIntent(changeServerIntent);
	// }

	// public void connectInternal(){
	// if(mProtocol != null){
	// mProtocol.stopThreads();
	// }
	// handleCommandInternal(connectionIntent);
	// }
	//
	// public void autoReconnectServer(){
	// if(wifiInfo != null && mobileInfo != null){
	// Log.i("conn", "WIFI or MOBILE not NULL");
	// while(!(wifiInfo.isConnected() || mobileInfo.isConnected())){
	// reconnectServer();
	// try {
	// Thread.sleep(500);
	// } catch (InterruptedException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	// }
	// // if(wifiInfo.isConnected() || mobileInfo.isConnected()){
	// // final Intent i = new Intent(this, HomeActivity.class);
	// // startActivity(i);
	// // }
	// }
	// }

	public void disconnect() {
		// Call disconnect on the connection.
		// It'll notify us with DISCONNECTED when it's done.
		this.setRecording(false);
		if (mClient != null) {
			mClient.disconnect();
			TtsProvider.speak("disconnected", true);
		}
		if (wakelock != null && wakelock.isHeld() == true) {
			try {
				wakelock.release();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				// e.printStackTrace();
			}
		}
	}

	// public void disconnectInternal() {
	// disconnectInternal = true;
	// Log.i("searchingThreads", "disconnect");
	// // Call disconnect on the connection.
	// // It'll notify us with DISCONNECTED when it's done.
	// this.setRecording(false);
	// if (mClient != null) {
	// mClient.disconnectInternal();
	// TtsProvider.speak("disconnected", true);
	// }
	// }

	public List<RRChannels> getChannelList() {
		return Collections.unmodifiableList(channels);
	}

	public int getCodec() {
		if (mProtocol != null) {
			if (mProtocol.codec == MumbleProtocol.CODEC_NOCODEC) {
				// no Codec is set, so we are setting Speex
				mProtocol.codec = MumbleProtocol.CODEC_GAMA;
				// throw new IllegalStateException(
				// "Called getCodec on a connection with unsupported codec");
			}

			return mProtocol.codec;
		} else {
			return MumbleProtocol.CODEC_GAMA;
		}

	}

	public int getConnectionState() {
		return serviceState;
	}

	public RRChannels getCurrentChannel() {
		try {
			return mProtocol.currentChannel;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public RRUser getCurrentUser() {
		if (mProtocol != null) {
			return mProtocol.currentUser;
		} else {
			return null;
		}
	}

	public String getError() {
		final String r = errorString;
		errorString = null;
		return r;
	}

	public List<RRMessages> getMessageList() {
		return Collections.unmodifiableList(messages);
	}

	public void messagesClearAll() {
		try {
			messages.clear();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// maunaly clear users and channels after reconnection
	public void clearUsersAndChannels() {
		users.clear();
		channels.clear();
	}

	public List<RRUser> getUserList() {
		return Collections.unmodifiableList(users);
	}

	public boolean isConnected() {
		return serviceState == CONNECTION_STATE_CONNECTED;
	}

	public boolean isRecording() {
		return (mRecordThread != null);
	}

	public void joinChannel(final int channelId) {
		try {
			ON_MY_CHANNEL = channelId;
			Globals.logDebug(this, "RRSRV joined " + ON_MY_CHANNEL);
			mProtocol.joinChannel(channelId);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void createChannel(String name, boolean temporary, String description) {
		try {
			mProtocol.createChannel(name, temporary, description);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void deafenSelf(Boolean deafenState) {
		mProtocol.deafenSelf(deafenState);
	}

	public void muteSelf(Boolean muteSelf) {
		mProtocol.muteSelf(muteSelf);
	}

	public void stopAudioThreads() {
		if (mProtocol != null) {
			mProtocol.stopAudioThreads();
		}
	}

	public void startAudioThreads() {
		try {
			mProtocol.startAudioThreads();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void setAudioVolume() {
		try {
			mProtocol.setAudioVolume();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public boolean audioOutputState() {
		if (mProtocol != null) {
			return mProtocol.audioOutputState();
		}
		return false;
	}

	public void stopPingAlarm() {
		if (mProtocol != null) {
			mProtocol.stopPingAlarm();
		}
	}

	public void restartPingAlarm(int pingTime) {
		if (mProtocol != null) {
			mProtocol.restartPingAlarm(pingTime);
		}
	}

	public void startPingThread(int pingTime) {
		if (mProtocol != null) {
			mProtocol.startPingThread(pingTime);
		}
	}

	// public void setPauseMumProtocolState(boolean state){
	// if(mProtocol != null){
	// mProtocol.setPauseMumProtocolState(state);
	// }
	// }
	public void sendChannelTextMessage(final String message,
			final RRChannels channel) {
		try {
			mProtocol.sendChannelTextMessage(message, channel);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void sendUserTextMessage(final String message, final int session) {
		try {
			mProtocol.sendUserTextMessage(message, session);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void sendUdpMessage(final byte[] buffer, final int length) {
		try {
			mClient.sendUdpMessage(buffer, length, false);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void setRecording(final boolean state) {
		try {
			if (mProtocol != null && mProtocol.currentUser != null
					&& mRecordThread == null && state) {

				// send message to users to wake up them
				// so they will be able to hear what we are talking
				RRChannels currentChannel = new RRChannels();
				currentChannel = getCurrentChannel();

				if (currentChannel.id == 0) {
					// do nothing, you are on the root channel
				} else {
					// string 1001 wake up the users which are on the channel
					// to send ping to the server
					sendChannelTextMessage("1001", currentChannel);
					Globals.logDebug(this, "Send 1001 msg");
				}

				// start record
				// TODO check initialized
				mRecordThread = new Thread(new RecordThread(this,
						getApplicationContext()), "record");
				mRecordThread.start();
				mAudioHost.setTalkState(mProtocol.currentUser,
						AudioOutputHost.STATE_TALKING);
			} else if (mRecordThread != null && !state) {
				// stop record
				mRecordThread.interrupt();
				mRecordThread = null;
				mAudioHost.setTalkState(mProtocol.currentUser,
						AudioOutputHost.STATE_PASSIVE);
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}

	}

	@Override
	public IBinder onBind(final Intent intent) {
		mHasConnections = true;
		Globals.logInfo(this, "Bound");
		return mBinder;
	}

	@Override
	public boolean onUnbind(final Intent intent) {

		if (disconnectInternal) {
			mHasConnections = true;
			disconnectInternal = false;
		} else /* if (DISCONNECT_FROM_RALLEE) */{
			mHasConnections = false;
		}
		Globals.logInfo(this, "Unbound");
		return false;

	}

	@Override
	public void onCreate() {
		super.onCreate();
		Globals.logDebug(this, "Service START Created");
		android.provider.Settings.System.putInt(getContentResolver(),
				android.provider.Settings.System.WIFI_SLEEP_POLICY,
				android.provider.Settings.System.WIFI_SLEEP_POLICY_NEVER);

		settings = new Settings(this);
		DISCONNECT_FROM_RALLEE = settings.getExitCode(true);

		// shPref = PreferenceManager.getDefaultSharedPreferences(this);
		// registerChangeSharedPrefListener();

		connectivityReceiver = new ConnectivityReceiver();
		registerReceiver(connectivityReceiver, new IntentFilter(
				ConnectivityManager.CONNECTIVITY_ACTION));
		mMwCommLogic = new MwCommunicationLogic(RalleeApp.getInstance());
		// commented by Nebojsa
		// TtsProvider.init(this);

		try {
			mStartForeground = getClass().getMethod("startForeground",
					mStartForegroundSignature);
			mStopForeground = getClass().getMethod("stopForeground",
					mStopForegroundSignature);
		} catch (final NoSuchMethodException e) {
			// Running on an older platform.
			mStartForeground = mStopForeground = null;
		}

		Globals.logDebug(this, "Service END Created");
	}

	@Override
	public void onDestroy() {

		super.onDestroy();

		// unregisterChangeSharedPrefListener();
		// commented by Nebojsa
		// TtsProvider.close();

		// Make sure our notification is gone.
		mMwCommLogic.unregisterGetServerIpAddressServiceReceiver();
		hideNotification();
		stopReconnectionAlarm();
		unregisterReceiver(connectivityReceiver);

		Globals.logInfo(this, "Destroyed");
		if (wakelock != null && wakelock.isHeld() == true) {
			try {
				wakelock.release();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				// e.printStackTrace();
			}
		}
	}

	// OnSharedPreferenceChangeListener shPrefListener = new
	// OnSharedPreferenceChangeListener() {
	//
	// public void onSharedPreferenceChanged(
	// SharedPreferences sharedPreferences, String key) {
	// if (key.equals("notification_visibility")) {
	// showNotification = shPref.getBoolean("notification_visibility",
	// false);
	// if (showNotification) {
	// showNotification();
	// } else {
	// hideNotification();
	// }
	// }
	// }
	// };

	// private void registerChangeSharedPrefListener() {
	// shPref.registerOnSharedPreferenceChangeListener(shPrefListener);
	// }

	// private void unregisterChangeSharedPrefListener() {
	// shPref.unregisterOnSharedPreferenceChangeListener(shPrefListener);
	// }

	@Override
	public void onStart(final Intent intent, final int startId) {
		handleCommand(intent, RadioRuntConnectionHost.STATE_CONNECTING);
		connectionIntent = intent;
	}

	@Override
	public int onStartCommand(final Intent intent, final int flags,
			final int startId) {

		// if((flags & START_FLAG_REDELIVERY)!=0){
		// //if crash than restarat service
		// Globals.logDebug(this, "START_FLAG_REDELIVERY");
		// connectionIntent = settings.getConnectionIntent();
		// serviceState = CONNECTION_STATE_RECONNECTING;
		// updateConnectionState();
		// return handleCommand(connectionIntent,
		// RadioRuntConnectionHost.STATE_RECONNECTING);
		// // get shared preference and exit code for state of connection
		// // if user didn't close the connection DISCONNECT_FROM_RALLEE must be
		// false
		// // If the service has been restarted and created again then it need
		// to create
		// // new connection
		//
		// if(DISCONNECT_FROM_RALLEE == 0){
		// serviceState = CONNECTION_STATE_RECONNECTING;
		// Globals.logDebug(this,
		// "DISCONNECT_FROM_RALLEE == false start reconnection");
		// updateConnectionState();
		// reconnectServer();
		//
		// }else{
		// serviceState = CONNECTION_STATE_DISCONNECTED;
		// }
		// }else
		if (!DISCONNECT_FROM_RALLEE) {
			Globals.logDebug(this,
					"onStartCommand DISCONNECT_FROM_RALLEE is false");
			connectionIntent = settings.getConnectionIntent();
			disconnectifHostXxx();
			return handleCommand(connectionIntent,
					RadioRuntConnectionHost.STATE_CONNECTING);
			// return handleCommandInternal(connectionIntent);

		} else if (DELETE_ACCOUNT != 0) {
			Globals.logDebug(this,
					"onStartCommand DISCONNECT_FROM_RALLEE is false");
			disconnectInternal = true;
			disconnect();

			return handleCommand(connectionIntent,
					RadioRuntConnectionHost.STATE_DISCONNECTED);
			// return handleCommandInternal(connectionIntent);

		} else {
			Globals.logDebug(this, "onStartCommand Regular");
			connectionIntent = intent;
			return handleCommand(intent,
					RadioRuntConnectionHost.STATE_CONNECTING);
		}

	}

	public void registerObserver(final IServiceObserver observer) {
		observers.put(observer, observer);
	}

	public void unregisterObserver(final IServiceObserver observer) {
		observers.remove(observer);
	}

	private int handleCommand(final Intent intent, final int connectionState) {
		// When using START_STICKY the onStartCommand can be called with
		// null intent after the whole service process has been killed.
		// Such scenario doesn't make sense for the service process so
		// returning START_NOT_STICKY for now.
		//
		// Leaving the null check in though just in case.
		//
		// TODO: Figure out the correct start type.
		Globals.logInfo(this, "handleCommand");
		if (intent == null) {
			Globals.logInfo(this, "handleCommand INTENT NULL!");
			return /* START_REDELIVER_INTENT */START_NOT_STICKY;
		}

		final String host = intent.getStringExtra(EXTRA_HOST);
		final int port = intent.getIntExtra(EXTRA_PORT, -1);
		final String username = intent.getStringExtra(EXTRA_USERNAME);
		final String password = intent.getStringExtra(EXTRA_PASSWORD);

		// set a class wide string for notification
		this.connectedServerString = host + ":" + port;

		if (mClient != null
				&& state != RadioRuntConnectionHost.STATE_DISCONNECTED
		/* && mClient.isSameServer(host, port, username, password) */) {
			Globals.logDebug(this,
					"RadioRuntConnectionHost.STATE_DISCONNECTED 2");
			return START_STICKY/* START_REDELIVER_INTENT *//* START_NOT_STICKY */;
		}
		//
		// if (mClient != null && state !=
		// RadioRuntConnectionHost.STATE_RECONNECTING) {
		// return START_STICKY/*START_REDELIVER_INTENT*//*START_NOT_STICKY*/;
		// }

		doConnectionDisconnect();

		mProtocolHost = new ServiceProtocolHost();
		mConnectionHost = new ServiceConnectionHost();
		mAudioHost = new ServiceAudioOutputHost();

		mClient = new RadioRuntConnection(mConnectionHost, host, port,
				username, password, getApplicationContext(), connectionState);

		mProtocol = new MumbleProtocol(mProtocolHost, mAudioHost, mClient,
				getApplicationContext());

		mClientThread = mClient.start(mProtocol);

		return START_STICKY/* START_REDELIVER_INTENT *//* START_NOT_STICKY */;
	}

	private void handleCommandInternal(final Intent intent) {
		Globals.logDebug(this, "handleCommandInternal");

		if (state == RadioRuntConnectionHost.STATE_CONNECTING) {
			return;
		}

		final String host = intent.getStringExtra(EXTRA_HOST);
		final int port = intent.getIntExtra(EXTRA_PORT, -1);
		final String username = intent.getStringExtra(EXTRA_USERNAME);
		final String password = intent.getStringExtra(EXTRA_PASSWORD);

		// set a class wide string for notification
		this.connectedServerString = host + ":" + port;

		doConnectionDisconnectInternal();

		mMwCommLogic.registerGetServerIpAddressServiceReceiver();

		mProtocolHost = new ServiceProtocolHost();
		mConnectionHost = new ServiceConnectionHost();
		mAudioHost = new ServiceAudioOutputHost();

		mClient = new RadioRuntConnection(mConnectionHost, host, port,
				username, password, getApplicationContext(),
				RadioRuntConnectionHost.STATE_CONNECTING/*
														 * RadioRuntConnectionHost.
														 * STATE_RECONNECTING
														 */);
		Globals.logDebug(this, "RadioRuntConnectionHost.STATE_CONNECTING 1");

		mProtocol = new MumbleProtocol(mProtocolHost, mAudioHost, mClient,
				getApplicationContext());

		mClientThread = mClient.start(mProtocol);
		WakeLockManager.release();
	}

	void doConnectionDisconnectInternal() {
		// if (state == RadioRuntConnectionHost.STATE_RECONNECTING || state ==
		// RadioRuntConnectionHost.STATE_CONNECTING) {
		// return;
		// }
		Globals.logDebug(this, "doConnectionDisconnectInternal");
		// First disable all hosts to prevent old callbacks from being
		// processed.
		if (mProtocolHost != null) {
			// mProtocolHost.disable();
			mProtocolHost = null;
		}

		if (mConnectionHost != null) {
			// mConnectionHost.disable();
			mConnectionHost = null;
		}

		if (mAudioHost != null) {
			// mAudioHost.disable();
			mAudioHost = null;
		}

		// Stop threads.
		if (mProtocol != null) {
			mProtocol.stop();
			Globals.logDebug(this,
					"doConnectionDisconnectInternal - mProtocol.stop();");
			mProtocol = null;
		}

		if (mClient != null && mClientThread != null) {
			mClient.disconnectInternal();
			Globals.logDebug(this,
					"doConnectionDisconnectInternal - mClient.disconnectInternal();");

			try {
				Globals.logDebug(this,
						"doConnectionDisconnectInternal - mClientThread.join();");
				mClientThread.join();
			} catch (final InterruptedException e) {
				mClientThread.interrupt();
			}

			// Leave mClient reference intact as its state might still be
			// queried.
			mClientThread = null;
		}

		// Broadcast state, this is synchronous with observers.
		// state = RadioRuntConnectionHost.STATE_RECONNECTING;
		// updateConnectionState();
		// Now observers shouldn't need these anymore.
		users.clear();
		channels.clear();

		// backgroundServiceCheck();
	}

	void doConnectionDisconnect() {
		// First disable all hosts to prevent old callbacks from being
		// processed.
		if (mProtocolHost != null) {
			mProtocolHost.disable();
			mProtocolHost = null;
		}

		if (mConnectionHost != null) {
			mConnectionHost.disable();
			mConnectionHost = null;
		}

		if (mAudioHost != null) {
			mAudioHost.disable();
			mAudioHost = null;
		}

		// Stop threads.
		if (mProtocol != null) {
			mProtocol.stop();
			mProtocol = null;
		}

		if (mClient != null && mClientThread != null) {
			mClient.disconnect();
			try {
				mClientThread.join();
			} catch (final InterruptedException e) {
				mClientThread.interrupt();
			}

			// Leave mClient reference intact as its state might still be
			// queried.
			mClientThread = null;
		}

		// Broadcast state, this is synchronous with observers.
		state = RadioRuntConnectionHost.STATE_DISCONNECTED;
		Globals.logDebug(this, "RadioRuntConnectionHost.STATE_DISCONNECTED 3");
		updateConnectionState();
		// Now observers shouldn't need these anymore.
		users.clear();
		channels.clear();

		backgroundServiceCheck();
	}

	public void backgroundServiceCheck() {
		// If the connection was disconnected and there are no bound
		// connections to this service, finish it.
		Globals.logWarn(this, "mHasConnections " + mHasConnections);
		Globals.logWarn(this, "DISCONNECT_FROM_RALLEE "
				+ DISCONNECT_FROM_RALLEE);

		if (!mHasConnections && DISCONNECT_FROM_RALLEE/*
													 * !settings.
													 * isBackgroundServiceEnabled
													 * ()
													 */) {
			Globals.logWarn(this, "SERVICE STOP SELF");
			stopSelf();
			// }else{
			// // Broadcast state, this is synchronous with observers.
			// state = RadioRuntConnectionHost.STATE_RECONNECTING;
			// updateConnectionState();
		}
	}

	public void hideNotification() {
		showNotification = false;
		if (mNotification != null) {
			stopForegroundCompat(1);
			mNotification = null;
		}
	}

	public void showNotification() {
		showNotification = true;
		// ralleeNotification();
	}

	// public void ralleeNotification() {
	// final Intent LogInActivityIntent;
	// LogInActivityIntent = new Intent(RadioRuntService.this,
	// LogInActivity.class);
	// LogInActivityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP).addFlags(
	// Intent.FLAG_ACTIVITY_NEW_TASK);
	//
	// final Intent HomeActivityIntent;
	// HomeActivityIntent = new Intent(RadioRuntService.this,
	// HomeActivity.class);
	// LogInActivityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP).addFlags(
	// Intent.FLAG_ACTIVITY_NEW_TASK);
	//
	// if (!tempNetState) {
	// if (mNotification != null) {
	// stopForegroundCompat(1);
	// mNotification = null;
	// }
	// mNotification = new Notification(R.drawable.icon,
	// "Please check your internet connection",
	// System.currentTimeMillis());
	// mNotification
	// .setLatestEventInfo(
	// RadioRuntService.this,
	// "Rallee waiting for connection..",
	// "Please check your internet connection or restart Rallee" /*
	// * +
	// * this
	// * .
	// * connectedServerString
	// */,
	// PendingIntent.getActivity(RadioRuntService.this, 0,
	// LogInActivityIntent, 0));
	// } else {
	// switch (state) {
	// case RadioRuntConnectionHost.STATE_CONNECTING:
	// break;
	// case RadioRuntConnectionHost.STATE_CONNECTED:
	// Globals.logDebug(this,
	// "RadioRuntConnectionHost.STATE_CONNECTED 2");
	// if (mNotification != null) {
	// stopForegroundCompat(1);
	// mNotification = null;
	// }
	// mNotification = new Notification(R.drawable.icon,
	// "Rallee connected", System.currentTimeMillis());
	// mNotification.setLatestEventInfo(RadioRuntService.this,
	// "Rallee", "Connected to Rallee" /*
	// * + this.
	// * connectedServerString
	// */, PendingIntent
	// .getActivity(RadioRuntService.this, 0,
	// HomeActivityIntent, 0));
	// break;
	// case RadioRuntConnectionHost.STATE_RECONNECTING:
	// Globals.logDebug(this,
	// "RadioRuntConnectionHost.STATE_RECONNECTING 2");
	// if (mNotification != null) {
	// stopForegroundCompat(1);
	// mNotification = null;
	// }
	// mNotification = new Notification(R.drawable.icon,
	// "Reconnecting to Rallee", System.currentTimeMillis());
	// mNotification.setLatestEventInfo(RadioRuntService.this,
	// "Reconnecting to Rallee",
	// "Reconnecting to Rallee" /*
	// * + this. connectedServerString
	// */, PendingIntent
	// .getActivity(RadioRuntService.this, 0,
	// HomeActivityIntent, 0));
	// break;
	// case RadioRuntConnectionHost.STATE_DISCONNECTED:
	// Globals.logDebug(this,
	// "RadioRuntConnectionHost.STATE_DISCONNECTED 4");
	// if (mNotification != null) {
	// stopForegroundCompat(1);
	// mNotification = null;
	// }
	// mNotification = new Notification(R.drawable.icon,
	// "Rallee disconnected", System.currentTimeMillis());
	// mNotification.setLatestEventInfo(RadioRuntService.this,
	// "Rallee disconnected", "Rallee disconnected" /*
	// * + this.
	// * connectedServerString
	// */,
	// PendingIntent.getActivity(RadioRuntService.this, 0,
	// LogInActivityIntent, 0));
	// break;
	// default:
	// if (mNotification != null) {
	// stopForegroundCompat(1);
	// mNotification = null;
	// }
	// break;
	// }
	// }
	// if (showNotification) {
	// startForegroundCompat(10001, mNotification);
	// } else {
	// if (mNotification != null) {
	// stopForegroundCompat(1);
	// mNotification = null;
	// }
	// }
	// }

	public void missedCallNotification() {
		final Intent LogInActivityIntent;
		LogInActivityIntent = new Intent(RadioRuntService.this,
				LogInActivity.class);
		LogInActivityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP).addFlags(
				Intent.FLAG_ACTIVITY_NEW_TASK);
		mNotification = new Notification(R.drawable.icon,
				"You had a missed call", System.currentTimeMillis());
		mNotification.setLatestEventInfo(RadioRuntService.this, "Rallee",
				"You had missed call from " /* + this.connectedServerString */,
				PendingIntent.getActivity(RadioRuntService.this, 0,
						LogInActivityIntent, 0));
	}

	/**
	 * This is a wrapper around the new startForeground method, using the older
	 * APIs if it is not available.
	 */
	void startForegroundCompat(final int id, final Notification notification) {
		// If we have the new startForeground API, then use it.
		if (mStartForeground != null) {
			mStartForegroundArgs[0] = Integer.valueOf(id);
			mStartForegroundArgs[1] = notification;
			try {
				mStartForeground.invoke(this, mStartForegroundArgs);
			} catch (final InvocationTargetException e) {
				// Should not happen.
				Globals.logError(this, "Unable to invoke startForeground", e);
			} catch (final IllegalAccessException e) {
				// Should not happen.
				Globals.logError(this, "Unable to invoke startForeground", e);
			}
			return;
		}

		// Fall back on the old API.
		setForeground(true);
		((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).notify(
				id, notification);
	}

	/**
	 * This is a wrapper around the new stopForeground method, using the older
	 * APIs if it is not available.
	 */
	void stopForegroundCompat(final int id) {
		// If we have the new stopForeground API, then use it.
		if (mStopForeground != null) {
			mStopForegroundArgs[0] = Boolean.TRUE;
			try {
				mStopForeground.invoke(this, mStopForegroundArgs);
			} catch (final InvocationTargetException e) {
				// Should not happen.
				Globals.logError(this, "Unable to invoke stopForeground", e);
			} catch (final IllegalAccessException e) {
				// Should not happen.
				Globals.logError(this, "Unable to invoke stopForeground", e);
			}
			return;
		}

		// Fall back on the old API. Note to cancel BEFORE changing the
		// foreground state, since we could be killed at that point.
		((NotificationManager) getSystemService(NOTIFICATION_SERVICE))
				.cancel(id);
		setForeground(false);
	}

	void updateConnectionState() {
		final int oldState = serviceState;
		Globals.logDebug(this, "updateConnectionState");
		switch (state) {
		case RadioRuntConnectionHost.STATE_CONNECTING:
			serviceState = CONNECTION_STATE_CONNECTING;
			// saveLogToSDCard("CONNECTION_STATE_CONNECTING", null);
			Globals.logDebug(this, "RadioRuntService.STATE_CONNECTING 2");
			break;
		case RadioRuntConnectionHost.STATE_CONNECTED:
			if (reconnAlarmReceiver != null) {
				stopReconnectionAlarm();
				Globals.logDebug(this, "setStopReconnectionALarm");
			}
			Globals.logDebug(this, "RadioRuntConnectionHost.STATE_CONNECTED 3");
			// saveLogToSDCard("STATE_CONNECTED",
			// Utility.timestampOfConnectedState);
			// ralleeNotification();
			serviceState = synced ? CONNECTION_STATE_CONNECTED
					: CONNECTION_STATE_SYNCHRONIZING;
			List<RRChannels> chList = getChannelList();
			RadioRuntService.tcpTimeOuted = false;

			// ReConnect to the last Channel we were
			Globals.logDebug(this, "RRSRV " + ON_MY_CHANNEL);
			if (ON_MY_CHANNEL != 0) {
				for (int i = 0; i < chList.size(); i++) {
					if (chList.get(i).id == ON_MY_CHANNEL) {
						joinChannel(chList.get(i).id);
						break;
					}
				}
			}
			break;
		case RadioRuntConnectionHost.STATE_RECONNECTING:
			Globals.logDebug(this,
					"RadioRuntConnectionHost.STATE_RECONNECTING 3");
			// ralleeNotification();
			serviceState = CONNECTION_STATE_RECONNECTING;
			break;
		case RadioRuntConnectionHost.STATE_DISCONNECTED:
			Globals.logDebug(this,
					"RadioRuntConnectionHost.STATE_DISCONNECTED 5");
			// ralleeNotification();
			serviceState = CONNECTION_STATE_DISCONNECTED;
			break;
		default:
			Assert.fail();
		}
		if (oldState != serviceState) {
			for (final IServiceObserver observer : observers.values()) {
				try {
					observer.onConnectionStateChanged(serviceState);
				} catch (final RemoteException e) {
					Globals.logError(this, "Failed to update connection state",
							e);
				}
			}
		}
	}

	// This method isn't needed but maybe we need it on an other occasion
	// FB_API_on
	private String getNetworkStateString(NetworkInfo.State state) {
		String stateString = "Unknown";

		switch (state) {
		case CONNECTED:
			stateString = "Connected";
			// tempNetState = true;
			break;
		case CONNECTING:
			stateString = "Connecting";
			break;
		case DISCONNECTED:
			stateString = "Disconnected";
			// tempNetState = false;
			break;
		case DISCONNECTING:
			stateString = "Disconnecting";
			break;
		case SUSPENDED:
			stateString = "Suspended";
			break;
		default:
			stateString = "Unknown";
			break;
		}
		Globals.logDebug(this, "NetworkState " + stateString);
		return stateString;
	}

	public static boolean tempNetState = false;
	public static boolean showNotification = false;
	public static boolean tcpTimeOuted = false;

	// private void getNetworkState(boolean netState){
	// if(netState){
	// if(!tempNetState){
	// if(mClient != null){
	// Log.i("connRallee", "updateInternetNewConn");
	// if(Utility.switchChannel == null){
	// Log.i("connRallee", "handleCommandInternal");
	// handleCommandInternal(connectionIntent);
	// }else{
	// Log.i("connRallee", "switchServer");
	// // final String host = Utility.switchChannel.serverIpAdr;
	// // final int port = Integer.valueOf(Utility.switchChannel.port);
	// // final String username = Utility.userUID.toString();
	// // final String password = Utility.userUID.toString();
	// //// c.close();
	// //
	// // Intent changeServerIntent = new Intent(LogInActivity.this,
	// RadioRuntService.class);
	// // changeServerIntent.setAction(RadioRuntService.ACTION_CONNECT);
	// // changeServerIntent.putExtra(RadioRuntService.EXTRA_HOST, host);
	// // changeServerIntent.putExtra(RadioRuntService.EXTRA_PORT, port);
	// // changeServerIntent.putExtra(RadioRuntService.EXTRA_USERNAME,
	// username);
	// // changeServerIntent.putExtra(RadioRuntService.EXTRA_PASSWORD,
	// password);
	// switchServer(LogInActivity.changeServerIntent);
	// }
	//
	// if(HOME_ACTIVITY_PAUSED == 1){
	// setPauseMumProtocolState(true);
	// }else{
	// setPauseMumProtocolState(false);
	// }
	// }
	// }
	// tempNetState = true;
	// }else{
	// Log.i("connRallee", "showDisconnectedNotification");
	// tempNetState = false;
	// showDisconnectedNotification();
	// disconnect();
	// }
	// }

	private void onInternetConnectedReconnect() {
		WakeLockManager.acquire(getApplicationContext());
		if (mClient != null) {
			Globals.logDebug(this,
					"onInternetConnectedReconnect client is NOT null");
			// state = RadioRuntConnectionHost.STATE_RECONNECTING;
			// if(SWITCH_SERVER == 0/*Utility.switchChannel == null*/){
			if (state == RadioRuntConnectionHost.STATE_RECONNECTING
					|| state == RadioRuntConnectionHost.STATE_CONNECTING) {

				// state = RadioRuntConnectionHost.STATE_CONNECTING;
				updateConnectionState();

				if (2 != RadioRuntConnection.CONNECTION_ERROR) {
					Intent i = new Intent();
					i.setAction(ReconnectionAlarmReceiver.ACTION_RECONNECT_STOP);
					sendBroadcast(i);
				}
				disconnectifHostXxx();
				connectionIntent = settings.getConnectionIntent();
				handleCommandInternal(connectionIntent);

			} else {
				Globals.logDebug(
						this,
						"onInternetConnectedReconnect state == RadioRuntConnectionHost.STATE_RECONNECTING || state == RadioRuntConnectionHost.STATE_CONNECTING");
			}
			// }else if(SWITCH_SERVER == 1){
			// Log.i("connRallee", "switchServer");
			// // final String host = Utility.switchChannel.serverIpAdr;
			// processTcp
			// // final int port = Integer.valueOf(Utility.switchChannel.port);
			// // final String username = Utility.userUID.toString();
			// // final String password = Utility.userUID.toString();
			// //// c.close();
			// //
			// // Intent changeServerIntent = new Intent(LogInActivity.this,
			// RadioRuntService.class);
			// // changeServerIntent.setAction(RadioRuntService.ACTION_CONNECT);
			// // changeServerIntent.putExtra(RadioRuntService.EXTRA_HOST,
			// host);
			// // changeServerIntent.putExtra(RadioRuntService.EXTRA_PORT,
			// port);
			// // changeServerIntent.putExtra(RadioRuntService.EXTRA_USERNAME,
			// username);
			// // changeServerIntent.putExtra(RadioRuntService.EXTRA_PASSWORD,
			// password);
			// // switchServerIntent(changeServerIntent);
			// handleCommand(changeServerIntent);
			// }else{
			// handleCommandInternal(connectionIntent);
			// }

			// if(HOME_ACTIVITY_PAUSED == 1){
			// setPauseMumProtocolState(true);
			// }else{
			// setPauseMumProtocolState(false);
			// }
		} else {
			Globals.logDebug(this,
					"onInternetConnectedReconnect client is null");
		}
	}

	private class ConnectivityReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (state == RadioRuntConnectionHost.STATE_CONNECTING) {
				return;
			}
			if (state == RadioRuntConnectionHost.STATE_CONNECTED) {

			}
			WakeLockManager.acquire(getApplicationContext());
			Globals.logDebug(this, "ConnectivityReceiver onReceive");
			// checkInternetConnection(intent);
			if (mClient != null) {
				reconnectServer();
			}
		}

		// public boolean isOnline() {
		// ConnectivityManager connMgr = (ConnectivityManager)
		// getSystemService(Context.CONNECTIVITY_SERVICE);
		// NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		// return (networkInfo != null && networkInfo.isConnected());
		// }

	}

	NetworkInfo info;
	String infoString;
	String internetState;
	boolean wifiConnected = false;
	boolean mobileConnected = false;

	public void checkInternetConnection(Intent intent) {

		ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

		NetworkInfo activeInfo = connMgr.getActiveNetworkInfo();
		info = intent
				.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
		if (info != null) {

			Globals.logDebug(this, "***info***");
			internetState = getNetworkStateString(info.getState());
			String stateString = info.toString().replace(',', '\n');

			// infoString =
			// String.format("Network Type: %s\nNetwork State: %s\n\n%s",
			// info.getTypeName(),state,stateString);

			Globals.logDebug(this, "NETWORK " + info.getTypeName());
			Globals.logDebug(this, "NETWORK " + internetState);
			Globals.logDebug(this, info.toString());

		} else {
			Globals.logDebug(this, "NO ACTIVE NETWORK");
		}

		if (activeInfo != null) {

			Globals.logDebug(this, "***activeInfo***");
			internetState = getNetworkStateString(activeInfo.getState());
			String stateString = activeInfo.toString().replace(',', '\n');

			// infoString =
			// String.format("Network Type: %s\nNetwork State: %s\n\n%s",
			// info.getTypeName(),state,stateString);

			Globals.logDebug(this, "NETWORK " + activeInfo.getTypeName());
			Globals.logDebug(this, "NETWORK " + internetState);
			Globals.logDebug(this, activeInfo.toString());

		} else {
			Globals.logDebug(this, "NO ACTIVE NETWORK");
		}

		if (activeInfo != null && activeInfo.isConnected()
				&& hasActiveInternetConnection(getApplicationContext())) {// isThereInternetConnection(connMgr,
																			// activeInfo.getType()))
																			// {
			wifiConnected = activeInfo.getType() == ConnectivityManager.TYPE_WIFI;
			mobileConnected = activeInfo.getType() == ConnectivityManager.TYPE_MOBILE;
		} else {
			wifiConnected = false;
			mobileConnected = false;
		}

		tempNetState = (mobileConnected || wifiConnected);
		// ralleeNotification();

		if (tempNetState) {// if(state.equals("Connected")){//if(isThereInternetConnection()
							// && state.equals("Connected") /*&& (tcpTimeOuted
							// || SWITCH_SERVER == 1)*/){
			Globals.logDebug(this,
					"tempNetState = true, onInternetConnectedReconnect");
			onInternetConnectedReconnect();
		} else {

			// if there is no internet connection stop automatic reconnection
			Intent i = new Intent();
			i.setAction(ReconnectionAlarmReceiver.ACTION_RECONNECT_STOP);
			sendBroadcast(i);
			stopReconnectionAlarm();
			Globals.logDebug(this, "tempNetState = false");
		}
		WakeLockManager.release();
	}

	public boolean hasActiveInternetConnection(Context context) {
		try {
			HttpURLConnection urlc = (HttpURLConnection) (new URL(
					"http://www.google.com").openConnection());
			urlc.setRequestProperty("User-Agent", "Test");
			urlc.setRequestProperty("Connection", "close");
			urlc.setConnectTimeout(1000);
			urlc.connect();
			tempNetState = (urlc.getResponseCode() == 200);
			if (tempNetState) {
				Globals.logDebug(this, "isThereInternetConnection - YES");
			} else {
				Globals.logDebug(this, "isThereInternetConnection - NO");
			}
			return tempNetState;
		} catch (IOException e) {
			tempNetState = false;
			Globals.logDebug(this, "isThereInternetConnection - NO");
			return false;
		}
	}

	public boolean isThereInternetConnection(ConnectivityManager cm,
			int networkType) {
		try {

			tempNetState = cm.requestRouteToHost(networkType,
					lookupHost("8.8.8.8"));
			if (tempNetState) {
				Globals.logDebug(this, "isThereInternetConnection - YES");
			} else {
				Globals.logDebug(this, "isThereInternetConnection - NO");
			}
			return tempNetState;
		} catch (UnknownHostException e) {
			tempNetState = false;
			Globals.logDebug(this, "isThereInternetConnection - NO");
			return false;
		} catch (IOException e) {
			tempNetState = false;
			Globals.logDebug(this, "isThereInternetConnection - NO");
			return false;
		}
	}

	public int lookupHost(String hostname) throws UnknownHostException,
			IOException {
		InetAddress inetAddress;
		try {
			inetAddress = InetAddress.getByName(hostname);
		} catch (UnknownHostException e) {
			return -1;
		}
		byte[] addrBytes;
		int addr;
		addrBytes = inetAddress.getAddress();
		addr = ((addrBytes[3] & 0xff) << 24) | ((addrBytes[2] & 0xff) << 16)
				| ((addrBytes[1] & 0xff) << 8) | (addrBytes[0] & 0xff);
//		System.out.println(addr);
		return addr;
	}

	private static boolean startReconnectionAlarm = true;
	private AlarmManager alarmMgr;
	Intent reconnectionAlarmintent;
	PendingIntent reconnAlarmPendingIntent;

	public void reconnectionAlarm(Context context, int timeoutInMiliSeconds) {

		Globals.logDebug(this, "Reconnection Alarm started");

		startReconnectionAlarm = true;

		alarmMgr = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);
		reconnectionAlarmintent = new Intent();
		reconnectionAlarmintent
				.setAction(ReconnectionAlarmReceiver.ACTION_RECONNECT_START);
		reconnAlarmPendingIntent = PendingIntent.getBroadcast(context, 0,
				reconnectionAlarmintent, 0);

		Globals.logDebug(this, "ReconnectionAlarm pending intent:"
				+ reconnAlarmPendingIntent);

		alarmMgr.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
				SystemClock.elapsedRealtime(), timeoutInMiliSeconds,
				reconnAlarmPendingIntent);

		Globals.logDebug(this, "ReconnectionAlarm set");

	}

	public void stopReconnectionAlarm() {
		RadioRuntService.startReconnectionAlarm = false;
		Globals.logDebug(this, "ReconnectionAlarm stoped");
		if (alarmMgr != null) {
			alarmMgr.cancel(reconnAlarmPendingIntent);
			alarmMgr = null;
		}

		if (reconnAlarmReceiver != null) {
			unregisterReceiver(reconnAlarmReceiver);
			reconnAlarmReceiver = null;
		}

	}

	public class ReconnectionAlarmReceiver extends BroadcastReceiver {

		public static final String ACTION_RECONNECT_START = "com.radiorunt.ReconnectionAlarmReceiver.start";
		public static final String ACTION_RECONNECT_STOP = "com.radiorunt.ReconnectionAlarmReceiver.stop";

		@Override
		public void onReceive(Context context, Intent intent) {

			Globals.logDebug(this, "ReconnectionAlarm onReceive");

			String action = intent.getAction();

			if (state == RadioRuntConnectionHost.STATE_CONNECTED) {
				Globals.logDebug(this,
						"RadioRuntConnectionHost.STATE_CONNECTED 4");
				return;
			}

			Globals.logDebug(this, "ReconnectionAlarm onReceive start");

			if (!startReconnectionAlarm) {
				return;
			}
			WakeLockManager.acquire(getApplicationContext());

			// here you can get the extras you passed in when creating the alarm
			// Bundle bundle = intent.getBundleExtra(ALARM_BUNDLE);
			Globals.logDebug(this, "=========Reconnection Alarm called========");

			if (action.equals(ACTION_RECONNECT_START)) {
				Globals.logDebug(this,
						"ReconnectionAlarm ACTION_RECONNECT_START");
				// handleCommandInternal(connectionIntent);
				checkInternetConnection(intent);
			} else if (action.equals(ACTION_RECONNECT_STOP)) {
				Globals.logDebug(this,
						"ReconnectionAlarm ACTION_RECONNECT_STOP");
				stopReconnectionAlarm();
				WakeLockManager.release();
			}

		}
	}

	Handler mHandl = new Handler() {
		@Override
		public void handleMessage(android.os.Message msg) {
			if (wakelock != null && wakelock.isHeld() == true) {
				try {
					wakelock.release();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					// e.printStackTrace();
				}
			}
		};
	};
	private int m_interval = 90000;

	public void killWakeLock() {
		mHandl.sendEmptyMessageDelayed(1, m_interval);
	}

	public ArrayList<String> getFbList() {
		HashMap<String, Integer> friendsIndexer = new HashMap<String, Integer>();
		;
		RRChannels rootChn = new RRChannels();

		Cursor c = getApplicationContext().getContentResolver().query(
				DbContentProvider.CONTENT_URI_USER_TABLE, null, null, null,
				null);

		if (c != null) {
			int size = c.getCount();
			while (c.moveToNext()) {
				friendsIndexer.put(c.getString(c
						.getColumnIndex(DbContentProvider.USER_COL_ID)), c
						.getPosition());
			}

			c.close();
		}

		Set<String> sectionLetters = friendsIndexer.keySet();

		// create a friend list from the set to sort
		ArrayList<String> sectionList = new ArrayList<String>(sectionLetters);

		// Create channels list for the filtering list
		ArrayList<String> channelList = new ArrayList<String>();
		channelList.add(0, "0123456789");
		// ivan
		// sectionList.add("0700416621");
		// lepa sojic
		// sectionList.add("0100000148346687");

		return sectionList;

	}

	// sending list of fb friends and channels to the murmur server for
	// filtering
	public void sendListsForFiltering() {
		Cursor c = null;
		Cursor cursorFB = null;
		String listMessage = "";
		try {

			HashMap<String, Integer> friendsIndexer = new HashMap<String, Integer>();
			RRChannels rootChn = new RRChannels();

			// c = getApplicationContext().getContentResolver().query(
			// DbContentProvider.CONTENT_URI_USER_TABLE, null,
			// DbContentProvider.USER_COL_INSTALLED + "=?",
			// new String[] { "1" }, null);
			c = getApplicationContext().getContentResolver().query(
					DbContentProvider.CONTENT_URI_USER_TABLE, null, null, null,
					null);

			if (c != null && c.moveToFirst()) {
				do {
					friendsIndexer.put(c.getString(c
							.getColumnIndex(DbContentProvider.USER_COL_ID)), c
							.getPosition());
				} while (c.moveToNext());
			}

			Set<String> sectionLetters = friendsIndexer.keySet();

			// create a friend list from the set to sort
			ArrayList<String> sectionList = new ArrayList<String>(
					sectionLetters);
			// ArrayList<String> sectionList = new ArrayList<String>();
			// Create channels list for the filtering list
			ArrayList<String> channelList = new ArrayList<String>();

			cursorFB = RalleeApp
					.getInstance()
					.getContentResolver()
					.query(DbContentProvider.CONTENT_URI_FB_GROUPS_TABLE,
							new String[] {}, null, null, null);

			if (cursorFB != null && cursorFB.moveToFirst()) {
				do {
					String fbgId = "_fbgroup_"
							+ cursorFB
									.getString(cursorFB
											.getColumnIndex(DbContentProvider.FB_GROUP_COL_ID));
					channelList.add(fbgId);
				} while (cursorFB.moveToNext());
			}

			RRPushFilteringMessagePayload filteringListMsg = new RRPushFilteringMessagePayload();
			filteringListMsg.listOfFriends = sectionList;
			filteringListMsg.channelName = "";
			filteringListMsg.listOfChannels = channelList;
			filteringListMsg.payloadType = "filteringOn";
			filteringListMsg.sender = RalleeApp.getInstance().getRalleeUID();
			filteringListMsg.timestamp = 0;

			ObjectMapper mapper = new ObjectMapper();

			String fbFrindList = "{\"payloadType\":\"\",\"listOfChannels\":[\"212912555503986\",\"2406504339\",\"355551534495613\"],"
					+ "\"sender\":\"0700416621\",\"channelName\":\"\",\"timestamp\":-1184800944,\"listOfFriends\":[\"0100000148346687\",\"01760079077\"]}";

			rootChn.id = 0;
			rootChn.name = "Root";

			listMessage = mapper.writeValueAsString(filteringListMsg);
			int n = 0;
			for (int i = 0; i < listMessage.length(); i += 1024) {
				n = n + 1;
				if (i + 1024 < listMessage.length())
					Globals.logDebug(this,
							"LOG:" + n + " sendListsForFiltering: "
									+ listMessage.substring(i, i + 1024));
				else
					Globals.logDebug(
							this,
							"LOG:"
									+ n
									+ " sendListsForFiltering: "
									+ listMessage.substring(i,
											listMessage.length()));
			}

			// Log.i("msg", "sendListsForFiltering: "+listMessage);
			sendChannelTextMessage(listMessage, rootChn);
		} catch (JsonGenerationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (c != null) {
				c.close();
			}
			if (cursorFB != null) {
				cursorFB.close();
			}
			// saveLogToSDCard(listMessage, null);
		}
	}

	private void sendOnePing() {
		final long timestamp = System.currentTimeMillis();
		final byte[] udpBuffer = new byte[9];

		// UDP
		udpBuffer[1] = (byte) ((timestamp >> 56) & 0xFF);
		udpBuffer[2] = (byte) ((timestamp >> 48) & 0xFF);
		udpBuffer[3] = (byte) ((timestamp >> 40) & 0xFF);
		udpBuffer[4] = (byte) ((timestamp >> 32) & 0xFF);
		udpBuffer[5] = (byte) ((timestamp >> 24) & 0xFF);
		udpBuffer[6] = (byte) ((timestamp >> 16) & 0xFF);
		udpBuffer[7] = (byte) ((timestamp >> 8) & 0xFF);
		udpBuffer[8] = (byte) ((timestamp) & 0xFF);

		if (mClient != null && 0 == RadioRuntConnection.CONNECTION_ERROR) {
			try {
				mClient.sendUdpMessage(udpBuffer, udpBuffer.length, true);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void disconnectifHostXxx() {
		String host = settings.getConnectionIntentHost();
		if (host.equals("xxx")) {
			// set shared preference and disconnect state of connection
			DISCONNECT_FROM_RALLEE = true;
			settings.setExitCode(true);

			SWITCH_CHANNEL = 99;
			messagesClearAll();
			disconnect();
		}

	}

	// private void saveLogToSDCard(String log, Timestamp rrcConnectedTime) {
	//
	// Timestamp datetime = new
	// Timestamp(Calendar.getInstance(TimeZone.getTimeZone("GMT"))
	// .getTimeInMillis());
	// String filename = "log.txt";
	//
	// try {
	// File file = new File(getApplicationContext().getExternalFilesDir(
	// null), filename);
	// if (file != null) {
	// file.createNewFile();
	// if (file.exists() && file.isFile()) {
	// FileWriter writer = new FileWriter(file, true);
	// writer.append("\n\n");
	// writer.append("**********");
	// writer.append("Save log data and time: ");
	// writer.append(datetime.toString());
	// writer.append("\n");
	// if(rrcConnectedTime != null){
	// writer.append("time: "+rrcConnectedTime.toString());
	// writer.append("\n");
	// writer.append("RadioRuntConnectionHost.STATE_CONNECTED");
	// writer.append("\n");
	// rrcConnectedTime = null;
	// }
	// writer.append(log);
	// writer.append("\n");
	// writer.append("**********");
	// writer.flush();
	// writer.close();
	// }
	// }
	// } catch (Exception e) {
	// e.printStackTrace();
	// Log.i("listLog", "EXTERNAL STORAGE: " + e.toString());
	// }
	// }

}
