package com.ebay.park.utils;

import android.content.Context;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

/**
 * Created by paula.baudo on 6/7/2016.
 */
public class KeyboardHelper {

    public static InputFilter EMOJI_FILTER = new InputFilter() {

        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            for (int index = start; index < end; index++) {

                int type = Character.getType(source.charAt(index));

                if (type == Character.SURROGATE || type == Character.OTHER_SYMBOL) {
                    return "";
                }
            }
            return null;
        }
    };

    public static void hide (Context context, View focusedView){
        if (context != null && focusedView != null){
            InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(focusedView.getWindowToken(), 0);
        }
    }

    public static void show (Context context, View view){
        final InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(view, 0);
    }

    public static void show (Context context, View view, int flag){
        final InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(view, flag);
    }

}
