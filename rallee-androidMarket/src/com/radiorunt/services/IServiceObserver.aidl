package com.radiorunt.services;

import com.radiorunt.businessobjects.RRUser;
import com.radiorunt.businessobjects.RRMessages;
import com.radiorunt.businessobjects.RRChannels;

interface IServiceObserver {
	void onChannelAdded(in RRChannels channel);
	void onChannelRemoved(in RRChannels channel);
	void onChannelUpdated(in RRChannels channel);

	void onCurrentChannelChanged();
	
	void onCurrentUserUpdated();
	
	void onUserAdded(in RRUser user);
	void onUserRemoved(in RRUser user);
	void onUserUpdated(in RRUser user);
	void onUserTalkStateUpdated(in RRUser user);
	
	void onMessageReceived(in RRMessages msg);
	void onMessageSent(in RRMessages msg);
	
	/**
	 * Called when the connection state changes.
	 */
	void onConnectionStateChanged(int state);
}
