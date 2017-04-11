package com.ebay.park.endpoints;

import com.ebay.park.TestUtils;
import com.ebay.park.model.SignupModel;
import com.ebay.park.publicEndpoints.EndpointTest;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.specification.RequestSpecification;

import org.junit.Assert;
import org.junit.BeforeClass;

import java.util.Map;

import static com.jayway.restassured.RestAssured.given;

/**
 * Created by jonatan on 11/08/15.
 */
public abstract class EndpointTestWithUser extends EndpointTest{

    protected static Map<String, String> mUserData;
    protected static SignupModel mSignupUser;

    @BeforeClass
    public static void registerUser() {
        registerNewUser();
    }

    protected static void registerNewUser() {
        mUserData = TestUtils.TestUser.get();
        mSignupUser = TestUtils.registerUser(mUserData);
        Assert.assertNotNull(mSignupUser);
        Assert.assertNotNull(mSignupUser.getToken());
        Assert.assertNotEquals(mSignupUser.getToken(), "");
        Assert.assertEquals(mUserData.get(TestUtils.TestUser.USERNAME), mSignupUser.getUsername());
    }

    public RequestSpecification getRequest(){
        return super.getRequest()
                .header("token", mSignupUser.getToken());
    }

}
