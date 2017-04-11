package com.ebay.park.model;

public class LoginModel {
	private String token;
	private String username;
	private String profilePicture;

	public String getSessionToken() {
		return token;
	}

	public void setSessionToken(String sessionToken) {
		this.token = sessionToken;
	}

	public String getProfilePicture() {
		return profilePicture;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public void setProfilePicture(String profilePicture) {
		this.profilePicture = profilePicture;
	}

}