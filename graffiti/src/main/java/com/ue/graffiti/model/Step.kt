package com.ue.graffiti.model

/**
 * Created by hawk on 2018/1/19.
 */
// 图元链表
//最早放入undo的图元
open class Step(var pelList: MutableList<Pel>, var curPel: Pel?)