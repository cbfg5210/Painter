package com.ue.fingercoloring.widget

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.TextView

class DragedTextView : TextView {
    private var mPreviousx = 0
    private var mPreviousy = 0
    // a array for save the drag position
    val currentLayout = IntArray(4)

    constructor(context: Context) : super(context) {}

    constructor(context: Context, attribute: AttributeSet) : super(context, attribute, 0) {}

    constructor(context: Context, attribute: AttributeSet, style: Int) : super(context, attribute, style) {}


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (currentLayout[0] != 0 || currentLayout[1] != 0 || currentLayout[2] != 0 || currentLayout[3] != 0)
            layout(currentLayout[0], currentLayout[1], currentLayout[2], currentLayout[3])
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val iAction = event.action
        val iCurrentx = event.x.toInt()
        val iCurrenty = event.y.toInt()
        when (iAction) {
            MotionEvent.ACTION_DOWN -> {
                mPreviousx = iCurrentx
                mPreviousy = iCurrenty
            }
            MotionEvent.ACTION_MOVE -> {
                val iDeltx = iCurrentx - mPreviousx
                val iDelty = iCurrenty - mPreviousy
                val iLeft = left
                val iTop = top
                if (iDeltx != 0 || iDelty != 0) {
                    currentLayout[0] = iLeft + iDeltx
                    currentLayout[1] = iTop + iDelty
                    currentLayout[2] = iLeft + iDeltx + width
                    currentLayout[3] = iTop + iDelty + height
                    postInvalidate()
                }
                mPreviousx = iCurrentx - iDeltx
                mPreviousy = iCurrenty - iDelty
            }
            MotionEvent.ACTION_UP -> {
            }
            MotionEvent.ACTION_CANCEL -> {
            }
        }
        return true
    }

    companion object {
        private val TAG = "qt"
    }
}
