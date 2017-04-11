package com.ebay.park.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.ebay.park.ParkApplication;
import com.ebay.park.R;
import com.ebay.park.base.BaseActivity;
import com.ebay.park.base.BaseTabFragment;
import com.ebay.park.base.BaseTabPagerFragment.EditManager;
import com.ebay.park.flow.ScreenManager;
import com.ebay.park.utils.MessageUtil;
import com.google.android.gms.analytics.HitBuilders;

/**
 * Shows group detail screen.
 *
 * @author Nicol�s Mat�as Fern�ndez
 */
public class GroupDetailFragment extends BaseTabFragment implements EditManager {

    public static final String TEST_TAG = GroupDetailFragment.class.getSimpleName();
    public static final int REQUEST_EDIT_GROUP = 100;
    public static final int RESULT_DELETED = 101;
    public static final String ID_PARAM = "id";
    public static final String TAG = "SEARCHABLE";
    private static final int GROUP_INFO_POS = 0;
    private static final int GROUP_ITEMS_POS = 1;
    private static final int GROUP_USERS_POS = 2;
    private static final int TAB_COUNT = 3;
    private static final long NO_ID = -123L;
    public static boolean mIsOwner = true;
    private long mGroupId;
    private Fragment[] mContents;
    public boolean mCanEdit = false;
    private TabAdapter mTabAdapter;

    /**
     * Get the group detail for a given group id.
     *
     * @param id The id of the group.
     */
    public static GroupDetailFragment forGroup(long id) {
        GroupDetailFragment fragment = new GroupDetailFragment();
        Bundle args = new Bundle();
        args.putLong(ID_PARAM, id);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onViewCreated(View rootView, Bundle savedInstanceState) {
        mTabAdapter = new GroupPageAdapater(getChildFragmentManager());
        mPagerChangeListener = new ParkPageChangeListener();
        super.onViewCreated(rootView, savedInstanceState);
        if (ParkApplication.sGroupJustCreated){
            mViewPager.setCurrentItem(GROUP_INFO_POS);
            ParkApplication.sGroupJustCreated = false;
        } else {
            mViewPager.setCurrentItem(GROUP_ITEMS_POS);
        }
        setTitle(R.string.group_details);
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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            setHasOptionsMenu(true);
            if (getArguments() == null || !getArguments().containsKey(ID_PARAM)) {
                throw new IllegalStateException("Group detail fragment called with no id args.");
            }
            getBaseActivity().setTheme(R.style.LeftTabsTheme);
            mGroupId = getArguments().getLong(ID_PARAM, NO_ID);
            setupContents();

        } catch (IllegalStateException e) {
            getBaseActivity().finish();
        } catch (Exception e) {
            MessageUtil.showError(getBaseActivity(), getString(R.string.error_generic),
                    getBaseActivity().getCroutonsHolder());
            getBaseActivity().finish();
        }
    }

    private void setupContents() {
        mContents = new Fragment[TAB_COUNT];
        mContents[GROUP_INFO_POS] = GroupInfoFragment.forGroup(this, mGroupId);
        mContents[GROUP_ITEMS_POS] = ItemListFragment.forGroup(mGroupId);
        mContents[GROUP_USERS_POS] = UserSearchFragment.forGroup(mGroupId);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_group_list, menu);
    }

    @Override
    protected int getOffsetLimit() {
        return 2;
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (menu.findItem(R.id.edit) != null) {
            menu.findItem(R.id.edit).setVisible(mCanEdit);
        }
        if (menu.findItem(R.id.action_search) != null) {
            menu.findItem(R.id.action_search).setVisible(!mCanEdit);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem aItem) {
        switch (aItem.getItemId()) {
            case R.id.action_search:
                ScreenManager.showGlobalSearch(getBaseActivity());
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(aItem);
    }

    public void showSuscribersTab(){
        if (mViewPager !=null) {
            mViewPager.setCurrentItem(GROUP_USERS_POS);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_EDIT_GROUP && resultCode == RESULT_DELETED) {
            getBaseActivity().finish();
        }
    }

    @Override
    public void onBackPressed() {
    }

    private class GroupPageAdapater extends TabAdapter {

        public GroupPageAdapater(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int pos) {
            return mContents[pos];
        }

        @Override
        public CharSequence getPageTitle(int position) {
            String title = "";
            switch (position) {
                case GROUP_INFO_POS:
                    title = getString(R.string.group_tab_info);
                    break;
                case GROUP_ITEMS_POS:
                    title = getString(R.string.group_tab_items);
                    break;
                case GROUP_USERS_POS:
                    title = getString(R.string.group_tab_users);
                    break;
            }
            return title;
        }

        @Override
        public int getCount() {
            return mContents.length;
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        if (ParkApplication.sJustLoggedThirdLevel) {
            navigationFlow();
        }
    }

    private void navigationFlow() {
        ParkApplication.sJustLoggedSecondLevel = false;
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
                case CREATE_GROUP:
                    ScreenManager.showCreateGroupActivity(getBaseActivity(), -1);
                    break;
                default:
                    break;
            }
            ParkApplication.sFgmtOrAct_toGo = null;
        }
    }

    @Override
    public void onRefresh() {
    }

    @Override
    public void setCanEdit(boolean canEdit) {
        this.mCanEdit = canEdit;
        if (getBaseActivity() != null) {
            getBaseActivity().supportInvalidateOptionsMenu();
        }
    }

    protected class ParkPageChangeListener implements ViewPager.OnPageChangeListener {
        @Override
        public void onPageSelected(int position) {
            if(!mIsOwner) {
                if (mFabPublish != null && mFabPublish.getVisibility() != View.GONE){
                    if (mFabPublish.isHidden()){
                        mFabPublish.show(true);
                    }
                }
                if (position==0){
                    changeFabVisibility(false);
                } else {
                    changeFabVisibility(true);
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

    private void changeFabVisibility(boolean vis){
        if(mFabPublish != null){
            if (vis) {
                mFabPublish.show(true);
            } else {
                mFabPublish.hide(true);
            }
        }
    }
}