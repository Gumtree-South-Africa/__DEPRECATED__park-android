package com.ebay.park.requests;

import android.text.TextUtils;
import android.util.Pair;

import com.ebay.park.responses.BaseParkResponse;
import com.ebay.park.responses.GroupDetailResponse;
import com.ebay.park.utils.Constants;
import com.octo.android.robospice.persistence.DurationInMillis;

import java.util.HashMap;
import java.util.Map;

public class GroupUpdateRequest extends BaseParkSessionRequest<GroupDetailResponse> {

	private Map<String, String> mPayload;
	private long mGroupId;
	
	public GroupUpdateRequest(long groupId, String name, String description, String location, String locationName, String zipCode) {
		super(GroupDetailResponse.class);
		this.mGroupId = groupId;
		this.mPayload = new HashMap<String, String>();
		this.mPayload.put("name", name);
		this.mPayload.put("description", description);
		if (!TextUtils.isEmpty(location)) {
			this.mPayload.put("location", location);
			this.mPayload.put("locationName", locationName);
			this.mPayload.put("zipCode", zipCode);
		}
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
		return getGson().toJson(mPayload);
	}

	@Override
	protected String getUrlFormat() {
		return String.format(ParkUrls.UPDATE_GROUP, getApiUri(), mGroupId);
	}

	@Override
	protected Method getMethod() {
		return Method.PUT;
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
