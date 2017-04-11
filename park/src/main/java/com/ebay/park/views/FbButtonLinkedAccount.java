package com.ebay.park.views;

import android.content.Context;
import android.util.AttributeSet;

import com.ebay.park.R;
import com.ebay.park.utils.FontsUtil;

/**
 * Created by paula.baudo on 3/22/2016.
 */
public class FbButtonLinkedAccount extends CustomFbLoginButton {

    public FbButtonLinkedAccount(Context context) {
        super(context);
    }

    public FbButtonLinkedAccount(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.setTypeface(FontsUtil.getFSBook(context));
    }

    public FbButtonLinkedAccount(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void setButtonText() {
        this.setText(getActivity().getString(R.string.facebook));
    }
}
