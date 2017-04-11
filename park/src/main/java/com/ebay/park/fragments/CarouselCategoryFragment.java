package com.ebay.park.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.ebay.park.ParkApplication;
import com.ebay.park.R;
import com.ebay.park.base.BaseActivity;
import com.ebay.park.base.BaseTabFragment;
import com.ebay.park.flow.ScreenManager;
import com.ebay.park.model.CategoryModel;
import com.ebay.park.requests.ItemCategoryRequest;
import com.ebay.park.responses.ItemCategoryResponse;
import com.ebay.park.responses.ResponseCodes;
import com.ebay.park.utils.FacebookUtil;
import com.ebay.park.utils.MessageUtil;
import com.ebay.park.utils.PreferencesUtil;
import com.ebay.park.utils.SwrveEvents;
import com.facebook.share.model.AppInviteContent;
import com.facebook.share.widget.AppInviteDialog;
import com.globant.roboneck.common.NeckSpiceManager;
import com.globant.roboneck.requests.BaseNeckRequestException;
import com.globant.roboneck.requests.BaseNeckRequestListener;
import com.google.android.gms.analytics.HitBuilders;
import com.octo.android.robospice.persistence.exception.CacheCreationException;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.swrve.sdk.SwrveSDK;
import com.swrve.sdk.messaging.ISwrveCustomButtonListener;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Created by paula.baudo on 10/6/2016.
 */

public class CarouselCategoryFragment extends BaseTabFragment {

    public static final int LOOPS = 10;
    private List<CategoryModel> mCategoryList;
    private CategoriesAdapter mTabAdapter;
    private ProgressBar mProgressBar;
    public static int sTabPosition = -1;
    public static boolean showFindFbFriendsInAppMsg = false;
    public static String categoryIdFromPush ="";
    private static String lastTrackedCategory;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mCategoryList != null && mCategoryList.isEmpty()) {
            mSpiceManager.addListenerIfPending(new ItemCategoryRequest(), new CategoriesListener());
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.menu_category_navigation, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_negotiations:
                showOffersScreen();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onViewCreated(View rootView, @Nullable Bundle savedInstanceState) {
        setTitle(R.string.buy_items_navdrawer_title);
        mCategoryList = new ArrayList<>();
        mTabAdapter = new CategoriesAdapter(getChildFragmentManager());

        disableRefreshSwipe();
        mViewPager = (ViewPager) rootView.findViewById(R.id.pager);
        mViewPager.setAdapter(getAdapter());
        mTabLayout = (TabLayout) rootView.findViewById(R.id.tl_tabs);
        mTabLayout.setupWithViewPager(mViewPager);
        mProgressBar = (ProgressBar) rootView.findViewById(R.id.progress_bar);
        mPagerChangeListener = new CarouselPageChangeListener();

        mTabLayout.setVisibility(View.INVISIBLE);
        mViewPager.setVisibility(View.INVISIBLE);
        setUpFab(rootView);
        showPostAdButton();
        initSwrveCustomAction();
    }

    @Override
    public void onResume() {
        super.onResume();
        getCategoryTab();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        showProgressBar();
        onRefresh();
    }

    @Override
    public void onRefresh() {
        try {
            ItemCategoryRequest request = new ItemCategoryRequest();
            if (NeckSpiceManager.isDeviceOnline(getBaseActivity())) {
                if (!TextUtils.isEmpty(ParkApplication.getInstance().getSessionToken())) {
                    mSpiceManager.executeCacheRequest(request, new CategoriesListener());
                } else {
                    mSpiceManager.execute(request, new CategoriesListener());
                }
            } else {
                if (isRequestCached(request)){
                    mSpiceManager.getResultFromCache(request, new CategoriesListener());
                }else{
                    hideProgress();
                    if (getBaseActivity() != null) {
                        MessageUtil.showError(getBaseActivity(), getString(R.string.error_no_internet),
                                getBaseActivity().getCroutonsHolder());
                    }
                }
            }
        } catch (Exception e) {
        }
    }

    @Override
    protected TabAdapter getAdapter() {
        return mTabAdapter;
    }

    @Override
    protected void releaseResources() {
    }

    @Override
    public void onBackPressed() {
    }

    @Override
    public void onPause() {
        super.onPause();
        sTabPosition = -1;
    }

    @Override
    protected void initializeCustomTabs() {
        for (int i = 0; i < mTabLayout.getTabCount(); i++) {
            TabLayout.Tab tab = mTabLayout.getTabAt(i);
            tab.setCustomView(getAdapter().getTabView(i));
            tab.getCustomView().setPaddingRelative(80, 0, 80, 0);
        }
        mTabLayout.getTabAt(mCategoryList.size()*LOOPS/2).getCustomView().setSelected(true);
    }

    @Override
    protected int getOffsetLimit() {
        return 2;
    }

    private class CategoriesListener extends BaseNeckRequestListener<ItemCategoryResponse> {

        @Override
        public void onRequestError(BaseNeckRequestException.Error error) {
            hideProgressBar();
            if (error.getErrorCode() != ResponseCodes.Signout.APP_DEPRECATED) {
                MessageUtil.showError(getBaseActivity(), error.getMessage(),
                        getBaseActivity().getCroutonsHolder());
            }
        }

        @Override
        public void onRequestSuccessfull(ItemCategoryResponse response) {
            hideProgressBar();
            if (getActivity() != null){
                mCategoryList = response.getCategories();
                mViewPager.getAdapter().notifyDataSetChanged();
                mViewPager.setCurrentItem(mCategoryList.size()*LOOPS/2, true);
                setUpTabs();

                getCategoryTab();

                if (!ParkApplication.sJustLogged && ParkApplication.sNavegarTab_toGo != -1 && ParkApplication.sJustLoggedFromNavegar){
                    ParkApplication.sJustLoggedFromNavegar = false;
                }
                mTabLayout.setVisibility(View.VISIBLE);
                mViewPager.setVisibility(View.VISIBLE);
                trackCategorySelected(mCategoryList.get(mViewPager.getCurrentItem() % mCategoryList.size()).getName());
            }
        }

        @Override
        public void onRequestException(SpiceException exception) {
            hideProgressBar();
            MessageUtil.showError(getBaseActivity(), exception.getMessage(),
                    getBaseActivity().getCroutonsHolder());
        }

        @Override
        public void onRequestNotFound() {
            ItemCategoryRequest request = new ItemCategoryRequest();
            if (isRequestCached(request) || (getBaseActivity() != null && NeckSpiceManager.isDeviceOnline(getBaseActivity()))){
                mSpiceManager.getResultFromCache(request, new CategoriesListener());
            }else{
                hideProgressBar();
                MessageUtil.showError(getBaseActivity(), getString(R.string.error_no_internet),
                        getBaseActivity().getCroutonsHolder());
            }
        }
    }

    private void getCategoryTab() {
        if(!TextUtils.isEmpty(categoryIdFromPush)){
            long catId = Long.valueOf(categoryIdFromPush).longValue();
            for (int i = 0; i < mCategoryList.size(); i++) {
                if(catId == mCategoryList.get(i).getId()) {
                    ParkApplication.sNavegarTab_toGo = i;
                    categoryIdFromPush = "";
                }
            }
        }

        if (!ParkApplication.sJustLogged && ParkApplication.sNavegarTab_toGo != -1 && !ParkApplication.sJustLoggedFromNavegar){
            mViewPager.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if(ParkApplication.sNavegarTab_toGo != -1){
                        mViewPager.setCurrentItem(mCategoryList.size()*LOOPS/2
                                + ParkApplication.sNavegarTab_toGo, false);
                        ParkApplication.sNavegarTab_toGo = -1;
                    }
                }
            }, 100);
        }
    }

    protected void showProgressBar(){
        mProgressBar.setVisibility(View.VISIBLE);
    }

    protected void hideProgressBar(){
        mProgressBar.setVisibility(View.GONE);
    }

    private boolean isRequestCached(ItemCategoryRequest request){
        boolean isCached = false;
        try {
            isCached = mSpiceManager.isDataInCache(request.getResultType(), request.getCachekey(), request.getCacheExpirationTime()).get();
        } catch (CacheCreationException e) {
        } catch (InterruptedException e) {
        } catch (NullPointerException e) {
        } catch (ExecutionException e) {
        }
        return isCached;
    }

    protected void showPostAdButton() {
        mFabPublish.setVisibility(View.VISIBLE);
    }

    private void trackCategorySelected(String category){

        if(!category.equals(lastTrackedCategory)){

            lastTrackedCategory = category;

            ParkApplication
                    .getInstance()
                    .getTracker(ParkApplication.TrackerName.GLOBAL_TRACKER)
                    .send(new HitBuilders.EventBuilder().setCategory(String.format("Buy screen %s",category))
                            .setAction("Category selected on Buy screen").setLabel(String.format("Buy screen %s",category))
                            .build());

            String mFormattedCategory = StringUtils.stripAccents(category);
            mFormattedCategory = mFormattedCategory.replace(" ","_").toLowerCase();
            mFormattedCategory = String.format(FacebookUtil.EVENT_CATEGORY_SELECTED, mFormattedCategory);
            ParkApplication.getInstance().getEventsLogger().logEvent(mFormattedCategory);
        }
    }

    private void initSwrveCustomAction() {
        SwrveSDK.setCustomButtonListener(new ISwrveCustomButtonListener() {
            @Override
            public void onAction(String customAction) {
                if (customAction.equals("invite_friends")){
                    showFacebookInvitesDialog();
                } else if (customAction.equals("post_ad")) {
                    showPostAdScreen();
                } else if (customAction.equals("find_friends")) {
                    showInviteFriendsScreen();
                }
            }
        });

        if(showFindFbFriendsInAppMsg){
            SwrveSDK.event(SwrveEvents.PUSH_NOTIF_TO_FIND_FB_FRIENDS_RECEIVED);
            showFindFbFriendsInAppMsg = false;
        }
    }

    private void showOffersScreen () {
        if (PreferencesUtil.getParkToken(getActivity()) != null) {
            LeftMenuFragment.LeftMenuOption option = LeftMenuFragment.LeftMenuOption.OFFERS;
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
        } else {
            MessageUtil.showLoginMsg(getActivity(), ParkApplication.UnloggedNavigations.OFFERS);
        }
    }

    private void showFacebookInvitesDialog() {
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

    private void showPostAdScreen () {
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

    private void showInviteFriendsScreen () {
        if (PreferencesUtil.getParkToken(getActivity()) != null) {
            ParkApplication
                    .getInstance()
                    .getTracker(ParkApplication.TrackerName.GLOBAL_TRACKER)
                    .send(new HitBuilders.EventBuilder().setCategory("Clicks on Find FB Friends")
                            .setAction("Clicks on Find FB Friends").setLabel("The user entered to find FB Friends")
                            .build());

            ParkApplication.getInstance().getEventsLogger().logEvent(FacebookUtil.EVENT_FIND_FB_FRIENDS_OPENED);

            ScreenManager.showFindFbFriendsActivity(getBaseActivity());
        } else {
            MessageUtil.showLoginMsg(getActivity(), ParkApplication.UnloggedNavigations.PREFERENCES);
        }
    }

    private class CategoriesAdapter extends TabAdapter {

        public CategoriesAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return ItemListFragment.forCategory(mCategoryList.get(position % mCategoryList.size()));
        }

        @Override
        public int getCount() {
            return mCategoryList.size()*LOOPS;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mCategoryList.get(position % mCategoryList.size()).getName();
        }
    }

    protected class CarouselPageChangeListener extends ParkPageChangeListener {

        @Override
        public void onPageSelected(int position) {
            super.onPageSelected(position);
            sTabPosition = position;
        }

        @Override
        public void onPageScrolled(int position, float arg1, int arg2) {
            super.onPageScrolled(position, arg1, arg2);
            trackCategorySelected(mCategoryList.get(position % mCategoryList.size()).getName());
        }
    }
}
