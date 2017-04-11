package com.ebay.park.requests;

import android.text.TextUtils;

import com.ebay.park.ParkApplication;

import java.util.Map;

/**
 * Base Request for authenticated end points.
 * 
 * @author federico.perez
 * 
 * @param <T>
 */
public abstract class BaseParkSessionRequest<T> extends BaseParkHttpRequest<T> {

	private static final String TOKEN_HEADER = "token";

	public BaseParkSessionRequest(Class<T> clazz) {
		super(clazz);
	}

	@Override
	protected Map<String, String> getHeaders() {
		Map<String, String> headers = super.getHeaders();
		if (!TextUtils.isEmpty(ParkApplication.getInstance().getSessionToken())) {
			headers.put(TOKEN_HEADER, ParkApplication.getInstance().getSessionToken());
		}
		return headers;
	}

}
