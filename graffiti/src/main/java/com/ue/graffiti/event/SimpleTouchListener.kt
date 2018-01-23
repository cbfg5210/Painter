package com.ue.graffiti.event

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint

import com.ue.graffiti.model.Pel

/**
 * Created by hawk on 2018/1/19.
 */

interface SimpleTouchListener {
    fun isSensorRegistered(): Boolean
    fun getBackgroundBitmap(): Bitmap
    fun getCopyOfBackgroundBitmap(): Bitmap
    fun getCurrentPaint(): Paint
    fun getContext(): Context
    fun invalidate()
    fun setSelectedPel(pel: Pel?)
    fun updateSavedBitmap(canvas: Canvas, bitmap: Bitmap?, pelList: List<Pel>, selectedPel: Pel?, isInvalidate: Boolean)
}
