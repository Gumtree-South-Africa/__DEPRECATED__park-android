package com.ebay.park.endpoints;

import com.ebay.park.TestUtils;
import com.ebay.park.model.ProfileModel;
import com.ebay.park.requests.ParkUrls;
import com.ebay.park.requests.ProfileUpdateRequest;
import com.ebay.park.responses.BaseParkResponse;
import com.ebay.park.responses.ResponseCodes;
import com.ebay.park.utils.FileUtils;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.response.Response;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static com.jayway.restassured.RestAssured.given;

/**
 * Created by jonatan on 10/08/15.
 */
public class ProfileEndpointTest extends EndpointTestWithUser {

    private static String UPDATE_ZIPCODE = "10003";
    private static String UPDATE_COORDINATES = "40.732795, -73.989858";
    private static String UPDATE_LOCATIONNAME = "New York, NY";

    @Test
    public void testGetProfile() {
        TestUtils.printSeparator();
        System.out.println("Testing get profile...");
        System.out.println("Getting profile for user: " + mSignupUser.getUsername());

        BaseParkResponse parkResponse = analyseResponse(get());
        ProfileModel mProfile = TestUtils.GSON.fromJson(TestUtils.GSON.toJson(parkResponse.getData()), ProfileModel.class);
        Assert.assertNotNull(mProfile);
        Assert.assertEquals(mSignupUser.getUsername(), mProfile.getUsername());
    }

    @Ignore
    @Test
    public void testUpdateProfile() {
        TestUtils.printSeparator();
        System.out.println("Testing update profile...");
        System.out.println("Updating profile for user: " + mSignupUser.getUsername());

        Map<String, String> mUpdateProfileData = new HashMap<>();
        mUpdateProfileData.put(ProfileUpdateRequest.Builder.ZIPCODE, UPDATE_ZIPCODE);
        mUpdateProfileData.put(ProfileUpdateRequest.Builder.LOCATION, UPDATE_COORDINATES);
        mUpdateProfileData.put(ProfileUpdateRequest.Builder.LOCATION_NAME, UPDATE_LOCATIONNAME);
        mUpdateProfileData.put(ProfileUpdateRequest.Builder.NAME, "none");
        mUpdateProfileData.put(ProfileUpdateRequest.Builder.LAST_NAME, "none");
        System.out.println("With data = " + mUpdateProfileData.toString());

        BaseParkResponse parkResponse = analyseResponse(
                getRequest()
                    .body(mUpdateProfileData)
                    .put(TestUtils.getUrl(String.format(ParkUrls.UPDATE_PROFILE, TestUtils.getApiUri(),
                        mSignupUser.getUsername()))));
        ProfileModel mProfile = TestUtils.GSON.fromJson(TestUtils.GSON.toJson(parkResponse.getData()), ProfileModel.class);
        Assert.assertNotNull(mProfile);
        Assert.assertEquals(mSignupUser.getUsername(), mProfile.getUsername());
    }

    @Override
    protected String getUrl() {
        return String.format(ParkUrls.USER_PROFILE, TestUtils.getApiUri(),
                mSignupUser.getUsername());
    }
}
