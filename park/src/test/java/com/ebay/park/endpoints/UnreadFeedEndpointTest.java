package com.ebay.park.endpoints;

import com.ebay.park.TestUtils;
import com.ebay.park.model.UnreadCountModel;
import com.ebay.park.requests.ParkUrls;
import com.ebay.park.responses.BaseParkResponse;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by jonatan on 14/08/15.
 */
public class UnreadFeedEndpointTest extends EndpointTestWithUser{

    @Test
    public void testGetUnreadFeedsCount() {
        System.out.println("Testing get profile...");
        System.out.println("Getting profile for user: " + mSignupUser.getUsername());

        BaseParkResponse parkResponse = analyseResponse(get());
        UnreadCountModel mUnreads = TestUtils.GSON.fromJson(TestUtils.GSON.toJson(parkResponse.getData()), UnreadCountModel.class);
        Assert.assertNotNull(mUnreads);
        Assert.assertTrue(mUnreads.getUnreadFeeds() == 0);
        Assert.assertTrue(mUnreads.getUnreadGroupItems() == 0);
    }

    @Override
    protected String getUrl() {
        return String.format(ParkUrls.UNREAD_COUNT, TestUtils.getApiUri(),
                mSignupUser.getUsername());
    }
}
