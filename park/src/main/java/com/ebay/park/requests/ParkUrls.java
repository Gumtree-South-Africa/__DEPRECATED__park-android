package com.ebay.park.requests;

import com.ebay.park.ParkApplication;

public class ParkUrls {

	public static final String GOOGLE_PLAY_URL = "https://play.google.com/store/apps/details?id=com.ebay.park";

	public static final String SERVER = ParkApplication.sParkConfiguration.getParkRestUrl();

	public static final String WEB = ParkApplication.sParkConfiguration.getParkWebUrl();

	public static final String GOOGLE_GEOCODEAPI_US = "https://maps.googleapis.com/maps/api/geocode/json?components=country:US%7C";

	public static final String GOOGLE_GEOCODEAPI_PR = "https://maps.googleapis.com/maps/api/geocode/json?components=country:PR%7C";

	public static final String GOOGLE_GEOCODEAPI_ZIPCODE_PARAM = "postal_code:%1$s";

	public static final String ASSET_UPLOAD = "assets/%s";

	public static final String ACTIVITY_FEED = "feeds/%1$s/users/%2$s";

	public static final String UNREAD_COUNT = "social/%s/unreadCount";

	public static final String UNREAD_FEEDS_ONLY_COUNTER = "feeds/%s/unreadFeedsCount";

	public static final String ACTIVITY_FEED_MARK_READ = "feeds/%1$s/%2$s/read";

	public static final String UNREAD_ITEMS_GROUPS = "/groups/%s/newItemsInfo";

	public static final String PUBLIC_ITEMS_CATEGORIES = "public/categories/%s";

	public static final String BANNER = "banners/%s";

	public static final String REGISTER_DEVICE = "public/device/%s/registration";

	// Start /conversations/v2.0/*

	public static final String CONVERSATIONS = "conversations/%s";

	public static final String GET_CONVERSATION = "conversations/%1$s/%2$d";

	public static final String SEND_CHAT = "conversations/%s/sendChat";

	public static final String SEND_OFFER = "conversations/%s/sendOffer";

	public static final String CONVERSATIONS_FOR_ITEM = "conversations/%1$s/item/%2$d";

	public static final String ACCEPT_NEGOTIATION = "conversations/%s/accept";

	public static final String CANCEL_NEGOTIATION = "conversations/%s/reject";

	// End /conversations/v2.0/*
	// Start /groups/v2.0/*

	public static final String CREATE_GROUP = "groups/%s";

	public static final String UPLOAD_UPDATE_GROUP_PICTURE = "groups/%1$s/%2$s/picture";

	public static final String GROUP_REMOVE_ITEMS = "groups/%1$s/%2$s/remove/items";

	public static final String GROUP_REMOVE_FOLLOWERS = "/groups/%1$s/%2$s/unsubscribe/followers";

	public static final String LIST_GROUPS = "groups/%1$s/user/%2$s";

	public static final String SEARCH_GROUP = "groups/%s";

	public static final String RECOMMENDED_GROUPS = "groups/%s/recommended";

	public static final String GET_GROUP = "groups/%1$s/%2$s";

	public static final String SUBSCRIBE_GROUP = "groups/%1$s/%2$s/subscribe";

	public static final String UNSUBSCRIBE_GROUP = "groups/%1$s/%2$s/unsubscribe";

	public static final String UPDATE_GROUP = "groups/%1$s/%2$s";

	public static final String DELETE_GROUP = "groups/%1$s/%2$s";

	public static final String SHARE_GROUP = "groups/%1$s/%2$s/share";

	// End /groups/v2.0/*

	public static final String PUBLISH_ITEM = "items/%s";

	public static final String PUBLIC_SEARCH_ITEMS = "public/items/%s/search";

	public static final String SEARCH_ITEMS = "items/%s/search";

	public static final String PUBLIC_SEARCH_ITEM_IDS = "public/items/%s/searchIds";

	public static final String SEARCH_ITEM_IDS = "items/%s/searchIds";

	public static final String GET_ITEM = "public/items/%1$s/%2$s";

	public static final String UPLOAD_UPDATE_ITEM_PICTURES = "items/%1$s/%2$s/pictures/";

	public static final String UPDATE_ITEM = "items/%1$s/%2$s";

	public static final String UPDATE_ITEM_BANNED = "items/%1$s/%2$s?feedWhenItemBanned=false";

	public static final String DELETE_ITEM_PICTURES = "items/%1$s/%2$s/pictures/%3$s";

	public static final String BUY_ITEM_DIRECTLY = "items/%1$s/%2$d/buy";

	public static final String SOLD_ITEM = "items/%1$s/%2$d/sold/";

	public static final String REPORT_ITEM = "items/%1$s/%2$d/report";

	public static final String FOLLOW_ITEM = "items/%1$s/%2$d/follow";

	public static final String UNFOLLOW_ITEM = "items/%1$s/%2$d/follow";

	public static final String REPUBLISH_ITEM = "items/%1$s/%2$d/republish/";

	public static final String RECOMMENDED_ITEMS = "items/%s/recommended";

	public static final String PUBLIC_RECOMMENDED_ITEMS = "public/items/%s/recommended";

	public static final String RECOMMENDED_ITEM_IDS = "items/%s/recommendedIds";

	public static final String PUBLIC_RECOMMENDED_ITEM_IDS = "public/items/%s/recommendedIds";

	public static final String LOCATION = "locations/%s";

	public static final String GET_UPDATE_NOTIFICATION_CONFIG = "notifications/%1$s/users/%2$s/config";

	public static final String UPDATE_PROFILE = "users/%1$s/%2$s/info";

	public static final String USER_PROFILE = "public/users/%1$s/%2$s/info";

	public static final String PROFILE_ADD_PICTURE = "users/%1$s/%2$s/picture";

	public static final String RATE = "ratings/%s/rate";

	public static final String PENDING_RATES = "ratings/%s/pending";

	public static final String PUBLIC_PENDING_RATES = "public/ratings/%s/pending";

	public static final String DELETE_PENDING_RATES = "ratings/%1$s/pending/%2$d";

	public static final String PUBLIC_GET_RATES = "public/ratings/%s";

	public static final String PUBLIC_FOLLOWERS = "public/users/%1$s/%2$s/followers";

	public static final String FOLLOWERS = "users/%1$s/%2$s/followers";

	public static final String PUBLIC_FOLLOWED = "public/users/%1$s/%2$s/follow";

	public static final String FOLLOWED = "users/%1$s/%2$s/follow";

	public static final String FOLLOW_USER = "users/%1$s/%2$s/follow";

	public static final String UNFOLLOW_USER = "users/%1$s/%2$s/unfollow";

	public static final String SOCIAL_NETWORKS_CONNECT = "social/%1$s/%2$s/connect";

	public static final String SOCIAL_NETWORKS_DISCONNECT = "social/%1$s/%2$s/disconnect";

	public static final String PUBLIC_DISCOVER_USERS = "public/social/%s/discover";

	public static final String DISCOVER_USERS = "social/%1$s/%2$s/discover";

	public static final String PUBLIC_SEARCH_USER = "public/users/%s/search";

	public static final String SEARCH_USER = "social/%1$s/%2$s/search";

	public static final String FACEBOOK_FRIENDS = "social/%1$s/%2$s/facebook/friends";

	// Start /users/v2.0/*

	public static final String LOGIN = "users/%s/signin";

    public static final String LOGIN_PHONE = LOGIN + "/phone";

    public static final String LOGIN_EMAIL= LOGIN + "/email";

    public static final String LOGIN_FACEBOOK = LOGIN + "/facebook";

	public static final String SIGNUP = "users/%s/signup";

    public static final String SIGNUP_PHONE = SIGNUP + "/phone";

    public static final String SIGNUP_EMAIL= SIGNUP + "/email";

    public static final String SIGNUP_FACEBOOK = SIGNUP + "/facebook";

	public static final String LOGOUT = "users/%s/signout";

	public static final String CHECK_USER_VALUE = "users/%s/check";

	public static final String FORGOT_PASS = "users/%s/forgotpwd";

	public static final String CHANGE_PASS = "users/%s/changepwd";

	public static final String SEND_VERIFICATION_MAIL = "users/%s/sendverification";

	// End /users/v2.0/*

	public static final String PUBLIC_SEARCH_GROUP = "public/groups/%s";

	public static final String PUBLIC_RECOMMENDED_GROUPS = "public/groups/%s/recommended";

	public static final String RESET_GROUP_NOTIFICATION_COUNTER = "groups/%s/resetCounter";

}
