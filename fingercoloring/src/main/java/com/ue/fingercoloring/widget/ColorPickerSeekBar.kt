package com.ue.fingercoloring.widget

import android.content.Context
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Shader
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RectShape
import android.os.Build
import android.util.AttributeSet
import android.widget.SeekBar

/**
 * Created by akolluru on 21/08/14.
 * A ColorPickerSeekBar is an extension of SeekBar for choosing a color.
 * The user can touch the thumb and drag left or right to set the color.
 *
 *
 * Clients can attach a ColorPickerSeekBar.OnColorSeekBarChangeListener to be notified
 * of color changes
 */
class ColorPickerSeekBar : SeekBar, SeekBar.OnSeekBarChangeListener {

    private var mOnColorSeekbarChangeListener: OnColorSeekBarChangeListener? = null
    var color: Int = 0
        private set

    fun setOnColorSeekbarChangeListener(listener: OnColorSeekBarChangeListener) {
        this.mOnColorSeekbarChangeListener = listener
    }

    constructor(context: Context) : super(context) {
        setOnSeekBarChangeListener(this)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        setOnSeekBarChangeListener(this)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        setOnSeekBarChangeListener(this)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        init()
    }

    /**
     * Initializes the color seekbar with the gradient
     */
    fun init() {
        val colorGradient: LinearGradient
        if (Build.VERSION.SDK_INT >= 16) {
            colorGradient = LinearGradient(0f, 0f, (this.measuredWidth - this.thumb.intrinsicWidth).toFloat(), 0f,
                    intArrayOf(-0x1000000, -0xffff01, -0xff0100, -0xff0001, -0x10000, -0xff01, -0x100, 0xFFFFFF), null, Shader.TileMode.CLAMP
            )
        } else {
            colorGradient = LinearGradient(0f, 0f, this.measuredWidth.toFloat(), 0f,
                    intArrayOf(-0x1000000, -0xffff01, -0xff0100, -0xff0001, -0x10000, -0xff01, -0x100, 0xFFFFFF), null, Shader.TileMode.CLAMP
            )
        }
        val shape = ShapeDrawable(RectShape())
        shape.paint.shader = colorGradient
        this.progressDrawable = shape
        this.max = 256 * 7 - 1
    }

    /**
     * A callback that notifies clients when the color has been changed.
     * This includes changes that were initiated by the user through a
     * touch gesture or arrow key/trackball as well as changes that were initiated programmatically.
     */
    interface OnColorSeekBarChangeListener {
        fun onColorChanged(seekBar: SeekBar, color: Int, b: Boolean)
        fun onStartTrackingTouch(seekBar: SeekBar)
        fun onStopTrackingTouch(seekBar: SeekBar)
    }

    abstract class SimpleColorSeekBarChangeListener : OnColorSeekBarChangeListener {
        override fun onColorChanged(seekBar: SeekBar, color: Int, b: Boolean) {}
        override fun onStartTrackingTouch(seekBar: SeekBar) {}
        override fun onStopTrackingTouch(seekBar: SeekBar) {}
    }

    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {

        var r = 0
        var g = 0
        var b = 0

        if (progress < 256) {
            b = progress
        } else if (progress < 256 * 2) {
            g = progress % 256
            b = 256 - progress % 256
        } else if (progress < 256 * 3) {
            g = 255
            b = progress % 256
        } else if (progress < 256 * 4) {
            r = progress % 256
            g = 256 - progress % 256
            b = 256 - progress % 256
        } else if (progress < 256 * 5) {
            r = 255
            g = 0
            b = progress % 256
        } else if (progress < 256 * 6) {
            r = 255
            g = progress % 256
            b = 256 - progress % 256
        } else if (progress < 256 * 7) {
            r = 255
            g = 255
            b = progress % 256
        }

        color = Color.argb(255, r, g, b)
        mOnColorSeekbarChangeListener?.onColorChanged(seekBar, color, fromUser)
    }

    override fun onStartTrackingTouch(seekBar: SeekBar) {
        mOnColorSeekbarChangeListener?.onStartTrackingTouch(seekBar)
    }

    override fun onStopTrackingTouch(seekBar: SeekBar) {
        mOnColorSeekbarChangeListener?.onStopTrackingTouch(seekBar)
    }
}