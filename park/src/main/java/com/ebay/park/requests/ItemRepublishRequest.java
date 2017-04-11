package com.ebay.park.requests;

import android.annotation.SuppressLint;
import android.util.Pair;

import com.ebay.park.responses.BaseParkResponse;
import com.ebay.park.responses.ItemRepublishResponse;
import com.ebay.park.utils.Constants;
import com.octo.android.robospice.persistence.DurationInMillis;

/**
 * Item Republish Request
 * 
 * @author Jonatan Collard Bovy
 * 
 */
public class ItemRepublishRequest extends BaseParkSessionRequest<ItemRepublishResponse> {

	private long mItemId;

	public ItemRepublishRequest(long itemId) {
		super(ItemRepublishResponse.class);
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
		return "{}";
	}

	@SuppressLint("DefaultLocale")
	@Override
	protected String getUrlFormat() {
		return String.format(ParkUrls.REPUBLISH_ITEM, getApiUri(), mItemId);
	}

	@Override
	protected Method getMethod() {
		return Method.POST;
	}

	@Override
	protected ItemRepublishResponse getRequestModel(BaseParkResponse response) {
		String json = getGson().toJson(response.getData());
		return getGson().fromJson(json, ItemRepublishResponse.class);
	}

	@Override
	protected Pair<String, String>[] getQueryParameters() {
		return null;
	}

}
