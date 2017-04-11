package com.ebay.park.interfaces;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import com.ebay.park.R;

/**
 * Created by paula.baudo on 5/27/2016.
 */
public class OnShowListenerPreLollipop implements DialogInterface.OnShowListener {

    private AlertDialog mAlertDialog;
    private Context mContext;

    public OnShowListenerPreLollipop (AlertDialog dialog, Context ctx){
        mAlertDialog = dialog;
        mContext = ctx;
    }

    @Override
    public void onShow(DialogInterface arg0) {
        mAlertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(
                mContext.getResources().getColor(R.color.NewParkColor));
        mAlertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextSize(12);
        mAlertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setAllCaps(true);
        mAlertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(
                mContext.getResources().getColor(R.color.NewParkColor));
        mAlertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextSize(12);
        mAlertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setAllCaps(true);
    }
}
