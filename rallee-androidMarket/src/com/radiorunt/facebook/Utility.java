package com.radiorunt.facebook;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Timestamp;
import java.util.Hashtable;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import com.facebook.FacebookException;
import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.Facebook;
import com.facebook.model.GraphObject;
import com.radiorunt.businessobjects.RRChannels;
import com.radiorunt.utilities.RalleeApp;

import android.app.Application;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.net.http.AndroidHttpClient;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

public class Utility extends Application {

	public static final String RALLEE_ID = "ralleeId";
	public static final String SOCIAL_NETWORK_TYPE = "snt";
	public static final String SOCIAL_NETWORK_ID = "snid";

	// public static Facebook mFacebook;
	// public static AsyncFacebookRunner mAsyncRunner;
	public static JSONObject mFriendsList;
	/**
	 * userUID represent concatenation of user id and socialNetworkCode. This is
	 * actual username for connection to the server
	 */
	// public static String userUID = null;
	/**
	 * ralleeUID represent actual user name of user before concatenation
	 */
	// public static String ralleeUID = null;

	/**
	 * Prefix for various networks Fb network - fb_
	 */
	public static String networkPrefix = "fb_";
	/**
	 * FB id of FB User
	 */
	public static String fbId = null;
	// public static String userName = null;
	public static boolean connectionStatus = false;
	public static String picUrl = null;
	public static String senderName = "";
	public static String senderPicUrl = "";
	public static String senderFBId = "";
	public static String channelName = "";
	public static long callTimestamp = 0;
	public static String objectID = null;
	public static String calledUserFirstName = null;
	public static String calledUserUsername = "";
	// public static FriendsGetProfilePics model;
	public static AndroidHttpClient httpclient = null;
	public static Hashtable<String, String> currentPermissions = new Hashtable<String, String>();

	public static String testServerAddress = null;
	public static String bestServer = null;
	public static String testServerName = "";
	public static String testGroup = "";

	private static final int MAX_IMAGE_DIMENSION = 720;
	public static RRChannels switchChannel;
	public static String invocedChannelName;
	// public static Timestamp timestampOfConnectedState = null;
	// public static final String APP_ICON_URL =
	// "http://ec2-174-129-43-112.compute-1.amazonaws.com/icon75x75.png";
	public static final String APP_ICON_URL = "http://rall.ee/icon75x75.png";

	public static Bitmap getBitmap(String url, Context c) {
		Bitmap bm = null;
		String filename= null;
		if(url.contains("width")==false) {
		 filename = makeFilename(url);
		bm = getBitmapFromExternalStorage(filename, c);
		if (bm != null) {
			return bm;
		}
		}
		try {
			URL aURL = new URL(url);
			URLConnection conn = aURL.openConnection();
			conn.connect();
			InputStream is = conn.getInputStream();
			BufferedInputStream bis = new BufferedInputStream(is);
			bm = BitmapFactory.decodeStream(new FlushedInputStream(is));
			bis.close();
			is.close();
			if(url.contains("width")==false)
			saveBitmapToExternalStorage(bm, filename, c);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (httpclient != null) {
				httpclient.close();
			}
		}
		return bm;
	}

	private static String makeFilename(String url) {
		String filename = url;
		if (filename == null) {
			return null;
		}
		int delimiter = '/';
		int index = filename.indexOf(delimiter);
		if (index == -1) {
			return null;
		}

		while (index != -1) {
			filename = filename.substring(index + 1);
			index = filename.indexOf(delimiter);
		}
		return filename;
	}

	private static Bitmap getBitmapFromExternalStorage(String filename,
			Context c) {
		Bitmap bm = null;
		if (filename == null) {
			return null;
		}
		File file = new File(c.getExternalFilesDir(null), filename);
		if (file != null) {
			if (file.exists() && file.isFile()) {
				bm = BitmapFactory.decodeFile(file.getPath());
			}
		}
		return bm;
	}

	private static boolean saveBitmapToExternalStorage(Bitmap bm,
			String filename, Context c) {
		boolean success = false;
		if (filename == null || bm == null) {
			return success;
		}
		try {
			File file = new File(c.getExternalFilesDir(null), filename);
			if (file != null) {
				file.createNewFile();
				if (file.exists() && file.isFile()) {
					FileOutputStream out = new FileOutputStream(file.getPath());
					bm.compress(Bitmap.CompressFormat.JPEG, 100, out);
					out.close();
					success = true;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
//			Log.i("photosave", "EXTERNAL STORAGE: " + e.toString());
		}
		return success;
	}

	static class FlushedInputStream extends FilterInputStream {
		public FlushedInputStream(InputStream inputStream) {
			super(inputStream);
		}

		@Override
		public long skip(long n) throws IOException {
			long totalBytesSkipped = 0L;
			while (totalBytesSkipped < n) {
				long bytesSkipped = in.skip(n - totalBytesSkipped);
				if (bytesSkipped == 0L) {
					int b = read();
					if (b < 0) {
						break; // we reached EOF
					} else {
						bytesSkipped = 1; // we read one byte
					}
				}
				totalBytesSkipped += bytesSkipped;
			}
			return totalBytesSkipped;
		}
	}

	public static byte[] scaleImage(Context context, Uri photoUri)
			throws IOException {
		InputStream is = context.getContentResolver().openInputStream(photoUri);
		BitmapFactory.Options dbo = new BitmapFactory.Options();
		dbo.inJustDecodeBounds = true;
		BitmapFactory.decodeStream(is, null, dbo);
		is.close();

		int rotatedWidth, rotatedHeight;
		int orientation = getOrientation(context, photoUri);

		if (orientation == 90 || orientation == 270) {
			rotatedWidth = dbo.outHeight;
			rotatedHeight = dbo.outWidth;
		} else {
			rotatedWidth = dbo.outWidth;
			rotatedHeight = dbo.outHeight;
		}

		Bitmap srcBitmap;
		is = context.getContentResolver().openInputStream(photoUri);
		if (rotatedWidth > MAX_IMAGE_DIMENSION
				|| rotatedHeight > MAX_IMAGE_DIMENSION) {
			float widthRatio = ((float) rotatedWidth)
					/ ((float) MAX_IMAGE_DIMENSION);
			float heightRatio = ((float) rotatedHeight)
					/ ((float) MAX_IMAGE_DIMENSION);
			float maxRatio = Math.max(widthRatio, heightRatio);

			// Create the bitmap from file
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inSampleSize = (int) maxRatio;
			srcBitmap = BitmapFactory.decodeStream(is, null, options);
		} else {
			srcBitmap = BitmapFactory.decodeStream(is);
		}
		is.close();

		/*
		 * if the orientation is not 0 (or -1, which means we don't know), we
		 * have to do a rotation.
		 */
		if (orientation > 0) {
			Matrix matrix = new Matrix();
			matrix.postRotate(orientation);

			srcBitmap = Bitmap.createBitmap(srcBitmap, 0, 0,
					srcBitmap.getWidth(), srcBitmap.getHeight(), matrix, true);
		}

		String type = context.getContentResolver().getType(photoUri);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		if (type.equals("image/png")) {
			srcBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
		} else if (type.equals("image/jpg") || type.equals("image/jpeg")) {
			srcBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
		}
		byte[] bMapArray = baos.toByteArray();
		baos.close();
		return bMapArray;
	}

	public static int getOrientation(Context context, Uri photoUri) {
		/* it's on the external media. */
		Cursor cursor = context.getContentResolver().query(photoUri,
				new String[] { MediaStore.Images.ImageColumns.ORIENTATION },
				null, null, null);

		if (cursor.getCount() != 1) {
			return -1;
		}

		cursor.moveToFirst();
		return cursor.getInt(0);
	}

	public static String buildUsername(String socialNetworkCode, String id,
			String[] groupIds) {
		id = socialNetworkCode + id;
		if (groupIds != null) {
			for (int i = 0; i < groupIds.length; i++) {
				if (!groupIds[i].equals("")) {
					if (i == 0) {
						id = id + "@" + groupIds[i];
					} else {
						id = id + "_" + groupIds[i]; // "+" za sada nije moguæ,
														// iskoristiti neki
														// drugi karakter
					}
				}
			}
		}
		return id;
	}

	// parse social network data (network type and id)
	public static ContentValues parseSNData(String userUID) {
//		Log.i("random", "parseSNData, userUID: " + userUID);
		ContentValues snd = new ContentValues();
		String[] res = { "Unknown" };
		if (userUID != null) {
			res = userUID.split("@");
		}
		String ralleeId = res[0];
//		Log.i("random", "parseSNData, ralleeId: " + ralleeId);
		snd.put(RALLEE_ID, ralleeId);
		String[] ralleIdItems = ralleeId.split("_");
		if (ralleIdItems.length > 0) {
			snd.put(SOCIAL_NETWORK_TYPE, ralleIdItems[0]);
//			Log.i("random", "parseSNData, SOCIAL_NETWORK_TYPE: "
//					+ ralleIdItems[0]);
		} else {
			snd.put(SOCIAL_NETWORK_TYPE, "Unknown");
//			Log.i("random", "parseSNData, SOCIAL_NETWORK_TYPE: Unknown");
		}

		if (ralleIdItems.length > 1) {
			snd.put(SOCIAL_NETWORK_ID, ralleIdItems[1]);
//			Log.i("random", "parseSNData, SOCIAL_NETWORK_ID: "
//					+ ralleIdItems[1]);
		} else {
			snd.put(SOCIAL_NETWORK_ID, "Unknown");
//			Log.i("random", "parseSNData, SOCIAL_NETWORK_ID: Unknown");
		}
		return snd;
	}

	/**
	 * @param userFBId
	 *            Facebook id of a user on whose wall to post
	 * @param mode
	 *            type of post, "link" or "post"
	 * @param message
	 *            Text to post
	 * @param caption
	 *            Caption, for link only
	 * @param picture
	 *            Url of a picture, for link only
	 * @param name
	 *            Name, for link only
	 * @param link
	 *            url of a link
	 */
	public static void sendFBpost(String userFBId, String mode, String message,
			String caption, String picture, String name, String link) {

		Bundle params = new Bundle();
		String graphAPImethod = "/feed";

		if (mode.equals("link")) {
			params.putString("description", message);
			params.putString("caption", caption);
			params.putString("picture", picture);
			params.putString("name", name);
			params.putString("link", link);

		} else if (mode.equals("post")) {
			params.putString("message", message);

		} else if (mode.equals("talk")) {
			String fbLocale = RalleeApp.getInstance().getFBLocale()
					.substring(0, 2);
			// params.putString("talk", "http://rall.ee/"+fbLocale+"/talk.htm");
			params.putString("talk", "http://rall.ee/talk1.htm");
			// params.putString("tags", with);
			graphAPImethod = "/gorallee:have";
		}

		try {
			Session session = Session.getActiveSession();
			Request request = new Request(session, userFBId + graphAPImethod,
					params, HttpMethod.POST);
			Response response = Request.executeAndWait(request);
			// if (response.getError() == null) {
			//
			// GraphObject data = response.getGraphObject();
			// JSONArray friendUsersArray = ((JSONArray) data.asMap().get(
			// "data"));
			// }
		} catch (FacebookException fbe) {
			// TODO Auto-generated catch block
			fbe.printStackTrace();
		}
	}
}
