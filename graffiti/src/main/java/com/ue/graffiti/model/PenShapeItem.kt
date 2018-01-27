package com.ue.graffiti.model

/**
 * Created by hawk on 2018/1/17.
 */

class PenShapeItem(var flag: Int, var image: Int, var name: String) {
    //多type RecyclerView中有作用
    var index = 0
    var isChecked: Boolean = false
}
