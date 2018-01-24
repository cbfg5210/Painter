package com.ue.graffiti.model

import android.graphics.Matrix
import android.graphics.Region

/**
 * Created by hawk on 2018/1/19.
 */

class TransformPelStep(pelList: MutableList<Pel>, var clipRegion: Region, pel: Pel) : Step(pelList, pel) {
    //变换前的matrix
    var toUndoMatrix: Matrix = Matrix()
    var savedPel: Pel = curPel!!.clone()

    fun setUndoMatrix(matrix: Matrix) {
        toUndoMatrix.set(matrix)
    }
}
