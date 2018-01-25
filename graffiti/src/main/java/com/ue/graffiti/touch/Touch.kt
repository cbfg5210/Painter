package com.ue.graffiti.touch

import android.app.ProgressDialog
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.PointF
import android.graphics.Region
import android.view.MotionEvent
import android.view.View
import com.ue.graffiti.R
import com.ue.graffiti.event.OnMultiTouchListener
import com.ue.graffiti.event.SimpleTouchListener
import com.ue.graffiti.model.Pel
import com.ue.graffiti.model.Step
import com.ue.graffiti.widget.CanvasView
import java.util.*

//触摸类
open class Touch(canvasView: CanvasView?) : View.OnTouchListener {
    //获取undo
    protected lateinit var undoStack: Stack<Step>
    // 图元链表// 屏幕宽高
    protected lateinit var pelList: MutableList<Pel>
    // 画布裁剪区域
    protected lateinit var clipRegion: Region
    // 当前选中图元
    protected var selectedPel: Pel? = null
    //重绘画布
    protected var savedCanvas: Canvas = Canvas()
    // 当前重绘位图
    private var savedBitmap: Bitmap? = null
    //当前第一只手指事件坐标
    var curPoint: PointF = PointF()
    //当前第二只手指事件坐标
    protected var secPoint: PointF
    //特殊处理用
    //贝塞尔曲线切换时敲定
    var control: Boolean = false
    //多边形时敲定
    var beginPoint: PointF

    protected var mSimpleTouchListener: SimpleTouchListener? = null
    var isProcessing: Boolean = false
        protected set
    private var progressDialog: ProgressDialog? = null

    private var mMultiTouchListener: OnMultiTouchListener? = null

    protected val context: Context
        get() = mSimpleTouchListener!!.getContext()

    fun setMultiTouchListener(multiTouchListener: OnMultiTouchListener) {
        mMultiTouchListener = multiTouchListener
    }

    init {
        secPoint = PointF()
        beginPoint = PointF()

        if (canvasView != null) {
            selectedPel = canvasView!!.getSelectedPel()
            //获取undo
            undoStack = canvasView.undoStack
            // 画布裁剪区域
            clipRegion = canvasView.clipRegion
            // 图元链表// 屏幕宽高
            pelList = canvasView.getPelList()
        }
    }

    fun setProcessing(processing: Boolean, tip: String?) {
        isProcessing = processing
        if (isProcessing) {
            showProgressDialog(tip)
            return
        }
        if (progressDialog != null) {
            progressDialog!!.dismiss()
        }
    }

    protected fun showProgressDialog(progressTip: String?) {
        if (progressDialog == null) {
            progressDialog = ProgressDialog(context)
            progressDialog!!.setCanceledOnTouchOutside(false)
        }
        progressDialog!!.setMessage(progressTip)
        if (!progressDialog!!.isShowing) {
            progressDialog!!.show()
        }
    }

    protected fun dismissProgressDialog() {
        if (progressDialog != null) {
            progressDialog!!.dismiss()
        }
    }

    fun setTouchListener(canvasView: CanvasView?, simpleTouchListener: SimpleTouchListener) {
        mSimpleTouchListener = simpleTouchListener
        canvasView?.setOnTouchListener(this)
    }

    // 第一只手指按下
    open fun down1() {}

    // 第二只手指按下
    protected open fun down2() {}

    // 手指移动
    open fun move() {}

    // 手指抬起
    open fun up() {}

    private fun setCurrentPoint(point: PointF) {
        curPoint.set(point)
    }

    private fun setSecondPoint(point: PointF) {
        secPoint.set(point)
    }

    protected fun updateSavedBitmap(isInvalidate: Boolean) {
        mSimpleTouchListener?.updateSavedBitmap(savedCanvas, savedBitmap, pelList, selectedPel, isInvalidate)
    }

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        if (isProcessing) {
            showProgressDialog(v.context.getString(R.string.gr_is_processing))
            return true
        }
        //非传感器模式才响应屏幕
        if (mSimpleTouchListener!!.isSensorRegistered()) {
            return true
        }
        //第一只手指坐标
        setCurrentPoint(PointF(event.getX(0), event.getY(0)))
        //第二只手指坐标
        setSecondPoint(if (event.pointerCount > 1) PointF(event.getX(1), event.getY(1)) else PointF(1f, 1f))

        val actionMasked = event.actionMasked
        when (actionMasked) {
        // 第一只手指按下
            MotionEvent.ACTION_DOWN -> down1()
            MotionEvent.ACTION_POINTER_DOWN -> {
                if (event.pointerCount > 2 && mMultiTouchListener != null) {
                    mMultiTouchListener!!.onMultiTouch()
                    return true
                }
                // 第二个手指按下
                down2()
            }
            MotionEvent.ACTION_MOVE -> move()
        // 第一只手指抬起
            MotionEvent.ACTION_UP,
                //第二只手抬起
            MotionEvent.ACTION_POINTER_UP -> up()
        }
        mSimpleTouchListener?.invalidate()
        return true
    }
}