package com.ebay.park.activities;

import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.DismissOverlayView;

import com.ebay.park.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

public class MapActivity extends WearableActivity implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener {

    public static final String LATITUDE = "lat";
    public static final String LONGITUDE = "long";
    private static final float MAP_ZOOM = 12;
    private static final int MAP_ANIMATION_DURATION = 2000;

    private GoogleMap mMap;
    private MapFragment mMapFragment;
    private DismissOverlayView mDissmissOverlay;
    private LatLng mCoordinates;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        mDissmissOverlay = (DismissOverlayView) findViewById(R.id.dismiss_overlay);
        mDissmissOverlay.showIntroIfNecessary();

        if (getIntent().getExtras()!=null){
            mCoordinates = new LatLng(getIntent().getExtras().getDouble(LATITUDE),
                    getIntent().getExtras().getDouble(LONGITUDE));
        }

        setAmbientEnabled();

        mMapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        mMapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        CameraPosition cameraPosition = CameraPosition.builder().target(mCoordinates).zoom(MAP_ZOOM).build();
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition),MAP_ANIMATION_DURATION,null);
        mMap.setOnMapLongClickListener(this);
    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        mDissmissOverlay.show();
    }
}
