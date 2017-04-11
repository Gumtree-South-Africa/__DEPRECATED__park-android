package com.ebay.park.requests;

import android.util.Pair;

import com.ebay.park.responses.BaseParkResponse;
import com.ebay.park.responses.SignupAddPicResponse;
import com.ebay.park.utils.Constants;
import com.google.gson.JsonObject;
import com.octo.android.robospice.persistence.DurationInMillis;

/**
 * Request to update user profile picture.
 * 
 * @author federico.perez
 * 
 */
public class SignupAddPicRequest extends BaseParkSessionRequest<SignupAddPicResponse> {

	private String mUsername;
	private String mUrl;

	public SignupAddPicRequest(String username, String url) {
		super(SignupAddPicResponse.class);
		this.mUsername = username;
		this.mUrl = url;
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
		JsonObject obj = new JsonObject();
		obj.addProperty("url", mUrl);
		return obj.toString();
	}

	@Override
	protected String getUrlFormat() {
		return String.format(ParkUrls.PROFILE_ADD_PICTURE, getApiUri(), mUsername);
	}

	@Override
	protected Method getMethod() {
		return Method.PUT;
	}

	@Override
	protected SignupAddPicResponse getRequestModel(BaseParkResponse response) {
		return getGson().fromJson(response.getData().toString(), SignupAddPicResponse.class);
	}

	@Override
	protected Pair<String, String>[] getQueryParameters() {
		return null;
	}

}
