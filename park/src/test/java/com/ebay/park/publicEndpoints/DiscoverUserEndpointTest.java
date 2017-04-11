package com.ebay.park.publicEndpoints;

import com.ebay.park.TestUtils;
import com.ebay.park.requests.DiscoverUsersRequest;
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
 * Created by jonatan on 13/08/15.
 */
public class DiscoverUserEndpointTest extends com.ebay.park.endpoints.DiscoverUserEndpointTest{

    @Override
    protected String getUrl() {
        return String.format(ParkUrls.PUBLIC_DISCOVER_USERS, TestUtils.getApiUri());
    }
}
