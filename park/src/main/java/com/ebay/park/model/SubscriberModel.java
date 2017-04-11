package com.ebay.park.model;

/**
 * Subscriber Model
 * 
 * @author Nicolás Matias Fernández
 * 
 */
public class SubscriberModel {

	private long userId;
	private long itemOwner;
	private String username;
	private String picture;

	public long getUserId() {
		return userId;
	}

	public void setUserId(long userId) {
		this.userId = userId;
	}

	public long getItemOwner() {
		return itemOwner;
	}

	public void setItemOwner(long itemOwner) {
		this.itemOwner = itemOwner;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPicture() {
		return picture;
	}

	public void setPicture(String picture) {
		this.picture = picture;
	}

}