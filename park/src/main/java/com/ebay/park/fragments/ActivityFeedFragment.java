package com.ebay.park.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.View;

import com.ebay.park.R;
import com.ebay.park.base.BaseTabFragment;
import com.ebay.park.utils.PreferencesUtil;

public class ActivityFeedFragment extends BaseTabFragment {

    public static final String TEST_TAG = ActivityFeedFragment.class.getSimpleName();
    public static int sUnreadCount = 0;
    private TabAdapter mTabAdapter;

    @Override
    public void onViewCreated(View rootView, Bundle savedInstanceState) {
        setTitle(R.string.notifications_config);
        if (PreferencesUtil.getParkToken(getActivity()) != null) {
            mTabAdapter = new NotificationTabAdapter(getChildFragmentManager());
            super.onViewCreated(rootView, savedInstanceState);
            setUpCenteredTabs();
        } else {
            LeftMenuFragment.LeftMenuOption option = LeftMenuFragment.LeftMenuOption.CATEGORIES;
            try {
                LeftMenuFragment menuFragment = (LeftMenuFragment) getBaseActivity().getSupportFragmentManager().findFragmentById(
                        R.id.left_menu_fragment);
                if (menuFragment != null) {
                    menuFragment.handleMenuOptionSelected(option);
                }
            } catch (NullPointerException e) {
                e.printStackTrace();
            } catch (ClassCastException e){
                e.printStackTrace();
            }
        }
    }

    @Override
    protected TabAdapter getAdapter() {
        return mTabAdapter;
    }

    @Override
    protected void releaseResources() {
        mTabAdapter = null;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        disableRefreshSwipe();
    }

    @Override
    public void onBackPressed() {
    }

    private class NotificationTabAdapter extends TabAdapter {

        private static final int ALL_NOTIFICATIONS = 0;
        private static final int MY_NOTIFICATIONS = 1;

        public NotificationTabAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        @Override
        public Fragment getItem(int pos) {
            Fragment aFragment = null;
            switch (pos) {
                case ALL_NOTIFICATIONS:
                    aFragment = NotificationListFragment.allNotifications();
                    break;
                case MY_NOTIFICATIONS:
                    aFragment = NotificationListFragment.myNotifications();
                    break;
                default:
                    getActivity().onBackPressed();
                    break;
            }
            return aFragment;
        }

        @Override
        public int getCount() {
            return MY_NOTIFICATIONS + 1;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            String title = "";
            switch (position) {
                case ALL_NOTIFICATIONS:
                    title = getString(R.string.all_feed_filter);
                    break;
                case MY_NOTIFICATIONS:
                    title = getString(R.string.only_mine_filter);
                    break;
            }
            return title;
        }

    }


}

