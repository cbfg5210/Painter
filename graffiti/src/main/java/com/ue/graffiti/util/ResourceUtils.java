package com.ue.graffiti.util;

import android.content.Context;
import android.content.res.TypedArray;

/**
 * Created by hawk on 2018/1/17.
 */

public class ResourceUtils {

    public static int[] getImageArray(Context context, int arrayId) {
        TypedArray ta = context.getResources().obtainTypedArray(arrayId);
        int[] imageArray = new int[ta.length()];
        for (int i = 0, len = ta.length(); i < len; i++) {
            imageArray[i] = ta.getResourceId(i, 0);
        }
        ta.recycle();
        return imageArray;
    }
}
