package com.ue.autodraw

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.ue.library.util.BitmapUtils
import com.ue.library.util.RxJavaUtils
import io.reactivex.Observable
import io.reactivex.ObservableOnSubscribe
import io.reactivex.android.schedulers.AndroidSchedulers
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
    private lateinit var sobelBitmap: Bitmap

    private var autoDrawListener: OnAutoDrawListener? = null

    private var bgBitmapRes = 0
    private var delaySpeed = 0L
    private var paintBitmapRes = 0

    private var mLastPoint = Point()
    private var isDrawing = false

    private var drawDisposable: Disposable? = null
    private var loadDisposable: Disposable? = null

    companion object {
        private val PAINT_WIDTH = 10
        private val PAINT_HEIGHT = 20
        private val REQ_SIZE = 150

        private val FLAG_PREPARE = 1
        private val FLAG_START = 2
        private val FLAG_COMPLETE = 3
    }

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle)

    init {
        setPaintBitmapRes(R.drawable.svg_pencil)
        holder.addCallback(this)
    }

    fun setAutoDrawListener(autoDrawListener: OnAutoDrawListener) {
        this.autoDrawListener = autoDrawListener
    }

    fun loadBitmapThenDraw(imgSrc: Int) {
        autoDrawListener?.onPrepare()
        val bm = BitmapUtils.getRatioBitmap(context, imgSrc, REQ_SIZE, REQ_SIZE)
        resetBitmapThenDraw(bm)
    }

    fun resetBitmapThenDraw(bm: Bitmap) {
        RxJavaUtils.dispose(loadDisposable)
        loadDisposable = Observable
                .create(ObservableOnSubscribe<Int> { e ->
                    //480x800,648x1152
                    //返回的是处理过的Bitmap
                    sobelBitmap =
                            if (resources.displayMetrics.widthPixels >= 1080) SobelUtils.sobel(bm, 648, 1152)
                            else SobelUtils.sobel(bm, 480, 800)

                    e.onNext(FLAG_START)

                    beginDraw(getArray(sobelBitmap))
                })
                .subscribeOn(Schedulers.single())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ flag ->
                    if (flag == FLAG_START) {
                        autoDrawListener?.onStart()
                    }
                })
    }

    fun redraw() {
        if (!isDrawing) {
            resetBgBitmap()
            beginDraw(getArray(sobelBitmap))
        }
    }

    //根据Bitmap信息，获取每个位置的像素点是否需要绘制
    //使用boolean数组而不是int[][]主要是考虑到内存的消耗
    private fun getArray(bitmap: Bitmap): Array<BooleanArray> {
        val b = Array(bitmap.width) { BooleanArray(bitmap.height) }
        for (i in 0 until bitmap.width) {
            for (j in 0 until bitmap.height) {
                b[i][j] = bitmap.getPixel(i, j) != Color.WHITE
            }
        }
        return b
    }

    //设置画笔图片
    fun setPaintBitmapRes(paintBitmapRes: Int) {
        if (this.paintBitmapRes == paintBitmapRes) {
            return
        }
        mPaintBm = BitmapUtils.getSvgBitmap(context, paintBitmapRes)
        this.paintBitmapRes = paintBitmapRes
    }

    fun setBgBitmapRes(bgBitmapRes: Int) {
        this.bgBitmapRes = bgBitmapRes
        resetBgBitmap()
    }

    fun setLineThickness(thickness: Int) {
        mPaint.strokeWidth = thickness.toFloat()
    }

    fun setDelaySpeed(delay: Int) {
        delaySpeed = delay + 1L
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
    private var tmpPoint: Point? = null

    private fun drawOutline(): Boolean {
        //获取100个点后，一次性绘制到bitmap在把bitmap绘制到SurfaceView
        for (i in 0..99) {
            //获取下一个需要绘制的点
            tmpPoint = getNearestPoint(mLastPoint)
            //如果tmpPoint为空，说明所有的点已经绘制完成
            tmpPoint ?: break
            mLastPoint = tmpPoint!!
            mTmpCanvas.drawPoint((mLastPoint.x + offsetX).toFloat(), (mLastPoint.y + offsetY).toFloat(), mPaint)
        }
        //将bitmap绘制到SurfaceView中
        val canvas = holder.lockCanvas()
        canvas.drawBitmap(mTmpBm, 0f, 0f, mPaint)
        if (tmpPoint != null) {
            canvas.drawBitmap(mPaintBm, (mLastPoint.x + offsetX).toFloat(), (mLastPoint.y - mPaintBm.height + offsetY).toFloat(), mPaint)
        }
        holder.unlockCanvasAndPost(canvas)
        return tmpPoint != null
    }

    private fun beginDraw(array: Array<BooleanArray>) {
        if (isDrawing) return
        isDrawing = true

        this.mArray = array
        mSrcBmWidth = array.size
        mSrcBmHeight = array[0].size

        offsetX = (measuredWidth - mSrcBmWidth) / 2
        offsetY = (measuredHeight - mSrcBmHeight) / 2

        mLastPoint = Point()

        RxJavaUtils.dispose(drawDisposable)
        drawDisposable = Observable
                .create(ObservableOnSubscribe<Int> { e ->
                    while (drawOutline()) {
                        if (delaySpeed > 0) {
                            try {
                                Thread.sleep(delaySpeed)
                            } catch (exp: InterruptedException) {
                            }
                        }
                    }
                    isDrawing = false
                    RxJavaUtils.dispose(drawDisposable)
                    e.onNext(FLAG_COMPLETE)
                    /*保存结果图片
                    val path = Environment.getExternalStorageDirectory().path + "/tt/"
                    FileUtils.saveImageLocally(context, mTmpBm!!, path, "tt.png", object : FileUtils.OnSaveImageListener {
                        override fun onSaved(path: String) {
                            Log.e("AutoDrawView", "onSaved: save path:$path")
                        }
                    })*/
                })
                .subscribeOn(Schedulers.single())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ flag ->
                    if (flag == FLAG_COMPLETE) {
                        autoDrawListener?.onComplete()
                    }
                })
    }

    override fun surfaceCreated(holder: SurfaceHolder) {}

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        resetBgBitmap()
    }

    private fun resetBgBitmap() {
        if (bgBitmapRes > 0) {
            mTmpBm = BitmapUtils.getRatioBitmap(context, bgBitmapRes, measuredWidth, measuredHeight)
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
        canvas.drawPicture(Picture())
        holder.unlockCanvasAndPost(canvas)
        //设置回轮廓画笔
        mPaint.style = Paint.Style.STROKE
        mPaint.color = Color.BLACK
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        isDrawing = false
        RxJavaUtils.dispose(loadDisposable)
        RxJavaUtils.dispose(drawDisposable)
    }

    interface OnAutoDrawListener {
        fun onPrepare()
        fun onStart()
        fun onComplete()
    }
}
