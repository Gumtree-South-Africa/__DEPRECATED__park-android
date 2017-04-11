package com.ebay.park.interfaces;

/**
 * Created by paula.baudo on 11/16/2015.
 */
public interface CameraOptionsListener {

    /**
     * Set photo as principal clicked
     */
    public void onSetPrincipalClick();

    /**
     * Move and scale option clicked
     */
    public void onCropClick();

    /**
     * Delete photo clicked
     */
    public void onDeleteClick();

    /**
     * Cancel clicked
     */
    public void onCancelClick();

}
