package com.ebay.park.requests;

import android.util.Pair;

import com.ebay.park.responses.BaseParkResponse;
import com.ebay.park.utils.Constants;
import com.octo.android.robospice.persistence.DurationInMillis;

import java.util.HashMap;
import java.util.Map;

/**
 * Request to accept a negotiation.
 * 
 * @author federico.perez
 *
 */
public class AcceptNegotiationRequest extends BaseParkSessionRequest<Boolean> {

	private static final String CONVERSATION_ID_PARAM_NAME = "conversationId";

	private Map<String, Long> mParams = new HashMap<String, Long>();

	public AcceptNegotiationRequest(long conversationId) {
		super(Boolean.class);
		mParams.put(CONVERSATION_ID_PARAM_NAME, conversationId);
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
		return getGson().toJson(mParams);
	}

	@Override
	protected String getUrlFormat() {
		return String.format(ParkUrls.ACCEPT_NEGOTIATION, getApiUri());
	}

	@Override
	protected Method getMethod() {
		return Method.POST;
	}

	@Override
	protected Boolean getRequestModel(BaseParkResponse response) {
		return true;
	}

	@Override
	protected Pair<String, String>[] getQueryParameters() {
		return null;
	}

}
