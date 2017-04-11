package com.ebay.park.model;

public class RateModel {

	private long ratingId;
	private long userId;
	private String username;
	private String userImageUrl;
	private String itemImageUrl;
	private String itemName;
	private Ranking ratingStatus;
	private String comment;
	private String rateDate;
	private long itemId;

	public enum Ranking {
		POSITIVE, NEGATIVE, NEUTRAL
	}

	public long getRatingId() {
		return ratingId;
	}

	public void setRatingId(long ratingId) {
		this.ratingId = ratingId;
	}

	public long getUserId() {
		return userId;
	}

	public void setUserId(long userId) {
		this.userId = userId;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
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

	public String getItemName() {
		return itemName;
	}

	public void setItemName(String itemName) {
		this.itemName = itemName;
	}

	public Ranking getRatingStatus() {
		return ratingStatus;
	}

	public void setRatingStatus(Ranking ratingStatus) {
		this.ratingStatus = ratingStatus;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public String getRateDate() {
		return rateDate;
	}

	public void setRateDate(String rateDate) {
		this.rateDate = rateDate;
	}

	public long getItemId() {
		return itemId;
	}

	public void setItemId(long itemId) {
		this.itemId = itemId;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (ratingId ^ (ratingId >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RateModel other = (RateModel) obj;
		if (ratingId != other.ratingId)
			return false;
		return true;
	}

}
