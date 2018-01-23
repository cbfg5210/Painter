package com.ue.graffiti.util

import android.graphics.BlurMaskFilter
import android.graphics.CornerPathEffect
import android.graphics.DashPathEffect
import android.graphics.DiscretePathEffect
import android.graphics.EmbossMaskFilter
import android.graphics.MaskFilter
import android.graphics.Matrix
import android.graphics.Path
import android.graphics.PathDashPathEffect
import android.graphics.PathEffect
import android.graphics.RectF

import com.ue.graffiti.R


/**
 * Created by hawk on 2018/1/23.
 */

object PenUtils {
    /**
     * 线型
     *
     * @param image
     * @return
     */
    fun getPaintShapeByImage(image: Int, width: Int, matrix: Matrix?): PathEffect {
        var matrix = matrix
        if (matrix == null) {
            matrix = Matrix()
            matrix.setSkew(2f, 2f)
        }
        when (image) {
            R.drawable.ic_line_solid -> return CornerPathEffect(10f)
            R.drawable.ic_line_dashed -> return DashPathEffect(floatArrayOf(20f, 20f), 0.5f)
            R.drawable.ic_line_dotted -> return DashPathEffect(floatArrayOf(40f, 20f, 10f, 20f), 0.5f)
            R.drawable.ic_line_lighting -> return DiscretePathEffect(5f, 9f)
            R.drawable.ic_line_oval -> {
                val p = Path()
                p.addOval(RectF(0f, 0f, width.toFloat(), width.toFloat()), Path.Direction.CCW)
                return PathDashPathEffect(p, (width + 10).toFloat(), 0f, PathDashPathEffect.Style.ROTATE)
            }
            R.drawable.ic_line_rect -> {
                val p3 = Path()
                p3.addRect(RectF(0f, 0f, width.toFloat(), width.toFloat()), Path.Direction.CCW)
                return PathDashPathEffect(p3, (width + 10).toFloat(), 0f, PathDashPathEffect.Style.ROTATE)
            }
            R.drawable.ic_line_brush -> {
                val p2 = Path()
                p2.addRect(RectF(0f, 0f, width.toFloat(), width.toFloat()), Path.Direction.CCW)
                p2.transform(matrix)
                return PathDashPathEffect(p2, 2f, 0f, PathDashPathEffect.Style.TRANSLATE)
            }
            R.drawable.ic_line_mark_pen -> {
                val p1 = Path()
                p1.addArc(RectF(0f, 0f, (width + 4).toFloat(), (width + 4).toFloat()), -90f, 90f)
                p1.addArc(RectF(0f, 0f, (width + 4).toFloat(), (width + 4).toFloat()), 90f, -90f)
                return PathDashPathEffect(p1, 2f, 0f, PathDashPathEffect.Style.TRANSLATE)
            }
            else -> return CornerPathEffect(10f)
        }
    }

    /**
     * 特效
     *
     * @param image
     * @return
     */
    fun getPaintEffectByImage(image: Int): MaskFilter? {
        when (image) {
            R.drawable.ic_effect_solid -> return null
            R.drawable.ic_effect_blur -> return BlurMaskFilter(8f, BlurMaskFilter.Blur.NORMAL)
            R.drawable.ic_effect_hollow -> return BlurMaskFilter(8f, BlurMaskFilter.Blur.OUTER)
            R.drawable.ic_effect_relievo -> return EmbossMaskFilter(floatArrayOf(1f, 1f, 1f), 0.4f, 6f, 3.5f)
            else -> return null
        }
    }
}