package com.ue.pixel.widget

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import com.github.clans.fab.FloatingActionButton
import com.ue.library.util.compatGetColor
import com.ue.pixel.R
import com.ue.pixel.util.Tool

/**
 * Created by BennyKok on 10/9/2016.
 */

class BorderFab : FloatingActionButton {
    internal var paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var colorPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var three = 0f
    private var one = 0f
    private lateinit var bg: Bitmap
    internal var color = 0

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : this(context, attrs, defStyleAttr, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        init()
    }

    fun setColor(color: Int) {
        this.color = color
        invalidate()
    }

    private fun init() {
        bg = Bitmap.createBitmap(2, 2, Bitmap.Config.ARGB_8888)
        bg.eraseColor(Color.WHITE)
        bg.setPixel(0, 0, Color.GRAY)
        bg.setPixel(1, 1, Color.GRAY)

        three = Tool.convertDpToPixel(2f, context)
        one = Tool.convertDpToPixel(1f, context)

        paint.style = Paint.Style.STROKE
        paint.color = context.compatGetColor(R.color.colorAccent)
        paint.strokeWidth = Tool.convertDpToPixel(6f, context)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        colorPaint.color = Color.WHITE

        canvas.save()
        val p = Path()
        p.addCircle((width / 2).toFloat(), (height / 2).toFloat(), width / 3 + one, Path.Direction.CCW)
        canvas.clipPath(p)
        canvas.drawBitmap(bg, null, Rect(0, 0, width, height), colorPaint)
        canvas.restore()

        colorPaint.color = color
        canvas.drawCircle((width / 2).toFloat(), (height / 2).toFloat(), width / 3 + one, colorPaint)
        canvas.drawCircle((width / 2).toFloat(), (height / 2).toFloat(), width / 3 + one, paint)
    }
}