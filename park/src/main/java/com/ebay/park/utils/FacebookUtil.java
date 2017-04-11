package com.ebay.park.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;

import com.facebook.AccessToken;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.DefaultAudience;
import com.facebook.login.LoginBehavior;
import com.facebook.login.LoginManager;

import java.util.Arrays;
import java.util.List;

public class FacebookUtil {

	// Events
	public static final String EVENT_VISIT_CHAT = "fb_mobile_chat_view";
	public static final String EVENT_VISIT_LIST_CHAT = "fb_mobile_chats_list";
	public static final String EVENT_OFFER_ACCEPTED = "fb_mobile_offer_accepted";
	public static final String EVENT_NEW_OFFER = "fb_mobile_offer_new";
	public static final String EVENT_CREATE_ITEM_BEGIN = "fb_mobile_create_item_begin";
	public static final String EVENT_CREATE_ITEM_COMPLETE = "fb_mobile_create_item_complete";
	public static final String EVENT_EDIT_ZIP_CODE = "fb_mobile_zip_code_edit";
	public static final String EVENT_ITEM_MARKED_AS_SOLD = "fb_mobile_item_mark_as_sold";
	public static final String EVENT_ITEM_DELETED_BY_OWNER = "fb_mobile_item_delete";
	public static final String EVENT_FIND_FB_FRIENDS_OPENED = "fb_find_fb_friends_opened";
	public static final String EVENT_FOLLOW_USER_FIND_FB_FRIENDS = "fb_follow_user_fb_friends";
	public static final String EVENT_FOLLOW_USER_PROFILE_REST = "fb_follow_user_profile";
	public static final String EVENT_NOT_FRIENDS_FIND_FB_FRIENDS = "fb_not_friends_find_fb_friends_opened";
	public static final String EVENT_NOT_LINKED_FIND_FB_FRIENDS = "fb_not_linked_find_fb_friends_opened";
	public static final String EVENT_CATEGORY_SELECTED = "fb_mobile_buy_screen_%s";

	// Preference Constants
	private static final String PREF_KEY_FACEBOOK_LOGIN = "isFacebookLogedIn";
	private static final String PREF_KEY_FACEBOOK_PUBLISH = "isPublishPermissionGranted";
	private static final String PREF_KEY_FACEBOOK_FRIENDS= "isFriendsPermissionGranted";
	private static final String PREF_KEY_FACEBOOK_USERNAME = "username_facebook";

	/**
	 * Facebook permissons for the app to use.
	 */
	public static final List<String> READ_PERMISSONS = Arrays.asList("public_profile", "email", "user_friends");
	public static final List<String> PUBLISH_PERMISSONS = Arrays.asList("publish_actions");
	public static final List<String> FRIENDS_PERMISSONS = Arrays.asList("user_friends");

	/**
	 * Facebook request for permissions.
	 */
	public static final int REQUEST_PUBLISH_FB = 64206;

	/**
	 * Check user already logged in your application using facebook Login flag
	 * is fetched from Shared Preferences
	 * */
	public static boolean getIsFacebookLoggedInAlready(Context ctx) {
		SharedPreferences prefs = ctx.getSharedPreferences(PreferencesUtil.PREFS_NAME, Context.MODE_PRIVATE);
		return prefs.getBoolean(PREF_KEY_FACEBOOK_LOGIN, false);
	}

	// Store login status - true
	public static void saveIsPublishPermissionGranted(Context ctx, Boolean granted) {
		SharedPreferences prefs = ctx.getSharedPreferences(PreferencesUtil.PREFS_NAME, Context.MODE_PRIVATE);
		prefs.edit().putBoolean(PREF_KEY_FACEBOOK_PUBLISH, granted).commit();
	}

	// Store login status - true
	public static void saveIsFriendsPermissionGranted(Context ctx, Boolean granted) {
		SharedPreferences prefs = ctx.getSharedPreferences(PreferencesUtil.PREFS_NAME, Context.MODE_PRIVATE);
		prefs.edit().putBoolean(PREF_KEY_FACEBOOK_FRIENDS, granted).commit();
	}

	// Get Facebook Username
	public static String getFacebookUsername(Context ctx) {
		SharedPreferences prefs = ctx.getSharedPreferences(PreferencesUtil.PREFS_NAME, Context.MODE_PRIVATE);
		return prefs.getString(PREF_KEY_FACEBOOK_USERNAME, "");
	}

	// Store Facebook Username
	public static void saveFacebookUsername(Context ctx, String fbUsername) {
		SharedPreferences prefs = ctx.getSharedPreferences(PreferencesUtil.PREFS_NAME, Context.MODE_PRIVATE);
		prefs.edit().putString(PREF_KEY_FACEBOOK_USERNAME, fbUsername).commit();
	}

	/**
	 * Check user already has publish permissions in your application using facebook flag
	 * fetched from Shared Preferences
	 * */
	public static boolean getIsPublishPermissionGranted(Context ctx) {
		SharedPreferences prefs = ctx.getSharedPreferences(PreferencesUtil.PREFS_NAME, Context.MODE_PRIVATE);
		return prefs.getBoolean(PREF_KEY_FACEBOOK_PUBLISH, false);
	}

	/**
	 * Check user already has friends permissions in your application using facebook flag
	 * fetched from Shared Preferences
	 * */
	public static boolean getIsFriendsPermissionGranted(Context ctx) {
		SharedPreferences prefs = ctx.getSharedPreferences(PreferencesUtil.PREFS_NAME, Context.MODE_PRIVATE);
		return prefs.getBoolean(PREF_KEY_FACEBOOK_FRIENDS, false);
	}

	// Store publish permissions - true
	public static void saveIsFacebookLoggedInAlready(Context ctx) {
		SharedPreferences prefs = ctx.getSharedPreferences(PreferencesUtil.PREFS_NAME, Context.MODE_PRIVATE);
		prefs.edit().putBoolean(PREF_KEY_FACEBOOK_LOGIN, true).commit();
	}

	/**
	 * Function to logout from facebook It will just clear the application
	 * shared preferences
	 * */
	public static void logoutFromFacebook(Context ctx) {
		SharedPreferences prefs = ctx.getSharedPreferences(PreferencesUtil.PREFS_NAME, Context.MODE_PRIVATE);
		Editor e = prefs.edit();
		e.remove(PREF_KEY_FACEBOOK_LOGIN);
		e.remove(PREF_KEY_FACEBOOK_PUBLISH);
		e.remove(PREF_KEY_FACEBOOK_USERNAME);
		e.remove(PREF_KEY_FACEBOOK_FRIENDS);
		e.commit();
	}

	public static void requestPublishPermissions(Activity activity, Context context){
		AccessToken accessToken = AccessToken.getCurrentAccessToken();

		LoginManager logger1 = LoginManager.getInstance();
		logger1.setDefaultAudience(DefaultAudience.FRIENDS);
		logger1.setLoginBehavior(LoginBehavior.NATIVE_WITH_FALLBACK);
		logger1.logInWithPublishPermissions(activity, FacebookUtil.PUBLISH_PERMISSONS);

		AppEventsLogger logger2 = AppEventsLogger.newLogger(context);
		Bundle parameters1 = new Bundle();
		parameters1.putInt("logging_in", accessToken != null ? 0 : 1);
		logger2.logSdkEvent("fb_login_view_usage", (Double) null, parameters1);
	}

	public static void requestFriendsPermissions(Activity activity, Context context){
		AccessToken accessToken = AccessToken.getCurrentAccessToken();

		LoginManager logger1 = LoginManager.getInstance();
		logger1.setDefaultAudience(DefaultAudience.FRIENDS);
		logger1.setLoginBehavior(LoginBehavior.NATIVE_WITH_FALLBACK);
		logger1.logInWithReadPermissions(activity, FacebookUtil.FRIENDS_PERMISSONS);

		AppEventsLogger logger2 = AppEventsLogger.newLogger(context);
		Bundle parameters1 = new Bundle();
		parameters1.putInt("logging_in", accessToken != null ? 0 : 1);
		logger2.logSdkEvent("fb_login_view_usage", (Double) null, parameters1);
	}
}
