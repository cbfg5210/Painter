package com.ue.autodraw

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.ue.library.util.RxJavaUtils
import io.reactivex.Observable
import io.reactivex.ObservableOnSubscribe
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

class AutoDrawView : SurfaceView, SurfaceHolder.Callback {

    private var mSrcBmWidth = 0
    private var mSrcBmHeight = 0
    private var offsetX = 0
    private var offsetY = 0

    private var mPaint: Paint = Paint()
    private lateinit var mArray: Array<BooleanArray>

    private lateinit var mTmpCanvas: Canvas
    private var mTmpBm: Bitmap? = null
    private lateinit var mPaintBm: Bitmap

    private var bgBitmapRes = 0
    private var delaySpeed = 20L

    private var mLastPoint = Point()
    private var isDrawing = false

    private var disposable: Disposable? = null

    companion object {
        private val PAINT_WIDTH = 10
        private val PAINT_HEIGHT = 20
    }

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle)

    init {
        setPaintBitmapRes(R.drawable.paint)
        holder.addCallback(this)
    }

    //设置画笔图片
    fun setPaintBitmapRes(paintBitmapRes: Int) {
        mPaintBm = AutoDrawUtils.getRatioBitmap(context, paintBitmapRes, PAINT_WIDTH, PAINT_HEIGHT)
    }

    fun setBgBitmapRes(bgBitmapRes: Int) {
        this.bgBitmapRes = bgBitmapRes
        resetBgBitmap()
    }

    //获取离指定点最近的一个未绘制过的点
    private fun getNearestPoint(p: Point): Point? {
        //以点p为中心，向外扩大搜索范围，每次搜索的是与p点相距add的正方形
        var add = 1
        while (add < mSrcBmWidth && add < mSrcBmHeight) {
            val beginX = if (p.x - add >= 0) p.x - add else 0
            val endX = if (p.x + add < mSrcBmWidth) p.x + add else mSrcBmWidth - 1
            val beginY = if (p.y - add >= 0) p.y - add else 0
            val endY = if (p.y + add < mSrcBmHeight) p.y + add else mSrcBmHeight - 1
            //搜索正方形的上下边
            for (x in beginX..endX) {
                if (mArray[x][beginY]) {
                    mArray[x][beginY] = false
                    return Point(x, beginY)
                }
                if (mArray[x][endY]) {
                    mArray[x][endY] = false
                    return Point(x, endY)
                }
            }
            //搜索正方形的左右边
            for (y in beginY + 1 until endY) {
                if (mArray[beginX][y]) {
                    mArray[beginX][beginY] = false
                    return Point(beginX, beginY)
                }
                if (mArray[endX][y]) {
                    mArray[endX][y] = false
                    return Point(endX, y)
                }
            }
            add++
        }
        return null
    }

    /**
     * 绘制
     * return :false 表示绘制完成，true表示还需要继续绘制
     */
    private fun drawOutline(): Boolean {
        //获取count个点后，一次性绘制到bitmap在把bitmap绘制到SurfaceView
        var count = 100
        var p: Point? = null
        while (count-- > 0) {
            //获取下一个需要绘制的点
            p = getNearestPoint(mLastPoint)
            //如果p为空，说明所有的点已经绘制完成
            p ?: break
            mLastPoint = p
            mTmpCanvas.drawPoint((p.x + offsetX).toFloat(), (p.y + offsetY).toFloat(), mPaint)
        }
        //将bitmap绘制到SurfaceView中
        val canvas = holder.lockCanvas()
        canvas.drawBitmap(mTmpBm, 0f, 0f, mPaint)
        if (p != null) {
            canvas.drawBitmap(mPaintBm, (p.x + offsetX).toFloat(), (p.y - mPaintBm!!.height + offsetY).toFloat(), mPaint)
        }
        holder.unlockCanvasAndPost(canvas)
        return p != null
    }

    //重画
    fun reDraw(array: Array<BooleanArray>) {
        if (isDrawing) return

        resetBgBitmap()
        beginDraw(array)
    }

    fun beginDraw(array: Array<BooleanArray>) {
        if (isDrawing) return
        isDrawing = true

        this.mArray = array
        mSrcBmWidth = array.size
        mSrcBmHeight = array[0].size

        offsetX = (measuredWidth - mSrcBmWidth) / 2
        offsetY = (measuredHeight - mSrcBmHeight) / 2

        mLastPoint = Point()

        RxJavaUtils.dispose(disposable)
        disposable = Observable
                .create(ObservableOnSubscribe<Any> {
                    while (isDrawing) {
                        isDrawing = drawOutline()
                        try {
                            Thread.sleep(delaySpeed)
                        } catch (exp: InterruptedException) {
                        }
                    }
                    RxJavaUtils.dispose(disposable)
                })
                .subscribeOn(Schedulers.single())
                .subscribe()
    }

    override fun surfaceCreated(holder: SurfaceHolder) {}

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        resetBgBitmap()
    }

    private fun resetBgBitmap() {
        if (bgBitmapRes > 0) {
            mTmpBm = AutoDrawUtils.getRatioBitmap(context, bgBitmapRes, measuredWidth, measuredHeight)
            mTmpBm = Bitmap.createScaledBitmap(mTmpBm, measuredWidth, measuredHeight, false)
            mTmpCanvas = Canvas(mTmpBm)
        } else {
            mTmpBm = Bitmap.createBitmap(measuredWidth, measuredHeight, Bitmap.Config.ARGB_8888)
            mTmpCanvas = Canvas(mTmpBm)
            mPaint.color = Color.WHITE
            mPaint.style = Paint.Style.FILL
            mTmpCanvas.drawRect(0f, 0f, measuredWidth.toFloat(), measuredHeight.toFloat(), mPaint)
        }
        val canvas = holder.lockCanvas()
        canvas.drawBitmap(mTmpBm, 0f, 0f, mPaint)
        holder.unlockCanvasAndPost(canvas)
        //设置回轮廓画笔
        mPaint.style = Paint.Style.STROKE
        mPaint.color = Color.BLACK
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        isDrawing = false
        RxJavaUtils.dispose(disposable)
    }
}
