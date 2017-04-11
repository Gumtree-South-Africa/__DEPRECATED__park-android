package com.ebay.park.requests;

import android.util.Pair;

import com.ebay.park.responses.BaseParkResponse;
import com.ebay.park.responses.GroupDetailResponse;
import com.ebay.park.utils.Constants;
import com.octo.android.robospice.persistence.DurationInMillis;

public class GroupDeleteRequest extends BaseParkSessionRequest<GroupDetailResponse> {

	private long mGroupId;
	
	public GroupDeleteRequest(long groupId) {
		super(GroupDetailResponse.class);
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
		return null;
	}

	@Override
	protected String getUrlFormat() {
		return String.format(ParkUrls.DELETE_GROUP, getApiUri(), mGroupId);
	}

	@Override
	protected Method getMethod() {
		return Method.DELETE;
	}

	@Override
	protected GroupDetailResponse getRequestModel(BaseParkResponse response) {
		String json = getGson().toJson(response.getData());
		return getGson().fromJson(json, GroupDetailResponse.class);
	}

	@Override
	protected Pair<String, String>[] getQueryParameters() {
		return null;
	}

}
