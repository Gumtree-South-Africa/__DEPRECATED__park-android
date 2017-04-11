package com.ebay.park.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.ebay.park.ParkApplication;
import com.ebay.park.R;
import com.ebay.park.base.BaseActivity;
import com.ebay.park.base.BaseListFragment;
import com.ebay.park.base.BaseUserListFragment;
import com.ebay.park.flow.ScreenManager;
import com.ebay.park.requests.BaseParkSessionRequest;
import com.ebay.park.requests.FacebookFriendsRequest;
import com.ebay.park.requests.SocialDisconnectRequest;
import com.ebay.park.responses.UserListResponse;
import com.ebay.park.utils.FacebookUtil;
import com.ebay.park.utils.MessageUtil;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.share.model.AppInviteContent;
import com.facebook.share.widget.AppInviteDialog;
import com.globant.roboneck.requests.BaseNeckRequestException;
import com.globant.roboneck.requests.BaseNeckRequestListener;
import com.google.android.gms.analytics.HitBuilders;
import com.octo.android.robospice.persistence.exception.SpiceException;

/**
 * Find Fb Friends screen.
 *
 * @author Nicol�s Mat�as Fern�ndez
 *
 */
public class FindFbFriendsFragment extends BaseUserListFragment {


	public static Boolean sShowFacebookInvitesIcon = true;
	private CallbackManager mCallbackManager;
	private Boolean mResultFromCallback = false;
	private Boolean mAskForPermAlreadyTried = false;
	private Boolean mFBDisconnectedOk = false;

	@Override
	public void onResume() {
		super.onResume();
		if (!FacebookSdk.isInitialized()) {
			FacebookSdk.sdkInitialize(getActivity());
		}
		onRefresh();
		setTitle(R.string.find_your_fb_friends_title);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);

		mCallbackManager = CallbackManager.Factory.create();
	}

	@Override
	protected void getDataFromServer() {
		if (!FacebookUtil.getIsFacebookLoggedInAlready(getBaseActivity())) {
			mUserList.setEmptyView(mEmptyView);
			mEmptyMessage.setText(getString(R.string.facebook_not_linked));
			mEmptyHint.setText(getString(R.string.facebook_not_linked_hint));
			mBtnAction.setVisibility(View.VISIBLE);
			mBtnAction.setText(getString(R.string.link_account));
			mBtnAction.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					ScreenManager.showSocialNetworksConfigActivity((BaseActivity) getActivity());
				}
			});
			trackEmptyOrNotLinkedFB(false);
			disableRefreshSwipe();

		} else {
			enableSwipeGesture();
			if (!FacebookUtil.getIsFriendsPermissionGranted(getActivity())) {
				if(!mAskForPermAlreadyTried){
					mAskForPermAlreadyTried = true;
					mResultFromCallback = true;
					sShowFacebookInvitesIcon = true;
					getActivity().supportInvalidateOptionsMenu();
					FacebookUtil.requestFriendsPermissions(getActivity(), getContext());
				}
			} else {
				showProgress();
				BaseParkSessionRequest<UserListResponse> request = new FacebookFriendsRequest(ParkApplication.getInstance().getUsername());
				mSpiceManager.execute(request, new UserListener());
			}

		}
	}

	@Override
	public void onBackPressed() {
	}

	private class UserListener extends BaseListFragment.ListListener {

		@Override
		public void onRequestError(BaseNeckRequestException.Error error) {

			if (error != null) {
				if (error.getErrorCode() == 821) {
					logoutFromFacebook();
				} else {
					MessageUtil.showError(getBaseActivity(), error.getMessage(),
								getBaseActivity().getCroutonsHolder());
				}
			}

			hideProgress();
		}
	};

	private void logoutFromFacebook() {
		showProgress();
		mSpiceManager.execute(new SocialDisconnectRequest("facebook"), new SocialNetworksDisconnectListener());
	}

	private class SocialNetworksDisconnectListener extends BaseNeckRequestListener<Boolean> {

		@Override
		public void onRequestError(BaseNeckRequestException.Error error) {
			hideProgress();
			if(!mFBDisconnectedOk) {
				MessageUtil.showError((BaseActivity) getActivity(), error.getMessage(),
						getBaseActivity().getCroutonsHolder());
			}
		}

		@Override
		public void onRequestSuccessfull(Boolean t) {
			if (t) {
				mFBDisconnectedOk=true;
				FacebookUtil.logoutFromFacebook(getBaseActivity());
				mUserList.setEmptyView(mEmptyView);
				mEmptyMessage.setText(getString(R.string.facebook_not_linked));
				mEmptyHint.setText(getString(R.string.facebook_not_linked_hint));
				mBtnAction.setVisibility(View.VISIBLE);
				mBtnAction.setText(getString(R.string.link_account));
				mBtnAction.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						ScreenManager.showSocialNetworksConfigActivity((BaseActivity) getActivity());
					}
				});
				trackEmptyOrNotLinkedFB(false);
				disableRefreshSwipe();
				hideProgress();
			}
		}

		@Override
		public void onRequestException(SpiceException exception) {
			hideProgress();
			MessageUtil.showError((BaseActivity) getActivity(), exception.getMessage(),
					getBaseActivity().getCroutonsHolder());
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.menu_fb_friends, menu);
		if (menu.findItem(R.id.action_fb_invite) != null){
			menu.findItem(R.id.action_fb_invite).setVisible(sShowFacebookInvitesIcon);
		}
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
			case R.id.action_fb_invite:
				showFacebookInvitesDialog();
				break;
			default:
				break;
		}

		return super.onOptionsItemSelected(item);
	}

	private void showFacebookInvitesDialog()
	{
		Toast.makeText(getBaseActivity(), getResources().getString(R.string.facebook_invite_initialize), Toast.LENGTH_SHORT).show();

		String appLinkUrl, previewImageUrl;

		appLinkUrl = ParkApplication.sParkConfiguration.getAppLink();
		previewImageUrl = "http://88ce383dde9f48196995-397f88d2b1d49d7274c1bd48b0389bff.r7.cf5.rackcdn.com/static/FB-invite_0001_v1a.png";

		if (AppInviteDialog.canShow()) {
			AppInviteContent content = new AppInviteContent.Builder()
					.setApplinkUrl(appLinkUrl)
					.setPreviewImageUrl(previewImageUrl)
					.build();
			AppInviteDialog.show(this, content);
		}
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		sShowFacebookInvitesIcon = false;
		getActivity().supportInvalidateOptionsMenu();
		createCallback();
	}

	@Override
	protected void loadData(UserListResponse aResponse) {

		mIsLoading = false;
		if(aResponse.getFriends()!=null){
			if (!aResponse.getFriends().isEmpty()) {
				mLoadedAllItems = true;
				getAdapter().merge(aResponse.getFriends());
			}
		}
		sShowFacebookInvitesIcon = true;
		getActivity().supportInvalidateOptionsMenu();
		if (getAdapter().isEmpty()) {
			mUserList.setEmptyView(mEmptyView);
			mEmptyMessage.setText(aResponse.getNoResultsMessage());
			mEmptyHint.setText(aResponse.getNoResultsHint());
			mBtnAction.setVisibility(View.VISIBLE);
			mBtnAction.setText(getString(R.string.invite_friends_tit));
			mBtnAction.setCompoundDrawablesWithIntrinsicBounds(R.drawable.icon_white_fb_invite, 0, 0, 0);
			mBtnAction.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					showFacebookInvitesDialog();
				}
			});
			trackEmptyOrNotLinkedFB(true);
		} else {
			mUserList.setEmptyView(null);
		}
		getAdapter().notifyDataSetChanged();
	}

	private void createCallback() {
		LoginManager.getInstance().registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
			@Override
			public void onSuccess(LoginResult loginResult) {
				FacebookUtil.saveIsFriendsPermissionGranted(getContext(), loginResult
						.getAccessToken().getPermissions().contains("user_friends"));
			}

			@Override
			public void onCancel() {
				showPermissionError();
			}

			@Override
			public void onError(FacebookException e) {
			}
		});
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (resultCode == Activity.RESULT_OK) {
			if (requestCode == FacebookUtil.REQUEST_PUBLISH_FB) {
				mResultFromCallback = true;
				mCallbackManager.onActivityResult(requestCode, resultCode, data);
			}
		} else {
			showPermissionError();
		}
	}

	private void showPermissionError(){
		mUserList.setEmptyView(mEmptyView);
		mEmptyMessage.setText(getString(R.string.facebook_friends_not_granted));
		mEmptyHint.setText(getString(R.string.facebook_friends_not_granted_hint));
	}

	private void trackEmptyOrNotLinkedFB (boolean emptyFriendsList) {
		if (emptyFriendsList) {
			ParkApplication
					.getInstance()
					.getTracker(ParkApplication.TrackerName.GLOBAL_TRACKER)
					.send(new HitBuilders.EventBuilder().setCategory("Do not have fb friends using vivanuncios")
							.setAction("Amount of users that do not have fb friends using vivanuncios").setLabel("The user does not have fb friends using vivanuncios")
							.build());
			ParkApplication.getInstance().getEventsLogger().logEvent(FacebookUtil.EVENT_NOT_FRIENDS_FIND_FB_FRIENDS);
		} else {
			ParkApplication
					.getInstance()
					.getTracker(ParkApplication.TrackerName.GLOBAL_TRACKER)
					.send(new HitBuilders.EventBuilder().setCategory("Have not linked his account with fb")
							.setAction("Amount of users that have not linked his account with fb").setLabel("The user has not linked his account with fb")
							.build());
			ParkApplication.getInstance().getEventsLogger().logEvent(FacebookUtil.EVENT_NOT_LINKED_FIND_FB_FRIENDS);
		}
	}
}