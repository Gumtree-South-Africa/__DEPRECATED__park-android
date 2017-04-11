package com.ebay.park.endpoints;

import com.ebay.park.TestUtils;
import com.ebay.park.model.ItemModel;
import com.ebay.park.requests.ItemListRequest;
import com.ebay.park.requests.ParkUrls;
import com.ebay.park.responses.BaseParkResponse;
import com.ebay.park.responses.ItemListResponse;

import org.hamcrest.Matcher;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.junit.Assert.assertThat;

/**
 * Created by jonatan on 13/08/15.
 */
public class ItemSearchEndpointTest extends EndpointTestWithUser {

    public static final String RADIUS = "20";

    private static final double MIN_PRICE = 3.0;
    private static final double MAX_PRICE = 15.0;

    @Test
    public void testGetAllItems() {
        TestUtils.printSeparator();
        System.out.println("Testing get items...");
        System.out.println("Getting items");

        BaseParkResponse parkResponse = analyseResponse(get());
        ItemListResponse mItemList = TestUtils.GSON.fromJson(TestUtils.GSON.toJson(parkResponse.getData()), ItemListResponse.class);
        Assert.assertNotNull(mItemList);
        Assert.assertNotNull(mItemList.getItems());
        if (mItemList.getAmountItemsFound() == 0){
            Assert.assertNotNull(mItemList.getNoResultsMessage());
            Assert.assertNotNull(mItemList.getNoResultsHint());
            Assert.assertEquals(mItemList.getItems().size(), mItemList.getAmountItemsFound());
        }
    }

    @Test
    public void testGetItemsWithinRadius() {
        TestUtils.printSeparator();
        System.out.println("Testing get items...");
        System.out.println("Getting items with location: " + String.format(TestUtils.TestUser.LOCATION_FORMAT, TestUtils.TestUser.LATITUDE_VALUE, TestUtils.TestUser.LONGITUDE_VALUE) + " and radius: " + RADIUS);

        Map<String, String> mParams = new HashMap<>();
        mParams.put(ItemListRequest.Builder.LATITUDE, TestUtils.TestUser.LATITUDE_VALUE);
        mParams.put(ItemListRequest.Builder.LONGITUDE, TestUtils.TestUser.LONGITUDE_VALUE);
        mParams.put(ItemListRequest.Builder.MAX_DISTANCE, RADIUS);

        BaseParkResponse parkResponse = analyseResponse(get(mParams));
        ItemListResponse mItemList = TestUtils.GSON.fromJson(TestUtils.GSON.toJson(parkResponse.getData()), ItemListResponse.class);
        Assert.assertNotNull(mItemList);
    }

    @Test
    public void testGetItemsForUser() {
        TestUtils.printSeparator();
        System.out.println("Testing get items...");
        System.out.println("Getting items for user: " + mSignupUser.getUsername());

        Map<String, String> mParams = new HashMap<>();
        mParams.put(ItemListRequest.Builder.USERNAME, mUserData.get(TestUtils.TestUser.USERNAME));

        BaseParkResponse parkResponse = analyseResponse(get(mParams));
        ItemListResponse mItemList = TestUtils.GSON.fromJson(TestUtils.GSON.toJson(parkResponse.getData()), ItemListResponse.class);
        Assert.assertNotNull(mItemList);
        Assert.assertNotNull(mItemList.getNoResultsMessage());
        Assert.assertNotNull(mItemList.getNoResultsHint());
        Assert.assertNotNull(mItemList.getItems());
        Assert.assertEquals(mItemList.getItems().size(), mItemList.getAmountItemsFound());
    }

    @Test
    public void testGetItemsWithPriceRange() {
        TestUtils.printSeparator();
        System.out.println("Testing get items...");
        System.out.println("Getting items with price from: " + MIN_PRICE + " to: " + MAX_PRICE);

        Map<String, Double> mParams = new HashMap<>();
        mParams.put(ItemListRequest.Builder.PRICE_FROM, MIN_PRICE);
        mParams.put(ItemListRequest.Builder.PRICE_TO, MAX_PRICE);

        BaseParkResponse parkResponse = analyseResponse(get(mParams));
        ItemListResponse mItemList = TestUtils.GSON.fromJson(TestUtils.GSON.toJson(parkResponse.getData()), ItemListResponse.class);
        Assert.assertNotNull(mItemList);
        Assert.assertNotNull(mItemList.getItems());
        for (ItemModel aItem : mItemList.getItems()){
            assertThat(aItem.getPrice(), between(MIN_PRICE, MAX_PRICE));
        }
    }

    @Test
    public void testGetItemsForCategory() {
        TestUtils.printSeparator();
        System.out.println("Testing get items...");
        System.out.println("Getting items for category: " + mSignupUser.getUsername());

        BaseParkResponse parkResponse = analyseResponse(get());
        ItemListResponse mItemList = TestUtils.GSON.fromJson(TestUtils.GSON.toJson(parkResponse.getData()), ItemListResponse.class);
        Assert.assertNotNull(mItemList);
        Assert.assertNotNull(mItemList.getItems());
    }

    private static Matcher<? super Double> between(double minPrice, double maxPrice) {
        return allOf(greaterThanOrEqualTo(minPrice), lessThanOrEqualTo(maxPrice));
    }

    @Override
    public String getUrl() {
        return String.format(ParkUrls.SEARCH_ITEMS, TestUtils.getApiUri());
    }
}
