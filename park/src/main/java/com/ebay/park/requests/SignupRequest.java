package com.ebay.park.requests;

import android.util.Pair;

import com.ebay.park.model.SignupModel;
import com.ebay.park.responses.BaseParkResponse;
import com.ebay.park.utils.Constants;
import com.google.gson.Gson;
import com.octo.android.robospice.persistence.DurationInMillis;

import java.util.*;

/**
 * User signup request.
 * 
 * @author federico.perez
 * 
 */
public class SignupRequest extends BaseParkHttpRequest<SignupModel> {

	private Map<String, Object> mPayload;
    private static boolean withFacebook = false;

	private SignupRequest(Builder builder) {
		super(SignupModel.class);
		mPayload = builder.params;
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
		return new Gson().toJson(mPayload).toString();
	}

	@Override
	protected String getContentType() {
		return Constants.APPLICATION_JSON;
	}

	@Override
	protected String getUrlFormat() {
        if(withFacebook) {
            return String.format(ParkUrls.SIGNUP_FACEBOOK, getApiUri4());
        } else {
            return String.format(ParkUrls.SIGNUP, getApiUri());
        }
	}

	@Override
	protected Method getMethod() {
		return Method.POST;
	}

	@Override
	protected Pair<String, String>[] getQueryParameters() {
		return null;
	}

	@Override
	protected SignupModel getRequestModel(BaseParkResponse response) {
		return getGson().fromJson(response.getData().toString(), SignupModel.class);
	}

	/**
	 * {@link SignupRequest} builder class.
	 * 
	 * @author federico.perez
	 *
	 */
	public static class Builder {
		private static final String USERNAME = "username";
		private static final String PASSWORD = "password";
		private static final String EMAIL = "email";
		private static final String LOCATION = "location";
		private static final String LOCATION_NAME = "locationName";
		private static final String FB_TOKEN = "fb_token";
		private static final String FB_USER_ID = "fb_user_id";
		private static final String LANGUAGE = "lang";
		private static final String DEVICE = "device";
		private static final String DEVICE_ID = "deviceId";
		private static final String DEVICE_TYPE = "deviceType";
		private static final String DEVICE_ID_UNIQUE = "uniqueDeviceId";
		private static final String NAME = "name";
		private static final String LAST_NAME = "lastname";
		private static final String ZIP_CODE = "zipCode";

		private Map<String, Object> params;

		/**
		 * Create a Builder for Signup request. All params of this constructor
		 * are required by the server.
		 * 
		 * @param username
		 *            Username to register the user
		 * @param password
		 *            User password
		 * @param email
		 *            Email to register the user
		 * @param location
		 *            The coordinates of the user with format. "-33.234,10.4321"
		 * @param locationName
		 *            The display name of the location of the user
		 * @param zipCode
		 *            Zip code of the user location
		 */
		public Builder(String username, String password, String email, String location, String locationName,
				String zipCode) {
			this.params = new HashMap<String, Object>();
			this.params.put(USERNAME, username);
			this.params.put(PASSWORD, password);
			this.params.put(EMAIL, email);
			this.params.put(LOCATION, location);
			this.params.put(LOCATION_NAME, locationName);
			this.params.put(ZIP_CODE, zipCode);
			addLanguage(Locale.getDefault().getLanguage());
		}

		/**
		 * Add users's facebook information.
		 * 
		 * @param facebookToken
		 *            User Facebook token.
		 * @param facebookId
		 *            User Facebook Id.
		 */
		public Builder withFacebook(String facebookToken, String facebookId) {
            withFacebook = true;
			this.params.put(FB_TOKEN, facebookToken);
			this.params.put(FB_USER_ID, facebookId);
			return this;
		}

		/**
		 * Add language to the request. If the language is not in
		 * {@link Constants#SUPPORTED_LANGUAGES} the language will be set to
		 * "en" as default.
		 * 
		 * @param lang
		 *            String representing language.
		 */
		public Builder withLanguage(String lang) {
			addLanguage(lang);
			return this;
		}

		/**
		 * Add the device id for Google Cloud Messaging.
		 * 
		 * @param registrationId
		 *            The registration string of the device.
		 * @param uDeviceId
		 * 			  The unique device identificator.
		 */
		public Builder withGCM(String registrationId, String uDeviceId) {
			HashMap<String, String> gcm = new HashMap<String, String>();
			gcm.put(DEVICE_TYPE, "android");
			gcm.put(DEVICE_ID, registrationId);
			gcm.put(DEVICE_ID_UNIQUE,uDeviceId);
			params.put(DEVICE, gcm);
			return this;
		}

		/**
		 * Add the unique device id without adding the id for Google Cloud Messaging.
		 *
		 * @param uDeviceId
		 * 			  The unique device id.
		 */
		public Builder withoutGCM(String uDeviceId) {
			HashMap<String, String> device = new HashMap<String, String>();
			device.put(DEVICE_ID_UNIQUE,uDeviceId);
			params.put(DEVICE, device);
			return this;
		}

		public Builder withPersonalData(String name, String lastname) {
			this.params.put(NAME, name);
			this.params.put(LAST_NAME, lastname);
			return this;
		}

		private void addLanguage(String language) {
			if (Arrays.asList(Constants.SUPPORTED_LANGUAGES).contains(language)) {
				this.params.put(LANGUAGE, language);
			} else {
				this.params.put(LANGUAGE, Constants.SUPPORTED_LANGUAGES[0]);
			}
		}

		public SignupRequest build() {
			return new SignupRequest(this);
		}
	}
}