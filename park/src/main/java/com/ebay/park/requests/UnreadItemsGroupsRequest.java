package com.ebay.park.requests;

import android.util.Pair;

import com.ebay.park.ParkApplication;
import com.ebay.park.model.UnreadItemsGroupsModel;
import com.ebay.park.responses.BaseParkResponse;
import com.ebay.park.utils.Constants;
import com.octo.android.robospice.persistence.DurationInMillis;

/**
 * Created by paula.baudo on 9/1/2015.
 */
public class UnreadItemsGroupsRequest extends BaseParkSessionRequest<UnreadItemsGroupsModel> {

    public UnreadItemsGroupsRequest() {
        super(UnreadItemsGroupsModel.class);
    }

    @Override
    protected String getContentType() {
        return Constants.APPLICATION_JSON;
    }

    @Override
    protected String getUrlFormat() {
        return String.format(ParkUrls.UNREAD_ITEMS_GROUPS, getApiUri(), ParkApplication.getInstance().getUsername());
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
    protected Method getMethod() {
        return Method.GET;
    }

    @Override
    protected UnreadItemsGroupsModel getRequestModel(BaseParkResponse response) {
        String json = getGson().toJson(response.getData());
        return getGson().fromJson(json, UnreadItemsGroupsModel.class);
    }

    @Override
    protected Pair<String, String>[] getQueryParameters() {
        return null;
    }
}
