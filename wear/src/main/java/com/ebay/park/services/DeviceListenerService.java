package com.ebay.park.services;

import android.content.Intent;
import android.support.wearable.activity.ConfirmationActivity;

import com.ebay.park.R;
import com.ebay.park.activities.FeaturesActivity;
import com.ebay.park.activities.ItemsActivity;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

/**
 * Created by paula.baudo on 7/29/2016.
 */
public class DeviceListenerService extends WearableListenerService {

    public static final String ERROR = "error";

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        super.onMessageReceived(messageEvent);

        String event = messageEvent.getPath();
        String [] message = event.split("--");

        byte[] bytes = messageEvent.getData();
        if (bytes.length != 0) {
            if (message[0].equals(FeaturesActivity.SERVICE_CALLED_WEAR)) {
                if (message[1].equals(FeaturesActivity.SERVICE_CALLED_WEAR_NEAR)) {
                    Intent itemsIntent = new Intent(this, ItemsActivity.class);
                    itemsIntent.putExtra(ItemsActivity.ITEMS_ARRAY, bytes);
                    startActivity(itemsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                }
            }
        }
    }

}
