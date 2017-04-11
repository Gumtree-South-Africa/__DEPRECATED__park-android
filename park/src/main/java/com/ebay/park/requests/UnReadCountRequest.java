package com.ebay.park.requests;

import android.util.Pair;

import com.ebay.park.ParkApplication;
import com.ebay.park.model.UnreadCountModel;
import com.ebay.park.responses.BaseParkResponse;
import com.ebay.park.utils.Constants;
import com.octo.android.robospice.persistence.DurationInMillis;

public class UnReadCountRequest extends BaseParkSessionRequest<UnreadCountModel> {

    public UnReadCountRequest() {
        super(UnreadCountModel.class);
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
        return null;
    }

    @Override
    protected String getUrlFormat() {
        return String.format(ParkUrls.UNREAD_COUNT, getApiUri(), ParkApplication.getInstance().getUsername());
    }

    @Override
    protected Method getMethod() {
        return Method.GET;
    }

    @Override
    protected UnreadCountModel getRequestModel(BaseParkResponse response) {
        String json = getGson().toJson(response.getData());
        return getGson().fromJson(json, UnreadCountModel.class);
    }

    @Override
    protected Pair<String, String>[] getQueryParameters() {
        return null;
    }

}
