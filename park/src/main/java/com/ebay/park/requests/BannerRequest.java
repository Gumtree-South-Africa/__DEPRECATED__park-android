package com.ebay.park.requests;

import android.util.Pair;

import com.ebay.park.model.BannerModel;
import com.ebay.park.responses.BaseParkResponse;
import com.ebay.park.utils.Constants;
import com.google.gson.Gson;
import com.octo.android.robospice.persistence.DurationInMillis;

public class BannerRequest extends BaseParkSessionRequest<BannerModel> {

	public BannerRequest() {
		super(BannerModel.class);
	}

	@Override
	protected String getContentType() {
		return Constants.APPLICATION_JSON;
	}

	@Override
	public Object getCachekey() {
		return "banner";
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
		return String.format(ParkUrls.BANNER, getApiUri());
	}

	@Override
	protected Method getMethod() {
		return Method.GET;
	}

	@Override
	protected BannerModel getRequestModel(BaseParkResponse response) {
		final Gson gson = getGson();
		String json = gson.toJson(response.getData());
		return gson.fromJson(json, BannerModel.class);
	}

	@Override
	protected Pair<String, String>[] getQueryParameters() {
		return null;
	}

}
