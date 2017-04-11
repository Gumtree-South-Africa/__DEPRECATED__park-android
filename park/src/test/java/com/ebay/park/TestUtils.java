package com.ebay.park;

import com.ebay.park.configuration.ConfigurationQA;
import com.ebay.park.configuration.ParkConfiguration;
import com.ebay.park.model.SignupModel;
import com.ebay.park.requests.ParkUrls;
import com.ebay.park.responses.BaseParkResponse;
import com.google.gson.Gson;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.response.Response;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;

import java.util.HashMap;
import java.util.Map;

import static com.jayway.restassured.RestAssured.given;


/**
 * Created by jonatan on 07/08/15.
 */
public class TestUtils {

    public static final Gson GSON = new Gson();

    public static final String TEST_PREFIX = "UnitTest_";

    public static ParkConfiguration mConfiguration;

    public static final String SEPARATOR = "-------------------------------------------------------------------------------------------------";

    private static ParkConfiguration getConfiguration(){
        if (mConfiguration == null){
            if (BuildConfig.BUILD_TYPE == ParkApplication.PROD_BUILD_VARIANT){
                mConfiguration = ParkApplication.getConfiguration(ParkApplication.QA_BUILD_VARIANT);
            }else{
                mConfiguration = ParkApplication.getConfiguration();
            }
        }
        return mConfiguration;
    }

    public static String randomUsername() {
        return TEST_PREFIX + RandomStringUtils.randomAlphanumeric(6);
    }

    public static String randomPass() {
        return RandomStringUtils.randomAlphanumeric(8);
    }

    public static String randomEmail() {
        return RandomStringUtils.randomAlphanumeric(8) + "@"
                + RandomStringUtils.randomAlphanumeric(8) + ".com";
    }

    public static String randomItemName() {
        return TEST_PREFIX + RandomStringUtils.randomAlphabetic(11);
    }

    public static String randomItemDesc() {
        return TEST_PREFIX + RandomStringUtils.randomAlphabetic(11);
    }

    public static double randomPrice() {
        return RandomUtils.nextDouble(100, 1000);
    }

    public static String randomDeviceId() {
        return RandomStringUtils.randomAlphabetic(50);
    }

    public static void printSeparator() {
        System.out.println();
        System.out.println(SEPARATOR);
        System.out.println();
    }

    protected static Integer getApiVersion() {
        return 3;
    }

    protected static Integer getApiMinorVersion() {
        return 0;
    }

    private static final String API_URL_FORMAT = "v%1$d.%2$d";

    public final static String getApiUri() {
        return String.format(API_URL_FORMAT, getApiVersion(),
                getApiMinorVersion());
    }

    public final static String getUrl(String aUrl){
        return getConfiguration().getParkRestUrl() + aUrl;
    }

    public static SignupModel registerUser(Map<String, String> aUser) {
        Response userRegister = given().contentType(ContentType.JSON)
                .body(new Gson().toJson(aUser))
                .post(getUrl(String.format(ParkUrls.SIGNUP, getApiUri())));

        String json = GSON.toJson(userRegister.body()
                .as(BaseParkResponse.class).getData());
        return GSON.fromJson(json, SignupModel.class);
    }

    public static SignupModel registerUser() {
        return registerUser(TestUser.get());
    }

    public static class TestUser{

        public static final String USERNAME = "username";
        public static final String PASSWORD = "password";
        public static final String EMAIL = "email";
        public static final String LOCATION = "location";
        public static final String LOCATION_NAME = "locationName";
        public static final String ZIP_CODE = "zipCode";

        public static final String LATITUDE_VALUE = "25.762372";
        public static final String LONGITUDE_VALUE = "-80.185362";
        public static final String LOCATION_FORMAT = "%1$s,%2$s";

        public static Map<String, String> get(){
            Map<String, String> aUserData = new HashMap<String, String>();
            aUserData.put(USERNAME, randomUsername());
            aUserData.put(PASSWORD, randomPass());
            aUserData.put(EMAIL, randomEmail());
            aUserData.put(LOCATION, String.format(LOCATION_FORMAT, LATITUDE_VALUE, LONGITUDE_VALUE));
            aUserData.put(LOCATION_NAME, "Miami, FL");
            aUserData.put(ZIP_CODE, "33160");
            return aUserData;
        }

    }



}
