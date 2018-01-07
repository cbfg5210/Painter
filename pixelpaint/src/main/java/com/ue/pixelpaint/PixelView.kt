package com.ue.pixelpaint

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView


/**
 * Created by hawk on 2018/1/4.
 */
class PixelView : SurfaceView, SurfaceHolder.Callback  {

    var lineNum = 16
    private var linePaint: Paint = Paint()
    private var squareSize = 0F
    private var tempSize = 0F
    private var canvasSize = 0F

    private var thirdOneRatio = 0F
    private var thirdTwoRatio = 0F

    private lateinit var renderThread: Thread
    private var isRenderEnabled: Boolean = false//是否继续渲染

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle) {
        thirdOneRatio = resources.getDimension(R.dimen.widget_size_1) * 0.3f
        thirdTwoRatio = resources.getDimension(R.dimen.widget_size_2) * 0.3f
        setOnTouchListener(GestureTouchListener())

        holder.addCallback(this)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec)
        Log.e("PixelView", "onMeasure: w=$measuredWidth,h=$measuredHeight")
    }

//    override fun setScaleX(scaleX: Float) {
//        super.setScaleX(scaleX)
//        val canvas = holder.lockCanvas()
//        val matrix=Matrix()
//        matrix.postScale(scaleX,scaleX)
//        canvas.matrix=matrix
//        holder.unlockCanvasAndPost(canvas)
//    }

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
    }

    private val renderRunnable = Runnable {
        while (isRenderEnabled) {
            var canvas: Canvas? = null

            try {
                canvas = holder.lockCanvas()
                synchronized(holder) {
                    render(canvas!!)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                if (canvas != null) holder.unlockCanvasAndPost(canvas)
            }

            try {
                Thread.sleep(1000)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }
    }

    /*
    * surface callback
    * */
    override fun surfaceCreated(holder: SurfaceHolder) {
        renderThread = Thread(renderRunnable)
        isRenderEnabled = true
        renderThread.start()
    }

    override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
        Log.e("PixelView", "surfaceChanged: w=$width,h=$height")
    }

    override fun surfaceDestroyed(holder: SurfaceHolder?) {
        isRenderEnabled = false
        holder?.removeCallback(this)
        try {
            renderThread.join()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }
}