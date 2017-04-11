package com.ebay.park.model;

public class OwnerModel {
	
	private long id;
	private String username;
	private String profilePicture;
	private String locationName;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public void setPictureUrl(String pictureUrl) {
		this.profilePicture = pictureUrl;
	}

	public String getPictureUrl() {
		return profilePicture;
	}

	public String getLocationName() {
		return locationName;
	}

	public void setLocationName(String locationName) {
		this.locationName = locationName;
	}

}
