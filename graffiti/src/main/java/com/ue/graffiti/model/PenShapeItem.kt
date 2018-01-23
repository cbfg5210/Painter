package com.ue.graffiti.model

import com.ue.adapterdelegate.Item

/**
 * Created by hawk on 2018/1/17.
 */

class PenShapeItem(var flag: Int, var image: Int, var name: String) : Item {
    //多type RecyclerView中有作用
    var index: Int = 0
    var isChecked: Boolean = false
}
