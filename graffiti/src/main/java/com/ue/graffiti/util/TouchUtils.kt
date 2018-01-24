package com.ue.graffiti.util

import android.graphics.*
import com.ue.graffiti.model.CrossFillStep
import com.ue.graffiti.model.Pel
import com.ue.graffiti.model.Step
import java.util.*

/**
 * Created by hawk on 2018/1/16.
 */

object TouchUtils {
    // 填充区域重新打印
    fun reprintFilledAreas(undoStack: Stack<Step>, bitmap: Bitmap) {
        // 若为填充步骤
        undoStack.filterIsInstance<CrossFillStep>()
                .forEach { StepUtils.fillInWhiteBitmap(it, bitmap) }
    }

    fun ensureBitmapRecycled(bitmap: Bitmap?) {
        //确保传入位图已经回收
        bitmap?.recycle()
    }

    fun calPelSavedMatrix(selectedPel: Pel): Matrix {
        val savedMatrix = Matrix()
        // 将Path封装成PathMeasure，方便获取path内的matrix用
        val pathMeasure = PathMeasure(selectedPel.path, true)
        pathMeasure.getMatrix(pathMeasure.length, savedMatrix, PathMeasure.POSITION_MATRIX_FLAG and PathMeasure.TANGENT_MATRIX_FLAG)

        return savedMatrix
    }

    fun calPelCenterPoint(selectedPel: Pel): PointF {
        val boundRect = Rect()
        selectedPel.region.getBounds(boundRect)

        return PointF(((boundRect.right + boundRect.left) / 2).toFloat(), ((boundRect.bottom + boundRect.top) / 2).toFloat())
    }

    fun distance(begin: PointF, end: PointF): Float {
        val x = begin.x - end.x
        val y = begin.y - end.y
        return Math.sqrt((x * x + y * y).toDouble()).toFloat()
    }
}