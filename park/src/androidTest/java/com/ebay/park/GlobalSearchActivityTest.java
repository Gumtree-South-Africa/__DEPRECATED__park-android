package com.ebay.park;

import android.support.v4.app.Fragment;
import android.test.ActivityInstrumentationTestCase2;
import android.view.View;

import com.ebay.park.activities.GlobalSearchActivity;
import com.ebay.park.fragments.GlobalSearchFragment;

import org.junit.Before;
import org.junit.Test;

/**
 * Created by paula.baudo on 8/13/2015.
 */
public class GlobalSearchActivityTest extends ActivityInstrumentationTestCase2<GlobalSearchActivity> {

    private GlobalSearchActivity mActivity;

    public GlobalSearchActivityTest() {
        super(GlobalSearchActivity.class);
    }

    @Before
    protected void setUp(){
        mActivity = getActivity();
    }

    @Test
    public void testActivityLaunchedProperly(){
        assertNotNull("Activity is not null", getActivity());
    }

    @Test
    public void testFilterActionButton(){
        final View menu = getActivity().findViewById(R.id.action_filter);
        assertNotNull("Filter action button was not null", menu);
        mActivity.runOnUiThread(new Runnable() {
            public void run() {
                menu.performClick();
            }
        });
    }

    @Test
    public void testPagerLoaded(){
        final View pager = getActivity().findViewById(R.id.pager);
        assertNotNull("Pager is not null", pager);
    }

    private Fragment startFragment() {
        getInstrumentation().waitForIdleSync();
        Fragment frag = mActivity.getSupportFragmentManager().findFragmentByTag(GlobalSearchFragment.TAG);
        return frag;
    }

    @Test
    public void testGlobalSearchFragment() {
        Fragment frag = startFragment();
        assertNotNull("Global Search Fragment was not null",frag);
    }

}
