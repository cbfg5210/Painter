package com.ue.graffiti.event

/**
 * Created by hawk on 2018/1/15.
 */

interface BarSensorListener {
    fun isTopToolbarVisible(): Boolean
    fun openTools()
    fun closeTools()
}
