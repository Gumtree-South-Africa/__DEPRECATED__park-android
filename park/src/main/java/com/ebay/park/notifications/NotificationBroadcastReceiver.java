package com.ebay.park.notifications;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import com.ebay.park.R;
import com.ebay.park.activities.ParkActivity;
import com.ebay.park.fragments.ActivityFeedFragment;
import com.ebay.park.fragments.LeftMenuFragment.LeftMenuOption;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.swrve.sdk.SwrveHelper;
import com.swrve.sdk.gcm.SwrveGcmConstants;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;

import me.leolin.shortcutbadger.ShortcutBadger;

/**
 * {@link BroadcastReceiver} for managing push notifications.
 * 
 * @author federico.perez
 * 
 */
public class NotificationBroadcastReceiver extends BroadcastReceiver {
	/**
	 * Park notification id.
	 */
	public static final int NOTIFICATION_ID = 12346;
	private NotificationManager mNotificationManager;
	private static final String MESSAGE_KEY = "message";
	private static final String BADGE_KEY = "badge";
	private static final String SWRVE_CUSTOM_INTENT_SERVICE = "com.ebay.park.notifications.SwrveCustomGcmIntentService";

	@Override
	public void onReceive(Context context, Intent intent) {
		boolean interceptedIntent = false;

		Bundle extras = intent.getExtras();
		GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(context);
		String messageType = gcm.getMessageType(intent);

		ActivityFeedFragment.sUnreadCount++;

		if (!isApplicationInForeground(context)) {

			if (!extras.isEmpty()) {

				// Call the Swrve intent service if the push contains the Swrve payload _p
				Object rawId = extras.get(SwrveGcmConstants.SWRVE_TRACKING_KEY);
				String msgId = (rawId != null) ? rawId.toString() : null;
				if (!SwrveHelper.isNullOrEmpty(msgId)) {
					// It is a Swrve push!
					interceptedIntent = true;
					ComponentName comp = new ComponentName(context.getPackageName(), SWRVE_CUSTOM_INTENT_SERVICE);
					intent.setComponent(comp);
					context.startService(intent);
				}

				if (!interceptedIntent) {
					// Continue normally if the push is not a Swrve push
					if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
					} else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)) {
					} else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
						String message = "";
						try {
							if (extras.containsKey(MESSAGE_KEY)) {
								message = URLDecoder.decode(extras.getString(MESSAGE_KEY), "UTF-8");
							}
							if (!message.isEmpty()) {
								if(extras.containsKey(BADGE_KEY)){
									int badge = Integer.parseInt(URLDecoder.decode(extras.getString(BADGE_KEY), "UTF-8"));
									if (badge > 0) {
										ShortcutBadger.applyCount(context, badge);
									} else {
										ShortcutBadger.removeCount(context);
									}
								}
								sendNotification(context, message);
							}
						} catch (UnsupportedEncodingException e) {
						} catch (ClassCastException e) {
						} catch (NullPointerException e) {
						} catch (IllegalArgumentException e) {
						}
					}
				}

			}
			setResultCode(Activity.RESULT_OK);
		}
	}

	public Boolean isApplicationInForeground(final Context context) {

		ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningTaskInfo> tasks = am.getRunningTasks(1);
		if (!tasks.isEmpty()) {
			ComponentName topActivity = tasks.get(0).topActivity;
			if (topActivity.getPackageName().equals(context.getPackageName())) {
				return true;// App in Foreground
			}
		}

		return false;// App closed or in Background
	}

	private void sendNotification(Context context, String msg) {
		mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

		final Intent actionIntent = new Intent(context, ParkActivity.class);
		actionIntent.putExtra(ParkActivity.MENU_OPTION_EXTRA, LeftMenuOption.ACTIVITY.ordinal());
		actionIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

		final PendingIntent contentIntent = PendingIntent
				.getActivity(context, 0, actionIntent, PendingIntent.FLAG_UPDATE_CURRENT);

		Uri alarmSound = Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.arpsynt4);

		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
				.setContentTitle(context.getString(R.string.app_name)).setSound(alarmSound).setAutoCancel(true)
				.setContentText(msg);

		if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
			mBuilder.setSmallIcon(R.drawable.ic_launcher);
		} else {
			mBuilder.setSmallIcon(R.drawable.viva_app_icon_lollipop);
			mBuilder.setColor(context.getResources().getColor(R.color.VivaTurquoise));
		}

		mBuilder.setContentIntent(contentIntent);
		mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
	}
}
