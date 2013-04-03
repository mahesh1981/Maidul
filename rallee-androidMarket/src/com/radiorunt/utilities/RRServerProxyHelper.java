package com.radiorunt.utilities;

import com.radiorunt.R;
import com.radiorunt.businessobjects.RRChannels;
import com.radiorunt.services.RRServerProxyService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

public class RRServerProxyHelper {

	private static void startService(Context context, String url,
			int httpMethod, String requestBody, String actionDoneName,
			String actionFailedName) {
		Intent scvIntent = new Intent(context, RRServerProxyService.class);
		scvIntent.putExtra(RRServerProxyService.PARAM_IN_URL, url);
		scvIntent.putExtra(RRServerProxyService.PARAM_IN_HTTP_METHOD,
				httpMethod);
		scvIntent.putExtra(RRServerProxyService.PARAM_IN_JSON_REQUEST_BODY,
				requestBody);
		scvIntent.putExtra(RRServerProxyService.ACTION_DONE, actionDoneName);
		scvIntent
				.putExtra(RRServerProxyService.ACTION_FAILED, actionFailedName);
		context.startService(scvIntent);
	}

	public static void registerGenericServiceReceiver(Context context,
			BroadcastReceiver receiver, String[] actions) {
		for (int i = 0; i < actions.length; i++) {
			IntentFilter filter = new IntentFilter(actions[i]);
			filter.addCategory(Intent.CATEGORY_DEFAULT);
			context.registerReceiver(receiver, filter);
		}
	}

	// ///////////////////////////////////////////////////////////////////////////////////////////////////
	// ////////////////////////////// STARTS
	// ////////////////////////////////////////////////
	// ///////////////////////////////////////////////////////////////////////////////////////////////////

	public static void startRegisterToAppService(Context context,
			String jsonString) {
		String url = getServerURL(context) + "register";
		startService(context, url, RRHttpHandler.POST_METHOD, jsonString,
				context.getString(R.string.RegisterToAppServiceDone),
				context.getString(R.string.RegisterToAppServiceFailed));

	}

	public static void startUnregisterToAppService(Context context,
			String jsonString) {
		String url = getServerURL(context) + "unregister";
		startService(context, url, RRHttpHandler.POST_METHOD, jsonString,
				context.getString(R.string.UnregisterToAppServiceDone),
				context.getString(R.string.UnregisterToAppServiceFailed));

	}

	// public static void startGetRegistredUsersService(Context context) {
	// String url = getServerURL(context) + "users/getregistered";
	// String jsonString = "";
	// // Gson gson = new Gson();
	// // String jsonString=gson.toJson(bwGeo);
	// startService(context, url, RRHttpHandler.GET_METHOD, jsonString,
	// context.getString(R.string.GetRegistredUsersServiceDone),
	// context.getString(R.string.GetRegistredUsersServiceFailed));
	//
	// }

	public static void startGetServerIdService(Context context) {
		String url = getServerURL(context) + "serverid";
		String jsonString = "";
		startService(context, url, RRHttpHandler.GET_METHOD, jsonString,
				context.getString(R.string.GetServerIdServiceDone),
				context.getString(R.string.GetServerIdServiceFailed));

	}

	public static void startGetServerIpAddressService(Context context) {
		String url = getServerURL(context) + "serveripaddress";
		String jsonString = "";
		startService(context, url, RRHttpHandler.GET_METHOD, jsonString,
				context.getString(R.string.GetServerIpAddressServiceDone),
				context.getString(R.string.GetServerIpAddressServiceFailed));

	}

	public static void startGetChannelsService(Context context,
			String jsonString) {
		String url = getServerURL(context) + "getchannels";
		startService(context, url, RRHttpHandler.POST_METHOD, jsonString,
				context.getString(R.string.GetChannelsServiceDone),
				context.getString(R.string.GetChannelsServiceFailed));

	}

	public static void startGetRandomUserService(Context context,
			String jsonString) {
		String url = getServerURL(context) + "getRandomUserWithUsername";
		startService(context, url, RRHttpHandler.POST_METHOD, jsonString,
				context.getString(R.string.GetRandomUserServiceDone),
				context.getString(R.string.GetRandomUserServiceFailed));
	}

	//
	// public static void startCreateChannelsService(Context context, String
	// jsonString) {
	// String url = getServerURL(context) + "channels/createchannel";
	// startService(context, url, RRHttpHandler.POST_METHOD, jsonString,
	// context.getString(R.string.CreateChannelsServiceDone),
	// context.getString(R.string.CreateChannelsServiceFailed));
	//
	// }
	//
	// public static void startSignUpService(Context context, String jsonString)
	// {
	// String url = getServerURL(context) + "users/signup";
	// startService(context, url, RRHttpHandler.POST_METHOD, jsonString,
	// context.getString(R.string.SignUpServiceDone),
	// context.getString(R.string.SignUpServiceFailed));
	//
	// }

	public static void startSendPushMessageService(Context context,
			String jsonString) {
		String url = getServerURL(context) + "push";
		startService(context, url, RRHttpHandler.POST_METHOD, jsonString,
				context.getString(R.string.SendPushMessageServiceDone),
				context.getString(R.string.SendPushMessageServiceFailed));
	}

	public static void startGetParticipantsService(Context context,
			String jsonString) {
		String url = getServerURL(context) + "participants";
		startService(context, url, RRHttpHandler.POST_METHOD, jsonString,
				context.getString(R.string.GetParticipantsServiceDone),
				context.getString(R.string.GetParticipantsServiceFailed));
	}

	public static void startGetLeastParticipantsServer(Context context) {
		String url = getServerURL(context) + "getLeastParticipantsServer";
		String jsonString = "";
		startService(context, url, RRHttpHandler.GET_METHOD, jsonString,
				context.getString(R.string.GetLeastParticipantsServerDone),
				context.getString(R.string.GetLeastParticipantsServerFailed));
	}

	public static void startSetInsertUserService(Context context,
			String jsonString) {
		String url = getServerURL(context) + "insertUser";
		startService(context, url, RRHttpHandler.POST_METHOD, jsonString,
				context.getString(R.string.SetInsertUserServiceDone),
				context.getString(R.string.SetInsertUserServiceFailed));
	}

	public static void startGetUserService(Context context, String jsonString) {
		String url = getServerURL(context) + "getUser";
		startService(context, url, RRHttpHandler.POST_METHOD, jsonString,
				context.getString(R.string.GetUserServiceDone),
				context.getString(R.string.GetUserServiceFailed));
	}

	public static void startDeleteUserService(Context context, String jsonString) {
		String url = getServerURL(context) + "deleteUser";
		startService(context, url, RRHttpHandler.POST_METHOD, jsonString,
				context.getString(R.string.SetDeleteUserServiceDone),
				context.getString(R.string.SetDeleteUserServiceFailed));
	}

	public static void startSetOver18Service(Context context, String jsonString) {
		String url = getServerURL(context) + "setOver18";
		startService(context, url, RRHttpHandler.POST_METHOD, jsonString,
				context.getString(R.string.SetOver18ServiceDone),
				context.getString(R.string.SetOver18ServiceFailed));
	}

	public static void startSetRandomService(Context context, String jsonString) {
		String url = getServerURL(context) + "setRandom";
		startService(context, url, RRHttpHandler.POST_METHOD, jsonString,
				context.getString(R.string.SetRandomServiceDone),
				context.getString(R.string.SetRandomServiceFailed));
	}

	public static void startGetOver18Service(Context context, String jsonString) {
		String url = getServerURL(context) + "getOver18";
		startService(context, url, RRHttpHandler.POST_METHOD, jsonString,
				context.getString(R.string.GetOver18ServiceDone),
				context.getString(R.string.GetOver18ServiceFailed));
	}

	public static void startGetRandomService(Context context, String jsonString) {
		String url = getServerURL(context) + "getRandom";
		startService(context, url, RRHttpHandler.POST_METHOD, jsonString,
				context.getString(R.string.GetRandomServiceDone),
				context.getString(R.string.GetRandomServiceFailed));
	}

	public static void startCheckForNewReleaseService(Context context,
			String jsonString) {
		String url = getServerURL(context) + "checkForNewRelease";
		startService(context, url, RRHttpHandler.POST_METHOD, jsonString,
				context.getString(R.string.CheckForNewReleaseServiceDone),
				context.getString(R.string.CheckForNewReleaseServiceFailed));
	}

	public static void startSetUserLocationService(Context context,
			String jsonString) {
		String url = getServerURL(context) + "setUserLocation";
		startService(context, url, RRHttpHandler.POST_METHOD, jsonString,
				context.getString(R.string.SetUserLocationServiceDone),
				context.getString(R.string.SetUserLocationServiceFailed));
	}

	public static void startReportUserService(Context context, String jsonString) {
		String url = getServerURL(context) + "reportUser";
		startService(context, url, RRHttpHandler.POST_METHOD, jsonString,
				context.getString(R.string.SetReportUserServiceDone),
				context.getString(R.string.SetReportUserServiceFailed));
	}

	// ///////////////////////////////////////////////////////////////////////////////////////////////////
	// ///////////////////////////// REGISTRATIONS
	// ///////////////////////////////////////
	// ///////////////////////////////////////////////////////////////////////////////////////////////////

	public static void registerGetServerIdReceiver(Context context,
			BroadcastReceiver receiver) {
		String[] actions = new String[] {
				context.getString(R.string.GetServerIdServiceDone),
				context.getString(R.string.GetServerIdServiceFailed) };
		registerGenericServiceReceiver(context, receiver, actions);
	}

	public static void registerGetServerIpAddressReceiver(Context context,
			BroadcastReceiver receiver) {
		String[] actions = new String[] {
				context.getString(R.string.GetServerIpAddressServiceDone),
				context.getString(R.string.GetServerIpAddressServiceFailed) };
		registerGenericServiceReceiver(context, receiver, actions);
	}

	// public static void registerGetRegistredUsersReceiver(Context context,
	// BroadcastReceiver receiver) {
	// String[] actions = new String[] {
	// context.getString(R.string.GetRegistredUsersServiceDone),
	// context.getString(R.string.GetRegistredUsersServiceFailed) };
	// registerGenericServiceReceiver(context, receiver, actions);
	// }
	//
	public static void registerGetChannelsReceiver(Context context,
			BroadcastReceiver receiver) {
		String[] actions = new String[] {
				context.getString(R.string.GetChannelsServiceDone),
				context.getString(R.string.GetChannelsServiceFailed) };
		registerGenericServiceReceiver(context, receiver, actions);
	}

	public static void registerGetRandomUserReceiver(Context context,
			BroadcastReceiver receiver) {
		String[] actions = new String[] {
				context.getString(R.string.GetRandomUserServiceDone),
				context.getString(R.string.GetRandomUserServiceFailed) };
		registerGenericServiceReceiver(context, receiver, actions);
	}

	//
	// public static void registerCreateChannelsReceiver(Context context,
	// BroadcastReceiver receiver) {
	// String[] actions = new String[] {
	// context.getString(R.string.CreateChannelsServiceDone),
	// context.getString(R.string.CreateChannelsServiceFailed) };
	// registerGenericServiceReceiver(context, receiver, actions);
	// }
	//
	// public static void registerSignUpReceiver(Context context,
	// BroadcastReceiver receiver) {
	// String[] actions = new String[] {
	// context.getString(R.string.SignUpServiceDone),
	// context.getString(R.string.SignUpServiceFailed) };
	// registerGenericServiceReceiver(context, receiver, actions);
	// }

	public static void registerRegisterToAppReceiver(Context context,
			BroadcastReceiver receiver) {
		String[] actions = new String[] {
				context.getString(R.string.RegisterToAppServiceDone),
				context.getString(R.string.RegisterToAppServiceFailed) };
		registerGenericServiceReceiver(context, receiver, actions);
	}

	public static void registerUnregisterToAppReceiver(Context context,
			BroadcastReceiver receiver) {
		String[] actions = new String[] {
				context.getString(R.string.UnregisterToAppServiceDone),
				context.getString(R.string.UnregisterToAppServiceFailed) };
		registerGenericServiceReceiver(context, receiver, actions);
	}

	public static void registerSendPushMessageReceiver(Context context,
			BroadcastReceiver receiver) {
		String[] actions = new String[] {
				context.getString(R.string.SendPushMessageServiceDone),
				context.getString(R.string.SendPushMessageServiceFailed) };
		registerGenericServiceReceiver(context, receiver, actions);
	}

	public static void registerGetParticipantsReceiver(Context context,
			BroadcastReceiver receiver) {
		String[] actions = new String[] {
				context.getString(R.string.GetParticipantsServiceDone),
				context.getString(R.string.GetParticipantsServiceFailed) };
		registerGenericServiceReceiver(context, receiver, actions);
	}

	public static void registerGetLeastParticipantsServerReceiver(
			Context context, BroadcastReceiver receiver) {
		String[] actions = new String[] {
				context.getString(R.string.GetLeastParticipantsServerDone),
				context.getString(R.string.GetLeastParticipantsServerFailed) };
		registerGenericServiceReceiver(context, receiver, actions);
	}

	public static void registerSetInsertUserReceiver(Context context,
			BroadcastReceiver receiver) {
		String[] actions = new String[] {
				context.getString(R.string.SetInsertUserServiceDone),
				context.getString(R.string.SetInsertUserServiceFailed) };
		registerGenericServiceReceiver(context, receiver, actions);
	}

	public static void registerGetUserReceiver(Context context,
			BroadcastReceiver receiver) {
		String[] actions = new String[] {
				context.getString(R.string.GetUserServiceDone),
				context.getString(R.string.GetUserServiceFailed) };
		registerGenericServiceReceiver(context, receiver, actions);
	}

	public static void registerSetDeleteUserReceiver(Context context,
			BroadcastReceiver receiver) {
		String[] actions = new String[] {
				context.getString(R.string.SetDeleteUserServiceDone),
				context.getString(R.string.SetDeleteUserServiceFailed) };
		registerGenericServiceReceiver(context, receiver, actions);
	}

	public static void registerSetOver18Receiver(Context context,
			BroadcastReceiver receiver) {
		String[] actions = new String[] {
				context.getString(R.string.SetOver18ServiceDone),
				context.getString(R.string.SetOver18ServiceFailed) };
		registerGenericServiceReceiver(context, receiver, actions);
	}

	public static void registerSetRandomsReceiver(Context context,
			BroadcastReceiver receiver) {
		String[] actions = new String[] {
				context.getString(R.string.SetRandomServiceDone),
				context.getString(R.string.SetRandomServiceFailed) };
		registerGenericServiceReceiver(context, receiver, actions);
	}

	public static void registerGetOver18Receiver(Context context,
			BroadcastReceiver receiver) {
		String[] actions = new String[] {
				context.getString(R.string.GetOver18ServiceDone),
				context.getString(R.string.GetOver18ServiceFailed) };
		registerGenericServiceReceiver(context, receiver, actions);
	}

	public static void registerGetRandomReceiver(Context context,
			BroadcastReceiver receiver) {
		String[] actions = new String[] {
				context.getString(R.string.GetRandomServiceDone),
				context.getString(R.string.GetRandomServiceFailed) };
		registerGenericServiceReceiver(context, receiver, actions);
	}

	public static void registerCheckForNewReleaseReceiver(Context context,
			BroadcastReceiver receiver) {
		String[] actions = new String[] {
				context.getString(R.string.CheckForNewReleaseServiceDone),
				context.getString(R.string.CheckForNewReleaseServiceFailed) };
		registerGenericServiceReceiver(context, receiver, actions);
	}

	public static void registerSetUserLocationReceiver(Context context,
			BroadcastReceiver receiver) {
		String[] actions = new String[] {
				context.getString(R.string.SetUserLocationServiceDone),
				context.getString(R.string.SetUserLocationServiceFailed) };
		registerGenericServiceReceiver(context, receiver, actions);
	}

	public static void registerReportUserReceiver(Context context,
			BroadcastReceiver receiver) {
		String[] actions = new String[] {
				context.getString(R.string.SetReportUserServiceDone),
				context.getString(R.string.SetRandomServiceFailed) };
		registerGenericServiceReceiver(context, receiver, actions);
	}

	// ///////////////////////////////////////////////////////////////////////////////////////////////////
	// //////////////////// ERROR ////////////////////////////
	// ///////////////////////////////////////////////////////////////////////////////////////////////////

	// public static String getRegistredUsersError(Context context, Intent
	// intent) {
	// String error = "";
	// if (intent.getAction().equals(
	// context.getString(R.string.GetRegistredUsersServiceFailed)))
	// error = intent.getStringExtra(RRServerProxyService.PARAM_OUT_ERROR);
	// return error;
	// }
	//
	public static String getChannelsError(Context context, Intent intent) {
		String error = "";
		if (intent.getAction().equals(
				context.getString(R.string.GetChannelsServiceFailed)))
			error = intent.getStringExtra(RRServerProxyService.PARAM_OUT_ERROR);
		return error;
	}

	public static String getRandomUserError(Context context, Intent intent) {
		String error = "";
		if (intent.getAction().equals(
				context.getString(R.string.GetRandomUserServiceFailed)))
			error = intent.getStringExtra(RRServerProxyService.PARAM_OUT_ERROR);
		return error;
	}

	public static String getServerIdError(Context context, Intent intent) {
		String error = "";
		if (intent.getAction().equals(
				context.getString(R.string.GetServerIdServiceFailed)))
			error = intent.getStringExtra(RRServerProxyService.PARAM_OUT_ERROR);
		return error;
	}

	public static String getServerIpAddressError(Context context, Intent intent) {
		String error = "";
		if (intent.getAction().equals(
				context.getString(R.string.GetServerIpAddressServiceFailed)))
			error = intent.getStringExtra(RRServerProxyService.PARAM_OUT_ERROR);
		return error;
	}

	// public static String createChannelsError(Context context, Intent intent)
	// {
	// String error = "";
	// if (intent.getAction().equals(
	// context.getString(R.string.CreateChannelsServiceFailed)))
	// error = intent.getStringExtra(RRServerProxyService.PARAM_OUT_ERROR);
	// return error;
	// }
	//
	// public static String SignUpError(Context context, Intent intent) {
	// String error = "";
	// if (intent.getAction().equals(
	// context.getString(R.string.SignUpServiceFailed)))
	// error = intent.getStringExtra(RRServerProxyService.PARAM_OUT_ERROR);
	// return error;
	// }

	public static String registerToAppError(Context context, Intent intent) {
		String error = "";
		if (intent.getAction().equals(
				context.getString(R.string.RegisterToAppServiceFailed)))
			error = intent.getStringExtra(RRServerProxyService.PARAM_OUT_ERROR);
		return error;
	}

	public static String unregisterToAppError(Context context, Intent intent) {
		String error = "";
		if (intent.getAction().equals(
				context.getString(R.string.UnregisterToAppServiceFailed)))
			error = intent.getStringExtra(RRServerProxyService.PARAM_OUT_ERROR);
		return error;
	}

	public static String sendPushMessageError(Context context, Intent intent) {
		String error = "";
		if (intent.getAction().equals(
				context.getString(R.string.SendPushMessageServiceFailed)))
			error = intent.getStringExtra(RRServerProxyService.PARAM_OUT_ERROR);
		return error;
	}

	public static String getParticipantsError(Context context, Intent intent) {
		String error = "";
		if (intent.getAction().equals(
				context.getString(R.string.GetParticipantsServiceFailed)))
			error = intent.getStringExtra(RRServerProxyService.PARAM_OUT_ERROR);
		return error;
	}

	public static String getLeastParticipantsServerError(Context context,
			Intent intent) {
		String error = "";
		if (intent.getAction().equals(
				context.getString(R.string.GetLeastParticipantsServerFailed)))
			error = intent.getStringExtra(RRServerProxyService.PARAM_OUT_ERROR);
		return error;
	}

	public static String setInsertUserError(Context context, Intent intent) {
		String error = "";
		if (intent.getAction().equals(
				context.getString(R.string.SetInsertUserServiceFailed)))
			error = intent.getStringExtra(RRServerProxyService.PARAM_OUT_ERROR);
		return error;
	}

	public static String getUserError(Context context, Intent intent) {
		String error = "";
		if (intent.getAction().equals(
				context.getString(R.string.GetUserServiceFailed)))
			error = intent.getStringExtra(RRServerProxyService.PARAM_OUT_ERROR);
		return error;
	}

	public static String setDeleteUserError(Context context, Intent intent) {
		String error = "";
		if (intent.getAction().equals(
				context.getString(R.string.SetDeleteUserServiceFailed)))
			error = intent.getStringExtra(RRServerProxyService.PARAM_OUT_ERROR);
		return error;
	}

	public static String setOver18Error(Context context, Intent intent) {
		String error = "";
		if (intent.getAction().equals(
				context.getString(R.string.SetOver18ServiceFailed)))
			error = intent.getStringExtra(RRServerProxyService.PARAM_OUT_ERROR);
		return error;
	}

	public static String setRandomError(Context context, Intent intent) {
		String error = "";
		if (intent.getAction().equals(
				context.getString(R.string.SetRandomServiceFailed)))
			error = intent.getStringExtra(RRServerProxyService.PARAM_OUT_ERROR);
		return error;
	}

	public static String getOver18Error(Context context, Intent intent) {
		String error = "";
		if (intent.getAction().equals(
				context.getString(R.string.GetOver18ServiceFailed)))
			error = intent.getStringExtra(RRServerProxyService.PARAM_OUT_ERROR);
		return error;
	}

	public static String getRandomError(Context context, Intent intent) {
		String error = "";
		if (intent.getAction().equals(
				context.getString(R.string.GetRandomServiceFailed)))
			error = intent.getStringExtra(RRServerProxyService.PARAM_OUT_ERROR);
		return error;
	}

	public static String checkForNewReleaseError(Context context, Intent intent) {
		String error = "";
		if (intent.getAction().equals(
				context.getString(R.string.CheckForNewReleaseServiceFailed)))
			error = intent.getStringExtra(RRServerProxyService.PARAM_OUT_ERROR);
		return error;
	}

	public static String setUserLocationError(Context context, Intent intent) {
		String error = "";
		if (intent.getAction().equals(
				context.getString(R.string.SetUserLocationServiceFailed)))
			error = intent.getStringExtra(RRServerProxyService.PARAM_OUT_ERROR);
		return error;
	}

	public static String setReportUserError(Context context, Intent intent) {
		String error = "";
		if (intent.getAction().equals(
				context.getString(R.string.SetReportUserServiceFailed)))
			error = intent.getStringExtra(RRServerProxyService.PARAM_OUT_ERROR);
		return error;
	}

	// ///////////////////////////////////////////////////////////////////////////////////////////////////
	// //////////////////////// RESPONSES ///////////////////////////
	// ///////////////////////////////////////////////////////////////////////////////////////////////////

	public static String getServerIdResponse(Context context, Intent intent) {
		return intent
				.getStringExtra(RRServerProxyService.PARAM_OUT_JSON_RESPONSE_BODY);
	}

	public static String getServerIpAddressResponse(Context context,
			Intent intent) {
		return intent
				.getStringExtra(RRServerProxyService.PARAM_OUT_JSON_RESPONSE_BODY);
	}

	// public static String getRegistredUsersResponse(Context context,
	// Intent intent) {
	// return intent
	// .getStringExtra(RRServerProxyService.PARAM_OUT_JSON_RESPONSE_BODY);
	// }
	//
	public static String getChannelsResponse(Context context, Intent intent) {
		return intent
				.getStringExtra(RRServerProxyService.PARAM_OUT_JSON_RESPONSE_BODY);
	}

	public static String getRandomUserResponse(Context context, Intent intent) {
		return intent
				.getStringExtra(RRServerProxyService.PARAM_OUT_JSON_RESPONSE_BODY);
	}

	//
	// public static String createChannelsResponse(Context context,
	// Intent intent) {
	// return intent
	// .getStringExtra(RRServerProxyService.PARAM_OUT_JSON_RESPONSE_BODY);
	// }

	public static String registerToAppResponse(Context context, Intent intent) {
		return intent
				.getStringExtra(RRServerProxyService.PARAM_OUT_JSON_RESPONSE_BODY);
	}

	public static String unregisterToAppResponse(Context context, Intent intent) {
		return intent
				.getStringExtra(RRServerProxyService.PARAM_OUT_JSON_RESPONSE_BODY);
	}

	// public static String SignUpResponse(Context context,
	// Intent intent) {
	// return intent
	// .getStringExtra(RRServerProxyService.PARAM_OUT_JSON_RESPONSE_BODY);
	// }

	public static String sendPushMessageResponse(Context context, Intent intent) {
		return intent
				.getStringExtra(RRServerProxyService.PARAM_OUT_JSON_RESPONSE_BODY);
	}

	public static String getParticipantsResponse(Context context, Intent intent) {
		return intent
				.getStringExtra(RRServerProxyService.PARAM_OUT_JSON_RESPONSE_BODY);
	}

	public static String getLeastParticipantsServerResponse(Context context,
			Intent intent) {
		return intent
				.getStringExtra(RRServerProxyService.PARAM_OUT_JSON_RESPONSE_BODY);
	}

	public static String setInsertUserResponse(Context context, Intent intent) {
		return intent
				.getStringExtra(RRServerProxyService.PARAM_OUT_JSON_RESPONSE_BODY);
	}

	public static String getUserResponse(Context context, Intent intent) {
		return intent
				.getStringExtra(RRServerProxyService.PARAM_OUT_JSON_RESPONSE_BODY);
	}

	public static String setDeleteUserResponse(Context context, Intent intent) {
		return intent
				.getStringExtra(RRServerProxyService.PARAM_OUT_JSON_RESPONSE_BODY);
	}

	public static String setOver18Response(Context context, Intent intent) {
		return intent
				.getStringExtra(RRServerProxyService.PARAM_OUT_JSON_RESPONSE_BODY);
	}

	public static String setRandomResponse(Context context, Intent intent) {
		return intent
				.getStringExtra(RRServerProxyService.PARAM_OUT_JSON_RESPONSE_BODY);
	}

	public static String getOver18Response(Context context, Intent intent) {
		return intent
				.getStringExtra(RRServerProxyService.PARAM_OUT_JSON_RESPONSE_BODY);
	}

	public static String getRandomResponse(Context context, Intent intent) {
		return intent
				.getStringExtra(RRServerProxyService.PARAM_OUT_JSON_RESPONSE_BODY);
	}

	public static String checkForNewReleaseResponse(Context context,
			Intent intent) {
		return intent
				.getStringExtra(RRServerProxyService.PARAM_OUT_JSON_RESPONSE_BODY);
	}

	public static String setUserLocationResponse(Context context, Intent intent) {
		return intent
				.getStringExtra(RRServerProxyService.PARAM_OUT_JSON_RESPONSE_BODY);
	}

	public static String setReportUserResponse(Context context, Intent intent) {
		return intent
				.getStringExtra(RRServerProxyService.PARAM_OUT_JSON_RESPONSE_BODY);
	}

	// ///////////////////////////////////////////////////////////////////////////////////////////////////
	// //////////////////////// RESPONSES END ///////////////////////////
	// //////////////////////// ///////////////////////////
	// ///////////////////////////////////////////////////////////////////////////////////////////////////

	private static String getServerURL(Context context) {
		return context.getString(R.string.AppServiceURL);

	}

}
