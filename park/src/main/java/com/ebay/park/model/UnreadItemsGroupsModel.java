package com.ebay.park.model;

/**
 * Created by paula.baudo on 9/1/2015.
 */
public class UnreadItemsGroupsModel {

    private int ownedGroupsItems;
    private int subscribedGroupItems;

    public int getOwnedGroupsItems() {
        return ownedGroupsItems;
    }

    public void setOwnedGroupsItems(int ownedGroupsItems) {
        this.ownedGroupsItems = ownedGroupsItems;
    }

    public int getSubscribedGroupItems() {
        return subscribedGroupItems;
    }

    public void setSubscribedGroupItems(int subscribedGroupItems) {
        this.subscribedGroupItems = subscribedGroupItems;
    }
}
