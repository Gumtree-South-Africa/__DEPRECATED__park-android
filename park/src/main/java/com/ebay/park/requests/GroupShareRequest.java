package com.ebay.park.requests;

import android.util.Pair;

import com.ebay.park.responses.BaseParkResponse;
import com.ebay.park.responses.GroupShareResponse;
import com.ebay.park.utils.Constants;
import com.octo.android.robospice.persistence.DurationInMillis;

import java.util.HashMap;
import java.util.Map;

public class GroupShareRequest extends BaseParkSessionRequest<GroupShareResponse> {

	private Map<String, String> mPayload;
	private long mGroupId;
	
	public GroupShareRequest(long groupId, boolean shareOnFacebook, boolean shareOnTwitter) {
		super(GroupShareResponse.class);
		this.mGroupId = groupId;
		this.mPayload = new HashMap<String, String>();
		this.mPayload.put("shareOnFacebook", String.valueOf(shareOnFacebook));
		this.mPayload.put("shareOnTwitter", String.valueOf(shareOnTwitter));
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
		return String.format(ParkUrls.SHARE_GROUP, getApiUri(), mGroupId);
	}

	@Override
	protected Method getMethod() {
		return Method.POST;
	}

	@Override
	protected GroupShareResponse getRequestModel(BaseParkResponse response) {
		String json = getGson().toJson(response.getData());
		return getGson().fromJson(json, GroupShareResponse.class);
	}

	@Override
	protected Pair<String, String>[] getQueryParameters() {
		return null;
	}

}
