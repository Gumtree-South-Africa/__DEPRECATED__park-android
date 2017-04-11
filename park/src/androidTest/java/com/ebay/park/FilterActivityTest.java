package com.ebay.park;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.test.ActivityInstrumentationTestCase2;
import android.view.View;

import com.ebay.park.activities.FilterActivity;
import com.ebay.park.fragments.FilterFragment;
import com.ebay.park.fragments.GlobalSearchFragment;
import com.ebay.park.fragments.GroupFilterFragment;
import com.ebay.park.fragments.UserFilterFragment;

import org.junit.Before;
import org.junit.Test;

/**
 * Created by paula.baudo on 8/14/2015.
 */
public class FilterActivityTest extends ActivityInstrumentationTestCase2<FilterActivity> {

    private FilterActivity mActivity;
    private Intent mActivityIntent;

    public FilterActivityTest() {
        super(FilterActivity.class);
    }

    @Before
    protected void setUp(){
        mActivityIntent = createActivityIntent();
        setActivityIntent(mActivityIntent);
        mActivity = getActivity();
    }

    /*
        This test must be executed with USERS_POS and GROUP_POS too
     */
    private Intent createActivityIntent(){
        Intent intent = new Intent();
        intent.putExtra(FilterActivity.EXTRA_FILTER_ID, GlobalSearchFragment.ITEMS_POS);
        return intent;
    }

    @Test
    public void testActivityLaunchedProperly(){
        assertNotNull("Activity is not null", getActivity());
    }

    /*
        Menu is at fragment level
     */
    @Test
    public void testOkActionButton(){
        if (mActivityIntent.getIntExtra(FilterActivity.EXTRA_FILTER_ID, -1) == GlobalSearchFragment.ITEMS_POS){
            final View menu = getActivity().findViewById(R.id.action_ok);
            assertNotNull("Ok action button was not null on Items", menu);
        } else {
            if (mActivityIntent.getIntExtra(FilterActivity.EXTRA_FILTER_ID, -1) == GlobalSearchFragment.USERS_POS){
                final View menu = getActivity().findViewById(R.id.action_ok);
                assertNotNull("Ok action button was not null on Users", menu);
            } else {
                final View menu = getActivity().findViewById(R.id.action_ok);
                assertNotNull("Ok action button was not null on Groups", menu);
            }
        }
    }

    private Fragment startFragment() {
        getInstrumentation().waitForIdleSync();
        Fragment frag;
        if (mActivityIntent.getIntExtra(FilterActivity.EXTRA_FILTER_ID, -1) == GlobalSearchFragment.ITEMS_POS){
            frag = mActivity.getSupportFragmentManager().findFragmentByTag(FilterFragment.TAG);
        } else {
            if (mActivityIntent.getIntExtra(FilterActivity.EXTRA_FILTER_ID, -1) == GlobalSearchFragment.USERS_POS){
                frag = mActivity.getSupportFragmentManager().findFragmentByTag(UserFilterFragment.TAG);
            } else {
                frag = mActivity.getSupportFragmentManager().findFragmentByTag(GroupFilterFragment.TAG);
            }
        }
        return frag;
    }

    @Test
    public void testCorrectFilterFragment() {
        Fragment frag = startFragment();
        assertNotNull("The Correct Filter Fragment was not null",frag);
    }

}
