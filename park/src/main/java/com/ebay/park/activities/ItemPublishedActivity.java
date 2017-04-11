package com.ebay.park.activities;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.ViewGroup;

import com.ebay.park.R;
import com.ebay.park.base.BaseSessionActivity;
import com.ebay.park.flow.ScreenManager;
import com.ebay.park.fragments.ItemPublishedFragment;
import com.ebay.park.utils.DeviceUtils;

public class ItemPublishedActivity extends BaseSessionActivity {

    public static final String EXTRA_ITEM_ID = "item_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_published);

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
            final long itemId = getIntent().getLongExtra(EXTRA_ITEM_ID, -1);
            if (itemId == -1) {
                throw new IllegalStateException("Item detail activity started with no item id");
            }
            ScreenManager.showItemPublishedFragment(this, itemId);
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
    public void onBackPressed() {
        ItemPublishedFragment fragment = (ItemPublishedFragment) getSupportFragmentManager().findFragmentById(
                R.id.container_published);
        fragment.onBackPressed();
    }

}
