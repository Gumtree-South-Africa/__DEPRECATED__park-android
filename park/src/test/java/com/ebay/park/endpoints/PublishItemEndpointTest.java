package com.ebay.park.endpoints;

import com.ebay.park.TestUtils;
import com.ebay.park.requests.ParkUrls;
import com.ebay.park.responses.BaseParkResponse;
import com.ebay.park.responses.ResponseCodes;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.response.Response;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static com.jayway.restassured.RestAssured.given;

/**
 * Created by jonatan on 10/08/15.
 */
public class PublishItemEndpointTest extends EndpointTestWithUser {

    @Test
    public void testPublishItem() {
        System.out.println("Testing  publish Item...");

        for (int i = 0; i < 10; i++) {
            BaseParkResponse parkResponse = analyseResponse(post(buildPublishItem()));
            //Assert.assertEquals(ResponseCodes.SUCCESS_CODE, parkResponse.getStatusCode());
        }
    }

    private RequestPayload buildPublishItem() {
        RequestPayload payload = new RequestPayload();
        payload.name = TestUtils.randomItemName();
        payload.description = TestUtils.randomItemDesc();
        payload.versionPublish = "21";
        payload.price = TestUtils.randomPrice();
        payload.location = "";
        payload.locationName = "";
        payload.latitude = "";
        payload.longitude = "";
        payload.zipCode = "";
        payload.shareOnFacebook = false;
        payload.shareOnTwitter = false;
        payload.categoryId = 10;
        payload.pictures = Arrays
                .asList("http://www.greenbiz.com/sites/default/files/imagecache/wide_large/*bottlesshutterstock_79109392.jpg",
                        "http://searchengineland.com/figz/wp-content/seloads/2012/05/google-shopping.jpg",
                        "http://alicarnold.files.wordpress.com/2009/11/new-product.jpg?w=150&150");
        return payload;
    }

    @Override
    protected String getUrl() {
        return String.format(ParkUrls.PUBLISH_ITEM, TestUtils.getApiUri());
    }

    private class RequestPayload {
        String brandPublish = "android";
        String name;
        String description;
        String location;
        String versionPublish;
        Double price;
        String latitude;
        String longitude;
        String locationName;
        String zipCode;
        Boolean shareOnFacebook;
        Boolean shareOnTwitter;
        long categoryId;
        List<Integer> groups = Arrays.asList(1);
        List<String> pictures;
        String fbToken;
        String twToken;
    }

}
