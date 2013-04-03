package com.radiorunt.utilities;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

public class SimpleHttp {

	public static String apacheHttp(String URL) throws ClientProtocolException,
			IOException {
		String responseString;
		HttpClient httpclient = new DefaultHttpClient();
		HttpResponse response = httpclient.execute(new HttpGet(URL));
		StatusLine statusLine = response.getStatusLine();
		if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			response.getEntity().writeTo(out);
			out.close();
			responseString = out.toString();
			// ..more logic
		} else {
			// Closes the connection.
			response.getEntity().getContent().close();
			throw new IOException(statusLine.getReasonPhrase());
		}
		return responseString;
	}

	public static String getJson(String url) throws Exception {
		HttpParams httpParameters = new BasicHttpParams();

		HttpConnectionParams.setConnectionTimeout(httpParameters, 30000);
		HttpConnectionParams.setSoTimeout(httpParameters, 60000);

		SchemeRegistry registry = new SchemeRegistry();
		registry.register(new Scheme("https", PlainSocketFactory
				.getSocketFactory(), 443));
		registry.register(new Scheme("http", PlainSocketFactory
				.getSocketFactory(), 80));
		ClientConnectionManager cm = new ThreadSafeClientConnManager(
				httpParameters, registry);
		HttpClient client = new DefaultHttpClient(cm, httpParameters);

		String json = null;
		StringEntity se = null;

		HttpGet get = new HttpGet(url);
		HttpResponse response = client.execute(get);

		int statusCode = response.getStatusLine().getStatusCode();
		if (statusCode == 200 || statusCode == 201) {
			HttpEntity entity = response.getEntity();
			json = EntityUtils.toString(entity);
		}

		return json;
	}

}
