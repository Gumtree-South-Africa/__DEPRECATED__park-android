package com.ebay.park.activities;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.ViewGroup;

import com.ebay.park.R;
import com.ebay.park.base.BaseSessionActivity;
import com.ebay.park.flow.ScreenManager;
import com.ebay.park.fragments.ChangePassFragment;
import com.ebay.park.utils.DeviceUtils;
import com.ebay.park.utils.KeyboardHelper;

public class OptionsActivity extends BaseSessionActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

        if (savedInstanceState == null){
            ScreenManager.showOptionsFragment(this);
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
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        boolean fragmentBack = getSupportFragmentManager().getBackStackEntryCount() > 0;
        KeyboardHelper.hide(this,getCurrentFocus());
        if (fragmentBack) {
            getSupportFragmentManager().popBackStack();
            return true;
        } else {
            onBackPressed();
            return true;
        }
    }

    @Override
    public void onBackPressed() {
        if (ChangePassFragment.processingPassChange == null || (ChangePassFragment.processingPassChange!=null && !ChangePassFragment.processingPassChange)){
            super.onBackPressed();
        }
    }

}
