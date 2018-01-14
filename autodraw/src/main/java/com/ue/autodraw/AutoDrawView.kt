package com.ue.autodraw

import android.content.Context
import android.graphics.*
import android.os.Environment
import android.util.AttributeSet
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.ue.library.constant.Constants
import com.ue.library.util.BitmapUtils
import com.ue.library.util.FileUtils
import com.ue.library.util.PermissionUtils
import com.ue.library.util.RxJavaUtils
import io.reactivex.Observable
import io.reactivex.ObservableOnSubscribe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.text.SimpleDateFormat

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
    private var sobelBitmap: Bitmap? = null
    private var paintColor = Color.BLACK
    private var bitmapName = ""

    var autoDrawListener: OnAutoDrawListener? = null

    private var bgBitmapRes = 0
    private var delaySpeed = 0L
    private var paintBitmapRes = 0

    private var mLastPoint = Point()
    var isDrawing = false
        private set

    var isReadyToDraw = false
        private set
        get() {
            return sobelBitmap != null
        }

    var isCanSave = false
        private set
        get() {
            return sobelBitmap != null && mTmpBm != null
        }

    private var drawDisposable: Disposable? = null
    private var loadDisposable: Disposable? = null

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle)

    init {
        setPaintBitmapRes(R.drawable.svg_pencil)
        holder.addCallback(this)
    }

    /**
     * 设置参数
     */
    //设置画笔图片
    fun setPaintBitmapRes(paintBitmapRes: Int) {
        if (this.paintBitmapRes == paintBitmapRes) {
            return
        }
        mPaintBm = BitmapUtils.getSvgBitmap(context, paintBitmapRes)
        this.paintBitmapRes = paintBitmapRes
    }

    fun setPaintColor(paintColor: Int) {
        this.paintColor = paintColor
    }

    fun setBgBitmapRes(bgBitmapRes: Int) {
        this.bgBitmapRes = bgBitmapRes
        resetCanvas(true)
    }

    fun setLineThickness(thickness: Int) {
        if (thickness > 0) {
            mPaint.strokeWidth = thickness.toFloat()
        }
    }

    fun setDelaySpeed(delay: Int) {
        delaySpeed = delay.toLong()
    }
    /* 设置参数 end */

    fun setOutlineObject(bm: Bitmap) {
        //重设对象时第一步就是取消原先的加载
        RxJavaUtils.dispose(loadDisposable)

        this.bitmapName = SimpleDateFormat("yyyyMMddHHmmss").format(System.currentTimeMillis())

        loadDisposable = Observable
                .create(ObservableOnSubscribe<Bitmap> { e ->
                    //480x800,648x1152
                    //返回的是处理过的Bitmap
                    val sobelBitmap =
                            if (resources.displayMetrics.widthPixels >= 1080) SobelUtils.sobel(bm, 648, 1152)
                            else SobelUtils.sobel(bm, 480, 800)

                    e.onNext(sobelBitmap)
                })
                .subscribeOn(Schedulers.single())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ sobelBitmap ->
                    this.sobelBitmap = sobelBitmap
                    autoDrawListener?.onReady()
                })
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
        return isDrawing && tmpPoint != null
    }

    /**
     * 保存结果图片
     */
    fun saveOutlinePicture(saveListener: FileUtils.OnSaveImageListener) {
        if (!isCanSave) return

        PermissionUtils.checkReadWriteStoragePerms(context,
                context.getString(R.string.no_read_storage_perm),
                object : PermissionUtils.SimplePermissionListener {
                    override fun onSucceed(requestCode: Int, grantPermissions: List<String>) {
                        val path = Environment.getExternalStorageDirectory().path + Constants.PATH_AUTO_DRAW
                        FileUtils.saveImageLocally(context, mTmpBm!!, path, "$bitmapName.png", saveListener)
                    }
                })
    }

    fun stopDrawing() {
        if (!isDrawing) {
            return
        }
        isDrawing = false
        RxJavaUtils.dispose(drawDisposable)
        autoDrawListener?.onStop()
    }

    fun startDrawing() {
        sobelBitmap ?: return
        if (isDrawing) return

        isDrawing = true

        mArray = getArray(sobelBitmap!!)
        mSrcBmWidth = mArray.size
        mSrcBmHeight = mArray[0].size

        offsetX = (measuredWidth - mSrcBmWidth) / 2
        offsetY = (measuredHeight - mSrcBmHeight) / 2

        mLastPoint = Point()

        RxJavaUtils.dispose(drawDisposable)
        drawDisposable = Observable
                .create(ObservableOnSubscribe<Any> { e ->
                    //绘制背景
                    resetCanvas(true)
                    //绘制轮廓
                    while (drawOutline()) {
                        try {
                            Thread.sleep(delaySpeed)
                        } catch (exp: InterruptedException) {
                        }
                    }
                    isDrawing = false
                    e.onNext(1)
                    e.onComplete()
                })
                .subscribeOn(Schedulers.single())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    autoDrawListener?.onComplete()
                })
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        resetCanvas(false)
    }

    fun resetCanvas(isClear: Boolean) {
        if (!isClear && mTmpBm != null) {
            mTmpBm = Bitmap.createScaledBitmap(mTmpBm, measuredWidth, measuredHeight, false)
            mTmpCanvas = Canvas(mTmpBm)
        } else {
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
        }
        val canvas = holder.lockCanvas()
        canvas.drawBitmap(mTmpBm, 0f, 0f, mPaint)
        holder.unlockCanvasAndPost(canvas)
        //设置回轮廓画笔
        mPaint.style = Paint.Style.STROKE
        mPaint.color = paintColor
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        isDrawing = false
        RxJavaUtils.dispose(loadDisposable)
        RxJavaUtils.dispose(drawDisposable)
    }

    interface OnAutoDrawListener {
        //fun onPrepare()
        fun onReady()

        //fun onStart()
        fun onStop()

        fun onComplete()
    }
}
