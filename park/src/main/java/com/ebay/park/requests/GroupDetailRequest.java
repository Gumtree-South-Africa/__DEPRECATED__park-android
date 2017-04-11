package com.ebay.park.requests;

import android.util.Pair;

import com.ebay.park.model.GroupModel;
import com.ebay.park.responses.BaseParkResponse;
import com.ebay.park.utils.Constants;
import com.octo.android.robospice.persistence.DurationInMillis;

/**
 * Group Detail Request
 * 
 * @author Nicolás Matias Fernández
 * 
 */
public class GroupDetailRequest extends BaseParkSessionRequest<GroupModel> {

	private long mId;

	public GroupDetailRequest(long id) {
		super(GroupModel.class);
		this.mId = id;
	}

	@Override
	protected String getContentType() {
		return Constants.APPLICATION_JSON;
	}

	@Override
	public Object getCachekey() {
		return "GroupDetailRequest" + mId;
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
		return String.format(ParkUrls.GET_GROUP, getApiUri(), mId);
	}

	@Override
	protected Method getMethod() {
		return Method.GET;
	}

	@Override
	protected GroupModel getRequestModel(BaseParkResponse response) {
		String json = getGson().toJson(response.getData());
		return getGson().fromJson(json, GroupModel.class);
	}

	@Override
	protected Pair<String, String>[] getQueryParameters() {
		return null;
	}

}
