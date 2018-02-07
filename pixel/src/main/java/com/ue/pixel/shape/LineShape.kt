package com.ue.pixel.shape

import android.graphics.Color
import android.graphics.Paint
import android.support.v4.graphics.ColorUtils
import com.ue.pixel.widget.PixelCanvasView
import java.util.*

/**
 * Created by BennyKok on 10/12/2016.
 */

class LineShape : BaseShape() {
    private val p = Paint()
    private var hasInit: Boolean = false
    private val previousPxer = ArrayList<PixelCanvasView.Pixel>()

    init {
        p.style = Paint.Style.STROKE
        p.strokeWidth = 1f
    }

    override fun onDraw(pixelCanvasView: PixelCanvasView, startX: Int, startY: Int, endX: Int, endY: Int): Boolean {
        if (!super.onDraw(pixelCanvasView, startX, startY, endX, endY)) return true

        if (!hasInit) {
            p.color = Color.YELLOW
            pixelCanvasView.preview.eraseColor(Color.TRANSPARENT)
            pixelCanvasView.previewCanvas.setBitmap(pixelCanvasView.preview)

            hasInit = true
        }

        val layerToDraw = pixelCanvasView.pixelCanvasLayers[pixelCanvasView.currentLayer].bitmap
        previousPxer.indices
                .map { previousPxer[it] }
                .forEach { layerToDraw.setPixel(it.x, it.y, it.color) }

        previousPxer.clear()
        pixelCanvasView.preview.eraseColor(Color.TRANSPARENT)
        pixelCanvasView.previewCanvas.drawLine(startX.toFloat(), startY.toFloat(), endX.toFloat(), endY.toFloat(), p)

        for (i in 0 until pixelCanvasView.picWidth) {
            for (y in 0 until pixelCanvasView.picHeight) {
                var c = if (i == startX && y == startY || i == endX && y == endY) Color.YELLOW else pixelCanvasView.preview.getPixel(i, y)
                if (c == Color.YELLOW) {
                    previousPxer.add(PixelCanvasView.Pixel(i, y, layerToDraw.getPixel(i, y)))
                    layerToDraw.setPixel(i, y, ColorUtils.compositeColors(pixelCanvasView.selectedColor, layerToDraw.getPixel(i, y)))
                }
            }
        }
        pixelCanvasView.invalidate()
        return true
    }

    override fun onDrawEnd(pixelCanvasView: PixelCanvasView) {
        super.onDrawEnd(pixelCanvasView)

        hasInit = false
        if (previousPxer.isEmpty()) return
        pixelCanvasView.currentHistory.addAll(previousPxer)
        previousPxer.clear()

        pixelCanvasView.setUnrecordedChanges(true)
        pixelCanvasView.finishAddHistory()
    }
}