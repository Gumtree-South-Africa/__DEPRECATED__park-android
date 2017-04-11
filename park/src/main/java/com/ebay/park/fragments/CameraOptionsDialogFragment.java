package com.ebay.park.fragments;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ebay.park.R;
import com.ebay.park.interfaces.CameraOptionsListener;

/**
 * Created by paula.baudo on 11/16/2015.
 */
public class CameraOptionsDialogFragment extends DialogFragment {

    public final static String TAG = CameraOptionsDialogFragment.class.getSimpleName();

    /**
     * Create a new instance of CameraOptionsDialogFragment.
     */
    static CameraOptionsDialogFragment newInstance() {
        CameraOptionsDialogFragment f = new CameraOptionsDialogFragment();
        return f;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NO_TITLE,0);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.dialog_camera_options, container, false);

        LinearLayout lyPrincipal = (LinearLayout) v.findViewById(R.id.ly_set_principal);
        lyPrincipal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((CameraOptionsListener) getTargetFragment()).onSetPrincipalClick();
                dismiss();
            }
        });

        LinearLayout lyCrop = (LinearLayout) v.findViewById(R.id.ly_crop);
        lyCrop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((CameraOptionsListener) getTargetFragment()).onCropClick();
                dismiss();
            }
        });

        LinearLayout lyDelete = (LinearLayout) v.findViewById(R.id.ly_delete);
        lyDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((CameraOptionsListener) getTargetFragment()).onDeleteClick();
                dismiss();
            }
        });

        TextView tvCancel = (TextView) v.findViewById(R.id.tv_option_cancel);
        tvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((CameraOptionsListener) getTargetFragment()).onCancelClick();
                dismiss();
            }
        });

        getDialog().setCanceledOnTouchOutside(false);

        return v;
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        ((CameraOptionsListener) getTargetFragment()).onCancelClick();
    }
}
