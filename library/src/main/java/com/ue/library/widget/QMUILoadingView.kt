package com.ue.library.widget

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import com.ue.library.R
import com.ue.library.util.QMUIDisplayHelper

/**
 * 用于显示 Loading 的 [View]，支持颜色和大小的设置。
 *
 * @author cginechen
 * @date 2016-09-21
 */
class QMUILoadingView : View {
    private var mSize: Int = 0
    private var mPaintColor: Int = 0
    private var mAnimateValue = 0
    private var mAnimator: ValueAnimator? = null
    private lateinit var mPaint: Paint

    private val mUpdateListener = ValueAnimator.AnimatorUpdateListener { animation ->
        mAnimateValue = animation.animatedValue as Int
        invalidate()
    }

    constructor (context: Context) : this(context, null)
    constructor (context: Context, attrs: AttributeSet?) : this(context, attrs, R.attr.QMUILoadingStyle)
    constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = R.attr.QMUILoadingStyle) : super(context, attrs, defStyleAttr) {
        val array = getContext().obtainStyledAttributes(attrs, R.styleable.QMUILoadingView, defStyleAttr, 0)
        mSize = array.getDimensionPixelSize(R.styleable.QMUILoadingView_qmui_loading_view_size, QMUIDisplayHelper.dp2px(context, 32))
        mPaintColor = array.getInt(R.styleable.QMUILoadingView_android_color, Color.WHITE)
        array.recycle()
        initPaint()
    }

    constructor(context: Context, size: Int, color: Int) : super(context) {
        mSize = size
        mPaintColor = color
        initPaint()
    }

    private fun initPaint() {
        mPaint = Paint()
        mPaint.color = mPaintColor
        mPaint.isAntiAlias = true
        mPaint.strokeCap = Paint.Cap.ROUND
    }

    fun setColor(color: Int) {
        mPaintColor = color
        mPaint.color = color
        invalidate()
    }

    fun setSize(size: Int) {
        mSize = size
        requestLayout()
    }

    private fun start() {
        if (mAnimator != null) {
            mAnimator!!.apply { if (!isStarted) start() }
            return
        }
        mAnimator = ValueAnimator.ofInt(0, LINE_COUNT - 1).apply {
            addUpdateListener(mUpdateListener)
            duration = 600
            repeatMode = ValueAnimator.RESTART
            repeatCount = ValueAnimator.INFINITE
            interpolator = LinearInterpolator()
            start()
        }
    }

    private fun stop() {
        mAnimator?.apply {
            removeUpdateListener(mUpdateListener)
            removeAllUpdateListeners()
            cancel()
            mAnimator = null
        }
    }

    private fun drawLoading(canvas: Canvas, rotateDegrees: Int) {
        val width = mSize / 12
        val height = mSize / 6
        mPaint.strokeWidth = width.toFloat()

        canvas.rotate(rotateDegrees.toFloat(), (mSize / 2).toFloat(), (mSize / 2).toFloat())
        canvas.translate((mSize / 2).toFloat(), (mSize / 2).toFloat())

        for (i in 0 until LINE_COUNT) {
            canvas.rotate(DEGREE_PER_LINE.toFloat())
            mPaint.alpha = (255f * (i + 1) / LINE_COUNT).toInt()
            canvas.translate(0f, (-mSize / 2 + width / 2).toFloat())
            canvas.drawLine(0f, 0f, 0f, height.toFloat(), mPaint)
            canvas.translate(0f, (mSize / 2 - width / 2).toFloat())
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(mSize, mSize)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val saveCount = canvas.saveLayer(0f, 0f, width.toFloat(), height.toFloat(), null, Canvas.ALL_SAVE_FLAG)
        drawLoading(canvas, mAnimateValue * DEGREE_PER_LINE)
        canvas.restoreToCount(saveCount)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        start()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stop()
    }

    override fun onVisibilityChanged(changedView: View, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)
        if (visibility == View.VISIBLE) start()
        else stop()
    }

    companion object {
        private const val LINE_COUNT = 12
        private const val DEGREE_PER_LINE = 360 / LINE_COUNT
    }
}