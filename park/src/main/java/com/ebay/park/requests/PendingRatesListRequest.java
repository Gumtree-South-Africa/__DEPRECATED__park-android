package com.ebay.park.requests;

import android.util.Pair;

import com.ebay.park.ParkApplication;
import com.ebay.park.model.PendingRateModel;
import com.ebay.park.requests.PendingRatesListRequest.PendingRatesResponse;
import com.ebay.park.responses.BaseParkResponse;
import com.ebay.park.utils.Constants;
import com.google.gson.Gson;
import com.octo.android.robospice.persistence.DurationInMillis;

import java.util.ArrayList;
import java.util.List;

public class PendingRatesListRequest extends BaseParkSessionRequest<PendingRatesResponse> {

	private int mPage;
	private int mPageSize;

	public PendingRatesListRequest(Integer page, int pageSize) {
		super(PendingRatesResponse.class);
		this.mPage = page;
		this.mPageSize = pageSize;
	}

	@Override
	protected String getContentType() {
		return Constants.APPLICATION_JSON;
	}

	@Override
	public Object getCachekey() {
		return ParkApplication.getInstance().getUsername() + "pending";
	}

	@Override
	public long getCacheExpirationTime() {
		return DurationInMillis.ONE_SECOND * 5;
	}

	@Override
	protected String getBody() {
		return null;
	}

	@Override
	protected String getUrlFormat() {
		if (ParkApplication.getInstance().getSessionToken() != null) {
			return String.format(ParkUrls.PENDING_RATES, getApiUri());
		} else {
			return String.format(ParkUrls.PUBLIC_PENDING_RATES, getApiUri());
		}
	}

	@Override
	protected Method getMethod() {
		return Method.GET;
	}

	@Override
	protected PendingRatesResponse getRequestModel(BaseParkResponse response) {
		final Gson gson = getGson();
		String json = gson.toJson(response.getData());
		return gson.fromJson(json, PendingRatesResponse.class);
	}

	@Override
	protected Pair<String, String>[] getQueryParameters() {
		List<Pair<String, String>> list = new ArrayList<Pair<String, String>>();
		list.add(Pair.create("page", Integer.toString(mPage)));
		list.add(Pair.create("pageSize", Integer.toString(mPageSize)));
		return list.toArray(new Pair[1]);
	}

	public static class PendingRatesResponse {

		private List<PendingRateModel> pendingRatings;
		private int totalElements;
		private int numberOfElements;
		private String noResultsMessage;
		private String noResultsHintMessage;

		public List<PendingRateModel> getPendingRatings() {
			return pendingRatings;
		}

		public void setPendingRatings(List<PendingRateModel> pendingRatings) {
			this.pendingRatings = pendingRatings;
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

		public int getTotalElements() {
			return totalElements;
		}

		public void setTotalElements(int totalElements) {
			this.totalElements = totalElements;
		}

		public int getNumberOfElements() {
			return numberOfElements;
		}

		public void setNumberOfElements(int numberOfElements) {
			this.numberOfElements = numberOfElements;
		}

	}

}
