package com.ebay.park.requests;

import android.util.Pair;

import com.ebay.park.responses.BaseParkResponse;
import com.ebay.park.utils.Constants;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.octo.android.robospice.persistence.DurationInMillis;

import java.util.HashMap;
import java.util.Map;

/**
 * Request to post a message to a chat.
 * 
 * @author federico.perez
 *
 */
public class SendChatRequest extends BaseParkSessionRequest<Long> {

	private static final String ITEM_ID_KEY = "itemId";
	private static final String COMMENT_ID_KEY = "comment";
	private static final String CONVERSATION_ID_KEY = "conversationId";
	private static final String BRAND_PUBLISH_KEY = "brandPublish";
	private static final String VERSION_PUBLISH = "versionPublish";

	/**
	 * Params to be send as json body.
	 */
	private Map<String, Object> mParams;

	/**
	 * Constructor for a send chat request.
	 * 
	 * @param itemId
	 *            The id of the item that the chat belongs.
	 * @param conversationId
	 *            The id of the conversation in which to post the message.
	 * @param comment
	 *            The content of the message.
	 */
	public SendChatRequest(long itemId, long conversationId, String comment) {
		super(Long.class);
		mParams = new HashMap<String, Object>();
		mParams.put(ITEM_ID_KEY, itemId);
		mParams.put(COMMENT_ID_KEY, comment);
		if (conversationId > 0) {
			mParams.put(CONVERSATION_ID_KEY, conversationId);
		}
		mParams.put(BRAND_PUBLISH_KEY, "android");
		mParams.put(VERSION_PUBLISH, android.os.Build.VERSION.SDK_INT);
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
		return getGson().toJson(mParams);
	}

	@Override
	protected String getUrlFormat() {
		return String.format(ParkUrls.SEND_CHAT, getApiUri());
	}

	@Override
	protected Method getMethod() {
		return Method.POST;
	}

	@Override
	protected Long getRequestModel(BaseParkResponse response) {
		String json = getGson().toJson(response.getData());
		JsonElement jelement = new JsonParser().parse(json);
		JsonObject jobject = jelement.getAsJsonObject();
		return jobject.get("chatId").getAsLong();
	}

	@Override
	protected Pair<String, String>[] getQueryParameters() {
		return null;
	}

}
