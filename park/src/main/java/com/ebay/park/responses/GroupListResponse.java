package com.ebay.park.responses;

import com.ebay.park.model.GroupModel;

import java.util.List;

public class GroupListResponse {

	private List<GroupModel> groups;
	private int totalElements;
	private String noResultsMessage;
	private String noResultsHintMessage;

	public GroupListResponse() {
	}

	public List<GroupModel> getGroups() {
		return groups;
	}

	public void setGroups(List<GroupModel> groups) {
		this.groups = groups;
	}

	public int getTotalGroupsFound() {
		return totalElements;
	}

	public void setTotalGroupsFound(int totalElements) {
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