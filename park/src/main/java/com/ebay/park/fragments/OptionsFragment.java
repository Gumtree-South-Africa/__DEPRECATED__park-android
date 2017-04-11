package com.ebay.park.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ebay.park.ParkApplication;
import com.ebay.park.R;
import com.ebay.park.base.BaseActivity;
import com.ebay.park.base.BaseFragment;
import com.ebay.park.base.BaseSessionActivity;
import com.ebay.park.flow.IntentFactory;
import com.ebay.park.flow.ScreenManager;
import com.ebay.park.requests.ParkUrls;
import com.ebay.park.utils.FacebookUtil;
import com.ebay.park.utils.PreferencesUtil;
import com.ebay.park.views.TextViewBook;
import com.ebay.park.views.TextViewDemi;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

/**
 * A simple {@link Fragment} subclass.
 */
public class OptionsFragment extends BaseFragment {

    private TextViewBook mTvFindFbFriends;
    private TextViewBook mTvInviteFriends;
    private TextViewBook mTvEditProfile;
    private TextViewBook mTvChangePassword;
    private TextViewBook mTvLinkedAccounts;
    private TextViewBook mTvNotifications;
    private TextViewBook mTvTerms;
    private TextViewBook mTvAbout;
    private TextViewDemi mTvLogout;

    private Tracker mGoogleAnalyticsTracker;

    public OptionsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View baseView = inflater.inflate(R.layout.fragment_options,
                (ViewGroup) super.onCreateView(inflater, container, savedInstanceState), true);
        disableRefreshSwipe();
        return baseView;
    }

    @Override
    public void onViewCreated(View baseview, Bundle savedInstanceState) {
        super.onViewCreated(baseview, savedInstanceState);
        setTitle(R.string.preferences_title);

        mTvFindFbFriends = (TextViewBook) baseview.findViewById(R.id.tv_find_fb_friends);
        mTvFindFbFriends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showInviteFriendsScreen();
            }
        });

        mTvInviteFriends = (TextViewBook) baseview.findViewById(R.id.tv_invite_my_friends);
        mTvInviteFriends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(IntentFactory
                        .getShareWebAppIntent((BaseActivity) getActivity(), ParkUrls.GOOGLE_PLAY_URL));
            }
        });

        mTvEditProfile = (TextViewBook) baseview.findViewById(R.id.tv_edit_profile);
        mTvEditProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ScreenManager.showProfileEditFragment(getBaseActivity(), false);
            }
        });

        mTvChangePassword = (TextViewBook) baseview.findViewById(R.id.tv_change_pass);
        mTvChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ScreenManager.showChangePassword((BaseActivity) getActivity());
            }
        });
        mTvChangePassword.setEnabled(PreferencesUtil.getHasEmail(getBaseActivity()));

        mTvLinkedAccounts = (TextViewBook) baseview.findViewById(R.id.tv_linked_accounts);
        mTvLinkedAccounts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ScreenManager.showSocialNetworksConfigActivity((BaseActivity) getActivity());
            }
        });

        mTvNotifications = (TextViewBook) baseview.findViewById(R.id.tv_push_notifications);
        mTvNotifications.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ScreenManager.showConfigScreen((BaseActivity) getActivity());
            }
        });

        mTvTerms = (TextViewBook) baseview.findViewById(R.id.tv_terms_conditions);
        mTvTerms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(IntentFactory.getTermsConditions());
            }
        });

        mTvAbout = (TextViewBook) baseview.findViewById(R.id.tv_about_viva);
        mTvAbout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(IntentFactory.getAboutIntent());
            }
        });

        mTvLogout = (TextViewDemi) baseview.findViewById(R.id.tv_logout);
        mTvLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mGoogleAnalyticsTracker.send(new HitBuilders.EventBuilder()
                        .setCategory("User action")
                        .setAction("Did begin logout")
                        .setLabel("The user tapped on logout option").build());
                if (getActivity() != null && getActivity() instanceof BaseSessionActivity) {
                    ((BaseSessionActivity) getActivity()).logout(false);
                }
            }
        });
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mGoogleAnalyticsTracker = ParkApplication.getInstance().getTracker(ParkApplication.TrackerName.GLOBAL_TRACKER);
    }

    @Override
    public void onResume() {
        super.onResume();
        mTvChangePassword.setEnabled(PreferencesUtil.getHasEmail(getBaseActivity()));
    }

    @Override
    public void onRefresh() {
    }

    @Override
    public void onBackPressed() {
    }

    private void showInviteFriendsScreen () {
        ParkApplication
                    .getInstance()
                    .getTracker(ParkApplication.TrackerName.GLOBAL_TRACKER)
                    .send(new HitBuilders.EventBuilder().setCategory("Clicks on Find FB Friends")
                            .setAction("Clicks on Find FB Friends").setLabel("The user entered to find FB Friends")
                            .build());

        ParkApplication.getInstance().getEventsLogger().logEvent(FacebookUtil.EVENT_FIND_FB_FRIENDS_OPENED);

        ScreenManager.showFindFbFriendsActivity(getBaseActivity());
    }
}
