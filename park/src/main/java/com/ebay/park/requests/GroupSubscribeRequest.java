package com.ebay.park.requests;

import android.util.Pair;

import com.ebay.park.responses.BaseParkResponse;
import com.ebay.park.utils.Constants;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.octo.android.robospice.persistence.DurationInMillis;

import java.util.HashMap;
import java.util.Map;

/**
 * Group Subcribe Request
 * 
 * @author Nicolás Matias Fernández
 * 
 */
public class GroupSubscribeRequest extends BaseParkSessionRequest<Boolean> {

	private long mIdGroup;

	public GroupSubscribeRequest(long idGroup) {
		super(Boolean.class);
		this.mIdGroup = idGroup;
	}

	@Override
	protected String getContentType() {
		return Constants.APPLICATION_JSON;
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
	protected Map<String, String> getHeaders() {
		Map<String, String> headers = super.getHeaders();
		headers.put("Content-Type", "application/json");
		return headers;
	}

	@Override
	protected String getBody() {
		Map<String, String> params = new HashMap<String, String>();
		return getGson().toJson(params);
	}

	@Override
	protected String getUrlFormat() {
		return String.format(ParkUrls.SUBSCRIBE_GROUP, getApiUri(), mIdGroup);
	}

	@Override
	protected Method getMethod() {
		return Method.POST;
	}

	@Override
	protected Boolean getRequestModel(BaseParkResponse response) {
		String json = getGson().toJson(response.getData());
		JsonElement jelement = new JsonParser().parse(json);
		JsonObject jobject = jelement.getAsJsonObject();
		return jobject.get("success").getAsBoolean();
	}

	@Override
	protected Pair<String, String>[] getQueryParameters() {
		return null;
	}

}
