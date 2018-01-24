package com.ue.graffiti.touch

import android.graphics.PointF

import com.ue.graffiti.model.Pel
import com.ue.graffiti.widget.CanvasView


class DrawFreehandTouch(canvasView: CanvasView) : DrawTouch(canvasView) {
    private val lastPoint: PointF = PointF()

    override fun down1() {
        super.down1()
        lastPoint.set(downPoint)

        newPel = Pel()
        newPel.path.moveTo(lastPoint.x, lastPoint.y)
    }

    override fun move() {
        super.move()
        movePoint.set(curPoint)
        newPel.path.quadTo(lastPoint.x, lastPoint.y, (lastPoint.x + movePoint.x) / 2, (lastPoint.y + movePoint.y) / 2)
        lastPoint.set(movePoint)
        selectedPel = newPel
        mSimpleTouchListener!!.setSelectedPel(selectedPel)
    }

    override fun up() {
        newPel.closure = true
        super.up()
    }
}