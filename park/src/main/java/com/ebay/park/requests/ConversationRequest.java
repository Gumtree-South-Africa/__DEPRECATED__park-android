package com.ebay.park.requests;

import android.annotation.SuppressLint;
import android.util.Pair;

import com.ebay.park.model.ConversationModel;
import com.ebay.park.responses.BaseParkResponse;
import com.ebay.park.utils.Constants;
import com.octo.android.robospice.persistence.DurationInMillis;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Request to get a conversation from server.
 * 
 * @author federico.perez
 *
 */
public class ConversationRequest extends BaseParkSessionRequest<ConversationModel> {

	private long mConversationId;

	public ConversationRequest(long conversationId) {
		super(ConversationModel.class);
		this.mConversationId = conversationId;
	}

	@Override
	protected String getContentType() {
		return Constants.APPLICATION_JSON;
	}

	@Override
	public Object getCachekey() {
		return "Conversation" + mConversationId;
	}

	@Override
	public long getCacheExpirationTime() {
		return DurationInMillis.ONE_SECOND * 2;
	}

	@Override
	protected String getBody() {
		return null;
	}

	@SuppressLint("DefaultLocale")
	@Override
	protected String getUrlFormat() {
		return String.format(ParkUrls.GET_CONVERSATION, getApiUri4(), mConversationId);
	}

	@Override
	protected Method getMethod() {
		return Method.GET;
	}

	@Override
	protected ConversationModel getRequestModel(BaseParkResponse response) {
		String json = getGson().toJson(response.getData());
		return getGson().fromJson(json, ConversationModel.class);
	}

	@SuppressWarnings("unchecked")
	@SuppressLint("SimpleDateFormat")
	@Override
	protected Pair<String, String>[] getQueryParameters() {
		List<Pair<String, String>> list = new ArrayList<Pair<String, String>>();
		list.add(Pair.create("lastRequest", String.valueOf(new Date().getTime())));
		return list.toArray(new Pair[1]);
	}

}
