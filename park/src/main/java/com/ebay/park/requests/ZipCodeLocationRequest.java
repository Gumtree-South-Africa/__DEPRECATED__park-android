package com.ebay.park.requests;

import android.util.Pair;

import com.ebay.park.errors.GenericError;
import com.ebay.park.model.ZipCodeLocationModel;
import com.ebay.park.responses.ZipCodesResponse;
import com.globant.roboneck.requests.BaseNeckHttpRequest;
import com.globant.roboneck.requests.BaseNeckRequestException.Error;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.octo.android.robospice.persistence.DurationInMillis;

import java.util.HashMap;
import java.util.Map;

/**
 * Zip Code location request. Gets location from zip code.
 * 
 * @author federico.perez
 * 
 */
public class ZipCodeLocationRequest extends BaseNeckHttpRequest<ZipCodeLocationModel, ZipCodesResponse> {

	private String mZipCode;

	public ZipCodeLocationRequest(String zipCode) {
		super(ZipCodeLocationModel.class);
		this.mZipCode = zipCode;
	}

	@Override
	public Object getCachekey() {
		return mZipCode;
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
	protected Map<String, String> getHeaders() {
		return new HashMap<String, String>();
	}

	@Override
	protected String getUrl() {
		if (mZipCode.startsWith("00")) {
			return ParkUrls.GOOGLE_GEOCODEAPI_PR + String.format(ParkUrls.GOOGLE_GEOCODEAPI_ZIPCODE_PARAM, mZipCode);
		} else {
			return ParkUrls.GOOGLE_GEOCODEAPI_US + String.format(ParkUrls.GOOGLE_GEOCODEAPI_ZIPCODE_PARAM, mZipCode);
		}

	}

	@Override
	protected Method getMethod() {
		return Method.GET;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected Pair<String, String>[] getQueryParameters() {
		Pair<String, String> sensor = Pair.create("sensor", Boolean.FALSE.toString());
		return new Pair[] { sensor };
	}

	@Override
	protected ZipCodesResponse processContent(String responseBody) {
		Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
		return gson.fromJson(responseBody, ZipCodesResponse.class);
	}

	@Override
	protected boolean isLogicError(ZipCodesResponse response) {
		return !response.getStatus().equals("OK");
	}

	@Override
	protected ZipCodeLocationModel getRequestModel(ZipCodesResponse response) {
		ZipCodeLocationModel location = new ZipCodeLocationModel();
		location.setResults(response.getResults());
		return location;
	}

	@Override
	protected Error processError(int httpStatus, ZipCodesResponse response, String responseBody) {
		if (response != null) {
			return new GenericError(httpStatus, httpStatus, response.getStatus());
		}

		return new GenericError(httpStatus, httpStatus, responseBody);
	}

}
