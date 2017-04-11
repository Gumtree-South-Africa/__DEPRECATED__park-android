package com.ebay.park.utils;

import android.content.Context;
import android.os.Build;
import android.telephony.TelephonyManager;

import com.ebay.park.base.BaseActivity;

import java.util.UUID;

/**
 * Created by paula.baudo on 11/6/2015.
 */
public class DeviceUtils {

    /**
     * Creating an unique device id that persists between installs and uninstalls
     * @param activity
     *      Actual activity
     */
    public static String getUniqueDeviceId(BaseActivity activity){
        final TelephonyManager telephonyManager = (TelephonyManager) activity.getBaseContext()
                .getSystemService(Context.TELEPHONY_SERVICE);

        final String tmDevice, tmSerial, androidId;
        tmDevice = "" + telephonyManager.getDeviceId();
        tmSerial = "" + telephonyManager.getSimSerialNumber();
        androidId = "" + android.provider.Settings.Secure.getString(activity.getContentResolver(),
                android.provider.Settings.Secure.ANDROID_ID);

        UUID deviceUuid = new UUID(androidId.hashCode(), ((long)tmDevice.hashCode() << 32) | tmSerial.hashCode());
        return deviceUuid.toString();
    }

    /**
     * Returns the status bar height in pixels
     * @param context
     */
    public static int getStatusBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    /**
     * Returns the navigation bar height in pixels
     * @param context
     */
    public static int getNavigationBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    /**
     * Checks if the OS installed on the device is API 21 or higher
     * @return
     */
    public static Boolean isDeviceLollipopOrHigher(){
        return (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP);
    }
}
