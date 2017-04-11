package com.ebay.park.requests;

import android.util.Pair;

import com.ebay.park.model.CheckValueModel;
import com.ebay.park.responses.BaseParkResponse;
import com.ebay.park.utils.Constants;
import com.google.gson.JsonObject;
import com.octo.android.robospice.persistence.DurationInMillis;

/**
 * Request to check if a username or email is available.
 * 
 * @author federico.perez
 * 
 */
public class CheckValueRequest extends BaseParkHttpRequest<CheckValueModel> {

	private static final String EMAIL_FIELD = "email";
	private static final String USERNAME_FIELD = "username";

	private Field mField;
	private String mValue;

	private enum Field {
		EMAIL, USERNAME
	}

	/**
	 * Builds a request to check for username availability.
	 * 
	 * @param value
	 *            the username.
	 * @return a {@link CheckValueRequest} instance for the given username.
	 */
	public static CheckValueRequest validateUsernameRequest(String value) {
		return new CheckValueRequest(Field.USERNAME, value);
	}

	/**
	 * Builds a request to check for email availability.
	 * 
	 * @param value
	 *            the email.
	 * @return a {@link CheckValueRequest} instance for the given email.
	 */
	public static CheckValueRequest validateEmailRequest(String value) {
		return new CheckValueRequest(Field.EMAIL, value);
	}

	private CheckValueRequest(Field field, String value) {
		super(CheckValueModel.class);
		this.mField = field;
		this.mValue = value;
	}

	@Override
	protected String getContentType() {
		return Constants.APPLICATION_JSON;
	}

	@Override
	public Object getCachekey() {
		return mField.name() + mValue;
	}

	@Override
	public long getCacheExpirationTime() {
		return DurationInMillis.ALWAYS_EXPIRED;
	}

	@Override
	protected String getBody() {
		JsonObject object = new JsonObject();
		switch (mField) {
		case EMAIL:
			object.addProperty("name", EMAIL_FIELD);
			break;
		case USERNAME:
			object.addProperty("name", USERNAME_FIELD);
			break;
		}
		object.addProperty("value", mValue);
		return object.toString();
	}

	@Override
	protected String getUrlFormat() {
		return String.format(ParkUrls.CHECK_USER_VALUE, getApiUri());
	}

	@Override
	protected Method getMethod() {
		return Method.POST;
	}

	@Override
	protected CheckValueModel getRequestModel(BaseParkResponse response) {
		return getGson().fromJson(response.getData().toString(), CheckValueModel.class);
	}

	@Override
	protected Pair<String, String>[] getQueryParameters() {
		return null;
	}

}
