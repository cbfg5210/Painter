package com.ue.graffiti.touch

import android.graphics.PointF

import com.ue.graffiti.model.Pel
import com.ue.graffiti.widget.CanvasView


class DrawBesselTouch(canvasView: CanvasView) : DrawTouch(canvasView) {
    private val endPoint: PointF = PointF()

    override fun down1() {
        super.down1()

        if (!control) {
            //非拉伸曲线操作表明是新图元的开端
            beginPoint.set(downPoint) //记录起点
            newPel = Pel()
        }
    }

    override fun move() {
        super.move()

        movePoint.set(curPoint)
        newPel.path.reset()

        if (control) {
            newPel.path.moveTo(beginPoint.x, beginPoint.y)
            newPel.path.cubicTo(beginPoint.x, beginPoint.y, movePoint.x, movePoint.y, endPoint.x, endPoint.y)
        } else {
            //非拉伸贝塞尔曲线操作
            newPel.path.moveTo(beginPoint.x, beginPoint.y)
            newPel.path.cubicTo(beginPoint.x, beginPoint.y, beginPoint.x, beginPoint.y, movePoint.x, movePoint.y)
        }

        selectedPel = newPel
        mSimpleTouchListener.setSelectedPel(selectedPel)
    }

    override fun up() {
        val upPoint = PointF()
        upPoint.set(curPoint)

        if (!control) {
            //非拉伸贝塞尔曲线操作则记录落脚点
            endPoint.set(upPoint)//记录落脚点
            control = true
        } else {
            newPel.closure = false
            super.up() //最终敲定

            control = false
        }
    }
}