package com.ebay.park.model;

public class PendingRateModel {

	private long ratingId;
	private long userIdToRate;
	private String usernameToRate;
	private String itemName;
	private String userImageUrl;
	private String itemImageUrl;
	private long itemId;

	public long getRatingId() {
		return ratingId;
	}

	public void setRatingId(long ratingId) {
		this.ratingId = ratingId;
	}

	public long getUserIdToRate() {
		return userIdToRate;
	}

	public void setUserIdToRate(long userIdToRate) {
		this.userIdToRate = userIdToRate;
	}

	public String getUsernameToRate() {
		return usernameToRate;
	}

	public void setUsernameToRate(String usernameToRate) {
		this.usernameToRate = usernameToRate;
	}

	public String getItemName() {
		return itemName;
	}

	public void setItemName(String itemName) {
		this.itemName = itemName;
	}

	public long getItemId() {
		return itemId;
	}

	public void setItemId(long itemId) {
		this.itemId = itemId;
	}

	public String getUserImageUrl() {
		return userImageUrl;
	}

	public void setUserImageUrl(String userImageUrl) {
		this.userImageUrl = userImageUrl;
	}

	public String getItemImageUrl() {
		return itemImageUrl;
	}

	public void setItemImageUrl(String itemImageUrl) {
		this.itemImageUrl = itemImageUrl;
	}
	
}
