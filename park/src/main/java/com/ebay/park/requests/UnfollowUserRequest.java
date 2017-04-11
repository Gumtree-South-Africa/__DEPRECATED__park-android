package com.ebay.park.requests;

import android.util.Pair;

import com.ebay.park.ParkApplication;
import com.ebay.park.responses.BaseParkResponse;
import com.ebay.park.utils.Constants;
import com.octo.android.robospice.persistence.DurationInMillis;

import java.util.HashMap;
import java.util.Map;

/**
 * Request to un-follow a user.
 * 
 * @author federico.perez
 * 
 */
public class UnfollowUserRequest extends BaseParkSessionRequest<Boolean> {

	private String mUserToUnfollow;

	public UnfollowUserRequest(String userToUnFollow) {
		super(Boolean.class);
		this.mUserToUnfollow = userToUnFollow;
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
		params.put("userToUnfollow", mUserToUnfollow);
		return getGson().toJson(params);
	}

	@Override
	protected String getUrlFormat() {
		return String.format(ParkUrls.UNFOLLOW_USER, getApiUri(), ParkApplication.getInstance().getUsername());
	}

	@Override
	protected Method getMethod() {
		return Method.PUT;
	}

	@Override
	protected Boolean getRequestModel(BaseParkResponse response) {
		return (Boolean) response.getData();
	}

	@Override
	protected Pair<String, String>[] getQueryParameters() {
		return null;
	}

}
