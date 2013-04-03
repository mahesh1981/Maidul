package com.radiorunt.activities;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import com.radiorunt.R;
import com.radiorunt.activities.LogInActivity;
import com.radiorunt.businessobjects.RRChannels;
import com.radiorunt.facebook.Utility;
import com.radiorunt.utilities.Globals;
import com.radiorunt.utilities.RRServerProxyHelper;

import android.app.Activity;
import android.app.LocalActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class IntentChannel extends Activity {
	CharSequence[] permanentChannelsSeq = null;
	public static ArrayList<RRChannels> permanentChannels = new ArrayList<RRChannels>();
	String trimedInvocedChannelName;
	String realInvocedChannelName;
	RRChannels intentChannel;
	private GetChannelsServiceReceiver mGetChannelsServiceReceiver;
	private ProgressBar checkSpiner;
	private TextView txtView;
	private volatile Handler mHandler;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.intent_channel);

		// ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// /////////////////////////////// Connect to Intent Channel
		// ////////////////////////////////////////////////////////////
		final ProgressBar checkSpiner = (ProgressBar) findViewById(R.id.progressBarIntentChann);
		final TextView txtView = (TextView) findViewById(R.id.textViewIntentCh);

		mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				if (msg.what == 1) {
					try {
						checkSpiner.setVisibility(View.VISIBLE);
						txtView.setVisibility(View.VISIBLE);
						txtView.setText("Searching Channels...");
					} catch (Exception e) {
						e.printStackTrace();
					}
				} else if (msg.what == 2) {
					checkSpiner.setVisibility(View.INVISIBLE);
					txtView.setVisibility(View.VISIBLE);
					txtView.setText("Channel " + realInvocedChannelName
							+ " exist");
				} else {
					checkSpiner.setVisibility(View.INVISIBLE);
					txtView.setVisibility(View.VISIBLE);
					txtView.setText("Channel " + realInvocedChannelName
							+ " doesn't exist");
				}
			}
		};
		mHandler.sendEmptyMessage(1);

		Intent intent = getIntent();

		String action = intent.getAction();
		Globals.logDebug(this, "Intent Channel " + action);
		Uri data = intent.getData();

		if (data != null) {
			Utility.invocedChannelName = data.getPath();
			Globals.logDebug(
					this,
					"Intent Channel "
							+ Utility.invocedChannelName.substring(6,
									Utility.invocedChannelName.length()));
		}

		intentChannel = new RRChannels();
		registerGetChannelsServiceReceiver();
	}

	@Override
	protected final void onPause() {
		unregisterGetChannelsServiceReceiver();
		finish();
		super.onPause();
	}

	private void registerGetChannelsServiceReceiver() {
		if (mGetChannelsServiceReceiver != null) {
			return;
		}
		String locale = Locale.getDefault().getLanguage();

		mGetChannelsServiceReceiver = new GetChannelsServiceReceiver();
		RRServerProxyHelper.registerGetChannelsReceiver(this,
				mGetChannelsServiceReceiver);
		RRServerProxyHelper.startGetChannelsService(getApplicationContext(),
				locale);
	}

	private void unregisterGetChannelsServiceReceiver() {
		if (mGetChannelsServiceReceiver == null) {
			return;
		}

		unregisterReceiver(mGetChannelsServiceReceiver);

		mGetChannelsServiceReceiver = null;
	}

	public class GetChannelsServiceReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {

			String errMsg = RRServerProxyHelper.getChannelsError(context,
					intent);
			if (errMsg.equals("")) {
				String jsonResponseString = RRServerProxyHelper
						.getChannelsResponse(context, intent);

				Globals.logDebug(this, "Channels " + jsonResponseString);

				ObjectMapper mapper = new ObjectMapper();
				RRChannels[] array = null;

				try {
					permanentChannels = mapper.readValue(jsonResponseString,
							new TypeReference<List<RRChannels>>() {
							});
					array = mapper.readValue(jsonResponseString,
							RRChannels[].class);
				} catch (JsonParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (JsonMappingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if (permanentChannels != null) {
					permanentChannelsSeq = new CharSequence[permanentChannels
							.size()];
					for (int i = 0; i < permanentChannels.size(); i++) {
						String name = new String(permanentChannels.get(i).name);
						permanentChannelsSeq[i] = name.subSequence(5,
								name.length());
					}

					// ///////////////////////////////// Coneect to Intent
					// Channel
					// /////////////////////////////////////////////////////////////////////////////

					trimedInvocedChannelName = Utility.invocedChannelName
							.substring(1, Utility.invocedChannelName.length());
					realInvocedChannelName = Utility.invocedChannelName
							.substring(6, Utility.invocedChannelName.length());

					Globals.logDebug(this, "Intent Channel "
							+ trimedInvocedChannelName);

					for (int i = 0; i < permanentChannels.size(); i++) {
						Globals.logDebug(this, "Intent Channel "
								+ permanentChannels.get(i).name);
						intentChannel = null;
						if (permanentChannels.get(i).name
								.equals(trimedInvocedChannelName)) {
							intentChannel = permanentChannels.get(i);
							break;
						}
					}
					if (intentChannel != null) {
						Globals.logDebug(this, "" + "gc Exist");
						mHandler.sendEmptyMessageDelayed(2, 3000);

						Globals.logDebug(this, "Intent Channel "
								+ intentChannel.name);
						Globals.logDebug(this, "Intent Channel "
								+ intentChannel.id);
						Globals.logDebug(this, "Intent Channel "
								+ intentChannel.serverIpAdr.toString());
						Globals.logDebug(this, "Intent Channel "
								+ intentChannel.port.toString());

						Utility.switchChannel = intentChannel;
						Utility.channelName = realInvocedChannelName;
						Intent gardenIntent = new Intent(
								getApplicationContext(), LogInActivity.class);
						gardenIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
								.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						startActivity(gardenIntent);

					} else {
						mHandler.sendEmptyMessageDelayed(3, 3000);
						Globals.logDebug(this, "Intent Channel "
								+ "gc doesn't Exist");
					}

					// //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

				}

			} else {
				// Toast.makeText(IntentChannel.this, errMsg, 3000).show();
			}
		}
	}
}