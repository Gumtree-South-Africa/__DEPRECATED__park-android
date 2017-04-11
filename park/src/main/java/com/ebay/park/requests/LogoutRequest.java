package com.ebay.park.requests;

import android.util.Pair;

import com.ebay.park.model.LogoutModel;
import com.ebay.park.responses.BaseParkResponse;
import com.ebay.park.utils.Constants;
import com.google.gson.JsonObject;
import com.octo.android.robospice.persistence.DurationInMillis;

/**
 * Logout request.
 * 
 * @author federico.perez
 * 
 */
public class LogoutRequest extends BaseParkSessionRequest<LogoutModel> {

	private String mLang;
	private String mDeviceId;

	public LogoutRequest(String deviceId) {
		super(LogoutModel.class);
		this.mDeviceId = deviceId;
	}

	@Override
	public Object getCachekey() {
		return null;
	}

	@Override
	public long getCacheExpirationTime() {
		return DurationInMillis.ALWAYS_EXPIRED;
	}

	@Override
	protected String getBody() {
		JsonObject object = new JsonObject();
		if (mLang != null) {
			object.addProperty("lang", mLang);
		}
		object.addProperty("deviceType", Constants.DEVICE_TYPE);
		object.addProperty("deviceId", mDeviceId);
		return object.toString();
	}

	@Override
	protected String getUrlFormat() {
		return String.format(ParkUrls.LOGOUT, getApiUri());
	}

	@Override
	protected Method getMethod() {
		return Method.POST;
	}

	@Override
	protected LogoutModel getRequestModel(BaseParkResponse response) {
		return getGson().fromJson(response.getData().toString(), LogoutModel.class);
	}

	@Override
	protected Pair<String, String>[] getQueryParameters() {
		return null;
	}

	@Override
	protected String getContentType() {
		return Constants.APPLICATION_JSON;
	}

}
