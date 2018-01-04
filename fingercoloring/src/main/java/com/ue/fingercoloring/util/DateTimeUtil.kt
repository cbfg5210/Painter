package com.ue.fingercoloring.util

import java.util.*

/**
 * Created by Swifty.Wang on 2015/9/4.
 */
object DateTimeUtil {
    fun formatTimeStamp(l: Long): String {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = l
        return calendar.get(Calendar.YEAR).toString() + "/" + (calendar.get(Calendar.MONTH) + 1) + "/" + calendar.get(Calendar.DAY_OF_MONTH)
    }
}
