package com.ebay.park.endpoints;

import com.ebay.park.TestUtils;
import com.ebay.park.requests.DiscoverUsersRequest;
import com.ebay.park.requests.ItemListRequest;
import com.ebay.park.requests.ParkUrls;
import com.ebay.park.responses.BaseParkResponse;
import com.ebay.park.responses.ResponseCodes;
import com.ebay.park.responses.UserListResponse;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.response.Response;

import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static com.jayway.restassured.RestAssured.given;

/**
 * Created by jonatan on 12/08/15.
 */
public class DiscoverUserEndpointTest extends EndpointTestWithUser {

    @Test
    public void testDiscoverUsers() {
        TestUtils.printSeparator();
        System.out.println("Testing discover users...");
        System.out.println("Getting discover users for user: " + mSignupUser.getUsername());

        BaseParkResponse parkResponse = analyseResponse(get());
        UserListResponse mUserList = TestUtils.GSON.fromJson(TestUtils.GSON.toJson(parkResponse.getData()), UserListResponse.class);
        Assert.assertNotNull(mUserList);
    }

    @Test
    public void testDiscoverUsersWithLocation() {
        TestUtils.printSeparator();
        System.out.println("Testing discover users...");
        System.out.println("Getting discover users for user: " + mSignupUser.getUsername() + " with location: " + mUserData.get(TestUtils.TestUser.LOCATION));

        Map<String, String> mParams = new HashMap<>();
        mParams.put(DiscoverUsersRequest.LATITUDE, TestUtils.TestUser.LATITUDE_VALUE);
        mParams.put(DiscoverUsersRequest.LONGITUDE, TestUtils.TestUser.LONGITUDE_VALUE);

        BaseParkResponse parkResponse = analyseResponse(get(mParams));
        UserListResponse mUserList = TestUtils.GSON.fromJson(TestUtils.GSON.toJson(parkResponse.getData()), UserListResponse.class);
        Assert.assertNotNull(mUserList);
    }

    @Override
    protected String getUrl() {
        return String.format(ParkUrls.DISCOVER_USERS, TestUtils.getApiUri(), mSignupUser.getUsername());
    }
}
