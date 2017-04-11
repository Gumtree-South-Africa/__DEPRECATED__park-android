package com.ebay.park.fragments;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.ebay.park.R;
import com.ebay.park.interfaces.PhotoSourceOptionsListener;
import com.ebay.park.views.TextViewBook;

/**
 * Created by paula.baudo on 3/8/2016.
 */
public class PhotoSourceDialogFragment extends DialogFragment {

    public final static String TAG = PhotoSourceDialogFragment.class.getSimpleName();

    /**
     * Create a new instance of PhotoSourceDialogFragment.
     */
    static PhotoSourceDialogFragment newInstance() {
        PhotoSourceDialogFragment f = new PhotoSourceDialogFragment();
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
        View v = inflater.inflate(R.layout.dialog_add_photo_origin, container, false);

        LinearLayout lyCamera = (LinearLayout) v.findViewById(R.id.ly_camera);
        lyCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((PhotoSourceOptionsListener) getTargetFragment()).onCameraClick();
                dismiss();
            }
        });

        LinearLayout lyGallery = (LinearLayout) v.findViewById(R.id.ly_gallery);
        lyGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((PhotoSourceOptionsListener) getTargetFragment()).onGalleryClick();
                dismiss();
            }
        });

        TextViewBook tvCancel = (TextViewBook) v.findViewById(R.id.tv_option_cancel);
        tvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((PhotoSourceOptionsListener) getTargetFragment()).onCancelClick();
                dismiss();
            }
        });

        getDialog().setCanceledOnTouchOutside(false);

        return v;
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        ((PhotoSourceOptionsListener) getTargetFragment()).onCancelClick();
    }
}
