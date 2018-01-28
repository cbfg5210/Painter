package com.ue.library.constant

/**
 * Created by hawk on 2018/1/28.
 */
object FileTypes {
    const val PNG = ".png"
    const val MP4 = ".mp4"

    fun getRawName(fileName: String): String {
        val lastDot = fileName.lastIndexOf(".")
        return fileName.substring(0, lastDot)
    }
}