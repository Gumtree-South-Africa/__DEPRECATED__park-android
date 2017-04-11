package com.ebay.park.interfaces;

/**
 * Created by paula.baudo on 3/8/2016.
 */
public interface PhotoSourceOptionsListener {

    /**
     * Camera source clicked
     */
    public void onCameraClick();

    /**
     * Gallery source clicked
     */
    public void onGalleryClick();

    /**
     * Dialog canceled
     */
    public void onCancelClick();
}
