package com.ebay.park.responses;

import com.ebay.park.model.CategoryModel;

import java.util.List;

public class ItemCategoryResponse {

	private List<CategoryModel> categories;

	public List<CategoryModel> getCategories() {
		return categories;
	}

	public void setCategories(List<CategoryModel> cateogries) {
		this.categories = cateogries;
	}

}
