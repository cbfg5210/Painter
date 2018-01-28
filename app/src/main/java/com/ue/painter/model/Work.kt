package com.ue.painter.model

/**
 * Created by hawk on 2018/1/28.
 */
open class Work(cat: Int, name: String, path: String, lastModTime: Long) {
    val cat = cat
    val name = name
    val path = path
    var lastModTime = lastModTime
    var wvHRadio: Float = 0f
}