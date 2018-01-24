package com.ue.graffiti.model

/**
 * Created by hawk on 2018/1/19.
 */
//0:draw,1:copy,2:delete
class DrawPelStep(var flag: Int, pelList: MutableList<Pel>, pel: Pel) : Step(pelList, pel) {
    //图元所在链表位置
    var location = pelList.indexOf(pel)
}
