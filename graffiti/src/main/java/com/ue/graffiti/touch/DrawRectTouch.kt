package com.ue.graffiti.touch

import android.graphics.Path
import android.graphics.RectF

import com.ue.graffiti.model.Pel
import com.ue.graffiti.widget.CanvasView

class DrawRectTouch(canvasView: CanvasView) : DrawTouch(canvasView) {

    override fun move() {
        super.move()

        newPel = Pel()
        movePoint.set(curPoint)
        newPel.path.addRect(RectF(downPoint.x, downPoint.y, movePoint.x, movePoint.y), Path.Direction.CCW)
        selectedPel = newPel
        mSimpleTouchListener!!.setSelectedPel(selectedPel)
    }

    override fun up() {
        newPel.closure = true
        super.up()
    }
}
