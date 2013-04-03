package com.radiorunt.activities;

import com.facebook.UiLifecycleHelper;
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
import android.support.v4.app.FragmentActivity;
import android.view.Window;
import android.widget.Toast;

/**
 * Base class for activities that want to access the RadioRuntService
 * 
 * Note: Remember to consider ConnectedListActivity when modifying this class.
 * 
 */

public class ConnectedFragmentActivity extends FragmentActivity {
	public UiLifecycleHelper uiHelper;
	private final Host logicHost = new Host() {
		@Override
		public boolean bindService(final Intent intent,
				final ServiceConnection mServiceConn, final int bindAutoCreate) {
			return ConnectedFragmentActivity.this.bindService(intent,
					mServiceConn, bindAutoCreate);
		}

		@Override
		public IServiceObserver createServiceObserver() {
			return ConnectedFragmentActivity.this.createServiceObserver();
		}

		@Override
		public void finish() {
			ConnectedFragmentActivity.this.finish();
		}

		@Override
		public Context getApplicationContext() {
			return ConnectedFragmentActivity.this.getApplicationContext();
		}

		@Override
		public RadioRuntService getService() {
			return mService;
		}

		@Override
		public void onConnected() {
			ConnectedFragmentActivity.this.onConnected();
		}

		@Override
		public void onConnecting() {
			ConnectedFragmentActivity.this.onConnecting();
		}

		@Override
		public void onReconnecting() {
			ConnectedFragmentActivity.this.onReconnecting();
			Globals.logInfo(ConnectedFragmentActivity.this,
					"Reconnecting Rallee");
		}

		@Override
		public void onDisconnected() {
			ConnectedFragmentActivity.this.onDisconnected();
		}

		@Override
		public void onServiceBound() {
			ConnectedFragmentActivity.this.onServiceBound();
		}

		@Override
		public void onSynchronizing() {
			ConnectedFragmentActivity.this.onSynchronizing();
		}

		@Override
		public void setService(final RadioRuntService service) {
			Globals.logInfo(ConnectedFragmentActivity.this, "Service set");
			mService = service;
		}

		@Override
		public void unbindService(final ServiceConnection mServiceConn) {
			ConnectedFragmentActivity.this.unbindService(mServiceConn);
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
	protected void onSaveInstanceState(Bundle outState) {
		uiHelper.onSaveInstanceState(outState);
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		uiHelper.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		// uiHelper.onDestroy();
	}

	@Override
	protected void onPause() {
		super.onPause();
		Globals.logInfo(this, "onPause()");
		logic.onPause();
		uiHelper.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
		Globals.logInfo(this, "onResume()");
		logic.onResume();
		uiHelper.onResume();
	}

	protected void onServiceBound() {
	}

	protected void onSynchronizing() {
	}
}
