package com.ebay.park.requests;

import android.text.TextUtils;
import android.util.Pair;

import com.ebay.park.model.PublishItemModel;
import com.ebay.park.responses.BaseParkResponse;
import com.ebay.park.utils.Constants;
import com.octo.android.robospice.persistence.DurationInMillis;

import java.util.*;

/**
 * Update Item Request.
 * 
 * @author Nicolás Matias Fernández
 * @author federico.perez
 * 
 */
public class ItemUpdateRequest extends BaseParkSessionRequest<PublishItemModel> {

	private long mId;
	private boolean mFlagBanned;
	private Map<String, Object> mParams;

	private ItemUpdateRequest(Builder builder) {
		super(PublishItemModel.class);
		mId = builder.itemId;
		mFlagBanned = builder.flagBanned;
		mParams = builder.params;
	}

	@Override
	protected String getUrlFormat() {
		if(mFlagBanned) {
			return String.format(ParkUrls.UPDATE_ITEM, getApiUri(), mId);
		} else {
			return String.format(ParkUrls.UPDATE_ITEM_BANNED, getApiUri(), mId);
		}

	}

	@Override
	protected Method getMethod() {
		return Method.PUT;
	}

	@Override
	protected Pair<String, String>[] getQueryParameters() {
		return null;
	}

	@Override
	protected Map<String, String> getHeaders() {
		Map<String, String> headers = super.getHeaders();
		headers.put("Content-Type", "application/json");
		return headers;
	}

	@Override
	public Object getCachekey() {
		return "items" + mId;
	}

	@Override
	public long getCacheExpirationTime() {
		return DurationInMillis.ONE_MINUTE;
	}

	@Override
	protected String getBody() {
		return getGson().toJson(mParams);
	}

	@Override
	protected PublishItemModel getRequestModel(BaseParkResponse response) {
		return new PublishItemModel();
	}

	@Override
	protected String getContentType() {
		return Constants.APPLICATION_JSON;
	}

	/**
	 * Builder for item update request.
	 * 
	 * @author federico.perez
	 *
	 */
	public static class Builder {
		private static final String DESCRIPTION_KEY = "description";
		private static final String LOCATION_KEY = "location";
		private static final String LOCATION_NAME_KEY = "locationName";
		private static final String NAME_KEY = "name";
		private static final String PRICE_KEY = "price";
		private static final String CATEGORY_KEY = "categoryId";
		private static final String GROUPS_KEY = "groups";
		private static final String LATITUDE_KEY = "latitude";
		private static final String LONGITUDE_KEY = "longitude";
		private static final String SHARE_FB_KEY = "shareOnFacebook";
		private static final String SHARE_TW_KEY = "shareOnTwitter";
		private static final String ZIPCODE_KEY = "zipCode";

		private Map<String, Object> params;
		private long itemId;
		private boolean flagBanned;

		/**
		 * Constructor for the builder.
		 * 
		 * @param itemId
		 *            The item id of the item to update.
		 */
		public Builder(long itemId, boolean flagBanned) {
			params = new HashMap<String, Object>();
			this.itemId = itemId;
			this.flagBanned = flagBanned;
		}

		/**
		 * Update the description.
		 * 
		 * @param description
		 *            String containing the description.
		 */
		public Builder description(String description) {
			if (!TextUtils.isEmpty(description)) {
				params.put(DESCRIPTION_KEY, description.trim());
			} else {
				params.put(DESCRIPTION_KEY, "");
			}
			return this;
		}

		/**
		 * Update the location.
		 * 
		 * @param location
		 *            String representing the location.
		 */
		public Builder location(String location) {
			if (!TextUtils.isEmpty(location)) {
				params.put(LOCATION_KEY, location);
			}
			return this;
		}

		/**
		 * Update the locationName.
		 * 
		 * @param locationName
		 *            String representing the location name.
		 */
		public Builder locationName(String locationName) {
			if (!TextUtils.isEmpty(locationName)) {
				params.put(LOCATION_NAME_KEY, locationName);
			}
			return this;
		}

		/**
		 * Update the name.
		 * 
		 * @param name
		 *            name of the item.
		 */
		public Builder name(String name) {
			params.put(NAME_KEY, name.trim());
			return this;
		}

		/**
		 * Set price to update.
		 * 
		 * @param price
		 *            Any double greater or equal than zero. Will throw
		 *            {@link IllegalArgumentException} if less than 0.
		 * @return
		 */
		public Builder price(double price) {
			if (price <= 0) {
				throw new IllegalArgumentException("Prices can not be negative");
			}
			params.put(PRICE_KEY, price);
			return this;
		}

		/**
		 * Update the category of the item.
		 * 
		 * @param category
		 *            The id of the category to change the item to.
		 */
		public Builder category(long category) {
			params.put(CATEGORY_KEY, category);
			return this;
		}

		/**
		 * Update groups to which the item belongs.
		 * 
		 * @param groups
		 *            A list of groups ids.
		 */
		public Builder groups(List<Integer> groups) {
			params.put(GROUPS_KEY, groups);
			return this;
		}

		/**
		 * Set latitude to update.
		 * 
		 * @param latitude
		 *            Double between -90 and 90. Will throw
		 *            {@link IllegalArgumentException} if number outside range.
		 */
		public Builder latitude(double latitude) {
			if (latitude <= -90 || latitude >= 90) {
				// throw new IllegalArgumentException("Illegal lattiude: " +
				// latitude
				// + " Latitudes must be between -90 and 90");
			} else {
				params.put(LATITUDE_KEY, latitude);
			}
			return this;
		}

		/**
		 * Set longitude to update.
		 * 
		 * @param longitude
		 *            Double between -180 and 180. Will throw
		 *            {@link IllegalArgumentException} if number outside range.
		 */
		public Builder longitude(double longitude) {
			if (longitude <= -180 || longitude >= 180) {
			} else {
				params.put(LONGITUDE_KEY, longitude);
			}
			return this;
		}

		public Builder shareOnFacebook(Boolean yes) {
			params.put(SHARE_FB_KEY, String.valueOf(yes));
			return this;
		}

		public Builder shareOnTwitter(Boolean yes) {
			params.put(SHARE_TW_KEY, String.valueOf(yes));
			return this;
		}
		
		public Builder zipCode(String aZipCode) {
			params.put(ZIPCODE_KEY, aZipCode);
			return this;
		}

		/**
		 * Build request for given builder params.
		 * 
		 * @return {@link ItemUpdateRequest}.
		 */
		public ItemUpdateRequest build() {
			return new ItemUpdateRequest(this);
		}		

	}

}
