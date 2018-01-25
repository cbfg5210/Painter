package com.ue.coloring.util

import android.graphics.BitmapFactory
import android.os.Environment
import com.ue.coloring.constant.Constants
import com.ue.coloring.model.LocalWork
import java.io.File
import java.util.*

/**
 * Created by Swifty.Wang on 2015/9/1.
 */
object FileUtils {

    fun obtainLocalImages(): List<LocalWork> {
        val localWorks = ArrayList<LocalWork>()
        val path = Environment.getExternalStorageDirectory().getPath() + Constants.FOLDER_WORKS
        val f = File(path)
        if (f.listFiles() != null) {
            val file = f.listFiles()
            for (i in file.indices) {
                localWorks.add(LocalWork(file[i].name, "file://" + path + "/" + file[i].name, DateTimeUtil.formatTimeStamp(file[i].lastModified()), file[i].lastModified(), getDropboxIMGSize(file[i])))
            }
        }
        Collections.sort(localWorks) { localWork, t1 ->
            val diff = t1.lastModTimeStamp - localWork.lastModTimeStamp
            if (diff > 0) 1 else if (diff < 0) -1 else 0
        }
        return localWorks
    }

    fun deleteFile(savedPicturePath: String): Boolean {
        val file = File(savedPicturePath)
        return if (file.exists()) file.delete() else false
    }

    fun deleteWork(workName: String): Boolean {
        val path = Environment.getExternalStorageDirectory().getPath() + Constants.FOLDER_WORKS + workName
        val file = File(path)
        return if (file.exists()) file.delete() else false
    }

    private fun getDropboxIMGSize(file: File): Float {
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeFile(file.getPath(), options)
        val imageHeight = options.outHeight.toFloat()
        val imageWidth = options.outWidth.toFloat()
        return imageWidth / imageHeight
    }
}