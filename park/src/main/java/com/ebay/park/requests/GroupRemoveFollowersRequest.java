package com.ebay.park.requests;

import android.util.Pair;

import com.ebay.park.responses.BaseParkResponse;
import com.ebay.park.utils.Constants;
import com.octo.android.robospice.persistence.DurationInMillis;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

public class GroupRemoveFollowersRequest extends BaseParkSessionRequest<BaseParkResponse> {

	private long mGroupId;
	private Map<String, String> mPayload;
	
	public GroupRemoveFollowersRequest(long groupId, String[] ids) {
		super(BaseParkResponse.class);
		this.mGroupId = groupId;
		this.mPayload = new HashMap<String, String>();
		this.mPayload.put("followersIds", StringUtils.join(ids, ","));
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
		return String.format(ParkUrls.GROUP_REMOVE_FOLLOWERS, getApiUri(), mGroupId);
	}

	@Override
	protected Method getMethod() {
		return Method.POST;
	}

	@Override
	protected BaseParkResponse getRequestModel(BaseParkResponse response) {
		return response;
	}

	@Override
	protected Pair<String, String>[] getQueryParameters() {
		return null;
	}
	
}
