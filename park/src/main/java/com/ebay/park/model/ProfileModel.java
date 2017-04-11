package com.ebay.park.model;

import java.util.List;

/**
 * User profile model.
 * 
 * @author federico.perez
 * 
 */
public class ProfileModel {

	private String username;
	private String name;
	private String lastname;
	private String email;
	private String profilePicture;
	private long creationDate;
	private String zipCode;
	private String locationName;
	private int positiveRatings;
	private int negativeRatings;
	private int neutralRatings;
	private int followers;
	private int following;
	private int offersMade;
	private int offersReceived;
	private int itemsPublishedCount;
	private List<String> userSocials;
	private boolean followedByUser;
	private boolean verified;
	private String status;
	private String url;
	private String phoneNumber;
	private boolean mobileVerified;

	public ProfileModel() {
	}

	public String getLocationName() {
		return locationName;
	}

	public void setLocationName(String locationName) {
		this.locationName = locationName;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
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

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getProfilePicture() {
		return profilePicture;
	}

	public void setProfilePicture(String profilePicture) {
		this.profilePicture = profilePicture;
	}

	public long getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(long creationDate) {
		this.creationDate = creationDate;
	}

	public int getPositiveRatings() {
		return positiveRatings;
	}

	public void setPositiveRatings(int positiveRatings) {
		this.positiveRatings = positiveRatings;
	}

	public int getNegativeRatings() {
		return negativeRatings;
	}

	public void setNegativeRatings(int negativeRatings) {
		this.negativeRatings = negativeRatings;
	}

	public int getNeutralRatings() {
		return neutralRatings;
	}

	public void setNeutralRatings(int neutralRatings) {
		this.neutralRatings = neutralRatings;
	}

	public int getFollowers() {
		return followers;
	}

	public void setFollowers(int followers) {
		this.followers = followers;
	}

	public int getFollowing() {
		return following;
	}

	public void setFollowing(int following) {
		this.following = following;
	}

	public int getOffersMade() {
		return offersMade;
	}

	public void setOffersMade(int offersMade) {
		this.offersMade = offersMade;
	}

	public int getOffersReceived() {
		return offersReceived;
	}

	public void setOffersReceived(int offersReceived) {
		this.offersReceived = offersReceived;
	}

	public int getItemsPublishedCount() {
		return itemsPublishedCount;
	}

	public void setItemsPublishedCount(int itemsPublishedCount) {
		this.itemsPublishedCount = itemsPublishedCount;
	}

	public void setUserSocials(List<String> userSocials) {
		this.userSocials = userSocials;
	}

	public List<String> getUserSocials() {
		return userSocials;
	}

	public boolean isFollowedByUser() {
		return followedByUser;
	}

	public void setFollowedByUser(boolean followedByUser) {
		this.followedByUser = followedByUser;
	}

	public void setVerified(boolean verified) {
		this.verified = verified;
	}

	public boolean isVerified() {
		return verified;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getZipCode() {
		return zipCode;
	}

	public void setZipCode(String zipCode) {
		this.zipCode = zipCode;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public void setMobileVerified(boolean mobileVerified) {
		this.mobileVerified = mobileVerified;
	}

	public boolean isMobileVerified() {
		return mobileVerified;
	}


}