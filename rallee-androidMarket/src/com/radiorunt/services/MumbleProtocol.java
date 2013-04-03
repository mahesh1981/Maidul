package com.radiorunt.services;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;
import net.sf.mumble.MumbleProto.ChannelRemove;
import net.sf.mumble.MumbleProto.ChannelState;
import net.sf.mumble.MumbleProto.ChannelState.Builder;
import net.sf.mumble.MumbleProto.CodecVersion;
import net.sf.mumble.MumbleProto.CryptSetup;
import net.sf.mumble.MumbleProto.PermissionDenied;
import net.sf.mumble.MumbleProto.PermissionDenied.DenyType;
import net.sf.mumble.MumbleProto.Authenticate;
import net.sf.mumble.MumbleProto.PermissionQuery;
import net.sf.mumble.MumbleProto.Ping;
import net.sf.mumble.MumbleProto.Reject;
import net.sf.mumble.MumbleProto.ServerConfig;
import net.sf.mumble.MumbleProto.ServerSync;
import net.sf.mumble.MumbleProto.TextMessage;
import net.sf.mumble.MumbleProto.UserRemove;
import net.sf.mumble.MumbleProto.UserState;
import net.sf.mumble.MumbleProto.UserStats;
import net.sf.mumble.MumbleProto.Version;

import android.content.Context;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;

import com.google.protobuf.ByteString;
import com.radiorunt.businessobjects.RRChannels;
import com.radiorunt.businessobjects.RRUser;
import com.radiorunt.businessobjects.RRMessages;
import com.radiorunt.facebook.Utility;
import com.radiorunt.services.audio.AudioOutput;
import com.radiorunt.services.audio.AudioOutputHost;
import com.radiorunt.utilities.Globals;

public class MumbleProtocol {
	public enum MessageType {
		Version, UDPTunnel, Authenticate, Ping, Reject, ServerSync, ChannelRemove, ChannelState, UserRemove, UserState, BanList, TextMessage, PermissionDenied, ACL, QueryUsers, CryptSetup, ContextActionAdd, ContextAction, UserList, VoiceTarget, PermissionQuery, CodecVersion, UserStats, RequestBlob, ServerConfig
	}

	public static final int UDPMESSAGETYPE_UDPVOICECELTALPHA = 0;
	public static final int UDPMESSAGETYPE_UDPPING = 1;
	public static final int UDPMESSAGETYPE_UDPVOICESPEEX = 2;
	public static final int UDPMESSAGETYPE_UDPVOICECELTBETA = 3;

	public static final int CODEC_NOCODEC = -1;
	public static final int CODEC_ALPHA = UDPMESSAGETYPE_UDPVOICECELTALPHA;
	public static final int CODEC_BETA = UDPMESSAGETYPE_UDPVOICECELTBETA;
	// ivan
	public static final int CODEC_GAMA = UDPMESSAGETYPE_UDPVOICESPEEX;

	public static final int SAMPLE_RATE = 16000;

	public static final int FRAME_SIZE = 320;
	// public static boolean pauseState = false;
	public static boolean notOnGlobalServer = false;

	Boolean firstTimeUserState = true;
	Boolean firstTimeChannelState = true;

	/**
	 * The time window during which the last successful UDP ping must have been
	 * transmitted. If the time since the last successful UDP ping is greater
	 * than this treshold the connection falls back on TCP tunneling.
	 * 
	 * NOTE: This is the time when the last successfully received ping was SENT
	 * by the client.
	 * 
	 * 61000 gives 1 second reply-time as the ping interval is 60000 seconds
	 * currently.
	 */
	public static final int UDP_PING_TRESHOLD = 121000;

	public static final int PING_TIME = 120; // in seconds

	private static final MessageType[] MT_CONSTANTS = MessageType.class
			.getEnumConstants();

	public Map<Integer, RRChannels> channels = new HashMap<Integer, RRChannels>();
	public Map<Integer, RRUser> users = new HashMap<Integer, RRUser>();
	public RRChannels currentChannel = null;
	public RRUser currentUser = null;
	public boolean canSpeak = true;
	public int codec = CODEC_NOCODEC;
	private final AudioOutputHost audioHost;
	private final Context ctx;

	private AudioOutput ao;
	private Thread audioOutputThread;
	private PingThread pingThread;
	private PingAlarm pingAlarm;

	private final RadioRuntProtocolHost host;
	private final RadioRuntConnection conn;
	private int iddleSec = 0;

	private boolean stopped = false;
	private boolean suspendFlag;

	public MumbleProtocol(final RadioRuntProtocolHost host,
			final AudioOutputHost audioHost,
			final RadioRuntConnection connection, final Context ctx) {
		this.host = host;
		this.audioHost = audioHost;
		this.conn = connection;
		this.ctx = ctx;

		this.host.setSynchronized(false);
	}

	public final void joinChannel(final int channelId) {
		try {
			final UserState.Builder us = UserState.newBuilder();
			us.setSession(currentUser.session);
			us.setChannelId(channelId);
			conn.sendTcpMessage(MessageType.UserState, us);
		} catch (Exception e) {
			Globals.logError(this, "joinChannel " + e);
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public final void createChannel(final String channelName,
			boolean channelTemporary, final String channelDescription) {
		try {
			ChannelState.Builder cs = ChannelState.newBuilder();
			cs.setParent(0);
			cs.setName(channelName);
			cs.setDescription(channelDescription);
			cs.setTemporary(channelTemporary);
			conn.sendTcpMessage(MessageType.ChannelState, cs);
		} catch (Exception e) {
			Globals.logDebug(this, "createChannel " + e);
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public final void deafenSelf(Boolean deafenState) {
		UserState.Builder usD = UserState.newBuilder();
		usD.setDeaf(deafenState);
		if (!deafenState) {
			usD.setMute(false);
		}
		conn.sendTcpMessage(MessageType.UserState, usD);
	}

	public final void muteSelf(Boolean muteState) {
		UserState.Builder usM = UserState.newBuilder();
		usM.setMute(muteState);
		conn.sendTcpMessage(MessageType.UserState, usM);
	}

	public void processTcp(final short type, final byte[] buffer)
			throws IOException {
		if (stopped) {
			Globals.logDebug(this, "processTcp stopped");
			return;
		}

		if (type >= MT_CONSTANTS.length) {
			Globals.logWarn(this,
					String.format("Unknown message type %s", type));
			return;
		}

		final MessageType t = MT_CONSTANTS[type];
		Globals.logDebug(this, "processTcp " + t);

		RRChannels channel;
		RRUser user;

		firstTimeUserState = true;
		firstTimeChannelState = true;
		Globals.logDebug(this, "NOT paused");
		switch (t) {
		case UDPTunnel:
			Globals.logDebug(this, "MumbelProtocol UDPTunnel");
			Globals.logDebug(this, "MumbleProtocol - Case UDP Tunel");
			processUdp(buffer, buffer.length);
			break;
		case Ping:
			// ignore
			Ping receivedPing = Ping.parseFrom(buffer);
			int getGood = receivedPing.getGood();
			Globals.logDebug(this, "getGood " + getGood);
			int getLost = receivedPing.getLost();
			Globals.logDebug(this, "getLost " + getLost);
			int getLate = receivedPing.getLate();
			Globals.logDebug(this, "getLate " + getLate);
			int getResync = receivedPing.getResync();
			Globals.logDebug(this, "getResync " + getResync);
			long getTimestamp = receivedPing.getTimestamp();
			Globals.logDebug(this, "getTimestamp " + getTimestamp);
			float getUdpPingAvg = receivedPing.getUdpPingAvg();
			Globals.logDebug(this, "getUdpPingAvg " + getUdpPingAvg);
			boolean hasGood = receivedPing.hasGood();
			Globals.logDebug(this, "hasGood " + hasGood);
			boolean hasLost = receivedPing.hasLost();
			Globals.logDebug(this, "hasLost " + hasLost);
			boolean hasLate = receivedPing.hasLate();
			Globals.logDebug(this, "hasLate " + hasLate);
			boolean hasResync = receivedPing.hasResync();
			Globals.logDebug(this, "hasResync " + hasResync);
			boolean hasTimestamp = receivedPing.hasTimestamp();
			Globals.logDebug(this, "hasTimestamp " + hasTimestamp);
			break;
		case CodecVersion:
			final boolean oldCanSpeak = canSpeak;
			final CodecVersion codecVersion = CodecVersion.parseFrom(buffer);
			codec = CODEC_GAMA;
			canSpeak = canSpeak && (codec != CODEC_NOCODEC);

			if (canSpeak != oldCanSpeak) {
				host.currentUserUpdated();
			}

			break;
		case Reject:
			final Reject reject = Reject.parseFrom(buffer);
			final String errorString = String.format("Connection rejected: %s",
					reject.getReason());
			Globals.logDebug(this,
					"MumbelProtocol Reject:" + reject.getReason());
			host.setError(errorString);
			Globals.logError(
					this,
					String.format("Received Reject message: %s",
							reject.getReason()));
			break;
		case ServerSync:
			final ServerSync ss = ServerSync.parseFrom(buffer);

			// We do some things that depend on being executed only once here
			// so for now assert that there won't be multiple ServerSyncs.
			Assert.assertNull("A second ServerSync received.", currentUser);

			currentUser = findUser(ss.getSession());
			currentUser.isCurrent = true;
			currentChannel = currentUser.getChannel();

			startPingThread(PING_TIME);

			// pingThread = new PingThread("Ping", conn, ctx);
			// Globals.logDebug(this, ">>> " + t);

			ao = new AudioOutput(ctx, audioHost);
			audioOutputThread = new Thread(ao, "audio output");
			audioOutputThread.start();

			final UserState.Builder usb = UserState.newBuilder();
			usb.setSession(currentUser.session);
			conn.sendTcpMessage(MessageType.UserState, usb);

			host.setSynchronized(true);

			host.currentChannelChanged();
			host.currentUserUpdated();
			break;
		case ChannelState:
			final ChannelState cs = ChannelState.parseFrom(buffer);
			channel = findChannel(cs.getChannelId());
			Globals.logDebug(this,
					"MumbelProtocol ChannelState:" + cs.getChannelId());
			Globals.logDebug(this, "MumbelProtocol ChannelName:" + cs.getName());
			if (channel != null) {
				if (cs.hasName()) {
					channel.name = cs.getName();
				}
				host.channelUpdated(channel);
				break;
			}

			// New channel
			channel = new RRChannels();
			channel.id = cs.getChannelId();
			channel.name = cs.getName();
			channels.put(channel.id, channel);
			host.channelAdded(channel);
			Globals.logDebug(this,
					"MumbelProtocol ChannelState ADDED:" + cs.getChannelId());
			break;
		case ChannelRemove:
			final ChannelRemove cr = ChannelRemove.parseFrom(buffer);
			channel = findChannel(cr.getChannelId());
			Globals.logDebug(this,
					"MumbelProtocol ChannelRemove:" + cr.getChannelId());
			channel.removed = true;
			channels.remove(channel.id);
			host.channelRemoved(channel.id);
			break;
		case UserState:
			final UserState us = UserState.parseFrom(buffer);

			user = findUser(us.getSession());
			RRChannels oldChannel = null;

			Globals.logDebug(this,
					"MumbelProtocol USER STATE:" + us.getSession());

			boolean added = false;
			boolean currentUserUpdated = false;
			boolean channelUpdated = false;
			boolean userStateChanged = false;

			if (user == null) {
				user = new RRUser();
				user.session = us.getSession();
				users.put(user.session, user);
				added = true;
			}

			if (us.hasSelfDeaf() || us.hasSelfMute()) {
				if (us.getSelfDeaf()) {
					user.userState = RRUser.USERSTATE_DEAFENED;
				} else if (us.getSelfMute()) {
					user.userState = RRUser.USERSTATE_MUTED;
				} else {
					user.userState = RRUser.USERSTATE_NONE;
				}
				userStateChanged = true;
			}

			if (us.hasMute()) {
				user.muted = us.getMute();
				user.userState = user.muted ? RRUser.USERSTATE_MUTED
						: RRUser.USERSTATE_NONE;
			}

			if (us.hasDeaf()) {
				user.deafened = us.getDeaf();
				user.muted |= user.deafened;
				user.userState = user.deafened ? RRUser.USERSTATE_DEAFENED
						: (user.muted ? RRUser.USERSTATE_MUTED
								: RRUser.USERSTATE_NONE);
				userStateChanged = true;
			}

			if (us.hasSuppress()) {
				user.userState = us.getSuppress() ? RRUser.USERSTATE_MUTED
						: RRUser.USERSTATE_NONE;
			}

			if (us.hasName()) {
				user.userName = Utility.parseSNData(us.getName()).getAsString(
						Utility.RALLEE_ID);
			}

			if (added || us.hasChannelId()) {
				Integer channelKey = us.getChannelId();
				RRChannels newChannel = channels.get(channelKey);
				newChannel.userCount++;
				channels.put(channelKey, newChannel);

				if (user.getChannel() != null) {
					oldChannel = channels.get(user.getChannel().id);
					oldChannel.userCount--;
					channels.put(oldChannel.id, oldChannel);
				}

				user.setChannel(newChannel);
				channelUpdated = true;
			}
			// If this is the current user, do extra updates on local state.
			if (currentUser != null && us.getSession() == currentUser.session) {
				if (us.hasMute() || us.hasSuppress()) {
					// TODO: Check the logic
					// Currently Mute+Suppress true -> Either of them false
					// results
					// in canSpeak = true
					if (us.hasMute()) {
						canSpeak = (codec != CODEC_NOCODEC) && !us.getMute();
					}
					if (us.hasSuppress()) {
						canSpeak = (codec != CODEC_NOCODEC)
								&& !us.getSuppress();
					}
				}

				currentUserUpdated = true;
			}

			if (channelUpdated) {
				host.channelUpdated(user.getChannel());
				if (oldChannel != null) {
					host.channelUpdated(oldChannel);
				}
			}

			if (added) {
				host.userAdded(user);
				Globals.logDebug(this,
						"MumbelProtocol userIsAdded:" + user.toString());
			} else {
				if (userStateChanged) {
					host.userStateUpdated(user);
				} else {
					host.userUpdated(user);
				}
				Globals.logDebug(this,
						"MumbelProtocol userIsUpdated:" + user.toString());
			}

			if (currentUserUpdated && channelUpdated) {
				currentChannel = user.getChannel();
				host.currentChannelChanged();
			}

			if (currentUserUpdated) {
				host.currentUserUpdated();
			}

			break;
		case UserRemove:
			final UserRemove ur = UserRemove.parseFrom(buffer);
			user = findUser(ur.getSession());
			RRUser removedUser = users.remove(user.session);
			Globals.logDebug(this,
					"MumbelProtocol UserRemove:" + ur.getSession()
							+ " getActor() " + ur.getActor() + " HAS BAN? "
							+ ur.hasBan() + " getReason " + ur.getReason());
			// Remove the user from the channel as well.
			Integer channelKey = removedUser.getChannel().id;
			RRChannels newChannel = channels.get(channelKey);
			newChannel.userCount--;
			channels.put(channelKey, newChannel);

			host.channelUpdated(newChannel);
			host.userRemoved(user.session);
			break;
		case TextMessage:
			Globals.logDebug(this, "MumbelProtocol TextMessage");
			handleTextMessage(TextMessage.parseFrom(buffer));
			break;
		case CryptSetup:
			final CryptSetup cryptsetup = CryptSetup.parseFrom(buffer);

			Globals.logDebug(this, "RadioRuntConnection: CryptSetup");

			if (cryptsetup.hasKey() && cryptsetup.hasClientNonce()
					&& cryptsetup.hasServerNonce()) {
				// Full key setup
				conn.cryptState.setKeys(cryptsetup.getKey().toByteArray(),
						cryptsetup.getClientNonce().toByteArray(), cryptsetup
								.getServerNonce().toByteArray());
			} else if (cryptsetup.hasServerNonce()) {
				// Server syncing its nonce to us.
				Globals.logDebug(this,
						"RadioRuntConnection: Server sending nonce");
				conn.cryptState.setServerNonce(cryptsetup.getServerNonce()
						.toByteArray());
			} else {
				// Server wants our nonce.
				Globals.logDebug(this,
						"RadioRuntConnection: Server requesting nonce");
				final CryptSetup.Builder nonceBuilder = CryptSetup.newBuilder();
				nonceBuilder.setClientNonce(ByteString.copyFrom(conn.cryptState
						.getClientNonce()));
				conn.sendTcpMessage(MessageType.CryptSetup, nonceBuilder);
			}
			break;
		case PermissionQuery:
			PermissionQuery permissionQuery = null;
			permissionQuery = permissionQuery.parseFrom(buffer);
			break;
		case PermissionDenied:
			PermissionDenied permissionDenied = null;
			permissionDenied = permissionDenied.parseFrom(buffer);
			int chId = permissionDenied.getChannelId();
			String name = permissionDenied.getName();
			int pmd = permissionDenied.getPermission();
			String reason = permissionDenied.getReason();
			int sess = permissionDenied.getSession();
			DenyType dtype = permissionDenied.getType();
			int numDT = dtype.getNumber();
			String nameDT = dtype.name();
			break;
		case ServerConfig:
			ServerConfig srvConf = ServerConfig.parseFrom(buffer);
			break;
		case Version:
			Version version = Version.parseFrom(buffer);
			String os = version.getOs();
			String osV = version.getOsVersion();
			version.getRelease();
			break;
		case Authenticate:
			Authenticate auth = Authenticate.parseFrom(buffer);
			String pass = auth.getPassword();
			String username = auth.getUsername();
			Boolean hasPass = auth.hasPassword();
			Boolean hasU = auth.hasUsername();
			break;
		case UserStats:
			Globals.logDebug(this, "MumbelProtocol UserStats");
			UserStats uStt = UserStats.parseFrom(buffer);
			List<ByteString> list1;
			list1 = uStt.getCertificatesList();
			iddleSec = uStt.getIdlesecs();
			Globals.logDebug(this, "IDDLE SEC " + iddleSec);
			int ses = uStt.getSession();
			int cC = uStt.getCertificatesCount();
			break;
		default:
			Globals.logWarn(this, "unhandled message type " + t);
		}
	}

	// }

	public void processUdp(final byte[] buffer, final int length) {
		if (stopped) {
			return;
		}

		final int type = buffer[0] >> 5 & 0x7;
		if (type == UDPMESSAGETYPE_UDPPING) {
			final long timestamp = ((long) (buffer[1] & 0xFF) << 56)
					| ((long) (buffer[2] & 0xFF) << 48)
					| ((long) (buffer[3] & 0xFF) << 40)
					| ((long) (buffer[4] & 0xFF) << 32)
					| ((long) (buffer[5] & 0xFF) << 24)
					| ((long) (buffer[6] & 0xFF) << 16)
					| ((long) (buffer[7] & 0xFF) << 8) | ((buffer[8] & 0xFF));

			conn.refreshUdpLimit(timestamp + UDP_PING_TRESHOLD);
		} else {
			Globals.logDebug(this, "MumbleProtocol - Proces Voice Packet");
			processVoicePacket(buffer);
		}
	}

	public final void sendChannelTextMessage(final String message,
			final RRChannels channel) {
		final TextMessage.Builder tmb = TextMessage.newBuilder();
		tmb.addChannelId(channel.id);
		tmb.setMessage(message);
		conn.sendTcpMessage(MessageType.TextMessage, tmb);

		final RRMessages msg = new RRMessages();
		msg.timestamp = System.currentTimeMillis();
		msg.message = message;
		msg.channel = channel;
		msg.direction = RRMessages.DIRECTION_SENT;
		try {
			host.messageSent(msg);
		} catch (Exception e) {
			Globals.logError(this, "sendChannelTextMessage " + e);
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public final void sendUserTextMessage(final String message,
			final int session) {
		final TextMessage.Builder tmb = TextMessage.newBuilder();
		tmb.addSession(session);
		// tmb.setActor(reciver.id);
		tmb.setMessage(message);
		conn.sendTcpMessage(MessageType.TextMessage, tmb);

		final RRMessages msg = new RRMessages();
		msg.timestamp = System.currentTimeMillis();
		msg.message = message;
		// msg.sender = session;
		msg.direction = RRMessages.DIRECTION_SENT;
		try {
			host.messageSent(msg);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Globals.logError(this, "sendUserTextMessage " + e);
			e.printStackTrace();
		}
	}

	public void stop() {
		stopped = true;
		stopThreads();
	}

	private RRChannels findChannel(final int id) {
		return channels.get(id);
	}

	private RRUser findUser(final int session_) {
		return users.get(session_);
	}

	private void handleTextMessage(final TextMessage ts) {
		RRUser u = null;
		if (ts.hasActor()) {
			u = findUser(ts.getActor());
		}
		final RRMessages msg = new RRMessages();
		msg.timestamp = System.currentTimeMillis();
		msg.message = ts.getMessage();
		msg.actor = u;
		msg.direction = RRMessages.DIRECTION_RECEIVED;
		msg.channelIds = ts.getChannelIdCount();
		msg.treeIds = ts.getTreeIdCount();
		host.messageReceived(msg);
		Globals.logDebug(this, "Txt Message actor is: " + u);
	}

	private void processVoicePacket(final byte[] buffer) {
		final int type = buffer[0] >> 5 & 0x7;
		final int flags = buffer[0] & 0x1f;

		if (type != UDPMESSAGETYPE_UDPVOICESPEEX) {
			return;
		}

		// Don't try to decode the unsupported codec version.
		if (type != codec) {
			return;
		}

		final PacketDataStream pds = new PacketDataStream(buffer);
		// skip type / flags
		pds.skip(1);
		final long uiSession = pds.readLong();

		final RRUser u = findUser((int) uiSession);
		if (u == null) {
			Globals.logError(this, "User session " + uiSession + " not found!");

			// This might happen if user leaves while there are still UDP
			// packets
			// en route to the clients. In this case we should just ignore these
			// packets.
			return;
		}

		// Rewind the packet. Otherwise consumers are confusing to implement.
		pds.rewind();

		// Nebojsa added if-else branch if (ao != null)
		if (ao != null) {
			ao.addFrameToBuffer(u, pds, flags);
		} else {
		}
		Globals.logDebug(this, "MumbleProtocol - END Proces Voice Packet");

	}

	public void stopThreads() {
		if (ao != null) {
			ao.stop();
			try {
				audioOutputThread.join();
			} catch (final InterruptedException e) {
				Globals.logWarn(this,
						"Interrupted while waiting for audio thread to end", e);
			}
		}
		stopPingAlarm();
		// if (pingThread != null) {
		// pingThread.t.interrupt();
		// try {
		// pingThread.t.join();
		// } catch (final InterruptedException e) {
		// Globals.logError(this,
		// "Interrupted while waiting for ping thread to end",
		// e);
		// }
		// }

	}

	public void stopPingAlarm() {
		// if (pingThread != null) {
		// pingThread.mysuspend();
		// }
		if (pingAlarm != null) {
			pingAlarm.setStopPingALarm(false);
			Globals.logDebug(this, "STOP PING");
		}
		pingAlarm = null;
	}

	public void restartPingAlarm(int pingTime) {
		stopPingAlarm();
		startPingThread(pingTime);
	}

	public void startPingThread(int pingTime) {
		Globals.logDebug(this, "start Ping thread");
		/*
		 * if(pingThread == null){ pingThread = new PingThread("Ping", conn,
		 * ctx); }else if(pingThread.suspendFlag){ pingThread.myresume(); }
		 */
		if (pingAlarm == null) {
			pingAlarm = new PingAlarm(ctx, conn, pingTime * 1000);
		}
	}

	// public void setPauseMumProtocolState(boolean state){
	// pauseState = state;
	// }
	// Ivan coded start Threads to start audio output when we change audio
	// output from menu

	public void startAudioThreads() {
		if (ao != null) {
			stopAudioThreads();
		}
		ao = new AudioOutput(ctx, audioHost);
		audioOutputThread = new Thread(ao, "audio output");
		audioOutputThread.start();
	}

	public void setAudioVolume() {
		if (ao != null) {
			ao.setAudioVolume();
		}
	}

	public void stopAudioThreads() {
		if (ao != null) {
			ao.stop();
			try {
				audioOutputThread.join();
			} catch (final InterruptedException e) {
				Globals.logWarn(this,
						"Interrupted while waiting for audio thread to end", e);
			}
		}
	}

	public boolean audioOutputState() {
		return ao.audioTrackState();
	}
}
