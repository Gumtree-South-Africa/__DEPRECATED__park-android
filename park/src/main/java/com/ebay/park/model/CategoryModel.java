package com.ebay.park.model;

/**
 * Item category model.
 * 
 * @author federico.perez
 * 
 */
public class CategoryModel {

	private long id;
	private String name;
	private String pictureUrl;
	private String color;
	private Boolean selectable;

	public CategoryModel() {
	}

	public CategoryModel(int id, String name) {
		this.id = id;
		this.name = name;
	}

	@Override
	public String toString() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setImage(String image) {
		this.pictureUrl = image;
	}

	public String getImage() {
		return pictureUrl;
	}

	public String getColor() {
		return color;
	}

	public void setColor(String color) {
		this.color = color;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getId() {
		return id;
	}

	public Boolean getSelectable() {
		return selectable;
	}

	public void setSelectable(Boolean selectable) {
		this.selectable = selectable;
	}

}