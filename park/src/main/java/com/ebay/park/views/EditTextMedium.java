package com.ebay.park.views;

import android.content.Context;
import android.text.InputFilter;
import android.util.AttributeSet;
import android.widget.EditText;

import com.ebay.park.utils.FontsUtil;
import com.ebay.park.utils.KeyboardHelper;

/**
 * Created by paula.baudo on 3/16/2016.
 */
public class EditTextMedium extends EditText {

    public EditTextMedium(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.setTypeface(FontsUtil.getFSMedium(context));

        // Create a new array of filters based in the filters before to keep them (i.e. max length)
        InputFilter[] editFilters = this.getFilters();
        InputFilter[] newFilters = new InputFilter[editFilters.length + 1];
        System.arraycopy(editFilters, 0, newFilters, 0, editFilters.length);
        newFilters[editFilters.length] = KeyboardHelper.EMOJI_FILTER;
        this.setFilters(newFilters);
    }
}
