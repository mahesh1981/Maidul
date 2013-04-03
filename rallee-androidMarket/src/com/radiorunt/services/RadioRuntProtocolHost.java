package com.radiorunt.services;

import com.radiorunt.businessobjects.RRChannels;
import com.radiorunt.businessobjects.RRMessages;
import com.radiorunt.businessobjects.RRUser;

/**
 * Callback interface for Protocol to communicate back to the Service
 * 
 */
public interface RadioRuntProtocolHost {
	public void channelAdded(RRChannels channel);

	public void channelRemoved(int channelId);

	public void channelUpdated(RRChannels channel);

	public void currentChannelChanged();

	public void currentUserUpdated();

	public void messageReceived(RRMessages msg);

	public void messageSent(RRMessages msg);

	public void setError(String error);

	public void setSynchronized(boolean synced);

	public void userAdded(RRUser user);

	public void userRemoved(int userId);

	public void userUpdated(RRUser user);

	public void userStateUpdated(RRUser user);
}
