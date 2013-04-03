package com.radiorunt.utilities;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import com.radiorunt.R;

import android.app.Activity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.widget.TextView;

public class FaqActivity extends Activity {
	TextView faq;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.faq);

		TextView faq = (TextView) findViewById(R.id.textViewFaq);
		faq.setMovementMethod(ScrollingMovementMethod.getInstance());
		faq.setText(readTxt());
	}

	@Override
	protected void onPause() {
		finish();
		// TODO Auto-generated method stub
		super.onPause();
	}

	private String readTxt() {

		InputStream inputStream = getResources().openRawResource(
				R.raw.rallee_faq);

		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

		int i;
		try {
			i = inputStream.read();
			while (i != -1) {
				byteArrayOutputStream.write(i);
				i = inputStream.read();
			}
			inputStream.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return byteArrayOutputStream.toString();
	}

}
