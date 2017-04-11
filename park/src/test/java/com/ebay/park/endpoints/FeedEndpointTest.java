package com.ebay.park.endpoints;

import com.ebay.park.TestUtils;
import com.ebay.park.model.ProfileModel;
import com.ebay.park.requests.ParkUrls;
import com.ebay.park.responses.BaseParkResponse;
import com.ebay.park.responses.FeedListResponse;
import com.ebay.park.responses.ResponseCodes;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.response.Response;

import org.junit.Assert;
import org.junit.Test;

import static com.jayway.restassured.RestAssured.given;

/**
 * Created by jonatan on 13/08/15.
 */
public class FeedEndpointTest extends EndpointTestWithUser {

    @Test
    public void testGetFeeds() {
        System.out.println("Testing get unread feeds count...");
        System.out.println("Getting amount of unread feeds for user: " + mSignupUser.getUsername());

        BaseParkResponse parkResponse = analyseResponse(get());
        FeedListResponse mFeedList = TestUtils.GSON.fromJson(TestUtils.GSON.toJson(parkResponse.getData()), FeedListResponse.class);
        Assert.assertNotNull(mFeedList);
        Assert.assertNotNull(mFeedList.getNoResultsMessage());
        Assert.assertNotNull(mFeedList.getNoResultsHint());
        Assert.assertNotNull(mFeedList.getItems());
        Assert.assertEquals(mFeedList.getItems().size(), mFeedList.getAmountItemsFound());
    }

    @Override
    protected String getUrl() {
        return String.format(ParkUrls.ACTIVITY_FEED, TestUtils.getApiUri(),
                mSignupUser.getUsername());
    }
}
