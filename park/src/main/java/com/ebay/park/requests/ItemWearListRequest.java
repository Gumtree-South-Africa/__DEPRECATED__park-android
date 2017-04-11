package com.ebay.park.requests;

import android.text.TextUtils;
import android.util.Pair;

import com.ebay.park.ParkApplication;
import com.ebay.park.responses.BaseParkResponse;
import com.ebay.park.responses.ItemWearListResponse;
import com.ebay.park.utils.Constants;
import com.octo.android.robospice.persistence.DurationInMillis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by paula.baudo on 8/1/2016.
 */
public class ItemWearListRequest extends BaseParkSessionRequest<ItemWearListResponse> {

    private Map<String, String> mFilters;

    private ItemWearListRequest(Builder builder) {
        super(ItemWearListResponse.class);
        mFilters = builder.params;
    }

    @Override
    protected String getContentType() {
        return Constants.APPLICATION_JSON;
    }

    @Override
    public Object getCachekey() {
        String key = "itemList";
        for (String param : mFilters.keySet())
            key += mFilters.get(param);
        return key;
    }

    @Override
    public long getCacheExpirationTime() {
        return DurationInMillis.ONE_SECOND * 30;
    }

    @Override
    protected String getBody() {
        return null;
    }

    @Override
    protected String getUrlFormat() {
        if (ParkApplication.getInstance().getSessionToken() != null) {
            return String.format(ParkUrls.SEARCH_ITEMS, getApiUri());
        } else {
            return String.format(ParkUrls.PUBLIC_SEARCH_ITEMS, getApiUri());
        }
    }

    @Override
    protected Method getMethod() {
        return Method.GET;
    }

    @Override
    protected ItemWearListResponse getRequestModel(BaseParkResponse response) {
        String json = getGson().toJson(response.getData());
        return getGson().fromJson(json, ItemWearListResponse.class);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected Pair<String, String>[] getQueryParameters() {
        List<Pair<String, String>> list = new ArrayList<Pair<String, String>>();
        for (String param : mFilters.keySet())
            list.add(Pair.create(param, mFilters.get(param)));
        return list.toArray(new Pair[1]);
    }

    public static class Builder {
        public static final String PAGE = "page";
        public static final String PAGE_SIZE = "pageSize";
        public static final String QUERY = "q";
        public static final String MAX_DISTANCE = "radius";
        public static final String PRICE_FROM = "priceFrom";
        public static final String PRICE_TO = "priceTo";
        public static final String CATEGORY = "categoryId";
        public static final String ORDER = "order";
        public static final String LATITUDE = "latitude";
        public static final String LONGITUDE = "longitude";


        private Map<String, String> params;

        public Builder() {
            this.params = new HashMap<String, String>();
        }

        public Builder categoryId(String val) {
            if (val != null)
                params.put(CATEGORY, val);
            return this;
        }

        public Builder query(String query) {
            if (!TextUtils.isEmpty(query))
                params.put(QUERY, query);
            return this;
        }

        public Builder maxDistance(String distance) {
            if (distance != null)
                params.put(MAX_DISTANCE, distance);
            return this;
        }

        public Builder priceFrom(String val) {
            if (val != null)
                params.put(PRICE_FROM, val);
            return this;
        }

        public Builder priceTo(String val) {
            if (val != null)
                params.put(PRICE_TO, val);
            return this;
        }

        public Builder page(Long page) {
            if (page != null)
                params.put(PAGE, String.valueOf(page));
            return this;
        }

        public Builder pageSize(Long pageSize) {
            if (pageSize != null)
                params.put(PAGE_SIZE, String.valueOf(pageSize));
            return this;
        }

        public Builder order(String order) {
            if (!TextUtils.isEmpty(order))
                params.put(ORDER, order);
            return this;
        }

        public Builder withPos(double latitude, double longitude) {
            params.put(LATITUDE, String.valueOf(latitude));
            params.put(LONGITUDE, String.valueOf(longitude));
            return this;
        }

        public ItemWearListRequest build() {
            return new ItemWearListRequest(this);
        }

    }
}
