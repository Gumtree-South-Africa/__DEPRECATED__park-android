package com.ebay.park.responses;

import com.ebay.park.model.ItemModel;

import java.util.List;

public class ItemListResponse {

	private List<ItemModel> items;
	private int totalElements;
	private String noResultsMessage;
	private String noResultsHintMessage;

	public ItemListResponse() {
	}

	public List<ItemModel> getItems() {
		return items;
	}

	public void setItems(List<ItemModel> items) {
		this.items = items;
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