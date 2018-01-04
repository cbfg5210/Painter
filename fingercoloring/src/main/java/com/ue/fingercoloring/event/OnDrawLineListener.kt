package com.ue.fingercoloring.event

/**
 * Created by Swifty.Wang on 2015/9/9.
 */
interface OnDrawLineListener {
    fun OnDrawFinishedListener(drawed: Boolean, startX: Int, startY: Int, endX: Int, endY: Int)

    fun OnGivenFirstPointListener(startX: Int, startY: Int)

    fun OnGivenNextPointListener(endX: Int, endY: Int)
}
