package com.ebay.park.requests;

import android.text.TextUtils;
import android.util.Pair;

import com.ebay.park.responses.BaseParkResponse;
import com.ebay.park.responses.ItemPicturesUpdateResponse;
import com.ebay.park.utils.Constants;
import com.octo.android.robospice.persistence.DurationInMillis;

import java.util.List;
import java.util.Map;

/**
 * Item pictures delete Request
 * 
 * @author Nicolás Matias Fernández
 * 
 */
public class ItemPicturesDeleteRequest extends BaseParkSessionRequest<ItemPicturesUpdateResponse> {

	private long mItemId;
	private List<Integer> mPhotosIds;

	public ItemPicturesDeleteRequest(long itemId, List<Integer> photosIds) {
		super(ItemPicturesUpdateResponse.class);
		this.mItemId = itemId;
		this.mPhotosIds = photosIds;
	}

	@Override
	protected String getUrlFormat() {
		return String.format(ParkUrls.DELETE_ITEM_PICTURES, getApiUri(), mItemId, getPhotosIds());
	}

	@Override
	protected Map<String, String> getHeaders() {
		Map<String, String> headers = super.getHeaders();
		headers.put("Content-Type", "application/json");
		return headers;
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
	public Object getCachekey() {
		return "items" + mItemId + getPhotosIds();
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
	protected ItemPicturesUpdateResponse getRequestModel(BaseParkResponse response) {
		String json = getGson().toJson(response.getData());
		return getGson().fromJson(json, ItemPicturesUpdateResponse.class);
	}

	@Override
	protected String getContentType() {
		return Constants.APPLICATION_JSON;
	}

	private String getPhotosIds(){
		String format = "";
		for (Integer id : mPhotosIds){
			if (!TextUtils.isEmpty(format)){
				format += ",";
			}
			format += id.toString();
		}
		return format;
	}

}
