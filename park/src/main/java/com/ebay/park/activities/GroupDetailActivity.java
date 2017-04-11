package com.ebay.park.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.ViewGroup;

import com.ebay.park.R;
import com.ebay.park.base.BaseFragment;
import com.ebay.park.base.BaseSessionActivity;
import com.ebay.park.flow.ScreenManager;
import com.ebay.park.utils.DeviceUtils;
import com.ebay.park.utils.Logger;
import com.ebay.park.utils.MessageUtil;

/**
 * Group Detail Activity
 *
 * @author Nicol�s Mat�as Fern�ndez
 */
public class GroupDetailActivity extends BaseSessionActivity {

    public static final String EXTRA_GROUP_ID = "group_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            setContentView(R.layout.activity_container);

            if (DeviceUtils.isDeviceLollipopOrHigher()) {
                Toolbar parkToolbar = (Toolbar) findViewById(R.id.park_toolbar);
                parkToolbar.setPadding(0, DeviceUtils.getStatusBarHeight(this), 0, 0);
                parkToolbar.setNavigationIcon(R.drawable.icon_white_back);
                setSupportActionBar(parkToolbar);
            } else {
                getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
                getSupportActionBar().setCustomView(R.layout.actionbar);
                getSupportActionBar().setHomeAsUpIndicator(R.drawable.icon_white_back);
            }
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setElevation(0);

            if (savedInstanceState == null) {
                final long groupId = getIntent().getLongExtra(EXTRA_GROUP_ID, -1);
                if (groupId == -1) {
                    throw new IllegalStateException("Group detail activity started with no group id");
                }
                ScreenManager.showGroupDetailFragment(this, groupId);
            }

        } catch (IllegalStateException e) {
            Logger.error("Group detail activity started with no group id");
            finish();
        } catch (Exception e) {
            MessageUtil.showError(this, getString(R.string.error_generic), getCroutonsHolder());
            finish();
        }
    }

    @Override
    public ViewGroup getCroutonsHolder() {
        ViewGroup view = (ViewGroup) findViewById(R.id.crouton_handle);
        if (view != null) {
            view.setPadding(0, DeviceUtils.getStatusBarHeight(this), 0, 0);
        }
        return view;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        BaseFragment fragment = (BaseFragment) getSupportFragmentManager().findFragmentById(R.id.container);
        fragment.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

}
