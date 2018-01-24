package com.ue.graffiti.util

import android.graphics.*
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
        return when (image) {
            R.drawable.ic_line_solid -> CornerPathEffect(10f)
            R.drawable.ic_line_dashed -> DashPathEffect(floatArrayOf(20f, 20f), 0.5f)
            R.drawable.ic_line_dotted -> DashPathEffect(floatArrayOf(40f, 20f, 10f, 20f), 0.5f)
            R.drawable.ic_line_lighting -> DiscretePathEffect(5f, 9f)
            R.drawable.ic_line_oval -> {
                PathDashPathEffect(
                        Path().apply { addOval(RectF(0f, 0f, width.toFloat(), width.toFloat()), Path.Direction.CCW) },
                        (width + 10).toFloat(), 0f, PathDashPathEffect.Style.ROTATE)
            }
            R.drawable.ic_line_rect -> {
                PathDashPathEffect(
                        Path().apply { addRect(RectF(0f, 0f, width.toFloat(), width.toFloat()), Path.Direction.CCW) },
                        (width + 10).toFloat(), 0f, PathDashPathEffect.Style.ROTATE)
            }
            R.drawable.ic_line_brush -> {
                PathDashPathEffect(
                        Path().apply {
                            addRect(RectF(0f, 0f, width.toFloat(), width.toFloat()), Path.Direction.CCW)
                            transform(matrix)
                        },
                        2f, 0f, PathDashPathEffect.Style.TRANSLATE)
            }
            R.drawable.ic_line_mark_pen -> {
                PathDashPathEffect(
                        Path().apply {
                            addArc(RectF(0f, 0f, (width + 4).toFloat(), (width + 4).toFloat()), -90f, 90f)
                            addArc(RectF(0f, 0f, (width + 4).toFloat(), (width + 4).toFloat()), 90f, -90f)
                        },
                        2f, 0f, PathDashPathEffect.Style.TRANSLATE)
            }
            else -> CornerPathEffect(10f)
        }
    }

    /**
     * 特效
     *
     * @param image
     * @return
     */
    fun getPaintEffectByImage(image: Int): MaskFilter? {
        return when (image) {
            R.drawable.ic_effect_solid -> null
            R.drawable.ic_effect_blur -> BlurMaskFilter(8f, BlurMaskFilter.Blur.NORMAL)
            R.drawable.ic_effect_hollow -> BlurMaskFilter(8f, BlurMaskFilter.Blur.OUTER)
            R.drawable.ic_effect_relievo -> EmbossMaskFilter(floatArrayOf(1f, 1f, 1f), 0.4f, 6f, 3.5f)
            else -> null
        }
    }
}