package com.ebay.park.requests;

import android.text.TextUtils;
import android.util.Pair;

import com.ebay.park.ParkApplication;
import com.ebay.park.model.MyProfileModel;
import com.ebay.park.responses.BaseParkResponse;
import com.ebay.park.utils.Constants;
import com.octo.android.robospice.persistence.DurationInMillis;

import java.util.HashMap;
import java.util.Map;

/**
 * Profile Update Request
 * 
 * @author Nicolás Matias Fernández
 * 
 */

public class ProfileUpdateRequest extends BaseParkSessionRequest<MyProfileModel> {

	private Map<String, String> mBody;

	private ProfileUpdateRequest(Builder builder) {
		super(MyProfileModel.class);
		mBody = builder.params;
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
		return getGson().toJson(mBody);
	}

	@Override
	protected String getUrlFormat() {
		return String.format(ParkUrls.UPDATE_PROFILE, getApiUri(), ParkApplication.getInstance().getUsername());
	}

	@Override
	protected Method getMethod() {
		return Method.PUT;
	}

	@Override
	protected MyProfileModel getRequestModel(BaseParkResponse response) {
		String json = getGson().toJson(response.getData());
		return getGson().fromJson(json, MyProfileModel.class);
	}

	@Override
	protected Pair<String, String>[] getQueryParameters() {
		return null;
	}

	public static class Builder {
		public static final String NAME = "name";
		public static final String LAST_NAME = "lastname";
		public static final String LOCATION = "location";
		public static final String LOCATION_NAME = "locationName";
		public static final String ZIPCODE = "zipCode";
		private static final String MOBILE = "mobile";
		private static final String GENDER = "gender";
		private static final String BIRTHDAY = "birthday";
		public static final String PICTURE = "picture";

		public static final String GENDER_MALE = "M";
		public static final String GENDER_FEMALE = "F";

		private Map<String, String> params;

		public Builder(String name, String lastName) {
			params = new HashMap<String, String>();
			params.put(NAME, name);
			params.put(LAST_NAME, lastName);
		}

		public Builder withLocation(String location, String locationName) {
			params.put(LOCATION, location);
			params.put(LOCATION_NAME, locationName);
			return this;
		}
		
		public Builder withZipCode(String zipCode){
			params.put(ZIPCODE, zipCode);
			return this;
		}

		public Builder withMobile(String mobile) {
			params.put(MOBILE, mobile);
			return this;
		}

		public Builder withGender(String gender) {
			if (GENDER_MALE.equals(gender) || GENDER_FEMALE.equals(gender)) {
				params.put(GENDER, gender);
			}
			return this;
		}

		public Builder withBirthday(String birthday) {
			params.put(BIRTHDAY, birthday);
			return this;
		}

		public Builder withPicture(String picture) {
			if (!TextUtils.isEmpty(picture)) {
				params.put(PICTURE, picture);
			}
			return this;
		}

		public ProfileUpdateRequest build() {
			return new ProfileUpdateRequest(this);
		}
	}
}