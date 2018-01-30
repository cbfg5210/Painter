package com.ue.pixel.shape

import android.support.v4.graphics.ColorUtils
import com.ue.pixel.widget.PxerView
import java.util.*

/**
 * Created by BennyKok on 10/12/2016.
 */

class RectShape : BaseShape() {
    private var previousPxer = ArrayList<PxerView.Pxer>()

    override fun onDraw(pxerView: PxerView, startX: Int, startY: Int, endX: Int, endY: Int): Boolean {
        if (!super.onDraw(pxerView, startX, startY, endX, endY)) return true

        val layerToDraw = pxerView.pxerLayers[pxerView.currentLayer].bitmap

        previousPxer.indices
                .map { previousPxer[it] }
                .forEach { layerToDraw.setPixel(it.x, it.y, it.color) }

        previousPxer.clear()

        val rectWidth = Math.abs(startX - endX)
        val rectHeight = Math.abs(startY - endY)

        for (i in 0 until rectWidth + 1) {
            val mX = startX + i * if (endX - startX < 0) -1 else 1

            previousPxer.add(PxerView.Pxer(mX, startY, layerToDraw.getPixel(mX, startY)))
            previousPxer.add(PxerView.Pxer(mX, endY, layerToDraw.getPixel(mX, endY)))

            layerToDraw.setPixel(mX, startY, ColorUtils.compositeColors(pxerView.selectedColor, layerToDraw.getPixel(mX, startY)))
            layerToDraw.setPixel(mX, endY, ColorUtils.compositeColors(pxerView.selectedColor, layerToDraw.getPixel(mX, endY)))
        }
        for (i in 1 until rectHeight) {
            val mY = startY + i * if (endY - startY < 0) -1 else 1

            previousPxer.add(PxerView.Pxer(startX, mY, layerToDraw.getPixel(startX, mY)))
            previousPxer.add(PxerView.Pxer(endX, mY, layerToDraw.getPixel(endX, mY)))
            layerToDraw.setPixel(startX, mY, ColorUtils.compositeColors(pxerView.selectedColor, layerToDraw.getPixel(startX, mY)))
            layerToDraw.setPixel(endX, mY, ColorUtils.compositeColors(pxerView.selectedColor, layerToDraw.getPixel(endX, mY)))
        }

        pxerView.invalidate()
        return true
    }

    override fun onDrawEnd(pxerView: PxerView) {
        super.onDrawEnd(pxerView)
        if (previousPxer.isEmpty()) return
        pxerView.currentHistory.addAll(previousPxer)
        previousPxer.clear()

        pxerView.setUnrecordedChanges(true)
        pxerView.finishAddHistory()
    }
}