package com.ebay.park.responses;

import java.util.List;

public class ItemIdsListResponse {

	private List<Long> itemIds;
	private int totalElements;
	private String noResultsMessage;
	private String noResultsHintMessage;

	public ItemIdsListResponse() {
	}

	public List<Long> getItemsIds() {
		return itemIds;
	}

	public void setItemIds(List<Long> itemIds) {
		this.itemIds = itemIds;
	}

	public int getAmountItemsFound() {
		return totalElements;
	}

	public void setAmountItemsFound(int totalElements) {
		this.totalElements = totalElements;
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