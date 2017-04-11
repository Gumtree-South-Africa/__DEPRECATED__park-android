package com.ebay.park.flow;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.ebay.park.R;
import com.ebay.park.activities.CameraActivity;
import com.ebay.park.activities.ChatActivity;
import com.ebay.park.activities.FilterActivity;
import com.ebay.park.activities.FindFbFriendsActivity;
import com.ebay.park.activities.GlobalSearchActivity;
import com.ebay.park.activities.GroupCreateActivity;
import com.ebay.park.activities.GroupDetailActivity;
import com.ebay.park.activities.GroupFollowersEditActivity;
import com.ebay.park.activities.GroupItemsEditActivity;
import com.ebay.park.activities.ItemCreateEditActivity;
import com.ebay.park.activities.ItemDetailActivity;
import com.ebay.park.activities.ItemPublishedActivity;
import com.ebay.park.activities.LoginActivity;
import com.ebay.park.activities.MapActivity;
import com.ebay.park.activities.OptionsActivity;
import com.ebay.park.activities.ParkActivity;
import com.ebay.park.activities.ProfileActivity;
import com.ebay.park.activities.RateListActivity;
import com.ebay.park.activities.SocialNetworksActivity;
import com.ebay.park.activities.UserGroupsActivity;
import com.ebay.park.base.BaseActivity;
import com.ebay.park.model.ConversationModel;
import com.ebay.park.model.ItemsListParamsModel;
import com.ebay.park.requests.ParkUrls;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author federico.perez
 * @author Nicol�s Mat�as Fern�ndez
 */
public class IntentFactory {

	public static Intent getLoginIntent(Context context) {
		Intent intent = new Intent(context, LoginActivity.class);
		return intent;
	}

	public static Intent getMainScreenIntent(Context context) {
		Intent intent = new Intent(context, ParkActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		return intent;
	}

	public static Intent getFilterIntent(Context context, int filterId) {
		Intent intent = new Intent(context, FilterActivity.class);
		intent.putExtra(FilterActivity.EXTRA_FILTER_ID, filterId);
		return intent;
	}

	public static Intent getItemDetailIntent(Context context, long itemId, ItemsListParamsModel itemsParams,
											 ArrayList<Long> itemIds) {
		Intent intent = new Intent(context, ItemDetailActivity.class);
		intent.putExtra(ItemDetailActivity.EXTRA_ITEM_ID, itemId);
		Bundle bundle = new Bundle();
		bundle.putSerializable(ItemDetailActivity.EXTRA_ITEM_LIST_PARAMS, itemsParams);
		bundle.putSerializable(ItemDetailActivity.EXTRA_ITEM_IDS_LIST, itemIds);
		intent.putExtras(bundle);
		return intent;
	}

	public static Intent getGroupDetailIntent(Context context, long groupId) {
		Intent intent = new Intent(context, GroupDetailActivity.class);
		intent.putExtra(GroupDetailActivity.EXTRA_GROUP_ID, groupId);
		return intent;
	}

	public static Intent getProfileIntent(Context origin) {
		return new Intent(origin, ProfileActivity.class);
	}

	public static Intent getOptionsIntent(Context origin) {
		return new Intent(origin, OptionsActivity.class);
	}

	public static Intent getProfileIntent(Context origin, String username) {
		Intent intent = new Intent(origin, ProfileActivity.class);
		intent.putExtra(ProfileActivity.USERNAME_EXTRA, username);
		return intent;
	}

	public static Intent getProfileIntent(BaseActivity origin, int contentType) {
		Intent intent = new Intent(origin, ProfileActivity.class);
		intent.putExtra(ProfileActivity.CONTENT_EXTRA, contentType);
		return intent;
	}

	public static Intent getProfileIntent(BaseActivity origin, String username, int contentType) {
		Intent intent = new Intent(origin, ProfileActivity.class);
		intent.putExtra(ProfileActivity.USERNAME_EXTRA, username);
		intent.putExtra(ProfileActivity.CONTENT_EXTRA, contentType);
		return intent;
	}

	public static Intent getPublishActivityIntent(Context context, long itemId) {
		Intent intent = new Intent(context, ItemCreateEditActivity.class);
		intent.putExtra(ItemDetailActivity.EXTRA_ITEM_ID, itemId);
		return intent;
	}

	public static Intent getPublishActivityIntent(Context context, long itemId, List<String> imagePaths) {
		Intent intent = new Intent(context, ItemCreateEditActivity.class);
		intent.putExtra(ItemCreateEditActivity.EXTRA_ITEM_ID, itemId);
		intent.putExtra(ItemCreateEditActivity.EXTRA_IMAGES_PATHS, imagePaths.toArray(new String[imagePaths.size()]));
		return intent;
	}

	public static Intent getCameraIntent(Context aConext){
		return new Intent(aConext, CameraActivity.class);
	}

	public static Intent getCameraIntentEdition(Context aContext, List<String> someImagePaths) {
		Intent aIntent = getCameraIntent(aContext);
		aIntent.putExtra(CameraActivity.ARG, someImagePaths.toArray(new String[someImagePaths.size()]));
		return aIntent;
	}

	public static Intent getItemPublishedIntent(Context origin, long itemId) {
        Intent intent = new Intent(origin, ItemPublishedActivity.class);
        intent.putExtra(ItemPublishedActivity.EXTRA_ITEM_ID, itemId);
        return intent;
	}

	public static Intent getCameraIntentCreation(Context aContext, List<String> someImagePaths) {
		Intent aIntent = getCameraIntent(aContext);
		aIntent.putExtra(CameraActivity.ARG_NOT_PUBLISHED, someImagePaths.toArray(new String[someImagePaths.size()]));
		return aIntent;
	}

	public static Intent getUserGroupsActivityIntent(Context context) {
		return new Intent(context, UserGroupsActivity.class);
	}

	public static Intent getRateListIntent(BaseActivity origin, String username, boolean comesFromPendingFeed) {
		Intent intent = new Intent(origin, RateListActivity.class);
		intent.putExtra(RateListActivity.USERNAME, username);
		intent.putExtra(RateListActivity.PENDING_FEED, comesFromPendingFeed);
		return intent;
	}

	public static Intent getFeedbackEmailIntent(BaseActivity origin) {
		Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", "support@vivanuncios.us", null));
		emailIntent.putExtra(Intent.EXTRA_SUBJECT, origin.getString(R.string.feedback_mail_subject));
		emailIntent.putExtra(Intent.EXTRA_TEXT, origin.getString(R.string.feedback_mail_body));
		return Intent.createChooser(emailIntent, origin.getString(R.string.feedback_support));
	}

	public static Intent getSocialNetworksActivityIntent(Context context) {
		Intent i = new Intent(context, SocialNetworksActivity.class);
		i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		return i;
	}

	public static Intent getTermsConditions() {
		return new Intent(Intent.ACTION_VIEW, Uri.parse(ParkUrls.WEB + "statics/legalDisclosures#terms"));
	}

	public static Intent getLegalDisclosures() {
		return new Intent(Intent.ACTION_VIEW, Uri.parse(ParkUrls.WEB + "statics/legalDisclosures"));
	}

	public static Intent getAboutIntent() {
		return new Intent(Intent.ACTION_VIEW, Uri.parse(ParkUrls.WEB + "statics/about"));
	}

	public static Intent getShareItemIntent(BaseActivity activity, String content) {
		Intent sendIntent = new Intent();
		sendIntent.setAction(Intent.ACTION_SEND);
		sendIntent.putExtra(Intent.EXTRA_TEXT, activity.getString(R.string.item_share_body) + " " + content);
		sendIntent.putExtra(Intent.EXTRA_SUBJECT, activity.getString(R.string.item_share_subject));
		sendIntent.setType("text/plain");

		Intent chooser = Intent.createChooser(sendIntent, activity.getString(R.string.share_selector));

		return chooser;
	}

	public static Intent getShareGroupIntent(BaseActivity activity, String content) {
		Intent sendIntent = new Intent();
		sendIntent.setAction(Intent.ACTION_SEND);
		sendIntent.putExtra(Intent.EXTRA_TEXT, activity.getString(R.string.group_share_body) + " " + content);
		sendIntent.putExtra(Intent.EXTRA_SUBJECT, activity.getString(R.string.group_share_subject));
		sendIntent.setType("text/plain");

		Intent chooser = Intent.createChooser(sendIntent, activity.getString(R.string.group_share_selector));

		return chooser;
	}

	public static Intent getShareProfileIntent(BaseActivity activity, String content) {
		Intent sendIntent = new Intent();
		sendIntent.setAction(Intent.ACTION_SEND);
		sendIntent.putExtra(Intent.EXTRA_TEXT, activity.getString(R.string.profile_share_body) + " " + content);
		sendIntent.putExtra(Intent.EXTRA_SUBJECT, activity.getString(R.string.profile_share_subject));
		sendIntent.setType("text/plain");

		Intent chooser = Intent.createChooser(sendIntent, activity.getString(R.string.profile_share_selector));

		return chooser;
	}

	public static Intent getShareWebAppIntent(BaseActivity activity, String content) {
		Intent sendIntent = new Intent();
		sendIntent.setAction(Intent.ACTION_SEND);
		sendIntent.putExtra(Intent.EXTRA_TEXT, activity.getString(R.string.invite_mail_body) + " " + content);
		sendIntent.putExtra(Intent.EXTRA_SUBJECT, activity.getString(R.string.invite_mail_subject));
		sendIntent.setType("text/plain");

		Intent chooser = Intent.createChooser(sendIntent, activity.getString(R.string.publish_invite_to_friends));

		return chooser;
	}

	public static Intent getChatActivityIntent(BaseActivity origin, ConversationModel conversation,
			long conversationId, long itemId, String role) {
		Intent intent = new Intent(origin, ChatActivity.class);
		intent.putExtra(ChatActivity.CONVERSATION, conversation);
		if (conversationId != -1) {
			intent.putExtra(ChatActivity.CONVERSATION_ID, conversationId);
		}
		if (itemId != -1) {
			intent.putExtra(ChatActivity.ITEM_ID, itemId);
		}
		intent.putExtra(ChatActivity.ROLE, role);
		return intent;
	}

	public static Intent getGroupCreateIntent(Context context, long groupId) {
		Intent intent = new Intent(context, GroupCreateActivity.class);
		intent.putExtra(GroupCreateActivity.EXTRA_GROUP_ID, groupId);
		return intent;
	}

	public static Intent getGroupItemsEditIntent(Context context, long groupId) {
		Intent intent = new Intent(context, GroupItemsEditActivity.class);
		intent.putExtra(GroupItemsEditActivity.EXTRA_GROUP_ID, groupId);
		return intent;
	}

	public static Intent getGroupFollowersEditIntent(Context context, long groupId) {
		Intent intent = new Intent(context, GroupFollowersEditActivity.class);
		intent.putExtra(GroupFollowersEditActivity.EXTRA_GROUP_ID, groupId);
		return intent;
	}

	public static Intent getGlobalSearchActivityIntent(BaseActivity origin) {
		return new Intent(origin, GlobalSearchActivity.class);
	}

	public static Intent getFindFbFriendsActivityIntent(Context context) {
		return new Intent(context, FindFbFriendsActivity.class);
	}

	/**
	 * Show Map on given coordinates.
	 *
	 * @param origin
	 * @param latitude
	 *            The latitude to place the marker.
	 * @param longitude
	 *            The longitude to place the marker.
	 */
	public static Intent getMapIntent(BaseActivity origin, double latitude, double longitude, String locationName){
		Intent intent = new Intent(origin, MapActivity.class);
		intent.putExtra(MapActivity.MARKER_LATITUDE,latitude);
		intent.putExtra(MapActivity.MARKER_LONGITUDE,longitude);
		intent.putExtra(MapActivity.MARKER_LOCATION_NAME,locationName);
		return intent;
	}

}
