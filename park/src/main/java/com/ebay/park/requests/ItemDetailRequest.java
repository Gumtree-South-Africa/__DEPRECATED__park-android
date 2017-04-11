package com.ebay.park.requests;

import android.util.Pair;

import com.ebay.park.model.ItemModel;
import com.ebay.park.responses.BaseParkResponse;
import com.ebay.park.utils.Constants;
import com.octo.android.robospice.persistence.DurationInMillis;

/**
 * Request to get item details by id.
 * 
 * @author federico.perez
 * 
 */
public class ItemDetailRequest extends BaseParkSessionRequest<ItemModel> {

	private long mId;

	public ItemDetailRequest(long id) {
		super(ItemModel.class);
		this.mId = id;
	}

	@Override
	protected String getContentType() {
		return Constants.APPLICATION_JSON;
	}

	@Override
	public Object getCachekey() {
		return "ItemDetailRequest" + mId;
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
		return String.format(ParkUrls.GET_ITEM, getApiUri(), mId);
	}

	@Override
	protected Method getMethod() {
		return Method.GET;
	}

	@Override
	protected ItemModel getRequestModel(BaseParkResponse response) {
		String json = getGson().toJson(response.getData());
		return getGson().fromJson(json, ItemModel.class);
	}

	@Override
	protected Pair<String, String>[] getQueryParameters() {
		return null;
	}

}
