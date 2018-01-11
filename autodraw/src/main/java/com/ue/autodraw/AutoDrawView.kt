package com.ue.autodraw

import android.content.Context
import android.graphics.*
import android.media.CamcorderProfile
import android.media.MediaRecorder
import android.os.Environment
import android.util.AttributeSet
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.widget.Toast
import com.ue.library.util.RxJavaUtils
import io.reactivex.Observable
import io.reactivex.ObservableOnSubscribe
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.io.File
import java.io.IOException

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
                    initRecorder()
                    prepareRecorder()
                    recorder.start()

                    Log.e("AutoDrawView", "beginDraw: Recording Started")
                    recording = true
                    while (isDrawing) {
                        isDrawing = drawOutline()
                        try {
                            Thread.sleep(delaySpeed)
                        } catch (exp: InterruptedException) {
                        }
                    }
                    RxJavaUtils.dispose(disposable)

                    recorder.stop()
                    recording = false
                    Toast.makeText(context, "Stopped Recording", Toast.LENGTH_SHORT).show()// toast shows a display of little sorts

                    /*保存结果图片
                    val path = Environment.getExternalStorageDirectory().path + "/tt/"
                    FileUtils.saveImageLocally(context, mTmpBm!!, path, "tt.png", object : FileUtils.OnSaveImageListener {
                        override fun onSaved(path: String) {
                            Log.e("AutoDrawView", "onSaved: save path:$path")
                        }
                    })*/
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


    private var recorder = MediaRecorder()
    private var recording = false

    /**
     * Called when the activity is first created.
     */
    /*override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)

        recorder = MediaRecorder()// Instantiate our media recording object
        initRecorder()
        setContentView(R.layout.view)

        val cameraView = findViewById<View>(R.id.surface_view) as SurfaceView
        holder = cameraView.holder
        holder.addCallback(this)
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS)

        cameraView.isClickable = true// make the surface view clickable
        cameraView.setOnClickListener(this)// onClicklistener to be called when the surface view is clicked
    }*/

    private fun initRecorder() {// this takes care of all the mediarecorder settings
        val outputFile = File(Environment.getExternalStorageDirectory().path)
        val cpHigh = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH)
//        recorder.setProfile(cpHigh)

        //recorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
        // default microphone to be used for audio
        // recorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);// default camera to be used for video capture.
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)// generally used also includes h264 and best for flash
        // recorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264); //well known video codec used by many including for flash
        //recorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);// typically amr_nb is the only codec for mobile phones so...

        //recorder.setVideoFrameRate(15);// typically 12-15 best for normal use. For 1080p usually 30fms is used.
        // recorder.setVideoSize(720,480);// best size for resolution.
        //recorder.setMaxFileSize(10000000);
        recorder.setOutputFile(outputFile.absolutePath + "/tt.3gp")
        //recorder.setVideoEncodingBitRate(256000);//
        //recorder.setAudioEncodingBitRate(8000);
        recorder.setMaxDuration(600000)
    }

    private fun prepareRecorder() {
        recorder.setPreviewDisplay(holder.surface)

        try {
            recorder.prepare()
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    /*override fun onClick(v: View) {
        if (recording) {
            recorder.stop()
            recording = false
            // Let's initRecorder so we can record again
            initRecorder()
            prepareRecorder()
            Toast.makeText(context, "Stopped Recording", Toast.LENGTH_SHORT).show()// toast shows a display of little sorts
        } else {
            recorder.start()
            Log.v(TAG, "Recording Started")
            recording = true
        }
    }*/

//    override fun surfaceCreated(holder: SurfaceHolder) {
//        initRecorder()
//        Log.v(TAG, "surfaceCreated")
//        prepareRecorder()
//    }

//    override fun surfaceDestroyed(holder: SurfaceHolder) {
//        if (recording) {
//            recorder.stop()
//            recording = false
//        }
//        recorder.release()
//        finish()
//    }
}
