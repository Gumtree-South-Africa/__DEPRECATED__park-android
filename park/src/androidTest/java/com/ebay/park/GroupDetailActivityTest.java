package com.ebay.park;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.test.ActivityInstrumentationTestCase2;

import com.ebay.park.activities.GroupDetailActivity;
import com.ebay.park.fragments.GroupDetailFragment;

import org.junit.Before;
import org.junit.Test;

/**
 * Created by paula.baudo on 8/14/2015.
 */
public class GroupDetailActivityTest extends ActivityInstrumentationTestCase2<GroupDetailActivity> {

    private static final long GROUP_ID_TEST = 321l;
    private Intent mActivityIntent;
    private GroupDetailActivity mActivity;
    
    public GroupDetailActivityTest() {
        super(GroupDetailActivity.class);
    }

    @Before
    protected void setUp(){
        mActivityIntent = createActivityIntent();
        setActivityIntent(mActivityIntent);
        mActivity = getActivity();
    }

    private Intent createActivityIntent() {
        Intent intent = new Intent();
        intent.putExtra(GroupDetailActivity.EXTRA_GROUP_ID, GROUP_ID_TEST);
        return intent;
    }

    @Test
    public void testActivityLaunchedProperly(){
        assertNotNull("Activity is not null", getActivity());
    }

    private Fragment startFragment(Fragment fragment){
        FragmentTransaction transaction = mActivity.getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, fragment, GroupDetailFragment.TEST_TAG);
        transaction.commit();
        getInstrumentation().waitForIdleSync();
        Fragment frag = mActivity.getSupportFragmentManager().findFragmentByTag(GroupDetailFragment.TEST_TAG);
        return frag;
    }

    @Test
    public void testFragmentLoadedProperly(){
        GroupDetailFragment groupDetailFragment = new GroupDetailFragment();
        Bundle groupDetailArguments = new Bundle();
        groupDetailArguments.putLong(GroupDetailFragment.ID_PARAM, GROUP_ID_TEST);
        groupDetailFragment.setArguments(groupDetailArguments);
        Fragment frag = startFragment(groupDetailFragment);
        assertNotNull("Group Detail Fragment was not null", frag);
    }
}
