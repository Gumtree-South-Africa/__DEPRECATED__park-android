package com.ebay.park.requests;

import android.text.TextUtils;
import android.util.Pair;

import com.ebay.park.responses.BaseParkResponse;
import com.ebay.park.responses.GroupListResponse;
import com.ebay.park.utils.Constants;
import com.octo.android.robospice.persistence.DurationInMillis;

import java.util.*;
/**
 * List Groups Request
 * 
 * @author Nicolás Matias Fernández
 * 
 */
public class GroupListRequest extends BaseParkSessionRequest<GroupListResponse> {

	private String mUserName;

	private Map<String, String> filters;

	private GroupListRequest(Builder builder, String userName) {
		super(GroupListResponse.class);
		filters = builder.params;
		this.mUserName = userName;
	}

	@Override
	protected String getUrlFormat() {
		return String.format(ParkUrls.LIST_GROUPS, getApiUri(), mUserName);
	}

	@Override
	protected Method getMethod() {
		return Method.GET;
	}

	@Override
	public Object getCachekey() {
		String key = "groups";
		for (String param : filters.keySet())
			key += filters.get(param);
		return key;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected Pair<String, String>[] getQueryParameters() {
		List<Pair<String, String>> list = new ArrayList<Pair<String, String>>();
		for (String param : filters.keySet())
			list.add(Pair.create(param, filters.get(param)));
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
		return getGson().fromJson(json, GroupListResponse.class);
	}

	@Override
	protected String getContentType() {
		return Constants.APPLICATION_JSON;
	}

	public static class Builder {
		private static final String PAGE = "page";
		private static final String PAGE_SIZE = "pageSize";
		private static final String QUERY = "q";
		private static final String MAX_DISTANCE = "radius";
		private static final String ORDER = "order";
		private static final String LATITUDE = "latitude";
		private static final String LONGITUDE = "longitude";
		private static final String ONLY_OWNED = "onlyOwned";

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
		
		public Builder onlyOwned(boolean onlyOwned) {
			params.put(ONLY_OWNED, String.valueOf(onlyOwned));
			return this;
		}

		public GroupListRequest build(String userName) {
			return new GroupListRequest(this, userName);
		}

	}

}
