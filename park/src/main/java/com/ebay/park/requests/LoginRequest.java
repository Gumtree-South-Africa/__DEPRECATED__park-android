package com.ebay.park.requests;

import android.text.TextUtils;
import android.util.Pair;

import com.ebay.park.model.LoginModel;
import com.ebay.park.responses.BaseParkResponse;
import com.ebay.park.utils.Constants;
import com.octo.android.robospice.persistence.DurationInMillis;

import java.util.HashMap;
import java.util.Map;

/**
 * Park Login Request.
 * 
 * @author federico.perez
 * 
 */
public class LoginRequest extends BaseParkHttpRequest<LoginModel> {

	private static final String USERNAME = "username";
	private static final String PASSWORD = "password";
	private static final String EMAIL = "email";
	private static final String DEVICE = "device";
	private static final String DEVICE_ID = "deviceId";
	private static final String DEVICE_TYPE = "deviceType";
	private static final String DEVICE_ID_UNIQUE = "uniqueDeviceId";

	private Map<String, Object> mParams;

	public LoginRequest(String username, String password, String email, String deviceId, String uniqueDeviceId) {
		super(LoginModel.class);
		mParams = new HashMap<String, Object>();
		if (!TextUtils.isEmpty(username)) {
			mParams.put(USERNAME, username);
		}
		if (!TextUtils.isEmpty(email)) {
			mParams.put(EMAIL, email);
		}
		mParams.put(PASSWORD, password);

		if (!TextUtils.isEmpty(uniqueDeviceId)) {
			HashMap<String, String> device = new HashMap<String, String>();
			if (!TextUtils.isEmpty(deviceId)) {
				device.put(DEVICE_ID, deviceId);
				device.put(DEVICE_TYPE, Constants.DEVICE_TYPE);
			}
			device.put(DEVICE_ID_UNIQUE, uniqueDeviceId);
			mParams.put(DEVICE, device);
		}
	}

	@Override
	protected String getUrlFormat() {
		return String.format(ParkUrls.LOGIN, getApiUri4());
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
	protected LoginModel getRequestModel(BaseParkResponse response) {
		String json = getGson().toJson(response.getData());
		return getGson().fromJson(json, LoginModel.class);
	}

	@Override
	protected String getContentType() {
		return Constants.APPLICATION_JSON;
	}

}
