package com.ue.graffiti.touch

import com.ue.graffiti.model.Pel
import com.ue.graffiti.widget.CanvasView

class DrawLineTouch(canvasView: CanvasView) : DrawTouch(canvasView) {

    override fun move() {
        super.move()

        newPel = Pel()
        movePoint.set(curPoint)

        newPel.path.moveTo(downPoint.x, downPoint.y)
        newPel.path.lineTo(movePoint.x, movePoint.y)
        newPel.path.lineTo(movePoint.x, movePoint.y + 1)

        selectedPel = newPel
        mSimpleTouchListener!!.setSelectedPel(selectedPel)
    }

    override fun up() {
        newPel.closure = false
        super.up()
    }
}
