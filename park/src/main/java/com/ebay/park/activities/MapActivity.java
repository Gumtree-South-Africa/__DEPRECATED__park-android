package com.ebay.park.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.ViewGroup;

import com.ebay.park.R;
import com.ebay.park.base.BaseActivity;
import com.ebay.park.utils.DeviceUtils;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

public class MapActivity extends BaseActivity implements OnMapReadyCallback {

    public static final String MARKER_LATITUDE = "latitude";
    public static final String MARKER_LONGITUDE = "longitude";
    public static final String MARKER_LOCATION_NAME = "location_name";
    private static final float MAP_ZOOM = 12;
    private static final int MAP_ANIMATION_DURATION = 2000;
    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

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

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        setTitle(getIntent().getStringExtra(MARKER_LOCATION_NAME));
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        UiSettings uiSettings = googleMap.getUiSettings();
        uiSettings.setCompassEnabled(true);
        googleMap.setMyLocationEnabled(true);
        Intent mapIntent = getIntent();
        LatLng marker = new LatLng(mapIntent.getDoubleExtra(MARKER_LATITUDE, 0), mapIntent.getDoubleExtra(MARKER_LONGITUDE,0));
        CameraPosition cameraPosition = CameraPosition.builder().target(marker).zoom(MAP_ZOOM).build();
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition),MAP_ANIMATION_DURATION,null);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mMap != null){
            mMap.setMyLocationEnabled(false);
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
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home){
            this.finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
