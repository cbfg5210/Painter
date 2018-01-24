package com.ue.graffiti.model

import android.graphics.Paint

/**
 * Created by hawk on 2018/1/19.
 */

class FillPelStep(pelList: MutableList<Pel>, pel: Pel, oldPaint: Paint, newPaint: Paint) : Step(pelList, pel) {
    var oldPaint: Paint = Paint(oldPaint)
    var newPaint: Paint = Paint(newPaint)

}
