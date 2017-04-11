package com.ebay.park.requests;

import android.text.TextUtils;
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
 * Social Networks Connect Request
 * 
 * @author Nicolás Matias Fernández
 * 
 */
public class SocialConnectRequest extends BaseParkSessionRequest<Boolean> {

	private static final String SOCIAL_NETWORK = "social_network";
	private static final String SOCIAL_TOKEN = "social_token";
	private static final String SOCIAL_SECRECT_TOKEN = "social_token_secret";
	private static final String SOCIAL_USER_ID = "social_user_id";
	private String mUsername;

	private Map<String, Object> mParams;

	public SocialConnectRequest(String socialNetwork, String token, String secretToken, String userId) {
		super(Boolean.class);

		this.mUsername = ParkApplication.getInstance().getUsername();
		mParams = new HashMap<String, Object>();
		mParams.put(SOCIAL_NETWORK, socialNetwork);
		mParams.put(SOCIAL_TOKEN, token);
		if (!TextUtils.isEmpty(secretToken)) {
			mParams.put(SOCIAL_SECRECT_TOKEN, secretToken);
		}
		mParams.put(SOCIAL_USER_ID, userId);

	}

	@Override
	protected String getUrlFormat() {
		return String.format(ParkUrls.SOCIAL_NETWORKS_CONNECT, getApiUri(), mUsername);
	}

	@Override
	protected Method getMethod() {
		return Method.POST;
	}

	@Override
	protected Pair<String, String>[] getQueryParameters() {
		return null;
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
		return getGson().toJson(mParams);
	}

	@Override
	protected Boolean getRequestModel(BaseParkResponse response) {
		String json = getGson().toJson(response.getData());
		JsonElement jelement = new JsonParser().parse(json);
		JsonObject jobject = jelement.getAsJsonObject();
		return jobject.get("success").getAsBoolean();
	}

	@Override
	protected String getContentType() {
		return Constants.APPLICATION_JSON;
	}

}
