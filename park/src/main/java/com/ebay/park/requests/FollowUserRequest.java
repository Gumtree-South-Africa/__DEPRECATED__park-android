package com.ebay.park.requests;

import android.util.Pair;

import com.ebay.park.ParkApplication;
import com.ebay.park.responses.BaseParkResponse;
import com.ebay.park.utils.Constants;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.octo.android.robospice.persistence.DurationInMillis;

import java.util.HashMap;
import java.util.Map;

/**
 * Request to follow a user.
 * 
 * @author federico.perez
 * 
 */
public class FollowUserRequest extends BaseParkSessionRequest<Boolean> {

	private String mUserToFollow;

	public FollowUserRequest(String userToFollow) {
		super(Boolean.class);
		this.mUserToFollow = userToFollow;
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
	protected String getBody() {
		Map<String, String> params = new HashMap<String, String>();
		params.put("userToFollow", mUserToFollow);
		return getGson().toJson(params);
	}

	@Override
	protected String getUrlFormat() {
		return String.format(ParkUrls.FOLLOW_USER, getApiUri(), ParkApplication.getInstance().getUsername());
	}

	@Override
	protected Method getMethod() {
		return Method.PUT;
	}

	@Override
	protected Boolean getRequestModel(BaseParkResponse response) {
		String json = getGson().toJson(response.getData());
		JsonElement jelement = new JsonParser().parse(json);
		JsonObject jobject = jelement.getAsJsonObject();
		return jobject.get("successfull").getAsBoolean();
	}

	@Override
	protected Pair<String, String>[] getQueryParameters() {
		return null;
	}

}
