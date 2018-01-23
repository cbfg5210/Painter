package com.ue.graffiti.model

import android.graphics.Paint
import android.graphics.PointF
import android.graphics.Typeface

class Text(val content: String, val transDx: Float, val transDy: Float, val scale: Float,
           val degree: Float, centerPoint: PointF, beginPoint: PointF, paintColor: Int) {
    val centerPoint: PointF
    val beginPoint: PointF
    val paint: Paint

    init {
        this.centerPoint = PointF()
        this.centerPoint.set(centerPoint)
        this.beginPoint = PointF()
        this.beginPoint.set(beginPoint)
        paint = Paint()
        //        paint.setColor(DrawTouch.getCurPaint().getColor());
        paint.color = paintColor
        paint.textSize = 50f
        paint.typeface = Typeface.DEFAULT_BOLD
    }
}
