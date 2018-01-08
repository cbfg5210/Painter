package com.ue.autodraw

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.SurfaceHolder
import android.view.SurfaceView

/**
 * Package com.hc.myoutline
 * Created by HuaChao on 2016/5/27.
 */
class AutoDrawView : SurfaceView, SurfaceHolder.Callback {

    private var mTmpBm: Bitmap? = null
    private var mTmpCanvas: Canvas? = null
    private var mWidth: Int = 0
    private var mHeight: Int = 0
    private lateinit var mPaint: Paint
    private var mSrcBmWidth: Int = 0
    private var mSrcBmHeight: Int = 0
    private lateinit var mArray: Array<BooleanArray>
    private val offsetY = 100

    private var mPaintBm: Bitmap? = null
    private var mLastPoint: Point? = Point(0, 0)

    private var isDrawing = false

    //获取下一个需要绘制的点
    private val nextPoint: Point?
        get() {
            mLastPoint = getNearestPoint(mLastPoint)
            return mLastPoint
        }

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle) {
        init()
    }

    private fun init() {
        holder.addCallback(this)
        mPaint = Paint()
        mPaint.color = Color.BLACK
    }

    //设置画笔图片
    fun setPaintBm(paintBm: Bitmap) {
        mPaintBm = paintBm
    }

    //获取离指定点最近的一个未绘制过的点
    private fun getNearestPoint(p: Point?): Point? {
        p ?: return null
        //以点p为中心，向外扩大搜索范围，每次搜索的是与p点相距add的正方形
        var add = 1
        while (add < mSrcBmWidth && add < mSrcBmHeight) {
            val beginX = if (p.x - add >= 0) p.x - add else 0
            val endX = if (p.x + add < mSrcBmWidth) p.x + add else mSrcBmWidth - 1
            val beginY = if (p.y - add >= 0) p.y - add else 0
            val endY = if (p.y + add < mSrcBmHeight) p.y + add else mSrcBmHeight - 1
            //搜索正方形的上下边
            for (x in beginX..endX) {
                if (mArray!![x][beginY]) {
                    mArray!![x][beginY] = false
                    return Point(x, beginY)
                }
                if (mArray!![x][endY]) {
                    mArray!![x][endY] = false
                    return Point(x, endY)
                }
            }
            //搜索正方形的左右边
            for (y in beginY + 1 until endY) {
                if (mArray!![beginX][y]) {
                    mArray!![beginX][beginY] = false
                    return Point(beginX, beginY)
                }
                if (mArray!![endX][y]) {
                    mArray!![endX][y] = false
                    return Point(endX, y)
                }
            }
            add++
        }

        return null
    }


    /**
     * //绘制
     * return :false 表示绘制完成，true表示还需要继续绘制
     */
    private fun draw(): Boolean {
        mPaint.style = Paint.Style.STROKE
        mPaint.color = Color.BLACK
        //获取count个点后，一次性绘制到bitmap在把bitmap绘制到SurfaceView
        var count = 100
        var p: Point? = null
        while (count-- > 0) {
            p = nextPoint
            //如果p为空，说明所有的点已经绘制完成
            p ?: return false

            mTmpCanvas?.drawPoint(p.x.toFloat(), (p.y + offsetY).toFloat(), mPaint)
        }
        //将bitmap绘制到SurfaceView中
        val canvas = holder.lockCanvas()
        canvas.drawBitmap(mTmpBm!!, 0f, 0f, mPaint)
        if (p != null) {
            canvas.drawBitmap(mPaintBm!!, p.x.toFloat(), (p.y - mPaintBm!!.height + offsetY).toFloat(), mPaint)
        }
        holder.unlockCanvasAndPost(canvas)
        return true
    }

    //重画
    fun reDraw(array: Array<BooleanArray>) {
        if (isDrawing) return
        mTmpBm = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888)
        mTmpCanvas = Canvas(mTmpBm!!)
        mPaint.color = Color.WHITE
        mPaint.style = Paint.Style.FILL
        mTmpCanvas?.drawRect(0f, 0f, mWidth.toFloat(), mHeight.toFloat(), mPaint)
        mLastPoint = Point(0, 0)
        beginDraw(array)
    }

    fun beginDraw(array: Array<BooleanArray>) {
        if (isDrawing) return
        this.mArray = array
        mSrcBmWidth = array.size
        mSrcBmHeight = array[0].size

        object : Thread() {
            override fun run() {
                while (true) {
                    isDrawing = true
                    if (!draw()) break
                    try {
                        Thread.sleep(20)
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                    }
                }
                isDrawing = false
            }
        }.start()
    }


    override fun surfaceCreated(holder: SurfaceHolder) {}

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        this.mWidth = width
        this.mHeight = height
        mTmpBm = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        mTmpCanvas = Canvas(mTmpBm!!)
        mPaint.color = Color.WHITE
        mPaint.style = Paint.Style.FILL
        mTmpCanvas?.drawRect(0f, 0f, mWidth.toFloat(), mHeight.toFloat(), mPaint)
        val canvas = holder.lockCanvas()
        canvas.drawBitmap(mTmpBm!!, 0f, 0f, mPaint)
        holder.unlockCanvasAndPost(canvas)

        mPaint.style = Paint.Style.STROKE
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {}
}
