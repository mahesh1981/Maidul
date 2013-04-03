package com.radiorunt.businessobjects;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public abstract class FBGroupsReceiver extends BroadcastReceiver {
	public static final String ACTION_DONE = "com.radiorunt.FBGroupsLoad.done";
	public static final String ACTION_FAILED = "com.radiorunt.FBGroupsLoad.failed";
	public static final String ACTION_IS_NOT_CHANGED = "com.radiorunt.FBGroupsLoad.notChanged";
	public static final String ACTION_NUMBER_OF_ITEMS = "com.radiorunt.FBGroupsLoad.numOfItems";
	public static final String ACTION_ITEM_LOADED = "com.radiorunt.FBGroupsLoad.itemLoaded";
	public static final String ACTION_ITEM_LOAD_FAILED = "com.radiorunt.FBGroupsLoad.itemLoadFailed";
}
