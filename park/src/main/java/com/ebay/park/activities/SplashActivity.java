package com.ebay.park.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ViewGroup;

import com.ebay.park.R;
import com.ebay.park.base.BaseActivity;
import com.ebay.park.flow.ScreenManager;
import com.ebay.park.utils.DeviceUtils;

import org.json.JSONObject;

import io.branch.referral.Branch;
import io.branch.referral.BranchError;


public class SplashActivity extends BaseActivity {

    // Set the duration of the splash screen
    private static final long SPLASH_SCREEN_DELAY = 3000;
    private BaseActivity mThisAct;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        mThisAct = this;

        if (DeviceUtils.isDeviceLollipopOrHigher()) {
            Toolbar parkToolbar = (Toolbar) findViewById(R.id.park_toolbar);
            parkToolbar.setPadding(0, DeviceUtils.getStatusBarHeight(this), 0, 0);
            setSupportActionBar(parkToolbar);
        } else {
            getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            getSupportActionBar().setCustomView(R.layout.actionbar);
        }
        getSupportActionBar().hide();
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
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
    public void onStart() {
        super.onStart();
        Branch branch = Branch.getInstance();

        branch.initSession(new Branch.BranchReferralInitListener() {
            @Override
            public void onInitFinished(JSONObject referringParams, BranchError error) {
                if (error == null) {
                    // params are the deep linked params associated with the link that the user clicked -> was re-directed to this app
                    // params will be empty if no data found
                    final String itemId = referringParams.optString("item_id", "");

                    if (itemId.equals("")) {
                        // NORMAL WAY, MAIN SCREEN
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                ScreenManager.showMainScreen(mThisAct);
                                finish();
                            }
                        }, SPLASH_SCREEN_DELAY);
                    } else {
                        // EXAMPLE TO GO TO A VIP SCREEN
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                ScreenManager.showItemDetailActivity(mThisAct, Long.valueOf(itemId), null, null);
                            }
                        }, SPLASH_SCREEN_DELAY);
                    }
                } else {
                    Log.i("Vivanuncios", error.getMessage());
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            ScreenManager.showMainScreen(mThisAct);
                            finish();
                        }
                    }, SPLASH_SCREEN_DELAY);
                }
            }
        }, this.getIntent().getData(), this);
    }

    @Override
    public void onNewIntent(Intent intent) {
        this.setIntent(intent);
    }

}