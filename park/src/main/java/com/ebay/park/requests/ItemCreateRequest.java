package com.ebay.park.requests;

import android.util.Pair;

import com.ebay.park.model.PublishItemModel;
import com.ebay.park.responses.BaseParkResponse;
import com.ebay.park.utils.Constants;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.octo.android.robospice.persistence.DurationInMillis;

import java.util.*;

/**
 * Publish Item Request
 * 
 * @author Nicolás Matias Fernández
 * 
 */
public class ItemCreateRequest extends BaseParkSessionRequest<PublishItemModel> {

	private PublishRequestPayload mPayload;

	public ItemCreateRequest(PublishRequestPayload oPublish) {

		super(PublishItemModel.class);
		this.mPayload = new PublishRequestPayload();

		mPayload.name = oPublish.name;
		mPayload.description = oPublish.description;
		mPayload.location = oPublish.location;
		mPayload.price = oPublish.price;
		mPayload.shareOnFacebook = oPublish.shareOnFacebook;
		mPayload.shareOnTwitter = oPublish.shareOnTwitter;
		mPayload.categoryId = oPublish.categoryId;
		mPayload.groups = oPublish.groups;
		mPayload.latitude = oPublish.latitude;
		mPayload.longitude = oPublish.longitude;
		mPayload.locationName = oPublish.locationName;
		mPayload.zipCode = oPublish.zipCode;
	}

	@Override
	protected String getUrlFormat() {
		return String.format(ParkUrls.PUBLISH_ITEM, getApiUri());
	}

	@Override
	protected Method getMethod() {
		return Method.POST;
	}

	@Override
	protected Pair<String, String>[] getQueryParameters() {
		// TODO Auto-generated method stub
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
		return "items";
	}

	@Override
	public long getCacheExpirationTime() {
		return DurationInMillis.ONE_MINUTE;
	}

	@Override
	protected String getBody() {
		JsonObject json = new JsonObject();

		json.addProperty("brandPublish", "android");
		json.addProperty("description", mPayload.description);
		json.addProperty("location", mPayload.location);
		json.addProperty("locationName", mPayload.locationName);
		json.addProperty("latitude", mPayload.latitude);
		json.addProperty("longitude", mPayload.longitude);
		json.addProperty("name", mPayload.name);
		json.addProperty("price", mPayload.price.toString());
		json.addProperty("versionPublish", String.valueOf(android.os.Build.VERSION.SDK_INT));
		json.addProperty("shareOnFacebook", mPayload.shareOnFacebook.toString());
		json.addProperty("shareOnTwitter", mPayload.shareOnTwitter.toString());
		json.addProperty("categoryId", Long.toString(mPayload.categoryId));
		json.addProperty("zipCode", mPayload.zipCode);

		final JsonArray jsonMarketPlacesArray = new JsonArray();
		for (final int market : mPayload.groups) {
			final JsonPrimitive jsonMarket = new JsonPrimitive(market);
			jsonMarketPlacesArray.add(jsonMarket);
		}
		json.add("groups", jsonMarketPlacesArray);

		return json.toString();
	}

	@Override
	protected PublishItemModel getRequestModel(BaseParkResponse response) {

		String json = getGson().toJson(response.getData());
		return getGson().fromJson(json, PublishItemModel.class);
	}

	@Override
	protected String getContentType() {
		return Constants.APPLICATION_JSON;
	}

	public static class PublishRequestPayload {
		private String name;
		private String description;
		private String location;
		private String locationName;
		private String longitude;
		private String latitude;
		private Double price;
		private Boolean shareOnFacebook;
		private Boolean shareOnTwitter;
		private long categoryId;
		private ArrayList<Integer> groups;
		private String zipCode;

		public void setName(String name) {
			this.name = name;
		}

		public void setDescription(String description) {
			this.description = description;
		}

		public void setLocation(String location) {
			this.location = location;
		}

		public void setPrice(Double price) {
			this.price = price;
		}

		public void setShareOnFacebook(Boolean shareOnFacebook) {
			this.shareOnFacebook = shareOnFacebook;
		}

		public void setShareOnTwitter(Boolean shareOnTwitter) {
			this.shareOnTwitter = shareOnTwitter;
		}

		public void setCategoryId(long categoryId) {
			this.categoryId = categoryId;
		}

		public void setGroups(ArrayList<Integer> groups) {
			this.groups = groups;
		}

		public void setLongitude(String longitude) {
			this.longitude = longitude;
		}

		public void setLatitude(String latitude) {
			this.latitude = latitude;
		}

		public void setLocationName(String locationName) {
			this.locationName = locationName;
		}

		public void setZipCode(String aZipCode) {
			this.zipCode = aZipCode;
		}

	}

}
