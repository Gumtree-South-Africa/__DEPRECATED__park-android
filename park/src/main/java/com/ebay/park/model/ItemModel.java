package com.ebay.park.model;

import java.util.List;

/**
 * Item Model
 * 
 * @author Nicolás Matias Fernández
 * @author federico.perez
 * 
 */
public class ItemModel {

	private Long id;
	private String name;
	private String description;
	private String location;
	private String locationName;
	private Double price;
	private String status;
	private String localizedStatus;
	private String published;
	private int totalOfFollowers;
	private int likes;
	private int totalOfComments;
	private CategoryModel category;
	private List<String> pictures;
	private List<GroupModel> groups;
	private List<CommentModel> comments;
	private String pictureUrl;
	private OwnerModel user;
	private double latitude;
	private double longitude;
	private String zipCode;
	private boolean reported;
	private boolean followedByUser;
	private boolean hasConversations;	
	private String url;
    private boolean newItem;

	public ItemModel() {
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public Double getPrice() {
		return price;
	}

	public void setPrice(Double price) {
		this.price = price;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
	
	public String getLocalizedStatus() {
		return localizedStatus;
	}

	public void setLocalizedStatus(String localizedStatus) {
		this.localizedStatus = localizedStatus;
	}

	public String getPublishedTime() {
		return published;
	}

	public void setPublishedTime(String postDateTime) {
		this.published = postDateTime;
	}

	public int getTotalOfFollowers() {
		return totalOfFollowers;
	}

	public void setTotalOfFollowers(int numberOfFollowers) {
		this.totalOfFollowers = numberOfFollowers;
	}

	public void like(){
		likes=likes+1;
	}

	public void unlike(){
		likes=likes-1;
	}

	public int getLikes(){
        int likeAmmount = totalOfFollowers;
        if (likes != 0){
            likeAmmount = Math.max(0, totalOfFollowers + ((int)Math.signum(likes)));
        }
        return likeAmmount;
	}

	public CategoryModel getCategory() {
		return category;
	}

	public void setCategory(CategoryModel category) {
		this.category = category;
	}

	public List<String> getPictures() {
		return pictures;
	}

	public void setPictures(List<String> pictures) {
		this.pictures = pictures;
	}

	public List<GroupModel> getGroups() {
		return groups;
	}

	public void setGroups(List<GroupModel> groups) {
		this.groups = groups;
	}

	public int getTotalOfComments() {
		return totalOfComments;
	}

	public void setTotalOfComments(int totalOfComments) {
		this.totalOfComments = totalOfComments;
	}

	public List<CommentModel> getComments() {
		return comments;
	}

	public void setComments(List<CommentModel> comments) {
		this.comments = comments;
	}

	public OwnerModel getUser() {
		return user;
	}

	public void setUser(OwnerModel user) {
		this.user = user;
	}

	public double getLatitude() {
		return latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public String getPictureUrl() {
		return pictureUrl;
	}

	public void setPictureUrl(String pictureUrl) {
		this.pictureUrl = pictureUrl;
	}

	public String getLocationName() {
		return locationName;
	}

	public void setLocationName(String locationName) {
		this.locationName = locationName;
	}

	public boolean isReported() {
		return reported;
	}

	public boolean isFollowedByUser() {
		return followedByUser;
	}


	public void setFollowedByUser(boolean followedByUser) {
		this.followedByUser = followedByUser;
	}

	public void setReported(boolean reported) {
		this.reported = reported;
	}	

	public boolean hasConversations() {
		return hasConversations;
	}

	public void setHasConversations(boolean hasConversations) {
		this.hasConversations = hasConversations;
	}
	
	public String getZipCode() {
		return zipCode;
	}

    public boolean isNewItem() {
        return newItem;
    }

    public void setNewItem(boolean newItem) {
        this.newItem = newItem;
    }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
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
		ItemModel other = (ItemModel) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}	

}
