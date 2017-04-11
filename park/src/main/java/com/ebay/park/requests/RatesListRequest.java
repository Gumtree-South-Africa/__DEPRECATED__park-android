package com.ebay.park.requests;

import android.util.Pair;

import com.ebay.park.model.RateModel;
import com.ebay.park.requests.RatesListRequest.RatesListResponse;
import com.ebay.park.responses.BaseParkResponse;
import com.ebay.park.utils.Constants;
import com.google.gson.Gson;
import com.octo.android.robospice.persistence.DurationInMillis;

import java.util.ArrayList;
import java.util.List;

public class RatesListRequest extends BaseParkSessionRequest<RatesListResponse> {

	public static final int ROLE_BUYER = 1;
	public static final int ROLE_SELLER = 2;
	public static final int ROLE_ALL = 3;
	public static final String USERNAME = "username";
	public static final String PAGE = "page";
	public static final String PAGE_SIZE = "pageSize";
	public static final String ROLE = "role";
	public static final String BUYER = "buyer";
	public static final String SELLER = "seller";

	private String mUsername;
	private int mRole;
	private int mPage;
	private int mPageSize;

	public RatesListRequest(String username, int role, int page, int pageSize) {
		super(RatesListResponse.class);
		this.mUsername = username;
		this.mRole = role;
		this.mPage = page;
		this.mPageSize = pageSize;
	}

	@Override
	protected String getContentType() {
		return Constants.APPLICATION_JSON;
	}

	@Override
	public Object getCachekey() {
		return mUsername + mRole;
	}

	@Override
	public long getCacheExpirationTime() {
		return DurationInMillis.ONE_SECOND;
	}

	@Override
	protected String getBody() {
		return null;
	}

	@Override
	protected String getUrlFormat() {
		return String.format(ParkUrls.PUBLIC_GET_RATES, getApiUri());
	}

	@Override
	protected Method getMethod() {
		return Method.GET;
	}

	@Override
	protected RatesListResponse getRequestModel(BaseParkResponse response) {
		final Gson gson = getGson();
		String json = gson.toJson(response.getData());
		return gson.fromJson(json, RatesListResponse.class);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected Pair<String, String>[] getQueryParameters() {
		List<Pair<String, String>> list = new ArrayList<Pair<String, String>>();
		list.add(Pair.create(USERNAME, mUsername));
		list.add(Pair.create(PAGE, Integer.toString(mPage)));
		list.add(Pair.create(PAGE_SIZE, Integer.toString(mPageSize)));
		switch (mRole) {
		case ROLE_ALL:
			break;
		case ROLE_BUYER:
			list.add(Pair.create(ROLE, BUYER));
			break;
		case ROLE_SELLER:
			list.add(Pair.create(ROLE, SELLER));
			break;
		default:
			break;
		}
		return list.toArray(new Pair[1]);
	}

	public static class RatesListResponse {

		private List<RateModel> ratings;
		private int totalElements;
		private String noResultsMessage;
		private String noResultsHintMessage;

		public List<RateModel> getRatings() {
			return ratings;
		}

		public void setRatings(List<RateModel> ratings) {
			this.ratings = ratings;
		}

		public int getAmountDataFound() {
			return totalElements;
		}

		public void setAmountItemsFound(int aAmount) {
			this.totalElements = aAmount;
		}

		public String getNoResultsMessage() {
			return noResultsMessage;
		}

		public void setNoResultsMessage(String noResultsMessage) {
			this.noResultsMessage = noResultsMessage;
		}

		public String getNoResultsHint() {
			return noResultsHintMessage;
		}

		public void setNoResultsHint(String noResultsHint) {
			this.noResultsHintMessage = noResultsHint;
		}

	}
}
