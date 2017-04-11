package com.ebay.park.requests;

import android.util.Pair;

import com.ebay.park.responses.BaseParkResponse;
import com.ebay.park.responses.ItemCategoryResponse;
import com.ebay.park.utils.Constants;
import com.octo.android.robospice.persistence.DurationInMillis;

/**
 * Categories Request.
 * 
 * @author federico.perez
 * 
 */
public class ItemCategoryRequest extends BaseParkSessionRequest<ItemCategoryResponse> {

	public ItemCategoryRequest() {
		super(ItemCategoryResponse.class);
	}

	@Override
	public Object getCachekey() {
		return "categories";
	}

	@Override
	public long getCacheExpirationTime() {
		return DurationInMillis.ALWAYS_RETURNED;
	}

	@Override
	protected String getBody() {
		return null;
	}

	@Override
	protected String getUrlFormat() {
		return String.format(ParkUrls.PUBLIC_ITEMS_CATEGORIES, getApiUri());
	}

	@Override
	protected Method getMethod() {
		return Method.GET;
	}

	@Override
	protected Pair<String, String>[] getQueryParameters() {
		return null;
	}

	@Override
	protected ItemCategoryResponse getRequestModel(BaseParkResponse response) {
		String json = getGson().toJson(response.getData());
		return getGson().fromJson(json, ItemCategoryResponse.class);
	}

	@Override
	protected String getContentType() {
		return Constants.APPLICATION_JSON;
	}

}
