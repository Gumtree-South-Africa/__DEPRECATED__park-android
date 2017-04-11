package com.ebay.park.responses;

import com.ebay.park.model.FeedModel;

import java.util.List;

public class FeedListResponse {
	
	private List<FeedModel> feeds;
	private int totalOfItems;
	private String noResultsMessage;
	private String noResultsHintMessage;

	public FeedListResponse() {
	}

	public List<FeedModel> getItems() {
		return feeds;
	}

	public void setItems(List<FeedModel> feeds) {
		this.feeds = feeds;
	}

	public int getAmountItemsFound() {
		return totalOfItems;
	}

	public void setAmountItemsFound(int amountItemsFound) {
		this.totalOfItems = amountItemsFound;
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
