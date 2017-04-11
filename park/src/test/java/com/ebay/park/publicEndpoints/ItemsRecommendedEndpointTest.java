package com.ebay.park.publicEndpoints;

import com.ebay.park.TestUtils;
import com.ebay.park.model.ProfileModel;
import com.ebay.park.requests.DiscoverUsersRequest;
import com.ebay.park.requests.ItemsRecommendedRequest;
import com.ebay.park.requests.ParkUrls;
import com.ebay.park.responses.BaseParkResponse;
import com.ebay.park.responses.ItemListResponse;
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
public class ItemsRecommendedEndpointTest extends com.ebay.park.endpoints.ItemsRecommendedEndpointTest{

    @Override
    protected String getUrl() {
        return String.format(ParkUrls.PUBLIC_RECOMMENDED_ITEMS, TestUtils.getApiUri());
    }
}
