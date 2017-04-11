package com.ebay.park.utils;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;

import com.ebay.park.base.BaseActivity;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.IOException;

/**
 * Utils for Google Cloud Messaging.
 * 
 * @author federico.perez
 *
 */
public final class GCMUtils {
	public static final String SENDER_ID = "1074889515860";
	private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

	/**
	 * Check device for Play Services APK.
	 * 
	 * @param context
	 *            {@link BaseActivity} to get play services.
	 * @return true if device supports Play Services.
	 */
	public static boolean checkPlayServices(BaseActivity context) {
		int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(context);
		if (resultCode != ConnectionResult.SUCCESS) {
			if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
				GooglePlayServicesUtil.getErrorDialog(resultCode, context, PLAY_SERVICES_RESOLUTION_REQUEST).show();
			} else {
				Logger.error("Device not supported");
			}
			return false;
		}
		return true;
	}

	/**
	 * Gets the current registration ID for application on GCM service.
	 * <p>
	 * If result is empty, the app needs to register.
	 *
	 * @return registration ID, or empty string if there is no existing
	 *         registration ID.
	 */
	public static String getRegistrationId(BaseActivity activity) {
		String registrationId = PreferencesUtil.getGCMRegistration(activity);
		if (registrationId.isEmpty()) {
			Logger.info("Registration not found.");
			return "";
		}
		int registeredVersion = PreferencesUtil.getAppVersionForGCMRegistration(activity);
		int currentVersion = getAppVersion(activity);
		if (registeredVersion != currentVersion) {
			Logger.info("App version changed.");
			return "";
		}
		return registrationId;
	}

	/**
	 * Register on Google Cloud Messaging. This method cannot be executed on
	 * MainThread.
	 * 
	 * @param context
	 * @return
	 */
	public static String registerOnGCM(BaseActivity context) {
		String regid = "";
		try {
			GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(context);
			regid = gcm.register(SENDER_ID);

			PreferencesUtil.setGCMRegistration(context, regid);
			PreferencesUtil.setAppVersionForGCMRegistration(context, getAppVersion(context));
		} catch (IOException ex) {
			Logger.error(ex.getMessage());
			// TODO handle error
		}
		return regid;
	}

	/**
	 * @return Application's version code from the {@code PackageManager}.
	 */
	private static int getAppVersion(BaseActivity context) {
		try {
			PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
			return packageInfo.versionCode;
		} catch (NameNotFoundException e) {
			throw new RuntimeException("Could not get package name: " + e);
		}
	}
}
