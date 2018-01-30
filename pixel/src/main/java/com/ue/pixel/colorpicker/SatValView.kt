package com.ue.pixel.colorpicker

import android.content.Context
import android.graphics.*
import android.support.v4.graphics.ColorUtils
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.SeekBar

/**
 * Created by BennyKok on 10/15/2016.
 */

class SatValView : View {
    private var satPaint = Paint()
    private var thumbPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var bgPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private lateinit var satBitmap: Bitmap
    private var satBound = Rect()

    private var hsb: HueSeekBar? = null
    private var asb: AlphaSeekBar? = null

    private var alpha: Int = 0
    private var hue = 0f
    private var sat = 0f
    private var value = 0f

    private var listener: OnColorChangeListener? = null

    private var fingerX = 0f
    private var fingerY = 0f
    private var bgbitmap: Bitmap? = null

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    fun withHueBar(hsb: HueSeekBar) {
        this.hsb = hsb
        hsb.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                hue = progress.toFloat()
                if (width > 0) satBitmap = getSatValBitmap(hue, alpha)
                onColorRetrieved(alpha, hue, sat, value)
                invalidate()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}

            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
    }

    fun withAlphaBar(asb: AlphaSeekBar) {
        this.asb = asb
        asb.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                alpha = progress
                onColorRetrieved(alpha, hue, sat, value)
                invalidate()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}

            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        satBitmap = getSatValBitmap(hue, alpha)
        reCalBackground()
        satBound.set(0, 0, getRight(), getBottom())
        placePointer(sat * width, height - value * height, false)
    }

    private fun init() {
        thumbPaint.style = Paint.Style.STROKE
        thumbPaint.strokeWidth = 8f
        thumbPaint.color = Color.WHITE

        satPaint.isAntiAlias = true
        satPaint.isFilterBitmap = true
        satPaint.isDither = true

        setWillNotDraw(false)
        isDrawingCacheEnabled = true
        setWillNotCacheDrawing(false)
    }


    override fun onDraw(canvas: Canvas) {
        satPaint.alpha = alpha
        if (bgbitmap != null && !isInEditMode) canvas.drawBitmap(bgbitmap, null, canvas.clipBounds, bgPaint)
        canvas.drawBitmap(satBitmap, null, canvas.clipBounds, satPaint)
        canvas.drawCircle(fingerX, fingerY, 20f, thumbPaint)
    }

    fun getSatValBitmap(hue: Float, alpha: Int): Bitmap {
        val skipCount = 1
        val width = 100
        val height = 100
        val hueBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)

        val colors = IntArray(width * height)
        var pix = 0
        var y = 0
        while (y < height) {
            run {
                var x = 0
                while (x < width) {
                    if (pix >= width * height) break

                    val sat = x / width.toFloat()
                    val value = (height - y) / height.toFloat()
                    val hsv = floatArrayOf(hue, sat, value)

                    val color = Color.HSVToColor(hsv)
                    for (m in 0 until skipCount) {
                        if (pix >= width * height) break
                        if (x + m < width) {
                            colors[pix] = color
                            pix++
                        }
                    }
                    x += skipCount
                }
            }

            for (n in 0 until skipCount) {
                if (pix >= width * height) break
                for (x in 0 until width) {
                    colors[pix] = colors[pix - width]
                    pix++
                }
            }
            y += skipCount
        }
        hueBitmap.setPixels(colors, 0, width, 0, 0, width, height)
        return hueBitmap
    }

    fun reCalBackground() {
        bgbitmap = Bitmap.createBitmap(10 * 2, 10 * 2, Bitmap.Config.ARGB_8888).apply {
            eraseColor(ColorUtils.setAlphaComponent(Color.GRAY, 200))
            for (i in 0..9) {
                for (j in 0 until 10 * 2) {
                    if (j % 2 != 0) setPixel(i * 2 + 1, j, Color.argb(200, 220, 220, 220))
                    else setPixel(i * 2, j, Color.argb(200, 220, 220, 220))
                }
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                placePointer(event.x, event.y, true)
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                placePointer(event.x, event.y, true)
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    private fun placePointer(x: Float, y: Float, notify: Boolean) {
        var x = x
        var y = y
        if (x < 0) x = 0f
        else if (x > width) x = width.toFloat()

        if (y < 0) y = 0f
        else if (y > height) y = height.toFloat()

        fingerX = x
        fingerY = y

        if (notify) retrieveColorAt(x, y)

        invalidate()
    }

    private fun retrieveColorAt(x: Float, y: Float) {
        fingerX = x
        fingerY = y

        sat = x / width.toFloat()
        value = (height - y) / height.toFloat()

        onColorRetrieved(alpha, hue, sat, value)
    }

    fun setListener(listener: OnColorChangeListener) {
        this.listener = listener
    }


    interface OnColorChangeListener {
        fun onColorChanged(newColor: Int)
    }

    private fun onColorRetrieved(alpha: Int, hue: Float, sat: Float, value: Float) {
        val color = ColorUtils.setAlphaComponent(Color.HSVToColor(floatArrayOf(hue, sat, value)), alpha)
        listener?.onColorChanged(color)
    }

    fun setColor(color: Int) {
        val hsv = FloatArray(3)
        Color.colorToHSV(color, hsv)
        setSaturationAndValue(hsv[1], hsv[2])
        alpha = Color.alpha(color)
        hsb?.progress = hsv[0].toInt()
        asb?.progress = Color.alpha(color)
    }

    private fun setSaturationAndValue(sat: Float, `val`: Float) {
        this.sat = sat
        this.value = `val`
        placePointer(sat * width, height - `val` * height, false)
    }
}