package com.ue.graffiti.model;

import android.graphics.Paint;

import java.util.List;

/**
 * Created by hawk on 2018/1/19.
 */

public class FillPelStep extends Step {
    public Paint oldPaint, newPaint;

    public FillPelStep(List<Pel> pelList, Pel pel, Paint oldPaint, Paint newPaint) {
        super(pelList, pel);
        this.oldPaint = new Paint(oldPaint);
        this.newPaint = new Paint(newPaint);
    }
}
