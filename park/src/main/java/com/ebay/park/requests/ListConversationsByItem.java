package com.ebay.park.requests;

import android.annotation.SuppressLint;
import android.util.Pair;

import com.ebay.park.responses.BaseParkResponse;
import com.ebay.park.responses.ConversationListResponse;
import com.ebay.park.utils.Constants;
import com.google.gson.Gson;
import com.octo.android.robospice.persistence.DurationInMillis;

import java.util.ArrayList;
import java.util.List;

/**
 * Request to get all chats list for an item.
 * 
 * @author federico.perez
 *
 */
public class ListConversationsByItem extends BaseParkSessionRequest<ConversationListResponse> {

	/**
	 * Page of conversations to get.
	 */
	private String mPage;

	/**
	 * Page size.
	 */
	private String mPageSize;

	/**
	 * The role for the chat list.
	 */
	private String mRole;

	/**
	 * The time the last request for this list was made.
	 */
	private long mLastRequest;

	/**
	 * Id of the item.
	 */
	private long mItemId;

	/**
	 * Builds a request for finding conversations as a buyer for a certain item.
	 * 
	 * @param itemId
	 *            The id of the item.
	 */
	public static ListConversationsByItem searchExistingNegotiations(long itemId) {
		return new ListConversationsByItem(0, 1000, itemId, -1, Constants.ROLE_BUYER);
	}

	/**
	 * Constructor for request, pagination is mandatory.
	 * 
	 * @param page
	 *            Must be equal or greater than zero.
	 * @param pageSize
	 *            Must be greater than zero.
	 * @param lastRequest
	 *            The time the last request for this list was made.
	 * @param role
	 *            Chat for buyer or seller. Use
	 *            {@link ListConversationsByItem#ROLE_BUYER} or
	 *            {@link ListConversationsByItem#ROLE_SELLER}
	 */
	public ListConversationsByItem(int page, int pageSize, long id, long lastRequest, String role) {
		super(ConversationListResponse.class);
		if (page < 0 || pageSize <= 0) {
			throw new IllegalArgumentException("Page must be greater or equals 0 and pageSize must be greater than 0.");
		}
		this.mPage = String.valueOf(page);
		this.mPageSize = String.valueOf(pageSize);
		this.mRole = role;
		this.mLastRequest = lastRequest;
		this.mItemId = id;
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
		return null;
	}

	@SuppressLint("DefaultLocale")
	@Override
	protected String getUrlFormat() {
		return String.format(ParkUrls.CONVERSATIONS_FOR_ITEM, getApiUri(), mItemId);
	}

	@Override
	protected Method getMethod() {
		return Method.GET;
	}

	@Override
	protected ConversationListResponse getRequestModel(BaseParkResponse response) {
		final Gson gson = getGson();
		String json = gson.toJson(response.getData());
		return gson.fromJson(json, ConversationListResponse.class);
	}

	@SuppressLint("SimpleDateFormat")
	@SuppressWarnings("unchecked")
	@Override
	protected Pair<String, String>[] getQueryParameters() {
		List<Pair<String, String>> list = new ArrayList<Pair<String, String>>();
		list.add(Pair.create("page", mPage));
		list.add(Pair.create("pageSize", mPageSize));
		list.add(Pair.create("role", mRole));
		list.add(Pair.create("lastRequest", String.valueOf(mLastRequest)));
		return list.toArray(new Pair[1]);
	}
}
