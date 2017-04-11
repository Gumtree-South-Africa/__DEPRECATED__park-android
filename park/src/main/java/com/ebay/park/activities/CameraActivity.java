package com.ebay.park.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.ViewGroup;

import com.ebay.park.R;
import com.ebay.park.base.BaseActivity;
import com.ebay.park.base.BaseFragment;
import com.ebay.park.flow.ScreenManager;
import com.ebay.park.fragments.CameraFragment;
import com.ebay.park.interfaces.OnShowListenerLollipop;
import com.ebay.park.interfaces.OnShowListenerPreLollipop;
import com.ebay.park.utils.DeviceUtils;
import com.ebay.park.utils.DialogUtils;

public class CameraActivity extends BaseActivity {

    public static final String ARG = "IMAGE_PATHS";
    public static final String ARG_NOT_PUBLISHED = "IMAGE_PATHS_NOT_PUBLISHED";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        if (DeviceUtils.isDeviceLollipopOrHigher()) {
            Toolbar parkToolbar = (Toolbar) findViewById(R.id.park_toolbar);
            parkToolbar.setPadding(0, DeviceUtils.getStatusBarHeight(this), 0, 0);
            setSupportActionBar(parkToolbar);
        } else {
            getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            getSupportActionBar().setCustomView(R.layout.actionbar);
        }
        getSupportActionBar().hide();

        if (savedInstanceState == null){
            if (getIntent().hasExtra(ARG)){
                ScreenManager.showCameraFragmentEdition(this, getIntent().getStringArrayExtra(ARG));
            } else if (getIntent().hasExtra(ARG_NOT_PUBLISHED)){
                ScreenManager.showCameraFragmentCreation(this, getIntent().getStringArrayExtra(ARG_NOT_PUBLISHED));
            } else {
                ScreenManager.showCameraFragment(this);
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
        BaseFragment fragment = (BaseFragment) getSupportFragmentManager().findFragmentById(R.id.camera_container);
        fragment.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onBackPressed() {
        CameraFragment fragment = (CameraFragment) getSupportFragmentManager().findFragmentById(
                R.id.camera_container);
        fragment.onBackPressed();
        showConfirmCancelPublishDialog();
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
