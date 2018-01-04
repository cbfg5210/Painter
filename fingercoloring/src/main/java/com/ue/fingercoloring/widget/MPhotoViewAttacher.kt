package com.ue.fingercoloring.widget

import android.graphics.Matrix
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.ImageView.ScaleType

import com.github.chrisbanes.photoview.PhotoViewAttacher
import com.github.chrisbanes.photoview.Util

class MPhotoViewAttacher(imageView: ImageView) : PhotoViewAttacher(imageView) {
    private var move = false

    init {
        mMinScale = 0.5f
        mMidScale = PhotoViewAttacher.DEFAULT_MID_SCALE
        mMaxScale = 8.0f

        imageView.isDrawingCacheEnabled = true
        // Make sure we using MATRIX Scale Type
        imageView.scaleType = ScaleType.MATRIX

        if (!imageView.isInEditMode) {
            //避免双击缩放
            mGestureDetector.setOnDoubleTapListener(null)
            // Finally, update the UI so that we're zoomable
            isZoomable = true
        }
    }

    override fun onDrag(dx: Float, dy: Float) {
        move = true
        super.onDrag(dx, dy)
    }

    override fun onTouch(v: View, ev: MotionEvent): Boolean {
        var handled = false

        if (mZoomEnabled && Util.hasDrawable(v as ImageView)) {
            handled = super.onTouch(v, ev)

            if (ev.action == MotionEvent.ACTION_CANCEL || ev.action == MotionEvent.ACTION_UP) {
                if (move || running) {
                    move = false
                    return true
                }
                running = true
                fillColor(v as ColourImageView, ev)
                running = false
            }
        }

        return handled
    }

    private fun fillColor(imageView: ColourImageView, ev: MotionEvent) {
        if (move) return

        val inverse = Matrix()
        imageView.imageMatrix.invert(inverse)
        val touchPoint = floatArrayOf(ev.x, ev.y)
        inverse.mapPoints(touchPoint)
        val x = Integer.valueOf(touchPoint[0].toInt())!!
        val y = Integer.valueOf(touchPoint[1].toInt())!!

        if (imageView.model == ColourImageView.Model.PICKCOLOR) {
            imageView.pickColor(x, y)
        } else if (imageView.model == ColourImageView.Model.DRAW_LINE) {
            imageView.drawLine(x, y)
        } else {
            imageView.fillColorToSameArea(x, y)
        }
    }

    override fun update() {
        super.update()

        (mImageView as ColourImageView).update()
    }

    companion object {
        private var running: Boolean = false
    }
}
