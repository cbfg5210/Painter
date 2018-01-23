package com.ue.graffiti.model

import android.graphics.Paint
import android.graphics.Path
import android.graphics.Region

class Pel {
    //图元类
    var path = Path() //路径
    var region = Region()//区域
    var paint = Paint()//画笔
    var text: Text? = null//文本
    var picture: Picture? = null//插画
    var closure = false//是否封闭
    //深拷贝
    fun clone(): Pel {
        val pel = Pel()
        pel.path.set(path)
        pel.region.set(region)
        pel.paint.set(paint)
        pel.closure = closure

        return pel
    }
}