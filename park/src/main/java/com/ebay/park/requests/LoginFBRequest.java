package com.ebay.park.requests;

import android.text.TextUtils;
import android.util.Pair;

import com.ebay.park.model.LoginModel;
import com.ebay.park.responses.BaseParkResponse;
import com.ebay.park.utils.Constants;
import com.octo.android.robospice.persistence.DurationInMillis;

import java.util.HashMap;
import java.util.Map;

public class LoginFBRequest extends BaseParkHttpRequest<LoginModel> {

	private static final String FB_TOKEN = "fb_token";
	private static final String FB_ID = "fb_user_id";
	private static final String DEVICE = "device";
	private static final String DEVICE_ID = "deviceId";
	private static final String DEVICE_TYPE = "deviceType";
	private static final String DEVICE_ID_UNIQUE = "uniqueDeviceId";

	private Map<String, Object> mParams;

	public LoginFBRequest(String facebookToken, String facebookId, String deviceId, String uniqueDeviceId) {
		super(LoginModel.class);
		mParams = new HashMap<String, Object>();
		mParams.put(FB_TOKEN, facebookToken);
		mParams.put(FB_ID, facebookId);

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
		return getGson().toJson(mParams);
	}

	@Override
	protected String getUrlFormat() {
		return String.format(ParkUrls.LOGIN_FACEBOOK, getApiUri());
	}

	@Override
	protected Method getMethod() {
		return Method.POST;
	}

	@Override
	protected LoginModel getRequestModel(BaseParkResponse response) {
		String json = getGson().toJson(response.getData());
		return getGson().fromJson(json, LoginModel.class);
	}

	@Override
	protected Pair<String, String>[] getQueryParameters() {
		return null;
	}

}
