package com.ebay.park.requests;

import android.net.Uri;
import android.util.Pair;

import com.ebay.park.errors.GenericError;
import com.ebay.park.responses.BaseParkResponse;
import com.ebay.park.responses.ResponseCodes;
import com.globant.roboneck.common.Multipart;
import com.globant.roboneck.requests.BaseNeckRequestException;
import com.globant.roboneck.requests.BaseNeckRequestException.Error;

import org.apache.commons.io.IOUtils;

import java.io.*;
import java.net.*;
import java.util.UUID;

/**
 * POST multipart/form-data.
 * 
 * @author federico.perez
 * 
 * @param <T>
 */
public abstract class BaseParkMultipartRequest<T> extends BaseParkSessionRequest<T> {
	/**
	 * Boundary used in the requests.
	 */
	private static final String BOUNDARY = UUID.randomUUID().toString();

	public BaseParkMultipartRequest(Class<T> clazz) {
		super(clazz);
	}

	@Override
	protected final String getContentType() {
		return "multipart/form-data;boundary=" + BOUNDARY;
	}

	@Override
	protected final String getBody() {
		throw new UnsupportedOperationException("Body is defined by multipart.");
	}

	@Override
	protected final Method getMethod() {
		throw new UnsupportedOperationException("Method is always POST");
	}

	@Override
	protected BaseParkResponse processContent(String responseBody) {
		return getGson().fromJson(responseBody, BaseParkResponse.class);
	}

	@Override
	protected boolean isLogicError(BaseParkResponse response) {
		return response.getStatusCode() != ResponseCodes.SUCCESS_CODE;
	}

	@Override
	protected Error processError(int httpStatus, BaseParkResponse response, String responseBody) {
		if (response != null) {
			return new GenericError(response.getStatusCode(), response.getErrorCode(), response.getStatusMessage());
		}

		// TODO: implement this well
		return new GenericError(httpStatus, httpStatus, responseBody);
	}

	/**
	 * Builds {@link Multipart} request. The request must use the provided
	 * boundary.
	 * 
	 * @param boundary
	 *            Use this boundary to build the request, this value will be
	 *            sent in the header.
	 */
	protected abstract Multipart getMultipartBody(String boundary);

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
			uri = new URI(uriBuilder.build().toString());

			HttpURLConnection connection = getConnection(uri);
			connection.setRequestMethod("POST");

			getMultipartBody(BOUNDARY).writeBodyTo(connection.getOutputStream());

			BaseNeckRequestException.Error e = null;
			BaseParkResponse r = null;
			T t = null;

			InputStream in = null;
			int responseCode = connection.getResponseCode();
			if (responseCode >= 400) {
				in = connection.getErrorStream();
			} else {
				in = connection.getInputStream();
			}

			String body = IOUtils.toString(in, ENCODING);

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

		} catch (NullPointerException e1) {
			throw new BaseNeckRequestException(e1);
		} catch (UnsupportedEncodingException e1) {
			throw new BaseNeckRequestException(e1);
		} catch (URISyntaxException e1) {
			throw new BaseNeckRequestException(e1);
		} catch (MalformedURLException e1) {
			throw new BaseNeckRequestException(e1);
		} catch (ProtocolException e1) {
			throw new BaseNeckRequestException(e1);
		} catch (IOException e1) {
			throw new BaseNeckRequestException(e1);
		}
	}
}
