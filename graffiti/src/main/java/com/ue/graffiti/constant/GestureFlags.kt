package com.ue.graffiti.constant

/**
 * Created by hawk on 2018/1/18.
 */

object GestureFlags {
    val MIN_ZOOM = 10f //缩放下限
    val MAX_DY = 70f //缩放与旋转切换上限
    val NONE = 0 // 平移操作
    val DRAG = 1 // 平移操作
    val ZOOM = 2 // 缩放操作
    val ROTATE = 3 // 旋转操作
}