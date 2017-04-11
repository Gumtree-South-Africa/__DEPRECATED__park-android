package com.ebay.park.publicEndpoints;

import com.ebay.park.TestUtils;
import com.ebay.park.responses.BaseParkResponse;
import com.ebay.park.responses.ItemListResponse;
import com.ebay.park.responses.ResponseCodes;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.response.Response;
import com.jayway.restassured.specification.RequestSpecification;

import org.junit.Assert;

import java.util.Map;

import static com.jayway.restassured.RestAssured.given;

/**
 * Created by jonatan on 14/08/15.
 */
public abstract class EndpointTest {

    public RequestSpecification getRequest(){
        return given()
                .contentType(ContentType.JSON);
    }

    public RequestSpecification getRequest(Map<String, ?> mParams){
        return getRequest()
                .params(mParams);
    }

    public Response get(){
        return getRequest().get(withUrl());
    }

    public Response get(Map<String, ?> mParams){
        return getRequest(mParams).get(withUrl());
    }

    protected Response post(Object mParams) {
        return getRequest()
                .body(mParams)
                .post(withUrl());
    }

    private String withUrl(){
        return TestUtils.getUrl(getUrl());
    }

    protected abstract String getUrl();

    protected BaseParkResponse analyseResponse(Response aResponse){
        System.out.println("Response is:");
        aResponse.body().prettyPrint();

        Assert.assertEquals(getExpectedStatusCode(), aResponse.getStatusCode());
        BaseParkResponse parkResponse = aResponse.body().as(BaseParkResponse.class);
        Assert.assertEquals(parkResponse.getStatusCode(), ResponseCodes.SUCCESS_CODE);
        return parkResponse;
    }

    protected int getExpectedStatusCode() {
        return 200;
    }

}
