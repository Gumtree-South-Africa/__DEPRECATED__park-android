package com.ebay.park.requests;

import android.util.Pair;

import com.ebay.park.ParkApplication;
import com.ebay.park.responses.BaseParkResponse;
import com.ebay.park.responses.NotificationConfigResponse;
import com.ebay.park.utils.Constants;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.octo.android.robospice.persistence.DurationInMillis;

public class NotificationConfigUpdateRequest extends BaseParkSessionRequest<Boolean> {

	private NotificationConfigResponse params;

	public NotificationConfigUpdateRequest(NotificationConfigResponse params) {
		super(Boolean.class);
		this.params = params;
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
		return "{\"userConfigurationPerGroup\":" + getGson().toJson(params) + "}";
	}

	@Override
	protected String getUrlFormat() {
		return String.format(ParkUrls.GET_UPDATE_NOTIFICATION_CONFIG, getApiUri(), ParkApplication.getInstance().getUsername());
	}

	@Override
	protected Method getMethod() {
		return Method.POST;
	}

	@Override
	protected Boolean getRequestModel(BaseParkResponse response) {
		String json = getGson().toJson(response.getData());
		JsonElement jelement = new JsonParser().parse(json);
		JsonObject jobject = jelement.getAsJsonObject();
		return jobject.get("success").getAsBoolean();
	}

	@Override
	protected Pair<String, String>[] getQueryParameters() {
		return null;
	}

}
