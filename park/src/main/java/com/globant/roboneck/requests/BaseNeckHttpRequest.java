package com.globant.roboneck.requests;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.util.Pair;

import com.ebay.park.ParkApplication;
import com.octo.android.robospice.request.okhttp.OkHttpSpiceRequest;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URLEncoder;
import java.security.GeneralSecurityException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

@SuppressLint("TrulyRandom")
public abstract class BaseNeckHttpRequest<T, R> extends OkHttpSpiceRequest<T> {

	protected static final String ENCODING = "UTF-8";

	protected enum Method {
		GET, POST, PUT, DELETE
	}

	public BaseNeckHttpRequest(Class<T> clazz) {
		super(clazz);
	}

	public abstract Object getCachekey();

	public abstract long getCacheExpirationTime();

	protected abstract String getBody();

	protected abstract Map<String, String> getHeaders();

	protected abstract String getUrl();

	protected abstract Method getMethod();

	protected abstract R processContent(String responseBody);

	protected abstract boolean isLogicError(R response);

	protected abstract T getRequestModel(R response);

	protected abstract BaseNeckRequestException.Error processError(int httpStatus, R response, String responseBody);

	protected abstract Pair<String, String>[] getQueryParameters();

//	private static TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
//		@Override
//		public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType)
//				throws CertificateException {
//		}
//
//		@Override
//		public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType)
//				throws CertificateException {
//
//		}
//
//		@Override
//		public java.security.cert.X509Certificate[] getAcceptedIssuers() {
//			return null;
//		}
//	} };

	private static SSLContext sslContext;
	private static SSLSocketFactory sslFactory;
    private static TrustManagerFactory tmf;

	static {
		if (sslContext == null) {
			try {
                try {
                    getSSLConnection();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                sslContext = SSLContext.getInstance("TLS");
				try {
					sslContext.init(null, tmf.getTrustManagers(), new java.security.SecureRandom());
					sslFactory = sslContext.getSocketFactory();
				} catch (KeyManagementException e) {
					e.printStackTrace();
				}
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			}
		}
	}

    protected static void getSSLConnection() throws IOException  {
        try {
            final KeyStore trustStore = KeyStore.getInstance("BKS");
            final InputStream in = ParkApplication.getInstance().getApplicationContext().
                    getResources().openRawResource(com.ebay.park.R.raw.park_prod_ssl_trust_cert);
            trustStore.load(in, null);

            tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(trustStore);

        } catch (GeneralSecurityException e) {
            throw new IOException("Could not connect to SSL Server", e);
        }
    }

	// Override this method if more configuration is needed for the Connection
	@SuppressLint("TrulyRandom")
	protected HttpURLConnection getConnection(URI uri) throws MalformedURLException {
        if (uri.getHost().equals("rest.vivanuncios.us")) {
            getOkHttpClient().setSslSocketFactory(sslFactory);
        } else {
			getOkHttpClient().setSslSocketFactory(null);
        }
		HttpURLConnection conn = getOkHttpClient().open(uri.toURL());
		final Map<String, String> headers = getHeaders();
		for (String header : headers.keySet()) {
			conn.addRequestProperty(header, headers.get(header));
		}
		return conn;
	}

	@SuppressWarnings("unchecked")
	@Override
	public T loadDataFromNetwork() throws BaseNeckRequestException {
		try {
			Uri.Builder uriBuilder = Uri.parse(getUrl()).buildUpon();

			Pair<String, String>[] params = getQueryParameters();
			if (params != null) {
				for (Pair<String, String> pair : params) {

					uriBuilder.appendQueryParameter(URLEncoder.encode(pair.first, ENCODING),
							URLEncoder.encode(pair.second, ENCODING));
				}
			}

			URI uri;
			uri = new URI(uriBuilder.build().toString().replace("%25", "%"));

			HttpURLConnection connection = getConnection(uri);

			switch (getMethod()) {
			case GET:
				connection.setRequestMethod("GET");
				break;
			case POST:
				connection.setRequestMethod("POST");
				writeBody(connection.getOutputStream());
				break;
			case PUT:
				connection.setRequestMethod("PUT");
				writeBody(connection.getOutputStream());
				break;
			case DELETE:
				connection.setRequestMethod("DELETE");
			default:
				break;
			}

			BaseNeckRequestException.Error e = null;
			R r = null;
			T t = null;

			InputStream in = null;
			int responseCode = connection.getResponseCode();
			if (responseCode >= 400) {
				in = connection.getErrorStream();
			} else {
				in = connection.getInputStream();
			}

			String body = IOUtils.toString(in, ENCODING);

			if (ParkApplication.sParkConfiguration.logRequests()) {
				writeMicroLog(uri, body);
			}

			if (responseCode == 200 || responseCode == 201) {
				r = processContent(body);
				if (isLogicError(r)) {
					e = processError(responseCode, r, body);
				} else {
					t = getRequestModel(r);
				}
			} else if (responseCode >= 400 && responseCode <= 600) {
				setRetryPolicy(null);
				e = processError(responseCode, null, body);
			} else {
				e = processError(responseCode, null, body);
			}

			if (in != null) {
				in.close();
			}

			if (e != null) {
				return (T) e;
			}

			return t;

		} catch (Exception e1) {
			throw new BaseNeckRequestException(e1);
		}
	}

	private void writeBody(OutputStream stream) throws UnsupportedEncodingException, IOException {
		String body = getBody();
		if (body != null && !body.isEmpty()) {
			stream.write(body.getBytes(ENCODING));
			stream.close();
		}
	}

	private void writeMicroLog(URI uri, String body) {

		String finalLog;
		String requestHeaders = "";
		final Map<String, String> headers = getHeaders();
		if (headers != null) {
			for (String header : headers.keySet()) {
				requestHeaders = requestHeaders + header + ": " + headers.get(header) + ", ";
			}
		} else {
			requestHeaders = "N/A";
		}
		String requestBody = getBody() == null ? "N/A" : getBody();
		String divider = "\n**************************\n";

		finalLog = divider + "* Request:\n-URL:" + uri + "\n-Type:" + getMethod() + "\n-Headers:" + requestHeaders
				+ "\n-Body Params:" + requestBody + "\n\n* Response:\n" + body + divider;

		ParkApplication.LOGGER.info(finalLog);
	}
}
