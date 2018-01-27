package com.ue.painter.util

import java.util.*

/**
 * Created by Swifty.Wang on 2015/9/4.
 */
object DateTimeUtil {
    fun formatTimeStamp(l: Long): String {
        val calendar = Calendar.getInstance().apply { timeInMillis = l }
        return "${calendar.get(Calendar.YEAR)}/${(calendar.get(Calendar.MONTH) + 1)}/${calendar.get(Calendar.DAY_OF_MONTH)}"
    }
}
