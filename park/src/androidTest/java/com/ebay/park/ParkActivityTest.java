package com.ebay.park;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.test.ActivityInstrumentationTestCase2;
import android.view.View;

import com.ebay.park.activities.ParkActivity;
import com.ebay.park.fragments.ActivityFeedFragment;
import com.ebay.park.fragments.LoginFragment;
import com.ebay.park.fragments.MyGroupsFragment;
import com.ebay.park.fragments.MyListsTab;
import com.ebay.park.fragments.OffersTabsFragment;
import com.ebay.park.fragments.ProfileItemListFragment;

import org.junit.Before;
import org.junit.Test;

/**
 * Created by paula.baudo on 8/10/2015.
 */
public class ParkActivityTest extends ActivityInstrumentationTestCase2<ParkActivity> {

    private static final String USERNAME_TO_TEST = "pvbaudo";
    private ParkActivity mActivity;

    public ParkActivityTest() {
        super(ParkActivity.class);
    }

    @Before
    protected void setUp(){
        mActivity = getActivity();
    }

    @Test
     public void testGlobalSearchActionButton(){
        final View menu = getActivity().findViewById(R.id.action_search);
        assertNotNull("Menu item was not null", menu);
//        mActivity.runOnUiThread(new Runnable() {
//            public void run() {
//                menu.performClick();
//            }
//        });
    }

    @Test
    public void testDrawer(){
        final View drawer = getActivity().findViewById(R.id.main_drawer);
        assertNotNull("Drawer was not null", drawer);
    }

    private Fragment startMyProfileFragment(Fragment fragment){
        FragmentTransaction transaction = mActivity.getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, fragment, ProfileItemListFragment.TEST_TAG);
        transaction.commit();
        getInstrumentation().waitForIdleSync();
        Fragment frag = mActivity.getSupportFragmentManager().findFragmentByTag(ProfileItemListFragment.TEST_TAG);
        return frag;
    }

//    @Test
//    public void testMyProfileFragment(){
//        ProfileFragment profileFragment = new ProfileFragment();
//        Bundle profileArguments = new Bundle();
//        profileArguments.putString(ProfileFragment.USERNAME_PARAM, USERNAME_TO_TEST);
//        profileArguments.putBoolean(ProfileFragment.FROMDRAWER_PARAM, true);
//        profileFragment.setArguments(profileArguments);
//        Fragment frag = startMyProfileFragment(profileFragment);
//        assertNotNull("My Profile Fragment was not null", frag);
//    }

    private Fragment startOffersFragment(Fragment fragment){
        FragmentTransaction transaction = mActivity.getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, fragment, OffersTabsFragment.TEST_TAG);
        transaction.commit();
        getInstrumentation().waitForIdleSync();
        Fragment frag = mActivity.getSupportFragmentManager().findFragmentByTag(OffersTabsFragment.TEST_TAG);
        return frag;
    }

    @Test
    public void testOffersFragment(){
        OffersTabsFragment offersTabsFragment = new OffersTabsFragment();
        Fragment frag = startOffersFragment(offersTabsFragment);
        assertNotNull("Offers Fragment was not null", frag);
    }

//    private Fragment startItemCategoryFragment(Fragment fragment){
//        FragmentTransaction transaction = mActivity.getSupportFragmentManager().beginTransaction();
//        transaction.replace(R.id.container, fragment, ItemCategoryFragment.TEST_TAG);
//        transaction.commit();
//        getInstrumentation().waitForIdleSync();
//        Fragment frag = mActivity.getSupportFragmentManager().findFragmentByTag(ItemCategoryFragment.TEST_TAG);
//        return frag;
//    }

//    @Test
//    public void testItemCategoryFragment(){
//        ItemCategoryFragment itemCategoryFragment = new ItemCategoryFragment();
//        Fragment frag = startItemCategoryFragment(itemCategoryFragment);
//        assertNotNull("Item Category Fragment was not null", frag);
//    }

    private Fragment startMyListsFragment(Fragment fragment){
        FragmentTransaction transaction = mActivity.getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, fragment, MyListsTab.TEST_TAG);
        transaction.commit();
        getInstrumentation().waitForIdleSync();
        Fragment frag = mActivity.getSupportFragmentManager().findFragmentByTag(MyListsTab.TEST_TAG);
        return frag;
    }

    @Test
    public void testMyListsFragment(){
        MyListsTab myListsTab = new MyListsTab();
        Fragment frag = startMyListsFragment(myListsTab);
        assertNotNull("My Lists Fragment was not null", frag);
    }

    private Fragment startGroupsFragment(Fragment fragment){
        FragmentTransaction transaction = mActivity.getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, fragment, MyGroupsFragment.TEST_TAG);
        transaction.commit();
        getInstrumentation().waitForIdleSync();
        Fragment frag = mActivity.getSupportFragmentManager().findFragmentByTag(MyGroupsFragment.TEST_TAG);
        return frag;
    }

    @Test
    public void testGroupsFragment(){
        MyGroupsFragment myGroupsFragment = new MyGroupsFragment();
        Fragment frag = startGroupsFragment(myGroupsFragment);
        assertNotNull("Groups Fragment was not null", frag);
    }

    private Fragment startFeedFragment(Fragment fragment){
        FragmentTransaction transaction = mActivity.getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, fragment, ActivityFeedFragment.TEST_TAG);
        transaction.commit();
        getInstrumentation().waitForIdleSync();
        Fragment frag = mActivity.getSupportFragmentManager().findFragmentByTag(ActivityFeedFragment.TEST_TAG);
        return frag;
    }

    @Test
    public void testFeedFragment(){
        ActivityFeedFragment activityFeedFragment = new ActivityFeedFragment();
        Fragment frag = startFeedFragment(activityFeedFragment);
        assertNotNull("Feeds Fragment was not null", frag);
    }

    private Fragment startLoginFragment(Fragment fragment){
        FragmentTransaction transaction = mActivity.getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, fragment, LoginFragment.TEST_TAG);
        transaction.commit();
        getInstrumentation().waitForIdleSync();
        Fragment frag = mActivity.getSupportFragmentManager().findFragmentByTag(LoginFragment.TEST_TAG);
        return frag;
    }

    @Test
    public void testLoginFragment(){
        LoginFragment loginFragment = new LoginFragment();
        Fragment frag = startLoginFragment(loginFragment);
        assertNotNull("Login Fragment was not null", frag);
    }
}
