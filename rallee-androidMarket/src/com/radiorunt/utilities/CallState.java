package com.radiorunt.utilities;

import java.util.ArrayList;
import java.util.List;

public class CallState {
	public static final int NORMAL = 0;
	public static final int INCOMING = 1;
	public static final int OUTGOING = 2;
	public static final int INCALL = 3;
	public static final int INCALL_NEARBY = 4;
	public static int sCurrent = NORMAL;

	public static final int CALL_IDLE = 0;
	public static final int CALL_PRIVATE = 1;
	public static final int CALL_PRIVATE_GROUP = 2;
	public static final int CALL_FB_GROUP = 3;
	public static final int CALL_PUBLIC_GROUP = 4;
	public static final int CALL_RANDOM = 5;
	public static int sCallType = CALL_IDLE;

	public static List<String> calledFriends = new ArrayList<String>();
}
