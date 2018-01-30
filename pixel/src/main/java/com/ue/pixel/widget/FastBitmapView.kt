package com.ue.pixel.widget

import android.content.Context
import android.graphics.*
import android.support.v4.graphics.ColorUtils
import android.util.AttributeSet
import android.view.View
import com.ue.pixel.R
import com.ue.pixel.util.Tool

/**
 * Created by BennyKok on 10/10/2016.
 */

class FastBitmapView : View {
    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val iconPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private lateinit var overlay: PorterDuffColorFilter
    private var selected: Boolean = false
    private var visible = true
    private var accentColor: Int = 0
    private val boundary = RectF()
    private val boundary2 = RectF()
    private val boundary3 = RectF()
    var bitmap: Bitmap? = null
        set(bitmap) {
            field = bitmap
            invalidate()
        }
    private var invisibleBitmap: Bitmap? = null
    private val visibilityBg = Path()
    private var strokeWidth = 2.5f
    private val radius = 0f
    private val iconSize = Tool.convertDpToPixel(24f, context)
    private val iconSize2 = iconSize / 2

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    fun setVisible(visible: Boolean) {
        this.visible = visible
    }

    override fun setSelected(selected: Boolean) {
        this.selected = selected
    }

    fun init() {
        accentColor = context.resources.getColor(R.color.colorAccent)
        bgPaint.style = Paint.Style.STROKE
        bgPaint.strokeWidth = Tool.convertDpToPixel(2f, context)
        bgPaint.color = Color.GRAY

        iconPaint.isFilterBitmap = true
        iconPaint.color = Color.WHITE
        iconPaint.style = Paint.Style.FILL_AND_STROKE

        overlay = PorterDuffColorFilter(ColorUtils.setAlphaComponent(Color.DKGRAY, 100), PorterDuff.Mode.SRC_OVER)

        strokeWidth = Tool.convertDpToPixel(strokeWidth, context)

        invisibleBitmap = Tool.drawableToBitmap(resources.getDrawable(R.drawable.ic_visibility_off_24dp))

        setWillNotDraw(false)
    }

    override fun onDraw(canvas: Canvas) {
        paint.style = Paint.Style.FILL_AND_STROKE
        paint.color = Color.WHITE
        paint.colorFilter = if (visible) null else overlay

        if (this.bitmap != null) canvas.drawBitmap(this.bitmap, null, boundary2, paint)
        if (!visible && invisibleBitmap != null) canvas.drawBitmap(invisibleBitmap, null, boundary3, iconPaint)

        paint.colorFilter = null
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = strokeWidth

        if (selected) paint.color = accentColor
        else paint.color = Color.parseColor("#c6c6c6")

        canvas.drawRoundRect(boundary, radius, radius, paint)

        super.onDraw(canvas)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        bitmap?.apply {
            val w = (width * Math.min(this.width, this.height) / Math.max(this.width, this.height)).toFloat()
            val h = (height * Math.min(this.width, this.height) / Math.max(this.width, this.height)).toFloat()

            boundary.set(0f, 0f, width.toFloat(), height.toFloat())
            boundary.inset(strokeWidth / 2, strokeWidth / 2)

            if (this.width < this.height) {
                boundary2.set(Math.abs(width - w) / 2, 0f, width - Math.abs(width - w) / 2, height.toFloat())
            } else {
                boundary2.set(0f, Math.abs(height - h) / 2, height.toFloat(), height - Math.abs(height - h) / 2)
            }
            boundary2.inset(strokeWidth, strokeWidth)
            boundary3.set(width / 2 - iconSize2, height / 2 - iconSize2, width / 2 + iconSize2, height / 2 + iconSize2)

            visibilityBg.reset()
            visibilityBg.moveTo(strokeWidth, strokeWidth)
            visibilityBg.lineTo(width - strokeWidth, height - strokeWidth)

            visibilityBg.moveTo(width - strokeWidth, strokeWidth)
            visibilityBg.lineTo(strokeWidth, height - strokeWidth)
        }
    }
}