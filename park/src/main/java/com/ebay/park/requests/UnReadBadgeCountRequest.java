package com.ebay.park.requests;

import android.util.Pair;

import com.ebay.park.ParkApplication;
import com.ebay.park.model.UnreadBadgeCountModel;
import com.ebay.park.model.UnreadCountModel;
import com.ebay.park.responses.BaseParkResponse;
import com.ebay.park.utils.Constants;
import com.octo.android.robospice.persistence.DurationInMillis;

public class UnReadBadgeCountRequest extends BaseParkSessionRequest<UnreadBadgeCountModel> {

    public UnReadBadgeCountRequest() {
        super(UnreadBadgeCountModel.class);
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
        return String.format(ParkUrls.UNREAD_FEEDS_ONLY_COUNTER, getApiUri(), ParkApplication.getInstance().getUsername());
    }

    @Override
    protected Method getMethod() {
        return Method.GET;
    }

    @Override
    protected UnreadBadgeCountModel getRequestModel(BaseParkResponse response) {
        String json = getGson().toJson(response.getData());
        return getGson().fromJson(json, UnreadBadgeCountModel.class);
    }

    @Override
    protected Pair<String, String>[] getQueryParameters() {
        return null;
    }

}
