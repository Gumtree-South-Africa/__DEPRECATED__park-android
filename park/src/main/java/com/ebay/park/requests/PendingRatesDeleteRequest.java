package com.ebay.park.requests;

import android.util.Pair;

import com.ebay.park.model.PendingRateModel;
import com.ebay.park.responses.BaseParkResponse;
import com.ebay.park.utils.Constants;
import com.octo.android.robospice.persistence.DurationInMillis;

import java.util.Locale;
import java.util.Map;

/**
 * Delete Pending Rates Request
 * 
 * @author Nicolás Matias Fernández
 * 
 */
public class PendingRatesDeleteRequest extends BaseParkSessionRequest<PendingRateModel> {

	private long mId;

	public PendingRatesDeleteRequest(long id) {
		super(PendingRateModel.class);
		this.mId = id;
	}

	@Override
	protected String getUrlFormat() {
		return String.format(ParkUrls.DELETE_PENDING_RATES, getApiUri(), mId);
	}

	@Override
	protected Method getMethod() {
		return Method.DELETE;
	}

	@Override
	protected Pair<String, String>[] getQueryParameters() {
		return null;
	}

	@Override
	protected Map<String, String> getHeaders() {
		Map<String, String> headers = super.getHeaders();
		headers.put("Content-Type", "application/json");
		headers.put("Accept-Language", Locale.getDefault().getLanguage().toString());
		return headers;
	}

	@Override
	public Object getCachekey() {
		return "rates" + mId;
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
	protected PendingRateModel getRequestModel(BaseParkResponse response) {
		return new PendingRateModel();

	}

	@Override
	protected String getContentType() {
		return Constants.APPLICATION_JSON;
	}

}
