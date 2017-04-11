package com.ebay.park.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ebay.park.ParkApplication;
import com.ebay.park.R;
import com.ebay.park.base.BaseActivity;
import com.ebay.park.base.BaseItemListFragment;
import com.ebay.park.flow.IntentFactory;
import com.ebay.park.flow.ScreenManager;
import com.ebay.park.model.ItemModel;
import com.ebay.park.model.ProfileModel;
import com.ebay.park.requests.FollowUserRequest;
import com.ebay.park.requests.ItemListRequest;
import com.ebay.park.requests.ProfileRequest;
import com.ebay.park.requests.SendVerificationMailRequest;
import com.ebay.park.requests.UnfollowUserRequest;
import com.ebay.park.responses.ItemListResponse;
import com.ebay.park.utils.LocationUtil;
import com.ebay.park.utils.Logger;
import com.ebay.park.utils.MessageUtil;
import com.ebay.park.utils.PreferencesUtil;
import com.ebay.park.utils.SwrveEvents;
import com.ebay.park.views.ButtonDemi;
import com.ebay.park.views.TextViewBook;
import com.ebay.park.views.TextViewDemi;
import com.ebay.park.views.TextViewMedium;
import com.github.clans.fab.FloatingActionButton;
import com.globant.roboneck.requests.BaseNeckRequestException;
import com.globant.roboneck.requests.BaseNeckRequestListener;
import com.google.android.gms.analytics.HitBuilders;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.squareup.picasso.Picasso;
import com.swrve.sdk.SwrveSDK;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by paula.baudo on 4/25/2016.
 */
public class ProfileItemListFragment extends BaseItemListFragment {

    private static final String USERNAME_PARAM = "username";
    private static final String FROMDRAWER_PARAM = "fromdrawer";
    public static final String FROMACTFEED_PARAM = "fromactfeed";
    public static final String TEST_TAG = ProfileItemListFragment.class.getSimpleName();

    private ViewGroup mHeaderContainer;
    private View mProfileView;
    private TextViewBook mUsernameView;
    private TextViewBook mUserMail;
    private TextViewBook mUserPhone;
    private TextViewMedium mPositiveFeedback;
    private TextViewMedium mNeutralFeedback;
    private TextViewMedium mNegativeFeedback;
    private TextView mFollowers;
    private TextViewBook mFollowing;
    private TextViewBook mUserSince;
    private TextViewBook mUserCity;
    private ImageView mProfilePicture;
    private ImageView mEmailBadge;
    private ImageView mPhoneBadge;
    private ImageView mFacebookBadge;
    private ButtonDemi mBtnFollow;
    private TextViewBook mFollowersLabel;
    private TextViewBook mFollowingLabel;
    private TextViewDemi mItemsPublished;
    private TextViewBook mRatingsLabel;
    private View mProfileOwnerLayout;
    private View mProfileUserLayout;
    private ImageView profilePictureOwner;
    private TextViewBook mOwnerName;
    private TextViewBook mOwnerCity;
    private View mDivider;
    private View mCounters;
    private FloatingActionButton mFabPublish;
    protected ProgressBar mProgressbar;

    public static Boolean sShowShareEditMenuItem = false;
    private ProfileModel mProfileInfo;
    private String mUsername;
    private boolean mIsFromDrawer;
    private boolean mIsFromActFeed;
    private boolean mListLoaded = false;
    private int mPreviousVisibleItem;

    /**
     * Use this fragment to show the profile of a given user.
     *
     * @param username
     *            The username to get the profile.
     */
    public static ProfileItemListFragment forUser(String username) {
        return forUser(username, false, false);
    }

    private static ProfileItemListFragment forUser(String username, boolean isFromDrawer, boolean fromActFeed) {
        ProfileItemListFragment fragment = new ProfileItemListFragment();
        Bundle args = new Bundle();
        args.putString(USERNAME_PARAM, username);
        args.putBoolean(FROMDRAWER_PARAM, isFromDrawer);
        args.putBoolean(FROMACTFEED_PARAM, fromActFeed);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Gets the current user profile screen.
     */
    public static ProfileItemListFragment forCurrentUser(boolean isFromDrawer, boolean fromActFeed) {
        return forUser(ParkApplication.getInstance().getUsername(), isFromDrawer, fromActFeed);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mUsername = getArguments().getString(USERNAME_PARAM);
        if (getArguments() != null && getArguments().containsKey(FROMDRAWER_PARAM)) {
            mIsFromDrawer = getArguments().getBoolean(FROMDRAWER_PARAM);
        }
        if (getArguments() != null && getArguments().containsKey(FROMACTFEED_PARAM)) {
            mIsFromActFeed = getArguments().getBoolean(FROMACTFEED_PARAM);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        setTitle(isOwnProfile() ? getString(R.string.my_profile) : getString(R.string.user_profile));
        View baseView = inflater.inflate(R.layout.fragment_profile_list,
                getContainerLayout(inflater, container, savedInstanceState), true);
        setUpFab(baseView);
        return baseView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.menu_profile, menu);
        if (menu.findItem(R.id.edit_profile) != null) {
            menu.findItem(R.id.edit_profile).setVisible(isOwnProfile());
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        try {
            if (item.getItemId() == R.id.edit_profile) {
                getBaseActivity().getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
                ScreenManager.showOptionsScreen(getBaseActivity());
            }
            if (item.getItemId() == R.id.share_profile) {
                if (mProfileInfo != null) {
                    if (!TextUtils.isEmpty(mProfileInfo.getUrl())) {
                        startActivity(IntentFactory.getShareProfileIntent(getBaseActivity(), mProfileInfo.getUrl()));
                    }
                }
            }
        } catch (NullPointerException e) {
        } catch (ClassCastException e) {
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onViewCreated(View rootView, Bundle savedInstanceState) {
        super.onViewCreated(rootView, savedInstanceState);
        mProgressbar = (ProgressBar) rootView.findViewById(R.id.progress_bar);
        mItemGrid.setOnScrollListener(new ProfileScroll());
    }

    protected void showProgressBar(){
        mProgressbar.setVisibility(View.VISIBLE);
        mIsLoading = true;
    }

    protected void hideProgressBar(){
        mProgressbar.setVisibility(View.GONE);
        mIsLoading = false;
    }


    @Override
    protected void loadHeaderView() {
        mHeaderContainer = (ViewGroup) View.inflate(getBaseActivity(), R.layout.header_container, null);
        mProfileView = View.inflate(getBaseActivity(), R.layout.header_profile_layout, null);
        mProfileView.setVisibility(View.INVISIBLE);

        mProfileOwnerLayout = mProfileView.findViewById(R.id.ly_owner_profile);
        mProfileUserLayout = mProfileView.findViewById(R.id.ly_user_profile);
        mDivider = mProfileView.findViewById(R.id.v_divider);
        mCounters = mProfileView.findViewById(R.id.ly_profile_counters);

        mEmailBadge = (ImageView) mProfileView.findViewById(R.id.profile_email_verified);
        mFacebookBadge = (ImageView) mProfileView.findViewById(R.id.profile_facebook_verified);
        mPhoneBadge = (ImageView) mProfileView.findViewById(R.id.profile_phone_verified);

        mUsernameView = (TextViewBook) mProfileView.findViewById(R.id.tv_username_profile);
        mItemsPublished = (TextViewDemi) mProfileView.findViewById(R.id.tv_items_counter);
        mUserMail = (TextViewBook) mProfileView.findViewById(R.id.tv_user_mail);
        mUserPhone = (TextViewBook) mProfileView.findViewById(R.id.tv_user_phone);
        mUserCity = (TextViewBook) mProfileView.findViewById(R.id.tv_city_profile);
        mOwnerName = (TextViewBook) mProfileView.findViewById(R.id.tv_owner_name);
        mOwnerCity = (TextViewBook) mProfileView.findViewById(R.id.tv_city_profile_owner);

        mRatingsLabel = (TextViewBook) mProfileView.findViewById(R.id.tv_qualification_label);
        mRatingsLabel.setOnClickListener(new ClickListener());

        mPositiveFeedback = (TextViewMedium) mProfileView.findViewById(R.id.tv_positive_feedback);
        mPositiveFeedback.setOnClickListener(new ClickListener());

        mNeutralFeedback = (TextViewMedium) mProfileView.findViewById(R.id.tv_neutral_feedback);
        mNeutralFeedback.setOnClickListener(new ClickListener());

        mNegativeFeedback = (TextViewMedium) mProfileView.findViewById(R.id.tv_negative_feedback);
        mNegativeFeedback.setOnClickListener(new ClickListener());

        mFollowers = (TextViewBook) mProfileView.findViewById(R.id.tv_followers);
        mFollowers.setOnClickListener(new ClickListener());

        mFollowersLabel = (TextViewBook) mProfileView.findViewById(R.id.tv_followers_label);
        mFollowersLabel.setOnClickListener(new ClickListener());

        mFollowing = (TextViewBook) mProfileView.findViewById(R.id.tv_following);
        mFollowing.setOnClickListener(new ClickListener());

        mFollowingLabel = (TextViewBook) mProfileView.findViewById(R.id.tv_following_label);
        mFollowingLabel.setOnClickListener(new ClickListener());

        mBtnFollow = (ButtonDemi) mProfileView.findViewById(R.id.profile_btn_follow);
        mBtnFollow.setOnClickListener(new FollowClickListener());

        mUserSince = (TextViewBook) mProfileView.findViewById(R.id.tv_user_since);
        mProfilePicture = (ImageView) mProfileView.findViewById(R.id.img_profile);
        profilePictureOwner = (ImageView) mProfileView.findViewById(R.id.img_profile_owner);

        if (isOwnProfile()) {
            mBtnFollow.setVisibility(View.GONE);
        } else {
            mBtnFollow.setVisibility(View.VISIBLE);
        }

        mHeaderContainer.addView(mProfileView);
        mItemGrid.removeHeaderView(mHeaderContainer);
        mItemGrid.addHeaderView(mHeaderContainer);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        try {
            disableRefreshSwipe();
            if ((mUsername.equals(ParkApplication.getInstance().getUsername()) && !PreferencesUtil
                    .hasShownItemAccountVerificationMessage(getActivity()))) {
                mSpiceManager.execute(new ProfileRequest(ParkApplication.getInstance().getUsername()),
                        new VerifiedListener());
            }
        } catch (Exception e) {
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        sShowShareEditMenuItem = false;
        if(mListLoaded){
            mSpiceManager.executeCacheRequest(new ProfileRequest(mUsername), new ProfileInfoListener(false));
        }
        else {
            mSpiceManager.executeCacheRequest(new ProfileRequest(mUsername), new ProfileInfoListener(true));
        }

        if (mFabPublish !=null) {
            mFabPublish.setVisibility(View.VISIBLE);
        }
        if (ParkApplication.sJustLoggedThirdLevel) {
            navigationFlow();
        }
    }

    @Override
    public void onRefresh() {
        sShowShareEditMenuItem = false;
        hideProgress();
        showProgressBar();
        mSpiceManager.executeCacheRequest(new ProfileRequest(mUsername), new ProfileInfoListener(true));
    }

    @Override
    public void onStart() {
        mHasPendingRequest = false;
        super.onStart();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mFabPublish !=null) {
            mFabPublish.setVisibility(View.GONE);
        }
    }

    @Override
    protected void getDataFromServer() {
        showProgressBar();
        Location location = LocationUtil.getLocation(getBaseActivity());

        if (location != null) {
            new LocationUtil.LocationResolverTask(getBaseActivity(), location, new LocationUtil.LocationResolverCallback() {
                @Override
                public void onLocationResolved(Location location) {
                    startItemsRequest(location);
                }
            }).execute();
        } else {
            startItemsRequest(location);
        }
    }

    @Override
    protected void loadData(ItemListResponse aResponse) {
        mIsLoading = false;
        if (!aResponse.getItems().isEmpty()) {
            List<ItemModel> itemsList = new ArrayList<ItemModel>();
            itemsList.addAll(aResponse.getItems());
            long fetchedItems = mItemAdapter.getCount() + aResponse.getItems().size();
            mLoadedAllItems = aResponse.getAmountItemsFound() <= fetchedItems;
            mItemAdapter.merge(itemsList);
        }
        if (mItemAdapter.isEmpty()) {
            mEmptyMessage.setText(aResponse.getNoResultsMessage());
            mEmptyHint.setText(aResponse.getNoResultsHint());
            mEmptyView.setPadding(0, mHeaderContainer.getHeight(), 0, 0);
            mEmptyView.setVisibility(View.VISIBLE);
        } else {
            mEmptyView.setVisibility(View.GONE);
        }
        hideProgressBar();
    }

    @Override
    protected void onItemClicked(ItemModel aItem, int pos) {
        if (aItem != null) {
            if (aItem.getId() != null){
                ParkApplication.sItemTapped = aItem;
                ScreenManager.showItemDetailActivity(getBaseActivity(), aItem.getId(),
                        null, null);
            }
        }
    }

    private void startItemsRequest(Location location) {
        // @formatter:off
        ItemListRequest.Builder builder = new ItemListRequest.Builder().page(page.longValue())
                .pageSize(((Integer) PAGE_SIZE).longValue()).forUser(mUsername);

        if (location != null) {
            builder.withPos(location.getLatitude(), location.getLongitude());
        }

        // @formatter:on
        mSpiceManager.execute(builder.build(), new ProfileItemListListener());
    }

    @Override
    public void onBackPressed() {
    }

    private class ProfileItemListListener extends ListListener {
    }

    private void navigationFlow() {
        ParkApplication.sJustLoggedThirdLevel = false;

        if (ParkApplication.sFgmtOrAct_toGo != null) {
            switch (ParkApplication.sFgmtOrAct_toGo) {
                case POST_AD:
                    ParkApplication
                            .getInstance()
                            .getTracker(ParkApplication.TrackerName.GLOBAL_TRACKER)
                            .send(new HitBuilders.EventBuilder().setCategory("Clicks on Publish")
                                    .setAction("Clicks on Publish").setLabel("The user entered to publish an item").build());
                    ScreenManager.showCameraActivity((BaseActivity) getActivity());
                    break;
                default:
                    break;
            }
            ParkApplication.sFgmtOrAct_toGo = null;
        }
    }

    private boolean isOwnProfile() {
        if (!TextUtils.isEmpty(ParkApplication.getInstance().getUsername())) {
            return ParkApplication.getInstance().getUsername().equals(mUsername);
        } else {
            return false;
        }
    }

    private void setUpFab(View aView) {
        mFabPublish = (FloatingActionButton) aView.findViewById(R.id.fab_publish);
        if (mFabPublish != null) {
            mFabPublish.setVisibility(View.VISIBLE);
            mFabPublish.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (PreferencesUtil.getParkToken(getActivity()) != null) {
                        ParkApplication
                                .getInstance()
                                .getTracker(ParkApplication.TrackerName.GLOBAL_TRACKER)
                                .send(new HitBuilders.EventBuilder().setCategory("Clicks on Publish")
                                        .setAction("Clicks on Publish").setLabel("The user entered to publish an item")
                                        .build());
                        ScreenManager.showCameraActivity((BaseActivity) getActivity());
                    } else {
                        MessageUtil.showLoginMsg(getActivity(), ParkApplication.UnloggedNavigations.POST_AD);
                    }
                }
            });
        }
    }

    private void setUpUserLayout(){
        if (isOwnProfile()){
            mProfileUserLayout.setVisibility(View.GONE);
            mProfileOwnerLayout.setVisibility(View.VISIBLE);
            mDivider.setBackgroundColor(getResources().getColor(R.color.DarkGrayDividerProfile));
            mCounters.setBackgroundColor(getResources().getColor(R.color.DarkGrayDrawer));
            mFollowing.setTextColor(getResources().getColor(R.color.White));
            mFollowingLabel.setTextColor(getResources().getColor(R.color.White));
            mFollowers.setTextColor(getResources().getColor(R.color.White));
            mFollowersLabel.setTextColor(getResources().getColor(R.color.White));
            mRatingsLabel.setTextColor(getResources().getColor(R.color.White));
            mRatingsLabel.setText(getString(R.string.myrates_nocapital));
            mFacebookBadge.setImageDrawable(getResources().getDrawable(R.drawable.sotial_fb_light_off));
            mPhoneBadge.setImageDrawable(getResources().getDrawable(R.drawable.sotial_phone_light_off));
            mEmailBadge.setImageDrawable(getResources().getDrawable(R.drawable.sotial_mail_light_off));
        } else {
            mProfileOwnerLayout.setVisibility(View.GONE);
            mProfileUserLayout.setVisibility(View.VISIBLE);
        }
        mProfileView.setVisibility(View.VISIBLE);
    }

    private void showVerifiedBadges(ProfileModel profile) {
        for (String social : profile.getUserSocials()) {
            if (social.equals("facebook")) {
                mFacebookBadge.setImageDrawable(isOwnProfile() ? getResources().getDrawable(R.drawable.sotial_fb_light_on) : getResources().getDrawable(R.drawable.sotial_fb_dark_on));
            }
        }

        if (profile.isMobileVerified()) {
            mPhoneBadge.setImageDrawable(isOwnProfile() ? getResources().getDrawable(R.drawable.sotial_phone_light_on) : getResources().getDrawable(R.drawable.sotial_phone_dark_on));
        }

        if (profile.isVerified()) {
            mEmailBadge.setImageDrawable(isOwnProfile() ? getResources().getDrawable(R.drawable.sotial_mail_light_on) : getResources().getDrawable(R.drawable.sotial_mail_dark_on));
        }
        PreferencesUtil.saveHasEmail(getBaseActivity(),profile.isVerified());
        PreferencesUtil.saveIsSmsUser(getBaseActivity(),profile.isMobileVerified());
    }

    private class ProfileInfoListener extends BaseNeckRequestListener<ProfileModel> {

        boolean mDoRefresh = false;
        ProfileInfoListener (boolean doRefresh){
            this.mDoRefresh = doRefresh;
        }

        @Override
        public void onRequestError(BaseNeckRequestException.Error error) {
            hideProgress();
            hideProgressBar();
            showLoadingError();
        }

        @Override
        public void onRequestSuccessfull(ProfileModel profile) {

            getActivity().supportInvalidateOptionsMenu();
            mProfileInfo = profile;
            mUsernameView.setText(profile.getUsername());
            mOwnerName.setText(profile.getUsername());
            mItemsPublished.setText(String.valueOf(profile.getItemsPublishedCount()));
            mUserMail.setText(profile.getEmail());
            mUserPhone.setText((profile.getPhoneNumber()));
            mUserCity.setText(profile.getLocationName());
            mOwnerCity.setText(profile.getLocationName());
            mPositiveFeedback.setText((profile.getPositiveRatings() >= 100) ? getString(R.string.more_100_rates) : String.valueOf(profile.getPositiveRatings()));
            mNeutralFeedback.setText((profile.getNeutralRatings() >= 100) ? getString(R.string.more_100_rates) : String.valueOf(profile.getNeutralRatings()));
            mNegativeFeedback.setText((profile.getNegativeRatings() >= 100) ? getString(R.string.more_100_rates) : String.valueOf(profile.getNegativeRatings()));
            mFollowers.setText(String.valueOf(profile.getFollowers()));
            mFollowing.setText(String.valueOf(profile.getFollowing()));

            SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");

            mUserSince.setText(String.format(
                    getString(R.string.user_since),
                    dateFormat.format(profile.getCreationDate() * 1000)));

            setUpUserLayout();
            showVerifiedBadges(profile);

            if (!TextUtils.isEmpty(profile.getProfilePicture())) {
                if (isOwnProfile()){
                    Picasso.with(getBaseActivity()).load(profile.getProfilePicture()).placeholder(R.drawable.avatar_big_ph_image_fit_gray)
                            .fit().centerCrop().into(profilePictureOwner);
                } else {
                    Picasso.with(getBaseActivity()).load(profile.getProfilePicture()).placeholder(R.drawable.avatar_ph_image_fit_orange)
                            .fit().centerCrop().into(mProfilePicture);
                }
            }

            setUpFollowBtn(profile.isFollowedByUser());

            hideProgress();

            if (mDoRefresh) {
                clearData();
                page = START_PAGE;
                mHasPendingRequest = true;
                hideProgressBar();
                getDataFromServer();
                mListLoaded = true;
            }
        }

        @Override
        public void onRequestException(SpiceException exception) {
            hideProgress();
            hideProgressBar();
            MessageUtil.showError(getBaseActivity(), exception.getMessage(),
                    getBaseActivity().getCroutonsHolder());
            Logger.error(exception.getMessage());
        }

        private void showLoadingError() {
            MessageUtil.showError(getBaseActivity(), R.string.error_loading_user,
                    getBaseActivity().getCroutonsHolder());
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mListLoaded = false;
    }

    private class VerifiedListener extends BaseNeckRequestListener<ProfileModel> {

        @Override
        public void onRequestError(BaseNeckRequestException.Error error) {
        }

        @Override
        public void onRequestSuccessfull(ProfileModel profile) {
            if (!profile.isVerified() && !profile.isMobileVerified()) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getBaseActivity());
                builder.setTitle(R.string.warning);
                builder.setMessage(getString(R.string.account_not_verified_message));
                builder.setPositiveButton(R.string.account_not_verified_positive,
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                showProgress();
                                mSpiceManager.execute(new SendVerificationMailRequest(),
                                        new SendVerificationMailListener());
                            }
                        });
                builder.setNegativeButton(R.string.account_not_verified_negative,
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                builder.create().show();
                PreferencesUtil.setShownItemAccountVerificationMessage(getActivity(), true);
            }

        }

        @Override
        public void onRequestException(SpiceException exception) {
            hideProgress();
            MessageUtil.showError(getBaseActivity(), exception.getMessage(),
                    getBaseActivity().getCroutonsHolder());
        }

    }

    private class SendVerificationMailListener extends BaseNeckRequestListener<Boolean> {

        @Override
        public void onRequestError(BaseNeckRequestException.Error error) {
            Map<String,String> payload = new HashMap<>();
            payload.put(SwrveEvents.EVENT_FAIL_KEY, error.getMessage());
            SwrveSDK.event(SwrveEvents.USER_ACTIVATION_FAIL, payload);
            hideProgress();
            MessageUtil.showError((BaseActivity) getActivity(), error.getMessage(),
                    getBaseActivity().getCroutonsHolder());
        }

        @Override
        public void onRequestSuccessfull(Boolean t) {
            if (t) {
                MessageUtil.showSuccess((BaseActivity) getActivity(),
                        getResources().getString(R.string.send_verification_email_success),
                        getBaseActivity().getCroutonsHolder());
            }
            SwrveSDK.event(SwrveEvents.USER_ACTIVATION_SUCCESS);
            hideProgress();
        }

        @Override
        public void onRequestException(SpiceException exception) {
            Map<String,String> payload = new HashMap<>();
            payload.put(SwrveEvents.EVENT_FAIL_KEY, exception.getMessage());
            SwrveSDK.event(SwrveEvents.USER_ACTIVATION_FAIL, payload);
            hideProgress();
            MessageUtil.showError((BaseActivity) getActivity(), exception.getMessage(),
                    getBaseActivity().getCroutonsHolder());
        }
    }

    private class ClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.tv_followers:
                case R.id.tv_followers_label:
                    ScreenManager.showFollowersFragment(getBaseActivity(), mUsername, mIsFromDrawer);
                    break;
                case R.id.tv_following:
                case R.id.tv_following_label:
                    ScreenManager.showFollowingFragement(getBaseActivity(), mUsername, mIsFromDrawer);
                    break;
                case R.id.tv_positive_feedback:
                case R.id.tv_neutral_feedback:
                case R.id.tv_negative_feedback:
                case R.id.tv_qualification_label:
                    ScreenManager.showRateListActivity(getBaseActivity(), mUsername);
                    break;
                default:
                    Logger.warn("Profile screen unknown view clicked.");
                    break;
            }
        }
    }

    private class FollowClickListener implements View.OnClickListener {

        BaseNeckRequestListener<Boolean> requestListener = new BaseNeckRequestListener<Boolean>() {

            @Override
            public void onRequestError(BaseNeckRequestException.Error error) {
                MessageUtil.showError(getBaseActivity(), error.getMessage(),
                        getBaseActivity().getCroutonsHolder());
                hideProgress();
            }

            @Override
            public void onRequestSuccessfull(Boolean success) {
                hideProgress();
                mProfileInfo.setFollowedByUser(!mProfileInfo.isFollowedByUser());
                setUpFollowBtn(mProfileInfo.isFollowedByUser());
                mBtnFollow.setEnabled(true);

                int followers = Integer.parseInt(mFollowers.getText().toString());
                if(mProfileInfo.isFollowedByUser()){
                    mFollowers.setText(String.valueOf(followers+1));
                } else {
                    mFollowers.setText(String.valueOf(followers-1));
                }
            }

            @Override
            public void onRequestException(SpiceException exception) {
                MessageUtil.showError(getBaseActivity(), exception.getMessage(),
                        getBaseActivity().getCroutonsHolder());
                hideProgress();
            }
        };

        @Override
        public void onClick(View v) {

            if (PreferencesUtil.getParkToken(getActivity()) != null) {
                showProgress();
                mBtnFollow.setEnabled(false);
                if (mProfileInfo.isFollowedByUser()) {
                    mSpiceManager.execute(new UnfollowUserRequest(mUsername), requestListener);
                } else {
                    mSpiceManager.execute(new FollowUserRequest(mUsername), requestListener);
                }

            } else {
                MessageUtil.showLoginMsg(getActivity(), ParkApplication.UnloggedNavigations.CATEGORIES);
            }

        }

    }

    private void setUpFollowBtn(boolean userFollowsProfile) {
        if (userFollowsProfile) {
            mBtnFollow.setActivated(true);
            mBtnFollow.setText(R.string.following);
        } else {
            mBtnFollow.setActivated(false);
            mBtnFollow.setText(R.string.follow);
        }
    }

    protected class ProfileScroll extends ListScroll{
        @Override
        public void onScroll(AbsListView aListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            if (mFabPublish != null){
                if (mFabPublish.getVisibility() != View.GONE) {
                    if (firstVisibleItem > mPreviousVisibleItem) {
                        mFabPublish.hide(true);
                    } else if (firstVisibleItem < mPreviousVisibleItem) {
                        mFabPublish.show(true);
                    }
                    mPreviousVisibleItem = firstVisibleItem;
                }
            }
            if (mItemGrid.getHeaderViewCount() != 0 && mItemGrid.getCount() == mItemGrid.getNumColumns()){
                visibleItemCount = visibleItemCount - mItemGrid.getNumColumns();
            }
            super.onScroll(aListView, firstVisibleItem, visibleItemCount, totalItemCount);
        }
    }

}
