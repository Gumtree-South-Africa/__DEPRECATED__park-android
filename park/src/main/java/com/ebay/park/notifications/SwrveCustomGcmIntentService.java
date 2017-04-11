package com.ebay.park.notifications;

import android.app.Notification;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;

import com.ebay.park.R;
import com.ebay.park.fragments.CarouselCategoryFragment;
import com.swrve.sdk.gcm.SwrveGcmIntentService;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by nicolfernand on 6/21/16.
 */
public class SwrveCustomGcmIntentService extends SwrveGcmIntentService {

    @Override
    public NotificationCompat.Builder createNotificationBuilder(String msgText, Bundle msg) {
        Bitmap largeIcon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext())
                .setSmallIcon(R.drawable.viva_app_icon_lollipop)
                .setContentText(msgText)
                .setAutoCancel(true)
                .setLargeIcon(largeIcon)
                .setPriority(Notification.PRIORITY_MAX);
        if (msg.containsKey("title")) {
            mBuilder.setContentTitle(msg.getString("title"));
        } else {
            mBuilder.setContentTitle(getString(R.string.app_name));
        }
        Uri soundUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.arpsynt4);
        mBuilder.setSound(soundUri);
        
        // Retrieve any Swrve Payload from the msg object
        if (msg.containsKey("swrve_screen")) {
            if(msg.getString("swrve_screen").equals("findFbFriends")){
                CarouselCategoryFragment.showFindFbFriendsInAppMsg = true;
            }
        }
        if (msg.containsKey("category_screen")) {
            if(!TextUtils.isEmpty(msg.getString("category_screen"))){
                CarouselCategoryFragment.categoryIdFromPush = msg.getString("category_screen");
            }
        }

        if (msg.containsKey("imageURL")) {
            Bitmap remote_picture = null;

            NotificationCompat.BigPictureStyle notifStyle = new
                    NotificationCompat.BigPictureStyle();
            if (msg.containsKey("title")) {
                notifStyle.setBigContentTitle(msg.getString("title"));
            } else {
                notifStyle.setBigContentTitle(getString(R.string.app_name));
            }
            notifStyle.setSummaryText(msgText);

            try {
                remote_picture = BitmapFactory.decodeStream(
                        (InputStream) new URL(msg.getString("imageURL")).getContent());
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (ClassCastException e) {
                e.printStackTrace();
            } catch (NullPointerException e) {
                e.printStackTrace();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            // Add the picture to the style.
            notifStyle.bigPicture(remote_picture);
            // Build notification
            mBuilder.setStyle(notifStyle);
        } else {
            mBuilder.setStyle((new NotificationCompat.BigTextStyle()).bigText(msgText)).setContentText(msgText).setAutoCancel(true);
        }
        return mBuilder;

    }

}
