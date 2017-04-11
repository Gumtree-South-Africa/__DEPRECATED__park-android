package com.ebay.park.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import java.sql.Timestamp;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Util class for read/write preferences.
 * 
 * @author federico.perez
 * 
 */
public class PreferencesUtil {

	public static final String PREFS_NAME = "ParkPrefs";
	private static final String FEED_READ = "feedRead";
	private static final String GCM_ID = "GCM_ID";
	private static final String PARK_TOKEN_KEY = "park_token";
	private static final String USER_KEY = "username";
	private static final String USER_PIC_KEY = "username_pic";
	private static final String LAST_REQUEST_CHAT = "last_request";
	private static final String VERIFIED_ITEM_MESSAGE = "verified_item_message";
	private static final String VERIFIED_GROUP_MESSAGE = "verified_group_message";
	private static final String GCM_ID_APP_VERSION = "GCM_ID_APP_VERSION";
	private static final String NOTIFICATION_KEY = "notification_visibility";
	private static final String LOCATION_NAME = "location_name";
	private static final String IS_SMS_USER = "is_sms_user";
	private static final String HAS_EMAIL = "has_email";
	private static final String ITEMS_WEAR = "items_wear";
	private static final String MAIN_SCREEN_ACTIVE = "park_activity_active";

	/**
	 * Saves token into preferences.
	 *
	 * @param ctx
	 *            Context to get preferences.
	 * @param token
	 *            The token to save.
	 */
	public static void saveParkToken(Context ctx, String token) {
		SharedPreferences prefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
		prefs.edit().putString(PARK_TOKEN_KEY, token).commit();
	}

	/**
	 * Retrieves park token from preferences.
	 *
	 * @param ctx
	 *            Context to get preferences.
	 * @return Park token.
	 */
	public static String getParkToken(Context ctx) {
		SharedPreferences prefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
		return prefs.getString(PARK_TOKEN_KEY, null);
	}

	/**
	 * Save the username to preferences.
	 *
	 * @param ctx
	 *            Context to get preferences.
	 * @param username
	 *            The username to save, the logged user.
	 */
	public static void saveCurrentUser(Context ctx, String username) {
		SharedPreferences prefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
		prefs.edit().putString(USER_KEY, username).commit();
	}

	/**
	 * Retrieves the username of the currently logged user.
	 *
	 * @param ctx
	 *            Context to get preferences.
	 * @return The username of the logged user, null if no one is loged in.
	 */
	public static String getCurrentUsername(Context ctx) {
		SharedPreferences prefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
		return prefs.getString(USER_KEY, null);
	}

	/**
	 * Clear SharedPreferences.
	 *
	 * @param ctx
	 *            Context to get preferences.
	 */
	public static void clearPreferences(Context ctx) {
		ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().clear().commit();
	}

	/**
	 * Get profile picture url.
	 *
	 * @param ctx
	 *            Context to get preferences.
	 * @return The url of the user profile picture.
	 */
	public static String getCurrentUserProfilePic(Context ctx) {
		SharedPreferences prefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
		return prefs.getString(USER_PIC_KEY, null);
	}

	/**
	 * Set user profile picture url.
	 *
	 * @param ctx
	 *            Context to get preferences.
	 */
	public static void setCurrentUserProfilePic(Context ctx, String url) {
		SharedPreferences prefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
		prefs.edit().putString(USER_PIC_KEY, url).commit();
	}

	/**
	 * Get last chat request date.
	 *
	 * @param ctx
	 *            Context to get preferences.
	 * @param role
	 *            Buyer or seller.
	 * @return The url of the user profile picture.
	 */
	public static long getLastChatRequest(Context ctx, String role) {
		SharedPreferences prefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
		return prefs.getLong(LAST_REQUEST_CHAT + "role", new Timestamp(new Date().getTime()).getTime());
	}

	/**
	 * Set last chat request date to now.
	 *
	 * @param ctx
	 *            Context to get preferences.
	 * @param role
	 *            Buyer or seller.
	 */
	public static void setLastChatRequest(Context ctx, String role) {
		SharedPreferences prefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
		prefs.edit().putLong(LAST_REQUEST_CHAT + role, new Timestamp(new Date().getTime()).getTime());
	}

	/**
	 * Get GCM registration string.
	 *
	 * @param ctx
	 *            Context to get preferences.
	 * @return GCM registration key.
	 */
	public static String getGCMRegistration(Context ctx) {
		SharedPreferences prefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
		return prefs.getString(GCM_ID, "");
	}

	/**
	 * Set GCM registration string.
	 *
	 * @param ctx
	 *            Context to get preferences.
	 * @param regid
	 *            GCM registration key.
	 */
	public static void setGCMRegistration(Context ctx, String regid) {
		SharedPreferences prefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
		prefs.edit().putString(GCM_ID, regid).commit();
	}

	/**
	 * Get app version for the GCM regsitration.
	 *
	 * @param ctx
	 *            Context to get preferences.
	 * @return App version.
	 */
	public static int getAppVersionForGCMRegistration(Context ctx) {
		SharedPreferences prefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
		return prefs.getInt(GCM_ID_APP_VERSION, -1);
	}

	/**
	 * Set app version for the GCM regsitration.
	 *
	 * @param ctx
	 *            Context to get preferences.
	 */
	public static void setAppVersionForGCMRegistration(Context ctx, int appVersion) {
		SharedPreferences prefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
		prefs.edit().putInt(GCM_ID_APP_VERSION, appVersion).commit();
	}

	public static void setParkActivityActive(Context ctx, Boolean active) {
		SharedPreferences prefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
		prefs.edit().putBoolean(MAIN_SCREEN_ACTIVE, active).apply();
	}

	public static boolean isParkActivityActive(Context ctx){
		SharedPreferences prefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
		return prefs.getBoolean(MAIN_SCREEN_ACTIVE, false);
	}

	public static boolean hasShownItemAccountVerificationMessage(Context ctx){
		return ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getBoolean(VERIFIED_ITEM_MESSAGE, false);
	}

	public static void setShownItemAccountVerificationMessage(Context ctx, boolean hasShown){
		SharedPreferences prefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
		prefs.edit().putBoolean(VERIFIED_ITEM_MESSAGE, hasShown).apply();
	}

	public static boolean hasShownGroupAccountVerificationMessage(Context ctx){
		return ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getBoolean(VERIFIED_GROUP_MESSAGE, false);
	}

	public static void setShownGroupAccountVerificationMessage(Context ctx, boolean hasShown){
		SharedPreferences prefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
		prefs.edit().putBoolean(VERIFIED_GROUP_MESSAGE, hasShown).apply();
	}

	public static void saveCurrentUserLocationName(Context ctx, String locationName) {
		SharedPreferences prefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
		prefs.edit().putString(LOCATION_NAME, locationName).commit();
	}

	public static String getCurrentUserLocationName(Context ctx) {
		SharedPreferences prefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
		return prefs.getString(LOCATION_NAME, null);
	}

	public static void saveIsSmsUser(Context ctx, boolean isSms) {
		SharedPreferences prefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
		prefs.edit().putBoolean(IS_SMS_USER, isSms).apply();
	}

	public static boolean getIsSmsUser(Context ctx) {
		SharedPreferences prefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
		return prefs.getBoolean(IS_SMS_USER, false);
	}

	public static void saveHasEmail(Context ctx, boolean hasEmail){
		SharedPreferences prefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
		prefs.edit().putBoolean(HAS_EMAIL, hasEmail).apply();
	}

	public static boolean getHasEmail(Context ctx) {
		SharedPreferences prefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
		return prefs.getBoolean(HAS_EMAIL, false);
	}

	public static void saveItemsWear(Context ctx, byte[] items){
		String itemsString = Base64.encodeToString(items, Base64.DEFAULT);
		SharedPreferences prefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
		prefs.edit().putString(ITEMS_WEAR, itemsString).apply();
	}

	public static byte[] getItemsWear(Context ctx) {
		SharedPreferences prefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
		String itemsString = prefs.getString(ITEMS_WEAR, null);
		if (itemsString != null){
			byte[] items = Base64.decode(itemsString, Base64.DEFAULT);
			return items;
		} else {
			return null;
		}
	}
}
