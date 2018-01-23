package com.ue.graffiti.util

import android.content.Context
import android.content.res.TypedArray

/**
 * Created by hawk on 2018/1/17.
 */

object ResourceUtils {

    fun getImageArray(context: Context, arrayId: Int): IntArray {
        val ta = context.resources.obtainTypedArray(arrayId)
        val imageArray = IntArray(ta.length())
        var i = 0
        val len = ta.length()
        while (i < len) {
            imageArray[i] = ta.getResourceId(i, 0)
            i++
        }
        ta.recycle()
        return imageArray
    }
}
