package com.ebay.park.endpoints;

import com.ebay.park.TestUtils;
import com.ebay.park.model.BannerModel;
import com.ebay.park.requests.ParkUrls;
import com.ebay.park.responses.BaseParkResponse;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by jonatan on 12/08/15.
 */
public class BannerEndpointTest extends EndpointTestWithUser {

    @Test
    public void testGetBanner() {
        System.out.println("Testing get banner...");
        System.out.println("Getting banner for user: " + mSignupUser.getUsername());

        BaseParkResponse parkResponse = analyseResponse(get());
        BannerModel mBanner = TestUtils.GSON.fromJson(TestUtils.GSON.toJson(parkResponse.getData()), BannerModel.class);
        Assert.assertNotNull(mBanner);
        Assert.assertNotNull(mBanner.getMessage());
        Assert.assertNotNull(mBanner.getType());
    }

    @Override
    protected String getUrl() {
        return String.format(ParkUrls.BANNER, TestUtils.getApiUri());
    }
}
