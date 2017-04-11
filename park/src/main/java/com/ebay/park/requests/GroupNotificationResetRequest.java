package com.ebay.park.requests;

import android.util.Pair;

import com.ebay.park.responses.BaseParkResponse;
import com.ebay.park.utils.Constants;
import com.octo.android.robospice.persistence.DurationInMillis;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by jonatan on 21/08/15.
 */
public class GroupNotificationResetRequest extends BaseParkSessionRequest<String> {

    private static final String GROUP_ID_PARAM = "groupId";
    private long mGroupId;

    public GroupNotificationResetRequest(long groupId) {
        super(String.class);
        this.mGroupId = groupId;
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
        Map<String, String> params = new HashMap<String, String>();
        params.put(GROUP_ID_PARAM, Long.toString(mGroupId));
        return getGson().toJson(params);
    }

    @Override
    protected String getUrlFormat() {
        return String.format(ParkUrls.RESET_GROUP_NOTIFICATION_COUNTER, getApiUri());
    }

    @Override
    protected Method getMethod() {
        return Method.POST;
    }

    @Override
    protected String getRequestModel(BaseParkResponse response) {
        String json = getGson().toJson(response.getData());
        return json;
    }

    @Override
    protected Pair<String, String>[] getQueryParameters() {
        return null;
    }

}

