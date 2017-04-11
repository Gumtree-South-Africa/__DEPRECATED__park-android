package com.ebay.park.activities;

import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.ViewGroup;

import com.ebay.park.R;
import com.ebay.park.base.BaseSessionActivity;
import com.ebay.park.flow.IntentFactory;
import com.ebay.park.flow.ScreenManager;
import com.ebay.park.utils.DeviceUtils;
import com.ebay.park.utils.MessageUtil;

public class RateListActivity extends BaseSessionActivity {

    public static final String USERNAME = "username";
    public static final String PENDING_FEED = "pending_feed";

    private String mUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {

            if (!getIntent().hasExtra(USERNAME)) {
                throw new IllegalStateException("RateListActivity called with no username argument");
            }
            setContentView(R.layout.activity_container);

            if (DeviceUtils.isDeviceLollipopOrHigher()) {
                Toolbar parkToolbar = (Toolbar) findViewById(R.id.park_toolbar);
                parkToolbar.setNavigationIcon(R.drawable.icon_white_back);
                setSupportActionBar(parkToolbar);
            } else {
                getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
                getSupportActionBar().setCustomView(R.layout.actionbar);
                getSupportActionBar().setHomeAsUpIndicator(R.drawable.icon_white_back);
            }
            getSupportActionBar().setElevation(0);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

            if (savedInstanceState == null) {
                if (getIntent().hasExtra(USERNAME)) {
                    mUsername = getIntent().getStringExtra(USERNAME);
                    ScreenManager.showRatesTabFragment(this, mUsername, getIntent().getExtras().getBoolean(PENDING_FEED));
                } else {
                    finish();
                }
            }
        } catch (IllegalStateException e) {
            finish();
        } catch (Exception e) {
            MessageUtil.showError(this, getString(R.string.error_generic), getCroutonsHolder());
            finish();
        }
    }

    @Override
    public ViewGroup getCroutonsHolder() {
        return (ViewGroup) findViewById(R.id.crouton_handle);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            NavUtils.navigateUpTo(this, IntentFactory.getProfileIntent(this, mUsername));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
