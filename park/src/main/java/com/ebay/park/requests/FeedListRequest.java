package com.ebay.park.requests;

import android.util.Pair;

import com.ebay.park.responses.BaseParkResponse;
import com.ebay.park.responses.FeedListResponse;
import com.ebay.park.utils.Constants;
import com.google.gson.Gson;
import com.octo.android.robospice.persistence.DurationInMillis;

public class FeedListRequest extends BaseParkSessionRequest<FeedListResponse> {

	private String mUsername;

	public FeedListRequest(String username) {
		super(FeedListResponse.class);
		this.mUsername = username;
	}

	@Override
	protected String getContentType() {
		return Constants.APPLICATION_JSON;
	}

	@Override
	public Object getCachekey() {
		return "feed";
	}

	@Override
	public long getCacheExpirationTime() {
		return DurationInMillis.ONE_SECOND * 5;
	}

	@Override
	protected String getBody() {
		return null;
	}

	@Override
	protected String getUrlFormat() {
		return String.format(ParkUrls.ACTIVITY_FEED, getNewApiUri(), mUsername);
	}

	@Override
	protected Method getMethod() {
		return Method.GET;
	}

	@Override
	protected FeedListResponse getRequestModel(BaseParkResponse response) {
		final Gson gson = getGson();
		String json = gson.toJson(response.getData());
		return gson.fromJson(json, FeedListResponse.class);
	}

	@Override
	protected Pair<String, String>[] getQueryParameters() {
		return null;
	}

}
