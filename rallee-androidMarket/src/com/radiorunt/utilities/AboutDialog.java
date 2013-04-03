package com.radiorunt.utilities;

import com.radiorunt.R;

import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.widget.TextView;

public class AboutDialog extends Dialog {

	public AboutDialog(Context context) {
		super(context, android.R.style.Theme_Translucent_NoTitleBar_Fullscreen);
	}

	public static AboutDialog newInstance(Context context) {
		AboutDialog about = new AboutDialog(context);

		return about;

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.about_dialog);

		TextView version = (TextView) findViewById(R.id.tvAboutVersion);
		try {
			version.setText(getContext().getString(R.string.label_version)
					+ RalleeApp
							.getInstance()
							.getPackageManager()
							.getPackageInfo(
									RalleeApp.getInstance().getPackageName(), 0).versionName);
		} catch (NameNotFoundException e) {

			e.printStackTrace();
		}

		super.onCreate(savedInstanceState);
	}

	// @Override
	// public View onCreateView(LayoutInflater inflater, ViewGroup container,
	// Bundle savedInstanceState) {
	// View aboutDialogView = inflater.inflate(R.layout.about_dialog,
	// container, false);
	// // mDialog.getWindow().setLayout(LayoutParams.MATCH_PARENT,
	// // LayoutParams.MATCH_PARENT);
	//
	//
	//
	// return aboutDialogView;
	// }
}
