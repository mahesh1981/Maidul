package com.radiorunt.businessobjects;

import java.util.ArrayList;

public class RRPushFilteringMessagePayload {
	public String sender;
	public String channelName;
	public String payloadType;
	public long timestamp;
	public ArrayList<String> listOfFriends;
	public ArrayList<String> listOfChannels;
}
