package com.ebay.park.endpoints;

import com.ebay.park.TestUtils;
import com.ebay.park.requests.ChangePassRequest;
import com.ebay.park.requests.ForgotPassRequest;
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
import static org.hamcrest.core.Is.is;

/**
 * Created by jonatan on 12/08/15.
 */
public class ForgotPassEndpointTest extends EndpointTestWithUser {

    @Test
    public void testForgetPassword() {
        System.out.println("Testing forgot password...");

        System.out.println("Sending forgot email for user: " + mSignupUser.getUsername() + " with email: " + mUserData.get(TestUtils.TestUser.EMAIL));
        Map<String, String> mEmailData = new HashMap<>();
        mEmailData.put(ForgotPassRequest.EMAIL, mUserData.get(TestUtils.TestUser.EMAIL));

        Response aResponse = post(mEmailData);
        aResponse.then().body("data.success", is("true")).and()
                .body("statusCode", is(ResponseCodes.SUCCESS_CODE));

        BaseParkResponse parkResponse = analyseResponse(aResponse);
        Assert.assertEquals(parkResponse.getStatusCode(), ResponseCodes.SUCCESS_CODE);
    }

    @Override
    protected String getUrl() {
        return String.format(ParkUrls.FORGOT_PASS, TestUtils.getApiUri(),
                mSignupUser.getUsername());
    }
}
