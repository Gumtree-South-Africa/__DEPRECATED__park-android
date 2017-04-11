package com.ebay.park.requests;

import android.util.Pair;

import com.ebay.park.ParkApplication;
import com.ebay.park.model.ProfileModel;
import com.ebay.park.responses.BaseParkResponse;
import com.ebay.park.utils.Constants;
import com.octo.android.robospice.persistence.DurationInMillis;

/**
 * Get user profile request.
 *
 * @author federico.perez
 */
public class ProfileRequest extends BaseParkSessionRequest<ProfileModel> {

    private String mUsername;

    public ProfileRequest(String username) {
        super(ProfileModel.class);
        this.mUsername = username;
    }

    @Override
    protected String getContentType() {
        return Constants.APPLICATION_JSON;
    }

    @Override
    public Object getCachekey() {
        return "GET" + mUsername;
    }

    @Override
    public long getCacheExpirationTime() {
        return DurationInMillis.ONE_SECOND;
    }

    @Override
    protected String getBody() {
        return null;
    }

    @Override
    protected String getUrlFormat() {
        if (ParkApplication.getInstance().getUsername() != null) {
            if (ParkApplication.getInstance().getUsername().equals(mUsername)) {
                return String.format(ParkUrls.UPDATE_PROFILE, getApiUri(), mUsername);
            } else {
                return String.format(ParkUrls.USER_PROFILE, getApiUri(), mUsername);
            }
        } else {
            return String.format(ParkUrls.USER_PROFILE, getApiUri(), mUsername);
        }
    }

    @Override
    protected Method getMethod() {
        return Method.GET;
    }

    @Override
    protected ProfileModel getRequestModel(BaseParkResponse response) {
        String json = getGson().toJson(response.getData());
        return getGson().fromJson(json, ProfileModel.class);
    }

    @Override
    protected Pair<String, String>[] getQueryParameters() {
        return null;
    }

}
