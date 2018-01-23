package com.ue.graffiti.model


import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.PointF

class Picture(private val contentId: Int, val transDx: Float, val transDy: Float, val scale: Float,
              val degree: Float, centerPoint: PointF, beginPoint: PointF) {
    private var content: Bitmap? = null
    val centerPoint: PointF
    val beginPoint: PointF

    init {
        this.centerPoint = PointF()
        this.centerPoint.set(centerPoint)
        this.beginPoint = PointF()
        this.beginPoint.set(beginPoint)
    }

    fun createContent(context: Context): Bitmap? {
        content = BitmapFactory.decodeResource(context.resources, contentId)
        return content
    }
}
