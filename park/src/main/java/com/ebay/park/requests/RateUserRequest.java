package com.ebay.park.requests;

import android.util.Pair;

import com.ebay.park.model.RateModel.Ranking;
import com.ebay.park.responses.BaseParkResponse;
import com.ebay.park.utils.Constants;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.octo.android.robospice.persistence.DurationInMillis;

import java.util.HashMap;
import java.util.Map;

public class RateUserRequest extends BaseParkSessionRequest<Boolean> {

	Map<String, Object> mBody;

	public RateUserRequest(String comment, long userToRate, long itemId, Ranking ranking) {
		super(Boolean.class);
		mBody = new HashMap<String, Object>();
		mBody.put("userToRate", userToRate);
		mBody.put("itemId", itemId);
		mBody.put("comment", comment);
		mBody.put("ratingStatus", ranking.toString().toLowerCase());
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
		return getGson().toJson(mBody);
	}

	@Override
	protected String getUrlFormat() {
		return String.format(ParkUrls.RATE, getApiUri());
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
