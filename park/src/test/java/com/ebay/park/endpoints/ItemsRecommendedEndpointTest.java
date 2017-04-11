package com.ebay.park.endpoints;

import com.ebay.park.TestUtils;
import com.ebay.park.requests.ItemsRecommendedRequest;
import com.ebay.park.requests.ParkUrls;
import com.ebay.park.responses.BaseParkResponse;
import com.ebay.park.responses.ItemListResponse;

import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by jonatan on 13/08/15.
 */
public class ItemsRecommendedEndpointTest extends EndpointTestWithUser{

    @Test
    public void testGetRecommendedItems() {
        TestUtils.printSeparator();
        System.out.println("Testing get recommended items...");
        System.out.println("Getting items recommended");

        BaseParkResponse parkResponse = analyseResponse(get());
        ItemListResponse mItemList = TestUtils.GSON.fromJson(TestUtils.GSON.toJson(parkResponse.getData()), ItemListResponse.class);
        Assert.assertNotNull(mItemList);
    }

    @Test
    public void testGetRecommendedItemsWithLocation() {
        TestUtils.printSeparator();
        System.out.println("Testing get recommended items...");
        System.out.println("Getting items recommended with location: " + String.format(TestUtils.TestUser.LOCATION_FORMAT, TestUtils.TestUser.LATITUDE_VALUE, TestUtils.TestUser.LONGITUDE_VALUE));

        Map<String, String> mParams = new HashMap<>();
        mParams.put(ItemsRecommendedRequest.LATITUDE, TestUtils.TestUser.LATITUDE_VALUE);
        mParams.put(ItemsRecommendedRequest.LONGITUDE, TestUtils.TestUser.LONGITUDE_VALUE);

        BaseParkResponse parkResponse = analyseResponse(get(mParams));
        ItemListResponse mItemList = TestUtils.GSON.fromJson(TestUtils.GSON.toJson(parkResponse.getData()), ItemListResponse.class);
        Assert.assertNotNull(mItemList);
    }

    @Override
    protected String getUrl() {
        return String.format(ParkUrls.PUBLIC_RECOMMENDED_ITEMS, TestUtils.getApiUri());
    }
}
