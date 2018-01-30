package com.ue.pixel.colorpicker

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.support.v7.widget.AppCompatSeekBar
import android.util.AttributeSet

/**
 * Created by BennyKok on 10/15/2016.
 */

class AlphaSeekBar : AppCompatSeekBar {
    private var hueBitmap: Bitmap? = null
    private val thumbPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val huePaint = Paint(Paint.ANTI_ALIAS_FLAG)

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    private fun init() {
        thumbPaint.color = Color.WHITE
        thumbPaint.style = Paint.Style.STROKE
        thumbPaint.strokeWidth = 8f

        max = 255
        progress = 255
        setPadding(0, 0, 0, 0)

        thumb = object : Drawable() {
            override fun getIntrinsicHeight() = height
            override fun getIntrinsicWidth() = height / 3
            override fun getOpacity() = PixelFormat.TRANSPARENT

            override fun draw(canvas: Canvas) {
                thumbPaint.color = Color.argb(progress, 0, 0, 0)
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
                if (hueBitmap != null) canvas.drawBitmap(hueBitmap, null, Rect(0, 0, width, height), huePaint)
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
        val width = width / 20
        val height = height / 20

        val hueBitmap = Bitmap.createBitmap(width * 2, height * 2, Bitmap.Config.ARGB_8888)

        for (i in 0 until width) {
            for (j in 0 until height * 2) {
                val alpha = (255 * (i.toFloat() / width)).toInt()
                if (j % 2 != 0) hueBitmap.setPixel(i * 2 + 1, j, Color.argb(alpha, 100, 100, 100))
                else hueBitmap.setPixel(i * 2, j, Color.argb(alpha, 100, 100, 100))
            }
        }
        return hueBitmap
    }
}
