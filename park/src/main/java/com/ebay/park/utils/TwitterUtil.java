package com.ebay.park.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.ebay.park.ParkApplication;

/**
 * Twitter Util
 * 
 * @author Nicolás Matias Fernández
 * 
 */

public class TwitterUtil {

	// Constants
	public static String TWITTER_CONSUMER_KEY = ParkApplication.sParkConfiguration.getTwitterConsumerKey();
	public static String TWITTER_CONSUMER_SECRET = ParkApplication.sParkConfiguration.getTwitterConsumerSecret();

	public static final String TWITTER_CALLBACK_URL = "oauth://t4jsample";

	// Preference Constants
	private static final String PREF_KEY_OAUTH_TOKEN_TW = "oauth_token";
	private static final String PREF_KEY_OAUTH_SECRET_TW = "oauth_token_secret";
	private static final String PREF_KEY_USER_ID_TW = "user_id_twitter";
	private static final String PREF_KEY_TWITTER_LOGIN = "isTwitterLogedIn";
	private static final String PREF_KEY_TWITTER_USERNAME = "username_twitter";

	// Twitter oauth urls
	public static final String URL_TWITTER_OAUTH_VERIFIER = "oauth_verifier";

	/**
	 * Check user already logged in your application using twitter Login flag is
	 * fetched from Shared Preferences
	 * */
	public static boolean getIsTwitterLoggedInAlready(Context ctx) {
		SharedPreferences prefs = ctx.getSharedPreferences(PreferencesUtil.PREFS_NAME, Context.MODE_PRIVATE);
		return prefs.getBoolean(PREF_KEY_TWITTER_LOGIN, false);
	}

	// Store login status - true
	public static void saveIsTwitterLoggedInAlready(Context ctx) {
		SharedPreferences prefs = ctx.getSharedPreferences(PreferencesUtil.PREFS_NAME, Context.MODE_PRIVATE);
		prefs.edit().putBoolean(PREF_KEY_TWITTER_LOGIN, true).commit();
	}

	// Get Access token
	public static String getTwitterAccessToken(Context ctx) {
		SharedPreferences prefs = ctx.getSharedPreferences(PreferencesUtil.PREFS_NAME, Context.MODE_PRIVATE);
		return prefs.getString(PREF_KEY_OAUTH_TOKEN_TW, null);
	}

	// Store Access token
	public static void saveTwitterAccessToken(Context ctx, String accessToken) {
		SharedPreferences prefs = ctx.getSharedPreferences(PreferencesUtil.PREFS_NAME, Context.MODE_PRIVATE);
		prefs.edit().putString(PREF_KEY_OAUTH_TOKEN_TW, accessToken).commit();
	}

	// Get Secret Access token
	public static String getTwitterSecretAccessToken(Context ctx) {
		SharedPreferences prefs = ctx.getSharedPreferences(PreferencesUtil.PREFS_NAME, Context.MODE_PRIVATE);
		return prefs.getString(PREF_KEY_OAUTH_SECRET_TW, null);
	}

	// Store Secret Access token
	public static void saveTwitterSecretAccessToken(Context ctx, String secretAccessToken) {
		SharedPreferences prefs = ctx.getSharedPreferences(PreferencesUtil.PREFS_NAME, Context.MODE_PRIVATE);
		prefs.edit().putString(PREF_KEY_OAUTH_SECRET_TW, secretAccessToken).commit();
	}

	// Get Twitter User Id
	public static long getTwitterUserId(Context ctx) {
		SharedPreferences prefs = ctx.getSharedPreferences(PreferencesUtil.PREFS_NAME, Context.MODE_PRIVATE);
		return prefs.getLong(PREF_KEY_USER_ID_TW, 0);
	}

	// Store Twitter User Id
	public static void saveTwitterUserId(Context ctx, long twUserId) {
		SharedPreferences prefs = ctx.getSharedPreferences(PreferencesUtil.PREFS_NAME, Context.MODE_PRIVATE);
		prefs.edit().putLong(PREF_KEY_USER_ID_TW, twUserId).commit();
	}

	// Get Twitter Username
	public static String getTwitterUsername(Context ctx) {
		SharedPreferences prefs = ctx.getSharedPreferences(PreferencesUtil.PREFS_NAME, Context.MODE_PRIVATE);
		return prefs.getString(PREF_KEY_TWITTER_USERNAME, "");
	}

	// Store Twitter Username
	public static void saveTwitterUsername(Context ctx, String twUsername) {
		SharedPreferences prefs = ctx.getSharedPreferences(PreferencesUtil.PREFS_NAME, Context.MODE_PRIVATE);
		prefs.edit().putString(PREF_KEY_TWITTER_USERNAME, twUsername).commit();
	}

	/**
	 * Function to logout from twitter It will just clear the application shared
	 * preferences
	 * */
	public static void logoutFromTwitter(Context ctx) {
		SharedPreferences prefs = ctx.getSharedPreferences(PreferencesUtil.PREFS_NAME, Context.MODE_PRIVATE);
		Editor e = prefs.edit();
		e.remove(PREF_KEY_OAUTH_TOKEN_TW);
		e.remove(PREF_KEY_OAUTH_SECRET_TW);
		e.remove(PREF_KEY_USER_ID_TW);
		e.remove(PREF_KEY_TWITTER_LOGIN);
		e.remove(PREF_KEY_TWITTER_USERNAME);
		e.commit();
	}
}
