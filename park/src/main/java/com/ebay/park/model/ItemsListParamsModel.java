package com.ebay.park.model;

import java.io.Serializable;

/**
 * Created by nicolfernand on 11/25/16.
 */

public class ItemsListParamsModel implements Serializable{

    private long page;
    private String categoryId;
    private int currentPos;

    public long getPage() {
        return page;
    }

    public void setPage(long page) {
        this.page = page;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public int getCurrentPos() {
        return currentPos;
    }

    public void setCurrentPos(int currentPos) {
        this.currentPos = currentPos;
    }
}
