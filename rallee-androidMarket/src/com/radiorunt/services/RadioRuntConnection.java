package com.radiorunt.services;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.TimeZone;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

import junit.framework.Assert;
import net.sf.mumble.MumbleProto.Authenticate;
import net.sf.mumble.MumbleProto.Version;

import android.content.Context;
import android.util.Log;

import com.radiorunt.R;
import com.radiorunt.utilities.Globals;
import com.radiorunt.utilities.RalleeApp;
import com.radiorunt.activities.HomeActivity;
import com.radiorunt.activities.LogInActivity;
import com.radiorunt.facebook.Utility;
import com.radiorunt.services.MumbleProtocol.MessageType;

import com.google.protobuf.MessageLite;

/**
 * Maintains connection to the server and implements the low level communication
 * protocol.
 * 
 * This class should support calls from both the main application thread and its
 * own connection thread. As a result the disconnecting state is quite
 * complicated. Disconnect can basically happen at any point as the server might
 * cut the connection due to restart or kick.
 * 
 * When disconnection happens, the connection reports "Disconnected" state and
 * stops handling incoming or outgoing messages. Since at this point some of the
 * sockets might already be closed there is no use waiting on Disconnected
 * reporting until all the other threads, such as PingThread or RecordThread
 * have been stopped.
 * 
 */
public class RadioRuntConnection implements Runnable {
	/**
	 * Socket reader for the TCP socket. Interprets the Mumble TCP envelope and
	 * extracts the data inside.
	 * 
	 */
	class TcpSocketReader extends RadioRuntSocketReader {
		private byte[] msg = null;

		public TcpSocketReader(final Object monitor) {
			super(monitor, "TcpReader", context);
		}

		@Override
		public boolean isRunning() {
			return !disconnecting && super.isRunning();
		}

		@Override
		public void stop() {
			try {
				tcpSocket.close();
			} catch (final IOException e) {
				Globals.logError(this, "Error when closing tcp socket", e);
			}
			super.stop();
		}

		@Override
		protected void process() throws IOException {
			// Log.i("radiorunt", "tcp process start");
			// if(!RadioRuntService.tempNetState){
			// Log.i("radiorunt", "tcp process tempNetState tcp end");
			// return;
			// }
			final short type = in.readShort();
			final int length = in.readInt();
			Globals.logDebug(this, "tcp process type:" + type + " length:"
					+ length);

			if (msg == null || msg.length != length) {
				msg = new byte[length];
			}
			in.readFully(msg);
			Globals.logDebug(this, "tcp process type:" + type + " length:"
					+ length);

			try {
				protocol.processTcp(type, msg);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				Globals.logError(this, "RRConn processTcp error  ", e);
			}
			// Log.i("radiorunt", "tcp process end");
		}
	};

	/**
	 * Socket reader for the UDP socket. Decrypts the data from the raw UDP
	 * packages.
	 * 
	 */
	class UdpSocketReader extends RadioRuntSocketReader {
		private final DatagramPacket packet = new DatagramPacket(
				new byte[UDP_BUFFER_SIZE], UDP_BUFFER_SIZE);

		public UdpSocketReader(final Object monitor) {
			super(monitor, "UdpReader", context);
		}

		@Override
		public boolean isRunning() {
			return !disconnecting && super.isRunning();
		}

		@Override
		public void stop() {
			udpSocket.close();
			super.stop();
		}

		@Override
		protected void process() throws IOException {
			// Log.i("radiorunt", "udp start");
			udpSocket.receive(packet);

			final byte[] buffer = cryptState.decrypt(packet.getData(),
					packet.getLength());

			// Decrypt might return null if the buffer was total garbage.
			if (buffer == null) {
				return;
			}

			protocol.processUdp(buffer, buffer.length);
			// Log.i("radiorunt", "udp end");
		}
	};

	public static final int UDP_BUFFER_SIZE = 2048;
	public static int CONNECTION_ERROR = 0;

	private final RadioRuntConnectionHost connectionHost;
	private MumbleProtocol protocol;

	private Socket tcpSocket;
	private DataInputStream in;
	private DataOutputStream out;
	private DatagramSocket udpSocket;
	private long useUdpUntil;
	boolean usingUdp = false;

	/**
	 * Signals disconnecting state. True if something has interrupted the normal
	 * operation of the MumbleConnection thread and it should stop.
	 */
	private volatile boolean disconnecting = false;

	/**
	 * Signals whether connection terminating errors should be suspected. Mainly
	 * used to suppress some IO/Interruption errors that occur as a result of
	 * stopping the MumbleConnection after calling disconnect()
	 */
	private volatile boolean suppressErrors = false;

	private InetAddress hostAddress;
	private final String host;
	private final int port;
	private final String username;
	private final String password;
	private final Context context;
	private final int connectionState;

	private final Object stateLock = new Object();
	final CryptState cryptState = new CryptState();

	/**
	 * Constructor for new connection thread.
	 * 
	 * This thread should be started shortly after construction. Construction
	 * sets the connection state for the host to "Connecting" even if the actual
	 * connection won't be attempted until the thread has been started.
	 * 
	 * This is to combat an issue where the Service is asked to connect and the
	 * thread is started but the thread isn't given execution time before
	 * another activity checks for connection state and finds out the service is
	 * in Disconnected state.
	 * 
	 * @param connectionHost
	 *            Host interface for this Connection
	 * @param host
	 *            Mumble server host address
	 * @param port
	 *            Mumble server port
	 * @param username
	 *            Username
	 * @param password
	 *            Server password
	 */
	public RadioRuntConnection(final RadioRuntConnectionHost connectionHost,
			final String host, final int port, final String username,
			final String password, final Context context,
			final int connectionState) {
		this.connectionHost = connectionHost;
		this.host = host;
		this.port = port;
		this.username = username;
		this.password = password;
		this.context = context;
		this.connectionState = connectionState;

		connectionHost.setConnectionState(connectionState);
	}

	public final void disconnect() {
		synchronized (stateLock) {
			if (disconnecting == true && RadioRuntService.SWITCH_CHANNEL != 99) {
				return;
			}

			Globals.logInfo(this, "disconnect()");
			disconnecting = true;
			suppressErrors = true;

			// Close sockets to interrupt the reader threads. We don't need to
			// be completely certain that they won't be re-opened by another
			// thread as the connection thread will close them anyway. This is
			// just to interrupt the reader threads.
			try {
				if (tcpSocket != null) {
					tcpSocket.close();
				}
			} catch (final IOException e) {
				Globals.logError(this, "Error disconnecting TCP socket", e);
			}
			if (udpSocket != null) {
				udpSocket.close();
			}

			connectionHost
					.setConnectionState(RadioRuntConnectionHost.STATE_DISCONNECTED);
			Globals.logDebug(this,
					"RadioRuntConnectionHost.STATE_DISCONNECTED 1");
			stateLock.notifyAll();
		}
	}

	public final void disconnectInternal() {
		synchronized (stateLock) {
			if (disconnecting == true) {
				return;
			}

			Globals.logInfo(this, "disconnectInternal()");
			disconnecting = true;
			suppressErrors = true;
			// RadioRuntService.tcpTimeOuted = false;
			// Close sockets to interrupt the reader threads. We don't need to
			// be completely certain that they won't be re-opened by another
			// thread as the connection thread will close them anyway. This is
			// just to interrupt the reader threads.
			try {
				if (tcpSocket != null) {
					tcpSocket.close();
				}
			} catch (final IOException e) {
				Globals.logError(this, "Error disconnecting TCP socket", e);
			}
			if (udpSocket != null) {
				udpSocket.close();
			}

			connectionHost
					.setConnectionState(RadioRuntConnectionHost.STATE_CONNECTING);
			Globals.logDebug(this,
					"RadioRuntConnectionHost.STATE_CONNECTING 10");
			stateLock.notifyAll();
		}
	}

	public final boolean isConnectionAlive() {
		return !disconnecting && udpSocket != null && tcpSocket != null
				&& !tcpSocket.isClosed() && tcpSocket.isConnected()
				&& !udpSocket.isClosed();
	}

	// public final boolean isSameServer(
	// final String host_,
	// final int port_,
	// final String username_,
	// final String password_) {
	// return host.equals(host_) && port == port_ &&
	// username.equals(username_) && password.equals(password_);
	// }

	public void refreshUdpLimit(final long limit) {
		useUdpUntil = limit;
	}

	@Override
	public final void run() {
		Assert.assertNotNull(protocol);
		if (disconnecting) {
			return;
		}
		boolean connected = false;
		try {
			try {
				Globals.logInfo(this, String.format("Connecting to Ralle"/*
																		 * "Connecting to host \"%s\", port %s"
																		 * ,
																		 * host,
																		 * port
																		 */));

				this.hostAddress = InetAddress.getByName(host);
				tcpSocket = connectTcp();
				udpSocket = connectUdp();
				connected = true;
				RadioRuntService.tcpTimeOuted = false;
				CONNECTION_ERROR = 0;
			} catch (final UnknownHostException e) {
				if (!RadioRuntService.tempNetState
						|| RadioRuntService.SWITCH_CHANNEL == 99) {
					return;
				}
				Globals.logError(this, "UnknownHostException " + e);
				final String errorString = String.format("Host \"%s\" unknown",
						host);
				reportError(errorString, e);
				clearStateFlags();
				CONNECTION_ERROR = 1;
				// Intent intent = new Intent(context,LogInActivity.class);
				// intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				// context.startActivity(intent);
			} catch (final ConnectException e) {
				// if(!RadioRuntService.tempNetState ||
				// RadioRuntService.SWITCH_CHANNEL == 99){
				// return;
				// }
				Globals.logError(this, "ConnectException " + e);
				final String errorString = "The host refused connection";
				reportError(errorString, e);
				clearStateFlags();
				CONNECTION_ERROR = 2;
				connectionHost
						.setConnectionState(RadioRuntConnectionHost.STATE_RECONNECTING);
				// Intent intent = new Intent(context,LogInActivity.class);
				// intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				// context.startActivity(intent);
			} catch (final KeyManagementException e) {
				if (!RadioRuntService.tempNetState
						|| RadioRuntService.SWITCH_CHANNEL == 99) {
					return;
				}
				Globals.logError(this, "KeyManagementException " + e);
				reportError(String.format("Could not connect to Rallee"), e);// \"%s:%s\""),
																				// e);
				clearStateFlags();
				CONNECTION_ERROR = 3;
				// Intent intent = new Intent(context,LogInActivity.class);
				// intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				// context.startActivity(intent);
			} catch (final NoSuchAlgorithmException e) {
				if (!RadioRuntService.tempNetState
						|| RadioRuntService.SWITCH_CHANNEL == 99) {
					return;
				}
				Globals.logError(this, "NoSuchAlgorithmException " + e);
				reportError(String.format("Could not connect to Rallee"), e);// \"%s:%s\""),
																				// e);
				clearStateFlags();
				CONNECTION_ERROR = 4;
				// Intent intent = new Intent(context,LogInActivity.class);
				// intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				// context.startActivity(intent);
			} catch (final IOException e) {
				// if(!RadioRuntService.tempNetState ||
				// RadioRuntService.SWITCH_CHANNEL == 99){
				// return;
				// }
				Globals.logError(
						this,
						"IOException " + "\n getLocalizedMessage"
								+ e.getLocalizedMessage() + "\n getCause "
								+ e.getCause() + "\n getMessage "
								+ e.getMessage());
				reportError(String.format("Could not connect to Rallee"), e);// \"%s:%s\""),
																				// e);
				clearStateFlags();
				CONNECTION_ERROR = 5;
				// Intent intent = new Intent(context,LogInActivity.class);
				// intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				// context.startActivity(intent);
			} catch (KeyStoreException e) {
				if (!RadioRuntService.tempNetState
						|| RadioRuntService.SWITCH_CHANNEL == 99) {
					return;
				}
				Globals.logError(this, "KeyStoreException " + e);
				// TODO Auto-generated catch block
				e.printStackTrace();
				clearStateFlags();
				CONNECTION_ERROR = 6;
				// Intent intent = new Intent(context,LogInActivity.class);
				// intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				// context.startActivity(intent);
			} catch (UnrecoverableKeyException e) {
				if (!RadioRuntService.tempNetState
						|| RadioRuntService.SWITCH_CHANNEL == 99) {
					return;
				}
				Globals.logError(this, "UnrecoverableKeyException " + e);
				// TODO Auto-generated catch block
				e.printStackTrace();
				clearStateFlags();
				CONNECTION_ERROR = 7;
				// Intent intent = new Intent(context,LogInActivity.class);
				// intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				// context.startActivity(intent);
			} catch (CertificateException e) {
				if (!RadioRuntService.tempNetState
						|| RadioRuntService.SWITCH_CHANNEL == 99) {
					return;
				}
				Globals.logError(this, "CertificateException " + e);
				// TODO Auto-generated catch block
				e.printStackTrace();
				clearStateFlags();
				CONNECTION_ERROR = 8;
				// Intent intent = new Intent(context,LogInActivity.class);
				// intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				// context.startActivity(intent);
			} catch (NoSuchProviderException e) {
				if (!RadioRuntService.tempNetState
						|| RadioRuntService.SWITCH_CHANNEL == 99) {
					return;
				}
				Globals.logError(this, "NoSuchProviderException " + e);
				// TODO Auto-generated catch block
				e.printStackTrace();
				clearStateFlags();
				CONNECTION_ERROR = 9;
				// Intent intent = new Intent(context,LogInActivity.class);
				// intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				// context.startActivity(intent);
			}

			// If we couldn't finish connecting, return.
			if (!connected) {
				LogInActivity.firstConnectionToServerDone = false;
				return;
			}

			synchronized (stateLock) {
				if (disconnecting) {
					return;
				}
				connectionHost
						.setConnectionState(RadioRuntConnectionHost.STATE_CONNECTED);
				// Utility.timestampOfConnectedState = new
				// Timestamp(Calendar.getInstance(TimeZone.getTimeZone("GMT"))
				// .getTimeInMillis());

			}

			try {
				handleProtocol();
			} catch (final IOException e) {
				final String errorString = String.format("Connection lost",
						host);
				reportError(errorString, e);
			} catch (final InterruptedException e) {
				final String errorString = String.format("Connection lost",
						host);
				reportError(errorString, e);
			}
		} finally {
			synchronized (stateLock) {
				// Don't re-update state in case we are already disconnecting.
				// ANOTHER RalleeConnection thread might have already set state
				// to connected and this confuses everything.
				Globals.logDebug(this, "CONNECTION_ERROR: " + CONNECTION_ERROR);
				if (!disconnecting && RadioRuntService.DISCONNECT_FROM_RALLEE
						&& CONNECTION_ERROR == 0) {
					// if(RadioRuntService.tcpTimeOuted){
					//
					// }else{
					disconnecting = true;
					connectionHost
							.setConnectionState(RadioRuntConnectionHost.STATE_DISCONNECTED);
					Globals.logDebug(this,
							"RadioRuntConnectionHost.STATE_DISCONNECTED 2");
					// }
				} else if ((RadioRuntService.tcpTimeOuted && RadioRuntConnection.CONNECTION_ERROR == 0)
						|| (RadioRuntConnection.CONNECTION_ERROR == 5)) {
					connectionHost
							.setConnectionState(RadioRuntConnectionHost.STATE_RECONNECTING);
					Globals.logDebug(this,
							"RadioRuntConnectionHost.STATE_RECONNECTING 2");
				}
			}

			cleanConnection();
		}
	}

	private void clearStateFlags() {
		// ////// delete user from online user in db on mw //////////
		// RRUser me = new RRUser();
		// me = protocol.currentUser;
		// if(me!=null){
		// me.isOnline = false;
		// ObjectMapper mapper = new ObjectMapper();
		// String rruser = "";
		// try {
		// rruser = mapper.writeValueAsString(me);
		// } catch (JsonGenerationException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// } catch (JsonMappingException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// } catch (IOException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		// RRServerProxyHelper.startSetOnlineUserService(
		// context, rruser);
		// }
		//
		// ////////////////////////////////////////////////////////////

		// HomeActivity.PREF_CHANGED_ACTIVITY = 0;
		// HomeActivity.ACTIVITY_PAUSED = 0;
		HomeActivity.CONNECTED = 0;
		// HomeActivity.ROOT_ID = 0;
		// HomeActivity.CURRENT_CH_ID = 0;
		// HomeActivity.CURRENT_CH_NAME = "";
		//
		// HomeActivity.BAR_MODE = 0;
		// HomeActivity.currentCallState = HomeActivity.CALL_STATE_NORMAL;
		// HomeActivity.WAS_ON_CHANNEL_LIST = 0;
		// HomeActivity.WAS_ON_FEEDBACK_ACTIVITY = 0;
		// HomeActivity.SWITCH_SERVER = 0;

		// Utility.calledUserUsername = "";
		// Utility.channelName = "";
		// Utility.senderName = "";
		// Utility.senderPicUrl = "";
		// Utility.senderFBId = "";
		// Utility.callTimestamp = 0;
		//
		// Utility.userUID = null;
		// Utility.userName = null;
		// Utility.firstName = null;
		// Utility.connectionStatus = false;
		// Utility.picUrl = null;
		// Utility.objectID = null;
		// Utility.calledUserFirstName = null;
	}

	/**
	 * Sends TCP message. As it is impossible to predict the socket state this
	 * method must be exception safe. If the sockets have gone stale it reports
	 * error and initiates connection shutdown.
	 * 
	 * @param t
	 *            Message type
	 * @param b
	 *            Protocol Buffer message builder
	 */
	public final void sendTcpMessage(final MessageType t,
			final MessageLite.Builder b) {
		final MessageLite m = b.build();
		final short type = (short) t.ordinal();
		final int length = m.getSerializedSize();
		Globals.logDebug(this, "sendTcpMessage TCP 1");

		if (disconnecting && RadioRuntService.DISCONNECT_FROM_RALLEE) {
			return;
		}
		Globals.logDebug(this, "sendTcpMessage TCP 2");

		try {
			synchronized (out) {
				out.writeShort(type);
				out.writeInt(length);
				m.writeTo(out);
				Globals.logDebug(this, "sendTcpMessage TCP 3");
			}
		} catch (final IOException e) {
			handleSendingException(e);
		}

		if (t != MessageType.Ping) {
			Globals.logDebug(this, "<<< " + t);
		}
	}

	/**
	 * Sends UDP message. See sendTcpMessage for additional information
	 * concerning exceptions.
	 * 
	 * @param buffer
	 *            Udp message buffer
	 * @param length
	 *            Message length
	 * @param forceUdp
	 *            True if the message should never be tunneled through TCP
	 */
	public final void sendUdpMessage(final byte[] buffer, final int length,
			final boolean forceUdp) {
		// FIXME: This would break things because we don't handle nonce resync
		// messages
		// if (!cryptState.isInitialized()) {
		// return;
		// }
		Globals.logDebug(this, "sendUdpMessage UDP 1");

		if (forceUdp || useUdpUntil > System.currentTimeMillis()) {
			Globals.logDebug(this, "sendUdpMessage UDP 2");

			if (!usingUdp && !forceUdp) {
				Globals.logInfo(this, "UDP enabled");
				usingUdp = true;
				Globals.logDebug(this, "sendUdpMessage UDP 3");
			}

			final byte[] encryptedBuffer = cryptState.encrypt(buffer, length);
			final DatagramPacket outPacket = new DatagramPacket(
					encryptedBuffer, encryptedBuffer.length);

			outPacket.setAddress(hostAddress);
			outPacket.setPort(port);
			Globals.logDebug(this, "sendUdpMessage UDP 4");
			if (disconnecting && RadioRuntService.DISCONNECT_FROM_RALLEE) {
				Globals.logDebug(this, "sendUdpMessage TCP 5 disconnecting");
				return;
			}

			Globals.logDebug(this, "sendUdpMessage UDP 6");

			try {
				udpSocket.send(outPacket);
				Globals.logDebug(this, "sendUdpMessage UDP 7 send");
			} catch (final IOException e) {
				handleSendingException(e);
			}
		} else {

			Globals.logDebug(this, "sendUdpMessage UDP 8");

			if (usingUdp) {
				Globals.logInfo(this, "UDP disabled");
				usingUdp = false;
				Globals.logDebug(this, "sendUdpMessage UDP 9");
			}

			final short type = (short) MessageType.UDPTunnel.ordinal();

			Globals.logDebug(this, "sendUdpMessage UDP 10");

			if (disconnecting && RadioRuntService.DISCONNECT_FROM_RALLEE) {
				Globals.logDebug(this, "sendUdpMessage TCP 11 disconnecting");
				return;
			}

			synchronized (out) {
				try {
					out.writeShort(type);
					out.writeInt(length);
					out.write(buffer, 0, length);
					Globals.logDebug(this, "sendUdpMessage TCP 12");
				} catch (final IOException e) {
					handleSendingException(e);
				}
			}
		}
	}

	public Thread start(final MumbleProtocol protocol_) {
		this.protocol = protocol_;

		final Thread t = new Thread(this, "RadioRuntConnection");
		t.start();
		return t;
	}

	private void cleanConnection() {
		// FIXME: These throw exceptions for some reason.
		// Even with the checks in place
		if (tcpSocket != null && tcpSocket.isConnected()) {
			try {
				tcpSocket.close();
			} catch (final IOException e) {
				Globals.logError(this, "IO error while closing the tcp socket",
						e);
			}
		}
		if (udpSocket != null && udpSocket.isConnected()) {
			udpSocket.close();
		}
	}

	private void handleProtocol() throws IOException, InterruptedException {
		if (disconnecting) {
			return;
		}

		out = new DataOutputStream(tcpSocket.getOutputStream());
		in = new DataInputStream(tcpSocket.getInputStream());

		final Version.Builder v = Version.newBuilder();
		v.setVersion(Globals.PROTOCOL_VERSION);
		v.setOs("Android");
		// Rallee 0.0.1 - Testing
		// Rallee 0.2 - 21.09. for Tony's Company
		v.setRelease(RalleeApp.getInstance().getRalleeRelease());

		final Authenticate.Builder a = Authenticate.newBuilder();
		a.setUsername(username);
		a.setPassword(password);
		// a.addCeltVersions(Globals.CELT_VERSION);

		sendTcpMessage(MessageType.Version, v);
		sendTcpMessage(MessageType.Authenticate, a);

		if (disconnecting) {
			return;
		}

		// Spawn one thread for each socket to allow concurrent processing.
		final RadioRuntSocketReader tcpReader = new TcpSocketReader(stateLock);
		final RadioRuntSocketReader udpReader = new UdpSocketReader(stateLock);

		tcpReader.start();
		udpReader.start();

		synchronized (stateLock) {
			while (!disconnecting && tcpReader.isRunning()
					&& udpReader.isRunning()) {
				stateLock.wait();
			}

			// Report error if we died without being in a disconnecting state.
			if (!disconnecting) {
				// reportError("Connection lost1", null);
			}

			if (!disconnecting) {
				disconnecting = true;
				// Must do somthing if error exist

			}
		}

		// Stop readers in case one of them is still running
		tcpReader.stop();
		udpReader.stop();
	}

	private boolean handleSendingException(final IOException e) {
		// If we are already disconnecting, just ignore this.
		if (disconnecting) {
			return true;
		}
		// Otherwise see if we should be disconnecting really.
		if (!isConnectionAlive()) {
			disconnect();
		} else {
			// disconnect();
			// Connection is alive but we still couldn't send message?
			reportError(
					String.format("Error while sending message: %s",
							e.getMessage()), e);
			connectionHost
					.setConnectionState(RadioRuntConnectionHost.STATE_RECONNECTING);

		}

		return false;
	}

	private void reportError(final String error, final Exception e) {
		if (suppressErrors) {
			Globals.logWarn(this, "Error while disconnecting");
			Globals.logWarn(this, error, e);
			return;
		}
		if (!RadioRuntService.tempNetState) {
			return;
		}
		connectionHost.setError(String.format(/* e.getMessage() */"error"));
		Globals.logError(this, error, e);
	}

	private SSLSocketFactory getSocketFactory() throws KeyStoreException,
			UnrecoverableKeyException, CertificateException,
			NoSuchProviderException, IOException, NoSuchAlgorithmException,
			KeyManagementException {
		final KeyManagerFactory kmf = KeyManagerFactory
				.getInstance(KeyManagerFactory.getDefaultAlgorithm());
		char[] password = null;
		KeyStore keystore = KeyStore.getInstance("BKS");
		InputStream is = context.getResources().openRawResource(
				R.raw.rrkeystore);
		// only use the keystore password if we have it
		String keystorepass = "radiorunt11!!";
		password = keystorepass.toCharArray();
		keystore.load(is, password);
		kmf.init(keystore, password);
		final SSLContext ctx_ = SSLContext.getInstance("TLS");
		KeyManager[] keyMng = kmf.getKeyManagers();
		ctx_.init(kmf.getKeyManagers(),
				new TrustManager[] { new LocalSSLTrustManager() },
				new SecureRandom());
		return ctx_.getSocketFactory();
	}

	protected Socket connectTcp() throws NoSuchAlgorithmException,
			KeyManagementException, IOException, UnknownHostException,
			KeyStoreException, UnrecoverableKeyException, CertificateException,
			NoSuchProviderException {

		final SSLSocketFactory factory = getSocketFactory();
		final SSLSocket sslSocket = (SSLSocket) factory.createSocket(
				hostAddress, port);
		sslSocket.setUseClientMode(true);
		sslSocket.setKeepAlive(true);
		// sslSocket.setSoTimeout(120000);
		sslSocket.setEnabledProtocols(new String[] { "TLSv1" });
		sslSocket.startHandshake();

		Globals.logInfo(this, "TCP/SSL socket opened");

		return sslSocket;
	}

	protected DatagramSocket connectUdp() throws SocketException,
			UnknownHostException {
		udpSocket = new DatagramSocket();
		udpSocket.connect(hostAddress, port);

		Globals.logInfo(this, "UDP Socket opened");

		return udpSocket;
	}
}
