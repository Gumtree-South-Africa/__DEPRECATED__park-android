package com.ebay.park.base;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.ViewGroup;

import com.ebay.park.ParkApplication;
import com.ebay.park.R;
import com.ebay.park.activities.ParkActivity;
import com.ebay.park.activities.SplashActivity;
import com.ebay.park.fragments.CarouselCategoryFragment;
import com.ebay.park.utils.DeviceUtils;
import com.ebay.park.views.TextViewDemi;
import com.facebook.appevents.AppEventsLogger;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.swrve.sdk.SwrveSDK;

import bolts.AppLinks;
import de.keyboardsurfer.android.widget.crouton.Crouton;

public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            super.onCreate(savedInstanceState);
        }catch(Exception e){
            this.finish();
        }
        ParkApplication.getInstance().getTracker(ParkApplication.TrackerName.GLOBAL_TRACKER);
        AppLinks.getTargetUrlFromInboundIntent(this, getIntent());

        if(!getClass().equals(SplashActivity.class)){
            SwrveSDK.onCreate(this);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        GoogleAnalytics.getInstance(this).reportActivityStart(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        GoogleAnalytics.getInstance(this).reportActivityStop(this);
    }

    @Override
    protected void onDestroy() {
        Crouton.cancelAllCroutons();
        SwrveSDK.onDestroy(this);
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        AppEventsLogger.deactivateApp(this);
        SwrveSDK.onPause();
    }

    public void onResume() {
        super.onResume();
        ParkApplication.sCurrentContext = this;
        AppEventsLogger.activateApp(this);
        SwrveSDK.onResume(this);
        if(!getClass().equals(ParkActivity.class) && !TextUtils.isEmpty(CarouselCategoryFragment.categoryIdFromPush)){
            CarouselCategoryFragment.categoryIdFromPush = "";
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        SwrveSDK.onNewIntent(intent);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        SwrveSDK.onLowMemory();
    }

    @Override
    public void setTitle(int titleId) {
        if (DeviceUtils.isDeviceLollipopOrHigher()) {
            TextViewDemi toolbarTitle = (TextViewDemi) findViewById(R.id.toolbar_title);
            if (toolbarTitle!=null) {
                toolbarTitle.setText(getString(titleId));
                super.setTitle("");
            }
        } else {
            TextViewDemi actionbarTitle = (TextViewDemi) findViewById(R.id.actionbar_title);
            if (actionbarTitle!=null) {
                actionbarTitle.setText(getString(titleId));
                super.setTitle("");
            }
        }
    }

    @Override
    public void setTitle(CharSequence title) {
        if (DeviceUtils.isDeviceLollipopOrHigher()) {
            TextViewDemi toolbarTitle = (TextViewDemi) findViewById(R.id.toolbar_title);
            toolbarTitle.setText(title);
            super.setTitle("");
        } else {
            TextViewDemi actionbarTitle = (TextViewDemi) findViewById(R.id.actionbar_title);
            actionbarTitle.setText(title);
            super.setTitle("");
        }
    }

    public abstract ViewGroup getCroutonsHolder();
}
