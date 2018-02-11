package com.ue.library.widget.colorpicker

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.support.v7.widget.AppCompatSeekBar
import android.util.AttributeSet

/**
 * Created by BennyKok on 10/15/2016.
 */

class HueSeekBar : AppCompatSeekBar {
    private var hueBitmap: Bitmap? = null
    private val thumbPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    private fun init() {
        thumbPaint.color = Color.WHITE
        thumbPaint.style = Paint.Style.STROKE
        thumbPaint.strokeWidth = 8f

        max = 360
        setPadding(0, 0, 0, 0)

        thumb = object : Drawable() {
            override fun getIntrinsicHeight() = height
            override fun getIntrinsicWidth() = height / 3
            override fun getOpacity() = PixelFormat.TRANSPARENT

            override fun draw(canvas: Canvas) {
                val hsv = floatArrayOf(progress.toFloat(), 1f, 1f)
                thumbPaint.color = Color.HSVToColor(hsv)
                thumbPaint.style = Paint.Style.FILL_AND_STROKE
                canvas.drawRect(bounds, thumbPaint)

                thumbPaint.color = Color.WHITE
                thumbPaint.style = Paint.Style.STROKE
                canvas.drawRect(bounds, thumbPaint)
            }

            override fun setAlpha(alpha: Int) {}
            override fun setColorFilter(colorFilter: ColorFilter?) {}
        }

        thumbOffset = -(height / 3) / 6

        progressDrawable = object : Drawable() {
            override fun getIntrinsicHeight() = height
            override fun getIntrinsicWidth() = width
            override fun getOpacity() = PixelFormat.TRANSPARENT

            override fun draw(canvas: Canvas) {
                if (hueBitmap != null) canvas.drawBitmap(hueBitmap, null, bounds, null)
            }

            override fun setAlpha(alpha: Int) {}

            override fun setColorFilter(colorFilter: ColorFilter?) {}
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)

        hueBitmap = getHueBitmap()
        invalidate()
    }

    fun getHueBitmap(): Bitmap {
        val hueBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        for (x in 0 until width) {
            var hue = if (width > height) x * 360f / width else 0f
            for (y in 0 until height) {
                if (width <= height) hue = y * 360f / height
                hueBitmap.setPixel(x, y, Color.HSVToColor(floatArrayOf(hue, 1f, 1f)))
            }
        }
        return hueBitmap
    }
}
