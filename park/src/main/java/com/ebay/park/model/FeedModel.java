package com.ebay.park.model;

public class FeedModel {

	private User user;
	private Item item;
	private String message;
	private Action action;
	private Long conversationId;
	private Boolean followedByUser;
	private long creationDate;
	private long feedId;
	private String groupName;
	private Boolean read;

	public String getUsername() {
		if (user != null)
			return user.username;
		else
			return "";
	}

	public String getProfilePicture() {
		if (user != null)
			return user.profilePicture;
		else
			return "";
	}

	public String getLocationName() {
		if (user != null)
			return user.locationName;
		else
			return "";
	}

	public long getUserId() {
		if (user != null)
			return user.id;
		else
			return -1;
	}

	public String getItemName() {
		if (item != null)
			return item.itemName;
		else
			return "";
	}

	public Boolean getFollowedByUser() {
		return followedByUser;
	}

	public void setFollowedByUser(Boolean followedByUser) {
		this.followedByUser = followedByUser;
	}

	public String getItemPicture() {
		if (item != null)
			return item.picture;
		else
			return "";
	}

	public long getItemId() {
		if (item != null)
			return item.id;
		else
			return -1;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Action getAction() {
		if (action == null)
			return Action.NULL;
		return action;
	}

	public void setAction(Action action) {
		this.action = action;
	}

	public Long getConversationId() {
		return conversationId;
	}

	public void setConversationId(Long conversationId) {
		this.conversationId = conversationId;
	}

	public long getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(long creationDate) {
		this.creationDate = creationDate;
	}

	public boolean isUnread(long date) {
		return creationDate > date;
	}

	private class User {
		long id;
		String username;
		String profilePicture;
		String locationName;
	}

	public String getItemOwnerName() {
		if (item != null && item.owner != null) {
			return item.owner.username;
		}
		return "";
	}

	public String getItemOwnerPicture() {
		if (item != null && item.owner != null) {
			return item.owner.profilePicture;
		}
		return "";
	}

	public String getItemOwnerLocationName(){
		if (item != null && item.owner != null) {
			return item.owner.locationName;
		}
		return "";
	}

	public long getItemOwnerId(){
		if (item != null && item.owner != null) {
			return item.owner.id;
		}
		return -1;
	}

	public String getItemStatus() {
		if (item != null) {
			return item.status;
		} else {
			return "";
		}
	}

	public Boolean getRead() {
		return read;
	}

	public void setRead(Boolean read) {
		this.read = read;
	}

	public long getFeedId() {
		return feedId;
	}

	public void setFeedId(long feedId) {
		this.feedId = feedId;
	}
	
	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	private class Item {
		long id;
		String itemName;
		String picture;
		User owner;
		String status;
	}	

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (feedId ^ (feedId >>> 32));
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
		FeedModel other = (FeedModel) obj;
		if (feedId != other.feedId)
			return false;
		return true;
	}	

	public enum Action {
		// @formatter:off
		USER_RATED, UPDATE_AN_ITEM, UNFOLLOW_USER, SOLD_AN_ITEM, PENDING_RATE, NEW_ITEM, NEW_COMMENT_WHEN_SUBSCRIBE, NEW_COMMENT_ON_ITEM, ITEM_REJECTED, ITEM_EXPIRE, ITEM_BANNED, ITEM_APROVED, FOLLOW_USER, FOLLOW_ITEM, DELETE_AN_ITEM, CONVERSATION_REJECTED, CONVERSATION_ACCEPTED, CHAT_SENT, ADD_ITEM_TO_GROUP, ITEM_ABOUT_TO_EXPIRE, FB_TOKEN_EXPIRED, TW_TOKEN_EXPIRED, FEED_FROM_MODERATION, ITEM_DELETED_FROM_MODERATION_DUPLICATED, ITEM_DELETED_FROM_MODERATION_PICTURES, ITEM_DELETED_FROM_MODERATION_SERVICES, ITEM_DELETED_FROM_MODERATION_MAKEUP, ITEM_DELETED_FROM_MODERATION_ANIMALS, ITEM_DELETED_FROM_MODERATION_COMMISSION, ITEM_DELETED_FROM_MODERATION_STYLE, ITEM_DELETED_FROM_MODERATION_PRICE, ITEM_DELETED_FROM_MODERATION_FORBIDDEN, SOLD_AN_ITEM_FOR_INTERESTED_FOLLOWERS,
		FB_FRIEND_USING_THE_APP, NULL
		// @formatter:on
	}

}
