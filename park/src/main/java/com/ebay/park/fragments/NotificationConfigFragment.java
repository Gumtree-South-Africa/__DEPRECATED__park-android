package com.ebay.park.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.ebay.park.ParkApplication;
import com.ebay.park.R;
import com.ebay.park.base.BaseActivity;
import com.ebay.park.interfaces.OnShowListenerLollipop;
import com.ebay.park.interfaces.OnShowListenerPreLollipop;
import com.ebay.park.model.NotificationConfigModel;
import com.ebay.park.model.ProfileModel;
import com.ebay.park.requests.NotificationConfigGetRequest;
import com.ebay.park.requests.NotificationConfigUpdateRequest;
import com.ebay.park.requests.ProfileRequest;
import com.ebay.park.requests.SendVerificationMailRequest;
import com.ebay.park.responses.NotificationConfigResponse;
import com.ebay.park.utils.DeviceUtils;
import com.ebay.park.utils.DialogUtils;
import com.ebay.park.utils.MessageUtil;
import com.ebay.park.utils.PreferencesUtil;
import com.ebay.park.views.CustomSwipeRefresh;
import com.ebay.park.views.TextViewBook;
import com.globant.roboneck.common.NeckSpiceManager;
import com.globant.roboneck.requests.BaseNeckRequestException.Error;
import com.globant.roboneck.requests.BaseNeckRequestListener;
import com.octo.android.robospice.persistence.exception.SpiceException;

import java.util.HashMap;
import java.util.Map;

/**
 * Notification Configuration Fragment
 *
 * @author Nicol�s Mat�as Fern�ndez
 *
 */

public class NotificationConfigFragment extends Fragment implements OnRefreshListener {
	private CustomSwipeRefresh mSyncProgressBar;
	private NeckSpiceManager mSpiceManager = new NeckSpiceManager();

	private View mLayout;
	private boolean mIsShowingProgress;
	private MenuItem mActionOk;

	private TextViewBook mItemPending;
	private CheckBox mEmailItemPending;
	private CheckBox mPushItemPending;

	private TextViewBook mNewMessage;
	private CheckBox mEmailNewMessage;
	private CheckBox mPushNewMessage;

	private TextViewBook mOfferAccepted;
	private CheckBox mEmailOfferAccepted;
	private CheckBox mPushOfferAccepted;

	private TextViewBook mOfferRejected;
	private CheckBox mEmailOfferRejected;
	private CheckBox mPushOfferRejected;

	private TextViewBook mUserRatedYou;
	private CheckBox mPushUserRatedYou;

	private TextViewBook mStartsFollowing;
	private CheckBox mPushStartsFollowing;

	private TextViewBook mFollowedItemSold;
	private CheckBox mPushFollowedItemSold;

	private TextViewBook mInterestedItemSold;
	private CheckBox mPushInterestedItemSold;
	private CheckBox mEmailInterestedItemSold;

	private TextViewBook mFriendFb;
	private CheckBox mPushFriendFb;
	private CheckBox mEmailFriendFb;


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View parentView = inflater.inflate(R.layout.fragment_base, container, false);
		mSyncProgressBar = (CustomSwipeRefresh) parentView.findViewById(R.id.swipe_container);
		mSyncProgressBar.setOnRefreshListener(this);
		disableRefreshSwipe();

		View baseView = inflater.inflate(R.layout.fragment_notification_config, (ViewGroup) parentView, true);

		mItemPending = (TextViewBook) baseView.findViewById(R.id.tv_email_item_pending);
		mEmailItemPending = (CheckBox) baseView.findViewById(R.id.email_item_pending);
		mPushItemPending = (CheckBox) baseView.findViewById(R.id.push_item_pending);

		mNewMessage = (TextViewBook) baseView.findViewById(R.id.tv_email_new_message);
		mEmailNewMessage = (CheckBox) baseView.findViewById(R.id.email_new_message);
		mPushNewMessage = (CheckBox) baseView.findViewById(R.id.push_new_message);

		mOfferAccepted = (TextViewBook) baseView.findViewById(R.id.tv_email_offer_accepted);
		mEmailOfferAccepted = (CheckBox) baseView.findViewById(R.id.email_offer_accepted);
		mPushOfferAccepted = (CheckBox) baseView.findViewById(R.id.push_offer_accepted);

		mOfferRejected = (TextViewBook) baseView.findViewById(R.id.tv_email_offer_rejected);
		mEmailOfferRejected = (CheckBox) baseView.findViewById(R.id.email_offer_rejected);
		mPushOfferRejected = (CheckBox) baseView.findViewById(R.id.push_offer_rejected);

		mUserRatedYou = (TextViewBook) baseView.findViewById(R.id.tv_user_rated_you);
		mPushUserRatedYou = (CheckBox) baseView.findViewById(R.id.push_user_rated_you);

		mStartsFollowing = (TextViewBook) baseView.findViewById(R.id.tv_starts_following_you);
		mPushStartsFollowing = (CheckBox) baseView.findViewById(R.id.push_starts_following_you);

		mFollowedItemSold = (TextViewBook) baseView.findViewById(R.id.tv_followed_item_sold);
		mPushFollowedItemSold = (CheckBox) baseView.findViewById(R.id.push_followed_item_sold);

		mInterestedItemSold = (TextViewBook) baseView.findViewById(R.id.tv_interested_item_sold);
		mPushInterestedItemSold = (CheckBox) baseView.findViewById(R.id.push_interested_item_sold);
		mEmailInterestedItemSold = (CheckBox) baseView.findViewById(R.id.email_interested_item_sold);

		mFriendFb = (TextViewBook) baseView.findViewById(R.id.tv_friend_fb);
		mPushFriendFb = (CheckBox) baseView.findViewById(R.id.push_friend_fb);
		mEmailFriendFb = (CheckBox) baseView.findViewById(R.id.email_friend_fb);

		mLayout = baseView.findViewById(R.id.notif_config_main_layout);
		mLayout.setVisibility(View.INVISIBLE);

		return baseView;
	}


	CompoundButton.OnCheckedChangeListener listener = (new CompoundButton.OnCheckedChangeListener(){
		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			buttonView.setChecked(false);
			showEmailNotVerifiedDialog();
		}
	});

	private void showEmailNotVerifiedDialog() {
		final AlertDialog dialog = DialogUtils.getDialogWithLabel(getActivity(), R.string.ooops,
				R.string.mail_not)
				.setPositiveButton(R.string.text_ok, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				}).create();

		if (DeviceUtils.isDeviceLollipopOrHigher()) {
			dialog.setOnShowListener(new OnShowListenerLollipop(dialog, getActivity()));
		} else {
			dialog.setOnShowListener(new OnShowListenerPreLollipop(dialog, getActivity()));
		}

		dialog.show();
	}


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mSpiceManager = new NeckSpiceManager();
		setHasOptionsMenu(true);
		if (savedInstanceState != null) {
			mIsShowingProgress = savedInstanceState.getBoolean("isShowingProgress");
		}

		((BaseActivity) getActivity()).setTitle(R.string.notifications_push_config);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		menu.clear();
		inflater.inflate(R.menu.menu_filter, menu);
		mActionOk = menu.findItem(R.id.action_ok);
		if (mActionOk != null) {
			mActionOk.setVisible(false);
		}
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.action_ok) {
			showProgress();
			updateNotificationsConfig();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onStart() {
		mSpiceManager.start(this.getActivity());
		super.onStart();
	}

	@Override
	public void onStop() {
		mSpiceManager.shouldStop();
		super.onStop();
	}

	@Override
	public void onResume() {
		super.onResume();
		if (mIsShowingProgress) {
			showProgress();
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putBoolean("isShowingProgress", mIsShowingProgress);
		super.onSaveInstanceState(outState);
	}

	/**
	 * Shows {@link SwipeRefreshLayout} on top of the screen. Override this
	 * method to provide additional actions but remember to call super.
	 */
	protected void showProgress() {
		mIsShowingProgress = true;
		mSyncProgressBar.setRefreshing(mIsShowingProgress);
	}

	/**
	 * Hides {@link SwipeRefreshLayout}.Override this method to provide
	 * additional actions but remember to call super.
	 */
	protected void hideProgress() {
		mIsShowingProgress = false;
		mSyncProgressBar.setRefreshing(mIsShowingProgress);
	}

	/**
	 * Call this on your fragment's {@link #onActivityCreated(Bundle)} if you
	 * want to disable swipe refresh gesture.
	 */
	protected void disableRefreshSwipe() {
		mSyncProgressBar.setEnabled(false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mSyncProgressBar.setColorSchemeResources(R.color.VivaBlue, R.color.VivaYellow, R.color.VivaGreen, R.color.VivaRed);
		showProgress();
		onRefresh();

	}

	@Override
	public void onRefresh() {
		mSpiceManager.execute(new ProfileRequest(ParkApplication.getInstance().getUsername()),
				new VerifiedListener());
		getNotificationsConfig();
	}

	private void updateNotificationsConfig() {

		NotificationConfigResponse params = new NotificationConfigResponse();
		Map<String, NotificationConfigModel> GENERAL = new HashMap<String, NotificationConfigModel>();
		Map<String, NotificationConfigModel> MY_PUBLICATION = new HashMap<String, NotificationConfigModel>();
		Map<String, NotificationConfigModel> NEGOTIATION_CHAT = new HashMap<String, NotificationConfigModel>();

		// My Publications
		MY_PUBLICATION.put("ITEM_BANNED", new NotificationConfigModel(mItemPending.getText().toString(),
				mEmailItemPending.isChecked(), mPushItemPending.isChecked()));

		// Negotiation Chat
		NEGOTIATION_CHAT.put("CHAT_SENT", new NotificationConfigModel(mNewMessage.getText().toString(),
				mEmailNewMessage.isChecked(), mPushNewMessage.isChecked()));
		NEGOTIATION_CHAT.put("CONVERSATION_ACCEPTED", new NotificationConfigModel(mOfferAccepted.getText()
				.toString(), mEmailOfferAccepted.isChecked(), mPushOfferAccepted.isChecked()));
		NEGOTIATION_CHAT.put("CONVERSATION_REJECTED", new NotificationConfigModel(mOfferRejected.getText()
				.toString(), mEmailOfferRejected.isChecked(), mPushOfferRejected.isChecked()));

		// General
		GENERAL.put("USER_RATED", new NotificationConfigModel(mUserRatedYou.getText().toString(), null,
				mPushUserRatedYou.isChecked()));
		GENERAL.put("FOLLOW_USER", new NotificationConfigModel(mStartsFollowing.getText().toString(), null,
				mPushStartsFollowing.isChecked()));
		GENERAL.put("SOLD_AN_ITEM", new NotificationConfigModel(mFollowedItemSold.getText().toString(), null,
				mPushFollowedItemSold.isChecked()));
		GENERAL.put("SOLD_AN_ITEM_FOR_INTERESTED_FOLLOWERS", new NotificationConfigModel(mInterestedItemSold.getText().toString(),
				mEmailInterestedItemSold.isChecked(), mPushInterestedItemSold.isChecked()));
		GENERAL.put("FB_FRIEND_USING_THE_APP", new NotificationConfigModel(mFriendFb.getText().toString(), mEmailFriendFb.isChecked(),
				mPushFriendFb.isChecked()));


		params.setNegotiationChatSetting(NEGOTIATION_CHAT);
		params.setGeneralSettings(GENERAL);
		params.setMyPublicationsSettings(MY_PUBLICATION);

		mSpiceManager.execute(new NotificationConfigUpdateRequest(params), new UpdateNotificationsConfigListener());

	}

	private void getNotificationsConfig() {
		mSpiceManager.execute(new NotificationConfigGetRequest(), new GetNotificationsConfigListener());
	}

	private class UpdateNotificationsConfigListener extends BaseNeckRequestListener<Boolean> {

		@Override
		public void onRequestError(Error error) {

			hideProgress();
			MessageUtil.showError((BaseActivity) getActivity(), error.getMessage(),
					((BaseActivity) getActivity()).getCroutonsHolder());

		}

		@Override
		public void onRequestSuccessfull(Boolean t) {

			if (t) {
				hideProgress();
				MessageUtil.showSuccess((BaseActivity) getActivity(), getString(R.string.notification_config_updated),
						((BaseActivity) getActivity()).getCroutonsHolder());
			}

		}

		@Override
		public void onRequestException(SpiceException exception) {

			hideProgress();
			MessageUtil.showError((BaseActivity) getActivity(), exception.getMessage(),
					((BaseActivity) getActivity()).getCroutonsHolder());

		}
	}

	private class GetNotificationsConfigListener extends BaseNeckRequestListener<NotificationConfigResponse> {

		@Override
		public void onRequestError(Error error) {
			hideProgress();
			MessageUtil.showError((BaseActivity) getActivity(), error.getMessage(),
					((BaseActivity) getActivity()).getCroutonsHolder());
		}

		@Override
		public void onRequestSuccessfull(NotificationConfigResponse notificationsConfig) {
			fillConfigurationFields(notificationsConfig);
			if (mActionOk != null){
				mActionOk.setVisible(true);
			}
			mLayout.setVisibility(View.VISIBLE);
			hideProgress();
		}

		@Override
		public void onRequestException(SpiceException exception) {
			hideProgress();
			MessageUtil.showError((BaseActivity) getActivity(), exception.getMessage(),
					((BaseActivity) getActivity()).getCroutonsHolder());
		}
	}

	private void fillConfigurationFields(NotificationConfigResponse notificationsConfig) {

		Map<String, NotificationConfigModel> generalSettings = notificationsConfig.getGeneralSettings();
		Map<String, NotificationConfigModel> myPublicationsSettings = notificationsConfig.getMyPublicationsSettings();
		Map<String, NotificationConfigModel> negotiationChatSetting = notificationsConfig.getNegotiationChatSetting();

		// My Publications
		setFieldStateFull(mItemPending, mEmailItemPending, myPublicationsSettings.get("ITEM_BANNED"), "mail");
		setFieldState(mPushItemPending, myPublicationsSettings.get("ITEM_BANNED"), "push");
		// Negotiation Chat
		setFieldStateFull(mNewMessage, mEmailNewMessage, negotiationChatSetting.get("CHAT_SENT"), "mail");
		setFieldStateFull(mOfferAccepted, mEmailOfferAccepted, negotiationChatSetting.get("CONVERSATION_ACCEPTED"), "mail");
		setFieldStateFull(mOfferRejected, mEmailOfferRejected, negotiationChatSetting.get("CONVERSATION_REJECTED"), "mail");
		setFieldState(mPushNewMessage, negotiationChatSetting.get("CHAT_SENT"), "push");
		setFieldState(mPushOfferAccepted, negotiationChatSetting.get("CONVERSATION_ACCEPTED"), "push");
		setFieldState(mPushOfferRejected, negotiationChatSetting.get("CONVERSATION_REJECTED"), "push");
		// General
		setFieldStateFull(mUserRatedYou, mPushUserRatedYou, generalSettings.get("USER_RATED"), "push");
		setFieldStateFull(mStartsFollowing, mPushStartsFollowing, generalSettings.get("FOLLOW_USER"), "push");
		setFieldStateFull(mFollowedItemSold, mPushFollowedItemSold, generalSettings.get("SOLD_AN_ITEM"), "push");
		setFieldStateFull(mInterestedItemSold, mEmailInterestedItemSold, generalSettings.get("SOLD_AN_ITEM_FOR_INTERESTED_FOLLOWERS"), "mail");
		setFieldState(mPushInterestedItemSold, generalSettings.get("SOLD_AN_ITEM_FOR_INTERESTED_FOLLOWERS"), "push");
		setFieldStateFull(mFriendFb, mPushFriendFb, generalSettings.get("FB_FRIEND_USING_THE_APP"), "push");
		setFieldStateFull(mFriendFb, mEmailFriendFb, generalSettings.get("FB_FRIEND_USING_THE_APP"), "mail");

	}

	private void setFieldStateFull(TextView aTextView, CheckBox aCheckBox, NotificationConfigModel oNot, String type) {
		if (oNot != null) {
			aTextView.setText(oNot.getActionDisplayName());
			if (type.equals("push")) {
				aCheckBox.setChecked(oNot.getPushConfig());
			} else {// mail
				aCheckBox.setChecked(oNot.getEmailConfig());
			}
		}
	}

	private void setFieldState(CheckBox chk, NotificationConfigModel oNot, String type) {
		if (oNot != null) {
			if (type.equals("push")) {
				chk.setChecked(oNot.getPushConfig());
			} else {// mail
				chk.setChecked(oNot.getEmailConfig());
			}
		}
	}

	private class VerifiedListener extends BaseNeckRequestListener<ProfileModel> {

		@Override
		public void onRequestError(Error error) {
			hideProgress();
		}

		@Override
		public void onRequestSuccessfull(ProfileModel profile) {
			if (!profile.isVerified()) {
				mEmailItemPending.setOnCheckedChangeListener(listener);
				mEmailNewMessage.setOnCheckedChangeListener(listener);
				mEmailOfferAccepted.setOnCheckedChangeListener(listener);
				mEmailOfferRejected.setOnCheckedChangeListener(listener);
				mEmailInterestedItemSold.setOnCheckedChangeListener(listener);
				mEmailFriendFb.setOnCheckedChangeListener(listener);
			}
		}

		@Override
		public void onRequestException(SpiceException exception) {
			hideProgress();
		}

	}
}
