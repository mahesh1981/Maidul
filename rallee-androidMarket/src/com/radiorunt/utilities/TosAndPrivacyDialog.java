package com.radiorunt.utilities;

import com.radiorunt.R;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;

public class TosAndPrivacyDialog extends Dialog {

	public static final int MODE_TOS = 0;
	public static final int MODE_PRIVACY = 1;
	private static int mode;

	public TosAndPrivacyDialog(Context context) {
		super(context, android.R.style.Theme_Translucent_NoTitleBar);

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.tos_privacy_dialog);

		WebView webView = (WebView) findViewById(R.id.wvTosPrivacyDialog);
		webView.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				view.loadUrl(url);
				return true;
			}
		});

		if (mode == MODE_TOS) {
			webView.loadUrl("file:///android_res/raw/rallee_tos.html");
			// webView.loadUrl("http://rall.ee/legal/tos");
		} else if (mode == MODE_PRIVACY) {
			webView.loadUrl("file:///android_res/raw/rallee_privacy_policy.html");
			// webView.loadUrl("http://rall.ee/legal/privacy");
		}

		((ImageButton) findViewById(R.id.imgbtn_close_dialog))
				.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						TosAndPrivacyDialog.this.dismiss();
					}
				});

		super.onCreate(savedInstanceState);
	}

	public static TosAndPrivacyDialog newInstance(Context context, int setMode) {
		mode = setMode;
		return new TosAndPrivacyDialog(context);
	}

}
