package com.ebay.park.requests;

import android.text.TextUtils;
import android.util.Pair;

import com.ebay.park.ParkApplication;
import com.ebay.park.responses.BaseParkResponse;
import com.ebay.park.responses.GroupListResponse;
import com.ebay.park.utils.Constants;
import com.octo.android.robospice.persistence.DurationInMillis;

import java.util.*;

/**
 * Search Groups Request
 * 
 * @author Nicolás Matias Fernández
 * 
 */
public class GroupSearchRequest extends BaseParkSessionRequest<GroupListResponse> {

	private Map<String, String> mFilters;

	private GroupSearchRequest(Builder builder) {
		super(GroupListResponse.class);
		mFilters = builder.params;
	}

	@Override
	protected String getUrlFormat() {
		if (ParkApplication.getInstance().getSessionToken() != null) {
			return String.format(ParkUrls.SEARCH_GROUP, getApiUri());
		} else {
			return String.format(ParkUrls.PUBLIC_SEARCH_GROUP, getApiUri());
		}
	}

	@Override
	protected Method getMethod() {
		return Method.GET;
	}

	@Override
	public Object getCachekey() {
		String key = "groups";
		for (String param : mFilters.keySet())
			key += mFilters.get(param);
		return key;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected Pair<String, String>[] getQueryParameters() {
		List<Pair<String, String>> list = new ArrayList<Pair<String, String>>();
		for (String param : mFilters.keySet())
			list.add(Pair.create(param, mFilters.get(param)));
		return list.toArray(new Pair[1]);
	}

	@Override
	public long getCacheExpirationTime() {
		return DurationInMillis.ONE_MINUTE;
	}

	@Override
	protected String getBody() {
		return null;
	}

	@Override
	protected GroupListResponse getRequestModel(BaseParkResponse response) {
		String json = getGson().toJson(response.getData());
		GroupListResponse res = getGson().fromJson(json, GroupListResponse.class);
		return res;
	}

	@Override
	protected String getContentType() {
		return Constants.APPLICATION_JSON;
	}

	public static class Builder {
		public static final String PAGE = "page";
		public static final String PAGE_SIZE = "pageSize";
		public static final String QUERY = "q";
		public static final String MAX_DISTANCE = "radius";
		public static final String ORDER = "order";
		public static final String LATITUDE = "latitude";
		public static final String LONGITUDE = "longitude";

		private Map<String, String> params;

		public Builder() {
			this.params = new HashMap<String, String>();
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

		public GroupSearchRequest build() {
			return new GroupSearchRequest(this);
		}

	}

}
