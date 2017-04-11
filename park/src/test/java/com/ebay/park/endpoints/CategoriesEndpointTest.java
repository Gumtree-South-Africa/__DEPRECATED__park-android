package com.ebay.park.endpoints;

import com.ebay.park.TestUtils;
import com.ebay.park.requests.ParkUrls;
import com.ebay.park.responses.BaseParkResponse;
import com.ebay.park.responses.ItemCategoryResponse;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by jonatan on 07/08/15.
 */
public class CategoriesEndpointTest extends EndpointTestWithUser {

    @Test
    public void testCategories() {
        System.out.println("Testing categories endpoint: ");

        BaseParkResponse parkResponse = analyseResponse(get());
        ItemCategoryResponse categories = TestUtils.GSON.fromJson(
                TestUtils.GSON.toJson(parkResponse.getData()), ItemCategoryResponse.class);
        Assert.assertNotNull(categories.getCategories());
        Assert.assertTrue(categories.getCategories().size() > 0);
        System.out.println("There are " + categories.getCategories().size() + " categories");

        TestUtils.printSeparator();
    }

    @Override
    protected String getUrl() {
        return String.format(ParkUrls.PUBLIC_ITEMS_CATEGORIES,
                TestUtils.getApiUri());
    }
}
