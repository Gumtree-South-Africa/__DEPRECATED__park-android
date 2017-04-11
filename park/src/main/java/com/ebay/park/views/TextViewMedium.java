package com.ebay.park.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import com.ebay.park.utils.FontsUtil;

/**
 * Created by Paula on 22/02/2016.
 */
public class TextViewMedium extends TextView {

    public TextViewMedium(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.setTypeface(FontsUtil.getFSMedium(context));
    }
}
