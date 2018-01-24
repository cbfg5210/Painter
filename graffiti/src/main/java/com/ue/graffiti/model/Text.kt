package com.ue.graffiti.model

import android.graphics.Paint
import android.graphics.PointF
import android.graphics.Typeface

class Text(val content: String, val transDx: Float, val transDy: Float, val scale: Float,
           val degree: Float, centerPoint: PointF, beginPoint: PointF, paintColor: Int) {
    val centerPoint = PointF().apply { set(centerPoint) }
    val beginPoint = PointF().apply { set(beginPoint) }
    val paint = Paint().apply {
        color = paintColor
        textSize = 50f
        typeface = Typeface.DEFAULT_BOLD
    }
}