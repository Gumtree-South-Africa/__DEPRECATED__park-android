package com.ebay.park.endpoints;

import com.ebay.park.TestUtils;
import com.ebay.park.requests.ItemListRequest;
import com.ebay.park.requests.ParkUrls;
import com.ebay.park.requests.RatesListRequest;
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
 * Created by jonatan on 13/08/15.
 */
public class RateEndpointTest extends EndpointTestWithUser{

    @Test
    public void testGetRatesAsBuyer() {
        TestUtils.printSeparator();
        System.out.println("Testing get rates as buyer...");
        System.out.println("Getting rate list for user: " + mSignupUser.getUsername() + " with role: " + RatesListRequest.BUYER);

        Map<String, String> mParams = new HashMap<>();
        mParams.put(RatesListRequest.USERNAME, mSignupUser.getUsername());
        mParams.put(RatesListRequest.ROLE, RatesListRequest.BUYER);

        BaseParkResponse parkResponse = analyseResponse(get(mParams));
        RatesListRequest.RatesListResponse mRateList = TestUtils.GSON.fromJson(TestUtils.GSON.toJson(parkResponse.getData()), RatesListRequest.RatesListResponse.class);
        Assert.assertNotNull(mRateList);
        Assert.assertNotNull(mRateList.getNoResultsMessage());
        Assert.assertNotNull(mRateList.getNoResultsHint());
        Assert.assertNotNull(mRateList.getRatings());
        Assert.assertEquals(mRateList.getRatings().size(), mRateList.getAmountDataFound());
    }

    @Test
    public void testGetRatesAsSeller() {
        TestUtils.printSeparator();
        System.out.println("Testing get rates as seller...");
        System.out.println("Getting rate list for user: " + mSignupUser.getUsername() + " with role: " + RatesListRequest.SELLER);

        Map<String, String> mParams = new HashMap<>();
        mParams.put(RatesListRequest.USERNAME, mSignupUser.getUsername());
        mParams.put(RatesListRequest.ROLE, RatesListRequest.SELLER);

        BaseParkResponse parkResponse = analyseResponse(get(mParams));
        RatesListRequest.RatesListResponse mRateList = TestUtils.GSON.fromJson(TestUtils.GSON.toJson(parkResponse.getData()), RatesListRequest.RatesListResponse.class);
        Assert.assertNotNull(mRateList);
        Assert.assertNotNull(mRateList.getNoResultsMessage());
        Assert.assertNotNull(mRateList.getNoResultsHint());
        Assert.assertNotNull(mRateList.getRatings());
        Assert.assertEquals(mRateList.getRatings().size(), mRateList.getAmountDataFound());
    }

    @Override
    protected String getUrl() {
        return String.format(ParkUrls.PUBLIC_GET_RATES, TestUtils.getApiUri());
    }
}
