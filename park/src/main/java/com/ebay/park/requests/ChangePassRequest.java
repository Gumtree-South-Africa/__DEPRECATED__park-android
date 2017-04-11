package com.ebay.park.requests;

import android.util.Pair;

import com.ebay.park.responses.BaseParkResponse;
import com.ebay.park.utils.Constants;
import com.octo.android.robospice.persistence.DurationInMillis;

import java.util.HashMap;
import java.util.Map;

public class ChangePassRequest extends BaseParkSessionRequest<String> {

	public static final String NEW_PASSWORD = "newPassword";
	public static final String CURRENT_PASSWORD = "currentPassword";

	private Map<String, String> mBody;

	public ChangePassRequest(String newPass, String currentPass) {
		super(String.class);
		mBody = new HashMap<String, String>();
		mBody.put(NEW_PASSWORD, newPass);
		mBody.put(CURRENT_PASSWORD, currentPass);
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
		return String.format(ParkUrls.CHANGE_PASS, getApiUri());
	}

	@Override
	protected Method getMethod() {
		return Method.POST;
	}

	@Override
	protected String getRequestModel(BaseParkResponse response) {
        return "nothing";
	}

	@Override
	protected Pair<String, String>[] getQueryParameters() {
		return null;
	}

}
