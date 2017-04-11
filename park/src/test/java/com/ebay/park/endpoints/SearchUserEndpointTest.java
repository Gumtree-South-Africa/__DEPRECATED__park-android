package com.ebay.park.endpoints;

import com.ebay.park.TestUtils;
import com.ebay.park.requests.ParkUrls;
import com.ebay.park.requests.SearchUserRequest;
import com.ebay.park.responses.BaseParkResponse;
import com.ebay.park.responses.ResponseCodes;
import com.ebay.park.responses.UserListResponse;

import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static com.jayway.restassured.RestAssured.given;

/**
 * Created by jonatan on 13/08/15.
 */
public class SearchUserEndpointTest extends EndpointTestWithUser {

    public static final String TEST_RADIUS = "50";

    @Test
    public void testSearchUsers() {
        TestUtils.printSeparator();
        System.out.println("Testing discover users...");
        System.out.println("Getting discover users for user: " + mSignupUser.getUsername());

        BaseParkResponse parkResponse = analyseResponse(get());
        Assert.assertEquals(parkResponse.getStatusCode(), ResponseCodes.SUCCESS_CODE);
        UserListResponse mUserList = TestUtils.GSON.fromJson(TestUtils.GSON.toJson(parkResponse.getData()), UserListResponse.class);
        Assert.assertNotNull(mUserList);
    }

    @Test
    public void testSearchUsersWithQuery() {
        TestUtils.printSeparator();
        System.out.println("Testing discover users...");
        System.out.println("Getting discover users for user: " + mSignupUser.getUsername() + " with query: " + TestUtils.TEST_PREFIX);

        Map<String, String> mParams = new HashMap<>();
        mParams.put(SearchUserRequest.Builder.QUERY, TestUtils.TEST_PREFIX);

        BaseParkResponse parkResponse = analyseResponse(get(mParams));
        UserListResponse mUserList = TestUtils.GSON.fromJson(TestUtils.GSON.toJson(parkResponse.getData()), UserListResponse.class);
        Assert.assertNotNull(mUserList);
    }

    @Test
    public void testSearchUsersWithRadius() {
        TestUtils.printSeparator();
        System.out.println("Testing discover users...");
        System.out.println("Getting discover users for user: " + mSignupUser.getUsername() + " with radius: " + TEST_RADIUS);

        Map<String, String> mParams = new HashMap<>();
        mParams.put(SearchUserRequest.Builder.RADIUS, TEST_RADIUS);

        BaseParkResponse parkResponse = analyseResponse(get(mParams));
        UserListResponse mUserList = TestUtils.GSON.fromJson(TestUtils.GSON.toJson(parkResponse.getData()), UserListResponse.class);
        Assert.assertNotNull(mUserList);
    }

    @Test
    public void testSearchUsersWithLocation() {
        TestUtils.printSeparator();
        System.out.println("Testing discover users...");
        System.out.println("Getting discover users for user: " + mSignupUser.getUsername() + " with location: " + mUserData.get(TestUtils.TestUser.LOCATION));

        Map<String, String> mParams = new HashMap<>();
        mParams.put(SearchUserRequest.Builder.LATITUDE, TestUtils.TestUser.LATITUDE_VALUE);
        mParams.put(SearchUserRequest.Builder.LONGITUDE, TestUtils.TestUser.LONGITUDE_VALUE);

        BaseParkResponse parkResponse = analyseResponse(get(mParams));
        UserListResponse mUserList = TestUtils.GSON.fromJson(TestUtils.GSON.toJson(parkResponse.getData()), UserListResponse.class);
        Assert.assertNotNull(mUserList);
    }

    @Override
    protected String getUrl() {
        return String.format(ParkUrls.SEARCH_USER, TestUtils.getApiUri(), mSignupUser.getUsername());
    }
}
