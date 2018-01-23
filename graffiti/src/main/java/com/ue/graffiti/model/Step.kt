package com.ue.graffiti.model

/**
 * Created by hawk on 2018/1/19.
 */

open class Step(// 图元链表
        var pelList: List<Pel>, //最早放入undo的图元
        var curPel: Pel)