package com.ue.graffiti.model;

import android.graphics.Matrix;
import android.graphics.Region;

import java.util.List;

/**
 * Created by hawk on 2018/1/19.
 */

public class TransformPelStep extends Step {
    //变换前的matrix
    public Matrix toUndoMatrix;
    public Region clipRegion;
    public Pel savedPel;

    public TransformPelStep(List<Pel> pelList, Region clipRegion, Pel pel) {
        super(pelList, pel);
        toUndoMatrix = new Matrix();
        savedPel = curPel.clone();
        this.clipRegion = clipRegion;
    }

    public void setToUndoMatrix(Matrix matrix) {
        toUndoMatrix.set(matrix);
    }
}
