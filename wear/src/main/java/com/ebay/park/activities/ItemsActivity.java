package com.ebay.park.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.wearable.activity.ConfirmationActivity;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.WatchViewStub;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.ebay.park.R;
import com.ebay.park.model.ItemWearModel;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.data.FreezableUtils;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import static com.ebay.park.activities.FeaturesActivity.SERVICE_CALLED_WEAR;
import static com.ebay.park.activities.FeaturesActivity.SERVICE_CALLED_WEAR_NEAR;

public class ItemsActivity extends WearableActivity implements View.OnClickListener, DataApi.DataListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, OnMapReadyCallback {

    public static final String ITEMS_ARRAY = "items_array";
    public static final String FEATURE = "feature";
    public static final String SERVICE_CALLED_WEAR_ITEM_LIKE = "Like";
    public static final String UNLIKE = "Unlike";
    public static final String USER_NOT_LOGGED = "not_logged";
    public static final String SERVICE_CALLED_WEAR_ITEM_IMAGE= "item_image";
    public static final String TAG = "ItemsActivity";
    public static final String ITEM_DEVICE = "item_device";
    private static final float MAP_ZOOM = 12;
    private static final int MAP_ANIMATION_DURATION = 2000;

    private List<ItemWearModel> mItemList = new ArrayList<>();
    private TextView mPrice;
    private TextView mName;
    private TextView mDescription;
    private ImageButton mPrev;
    private ImageButton mNext;
    private ImageButton mLike;
    private ImageView mImage;
    private ScrollView mScrollView;
    private LinearLayout mEmptyView;
    private int index = 0;
    private GoogleMap mMap;
    private MapFragment mMapFragment;
    private Boolean mLikeRequestActive = false;
    private Boolean mVIPRequestActive = false;
    private ImageButton mGoToDeviceButton;
    Node mNode; // the connected device to send the message to
    GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_items);

        //Connect the GoogleApiClient
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        setAmbientEnabled();

        if (getIntent().getExtras().containsKey(ITEMS_ARRAY)){
            byte[] itemsArray = getIntent().getExtras().getByteArray(ITEMS_ARRAY);

            ObjectInputStream ois = null;
            try {
                ois = new ObjectInputStream(new ByteArrayInputStream(itemsArray));
                mItemList = (ArrayList<ItemWearModel>) ois.readObject();
                ois.close();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                mPrice = (TextView) stub.findViewById(R.id.tv_price);
                mName = (TextView) stub.findViewById(R.id.tv_name);
                mDescription = (TextView) stub.findViewById(R.id.tv_description);
                mImage = (ImageView) stub.findViewById(R.id.iv_item_photo);
                mPrev = (ImageButton) stub.findViewById(R.id.btn_previous);
                mLike = (ImageButton) stub.findViewById(R.id.btn_like);
                mNext = (ImageButton) stub.findViewById(R.id.btn_next);
                mScrollView = (ScrollView) stub.findViewById(R.id.scroll_items_layout);
                mEmptyView = (LinearLayout) stub.findViewById(R.id.no_items_layout);
                mGoToDeviceButton = (ImageButton) stub.findViewById(R.id.see_on_device);

//                mImage.setOnClickListener(ItemsActivity.this);
                mLike.setOnClickListener(ItemsActivity.this);
                mPrev.setOnClickListener(ItemsActivity.this);
                mNext.setOnClickListener(ItemsActivity.this);
                mGoToDeviceButton.setOnClickListener(ItemsActivity.this);

                setNavigationButtons();

                mMapFragment = (MapFragment) getFragmentManager()
                        .findFragmentById(R.id.map);
                mMapFragment.getMapAsync(ItemsActivity.this);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_previous:
                if (index > 0) {
                    index--;
                } else if (index == 0){
                    index = mItemList.size()-1;
                }
                mScrollView.fullScroll(View.FOCUS_UP);
                mImage.setImageResource(R.drawable.placeholder_img);
                askForItemImage();
                setItemView(mItemList.get(index));
                mLikeRequestActive = false;
                mVIPRequestActive = false;
                break;
            case R.id.btn_next:
                if (index < (mItemList.size()-1)) {
                    index++;
                } else if (index == mItemList.size()-1){
                    index = 0;
                }
                mScrollView.fullScroll(View.FOCUS_UP);
                mImage.setImageResource(R.drawable.placeholder_img);
                askForItemImage();
                setItemView(mItemList.get(index));
                mLikeRequestActive = false;
                mVIPRequestActive = false;
                break;
            case R.id.btn_like:
                if (!mLikeRequestActive) {
                    sendMessageLikeItem();
                }
                break;
            case R.id.iv_item_photo:
                Intent imageIntent = new Intent(getApplicationContext(), ImageActivity.class);
                imageIntent.putExtra(ImageActivity.IMAGE_BITMAP, ((BitmapDrawable) mImage.getDrawable()).getBitmap());
                startActivity(imageIntent);
                break;
            case R.id.see_on_device:
                if (!mVIPRequestActive) {
                    sendMessageItemOnDevice(mItemList.get(index).getId().toString());
                }
                break;
            default:
                break;
        }
    }

    private void sendMessageItemOnDevice(String key) {
        if (mNode != null && mGoogleApiClient!= null && mGoogleApiClient.isConnected()) {
            Log.d(TAG, "-- " + mGoogleApiClient.isConnected());
            mVIPRequestActive = true;
            Wearable.MessageApi.sendMessage(
                    mGoogleApiClient, mNode.getId(), ITEM_DEVICE + "--" + key, null).setResultCallback(
                    new ResultCallback<MessageApi.SendMessageResult>() {
                        @Override
                        public void onResult(MessageApi.SendMessageResult sendMessageResult) {
                            if (!sendMessageResult.getStatus().isSuccess()) {
                                Log.e(TAG, "Failed to send message with status code: "
                                        + sendMessageResult.getStatus().getStatusCode());
                            }
                        }
                    }
            );
        }
    }

    private void sendEvent(String key) {
        if (mNode != null && mGoogleApiClient!= null && mGoogleApiClient.isConnected()) {
            Log.d(TAG, "-- " + mGoogleApiClient.isConnected());
            Wearable.MessageApi.sendMessage(
                    mGoogleApiClient, mNode.getId(), FeaturesActivity.SERVICE_CALLED_WEAR_EVENT + "--" + key, null).setResultCallback(
                    new ResultCallback<MessageApi.SendMessageResult>() {
                        @Override
                        public void onResult(MessageApi.SendMessageResult sendMessageResult) {
                            if (!sendMessageResult.getStatus().isSuccess()) {
                                Log.e(TAG, "Failed to send message with status code: "
                                        + sendMessageResult.getStatus().getStatusCode());
                            }
                        }
                    }
            );
        }
    }

    private void setNavigationButtons(){
        if (mItemList.size()>1){
            mPrev.setBackgroundResource(R.mipmap.btn_previous);
            mNext.setBackgroundResource(R.mipmap.btn_next);
        } else {
            mPrev.setBackgroundResource(R.mipmap.btn_previous_off);
            mPrev.setEnabled(false);
            mNext.setBackgroundResource(R.mipmap.btn_next_off);
            mNext.setEnabled(false);
        }
    }

    private void setItemView(ItemWearModel item){
        mPrice.setText(String.format(getString(R.string.price), formatPrice(item.getPrice()).replace("$", "")));
        mName.setText(item.getName());
        if (TextUtils.isEmpty(item.getDescription())){
            mDescription.setVisibility(View.GONE);
        } else {
            mDescription.setText(item.getDescription());
            mDescription.setVisibility(View.VISIBLE);
        }
        mLike.setBackgroundResource(item.isFollowedByUser() ? R.mipmap.btn_liked :
                R.mipmap.btn_unliked);
        onMapReady(mMap);
    }

    /**
     * Send message asking item image to mobile handheld
     */
    private void sendMessageForItemImage(String Key) {

        if (mNode != null && mGoogleApiClient!= null && mGoogleApiClient.isConnected()) {
            Log.d(TAG, "-- " + mGoogleApiClient.isConnected());
            Wearable.MessageApi.sendMessage(
                    mGoogleApiClient, mNode.getId(), SERVICE_CALLED_WEAR_ITEM_IMAGE + "--" + Key, null).setResultCallback(

                    new ResultCallback<MessageApi.SendMessageResult>() {
                        @Override
                        public void onResult(MessageApi.SendMessageResult sendMessageResult) {
                            if (!sendMessageResult.getStatus().isSuccess()) {
                                Log.e(TAG, "Failed to send message with status code: "
                                        + sendMessageResult.getStatus().getStatusCode());
                            }
                        }
                    }
            );
        }
    }

    /**
     * Send message to like an item to mobile handheld
     */
    private void sendMessageLikeItem() {
        if (mNode != null && mGoogleApiClient!= null && mGoogleApiClient.isConnected()) {
            Log.d(TAG, "-- " + mGoogleApiClient.isConnected());
            mLikeRequestActive = true;
            Wearable.MessageApi.sendMessage(
                    mGoogleApiClient, mNode.getId(), SERVICE_CALLED_WEAR + "--" +
                            SERVICE_CALLED_WEAR_ITEM_LIKE + "--" +
                            Integer.toString(index) + "--" +
                            (mItemList.get(index).isFollowedByUser() ? UNLIKE : SERVICE_CALLED_WEAR_ITEM_LIKE), null)
                    .setResultCallback(new ResultCallback<MessageApi.SendMessageResult>() {
                                           @Override
                                           public void onResult(MessageApi.SendMessageResult sendMessageResult) {
                                               if (!sendMessageResult.getStatus().isSuccess()) {
                                                   Log.e(TAG, "Failed to send message with status code: "
                                                           + sendMessageResult.getStatus().getStatusCode());
                                               }
                                           }
                                       }
                    );
        }

    }

    public static String formatPrice(Number price) {
        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US);
        return currencyFormatter.format(price).replaceAll("\\.00", "");
    }

    public void askForItemImage() {
        Log.d(TAG, SERVICE_CALLED_WEAR_ITEM_IMAGE);
        sendMessageForItemImage(String.valueOf(index));
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        final List<DataEvent> events = FreezableUtils.freezeIterable(dataEvents);
        for (DataEvent event : events) {
            if (event.getType() == DataEvent.TYPE_CHANGED){
                if (event.getDataItem().getUri().getPath().equals("/image")) {
                    DataMapItem dataMapItem = DataMapItem.fromDataItem(event.getDataItem());
                    Asset itemAsset = dataMapItem.getDataMap().getAsset("itemImage");
                    new SetImageTask().execute(itemAsset);
                } else if (event.getDataItem().getUri().getPath().equals("/like")){
                    DataMapItem dataMapItem = DataMapItem.fromDataItem(event.getDataItem());
                    String result = dataMapItem.getDataMap().getString("itemAction");
                    mLikeRequestActive = false;
                    switch (result){
                        case SERVICE_CALLED_WEAR_ITEM_LIKE:
                            mLike.setBackgroundResource(R.mipmap.btn_liked);
                            mItemList.get(index).setFollowedByUser(true);
                            break;
                        case USER_NOT_LOGGED:
                            Intent intent = new Intent(this, ConfirmationActivity.class);
                            intent.putExtra(ConfirmationActivity.EXTRA_ANIMATION_TYPE,
                                    ConfirmationActivity.FAILURE_ANIMATION);
                            intent.putExtra(ConfirmationActivity.EXTRA_MESSAGE,
                                    getString(R.string.like_session_error));
                            startActivity(intent);
                            break;
                        case UNLIKE:
                            mLike.setBackgroundResource(R.mipmap.btn_unliked);
                            mItemList.get(index).setFollowedByUser(false);
                            break;
                        default:
                            break;
                    }
                } else if (event.getDataItem().getUri().getPath().equals("/vip")){
                    Intent intent = new Intent(this, ConfirmationActivity.class);
                    intent.putExtra(ConfirmationActivity.EXTRA_ANIMATION_TYPE,
                            ConfirmationActivity.SUCCESS_ANIMATION);
                    intent.putExtra(ConfirmationActivity.EXTRA_MESSAGE,
                            "Anuncio abierto con éxito");
                    mVIPRequestActive = false;
                    startActivity(intent);
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Wearable.DataApi.removeListener(mGoogleApiClient, this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        if(mMap == null && googleMap != null) {
            mMap = googleMap;
        }

        if (mItemList.size() > 0) {
            LatLng location = new LatLng(mItemList.get(index).getLatitude(), mItemList.get(index).getLongitude());
            CameraPosition cameraPosition = CameraPosition.builder().target(location).zoom(MAP_ZOOM).build();
            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), MAP_ANIMATION_DURATION, null);
        }

        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                Intent itemsIntent = new Intent(getApplicationContext(), MapActivity.class);
                itemsIntent.putExtra(MapActivity.LATITUDE, mItemList.get(index).getLatitude());
                itemsIntent.putExtra(MapActivity.LONGITUDE, mItemList.get(index).getLongitude());
                startActivity(itemsIntent);
            }
        });
    }

    private class SetImageTask extends AsyncTask<Asset, Void, Bitmap> {

        protected Bitmap doInBackground(Asset... asset) {
            return loadBitmapFromAsset(asset[0]);
        }
        protected void onPostExecute(Bitmap result) {
            if (mItemList.size() > 0) {
                mImage.setImageBitmap(result);
            }
        }
    }

    public Bitmap loadBitmapFromAsset(Asset asset) {
        if (asset != null) {
            ConnectionResult result =
                    mGoogleApiClient.blockingConnect(20000, TimeUnit.MILLISECONDS);
            if (!result.isSuccess()) {
                return null;
            }
            // convert asset into a file descriptor and block until it's ready
            InputStream assetInputStream = Wearable.DataApi.getFdForAsset(
                    mGoogleApiClient, asset).await().getInputStream();

            if (assetInputStream == null) {
                Log.w(TAG, "Requested an unknown Asset.");
                return null;
            }

            // decode the stream into a bitmap
            return BitmapFactory.decodeStream(assetInputStream);
        } else {
            return null;
        }
    }

    /**
     * Resolve the node = the connected device to send the message to
     */
    private void resolveNode() {
        Wearable.DataApi.addListener(mGoogleApiClient, this);
        Wearable.NodeApi.getConnectedNodes(mGoogleApiClient)
                .setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
                    @Override
                    public void onResult(NodeApi.GetConnectedNodesResult nodes) {
                        for (Node node : nodes.getNodes()) {
                            mNode = node;
                        }
                        if (mItemList.size() > 0) {
                            mScrollView.setVisibility(View.VISIBLE);
                            mEmptyView.setVisibility(View.GONE);
                            mImage.setImageResource(R.drawable.placeholder_img);
                            askForItemImage();
                            setItemView(mItemList.get(index));
                        } else {
                            mScrollView.setVisibility(View.GONE);
                            mEmptyView.setVisibility(View.VISIBLE);
                        }
                        sendEvent(FeaturesActivity.WatchEvents.VIP.name());
                        if (getIntent().getExtras().containsKey(FEATURE)){
                            if (getIntent().getExtras().getString(FEATURE).equals(SERVICE_CALLED_WEAR_NEAR)){
                                sendEvent(FeaturesActivity.WatchEvents.NEAR.name());
                            } else {
                                sendEvent(FeaturesActivity.WatchEvents.SEARCH.name());
                            }
                        }
                    }
                });
    }

    @Override
    public void onConnected(Bundle bundle) {
        resolveNode();
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
    }
}
