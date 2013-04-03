package com.radiorunt.activities;

import junit.framework.Assert;

import com.radiorunt.utilities.Globals;
import com.radiorunt.services.BaseServiceObserver;
import com.radiorunt.services.IServiceObserver;
import com.radiorunt.services.RadioRuntService;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;

public class ConnectedActivityLogic {
	public interface Host {

		boolean bindService(Intent intent, ServiceConnection conn, int flags);

		IServiceObserver createServiceObserver();

		void finish();

		Context getApplicationContext();

		RadioRuntService getService();

		void onConnected();

		void onConnecting();

		void onReconnecting();

		void onDisconnected();

		void onServiceBound();

		void onSynchronizing();

		void setService(RadioRuntService service);

		void unbindService(ServiceConnection conn);
	}

	class ConnectedServiceObserver extends BaseServiceObserver {
		@Override
		public void onConnectionStateChanged(final int state)
				throws RemoteException {
			connectionStateUpdated(state);
		}
	}

	ServiceConnection mServiceConn = new ServiceConnection() {

		public void onServiceConnected(final ComponentName className,
				final IBinder binder) {
			if (paused) {
				// Don't bother doing anything if the activity is already
				// paused.
				return;
			}

			final RadioRuntService service = ((RadioRuntService.LocalBinder) binder)
					.getService();
			mHost.setService(service);

			mInternalObserver = new ConnectedServiceObserver();
			service.registerObserver(mInternalObserver);

			if (mObserver == null) {
				mObserver = mHost.createServiceObserver();
				if (mObserver != null) {
					service.registerObserver(mObserver);
				}
			}

			mHost.onServiceBound();
			connectionStateUpdated(service.getConnectionState());
		}

		public void onServiceDisconnected(final ComponentName arg0) {
			mHost.setService(null);

		}
	};

	private IServiceObserver mInternalObserver;
	private final Host mHost;
	private boolean paused = false;

	protected IServiceObserver mObserver;

	public ConnectedActivityLogic(final Host host) {
		this.mHost = host;
	}

	public void onPause() {
		paused = true;

		if (mInternalObserver != null) {
			mHost.getService().unregisterObserver(mInternalObserver);
			mInternalObserver = null;
		}

		if (mObserver != null) {
			mHost.getService().unregisterObserver(mObserver);
			mObserver = null;
		}

		mHost.unbindService(mServiceConn);
	}

	public void onResume() {
		paused = false;
		bindService();

	}

	public void bindService() {
		paused = false;
		final Intent intent = new Intent(mHost.getApplicationContext(),
				RadioRuntService.class);
		mHost.bindService(intent, mServiceConn, Context.BIND_AUTO_CREATE);
	}

	private void connectionStateUpdated(final int state) {
		switch (state) {
		case RadioRuntService.CONNECTION_STATE_CONNECTING:
			Globals.logDebug(this, "RadioRuntService.STATE_CONNECTING");
			mHost.onConnecting();
			break;
		case RadioRuntService.CONNECTION_STATE_SYNCHRONIZING:
			Globals.logInfo(this, "Synchronizing");
			mHost.onSynchronizing();
			break;
		case RadioRuntService.CONNECTION_STATE_CONNECTED:
			Globals.logDebug(this, "RadioRuntService.STATE_CONNECTED 1");
			mHost.onConnected();
			break;
		case RadioRuntService.CONNECTION_STATE_RECONNECTING:
			Globals.logDebug(this, "RadioRuntService.STATE_RECONNECTING");
			mHost.onReconnecting();
			break;
		case RadioRuntService.CONNECTION_STATE_DISCONNECTED:
			Globals.logDebug(this, "RadioRuntService.STATE_DISCONNECTED 1");
			mHost.onDisconnected();
			break;
		default:
			Assert.fail("Unknown connection state");
		}
	}
}
