package com.ebay.park.activities;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
import android.widget.ImageView;

import com.ebay.park.R;

public class ImageActivity extends Activity {

    public static final String IMAGE_BITMAP = "image_bitmap";
    private ImageView mExpandedImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);

        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                mExpandedImage = (ImageView) stub.findViewById(R.id.photo);

                if (getIntent().getExtras()!=null && getIntent().getExtras().containsKey(IMAGE_BITMAP)){
                    mExpandedImage.setImageBitmap((Bitmap) getIntent().getExtras().getParcelable(IMAGE_BITMAP));
                }
            }
        });
    }
}
