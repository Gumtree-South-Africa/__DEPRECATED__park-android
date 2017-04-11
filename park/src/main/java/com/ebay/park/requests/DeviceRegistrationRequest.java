package com.ebay.park.requests;

import android.text.TextUtils;
import android.util.Pair;

import com.ebay.park.responses.BaseParkResponse;
import com.ebay.park.utils.Constants;
import com.octo.android.robospice.persistence.DurationInMillis;

import java.util.HashMap;
import java.util.Map;

/**
 * Request to register device
 *
 * @author Nicolás Matias Fernández
 */
public class DeviceRegistrationRequest extends BaseParkHttpRequest<String> {

    private static final String DEVICE_ID = "deviceId";
    private static final String DEVICE_TYPE = "deviceType";
    private static final String DEVICE_ID_UNIQUE = "uniqueDeviceId";

    private Map<String, Object> mParams;

    public DeviceRegistrationRequest(String deviceId, String uniqueDeviceId) {
        super(String.class);
        mParams = new HashMap<String, Object>();

        if (!TextUtils.isEmpty(uniqueDeviceId)) {
            if (!TextUtils.isEmpty(deviceId)) {
                mParams.put(DEVICE_ID, deviceId);
                mParams.put(DEVICE_TYPE, Constants.DEVICE_TYPE);
            }
            mParams.put(DEVICE_ID_UNIQUE, uniqueDeviceId);
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
        return String.format(ParkUrls.REGISTER_DEVICE, getApiUri());
    }

    @Override
    protected Method getMethod() {
        return Method.POST;
    }

    @Override
    protected String getRequestModel(BaseParkResponse response) {
        return getGson().fromJson(response.getData().toString(), String.class);
    }

    @Override
    protected Pair<String, String>[] getQueryParameters() {
        return null;
    }

}

