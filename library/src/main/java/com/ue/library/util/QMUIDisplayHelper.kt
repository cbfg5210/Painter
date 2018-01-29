package com.ue.library.util

import android.content.Context

/**
 * @author cginechen
 * @date 2016-03-17
 */
object QMUIDisplayHelper {

    fun dpToPx(context: Context, dpValue: Int): Int {
        return (context.resources.displayMetrics.density * dpValue + 0.5f).toInt()
    }

    /**
     * 单位转换: dp -> px
     *
     * @param dp
     * @return
     */
    fun dp2px(context: Context, dp: Int): Int {
        return (context.resources.displayMetrics.density * dp + 0.5).toInt()
    }
}
