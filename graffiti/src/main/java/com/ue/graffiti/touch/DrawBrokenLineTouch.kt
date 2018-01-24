package com.ue.graffiti.touch

import android.graphics.Path
import android.graphics.PointF

import com.ue.graffiti.model.Pel
import com.ue.graffiti.widget.CanvasView

class DrawBrokenLineTouch(canvasView: CanvasView) : DrawTouch(canvasView) {
    private var firstDown = true
    private val lastPath: Path
    var hasFinished: Boolean = false

    init {
        lastPath = Path()
    }

    override fun down1() {
        super.down1()
        if (!firstDown) {
            return
        }
        // 画折线的第一笔
        beginPoint.set(downPoint)

        newPel = Pel()
        newPel.path.moveTo(beginPoint.x, beginPoint.y)
        lastPath.set(newPel.path)

        firstDown = false
    }

    override fun move() {
        super.move()
        movePoint.set(curPoint)
        newPel.path.set(lastPath)
        newPel.path.lineTo(movePoint.x, movePoint.y)
        selectedPel = newPel
        mSimpleTouchListener.setSelectedPel(selectedPel)
    }

    override fun up() {
        val endPoint = PointF()
        endPoint.set(curPoint)

        if (!hasFinished) {
            lastPath.set(newPel.path)
            return
        }
        newPel.closure = false
        super.up()
        hasFinished = false
        firstDown = true
    }
}