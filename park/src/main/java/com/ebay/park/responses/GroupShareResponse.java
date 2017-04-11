package com.ebay.park.responses;

public class GroupShareResponse {

	private boolean success;
	private String facebookPublishError;
	private String twitterPublishError;
	
	public boolean isSuccess() {
		return success;
	}
	public void setSuccess(boolean success) {
		this.success = success;
	}
	public String getFacebookPublishError() {
		return facebookPublishError;
	}
	public void setFacebookPublishError(String facebookPublishError) {
		this.facebookPublishError = facebookPublishError;
	}
	public String getTwitterPublishError() {
		return twitterPublishError;
	}
	public void setTwitterPublishError(String twitterPublishError) {
		this.twitterPublishError = twitterPublishError;
	}
	
}
