package com.ue.pixel.shape

import com.ue.pixel.widget.PixelCanvasView

/**
 * Created by BennyKok on 10/12/2016.
 */

abstract class BaseShape {
    private var hasEnded = false
    private var startX = -1
    private var startY = -1
    private var endX = -1
    private var endY = -1

    open fun onDraw(pixelCanvasView: PixelCanvasView, startX: Int, startY: Int, endX: Int, endY: Int): Boolean {
        if (this.startX == startX && this.startY == startY && this.endX == endX && this.endY == endY) return false
        this.startX = startX
        this.startY = startY
        this.endX = endX
        this.endY = endY
        return true
    }

    open fun onDrawEnd(pixelCanvasView: PixelCanvasView) {
        hasEnded = true
    }

    fun hasEnded(): Boolean {
        hasEnded = !hasEnded
        return !hasEnded
    }
}