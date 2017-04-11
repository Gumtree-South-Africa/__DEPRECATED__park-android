package com.ebay.park.activities;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.wearable.activity.ConfirmationActivity;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.ebay.park.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.data.FreezableUtils;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class FeaturesActivity extends WearableActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, View.OnClickListener, DataApi.DataListener {

    public static final String SERVICE_CALLED_WEAR = "WearFeatureClicked";
    public static final String SERVICE_CALLED_WEAR_SEARCH = "Buscar";
    public static final String SERVICE_CALLED_WEAR_NEAR = "Cercanos";
    private static final int SPEECH_REQUEST_CODE = 0;
    public static final String SERVICE_CALLED_WEAR_EVENT = "events";

    private static final long CONNECTION_TIME_OUT_MS = 1000;

    public static String TAG = "FeaturesActivity";

    Node mNode; // the connected device to send the message to
    GoogleApiClient mGoogleApiClient;
    private int mError = 0;
    private ImageButton mSearch;
    private ImageButton mNear;
    private ProgressBar mProgressBar;
    private LinearLayout mLayout;
    private Boolean disconnect = true;
    private Boolean search = false;

    public enum WatchEvents {
        HOME, NEAR, SEARCH, VIP, WORD_SEARCHED
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_features);

        //Connect the GoogleApiClient
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        setAmbientEnabled();

        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                mLayout = (LinearLayout) stub.findViewById(R.id.features_layout);
                mSearch = (ImageButton) stub.findViewById(R.id.btn_search);
                mNear = (ImageButton) stub.findViewById(R.id.btn_near);
                mProgressBar = (ProgressBar) stub.findViewById(R.id.progress_bar_features);

                mSearch.setOnClickListener(FeaturesActivity.this);
                mNear.setOnClickListener(FeaturesActivity.this);
            }
        });
    }

    /**
     * Resolve the node = the connected device to send the message to
     */
    private void resolveNode() {
        Wearable.NodeApi.getConnectedNodes(mGoogleApiClient)
                .setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
                    @Override
                    public void onResult(NodeApi.GetConnectedNodesResult nodes) {
                        for (Node node : nodes.getNodes()) {
                            mNode = node;
                        }
                        sendEvent(WatchEvents.HOME.name());
                    }
                });
    }

    @Override
    public void onConnected(Bundle bundle) {
        resolveNode();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Wearable.DataApi.addListener(mGoogleApiClient, this);
        if (disconnect) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        mError = connectionResult.getErrorCode();
        showConnectionMessage();
    }

    private void showConnectionMessage() {
        Intent intent = new Intent(this, ConfirmationActivity.class);
        intent.putExtra(ConfirmationActivity.EXTRA_ANIMATION_TYPE,
                ConfirmationActivity.FAILURE_ANIMATION);
        switch (mError){
            case ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED:
                intent.putExtra(ConfirmationActivity.EXTRA_MESSAGE,
                        getString(R.string.sync_error));
                break;
            case ConnectionResult.NETWORK_ERROR:
                intent.putExtra(ConfirmationActivity.EXTRA_MESSAGE,
                        getString(R.string.network_error));
                break;
            case ConnectionResult.SERVICE_MISSING_PERMISSION:
                intent.putExtra(ConfirmationActivity.EXTRA_MESSAGE,
                        getString(R.string.permission_error));
                break;
            default:
                intent.putExtra(ConfirmationActivity.EXTRA_MESSAGE,
                        getString(R.string.general_error));
                break;
        }
        startActivity(intent);
    }

    /**
     * Send message to mobile handheld
     */
    private void sendMessage(String key) {
        if (mNode != null && mGoogleApiClient!= null && mGoogleApiClient.isConnected()) {
            Log.d(TAG, "-- " + mGoogleApiClient.isConnected());
            Wearable.MessageApi.sendMessage(
                    mGoogleApiClient, mNode.getId(), SERVICE_CALLED_WEAR + "--" + key, null).setResultCallback(
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
        } else {
            mLayout.setVisibility(View.VISIBLE);
            mProgressBar.setVisibility(View.GONE);
        }
    }

    private void sendEvent(String key) {
        if (mNode != null && mGoogleApiClient!= null && mGoogleApiClient.isConnected()) {
            Log.d(TAG, "-- " + mGoogleApiClient.isConnected());
            Wearable.MessageApi.sendMessage(
                    mGoogleApiClient, mNode.getId(), SERVICE_CALLED_WEAR_EVENT + "--" + key, null).setResultCallback(
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

    @Override
    public void onClick(View v) {
        if (mError == 0) {
            switch (v.getId()) {
                case R.id.btn_near:
                    checkIfWearableConnected(true);
                    break;
                case R.id.btn_search:
                    checkIfWearableConnected(false);
                    break;
                default:
                    break;
            }
        } else {
            showConnectionMessage();
        }
    }

    private void retrieveDeviceNode(final Callback callback) {
        final GoogleApiClient client = mGoogleApiClient;
        new Thread(new Runnable() {

            @Override
            public void run() {
                client.blockingConnect(CONNECTION_TIME_OUT_MS, TimeUnit.MILLISECONDS);
                NodeApi.GetConnectedNodesResult result =
                        Wearable.NodeApi.getConnectedNodes(client).await();
                List<Node> nodes = result.getNodes();
                if (nodes.size() > 0) {
                    callback.success();
                } else {
                    callback.failed();
                }
            }
        }).start();
    }

    public void checkIfWearableConnected(final boolean near) {

        retrieveDeviceNode(new Callback() {
            @Override
            public void success() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(near){
                            doNearAction();
                        } else {
                            doSearchAction();
                        }
                    }
                });
            }

            @Override
            public void failed() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showBluetoothConnectionError();
                    }
                });
            }
        });
    }

    private void doNearAction(){
        Log.d(TAG, SERVICE_CALLED_WEAR_NEAR);
        mLayout.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.VISIBLE);
        sendMessage(SERVICE_CALLED_WEAR_NEAR);
    }

    private void doSearchAction(){
        Log.d(TAG, SERVICE_CALLED_WEAR_SEARCH);
        disconnect = false;
        search = true;
        displaySpeechRecognizer();
    }

    private void showBluetoothConnectionError() {
        mLayout.setVisibility(View.VISIBLE);
        mProgressBar.setVisibility(View.GONE);
        Intent intent = new Intent(this, ConfirmationActivity.class);
        intent.putExtra(ConfirmationActivity.EXTRA_ANIMATION_TYPE,
                ConfirmationActivity.FAILURE_ANIMATION);
        intent.putExtra(ConfirmationActivity.EXTRA_MESSAGE,
                        getString(R.string.no_bluetooth_connection));
        startActivity(intent);
    }

    private interface Callback {
        void success();
        void failed();
    }


    @Override
    protected void onPause() {
        super.onPause();
        if (disconnect) {
            Wearable.DataApi.removeListener(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
    }

    private void displaySpeechRecognizer() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        startActivityForResult(intent, SPEECH_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        if (requestCode == SPEECH_REQUEST_CODE && resultCode == RESULT_OK) {
            List<String> results = data.getStringArrayListExtra(
                    RecognizerIntent.EXTRA_RESULTS);
            String spokenText = results.get(0);
            mLayout.setVisibility(View.GONE);
            mProgressBar.setVisibility(View.VISIBLE);
            disconnect = true;
            search = true;
            sendMessage(SERVICE_CALLED_WEAR_SEARCH + "--" + spokenText);
            sendEvent(WatchEvents.WORD_SEARCHED.name() + "--" + spokenText);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        final List<DataEvent> events = FreezableUtils.freezeIterable(dataEvents);
        for (DataEvent event : events) {
            if (event.getType() == DataEvent.TYPE_CHANGED){
                if (event.getDataItem().getUri().getPath().equals("/items")) {
                    DataMapItem dataMapItem = DataMapItem.fromDataItem(event.getDataItem());
                    byte[] bytes = dataMapItem.getDataMap().getByteArray("items");
                    mProgressBar.setVisibility(View.GONE);
                    mLayout.setVisibility(View.VISIBLE);
                    if (bytes.length != 0) {
                        Intent itemsIntent = new Intent(this, ItemsActivity.class);
                        itemsIntent.putExtra(ItemsActivity.ITEMS_ARRAY, bytes);
                        itemsIntent.putExtra(ItemsActivity.FEATURE, search ? SERVICE_CALLED_WEAR_SEARCH : SERVICE_CALLED_WEAR_NEAR);
                        search = false;
                        startActivity(itemsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                    }
                } else if (event.getDataItem().getUri().getPath().equals("/error")){
                    DataMapItem dataMapItem = DataMapItem.fromDataItem(event.getDataItem());
                    if (dataMapItem.getDataMap().getString("exception").equals("NoNetworkException")){
                        mProgressBar.setVisibility(View.GONE);
                        mLayout.setVisibility(View.VISIBLE);
                        Intent intent = new Intent(this, ConfirmationActivity.class);
                        intent.putExtra(ConfirmationActivity.EXTRA_ANIMATION_TYPE,
                                ConfirmationActivity.FAILURE_ANIMATION);
                        intent.putExtra(ConfirmationActivity.EXTRA_MESSAGE,
                                getString(R.string.no_network_error));
                        startActivity(intent);
                    }
                }
            }
        }
    }
}