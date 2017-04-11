package com.ebay.park;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.test.ActivityInstrumentationTestCase2;

import com.ebay.park.activities.ItemDetailActivity;
import com.ebay.park.fragments.ItemDetailFragment;

import org.junit.Before;
import org.junit.Test;

/**
 * Created by paula.baudo on 8/14/2015.
 */
public class ItemDetailActivityTest extends ActivityInstrumentationTestCase2<ItemDetailActivity> {

    private static final long ITEM_ID_TEST = 1510l;
    private ItemDetailActivity mActivity;
    private Intent mActivityIntent;

    public ItemDetailActivityTest() {
        super(ItemDetailActivity.class);
    }

    @Before
    protected void setUp(){
        mActivityIntent = createActivityIntent();
        setActivityIntent(mActivityIntent);
        mActivity = getActivity();
    }

    private Intent createActivityIntent() {
        Intent intent = new Intent();
        intent.putExtra(ItemDetailActivity.EXTRA_ITEM_ID, ITEM_ID_TEST);
        intent.putExtra(ItemDetailActivity.EXTRA_SHOW_COMMENTS, true);
        return intent;
    }

    @Test
    public void testActivityLaunchedProperly(){
        assertNotNull("Activity is not null", getActivity());
    }

    private Fragment startFragment(Fragment fragment){
        FragmentTransaction transaction = mActivity.getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, fragment, ItemDetailFragment.TEST_TAG);
        transaction.commit();
        getInstrumentation().waitForIdleSync();
        Fragment frag = mActivity.getSupportFragmentManager().findFragmentByTag(ItemDetailFragment.TEST_TAG);
        return frag;
    }

    @Test
    public void testFragmentLoadedProperly(){
        ItemDetailFragment itemDetailFragment = new ItemDetailFragment();
        Bundle itemDetailArguments = new Bundle();
        itemDetailArguments.putLong(ItemDetailFragment.ID_PARAM, ITEM_ID_TEST);
        itemDetailFragment.setArguments(itemDetailArguments);
        Fragment frag = startFragment(itemDetailFragment);
        assertNotNull("Item Detail Fragment was not null", frag);
    }
}
