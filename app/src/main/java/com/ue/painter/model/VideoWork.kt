package com.ue.painter.model

/**
 * Created by hawk on 2018/1/25.
 */
class VideoWork(cat: Int, name: String, path: String, lastModTime: Long) : Work(cat, name, path, lastModTime) {
    var picPath: String? = null
}