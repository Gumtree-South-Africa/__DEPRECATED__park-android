package com.ebay.park.endpoints;

import com.ebay.park.TestUtils;
import com.ebay.park.model.LoginModel;
import com.ebay.park.model.SignupModel;
import com.ebay.park.requests.ParkUrls;
import com.ebay.park.responses.BaseParkResponse;
import com.ebay.park.responses.ResponseCodes;
import com.ebay.park.utils.Constants;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.response.Response;

import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;

/**
 * Created by jonatan on 10/08/15.
 */
public class LoginEndpointTest {

    @Test
    public void testSuccesfulSignUp(){
        TestUtils.printSeparator();
        System.out.println("Testing a succesful signup");
        Map<String, String> mUserData = TestUtils.TestUser.get();
        System.out.println("With data = " + mUserData.toString());
        Response userRegisterResponse = given().contentType(ContentType.JSON)
                .body(new Gson().toJson(mUserData))
                .post(TestUtils.getUrl(String.format(ParkUrls.SIGNUP, TestUtils.getApiUri())));
        System.out.println("Response is:");
        userRegisterResponse.prettyPrint();

        userRegisterResponse.then().assertThat().body("data.token", notNullValue())
                .and().body("statusCode", is(ResponseCodes.SUCCESS_CODE));

        BaseParkResponse parkResponse = userRegisterResponse.body().as(BaseParkResponse.class);
        Assert.assertEquals(ResponseCodes.SUCCESS_CODE, parkResponse.getStatusCode());
        SignupModel aSignUp = TestUtils.GSON.fromJson(TestUtils.GSON.toJson(parkResponse.getData()), SignupModel.class);
        Assert.assertNotNull(aSignUp);
        Assert.assertNotNull(aSignUp.getToken());
        Assert.assertNotEquals(aSignUp.getToken(), "");
        Assert.assertNotNull(aSignUp.getUsername());
        Assert.assertEquals(aSignUp.getUsername(), mUserData.get("username"));
    }

    @Test
    public void testSuccesfulLogin() {
        TestUtils.printSeparator();

        System.out.println("Testing a succesful login");

        Map<String, String> mUserData = TestUtils.TestUser.get();

        System.out.println("With data " + mUserData.toString());

        System.out.println("Registering user...");
        Response userRegisterResponse = given().contentType(ContentType.JSON)
                .body(new Gson().toJson(mUserData))
                .post(TestUtils.getUrl(String.format(ParkUrls.SIGNUP, TestUtils.getApiUri())));
        System.out.println("Response is:");
        userRegisterResponse.prettyPrint();

        userRegisterResponse.then().assertThat().body("data.token", notNullValue())
                .and().body("statusCode", is(ResponseCodes.SUCCESS_CODE));

        Map<String, String> loginUserData = new HashMap<>();
        loginUserData.put("username", mUserData.get("username"));
        loginUserData.put("password", mUserData.get("password"));
        System.out.println("Login in with " + loginUserData.toString());

        Response loginResponse = given().contentType(ContentType.JSON)
                .body(new Gson().toJson(loginUserData))
                .post(TestUtils.getUrl(String.format(ParkUrls.LOGIN, TestUtils.getApiUri())));
        System.out.println("Response is:");
        loginResponse.prettyPrint();

        loginResponse.then().assertThat().body("data.token", notNullValue()).and()
                .body("statusCode", is(ResponseCodes.SUCCESS_CODE));

        BaseParkResponse parkResponse = loginResponse.body().as(BaseParkResponse.class);
        Assert.assertEquals(ResponseCodes.SUCCESS_CODE, parkResponse.getStatusCode());
        LoginModel aLogin = TestUtils.GSON.fromJson(TestUtils.GSON.toJson(parkResponse.getData()), LoginModel.class);
        Assert.assertNotNull(aLogin);
        Assert.assertNotNull(aLogin.getSessionToken());
        Assert.assertNotEquals(aLogin.getSessionToken(), "");
        Assert.assertNotNull(aLogin.getUsername());
        Assert.assertEquals(aLogin.getUsername(), loginUserData.get("username"));

    }

    @Test
    public void testIncorrectLogin() {
        TestUtils.printSeparator();
        System.out.println("Testing incorrect login...");
        Map<String, String> data = TestUtils.TestUser.get();
        System.out.println("With data = " + data.toString());

        Response response = given().contentType(ContentType.JSON)
                .body(new Gson().toJson(data))
                .post(TestUtils.getUrl(String.format(ParkUrls.LOGIN, TestUtils.getApiUri())));
        System.out.println("Response is:");
        response.prettyPrint();

        response.then().assertThat().body("data.token", nullValue()).and()
                .body("statusCode", is(ResponseCodes.FAIL_CODE));
    }

    @Test
    public void testLogout() {
        TestUtils.printSeparator();

        System.out.println("Testing logout...");
        System.out.println("Creating user to logout...");
        final SignupModel user = TestUtils.registerUser();
        System.out.println("User created: " + user.toString() + "\n");

        System.out.println("Logging out user...");
        Response response = given().contentType(ContentType.JSON)
                .body(logoutBody()).header("token", user.getToken())
                .post(TestUtils.getUrl(String.format(ParkUrls.LOGOUT, TestUtils.getApiUri())));
        System.out.println("Response is");
        response.prettyPrint();

        response.then().body("data.success", is("true")).and()
                .body("statusCode", is(ResponseCodes.SUCCESS_CODE));

    }

    private String logoutBody() {
        JsonObject object = new JsonObject();
        object.addProperty("deviceType", Constants.DEVICE_TYPE);
        object.addProperty("deviceId", TestUtils.randomDeviceId());
        return object.toString();
    }

}
