package com.ue.graffiti.util;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.PathMeasure;
import android.graphics.PointF;
import android.graphics.Rect;

import com.ue.graffiti.model.CrossFillStep;
import com.ue.graffiti.model.Pel;
import com.ue.graffiti.model.Step;

import java.util.Stack;

/**
 * Created by hawk on 2018/1/16.
 */

public class TouchUtils {
    // 填充区域重新打印
    public static void reprintFilledAreas(Stack<Step> undoStack, Bitmap bitmap) {
        for (Step step : undoStack) {
            // 若为填充步骤
            if (step instanceof CrossFillStep) {
                StepUtils.fillInWhiteBitmap((CrossFillStep) step, bitmap);
            }
        }
    }

    public static void ensureBitmapRecycled(Bitmap bitmap) {
        //确保传入位图已经回收
        if (bitmap != null && !bitmap.isRecycled()) {
            bitmap.recycle();
        }
    }

    public static Matrix calPelSavedMatrix(Pel selectedPel) {
        Matrix savedMatrix = new Matrix();
        PathMeasure pathMeasure = new PathMeasure(selectedPel.getPath(), true);// 将Path封装成PathMeasure，方便获取path内的matrix用
        pathMeasure.getMatrix(pathMeasure.getLength(), savedMatrix, PathMeasure.POSITION_MATRIX_FLAG & PathMeasure.TANGENT_MATRIX_FLAG);

        return savedMatrix;
    }

    public static PointF calPelCenterPoint(Pel selectedPel) {
        Rect boundRect = new Rect();
        selectedPel.getRegion().getBounds(boundRect);

        return new PointF((boundRect.right + boundRect.left) / 2, (boundRect.bottom + boundRect.top) / 2);
    }

    public static float distance(PointF begin, PointF end) {
        float x = begin.x - end.x;
        float y = begin.y - end.y;
        return (float) Math.sqrt(x * x + y * y);
    }
}
