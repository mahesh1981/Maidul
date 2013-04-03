package com.radiorunt.services;

import com.radiorunt.businessobjects.RRChannels;
import com.radiorunt.businessobjects.RRMessages;
import com.radiorunt.businessobjects.RRUser;

import android.os.IBinder;
import android.os.RemoteException;

public class BaseServiceObserver implements IServiceObserver {
	@Override
	public IBinder asBinder() {
		return null;
	}

	@Override
	public void onChannelAdded(final RRChannels channel) throws RemoteException {
	}

	@Override
	public void onChannelRemoved(final RRChannels channel)
			throws RemoteException {
	}

	@Override
	public void onChannelUpdated(final RRChannels channel)
			throws RemoteException {
	}

	@Override
	public void onConnectionStateChanged(final int state)
			throws RemoteException {
	}

	@Override
	public void onCurrentChannelChanged() throws RemoteException {
	}

	@Override
	public void onCurrentUserUpdated() throws RemoteException {
	}

	@Override
	public void onMessageReceived(final RRMessages msg) throws RemoteException {
	}

	@Override
	public void onMessageSent(final RRMessages msg) throws RemoteException {
	}

	@Override
	public void onUserAdded(final RRUser user) throws RemoteException {
	}

	@Override
	public void onUserRemoved(final RRUser user) throws RemoteException {
	}

	@Override
	public void onUserUpdated(final RRUser user) throws RemoteException {
	}

	@Override
	public void onUserTalkStateUpdated(RRUser user) throws RemoteException {
		// TODO Auto-generated method stub

	}
}
