package com.ebay.park.responses;

import com.ebay.park.model.FollowerModel;

import java.util.List;

public class UserListResponse {
	
	private List<FollowerModel> users;
	private List<FollowerModel> friends;
	private String noResultsMessage;
	private String noResultsHintMessage;
	private int totalElements;

	public List<FollowerModel> getUsers() {
		return users;
	}

	public void setUsers(List<FollowerModel> users) {
		this.users = users;
	}

	public List<FollowerModel> getFriends() {
		return friends;
	}

	public void setFriends(List<FollowerModel> friends) {
		this.friends = friends;
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

	public long getAmountItemsFound() {
		return totalElements;
	}
	
	public void setAmountItemsFound(int totalElements){
		this.totalElements = totalElements;
	}

}
