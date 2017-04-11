package com.ebay.park.endpoints;

import com.ebay.park.TestUtils;
import com.ebay.park.model.ProfileModel;
import com.ebay.park.requests.ChangePassRequest;
import com.ebay.park.requests.GroupCreateRequest;
import com.ebay.park.requests.ParkUrls;
import com.ebay.park.responses.BaseParkResponse;
import com.ebay.park.responses.ResponseCodes;
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
public class ChangePasswordEndpointTest extends EndpointTestWithUser {

    @Test
    public void testChangePassword() {
        System.out.println("Testing change password...");
        System.out.println("Changing password for user: " + mSignupUser.getUsername() + " with password: " + mUserData.get(TestUtils.TestUser.PASSWORD));

        Map<String, String> mPasswordData = new HashMap<>();
        mPasswordData.put(ChangePassRequest.NEW_PASSWORD, TestUtils.randomPass());
        mPasswordData.put(ChangePassRequest.CURRENT_PASSWORD, mUserData.get(TestUtils.TestUser.PASSWORD));

        BaseParkResponse parkResponse = analyseResponse(post(mPasswordData));
        String newToken = TestUtils.GSON.toJson(parkResponse.getData());
        Assert.assertNotNull(newToken);
    }

    @Override
    protected String getUrl() {
        return String.format(ParkUrls.CHANGE_PASS, TestUtils.getApiUri());
    }
}
