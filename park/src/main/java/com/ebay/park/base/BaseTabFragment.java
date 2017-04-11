package com.ebay.park.base;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ebay.park.ParkApplication;
import com.ebay.park.R;
import com.ebay.park.flow.ScreenManager;
import com.ebay.park.utils.MessageUtil;
import com.ebay.park.utils.PreferencesUtil;
import com.ebay.park.views.TextViewDemi;
import com.github.clans.fab.FloatingActionButton;
import com.google.android.gms.analytics.HitBuilders;

/**
 * Created by Jonatan on 22/9/2015.
 */
public abstract class BaseTabFragment extends BaseFragment {

    protected TabLayout mTabLayout;
    protected ViewPager mViewPager;
    protected FloatingActionButton mFabPublish;
    protected ViewPager.OnPageChangeListener mPagerChangeListener = new ParkPageChangeListener();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_tabs, (ViewGroup) super.onCreateView(inflater, container, savedInstanceState), true);
    }

    @Override
    public void onViewCreated(View rootView, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(rootView, savedInstanceState);
        disableRefreshSwipe();
        mViewPager = (ViewPager) rootView.findViewById(R.id.pager);
        mViewPager.setAdapter(getAdapter());
        mTabLayout = (TabLayout) rootView.findViewById(R.id.tl_tabs);
        mTabLayout.setupWithViewPager(mViewPager);

        setUpFab(rootView);
        initializeTabs();
    }

    protected void initializeTabs(){
        setUpTabs();
    }

    protected void setUpTabs() {
        mTabLayout.setSelectedTabIndicatorColor(getDefaultColor());
		mTabLayout.setSelectedTabIndicatorHeight(9);
        mViewPager.setOffscreenPageLimit(getOffsetLimit());
        mViewPager.addOnPageChangeListener(mPagerChangeListener);
        initializeCustomTabs();
    }

    protected void initializeCustomTabs() {
        for (int i = 0; i < mTabLayout.getTabCount(); i++) {
            TabLayout.Tab tab = mTabLayout.getTabAt(i);
            tab.setCustomView(getAdapter().getTabView(i));
        }
        mTabLayout.getTabAt(0).getCustomView().setSelected(true);
    }

    protected void setUpCenteredTabs(){
        mTabLayout.setTabMode(TabLayout.MODE_FIXED);
        mTabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
    }

    public void setUpFab(View aView) {
        mFabPublish = (FloatingActionButton) aView.findViewById(R.id.fab_publish);
        if (mFabPublish != null) {
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

    protected abstract TabAdapter getAdapter();

    @Override
    public void onDestroy() {
        super.onDestroy();
        releaseResources();
    }

    protected abstract void releaseResources();

    protected void drawBoldTitle(int position, boolean isBold){
        drawBoldTitle(mTabLayout.getTabAt(position), isBold);
    }

    protected void drawBoldTitle(TabLayout.Tab tab, boolean isBold){
        TextViewDemi title = (TextViewDemi)tab.getCustomView().findViewById(R.id.title);
        if (isBold){
            title.setCompoundDrawablesWithIntrinsicBounds(R.drawable.tab_layout_dot_selector, 0, 0, 0);
        }else{
            title.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        }
    }

    private int getDefaultColor() {
        return getResources().getColor(R.color.White);
    }

    protected abstract class TabAdapter extends FragmentStatePagerAdapter {

        public TabAdapter(FragmentManager fm) {
            super(fm);
        }

        public View getTabView(int position) {
            View aView = View.inflate(getActivity(), R.layout.tab_item, null);
            ((TextViewDemi)aView.findViewById(R.id.title)).setText(getPageTitle(position));
            return aView;
        }

    }

    @Override
    public void onRefresh() {
    }

    protected int getOffsetLimit() {
        return 1;
    }

    protected class ParkPageChangeListener implements ViewPager.OnPageChangeListener {
        @Override
        public void onPageSelected(int position) {
            if (mFabPublish != null && mFabPublish.getVisibility() != View.GONE){
                if (mFabPublish.isHidden()){
                    mFabPublish.show(true);
                }
            }
        }

        @Override
        public void onPageScrolled(int position, float arg1, int arg2) {
        }

        @Override
        public void onPageScrollStateChanged(int position) {
        }
    }

    protected void showPostAdButton() {
        mFabPublish.setVisibility(View.VISIBLE);
    }

    public void showCreateGroupButton() {
        mFabPublish.setImageDrawable(getResources().getDrawable(R.drawable.floating_action_button_group_selector));
        mFabPublish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (PreferencesUtil.getParkToken(getActivity()) != null) {
                    ScreenManager.showCreateGroupActivity(getBaseActivity(), -1);
                } else {
                    MessageUtil.showLoginMsg(getActivity(), ParkApplication.UnloggedNavigations.CREATE_GROUP);
                }
            }
        });
        mFabPublish.setShadowYOffset(getResources().getDimension(R.dimen.fab_shadow_group));
        mFabPublish.setShadowXOffset(getResources().getDimension(R.dimen.fab_shadow_group));
        mFabPublish.setVisibility(View.VISIBLE);
    }
}
