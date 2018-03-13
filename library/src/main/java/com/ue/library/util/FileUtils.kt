package com.ue.library.util

import android.content.Context
import android.graphics.Bitmap
import com.ue.library.R
import com.ue.library.constant.FileTypes
import com.ue.library.event.SimplePermissionListener
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * Created by hawk on 2018/1/10.
 */
object FileUtils {

    fun deleteFile(savedPicturePath: String): Boolean {
        val file = File(savedPicturePath)
        return if (file.exists()) file.delete() else false
    }

    fun saveImageLocally(context: Context, bmp: Bitmap, path: String, workName: String, listener: OnSaveImageListener? = null, showToast: Boolean? = true) {
        PermissionUtils.checkReadWriteStoragePerms(context,
                context.getString(R.string.save_error_no_perm),
                object : SimplePermissionListener() {
                    override fun onSucceed(requestCode: Int, grantPermissions: MutableList<String>) {
                        val dir = File(path)
                        if (dir.exists() && !dir.isDirectory) {
                            dir.delete()
                        }
                        if (!dir.exists()) dir.mkdirs()

                        saveImage(context, bmp, "$path$workName${FileTypes.PNG}", listener, showToast!!)
                    }
                })
    }

    private fun saveImage(context: Context, bmp: Bitmap, path: String, listener: OnSaveImageListener?, showToast: Boolean) {
        var out: FileOutputStream? = null
        var finalPath = ""
        try {
            out = FileOutputStream(File(path))
            bmp.compress(Bitmap.CompressFormat.PNG, 100, out)
            finalPath = path
            if (showToast) context.toast(context.getString(R.string.save_to, finalPath))
        } catch (exp: Exception) {
            if (showToast) context.toast(context.getString(R.string.save_error_reason, exp.message))
        } finally {
            try {
                out?.close()
            } catch (e1: IOException) {
            }
        }
        listener?.onSaved(finalPath)
    }

    interface OnSaveImageListener {
        fun onSaved(path: String)
    }
}