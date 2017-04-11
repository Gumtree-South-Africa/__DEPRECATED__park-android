package com.ebay.park.requests;

import android.util.Pair;

import com.ebay.park.responses.BaseParkResponse;
import com.ebay.park.responses.UserListResponse;
import com.ebay.park.utils.Constants;
import com.octo.android.robospice.persistence.DurationInMillis;

public class FacebookFriendsRequest extends BaseParkSessionRequest<UserListResponse> {

	private String mUsername;

	public FacebookFriendsRequest(String username) {
		super(UserListResponse.class);
		this.mUsername = username;
	}

	@Override
	protected String getContentType() {
		return Constants.APPLICATION_JSON;
	}

	@Override
	public Object getCachekey() {
		return "fb friends" + mUsername;
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
		return String.format(ParkUrls.FACEBOOK_FRIENDS, getApiUri(), mUsername);
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