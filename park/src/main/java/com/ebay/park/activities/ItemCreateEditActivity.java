package com.ebay.park.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;

import com.ebay.park.R;
import com.ebay.park.base.BaseFragment;
import com.ebay.park.base.BaseSessionActivity;
import com.ebay.park.flow.ScreenManager;
import com.ebay.park.fragments.ItemCreateEditFragment;
import com.ebay.park.interfaces.OnShowListenerLollipop;
import com.ebay.park.interfaces.OnShowListenerPreLollipop;
import com.ebay.park.utils.DeviceUtils;
import com.ebay.park.utils.DialogUtils;

/**
 * Create edit Item Activity
 *
 * @author Nicolás Matias Fernández
 */

public class ItemCreateEditActivity extends BaseSessionActivity {

    public static final String EXTRA_ITEM_ID = "item_id";
    public static final String EXTRA_IMAGES_PATHS = "images_paths";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_create_edit);

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

        if (savedInstanceState == null) {
            final long itemId = getIntent().getLongExtra(EXTRA_ITEM_ID, -1);
            if (!getIntent().hasExtra(EXTRA_IMAGES_PATHS)) {
                ScreenManager.showPublishScreen(this, itemId);
            } else {
                ScreenManager.showPublishScreen(this, itemId, getIntent().getStringArrayExtra(EXTRA_IMAGES_PATHS));
            }
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
        BaseFragment fragment = (BaseFragment) getSupportFragmentManager().findFragmentById(R.id.container_publish);
        fragment.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                showConfirmCancelPublishDialog();
                return true;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (ItemCreateEditFragment.sShowPublishMenuItem != null && ItemCreateEditFragment.sShowPublishMenuItem) {
            showConfirmCancelPublishDialog();
        }
    }

    private void showConfirmCancelPublishDialog() {
        final AlertDialog dialog = DialogUtils.getDialogWithLabel(this, R.string.cancel,
                R.string.publish_cancel)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .setNegativeButton(R.string.no, null).create();

        if (DeviceUtils.isDeviceLollipopOrHigher()){
            dialog.setOnShowListener(new OnShowListenerLollipop(dialog, this));
        } else {
            dialog.setOnShowListener(new OnShowListenerPreLollipop(dialog, this));
        }

        dialog.show();
    }
}
