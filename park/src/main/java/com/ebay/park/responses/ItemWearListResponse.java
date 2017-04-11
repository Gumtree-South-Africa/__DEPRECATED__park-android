package com.ebay.park.responses;

import com.ebay.park.model.ItemWearModel;

import java.util.List;

/**
 * Created by paula.baudo on 8/1/2016.
 */
public class ItemWearListResponse {

    private List<ItemWearModel> items;
    private int totalElements;
    private String noResultsMessage;
    private String noResultsHintMessage;

    public ItemWearListResponse() {
    }

    public List<ItemWearModel> getItems() {
        return items;
    }

    public void setItems(List<ItemWearModel> items) {
        this.items = items;
    }

    public String getNoResultsHintMessage() {
        return noResultsHintMessage;
    }

    public void setNoResultsHintMessage(String noResultsHintMessage) {
        this.noResultsHintMessage = noResultsHintMessage;
    }

    public int getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(int totalElements) {
        this.totalElements = totalElements;
    }

    public String getNoResultsMessage() {
        return noResultsMessage;
    }

    public void setNoResultsMessage(String noResultsMessage) {
        this.noResultsMessage = noResultsMessage;
    }
}
