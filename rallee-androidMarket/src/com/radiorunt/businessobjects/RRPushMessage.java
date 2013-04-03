package com.radiorunt.businessobjects;

public class RRPushMessage {

	public static final int INVITE_TO_PRIVATE_CHAT = 1;
	public static final int POKE = 1;

	public String body;

	public String[] receivers;
	public int requestCode;
	public String data;

	public RRPushMessage(String[] receivers) {
		this.receivers = receivers;
		this.requestCode = POKE;
	}

}
