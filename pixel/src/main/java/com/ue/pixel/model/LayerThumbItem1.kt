package com.ue.pixel.model

import android.graphics.Bitmap

/**
 * Created by hawk on 2018/2/5.
 */
class LayerThumbItem1(var bitmap: Bitmap, var visible: Boolean) {
    var pressedTime = 0

    val isPressSecondTime: Boolean
        get() = pressedTime == 2

    fun pressed() {
        pressedTime++
        pressedTime = Math.min(2, pressedTime)
    }
}