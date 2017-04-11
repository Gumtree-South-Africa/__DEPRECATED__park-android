package com.ebay.park.model;

import java.util.List;

/**
 * Group Model
 * 
 * @author Nicol�s Mat�as Fern�ndez
 * 
 */

public class GroupModel {

	private int id;
	private String name;
	private String description;
	private String pictureUrl;
	private String location;
	private String locationName;
	private String zipCode;
	private int totalSubscribers;
	private int totalItems;
	private int newItems = 0;
	private boolean subscribed;
	private OwnerModel owner;
	private List<SubscriberModel> subscribers;
	private String url;

	@Override
	public String toString() {
		return this.name;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPictureUrl() {
		return pictureUrl;
	}

	public void setPictureUrl(String pictureUrl) {
		this.pictureUrl = pictureUrl;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}
	
	public String getLocationName(){
		return locationName;
	}
	
	public void setLocationName(String locationName){
		this.locationName = locationName;
	}

	public int getTotalSubscribers() {
		return totalSubscribers;
	}

	public void setTotalSubscribers(int totalSubscribers) {
		this.totalSubscribers = totalSubscribers;
	}

	public Boolean getSubscribed() {
		return subscribed;
	}

	public void setSubscribed(Boolean subscribed) {
		this.subscribed = subscribed;
	}

	public int getTotalItems() {
		return totalItems;
	}

	public void setTotalItems(int totalItems) {
		this.totalItems = totalItems;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public OwnerModel getOwner() {
		return owner;
	}

	public void setOwner(OwnerModel owner) {
		this.owner = owner;
	}

	public List<SubscriberModel> getSubscribers() {
		return subscribers;
	}

	public void setSubscribers(List<SubscriberModel> subscribers) {
		this.subscribers = subscribers;
	}

	public int getNewItems() {
		return newItems;
	}

	public void setNewItems(int newItems) {
		this.newItems = newItems;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
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
		GroupModel other = (GroupModel) obj;
		if (id != other.id)
			return false;
		return true;
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

}
