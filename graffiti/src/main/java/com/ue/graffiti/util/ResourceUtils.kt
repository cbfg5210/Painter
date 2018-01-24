package com.ue.graffiti.util

import android.content.Context

/**
 * Created by hawk on 2018/1/17.
 */

object ResourceUtils {
    fun getImageArray(context: Context, arrayId: Int): IntArray {
        val ta = context.resources.obtainTypedArray(arrayId)
        val imageArray = IntArray(ta.length())
        for (i in imageArray.indices) {
            imageArray[i] = ta.getResourceId(i, 0)
        }
        ta.recycle()
        return imageArray
    }
}
