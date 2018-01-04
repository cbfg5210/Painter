package com.ue.library.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View

/**
 * Created by hawk on 2017/12/21.
 */
class SquareView : View {
    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec)
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = MeasureSpec.getSize(heightMeasureSpec)
        var size = Math.min(width, height)
        if (size == 0) size = Math.max(width, height)
        setMeasuredDimension(size, size)
    }
}