package com.ebay.park.interfaces;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import com.ebay.park.R;

/**
 * Created by paula.baudo on 5/27/2016.
 */    public class OnShowListenerLollipop implements DialogInterface.OnShowListener {

    private AlertDialog mAlertDialog;
    private Context mContext;

    public OnShowListenerLollipop (AlertDialog dialog, Context ctx){
        mAlertDialog = dialog;
        mContext = ctx;
    }

    @Override
    public void onShow(DialogInterface arg0) {
        mAlertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(
                mContext.getResources().getColor(R.color.IndicatorOrange));
        mAlertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(
                mContext.getResources().getColor(R.color.feed_filter_grey));
    }
}
