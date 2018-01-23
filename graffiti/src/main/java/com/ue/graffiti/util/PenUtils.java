package com.ue.graffiti.util;

import android.graphics.BlurMaskFilter;
import android.graphics.CornerPathEffect;
import android.graphics.DashPathEffect;
import android.graphics.DiscretePathEffect;
import android.graphics.EmbossMaskFilter;
import android.graphics.MaskFilter;
import android.graphics.Matrix;
import android.graphics.Path;
import android.graphics.PathDashPathEffect;
import android.graphics.PathEffect;
import android.graphics.RectF;

import com.ue.graffiti.R;


/**
 * Created by hawk on 2018/1/23.
 */

public class PenUtils {
    /**
     * 线型
     *
     * @param image
     * @return
     */
    public static PathEffect getPaintShapeByImage(int image, int width, Matrix matrix) {
        if (matrix == null) {
            matrix = new Matrix();
            matrix.setSkew(2, 2);
        }
        switch (image) {
            case R.drawable.ic_line_solid:
                return new CornerPathEffect(10);
            case R.drawable.ic_line_dashed:
                return new DashPathEffect(new float[]{20, 20}, 0.5f);
            case R.drawable.ic_line_dotted:
                return new DashPathEffect(new float[]{40, 20, 10, 20}, 0.5f);
            case R.drawable.ic_line_lighting:
                return new DiscretePathEffect(5f, 9f);
            case R.drawable.ic_line_oval:
                Path p = new Path();
                p.addOval(new RectF(0, 0, width, width), Path.Direction.CCW);
                return new PathDashPathEffect(p, width + 10, 0, PathDashPathEffect.Style.ROTATE);
            case R.drawable.ic_line_rect:
                Path p3 = new Path();
                p3.addRect(new RectF(0, 0, width, width), Path.Direction.CCW);
                return new PathDashPathEffect(p3, width + 10, 0, PathDashPathEffect.Style.ROTATE);
            case R.drawable.ic_line_brush:
                Path p2 = new Path();
                p2.addRect(new RectF(0, 0, width, width), Path.Direction.CCW);
                p2.transform(matrix);
                return new PathDashPathEffect(p2, 2, 0, PathDashPathEffect.Style.TRANSLATE);
            case R.drawable.ic_line_mark_pen:
                Path p1 = new Path();
                p1.addArc(new RectF(0, 0, width + 4, width + 4), -90, 90);
                p1.addArc(new RectF(0, 0, width + 4, width + 4), 90, -90);
                return new PathDashPathEffect(p1, 2, 0, PathDashPathEffect.Style.TRANSLATE);
            default:
                return new CornerPathEffect(10);
        }
    }

    /**
     * 特效
     *
     * @param image
     * @return
     */
    public static MaskFilter getPaintEffectByImage(int image) {
        switch (image) {
            case R.drawable.ic_effect_solid:
                return null;
            case R.drawable.ic_effect_blur:
                return new BlurMaskFilter(8, BlurMaskFilter.Blur.NORMAL);
            case R.drawable.ic_effect_hollow:
                return new BlurMaskFilter(8, BlurMaskFilter.Blur.OUTER);
            case R.drawable.ic_effect_relievo:
                return new EmbossMaskFilter(new float[]{1, 1, 1}, 0.4f, 6, 3.5f);
            default:
                return null;
        }
    }
}