package com.ue.pixel.shape

import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import com.ue.pixel.widget.PixelCanvasView
import java.util.*

/**
 * Created by BennyKok on 10/15/2016.
 */

class EraserShape : BaseShape() {
    private val p = Paint()
    private var hasInit = false
    private val previousPxer = ArrayList<PixelCanvasView.Pixel>()
    private lateinit var path: Path

    init {
        p.style = Paint.Style.STROKE
        p.strokeWidth = 3f
    }

    override fun onDraw(pixelCanvasView: PixelCanvasView, startX: Int, startY: Int, endX: Int, endY: Int): Boolean {
        if (!super.onDraw(pixelCanvasView, startX, startY, endX, endY)) return true
        if (!hasInit) {
            path = Path()
            path.moveTo(startX.toFloat(), startY.toFloat())
            p.color = Color.YELLOW
            pixelCanvasView.preview.eraseColor(Color.TRANSPARENT)
            pixelCanvasView.previewCanvas.setBitmap(pixelCanvasView.preview)

            hasInit = true
        }

        val layerToDraw = pixelCanvasView.pixelCanvasLayers[pixelCanvasView.currentLayer].bitmap
        path.lineTo(endX.toFloat(), endY.toFloat())
        pixelCanvasView.previewCanvas.drawPath(path, p)

        for (i in 0 until pixelCanvasView.picWidth) {
            for (y in 0 until pixelCanvasView.picHeight) {
                val c = pixelCanvasView.preview.getPixel(i, y)
                if (c != Color.TRANSPARENT) {
                    val history = PixelCanvasView.Pixel(i, y, layerToDraw.getPixel(i, y))
                    if (!previousPxer.contains(history)) previousPxer.add(history)
                    layerToDraw.setPixel(i, y, Color.TRANSPARENT)
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