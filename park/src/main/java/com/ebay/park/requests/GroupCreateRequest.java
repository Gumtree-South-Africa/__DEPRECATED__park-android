package com.ebay.park.requests;

import android.text.TextUtils;
import android.util.Pair;

import com.ebay.park.responses.BaseParkResponse;
import com.ebay.park.responses.GroupDetailResponse;
import com.ebay.park.utils.Constants;
import com.octo.android.robospice.persistence.DurationInMillis;

import java.util.HashMap;
import java.util.Map;

/**
 * Create Group Request
 * 
 * @author Nicol�s Mat�as Fern�ndez
 * 
 */
public class GroupCreateRequest extends BaseParkSessionRequest<GroupDetailResponse> {

	public static final String NAME = "name";
	public static final String LOCATION = "location";
	public static final String LOCATION_NAME = "locationName";
	public static final String ZIP_CODE = "zipCode";
	public static final String DESCRIPTION = "description";

	private Map<String, String> mPayload;

	public GroupCreateRequest(String name, String description, String location, String locationName, String zipCode) {
		super(GroupDetailResponse.class);
		this.mPayload = new HashMap<>();
		this.mPayload.put(NAME, name);
		if (!TextUtils.isEmpty(location)) {
			this.mPayload.put(LOCATION, location);
			this.mPayload.put(LOCATION_NAME, locationName);
			this.mPayload.put(ZIP_CODE, zipCode);
		}
		if (!TextUtils.isEmpty(description)) {
			this.mPayload.put(DESCRIPTION, description);
		}
	}

	@Override
	protected String getUrlFormat() {
		return String.format(ParkUrls.CREATE_GROUP, getApiUri());
	}

	@Override
	protected Method getMethod() {
		return Method.POST;
	}

	@Override
	protected Pair<String, String>[] getQueryParameters() {
		return null;
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
	protected GroupDetailResponse getRequestModel(BaseParkResponse response) {
		String json = getGson().toJson(response.getData());
		return getGson().fromJson(json, GroupDetailResponse.class);
	}

	@Override
	protected String getContentType() {
		return Constants.APPLICATION_JSON;
	}

}
