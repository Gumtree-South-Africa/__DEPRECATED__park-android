package com.ebay.park.requests;

import android.util.Pair;

import com.ebay.park.ParkApplication;
import com.ebay.park.responses.BaseParkResponse;
import com.ebay.park.responses.UserListResponse;
import com.ebay.park.utils.Constants;
import com.octo.android.robospice.persistence.DurationInMillis;

public class FollowersRequest extends BaseParkSessionRequest<UserListResponse> {

	private String mUsername;

	public FollowersRequest(String username) {
		super(UserListResponse.class);
		this.mUsername = username;
	}

	@Override
	protected String getContentType() {
		return Constants.APPLICATION_JSON;
	}

	@Override
	public Object getCachekey() {
		return "followers" + mUsername;
	}

	@Override
	public long getCacheExpirationTime() {
		return DurationInMillis.ONE_MINUTE;
	}

	@Override
	protected String getBody() {
		return null;
	}

	@Override
	protected String getUrlFormat() {
		if (ParkApplication.getInstance().getSessionToken() != null) {
			return String.format(ParkUrls.FOLLOWERS, getApiUri(), mUsername);
		} else {
			return String.format(ParkUrls.PUBLIC_FOLLOWERS, getApiUri(), mUsername);
		}
	}

	@Override
	protected Method getMethod() {
		return Method.GET;
	}

	@Override
	protected UserListResponse getRequestModel(BaseParkResponse response) {
		String json = getGson().toJson(response.getData());
		return getGson().fromJson(json, UserListResponse.class);
	}

	@Override
	protected Pair<String, String>[] getQueryParameters() {
		return null;
	}

}