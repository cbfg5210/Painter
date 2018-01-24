package com.ue.graffiti.touch

import android.content.DialogInterface
import android.graphics.PointF
import android.graphics.drawable.BitmapDrawable
import android.widget.Toast

import com.ue.graffiti.R
import com.ue.graffiti.helper.DialogHelper
import com.ue.graffiti.widget.CanvasView

class KeepDrawingTouch(canvasView: CanvasView) : Touch(canvasView) {
    private val downPoint: PointF = PointF()
    private var mKeepDrawingTouchListener: KeepDrawingTouchListener? = null

    fun setKeepDrawingListener(keepDrawingTouchListener: KeepDrawingTouchListener) {
        mKeepDrawingTouchListener = keepDrawingTouchListener
    }

    // 第一只手指按下
    override fun down1() {
        downPoint.set(curPoint)
        updateSavedBitmap(false)
        //调取起始点标志图片
        val startFlag = context.resources.getDrawable(R.drawable.img_startflag) as BitmapDrawable
        savedCanvas.drawBitmap(startFlag.bitmap, downPoint.x, downPoint.y, null)

        val tip = "您确定以(" + downPoint.x.toInt() + "," + downPoint.y.toInt() + ")为起始点并开始重力绘图？"
        DialogHelper.showSensorDrawingDialog(context, tip, DialogInterface.OnClickListener { _, _ ->
            if (mKeepDrawingTouchListener != null) {
                mKeepDrawingTouchListener!!.onDownPoint(downPoint)
                mKeepDrawingTouchListener!!.registerKeepDrawingSensor()
            }
            Toast.makeText(context, "摆动手机画图吧", Toast.LENGTH_SHORT).show()
        })
    }

    interface KeepDrawingTouchListener {
        fun onDownPoint(downPoint: PointF)

        fun registerKeepDrawingSensor()
    }
}