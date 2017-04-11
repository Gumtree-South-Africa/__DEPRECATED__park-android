package com.ebay.park.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Button;

import com.ebay.park.utils.FontsUtil;

/**
 * Created by Paula on 22/02/2016.
 */
public class ButtonDemi extends Button {

    public ButtonDemi(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.setTypeface(FontsUtil.getFSDemi(context));
    }
}
