package com.radiorunt.services;

import java.util.Locale;

import com.radiorunt.utilities.Settings;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.widget.Toast;

/**
 * A static TTS connector class handler
 */
public class TtsProvider {

	private static TextToSpeech mTts;
	private static Settings settings;
	private static boolean ttsEnabled;

	/**
	 * Initializes the TTS connection and parses settings
	 * 
	 * @param context
	 */
	public static void init(final Context context) {

		if (settings == null)
			settings = new Settings(context);
		ttsEnabled = settings.isTtsEnabled();
		// if (mTts == null && TtsEnabled) {
		// mTts = new TextToSpeech(context, new OnInitListener() {
		// public void onInit(int status) {
		//
		// Locale loc = Locale.US;
		// if(mTts.isLanguageAvailable(loc) >= TextToSpeech.LANG_AVAILABLE) {
		// mTts.setLanguage(loc);
		// }
		// }
		// });
		// }
		if (mTts == null && ttsEnabled) {
			mTts = new TextToSpeech(context, new OnInitListener() {
				public void onInit(int status) {

					Locale loc = Locale.US;
					if (mTts.isLanguageAvailable(loc) >= TextToSpeech.LANG_AVAILABLE) {
						mTts.setLanguage(loc);
						// Toast.makeText(context, "Language sat to US!",
						// 500).show();
					}
				}
			});
			// Toast.makeText(context, "Text to speach enabled!", 1000).show();
			if (mTts != null) {
				// Toast.makeText(context, "TTS object created!", 500).show();
			}
		}
	}

	/**
	 * Closes the TTS connection (needed for a clean exit and and reuse of the
	 * TTS object)
	 */
	public static void close() {
		if (mTts != null) {
			mTts.stop();
			mTts.shutdown();
			mTts = null;
		}
	}

	/**
	 * Sends specified text to the TTS service
	 * 
	 * @param text
	 *            The text to send
	 * @param append
	 *            <code>true</code> if TTS queue should be overriden,
	 *            <code>false</code> to append to the existing queue
	 */
	public static void speak(String text, boolean append) {
		// if (mTts != null && TtsEnabled){
		// if (append) mTts.speak(text, TextToSpeech.QUEUE_ADD, null);
		// else mTts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
		// }
		if (mTts != null) {
			if (append)
				mTts.speak(text, TextToSpeech.QUEUE_ADD, null);
			else
				mTts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
		}
	}

}
