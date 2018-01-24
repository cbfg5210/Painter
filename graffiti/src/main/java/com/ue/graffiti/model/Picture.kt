package com.ue.graffiti.model


import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.PointF

class Picture(private val contentId: Int, val transDx: Float, val transDy: Float, val scale: Float,
              val degree: Float, centerPoint: PointF, beginPoint: PointF) {
    private var content: Bitmap? = null
    val centerPoint = PointF().apply { set(centerPoint) }
    val beginPoint = PointF().apply { set(beginPoint) }

    fun createContent(context: Context): Bitmap? {
        content = BitmapFactory.decodeResource(context.resources, contentId)
        return content
    }
}
