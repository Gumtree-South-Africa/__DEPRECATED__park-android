package com.ebay.park.flow;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;

import com.ebay.park.ParkApplication;
import com.ebay.park.R;
import com.ebay.park.activities.ProfileActivity;
import com.ebay.park.base.BaseActivity;
import com.ebay.park.fragments.ActivityFeedFragment;
import com.ebay.park.fragments.CameraFragment;
import com.ebay.park.fragments.CarouselCategoryFragment;
import com.ebay.park.fragments.ChangePassFragment;
import com.ebay.park.fragments.ChatFragment;
import com.ebay.park.fragments.ConversationListFragment;
import com.ebay.park.fragments.EditProfileFragment;
import com.ebay.park.fragments.FilterFragment;
import com.ebay.park.fragments.FindFbFriendsFragment;
import com.ebay.park.fragments.FollowFragment;
import com.ebay.park.fragments.GlobalSearchFragment;
import com.ebay.park.fragments.GroupCreateFragment;
import com.ebay.park.fragments.GroupDetailFragment;
import com.ebay.park.fragments.GroupFilterFragment;
import com.ebay.park.fragments.GroupFollowersEditFragment;
import com.ebay.park.fragments.GroupItemsEditFragment;
import com.ebay.park.fragments.ItemCreateEditFragment;
import com.ebay.park.fragments.ItemDetailFragment;
import com.ebay.park.fragments.ItemGroupsListFragment;
import com.ebay.park.fragments.ItemPublishedFragment;
import com.ebay.park.fragments.LoginFragment;
import com.ebay.park.fragments.MyGroupsFragment;
import com.ebay.park.fragments.MyListsTab;
import com.ebay.park.fragments.NewOfferFragment;
import com.ebay.park.fragments.NotificationConfigFragment;
import com.ebay.park.fragments.OffersTabsFragment;
import com.ebay.park.fragments.OptionsFragment;
import com.ebay.park.fragments.ProfileItemListFragment;
import com.ebay.park.fragments.RatesFragment;
import com.ebay.park.fragments.RegistrationAKFragment;
import com.ebay.park.fragments.RegistrationFBFragment;
import com.ebay.park.fragments.RegistrationFragment;
import com.ebay.park.fragments.SetPassFragment;
import com.ebay.park.fragments.SocialNetworksFragment;
import com.ebay.park.fragments.StandardLoginFragment;
import com.ebay.park.fragments.UserFilterFragment;
import com.ebay.park.fragments.UserGroupsFragment;
import com.ebay.park.model.ConversationModel;
import com.ebay.park.model.ItemsListParamsModel;
import com.ebay.park.utils.Constants;
import com.ebay.park.utils.PreferencesUtil;

import java.util.ArrayList;
import java.util.List;

import me.leolin.shortcutbadger.ShortcutBadger;

/**
 * Class that manages screen flows.
 *
 * @author federico.perez
 * @athutor Nicol�s Mat�as Fern�ndez
 *
 */
public class ScreenManager {

	/**
	 * Starts the login activity screen.
	 *
	 * @param origin
	 */
	public static void showLoginScreen(BaseActivity origin) {
		origin.startActivity(IntentFactory.getLoginIntent(origin));
	}

	/**
	 * Shows login fragment.
	 *
	 * @param origin
	 */
	public static void showLoginFragment(BaseActivity origin) {
		origin.getSupportFragmentManager().beginTransaction().add(R.id.container, new LoginFragment(), LoginFragment.TEST_TAG).commit();
	}

	/**
	 * Starts the main activity screen.
	 *
	 * @param origin
	 */
	public static void showMainScreen(BaseActivity origin) {
		origin.startActivity(IntentFactory.getMainScreenIntent(origin));
	}

	/**
	 * Shows the change password screen.
	 *
	 * @param origin
	 */
	public static void showChangePassword(BaseActivity origin) {
		origin.getSupportFragmentManager().beginTransaction().replace(R.id.container, new ChangePassFragment())
				.addToBackStack(null).commit();
	}

	/**
	 * Shows the set password screen.
	 *
	 * @param origin
	 */
	public static void showSetPassword(BaseActivity origin) {
		origin.getSupportFragmentManager().beginTransaction().replace(R.id.container, new SetPassFragment())
				.addToBackStack(null).commit();
	}

	/**
	 * Shows the notification config screen.
	 *
	 * @param origin
	 */
	public static void showConfigScreen(BaseActivity origin) {
		origin.getSupportFragmentManager().beginTransaction().replace(R.id.container, new NotificationConfigFragment())
				.addToBackStack(null).commit();
	}

	/**
	 * Show the item published screen.
	 *
	 * @param origin
	 */
	public static void showItemPublishedScreen(BaseActivity origin, long itemId){
		origin.startActivity(IntentFactory.getItemPublishedIntent(origin, itemId));
	}

	/**
	 * Shows social networks activity.
	 *
	 * @param origin
	 */
	public static void showSocialNetworksConfigActivity(BaseActivity origin) {
		origin.startActivity(IntentFactory.getSocialNetworksActivityIntent(origin));
	}

	/**
	 * Shows the social networks config screen.
	 *
	 * @param origin
	 */
	public static void showSocialNetworksConfigScreen(BaseActivity origin) {
		origin.getSupportFragmentManager().beginTransaction()
				.replace(R.id.container_social_networks, new SocialNetworksFragment()).commit();

	}

	/**
	 * Shows the user registration fragment.
	 *
	 * @param origin
	 */
	public static void showUserRegisterFragment(BaseActivity origin) {
		origin.getSupportFragmentManager().beginTransaction().replace(R.id.container, new RegistrationFragment(), RegistrationFragment.TAG)
				.addToBackStack(null).commit();
	}

	/**
	 * Shows the user registration fragment.
	 *
	 * @param origin
	 */
	public static void showUserRegisterFragmentWithoutStack(BaseActivity origin) {
		origin.getSupportFragmentManager().beginTransaction().replace(R.id.container, new RegistrationFragment(), RegistrationFragment.TAG).commit();
	}


	/**
	 * Shows the user account kit registration fragment.
	 *
	 * @param origin
	 */
	public static void showUserAccKitRegisterFragment(BaseActivity origin, boolean wSMS) {
		origin.getSupportFragmentManager().beginTransaction().replace(R.id.container, RegistrationAKFragment.initWith(wSMS), RegistrationAKFragment.TAG)
				.addToBackStack(null).commitAllowingStateLoss();
	}

	public static void showUserFBRegisterFragment(BaseActivity origin) {
		if(!origin.isFinishing()) {
			origin.getSupportFragmentManager().beginTransaction().replace(R.id.container, new RegistrationFBFragment(), RegistrationFBFragment.TAG)
					.addToBackStack(null).commitAllowingStateLoss();
		}
	}

	/**
	 * Shows the standard login fragment.
	 *
	 * @param origin
	 */
	public static void showStandardLoginFragment(BaseActivity origin) {
		origin.getSupportFragmentManager().beginTransaction().replace(R.id.container, new StandardLoginFragment(), StandardLoginFragment.TAG)
				.addToBackStack(null).commit();
	}

	/**
	 * Sends a Broadcast for logout and clears users preferences.
	 *
	 * @param origin
	 */
	public static void logout(BaseActivity origin) {
		// Reset logged values
		ParkApplication.sJustLogged = false;
		ParkApplication.sJustLoggedSecondLevel = false;
		ParkApplication.sJustLoggedThirdLevel = false;
		ParkApplication.sJustLoggedFromNavegar = false;
		ParkApplication.sFgmtOrAct_toGo = null;
		ParkApplication.sNavegarTab_toGo = -1;

		ParkApplication.getInstance().clearSession();
		PreferencesUtil.clearPreferences(origin);

		// Clear all notification
		NotificationManager nMgr = (NotificationManager) origin.getSystemService(Context.NOTIFICATION_SERVICE);
		nMgr.cancelAll();

		ShortcutBadger.removeCount(origin);

		Intent broadcastIntent = new Intent();
		broadcastIntent.setAction(Constants.LOGOUT_BROADCAST);
		origin.sendBroadcast(broadcastIntent);
		origin.startActivity(IntentFactory.getMainScreenIntent(origin));
	}

	/**
	 * Shows item activity.
	 *
	 * @param origin
	 */
	public static void showItemDetailActivity(BaseActivity origin, long itemId, ItemsListParamsModel itemsParams,
											  ArrayList<Long> itemIds) {
		origin.startActivity(IntentFactory.getItemDetailIntent(origin, itemId, itemsParams, itemIds));
	}

	/**
	 * Shows a specific item by its id.
	 *
	 * @param origin
	 * @param itemId
	 *            The id of the item.
	 */
	public static void showItemDetailFragment(BaseActivity origin, long itemId, ItemsListParamsModel itemsParams,
											  ArrayList<Long> itemIds) {
		origin.getSupportFragmentManager().beginTransaction().add(R.id.container, ItemDetailFragment.forItem(itemId,itemsParams,itemIds))
				.commit();
	}

	/**
	 * Shows group activity.
	 *
	 * @param origin
	 */
	public static void showGroupDetailActivity(BaseActivity origin, long groupId) {
		origin.startActivity(IntentFactory.getGroupDetailIntent(origin, groupId));
	}

	/**
	 * Shows a specific group by its id.
	 *
	 * @param origin
	 * @param groupId
	 *            The id of the group.
	 * @return
	 */
	public static void showGroupDetailFragment(BaseActivity origin, long groupId) {
		origin.getSupportFragmentManager().beginTransaction()
				.add(R.id.container, GroupDetailFragment.forGroup(groupId), GroupDetailFragment.TEST_TAG).commit();
	}

	/**
	 *
	 * @param origin
	 * @param conversation
	 * @param role
	 */
	public static void showNewOfferFragment(BaseActivity origin, ConversationModel conversation, String role){
		origin.getSupportFragmentManager().beginTransaction()
				.replace(R.id.container, NewOfferFragment.forConversation(conversation, role))
				.addToBackStack(null).commit();
	}

	/**
	 * Starts profile activity.
	 *
	 * @param origin
	 */
	public static void showProfileActivity(BaseActivity origin) {
		origin.startActivity(IntentFactory.getProfileIntent(origin));
	}

	/**
	 * Starts profile activity with a username extra.
	 *
	 * @param origin
	 * @param username
	 *            Username to be added as extra.
	 */
	public static void showProfileActivity(BaseActivity origin, String username) {
		origin.startActivity(IntentFactory.getProfileIntent(origin, username));
	}

	/**
	 * Shows a user profile fragment.
	 *
	 * @param origin
	 * @param username
	 *            Username of the user to show.
	 */
	public static void showUserProfileFragment(BaseActivity origin, String username, boolean addToBackStack) {
		ProfileItemListFragment fragment = ProfileItemListFragment.forUser(username);
		if (addToBackStack) {
			origin.getSupportFragmentManager().beginTransaction().replace(R.id.container, fragment)
					.addToBackStack(null).commit();
		} else {
			origin.getSupportFragmentManager().beginTransaction().replace(R.id.container, fragment).commit();
		}
	}

	/**
	 * Shows profile fragment.
	 *
	 * @param origin
	 */
	public static void showCurrentUserProfileFragment(BaseActivity origin) {
		origin.getSupportFragmentManager().beginTransaction()
				.replace(R.id.container, ProfileItemListFragment.forCurrentUser(false, true)).commit();
	}

	/**
	 * Shows publish activity.
	 *
	 * @param origin
	 * @param itemId
	 *            The id of the item.
	 */
	public static void showPublishActivity(BaseActivity origin, long itemId) {
		origin.startActivity(IntentFactory.getPublishActivityIntent(origin, itemId));
	}

	/**
	 * Shows publish activity with images
	 *
	 * @param origin
	 * @param itemId
	 * @param imagesPaths
	 *            The id of the item.
	 */
	public static void showPublishActivity(BaseActivity origin, long itemId, List<String> imagesPaths) {
		origin.startActivity(IntentFactory.getPublishActivityIntent(origin, itemId, imagesPaths));
	}

	/**
	 * Shows publish fragment.
	 *
	 * @param origin
	 * @param itemId
	 *            The id of the item.
	 */
	public static void showPublishScreen(BaseActivity origin, long itemId) {
		origin.getSupportFragmentManager().beginTransaction()
				.add(R.id.container_publish, ItemCreateEditFragment.forItem(itemId)).commit();
	}

	/**
	 * Shows publish fragment.
	 *
	 * @param origin
	 * @param itemId
	 * @param imagesPaths
	 *            The id of the item.
	 */
	public static void showPublishScreen(BaseActivity origin, long itemId, String[] imagesPaths) {
		origin.getSupportFragmentManager().beginTransaction()
				.add(R.id.container_publish, ItemCreateEditFragment.forItem(itemId, imagesPaths)).commit();
	}

	/**
	 * Shows user groups activity.
	 *
	 * @param origin
	 */
	public static void showUserGroupsActivity(BaseActivity origin) {
		origin.startActivity(IntentFactory.getUserGroupsActivityIntent(origin));
	}

	/**
	 * Shows user groups fragment.
	 *
	 * @param origin
	 */
	public static void showUserGroupsScreen(BaseActivity origin) {
		origin.getSupportFragmentManager().beginTransaction().add(R.id.container_user_groups, new UserGroupsFragment())
				.commit();
	}

	/**
	 * Starts create group fragment.
	 *
	 * @param origin
	 */
	public static void showCreateGroupActivity(BaseActivity origin, long groupId) {
		origin.startActivityForResult(IntentFactory.getGroupCreateIntent(origin, groupId),
				GroupDetailFragment.REQUEST_EDIT_GROUP);
	}

	public static void showGroupItemsEditActivity(BaseActivity origin, long groupId) {
		origin.startActivity(IntentFactory.getGroupItemsEditIntent(origin, groupId));
	}

	public static void showGroupFollowersEditActivity(BaseActivity origin, long groupId) {
		origin.startActivity(IntentFactory.getGroupFollowersEditIntent(origin, groupId));
	}

	/**
	 * Starts create group fragment.
	 *
	 * @param origin
	 */
	public static void showCreateGroupFragment(BaseActivity origin, long groupId) {
		origin.getSupportFragmentManager().beginTransaction()
				.replace(R.id.container, GroupCreateFragment.forGroup(groupId)).commit();
	}

	/**
	 * Show Followers list for a given user.
	 *
	 * @param origin
	 * @param username
	 *            The username to show followers list.
	 */
	public static void showFollowersFragment(BaseActivity origin, String username) {
		showFollowersFragment(true, origin, username);
	}

	public static void showFollowersFragment(boolean addToBackStack, BaseActivity origin, String username) {

		if (addToBackStack) {
			origin.getSupportFragmentManager().beginTransaction()
					.replace(R.id.container, FollowFragment.getFollowersInstance(username)).addToBackStack(null)
					.commit();
		} else {
			origin.getSupportFragmentManager().beginTransaction()
					.replace(R.id.container, FollowFragment.getFollowersInstance(username)).commit();
		}
	}

	public static void showFollowersFragment(BaseActivity origin, String username, boolean comesFromDrawer) {
		if (comesFromDrawer) {
			origin.startActivity(IntentFactory.getProfileIntent(origin, username, ProfileActivity.FOLLOWERS));
		} else {
			showFollowersFragment(origin, username);
		}
	}

	/**
	 * Show Following list for a given user.
	 *
	 * @param origin
	 * @param username
	 *            The username to show following list.
	 */
	public static void showFollowingFragement(BaseActivity origin, String username) {
		showFollowingFragement(true, origin, username);
	}

	public static void showFollowingFragement(boolean addToBackStack, BaseActivity origin, String username) {

		if (addToBackStack) {
			origin.getSupportFragmentManager().beginTransaction()
					.replace(R.id.container, FollowFragment.getFollowingInstance(username)).addToBackStack(null)
					.commit();
		} else {
			origin.getSupportFragmentManager().beginTransaction()
					.replace(R.id.container, FollowFragment.getFollowingInstance(username)).commit();
		}

	}

	public static void showFollowingFragement(BaseActivity origin, String username, boolean comesFromDrawer) {
		if (comesFromDrawer) {
			origin.startActivity(IntentFactory.getProfileIntent(origin, username, ProfileActivity.FOLLOWING));
		} else {
			showFollowingFragement(origin, username);
		}
	}

	/**
	 * Shows profile edit fragment.
	 *
	 * @param origin
	 */
	public static void showProfileEditFragment(BaseActivity origin) {
		showProfileEditFragment(true, origin);
	}

	public static void showProfileEditFragment(boolean addToBackStack, BaseActivity origin) {
		if (addToBackStack) {
			origin.getSupportFragmentManager().beginTransaction().replace(R.id.container, new EditProfileFragment())
					.addToBackStack(null).commit();
		} else {
			origin.getSupportFragmentManager().beginTransaction().replace(R.id.container, new EditProfileFragment())
					.commit();
		}
	}

	public static void showProfileEditFragment(BaseActivity origin, boolean comesFromDrawer) {
		if (comesFromDrawer) {
			origin.startActivity(IntentFactory.getProfileIntent(origin, ProfileActivity.PROFILE_EDIT));
		} else {
			showProfileEditFragment(origin);
		}
	}

	public static void showConversationListAsSellerFragment(boolean addToStack, BaseActivity origin) {
		if (addToStack) {
			origin.getSupportFragmentManager().beginTransaction()
					.replace(R.id.container, ConversationListFragment.conversationsAsSeller()).addToBackStack(null)
					.commit();
		} else {
			origin.getSupportFragmentManager().beginTransaction()
					.replace(R.id.container, ConversationListFragment.conversationsAsSeller()).commit();
		}

	}

	public static void showConversationListAsBuyerFragment(boolean addToBackStack, BaseActivity origin) {
		if (addToBackStack) {
			origin.getSupportFragmentManager().beginTransaction()
					.replace(R.id.container, ConversationListFragment.conversationsAsBuyer()).addToBackStack(null)
					.commit();
		} else {
			origin.getSupportFragmentManager().beginTransaction()
					.replace(R.id.container, ConversationListFragment.conversationsAsBuyer()).commit();
		}
	}

	/**
	 * Show chat screen.
	 *
	 * @param context
	 * @param conversation
	 * @param role
	 *
	 */
	public static void showChatFragment(BaseActivity context, ConversationModel conversation, String role) {
		context.getSupportFragmentManager().beginTransaction()
				.replace(R.id.container, ChatFragment.forConversation(conversation, role)).commit();
	}

	public static void showChatFragment(BaseActivity context, long conversationId, long itemId, String role) {
		context.getSupportFragmentManager().beginTransaction()
				.replace(R.id.container, ChatFragment.forConversationId(conversationId, itemId, role)).commit();
	}

	/**
	 * Show new chat screen.
	 *
	 * @param context
	 * @param itemId
	 *            ItemId of the chat.
	 */
	public static void showNewChatFragment(BaseActivity context, long itemId) {
		context.getSupportFragmentManager().beginTransaction()
				.replace(R.id.container, ChatFragment.forNewConversation(itemId)).addToBackStack(null).commit();
	}

	/**
	 * Shows conversation list for a certain item.
	 *
	 * @param context
	 * @param itemId
	 */
	public static void showConversationListForItem(BaseActivity context, long itemId) {
		context.getSupportFragmentManager().beginTransaction()
				.replace(R.id.container, ConversationListFragment.forItemIdSelling(itemId)).addToBackStack(null)
				.commit();
	}

	public static void showFilterFragment(BaseActivity origin) {
		origin.getSupportFragmentManager().beginTransaction()
				.replace(R.id.container, new FilterFragment(), FilterFragment.TAG).commit();
	}

	public static void showUserFilterFragment(BaseActivity origin) {
		origin.getSupportFragmentManager().beginTransaction()
				.replace(R.id.container, new UserFilterFragment(), UserFilterFragment.TAG).commit();
	}

	public static void showGroupFilterFragment(BaseActivity origin) {
		origin.getSupportFragmentManager().beginTransaction()
				.replace(R.id.container, new GroupFilterFragment(), GroupFilterFragment.TAG).commit();
	}

	public static void showRateListActivity(BaseActivity origin, String username) {
		showRateListActivity(origin, username, false);
	}

	public static void showRateListActivity(BaseActivity origin, String username, boolean comesFromPendingFeed) {
		origin.startActivity(IntentFactory.getRateListIntent(origin, username, comesFromPendingFeed));
	}

	public static void showChatActivity(BaseActivity origin, ConversationModel conversation, long conversationId,
										long itemId, String role) {
		origin.startActivity(IntentFactory.getChatActivityIntent(origin, conversation, conversationId, itemId, role));
	}

	public static void showGlobalSearchFragment(BaseActivity origin) {
		origin.getSupportFragmentManager().beginTransaction()
				.replace(R.id.container, new GlobalSearchFragment(), GlobalSearchFragment.TAG).commit();
	}

	public static void showGlobalSearch(BaseActivity origin) {
		origin.startActivity(IntentFactory.getGlobalSearchActivityIntent(origin));

	}

	public static void showItemGroupsFragment(BaseActivity origin, Long id) {
		origin.getSupportFragmentManager().beginTransaction()
				.replace(R.id.container, ItemGroupsListFragment.forItem(id)).addToBackStack(null).commit();
	}

	public static void showGroupItemsEditFragment(BaseActivity origin, Long id) {
		origin.getSupportFragmentManager().beginTransaction()
				.replace(R.id.container, GroupItemsEditFragment.forGroup(id)).commit();
	}

	public static void showGroupFollowersEditFragment(BaseActivity origin, Long id) {
		origin.getSupportFragmentManager().beginTransaction()
				.replace(R.id.container, GroupFollowersEditFragment.forGroup(id)).commit();
	}

	public static void showRatesTabFragment(BaseActivity origin, String username, boolean comesFromPendingRates) {
		origin.getSupportFragmentManager().beginTransaction()
				.replace(R.id.container, RatesFragment.forUser(username, comesFromPendingRates)).commit();
	}

	// **********************************************************************************************************
	// ***** FRAGMENTS called from Navigation DRAWER:

	/**
	 * Shows current user fragment.
	 */
	public static void showMyProfileFragmentFromDrawer(BaseActivity origin, boolean fromActFeed) {
		origin.getSupportFragmentManager().beginTransaction()
				.replace(R.id.park_container, ProfileItemListFragment.forCurrentUser(true, fromActFeed)).commit();
	}

	/**
	 * Shows grid view of categories fragment.
	 */
	public static void showCategoryItemsFragmentFromDrawer(BaseActivity origin) {
		origin.getSupportFragmentManager().beginTransaction().replace(R.id.park_container, new CarouselCategoryFragment())
				.commit();
	}

	/**
	 * Show custom camera activity.
	 *
	 * @param origin
	 */
	public static void showCameraActivity(BaseActivity origin){
		origin.startActivity(IntentFactory.getCameraIntent(origin));
	}

	/**
	 * Show custom camera activity for item edition.
	 *
	 * @param origin
	 * @param imagePaths
	 */
	public static void showCameraActivityEdition(BaseActivity origin, List<String> imagePaths) {
		origin.startActivityForResult(IntentFactory.getCameraIntentEdition(origin, imagePaths),
				ItemCreateEditFragment.REQUEST_CAMERA);
	}

	/**
	 * Show custom camera activity for item creation.
	 *
	 * @param origin
	 * @param imagePaths
	 */
	public static void showCameraActivityCreation(BaseActivity origin, List<String> imagePaths) {
		origin.startActivityForResult(IntentFactory.getCameraIntentCreation(origin, imagePaths),
				ItemCreateEditFragment.REQUEST_CAMERA);
	}

	/**
	 * Shows custom camera fragment.
	 *
	 * @param origin
	 */
	public static void showCameraFragment(BaseActivity origin){
		origin.getSupportFragmentManager().beginTransaction().add(R.id.camera_container, new CameraFragment())
				.commit();
	}

	/**
	 * Shows custom camera fragment for item edition.
	 *
	 * @param origin
	 * @param someImagePaths
	 */
	public static void showCameraFragmentEdition(BaseActivity origin, String[] someImagePaths) {
		origin.getSupportFragmentManager().beginTransaction().add(R.id.camera_container, CameraFragment.forEdition(someImagePaths))
				.commit();
	}

	/**
	 * Shows custom camera fragment for item creation.
	 *
	 * @param origin
	 * @param someImagePaths
	 */
	public static void showCameraFragmentCreation(BaseActivity origin, String[] someImagePaths) {
		origin.getSupportFragmentManager().beginTransaction().add(R.id.camera_container, CameraFragment.forCreation(someImagePaths))
				.commit();
	}

	/**
	 * Shows activity feeds fragment.
	 */
	public static void showActivityFeedsFragmentFromDrawer(BaseActivity origin) {
		origin.setTheme(R.style.AppTheme);
		origin.getSupportFragmentManager().beginTransaction().replace(R.id.park_container, new ActivityFeedFragment())
				.commit();
	}

	/**
	 * Shows my lists fragment.
	 */
	public static void showMyListsFragmentFromDrawer(BaseActivity origin) {
		origin.getSupportFragmentManager().beginTransaction().replace(R.id.park_container, new MyListsTab(), MyListsTab.TAG)
				.commit();
	}

	/**
	 * Shows offers fragment.
	 */
	public static void showOffersFragmentFromDrawer(BaseActivity origin) {
		origin.getSupportFragmentManager().beginTransaction().replace(R.id.park_container, new OffersTabsFragment())
				.commit();
	}

	/**
	 * Show the item published fragment.
	 *
	 * @param origin
	 */
	public static void showItemPublishedFragment(BaseActivity origin, long itemId){
		origin.getSupportFragmentManager().beginTransaction().add(R.id.container_published, ItemPublishedFragment.forItem(itemId))
				.commit();
	}

	/**
	 * Shows user groups fragment.
	 */
	public static void showUserGroupsFragmentFromDrawer(BaseActivity origin) {
		origin.getSupportFragmentManager().beginTransaction()
				.replace(R.id.park_container, new MyGroupsFragment(), MyGroupsFragment.TAG).commit();
	}

	public static void showOptionsScreen(BaseActivity origin){
		origin.startActivity(IntentFactory.getOptionsIntent(origin));
	}

	public static void showOptionsFragment(BaseActivity origin) {
		origin.getSupportFragmentManager().beginTransaction().replace(R.id.container, new OptionsFragment())
				.commit();
	}

	/**
	 * Shows find fb friends activity.
	 *
	 * @param origin
	 */
	public static void showFindFbFriendsActivity(BaseActivity origin) {
		origin.startActivity(IntentFactory.getFindFbFriendsActivityIntent(origin));
	}

	/**
	 * Shows find fb friends fragment.
	 *
	 * @param origin
	 */
	public static void showFindFbFriendsScreen(BaseActivity origin) {
		origin.getSupportFragmentManager().beginTransaction().add(R.id.container, new FindFbFriendsFragment())
				.commit();
	}

}