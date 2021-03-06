package com.ue.graffiti.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View

class PenEffectView : View {
    private lateinit var path: Path
    private var mPaint = Paint()

    constructor(context: Context?) : this(context, null)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs, 0)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        initPath()
    }

    fun setPaint(newPaint: Paint) {
        mPaint = newPaint
        invalidate()
    }

    //以当前选中的笔触（粗细、特效）画在矩形示意框里
    override fun onDraw(canvas: Canvas) {
        canvas.drawPath(path, mPaint)
    }

    private fun initPath() {
        path = Path()

        val width = resources.displayMetrics.widthPixels.toFloat()
        val height = 160f

        path.moveTo(0f, height / 2)
        path.cubicTo(0f, height / 2, width / 4, height / 4, width / 2, height / 2)
        //下波浪 连接用
        val path2 = Path()
        path2.moveTo(width / 2, height / 2)
        path2.cubicTo(width / 2, height / 2, width / 4 * 3, height / 4 * 3, width, height / 2)

        path.addPath(path2)
    }
}