package com.ue.graffiti.model

import android.graphics.Matrix
import android.graphics.Region

/**
 * Created by hawk on 2018/1/19.
 */

class TransformPelStep(pelList: List<Pel>, var clipRegion: Region, pel: Pel) : Step(pelList, pel) {
    //变换前的matrix
    var toUndoMatrix: Matrix
    var savedPel: Pel

    init {
        toUndoMatrix = Matrix()
        savedPel = curPel.clone()
    }

    fun setUndoMatrix(matrix: Matrix) {
        toUndoMatrix.set(matrix)
    }
}
