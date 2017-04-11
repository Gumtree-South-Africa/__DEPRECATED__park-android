package com.ebay.park.requests;

import android.util.Pair;

import com.ebay.park.responses.BaseParkResponse;
import com.ebay.park.utils.Constants;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.octo.android.robospice.persistence.DurationInMillis;

/**
 * Request to unfollow an item.
 * 
 * @author federico.perez
 *
 */
public class ItemUnFollowRequest extends BaseParkSessionRequest<Boolean> {

	private long mItemId;

	public ItemUnFollowRequest(long itemId) {
		super(Boolean.class);
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
		return "";
	}

	@Override
	protected String getUrlFormat() {
		return String.format(ParkUrls.UNFOLLOW_ITEM, getApiUri(), mItemId);
	}

	@Override
	protected Method getMethod() {
		return Method.DELETE;
	}

	@Override
	protected Boolean getRequestModel(BaseParkResponse response) {
		String json = getGson().toJson(response.getData());
		JsonElement jelement = new JsonParser().parse(json);
		JsonObject jobject = jelement.getAsJsonObject();
		return jobject.get("success").getAsBoolean();
	}

	@Override
	protected Pair<String, String>[] getQueryParameters() {
		return null;
	}

}
