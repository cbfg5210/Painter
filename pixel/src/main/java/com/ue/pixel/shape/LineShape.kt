package com.ue.pixel.shape

import android.graphics.Color
import android.graphics.Paint
import android.support.v4.graphics.ColorUtils
import com.ue.pixel.widget.PxerView
import java.util.*

/**
 * Created by BennyKok on 10/12/2016.
 */

class LineShape : BaseShape() {
    private val p = Paint()
    private var hasInit: Boolean = false
    private val previousPxer = ArrayList<PxerView.Pxer>()

    init {
        p.style = Paint.Style.STROKE
        p.strokeWidth = 1f
    }

    override fun onDraw(pxerView: PxerView, startX: Int, startY: Int, endX: Int, endY: Int): Boolean {
        if (!super.onDraw(pxerView, startX, startY, endX, endY)) return true

        if (!hasInit) {
            p.color = Color.YELLOW
            pxerView.preview.eraseColor(Color.TRANSPARENT)
            pxerView.previewCanvas.setBitmap(pxerView.preview)

            hasInit = true
        }

        val layerToDraw = pxerView.pxerLayers[pxerView.currentLayer].bitmap
        previousPxer.indices
                .map { previousPxer[it] }
                .forEach { layerToDraw.setPixel(it.x, it.y, it.color) }

        previousPxer.clear()
        pxerView.preview.eraseColor(Color.TRANSPARENT)
        pxerView.previewCanvas.drawLine(startX.toFloat(), startY.toFloat(), endX.toFloat(), endY.toFloat(), p)

        for (i in 0 until pxerView.picWidth) {
            for (y in 0 until pxerView.picHeight) {
                var c = if (i == startX && y == startY || i == endX && y == endY) Color.YELLOW else pxerView.preview.getPixel(i, y)
                if (c == Color.YELLOW) {
                    previousPxer.add(PxerView.Pxer(i, y, layerToDraw.getPixel(i, y)))
                    layerToDraw.setPixel(i, y, ColorUtils.compositeColors(pxerView.selectedColor, layerToDraw.getPixel(i, y)))
                }
            }
        }
        pxerView.invalidate()
        return true
    }

    override fun onDrawEnd(pxerView: PxerView) {
        super.onDrawEnd(pxerView)

        hasInit = false
        if (previousPxer.isEmpty()) return
        pxerView.currentHistory.addAll(previousPxer)
        previousPxer.clear()

        pxerView.setUnrecordedChanges(true)
        pxerView.finishAddHistory()
    }
}