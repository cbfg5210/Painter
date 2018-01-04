package com.ue.fingercoloring.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.View

import com.ue.fingercoloring.R

class TimeLineDecoration(private val mContext: Context, private val distance: Int) : RecyclerView.ItemDecoration() {

    private val drawable: Drawable
    private val linePaint: Paint

    init {
        drawable = ContextCompat.getDrawable(mContext, R.drawable.fc_svg_time)

        linePaint = Paint()
        linePaint.strokeWidth = 2f
        linePaint.color = -0x686869
    }

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State?) {
        outRect.left = distance
        outRect.right = distance
        outRect.bottom = distance

        if (parent.getChildAdapterPosition(view) == 0) {
            outRect.top = distance
        } else if (parent.getChildAdapterPosition(view) == 1) {
            outRect.top = 2 * distance
        }

        if (parent.getChildAdapterPosition(view) % 2 == 0) {
            outRect.left = distance
        } else {
            outRect.right = distance
        }
    }

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State?) {
        if (parent.childCount == 0) return
        drawVerticalLine(c, parent)
        drawVerticalViews(c, parent)
    }

    fun drawVerticalLine(c: Canvas, parent: RecyclerView) {
        val x = parent.measuredWidth / 2
        val startY = parent.getChildAt(0).top

        val itemCount = parent.adapter.itemCount
        val lastChild = parent.getChildAt(parent.childCount - 1)

        val endY = if (parent.getChildAdapterPosition(lastChild) == itemCount - 1) lastChild.top else lastChild.bottom

        c.drawLine(x.toFloat(), startY.toFloat(), x.toFloat(), endY.toFloat(), linePaint)
    }

    fun drawVerticalViews(c: Canvas, parent: RecyclerView) {
        val parentWidth = parent.measuredWidth

        val childCount = parent.childCount
        for (i in 0 until childCount) {
            val child = parent.getChildAt(i)

            val horizontalLineTop = child.top + drawable.intrinsicHeight / 2
            val horizontalLineLeft: Int
            val horizontalLineRight: Int

            if (child.left > parentWidth / 2) {
                horizontalLineLeft = parentWidth / 2
                horizontalLineRight = child.left
            } else {
                horizontalLineLeft = child.right
                horizontalLineRight = parentWidth / 2
            }

            c.drawLine(horizontalLineLeft.toFloat(), horizontalLineTop.toFloat(), horizontalLineRight.toFloat(), horizontalLineTop.toFloat(), linePaint)

            val top = child.top
            val bottom = top + drawable.intrinsicHeight

            val drawableLeft = parentWidth / 2 - drawable.intrinsicWidth / 2
            val drawableRight = parentWidth / 2 + drawable.intrinsicWidth / 2

            drawable.setBounds(drawableLeft, top, drawableRight, bottom)
            drawable.draw(c)
        }
    }
}