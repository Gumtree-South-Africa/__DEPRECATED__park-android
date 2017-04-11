package com.ebay.park.requests;

import android.util.Pair;

import com.ebay.park.ParkApplication;
import com.ebay.park.responses.BaseParkResponse;
import com.ebay.park.responses.UserListResponse;
import com.ebay.park.utils.Constants;
import com.octo.android.robospice.persistence.DurationInMillis;

import java.util.*;

public class SearchUserRequest extends BaseParkSessionRequest<UserListResponse> {

	private String mUsername;
	private Map<String, String> mParams;

	private SearchUserRequest(Builder builder) {
		super(UserListResponse.class);
		this.mUsername = builder.username;
		this.mParams = builder.params;
	}

	@Override
	protected String getContentType() {
		return Constants.APPLICATION_JSON;
	}

	@Override
	public Object getCachekey() {
		return mUsername;
	}

	@Override
	public long getCacheExpirationTime() {
		return DurationInMillis.ONE_MINUTE;
	}

	@Override
	protected String getBody() {
		return getGson().toJson(mParams);
	}

	@Override
	protected String getUrlFormat() {
		if (ParkApplication.getInstance().getSessionToken() != null) {
			return String.format(ParkUrls.SEARCH_USER, getApiUri(), mUsername);
		} else {
			return String.format(ParkUrls.PUBLIC_SEARCH_USER, getApiUri());
		}

	}

	@Override
	protected Method getMethod() {
		return Method.GET;
	}

	@Override
	protected UserListResponse getRequestModel(BaseParkResponse response) {
		String json = getGson().toJson(response.getData());
		return getGson().fromJson(json, UserListResponse.class);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected Pair<String, String>[] getQueryParameters() {
		List<Pair<String, String>> list = new ArrayList<Pair<String, String>>();
		for (String param : mParams.keySet()) {
			if (param != null && mParams.get(param) != null) {
				list.add(Pair.create(param, mParams.get(param)));
			}
		}
		return list.toArray(new Pair[1]);
	}

	public static class Builder {
		public static final String LATITUDE = "latitude";
		public static final String LONGITUDE = "longitude";
		public static final String RADIUS = "radius";
		public static final String QUERY = "q";
		private static final String ORDER = "order";
		private static final String PAGE = "page";
		private static final String PAGE_SIZE = "pageSize";
		private static final String GROUP_ID = "groupId";

		private String username;
		private Map<String, String> params;

		public Builder(String username) {
			params = new HashMap<String, String>();
			this.username = username;
		}

		public Builder(String username, String query) {
			params = new HashMap<String, String>();
			params.put(QUERY, query);
			this.username = username;
		}

		public Builder(String username, long groupId) {
			params = new HashMap<String, String>();
			params.put(GROUP_ID, String.valueOf(groupId));
			this.username = username;
		}

		public Builder withLocation(String latitude, String longitude) {
			params.put(LATITUDE, latitude);
			params.put(LONGITUDE, longitude);
			return this;
		}

		public Builder withRadius(String radius) {
			if (radius != null) {
				params.put(RADIUS, radius);
			}
			return this;
		}

		public Builder orderBy(String orderBy) {
			params.put(ORDER, orderBy);
			return this;
		}

		public Builder page(int page) {
			if (page >= 0) {
				params.put(PAGE, String.valueOf(page));
			}
			return this;
		}

		public Builder pageSize(int pageSize) {
			if (pageSize > 0) {
				params.put(PAGE_SIZE, String.valueOf(pageSize));
			}
			return this;
		}

		public Builder forGroup(long groupId) {
			params.put(GROUP_ID, String.valueOf(groupId));
			return this;
		}

		public SearchUserRequest build() {
			return new SearchUserRequest(this);
		}
	}

}