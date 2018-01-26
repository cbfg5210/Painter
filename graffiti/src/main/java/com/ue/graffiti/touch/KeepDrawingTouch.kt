package com.ue.graffiti.touch

import android.content.DialogInterface
import android.graphics.PointF
import android.graphics.drawable.BitmapDrawable
import com.ue.graffiti.R
import com.ue.graffiti.helper.DialogHelper
import com.ue.library.util.toast
import com.ue.graffiti.widget.CanvasView

class KeepDrawingTouch(canvasView: CanvasView) : Touch(canvasView) {
    private val downPoint: PointF = PointF()
    var keepDrawingTouchListener: KeepDrawingTouchListener? = null

    // 第一只手指按下
    override fun down1() {
        downPoint.set(curPoint)
        updateSavedBitmap(false)
        //调取起始点标志图片
        val startFlag = context.resources.getDrawable(R.drawable.img_startflag) as BitmapDrawable
        savedCanvas.drawBitmap(startFlag.bitmap, downPoint.x, downPoint.y, null)

        val tip = context.getString(R.string.gr_begin_sensor_draw, downPoint.x.toInt(), downPoint.y.toInt())
        DialogHelper.showSensorDrawingDialog(context, tip, DialogInterface.OnClickListener { _, _ ->
            keepDrawingTouchListener?.apply {
                onDownPoint(downPoint)
                registerKeepDrawingSensor()
            }
            context.toast(R.string.gr_shake_to_draw)
        })
    }

    interface KeepDrawingTouchListener {
        fun onDownPoint(downPoint: PointF)

        fun registerKeepDrawingSensor()
    }
}