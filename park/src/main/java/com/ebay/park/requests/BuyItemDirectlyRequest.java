package com.ebay.park.requests;

import android.annotation.SuppressLint;
import android.util.Pair;

import com.ebay.park.responses.BaseParkResponse;
import com.ebay.park.utils.Constants;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.octo.android.robospice.persistence.DurationInMillis;

/**
 * Request to buy an item when no chat has been started.
 * 
 * @author federico.perez
 *
 */
public class BuyItemDirectlyRequest extends BaseParkSessionRequest<Long> {

	private long mItemId;

	public BuyItemDirectlyRequest(long itemId) {
		super(Long.class);
		this.mItemId = itemId;
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
		return null;
	}

	@SuppressLint("DefaultLocale")
	@Override
	protected String getUrlFormat() {
		return String.format(ParkUrls.BUY_ITEM_DIRECTLY, getApiUri(), mItemId);
	}

	@Override
	protected Method getMethod() {
		return Method.PUT;
	}

	@Override
	protected Long getRequestModel(BaseParkResponse response) {
		final Gson gson = getGson();
		String json = gson.toJson(response.getData());
		JsonElement jelement = new JsonParser().parse(json);
		JsonObject jobject = jelement.getAsJsonObject();
		return jobject.get("conversationId").getAsLong();
	}

	@Override
	protected Pair<String, String>[] getQueryParameters() {
		return null;
	}

}
