package com.radiorunt.utilities;

import org.apache.http.ConnectionClosedException;
import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class RRHttpHandler {

	private static int mTimeoutConnection = 30000;
	private static int mTimeoutSocket = 60000;

	public static final int GET_METHOD = 0;
	public static final int POST_METHOD = 1;
	public static final int PUT_METHOD = 2;
	public static final int DELETE_METHOD = 3;

	private static HttpParams GetMyHttpParameters() {
		HttpParams httpParameters = new BasicHttpParams();

		HttpConnectionParams.setConnectionTimeout(httpParameters,
				mTimeoutConnection);
		HttpConnectionParams.setSoTimeout(httpParameters, mTimeoutSocket);
		return httpParameters;
	}

	private static HttpClient GetMyHttpClient() {
		HttpParams params = GetMyHttpParameters();
		SchemeRegistry registry = new SchemeRegistry();
		registry.register(new Scheme("https", PlainSocketFactory
				.getSocketFactory(), 443));
		registry.register(new Scheme("http", PlainSocketFactory
				.getSocketFactory(), 80));
		ClientConnectionManager cm = new ThreadSafeClientConnManager(params,
				registry);
		HttpClient client = new DefaultHttpClient(cm, params);
		return client;
	}

	public static boolean isOnline(Context context) {
		ConnectivityManager cm = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = cm.getActiveNetworkInfo();
		if (netInfo != null && netInfo.isConnected()) {
			return true;
		}
		return false;
	}

	static boolean IsResponseValid(HttpResponse response) {
		int statusCode = response.getStatusLine().getStatusCode();
		if (statusCode == 200 || statusCode == 201)
			return true;
		else
			return false;
	}

	static boolean IsTokenExpired(HttpResponse response) {
		Header header = response.getFirstHeader("WWW-Authenticate");
		if (header != null) {
			HeaderElement[] headerElements = header.getElements();
			for (HeaderElement hElement : headerElements) {
				if (hElement.getName().equals("error")) {
					if (hElement.getValue().equals("invalid_token"))
						return true;
				}
			}
		}
		return false;
	}

	static void HandleUnexpectedHttpError(HttpResponse response)
			throws Exception {
		int statusCode = response.getStatusLine().getStatusCode();
		String reason = response.getStatusLine().getReasonPhrase();
		throw new Exception("Trouble reading status(code=" + statusCode + "):"
				+ reason);
	}

	public static String GenericHttpMethod(Context context, int operation,
			String serviceUrl, String secToken) throws Exception {
		return GenericHttpMethod(context, operation, serviceUrl, secToken, null);
	}

	public static String GenericHttpMethod(Context context, int operation,
			String serviceUrl, String secToken, String jsonMessageBody)
			throws Exception {

		if (!isOnline(context))
			throw new ConnectionClosedException(
					"No internet connection, please try later");
		String json = null;
		HttpClient client = GetMyHttpClient();
		HttpResponse response = null;

		StringEntity se = null;
		if (jsonMessageBody != null) {
			se = new StringEntity(jsonMessageBody, HTTP.UTF_8);
			se.setContentEncoding(new BasicHeader(HTTP.UTF_8/* CONTENT_TYPE */,
					"application/json"));
		}

		switch (operation) {
		case GET_METHOD:
			HttpGet get = new HttpGet(serviceUrl);
			get.addHeader("Authorization", "Bearer " + secToken);
			response = client.execute(get);
			break;
		case POST_METHOD:
			HttpPost post = new HttpPost(serviceUrl);
			post.addHeader("Authorization", "Bearer " + secToken);
			if (se != null) {
				post.addHeader("Content-type", "application/json");
				post.setEntity(se);
			}
			response = client.execute(post);
			break;
		case PUT_METHOD:
			HttpPut put = new HttpPut(serviceUrl);
			put.addHeader("Authorization", "Bearer " + secToken);
			if (se != null) {
				put.addHeader("Content-type", "application/json");
				put.setEntity(se);
			}
			response = client.execute(put);
			break;
		case DELETE_METHOD:
			HttpDelete delete = new HttpDelete(serviceUrl);
			delete.addHeader("Authorization", "Bearer " + secToken);
			response = client.execute(delete);
			break;
		}

		if (IsResponseValid(response)) {
			HttpEntity entity = response.getEntity();
			json = EntityUtils.toString(entity, HTTP.UTF_8);
		} else {
			if (IsTokenExpired(response)) {
				throw new TokenExpiredException("Token expired");
				// InvalidateToken(context, secToken);
				// return GetStatusesStream(context,
				// account,userId,pageNo,pageSize,lastStatusId);
			} else {
				HandleUnexpectedHttpError(response);
			}
		}

		return json;
	}

}
