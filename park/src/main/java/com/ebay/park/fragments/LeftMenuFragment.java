package com.ebay.park.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.ebay.park.ParkApplication;
import com.ebay.park.ParkApplication.UnloggedNavigations;
import com.ebay.park.R;
import com.ebay.park.activities.ParkActivity;
import com.ebay.park.base.BaseActivity;
import com.ebay.park.flow.IntentFactory;
import com.ebay.park.flow.ScreenManager;
import com.ebay.park.model.ProfileModel;
import com.ebay.park.model.UnreadCountModel;
import com.ebay.park.requests.ParkUrls;
import com.ebay.park.requests.ProfileRequest;
import com.ebay.park.requests.UnReadCountRequest;
import com.ebay.park.utils.DeviceUtils;
import com.ebay.park.utils.MessageUtil;
import com.ebay.park.utils.PreferencesUtil;
import com.ebay.park.views.TextViewDemi;
import com.facebook.share.model.AppInviteContent;
import com.facebook.share.widget.AppInviteDialog;
import com.globant.roboneck.common.NeckSpiceManager;
import com.globant.roboneck.requests.BaseNeckRequestException.Error;
import com.globant.roboneck.requests.BaseNeckRequestListener;
import com.google.android.gms.analytics.HitBuilders;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.squareup.picasso.Picasso;

/**
 * Custom Fragment to load the options on the Drawer Navigation
 *
 * @author Nicol�s Mat�as Fern�ndez
 */

public class LeftMenuFragment extends Fragment {

    public static final LeftMenuOption DEFAULT_MENU_OPTION = LeftMenuOption.CATEGORIES;

    protected NeckSpiceManager mSpiceManager = new NeckSpiceManager();
    private TextViewDemi mLogin;
    private TextViewDemi mProfile;
    private TextViewDemi mOffers;
    private TextViewDemi mNavigate;
    private TextViewDemi mPostAd;
    private TextViewDemi mMyLists;
    private TextViewDemi mGroups;
    private TextViewDemi mActivity;
    private TextViewDemi mInviteFriends;
    private TextViewDemi mInviteFbFriends;
    private TextViewDemi mSupport;
    private TextViewDemi mFeedCount;
    private TextViewDemi mGroupCount;
    private View mLastTvSelected;
    private View mProfileLayout;
    private ImageView mProfilePic;
    private ImageView mProfileMask;
    private LinearLayout mLeftMenu;
    private TextViewDemi mLegals;

    public enum LeftMenuOption {
        LOGIN, PROFILE, OFFERS, CATEGORIES, POST_AD, MY_LISTS, GROUPS, ACTIVITY, INVITE_FRIENDS, INVITE_FB_FRIENDS, SUPPORT, LEGALS;
    }

    public LeftMenuFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSpiceManager = new NeckSpiceManager();
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
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View baseview = inflater.inflate(R.layout.fragment_leftmenu, container, false);

        mLeftMenu = (LinearLayout) baseview.findViewById(R.id.left_menu_scroll_area);
        mLogin = (TextViewDemi) baseview.findViewById(R.id.tv_login);
        mProfile = (TextViewDemi) baseview.findViewById(R.id.tv_profile);
        mOffers = (TextViewDemi) baseview.findViewById(R.id.tv_offers);
        mNavigate = (TextViewDemi) baseview.findViewById(R.id.tv_navigate);
        mPostAd = (TextViewDemi) baseview.findViewById(R.id.tv_post_ad);
        mMyLists = (TextViewDemi) baseview.findViewById(R.id.tv_my_lists);
        mGroups = (TextViewDemi) baseview.findViewById(R.id.tv_groups);
        mActivity = (TextViewDemi) baseview.findViewById(R.id.tv_activity);
        mInviteFriends = (TextViewDemi) baseview.findViewById(R.id.tv_invite_friends);
        mInviteFbFriends = (TextViewDemi) baseview.findViewById(R.id.tv_invite_fb_friends);
        mSupport = (TextViewDemi) baseview.findViewById(R.id.tv_support);
        mFeedCount = (TextViewDemi) baseview.findViewById(R.id.feed_unread_count);
        mGroupCount = (TextViewDemi) baseview.findViewById(R.id.groups_count);
        mProfileLayout = baseview.findViewById(R.id.ly_profile);
        mProfilePic = (ImageView) baseview.findViewById(R.id.iv_profile_picture_drawer);
        mProfileMask = (ImageView) baseview.findViewById(R.id.iv_profile_mask_drawer);
        mLegals = (TextViewDemi) baseview.findViewById(R.id.tv_legal);

        if (PreferencesUtil.getParkToken(getActivity()) != null) {
            mLogin.setVisibility(View.GONE);
            getUserIcon();
            mProfileLayout.setVisibility(View.VISIBLE);
        } else {
            mLogin.setVisibility(View.VISIBLE);
            mProfileLayout.setVisibility(View.GONE);
        }

        if (DeviceUtils.isDeviceLollipopOrHigher()){
            mLeftMenu.setPadding(0, 0, 0, DeviceUtils.getNavigationBarHeight(getActivity()));
        }

        // Initial fragment:
        setSelectedRow(mNavigate);
        mLastTvSelected = mNavigate;
        ScreenManager.showCategoryItemsFragmentFromDrawer((BaseActivity) getActivity());

        if (ParkApplication.sJustLogged) {
            navigationFlow();
        }
        if (!ParkApplication.sJustLogged && ParkApplication.sJustLoggedSecondLevel) {
            navigationFlow2ndLevel();
        }

        OnClickListener listener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                handleMenuOptionSelected((LeftMenuOption) v.getTag());
            }
        };

        OnClickListener listenerUnlogged = new OnClickListener() {
            @Override
            public void onClick(View v) {
                LeftMenuOption opt = (LeftMenuOption) v.getTag();
                UnloggedNavigations option = null;

                switch (opt) {
                    case POST_AD:
                        option = UnloggedNavigations.POST_AD;
                        break;
                    case OFFERS:
                        option = UnloggedNavigations.OFFERS;
                        break;
                    case MY_LISTS:
                        option = UnloggedNavigations.MY_LISTS;
                        break;
                    case ACTIVITY:
                        option = UnloggedNavigations.ACTIVITY;
                        break;

                    default:
                        break;
                }
                MessageUtil.showLoginMsg(getActivity(), option);
            }
        };

        mLogin.setTag(LeftMenuOption.LOGIN);
        mLogin.setOnClickListener(listener);

        mProfileLayout.setTag(LeftMenuOption.PROFILE);
        mProfileLayout.setOnClickListener(listener);

        mNavigate.setTag(LeftMenuOption.CATEGORIES);
        mNavigate.setOnClickListener(listener);

        mGroups.setTag(LeftMenuOption.GROUPS);
        mGroups.setOnClickListener(listener);

        mInviteFriends.setTag(LeftMenuOption.INVITE_FRIENDS);
        mInviteFriends.setOnClickListener(listener);

        mInviteFbFriends.setTag(LeftMenuOption.INVITE_FB_FRIENDS);
        mInviteFbFriends.setOnClickListener(listener);

        mSupport.setTag(LeftMenuOption.SUPPORT);
        mSupport.setOnClickListener(listener);

        mLegals.setTag(LeftMenuOption.LEGALS);
        mLegals.setOnClickListener(listener);

        if (PreferencesUtil.getParkToken(getActivity()) != null) {

            mPostAd.setTag(LeftMenuOption.POST_AD);
            mPostAd.setOnClickListener(listener);

            mOffers.setTag(LeftMenuOption.OFFERS);
            mOffers.setOnClickListener(listener);

            mMyLists.setTag(LeftMenuOption.MY_LISTS);
            mMyLists.setOnClickListener(listener);

            mActivity.setTag(LeftMenuOption.ACTIVITY);
            mActivity.setOnClickListener(listener);

        } else {

            mPostAd.setTag(LeftMenuOption.POST_AD);
            mPostAd.setOnClickListener(listenerUnlogged);

            mOffers.setTag(LeftMenuOption.OFFERS);
            mOffers.setOnClickListener(listenerUnlogged);

            mMyLists.setTag(LeftMenuOption.MY_LISTS);
            mMyLists.setOnClickListener(listenerUnlogged);

            mActivity.setTag(LeftMenuOption.ACTIVITY);
            mActivity.setOnClickListener(listenerUnlogged);
        }

        return baseview;
    }

    private void getUserIcon(){
        String url = PreferencesUtil.getCurrentUserProfilePic(getActivity());
        if (url != null) {
            Picasso.with(getActivity()).load(url).resize(200, 200)
                    .centerCrop().into(mProfilePic);
        }
    }

    public void handleMenuOptionSelected(LeftMenuOption option) {
        ((ParkActivity) getActivity()).hideLeftmenu();

        switch (option) {
            case LOGIN:
                setSelectedRow(mLogin);
                ScreenManager.showLoginScreen((BaseActivity) getActivity());
                break;
            case PROFILE:
                setSelectedRow(mProfileLayout);
                mProfileMask.setImageDrawable(getResources().getDrawable(R.drawable.avatar_mask_over));
                mLastTvSelected = mProfileLayout;
                ScreenManager.showMyProfileFragmentFromDrawer((BaseActivity) getActivity(), false);
                break;

            case OFFERS:
                setSelectedRow(mOffers);
                mLastTvSelected = mOffers;
                ScreenManager.showOffersFragmentFromDrawer((BaseActivity) getActivity());
                break;

            case CATEGORIES:
                setSelectedRow(mNavigate);
                mLastTvSelected = mNavigate;
                ScreenManager.showCategoryItemsFragmentFromDrawer((BaseActivity) getActivity());
                break;

            case POST_AD:
                setSelectedRow(mPostAd);
                ParkApplication
                        .getInstance()
                        .getTracker(ParkApplication.TrackerName.GLOBAL_TRACKER)
                        .send(new HitBuilders.EventBuilder().setCategory("Clicks on Publish")
                                .setAction("Clicks on Publish").setLabel("The user entered to publish an item").build());
                ScreenManager.showCameraActivity((BaseActivity) getActivity());
                break;

            case MY_LISTS:
                setSelectedRow(mMyLists);
                mLastTvSelected = mMyLists;
                ScreenManager.showMyListsFragmentFromDrawer((BaseActivity) getActivity());
                break;

            case GROUPS:
                setSelectedRow(mGroups);
                mLastTvSelected = mGroups;
                ScreenManager.showUserGroupsFragmentFromDrawer((BaseActivity) getActivity());
                break;

            case ACTIVITY:
                setSelectedRow(mActivity);
                mLastTvSelected = mActivity;
                ScreenManager.showActivityFeedsFragmentFromDrawer((BaseActivity) getActivity());
//                mFeedCount.setVisibility(View.GONE);
                break;

            case INVITE_FRIENDS:
                startActivity(IntentFactory
                        .getShareWebAppIntent((BaseActivity) getActivity(), ParkUrls.GOOGLE_PLAY_URL));
                break;

            case INVITE_FB_FRIENDS:
                showFacebookInvitesDialog();
                break;

            case SUPPORT:
                startActivity(IntentFactory.getFeedbackEmailIntent((BaseActivity) getActivity()));
                break;

            case LEGALS:
                startActivity(IntentFactory.getLegalDisclosures());
                break;
        }
    }

    public interface OnLeftMenuInteractionListener {
    }

    private void getToBuyScreen(){
        setSelectedRow(mNavigate);
        mLastTvSelected = mNavigate;
        ScreenManager.showCategoryItemsFragmentFromDrawer((BaseActivity) getActivity());
    }

    private void setSelectedRow(View v) {
        mLogin.setSelected(false);
        mProfileLayout.setSelected(false);
        mOffers.setSelected(false);
        mNavigate.setSelected(false);
        mPostAd.setSelected(false);
        mMyLists.setSelected(false);
        mGroups.setSelected(false);
        mActivity.setSelected(false);
        mProfileMask.setImageDrawable(getResources().getDrawable(R.drawable.avatar_mask));
        v.setSelected(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        setSelectedRow(mLastTvSelected);
        getUserIcon();
        if (mLastTvSelected == mProfileLayout){
            mProfileMask.setImageDrawable(getResources().getDrawable(R.drawable.avatar_mask_over));
        }

        unReadCountersHandler();

        if (ParkApplication.sJustLogged) {
            ScreenManager.showMainScreen((BaseActivity) getActivity());
        }

        if (!TextUtils.isEmpty(CarouselCategoryFragment.categoryIdFromPush)){
            getToBuyScreen();
        }

    }

    public void updateProfileIconPic(){

        if (PreferencesUtil.getParkToken(getActivity()) != null) {
            mSpiceManager.executeCacheRequest(new ProfileRequest(ParkApplication.getInstance().getUsername()), new BaseNeckRequestListener<ProfileModel>() {
                @Override
                public void onRequestError(Error error) {
                }

                @Override
                public void onRequestSuccessfull(ProfileModel profile) {
                    if (profile.getProfilePicture()!=null && !profile.getProfilePicture().equals(PreferencesUtil.getCurrentUserProfilePic(getActivity()))){
                        ParkApplication.getInstance().setUserProfilePicture(profile.getProfilePicture());
                        getUserIcon();
                    }
                }

                @Override
                public void onRequestException(SpiceException exception) {
                }
            });
        }
    }

    public void unReadCountersHandler()
    {
        mSpiceManager.execute(new UnReadCountRequest(), new BaseNeckRequestListener<UnreadCountModel>() {

            @Override
            public void onRequestError(Error error) {
                mFeedCount.setVisibility(View.GONE);
                mGroupCount.setVisibility(View.GONE);
            }

            @Override
            public void onRequestSuccessfull(UnreadCountModel count) {
                if (count != null) {
                    if (count.getUnreadFeeds() > 0) {
                        ActivityFeedFragment.sUnreadCount = count.getUnreadFeeds();
                        if (count.getUnreadFeeds() <= 20) {
                            mFeedCount.setText(String.valueOf(count.getUnreadFeeds()));
                        } else {
                            mFeedCount.setText("20+");
                        }
                        mFeedCount.setVisibility(View.VISIBLE);
                    } else {
                        mFeedCount.setVisibility(View.GONE);
                    }

                    if (count.getUnreadGroupItems() > 0) {
                        if (count.getUnreadGroupItems() <= 20) {
                            mGroupCount.setText(String.valueOf(count.getUnreadGroupItems()));
                        } else {
                            mGroupCount.setText("20+");
                        }
                        mGroupCount.setVisibility(View.VISIBLE);
                    } else {
                        mGroupCount.setVisibility(View.GONE);
                    }
                } else {
                    mFeedCount.setVisibility(View.GONE);
                    mGroupCount.setVisibility(View.GONE);
                }
            }

            @Override
            public void onRequestException(SpiceException exception) {
                mFeedCount.setVisibility(View.GONE);
                mGroupCount.setVisibility(View.GONE);
            }
        });
    }

    private void navigationFlow() {

        ParkApplication.sJustLogged = false;

        if (ParkApplication.sFgmtOrAct_toGo != null) {
            switch (ParkApplication.sFgmtOrAct_toGo) {
                case POST_AD:
                    setSelectedRow(mPostAd);
                    ParkApplication
                            .getInstance()
                            .getTracker(ParkApplication.TrackerName.GLOBAL_TRACKER)
                            .send(new HitBuilders.EventBuilder().setCategory("Clicks on Publish")
                                    .setAction("Clicks on Publish").setLabel("The user entered to publish an item").build());
                    ScreenManager.showCameraActivity((BaseActivity) getActivity());

                    break;
                case CREATE_GROUP:
                    ScreenManager.showCreateGroupActivity((BaseActivity) getActivity(), -1);
                    break;
                case MY_LISTS:
                    setSelectedRow(mMyLists);
                    mLastTvSelected = mMyLists;
                    ScreenManager.showMyListsFragmentFromDrawer((BaseActivity) getActivity());
                    break;

                case GROUPS:
                    setSelectedRow(mGroups);
                    mLastTvSelected = mGroups;
                    ScreenManager.showUserGroupsFragmentFromDrawer((BaseActivity) getActivity());
                    break;

                case OFFERS:
                    setSelectedRow(mOffers);
                    mLastTvSelected = mOffers;
                    ScreenManager.showOffersFragmentFromDrawer((BaseActivity) getActivity());
                    break;

                case ACTIVITY:
                    setSelectedRow(mActivity);
                    mLastTvSelected = mActivity;
                    ScreenManager.showActivityFeedsFragmentFromDrawer((BaseActivity) getActivity());
                    break;

                default:
                    break;
            }
        }
    }

    private void navigationFlow2ndLevel() {

        ParkApplication.sJustLoggedSecondLevel = false;
        ParkApplication.sJustLoggedThirdLevel = false;

        if (ParkApplication.sFgmtOrAct_toGo != null) {
            switch (ParkApplication.sFgmtOrAct_toGo) {
                case CREATE_GROUP:
                    setSelectedRow(mGroups);
                    mLastTvSelected = mGroups;
                    ScreenManager.showUserGroupsFragmentFromDrawer((BaseActivity) getActivity());
                    break;

                default:
                    break;
            }
        }
    }


    private void showFacebookInvitesDialog()
    {
        Toast.makeText(getActivity(), getResources().getString(R.string.facebook_invite_initialize), Toast.LENGTH_SHORT).show();

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

}
