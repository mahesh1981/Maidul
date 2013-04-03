package com.radiorunt.activities;

import com.radiorunt.utilities.Globals;
import com.radiorunt.activities.ConnectedActivityLogic.Host;
import com.radiorunt.services.IServiceObserver;
import com.radiorunt.services.RadioRuntService;
import com.radiorunt.services.TtsProvider;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.view.Window;
import android.widget.Toast;

/**
 * Base class for activities that want to access the RadioRuntService
 * 
 * Note: Remember to consider ConnectedListActivity when modifying this class.
 * 
 */
public class ConnectedActivity extends Activity {
	private final Host logicHost = new Host() {
		@Override
		public boolean bindService(final Intent intent,
				final ServiceConnection mServiceConn, final int bindAutoCreate) {
			return ConnectedActivity.this.bindService(intent, mServiceConn,
					bindAutoCreate);
		}

		@Override
		public IServiceObserver createServiceObserver() {
			return ConnectedActivity.this.createServiceObserver();
		}

		@Override
		public void finish() {
			ConnectedActivity.this.finish();
		}

		@Override
		public Context getApplicationContext() {
			return ConnectedActivity.this.getApplicationContext();
		}

		@Override
		public RadioRuntService getService() {
			return mService;
		}

		@Override
		public void onConnected() {
			ConnectedActivity.this.onConnected();
		}

		@Override
		public void onConnecting() {
			ConnectedActivity.this.onConnecting();
		}

		@Override
		public void onReconnecting() {
			ConnectedActivity.this.onReconnecting();
			Globals.logInfo(ConnectedActivity.this, "Reconnecting Rallee");
		}

		@Override
		public void onDisconnected() {
			ConnectedActivity.this.onDisconnected();
		}

		@Override
		public void onServiceBound() {
			ConnectedActivity.this.onServiceBound();
		}

		@Override
		public void onSynchronizing() {
			ConnectedActivity.this.onSynchronizing();
		}

		@Override
		public void setService(final RadioRuntService service) {
			Globals.logInfo(ConnectedActivity.this, "Service set");
			mService = service;
		}

		@Override
		public void unbindService(final ServiceConnection mServiceConn) {
			ConnectedActivity.this.unbindService(mServiceConn);
		}
	};

	private final ConnectedActivityLogic logic = new ConnectedActivityLogic(
			logicHost);

	protected RadioRuntService mService;
	protected IServiceObserver mObserver;

	protected IServiceObserver createServiceObserver() {
		return null;
	}

	protected void onConnected() {
	}

	protected void onConnecting() {
	}

	protected void onReconnecting() {
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		Globals.logInfo(this, "onCreate()");
	}

	protected void onDisconnected() {
		final String error = mService.getError();
		if (error != null) {
			// Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
			// TtsProvider.speak(error, false);
		}
		finish();
	}

	@Override
	protected void onPause() {
		super.onPause();
		Globals.logInfo(this, "onPause()");
		logic.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
		Globals.logInfo(this, "onResume()");
		logic.onResume();
	}

	protected void onServiceBound() {
	}

	protected void onSynchronizing() {
	}
}
