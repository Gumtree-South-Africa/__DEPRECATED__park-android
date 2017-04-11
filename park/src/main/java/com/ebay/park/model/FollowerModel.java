package com.ebay.park.model;

/**
 * Follower and following model.
 * 
 * @author federico.perez
 * 
 */
public class FollowerModel {

	private long userId;
	private String username;
	private String email;
	private String name;
	private String lastname;
	private boolean followedByUser;
	private String profilePicture;
	private String locationName;
	private String location;
	private String friendOf;

	public String getProfilePicture() {
		return profilePicture;
	}

	public void setProfilePicture(String profilePicture) {
		this.profilePicture = profilePicture;
	}

	public String getLocationName() {
		return locationName;
	}

	public void setLocationName(String locationName) {
		this.locationName = locationName;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLastname() {
		return lastname;
	}

	public void setLastname(String lastname) {
		this.lastname = lastname;
	}

	public void setFollowsUser(boolean followsUser) {
		this.followedByUser = followsUser;
	}

	public boolean isFollowingActiveUser() {
		return followedByUser;
	}
	
	public long getId() {
		return userId;
	}

	public void setId(long id) {
		this.userId = id;
	}

	public String getFriendOf() {
		return friendOf;
	}

	public void setFriendOf(String friendOf) {
		this.friendOf = friendOf;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((username == null) ? 0 : username.hashCode());
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
		FollowerModel other = (FollowerModel) obj;
		if (username == null) {
			if (other.username != null)
				return false;
		} else if (!username.equals(other.username))
			return false;
		return true;
	}
	
}
