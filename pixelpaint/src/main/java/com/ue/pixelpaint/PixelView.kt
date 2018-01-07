package com.ue.pixelpaint

import android.content.Context
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View


/**
 * Created by hawk on 2018/1/4.
 */
class PixelView : View {
    var lineNum = 16
    private var linePaint: Paint = Paint()
    private var squareSize = 0F
    private var tempSize = 0F
    private var canvasSize = 0F

    private var thirdOneRatio = 0F
    private var thirdTwoRatio = 0F

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle) {
//        thirdOneRatio = resources.getDimension(R.dimen.widget_size_1) / 3
//        thirdTwoRatio = resources.getDimension(R.dimen.widget_size_2) / 3
    }

    fun setTranslateEnabled(isTranslateEnabled: Boolean) {
    }

/*    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec)
        Log.e("PixelView", "onMeasure: w=$measuredWidth,h=$measuredHeight")
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        render(canvas)
    }

    private fun render(canvas: Canvas) {
        canvas.drawColor(Color.WHITE)

        squareSize = measuredWidth * scaleX / lineNum
        canvasSize = squareSize * lineNum
        Log.e("PixelView", "render: squareSize=$squareSize,canvasSize=$canvasSize,w=$measuredWidth,h=$measuredHeight")

        linePaint.color = Color.parseColor("#010101")
        linePaint.strokeWidth = thirdOneRatio

        for (i in 1 until lineNum) {
            tempSize = squareSize * i
            //horizontal line
            canvas.drawLine(0F, tempSize, canvasSize, tempSize, linePaint)
            //vertical line
            canvas.drawLine(tempSize, 0F, tempSize, canvasSize, linePaint)
        }

        linePaint.color = Color.BLACK
        linePaint.strokeWidth = thirdTwoRatio

        var i = 8
        while (i < lineNum) {
            tempSize = squareSize * i
            //horizontal line
            canvas.drawLine(0F, tempSize, canvasSize, tempSize, linePaint)
            //vertical line
            canvas.drawLine(tempSize, 0F, tempSize, canvasSize, linePaint)

            i += 8
        }
    }*/
}