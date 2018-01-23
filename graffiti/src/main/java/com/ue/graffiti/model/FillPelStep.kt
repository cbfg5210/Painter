package com.ue.graffiti.model

import android.graphics.Paint

/**
 * Created by hawk on 2018/1/19.
 */

class FillPelStep(pelList: MutableList<Pel>, pel: Pel, oldPaint: Paint, newPaint: Paint) : Step(pelList, pel) {
    var oldPaint: Paint
    var newPaint: Paint

    init {
        this.oldPaint = Paint(oldPaint)
        this.newPaint = Paint(newPaint)
    }
}
