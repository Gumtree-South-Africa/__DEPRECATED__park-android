package com.ebay.park.utils;

import android.content.Context;
import android.graphics.Typeface;

/**
 * Created by Paula on 22/02/2016.
 */
public class FontsUtil {

    public static Typeface getFSBook(Context context){
        return Typeface.createFromAsset(context.getAssets(), "fonts/foundry-sterling-book.ttf");
    }

    public static Typeface getFSDemi(Context context){
        return Typeface.createFromAsset(context.getAssets(), "fonts/foundry-sterling-demi.ttf");
    }

    public static Typeface getFSMedium(Context context){
        return Typeface.createFromAsset(context.getAssets(), "fonts/foundry-sterling-medium.ttf");
    }

}
