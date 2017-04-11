package com.ebay.park.requests;

import android.util.Pair;

import com.ebay.park.ParkApplication;
import com.ebay.park.responses.BaseParkResponse;
import com.ebay.park.responses.GroupListResponse;
import com.ebay.park.utils.Constants;
import com.octo.android.robospice.persistence.DurationInMillis;

public class GroupsRecommendedRequest extends BaseParkSessionRequest<GroupListResponse> {

	public GroupsRecommendedRequest() {
		super(GroupListResponse.class);
	}

	@Override
	protected String getContentType() {
		return Constants.APPLICATION_JSON;
	}

	@Override
	public Object getCachekey() {
		return "groups";
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
		return getGson().fromJson(getGson().toJson(response.getData()), GroupListResponse.class);
	}

	@Override
	protected Pair<String, String>[] getQueryParameters() {
		return null;
	}

	@Override
	protected String getUrlFormat() {
		if (ParkApplication.getInstance().getSessionToken() != null) {
			return String.format(ParkUrls.RECOMMENDED_GROUPS, getApiUri());
		} else {
			return String.format(ParkUrls.PUBLIC_RECOMMENDED_GROUPS, getApiUri());
		}		
	}

	@Override
	protected Method getMethod() {
		return Method.GET;
	}

}
