package com.ebay.park.requests;

import android.util.Pair;

import com.ebay.park.ParkApplication;
import com.ebay.park.responses.BaseParkResponse;
import com.ebay.park.responses.UserListResponse;
import com.ebay.park.utils.Constants;
import com.octo.android.robospice.persistence.DurationInMillis;

import java.util.ArrayList;
import java.util.List;

public class DiscoverUsersRequest extends BaseParkSessionRequest<UserListResponse> {

	public static final String LATITUDE = "latitude";
	public static final String LONGITUDE = "longitude";

	private String mUsername;
	private double mLatitude;
	private double mLongitude;
	private boolean mWithLocation = false;

	public DiscoverUsersRequest(String username) {
		super(UserListResponse.class);
		this.mUsername = username;
	}

	public DiscoverUsersRequest(String username, double latitude, double longitude) {
		this(username);
		this.mLatitude = latitude;
		this.mLongitude = longitude;
		this.mWithLocation = true;
	}

	@Override
	protected String getContentType() {
		return Constants.APPLICATION_JSON;
	}

	@Override
	public Object getCachekey() {
		return "discoverusers" + mUsername;
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
	protected String getUrlFormat() {

		if (ParkApplication.getInstance().getSessionToken() != null) {
			return String.format(ParkUrls.DISCOVER_USERS, getApiUri(), mUsername);
		} else {
			return String.format(ParkUrls.PUBLIC_DISCOVER_USERS, getApiUri());
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

	@Override
	protected Pair<String, String>[] getQueryParameters() {
		if (mWithLocation) {
			List<Pair<String, String>> list = new ArrayList<Pair<String, String>>();
			list.add(Pair.create(LATITUDE, String.valueOf(mLatitude)));
			list.add(Pair.create(LONGITUDE, String.valueOf(mLongitude)));
			return list.toArray(new Pair[1]);
		} else {
			return null;
		}
	}

}
