package com.radiorunt.services;

/**
 * Callback interface for Connection to communicate back to the service.
 * 
 */
public interface RadioRuntConnectionHost {
	public final static int STATE_DISCONNECTED = 0;
	public final static int STATE_CONNECTING = 1;
	public final static int STATE_CONNECTED = 2;
	public final static int STATE_RECONNECTING = 3;

	public void setConnectionState(int state);

	public void setError(final String error);
}
