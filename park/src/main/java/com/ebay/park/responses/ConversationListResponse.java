package com.ebay.park.responses;

import com.ebay.park.model.ConversationModel;

import java.util.List;

public class ConversationListResponse {
	
	private List<ConversationModel> conversations;
	private int totalResults;
	private String noResultsMessage;
	private String noResultsHintMessage;
	
	public ConversationListResponse() {
	}

	public List<ConversationModel> getItems() {
		return conversations;
	}

	public void setItems(List<ConversationModel> items) {
		this.conversations = items;
	}

	public int getAmountTotalItemsFound() {
		return totalResults;
	}

	public void setAmountTotalItemsFound(int amountItemsFound) {
		this.totalResults = amountItemsFound;
	}

	public String getNoResultsMessage() {
		return noResultsMessage;
	}

	public void setNoResultsMessage(String noResultsMessage) {
		this.noResultsMessage = noResultsMessage;
	}

	public String getNoResultsHint() {
		return noResultsHintMessage;
	}

	public void setNoResultsHint(String noResultsHint) {
		this.noResultsHintMessage = noResultsHint;
	}

}
