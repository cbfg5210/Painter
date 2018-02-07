package com.ue.pixel.shape

import android.support.v4.graphics.ColorUtils
import com.ue.pixel.widget.PixelCanvasView
import java.util.*

/**
 * Created by BennyKok on 10/12/2016.
 */

class RectShape : BaseShape() {
    private var previousPxer = ArrayList<PixelCanvasView.Pixel>()

    override fun onDraw(pixelCanvasView: PixelCanvasView, startX: Int, startY: Int, endX: Int, endY: Int): Boolean {
        if (!super.onDraw(pixelCanvasView, startX, startY, endX, endY)) return true

        val layerToDraw = pixelCanvasView.pixelCanvasLayers[pixelCanvasView.currentLayer].bitmap

        previousPxer.indices
                .map { previousPxer[it] }
                .forEach { layerToDraw.setPixel(it.x, it.y, it.color) }

        previousPxer.clear()

        val rectWidth = Math.abs(startX - endX)
        val rectHeight = Math.abs(startY - endY)

        for (i in 0 until rectWidth + 1) {
            val mX = startX + i * if (endX - startX < 0) -1 else 1

            previousPxer.add(PixelCanvasView.Pixel(mX, startY, layerToDraw.getPixel(mX, startY)))
            previousPxer.add(PixelCanvasView.Pixel(mX, endY, layerToDraw.getPixel(mX, endY)))

            layerToDraw.setPixel(mX, startY, ColorUtils.compositeColors(pixelCanvasView.selectedColor, layerToDraw.getPixel(mX, startY)))
            layerToDraw.setPixel(mX, endY, ColorUtils.compositeColors(pixelCanvasView.selectedColor, layerToDraw.getPixel(mX, endY)))
        }
        for (i in 1 until rectHeight) {
            val mY = startY + i * if (endY - startY < 0) -1 else 1

            previousPxer.add(PixelCanvasView.Pixel(startX, mY, layerToDraw.getPixel(startX, mY)))
            previousPxer.add(PixelCanvasView.Pixel(endX, mY, layerToDraw.getPixel(endX, mY)))
            layerToDraw.setPixel(startX, mY, ColorUtils.compositeColors(pixelCanvasView.selectedColor, layerToDraw.getPixel(startX, mY)))
            layerToDraw.setPixel(endX, mY, ColorUtils.compositeColors(pixelCanvasView.selectedColor, layerToDraw.getPixel(endX, mY)))
        }

        pixelCanvasView.invalidate()
        return true
    }

    override fun onDrawEnd(pixelCanvasView: PixelCanvasView) {
        super.onDrawEnd(pixelCanvasView)
        if (previousPxer.isEmpty()) return
        pixelCanvasView.currentHistory.addAll(previousPxer)
        previousPxer.clear()

        pixelCanvasView.setUnrecordedChanges(true)
        pixelCanvasView.finishAddHistory()
    }
}