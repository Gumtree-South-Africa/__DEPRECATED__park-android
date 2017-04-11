package com.ebay.park.services;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.ebay.park.ParkApplication;
import com.ebay.park.activities.ItemDetailActivity;
import com.ebay.park.activities.ParkActivity;
import com.ebay.park.flow.IntentFactory;
import com.ebay.park.model.ItemWearModel;
import com.ebay.park.requests.BaseParkSessionRequest;
import com.ebay.park.requests.ItemFollowRequest;
import com.ebay.park.requests.ItemUnFollowRequest;
import com.ebay.park.requests.ItemWearListRequest;
import com.ebay.park.responses.ItemWearListResponse;
import com.ebay.park.utils.LocationUtil;
import com.ebay.park.utils.Logger;
import com.ebay.park.utils.PreferencesUtil;
import com.ebay.park.utils.SwrveEvents;
import com.globant.roboneck.common.NeckSpiceManager;
import com.globant.roboneck.requests.BaseNeckRequestException;
import com.globant.roboneck.requests.BaseNeckRequestListener;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;
import com.octo.android.robospice.exception.NoNetworkException;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.swrve.sdk.SwrveSDK;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by nicolfernand on 7/21/16.
 */
public class WearCallListenerService extends WearableListenerService implements NeckSpiceManager.ProgressListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{

    public static String TAG = WearCallListenerService.class.getSimpleName();
    public static final String SERVICE_CALLED_WEAR = "WearFeatureClicked";
    public static final String SERVICE_CALLED_WEAR_LIKE = "Like";
    public static final String USER_NOT_LOGGED = "not_logged";
    public static final String UNLIKE = "Unlike";
    public static final String SERVICE_CALLED_WEAR_NEAR = "Cercanos";
    public static final String SERVICE_CALLED_WEAR_ITEM_IMAGE= "item_image";
    public static final String SERVICE_CALLED_WEAR_EVENT = "events";
    public static final String ITEM_DEVICE = "item_device";
    public static final String ITEM_ID = "item_id";
    static List<ItemWearModel> itemsList;

    private String mNodeId;
    private GoogleApiClient mGoogleApiClient;
    private Tracker mGAnalyticsTracker;
    private Long itemId;
    protected NeckSpiceManager mSpiceManager = new NeckSpiceManager();

    public enum WatchEvents {
        HOME, NEAR, SEARCH, VIP, WORD_SEARCHED
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mSpiceManager = new NeckSpiceManager(this);
        mSpiceManager.start(this);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mGoogleApiClient.connect();

        mGAnalyticsTracker = ParkApplication.getInstance().getTracker(ParkApplication.TrackerName.GLOBAL_TRACKER);
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        super.onMessageReceived(messageEvent);

        String event = messageEvent.getPath();
        mNodeId = messageEvent.getSourceNodeId();

        String [] message = event.split("--");

        if (message[0].equals(SERVICE_CALLED_WEAR)) {
            if (message[1].equals(SERVICE_CALLED_WEAR_NEAR)){
                getItems("");
            } else if (message[1].equals(SERVICE_CALLED_WEAR_LIKE)){
                likeItem(Integer.valueOf(message[2]),message[3]);
            } else {
                getItems(message[2]);
            }
        } else if (message[0].equals(SERVICE_CALLED_WEAR_ITEM_IMAGE)) {
            int pos = Integer.valueOf(message[1]);
            if (itemsList == null){
                getItemsFromPreferences();
            }
            downloadItemImage(itemsList.get(pos).getPictureUrl());
        } else if (message[0].equals(SERVICE_CALLED_WEAR_EVENT)){
            WatchEvents eventToTrack = WatchEvents.valueOf(message[1].toUpperCase());
            trackEvent(eventToTrack, eventToTrack.equals(WatchEvents.WORD_SEARCHED) ? message[2] : "");
        } else if (message[0].equals(ITEM_DEVICE)){
            itemId = Long.valueOf(message[1]);
            if (PreferencesUtil.isParkActivityActive(this)){
                Intent itemsIntent = new Intent(getApplicationContext(), ItemDetailActivity.class);
                itemsIntent.putExtra(ItemDetailActivity.EXTRA_ITEM_ID, itemId);
                startActivity(itemsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            } else {
                Intent itemsIntent = new Intent(this, ParkActivity.class);
                itemsIntent.putExtra(ITEM_ID, itemId);
                startActivity(itemsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            }
            sendVIPConfirmation();
        }
    }

    private void trackEvent(WatchEvents event, String word) {
        switch (event){
            case HOME:
                mGAnalyticsTracker.send(new HitBuilders.EventBuilder().setCategory("User Action")
                        .setAction("Watch: User is in Home Screen").build());
                break;
            case NEAR:
                mGAnalyticsTracker.send(new HitBuilders.EventBuilder().setCategory("User Action")
                        .setAction("Watch: User is in Nearby Items Screen").build());
                break;
            case SEARCH:
                mGAnalyticsTracker.send(new HitBuilders.EventBuilder().setCategory("User Action")
                        .setAction("Watch: User is in Search Screen").build());
                break;
            case VIP:
                mGAnalyticsTracker.send(new HitBuilders.EventBuilder().setCategory("User Action")
                        .setAction("Watch: User is in VIP").build());
                break;
            case WORD_SEARCHED:
                mGAnalyticsTracker.send(new HitBuilders.EventBuilder().setCategory("User Action")
                        .setAction("Watch: User searched a word").setLabel(word).build());
                break;
        }

    }

    private void getItemsFromPreferences() {
        ObjectInputStream ois = null;
        try {
            ois = new ObjectInputStream(new ByteArrayInputStream(PreferencesUtil.getItemsWear(getApplicationContext())));
            itemsList = (ArrayList<ItemWearModel>) ois.readObject();
            ois.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NullPointerException e){
            e.printStackTrace();
        }
    }

    private void likeItem(int pos, String action){
        if (itemsList == null){
            getItemsFromPreferences();
        }
        ItemWearModel item = itemsList.get(pos);
        if (PreferencesUtil.getParkToken(getApplicationContext()) != null) {
            if (action.equals(UNLIKE)) {
                SwrveSDK.event(SwrveEvents.WATCHLIST_REMOVE_ATTEMPT);
                mSpiceManager.execute(new ItemUnFollowRequest(item.getId()), new UnfollowListener());
            } else {
                SwrveSDK.event(SwrveEvents.WATCHLIST_ADD_ATTEMPT);
                mSpiceManager.execute(new ItemFollowRequest(item.getId()), new FollowListener());
            }
        } else {
            sendLikeConfirmation(USER_NOT_LOGGED);
        }
    }

    private class FollowListener extends BaseNeckRequestListener<Boolean> {

        @Override
        public void onRequestError(BaseNeckRequestException.Error error) {
            Map<String,String> payload = new HashMap<>();
            payload.put(SwrveEvents.EVENT_FAIL_KEY, error.getMessage());
            SwrveSDK.event(SwrveEvents.WATCHLIST_ADD_FAIL, payload);
        }

        @Override
        public void onRequestSuccessfull(Boolean success) {
            SwrveSDK.event(SwrveEvents.WATCHLIST_ADD_SUCCESS);
            sendLikeConfirmation(SERVICE_CALLED_WEAR_LIKE);
        }

        @Override
        public void onRequestException(SpiceException exception) {
            Map<String,String> payload = new HashMap<>();
            payload.put(SwrveEvents.EVENT_FAIL_KEY, exception.getMessage());
            SwrveSDK.event(SwrveEvents.WATCHLIST_ADD_FAIL, payload);
            Logger.error(exception.getMessage());
            if (exception instanceof NoNetworkException){
                sendError(exception.getClass().getSimpleName());
            }
        }
    }

    private class UnfollowListener extends FollowListener {

        @Override
        public void onRequestException(SpiceException exception) {
            Map<String,String> payload = new HashMap<>();
            payload.put(SwrveEvents.EVENT_FAIL_KEY, exception.getMessage());
            SwrveSDK.event(SwrveEvents.WATCHLIST_REMOVE_FAIL, payload);
            if (exception instanceof NoNetworkException){
                sendError(exception.getClass().getSimpleName());
            }
        }

        @Override
        public void onRequestSuccessfull(Boolean success) {
            SwrveSDK.event(SwrveEvents.WATCHLIST_REMOVE_SUCCESS);
            sendLikeConfirmation(UNLIKE);
        }

        @Override
        public void onRequestError(BaseNeckRequestException.Error error) {
            Map<String,String> payload = new HashMap<>();
            payload.put(SwrveEvents.EVENT_FAIL_KEY, error.getMessage());
            SwrveSDK.event(SwrveEvents.WATCHLIST_REMOVE_FAIL, payload);
        }
    }

    private void getItems(final String query) {
        Location location = LocationUtil.getLocation(this);

        if (location != null) {
            new LocationUtil.LocationResolverTask(this, location, new LocationUtil.LocationResolverCallback() {
                @Override
                public void onLocationResolved(Location location) {
                    startItemsRequest(location, query);
                }
            }).execute();
        } else {
            startItemsRequest(location, query);
        }
    }

    private void startItemsRequest(Location location, String query) {

        BaseParkSessionRequest<ItemWearListResponse> aRequest;
        Double mLatitude = null;
        Double mLongitude = null;
        if (location != null) {
            mLatitude = location.getLatitude();
            mLongitude = location.getLongitude();
        }
        ItemWearListRequest.Builder builder;
        if (TextUtils.isEmpty(query)){
            builder = new ItemWearListRequest.Builder();
        } else {
            builder = new ItemWearListRequest.Builder().query(query);
        }
        builder = builder.page(((Integer) 0).longValue()).pageSize(((Integer) 24).longValue())
                .categoryId(null).priceFrom(null).priceTo(null).order("published").maxDistance("20");

        if (location != null) {
            builder.withPos(mLatitude, mLongitude);
        }
        aRequest = builder.build();
        mSpiceManager.execute(aRequest, new ItemListListener());
    }

    @Override
    public void onShowProgress() {
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

    private class ItemListListener extends BaseNeckRequestListener<ItemWearListResponse> {

        @Override
        public void onRequestError(BaseNeckRequestException.Error error) {
            Log.d(TAG, error.getMessage());
        }

        @Override
        public void onRequestSuccessfull(ItemWearListResponse itemListResponse) {
            itemsList = itemListResponse.getItems();
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = null;
            try {
                oos = new ObjectOutputStream(bos);
                oos.writeObject(itemListResponse.getItems());
            } catch (IOException e) {
                e.printStackTrace();
            }
            byte[] bytes = bos.toByteArray();
            PreferencesUtil.saveItemsWear(getApplicationContext(), bytes);
            sendItems(bytes);
        }

        @Override
        public void onRequestException(SpiceException exception) {
            Log.d(TAG, exception.getMessage());
            if (exception instanceof NoNetworkException){
                sendError(exception.getClass().getSimpleName());
            }
        }
    }

    private void sendItemImage (Bitmap itemImage) {
        Asset asset = createAssetFromBitmap(itemImage);
        PutDataMapRequest mapRequest = PutDataMapRequest.create("/image");
        DataMap map = mapRequest.getDataMap();
        map.putLong("time", new Date().getTime());
        map.putAsset("itemImage", asset);

        PutDataRequest request = mapRequest.asPutDataRequest();
        PendingResult<DataApi.DataItemResult> pendingResult = Wearable.DataApi
                .putDataItem(mGoogleApiClient, request);
    }

    private void sendItems(byte[] bytes){
        PutDataMapRequest mapRequest = PutDataMapRequest.create("/items");
        DataMap map = mapRequest.getDataMap();
        map.putLong("time", new Date().getTime());
        map.putByteArray("items",bytes);

        PutDataRequest request = mapRequest.asPutDataRequest();
        PendingResult<DataApi.DataItemResult> pendingResult = Wearable.DataApi
                .putDataItem(mGoogleApiClient, request);
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        super.onDataChanged(dataEvents);
    }

    private void downloadItemImage(final String aImagePath) {
        Picasso.with(getApplicationContext()).load(aImagePath).into(new Target() {

            @Override
            public void onPrepareLoad(Drawable arg0) {
                return;
            }

            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom arg1) {
                try {
                    sendItemImage(bitmap);
                } catch (Exception ex) {
                    Logger.error(ex.getMessage());
                }
            }

            @Override
            public void onBitmapFailed(Drawable arg0) {
                Logger.error("Image not downloaded");
                return;
            }
        });
    }

    private static Asset createAssetFromBitmap(Bitmap bitmap) {
        final ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteStream);
        return Asset.createFromBytes(byteStream.toByteArray());
    }

    private void sendError(String message) {
        PutDataMapRequest mapRequest = PutDataMapRequest.create("/error");
        DataMap map = mapRequest.getDataMap();
        map.putLong("time", new Date().getTime());
        map.putString("exception", message);

        PutDataRequest request = mapRequest.asPutDataRequest();
        PendingResult<DataApi.DataItemResult> pendingResult = Wearable.DataApi
                .putDataItem(mGoogleApiClient, request);
    }

    private void sendLikeConfirmation(String like) {
        PutDataMapRequest mapRequest = PutDataMapRequest.create("/like");
        DataMap map = mapRequest.getDataMap();
        map.putLong("time", new Date().getTime());
        map.putString("itemAction", like);

        PutDataRequest request = mapRequest.asPutDataRequest();
        PendingResult<DataApi.DataItemResult> pendingResult = Wearable.DataApi
                .putDataItem(mGoogleApiClient, request);
    }

    private void sendVIPConfirmation() {
        PutDataMapRequest mapRequest = PutDataMapRequest.create("/vip");
        DataMap map = mapRequest.getDataMap();
        map.putLong("time", new Date().getTime());

        PutDataRequest request = mapRequest.asPutDataRequest();
        PendingResult<DataApi.DataItemResult> pendingResult = Wearable.DataApi
                .putDataItem(mGoogleApiClient, request);
    }

}
