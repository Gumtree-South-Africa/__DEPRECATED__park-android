package com.ebay.park.requests;

import android.util.Pair;

import com.ebay.park.ParkApplication;
import com.ebay.park.responses.BaseParkResponse;
import com.ebay.park.responses.ItemListResponse;
import com.ebay.park.utils.Constants;
import com.octo.android.robospice.persistence.DurationInMillis;

import java.util.ArrayList;
import java.util.List;

public class ItemsRecommendedRequest extends BaseParkSessionRequest<ItemListResponse> {

	public static final String LATITUDE = "latitude";
	public static final String LONGITUDE = "longitude";

	private Double mLatitude;
	private Double mLongitude;

	public ItemsRecommendedRequest(Double latitude, Double longitude) {
		super(ItemListResponse.class);
		mLatitude = latitude;
		mLongitude = longitude;
	}

	@Override
	protected String getContentType() {
		return Constants.APPLICATION_JSON;
	}

	@Override
	public Object getCachekey() {
		return "items";
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
	protected ItemListResponse getRequestModel(BaseParkResponse response) {
		return getGson().fromJson(getGson().toJson(response.getData()), ItemListResponse.class);
	}

	@Override
	protected Pair<String, String>[] getQueryParameters() {
		if (mLatitude != null && mLongitude != null) {
			List<Pair<String, String>> list = new ArrayList<Pair<String, String>>();
			list.add(Pair.create(LATITUDE, mLatitude.toString()));
			list.add(Pair.create(LONGITUDE, mLongitude.toString()));
			return list.toArray(new Pair[1]);
		} else {
			return null;
		}
	}

	@Override
	protected String getUrlFormat() {
		if (ParkApplication.getInstance().getSessionToken() != null) {
			return String.format(ParkUrls.RECOMMENDED_ITEMS, getApiUri());
		} else {
			return String.format(ParkUrls.PUBLIC_RECOMMENDED_ITEMS, getApiUri());
		}
	}

	@Override
	protected Method getMethod() {
		return Method.GET;
	}

}
