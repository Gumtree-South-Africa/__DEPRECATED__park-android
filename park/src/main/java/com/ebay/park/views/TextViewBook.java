package com.ebay.park.views;

import android.content.Context;
import android.util.AttributeSet;

import com.ebay.park.utils.FontsUtil;

/**
 * Created by Paula on 22/02/2016.
 */
public class TextViewBook extends TextViewDemi {

    public TextViewBook(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.setTypeface(FontsUtil.getFSBook(context));
    }
}
