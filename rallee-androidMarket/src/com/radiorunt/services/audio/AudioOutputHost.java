package com.radiorunt.services.audio;

import com.radiorunt.businessobjects.RRUser;

public interface AudioOutputHost {
	public static final int STATE_PASSIVE = 0;
	public static final int STATE_TALKING = 1;

	public void setTalkState(RRUser user, int talkState);
}
