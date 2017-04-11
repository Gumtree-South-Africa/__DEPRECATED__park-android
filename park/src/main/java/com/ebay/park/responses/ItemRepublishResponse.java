package com.ebay.park.responses;

public class ItemRepublishResponse {

	private long republishedItemId;
	private boolean sharedOnFacebook;
	private boolean sharedOnTwitter;
	
	public long getRepublishedItemId() {
		return republishedItemId;
	}
	public void setRepublishedItemId(long republishedItemId) {
		this.republishedItemId = republishedItemId;
	}
	public boolean isSharedOnFacebook() {
		return sharedOnFacebook;
	}
	public void setSharedOnFacebook(boolean sharedOnFacebook) {
		this.sharedOnFacebook = sharedOnFacebook;
	}
	public boolean isSharedOnTwitter() {
		return sharedOnTwitter;
	}
	public void setSharedOnTwitter(boolean sharedOnTwitter) {
		this.sharedOnTwitter = sharedOnTwitter;
	}
	
}
