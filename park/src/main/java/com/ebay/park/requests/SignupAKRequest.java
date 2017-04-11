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
 */
public class SignupAKRequest extends BaseParkHttpRequest<SignupModel> {

    private Map<String, Object> mPayload;
    private static boolean withPhone = false;

    private SignupAKRequest(Builder builder) {
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
        if(withPhone) {
            return String.format(ParkUrls.SIGNUP_PHONE, getApiUri());
        } else {
            return String.format(ParkUrls.SIGNUP_EMAIL, getApiUri4());
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
     * {@link SignupAKRequest} builder class.
     *
     * @author Nicolas Fernandez
     */
    public static class Builder {
        private static final String USERNAME = "username";
        private static final String MOBILE_PHONE = "mobilePhone";
        private static final String EMAIL = "email";
        private static final String ACC_KIT_TOKEN = "accountKitToken";
        private static final String LOCATION = "location";
        private static final String LOCATION_NAME = "locationName";
        private static final String LANGUAGE = "lang";
        private static final String DEVICE = "device";
        private static final String DEVICE_ID = "deviceId";
        private static final String DEVICE_TYPE = "deviceType";
        private static final String DEVICE_ID_UNIQUE = "uniqueDeviceId";
        private static final String ZIP_CODE = "zipCode";

        private Map<String, Object> params;

        /**
         * Create a Builder for Signup request. All params of this constructor
         * are required by the server.
         *
         * @param location     The coordinates of the user with format. "-33.234,10.4321"
         * @param locationName The display name of the location of the user
         * @param zipCode      Zip code of the user location
         */
        public Builder(String accKitToken, String location, String locationName, String zipCode) {
            this.params = new HashMap<String, Object>();
            this.params.put(ACC_KIT_TOKEN, accKitToken);
            this.params.put(LOCATION, location);
            this.params.put(LOCATION_NAME, locationName);
            this.params.put(ZIP_CODE, zipCode);
            addLanguage(Locale.getDefault().getLanguage());
        }

        public Builder withPhone(String phone) {
            withPhone = true;
            this.params.put(MOBILE_PHONE, phone);
            return this;
        }

        public Builder withEmail(String email) {
            withPhone = false;
            this.params.put(EMAIL, email);
            return this;
        }

        public Builder withUsername(String username) {
            this.params.put(USERNAME, username);
            return this;
        }

        /**
         * Add language to the request. If the language is not in
         * {@link Constants#SUPPORTED_LANGUAGES} the language will be set to
         * "en" as default.
         *
         * @param lang String representing language.
         */
        public Builder withLanguage(String lang) {
            addLanguage(lang);
            return this;
        }

        /**
         * Add the device id for Google Cloud Messaging.
         *
         * @param registrationId The registration string of the device.
         * @param uDeviceId      The unique device identificator.
         */
        public Builder withGCM(String registrationId, String uDeviceId) {
            HashMap<String, String> gcm = new HashMap<String, String>();
            gcm.put(DEVICE_TYPE, "android");
            gcm.put(DEVICE_ID, registrationId);
            gcm.put(DEVICE_ID_UNIQUE, uDeviceId);
            params.put(DEVICE, gcm);
            return this;
        }

        /**
         * Add the unique device id without adding the id for Google Cloud Messaging.
         *
         * @param uDeviceId The unique device id.
         */
        public Builder withoutGCM(String uDeviceId) {
            HashMap<String, String> device = new HashMap<String, String>();
            device.put(DEVICE_ID_UNIQUE, uDeviceId);
            params.put(DEVICE, device);
            return this;
        }

        private void addLanguage(String language) {
            if (Arrays.asList(Constants.SUPPORTED_LANGUAGES).contains(language)) {
                this.params.put(LANGUAGE, language);
            } else {
                this.params.put(LANGUAGE, Constants.SUPPORTED_LANGUAGES[0]);
            }
        }

        public SignupAKRequest build() {
            return new SignupAKRequest(this);
        }
    }
}