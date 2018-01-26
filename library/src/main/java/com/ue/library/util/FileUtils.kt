package com.ue.library.util

import android.content.Context
import android.graphics.Bitmap
import android.widget.Toast
import com.ue.library.R
import com.ue.library.event.SimplePermissionListener
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * Created by hawk on 2018/1/10.
 */
object FileUtils {

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

                        saveImage(context, bmp, "$path$workName.png", listener, showToast!!)
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
            if (showToast) Toast.makeText(context, context.getString(R.string.save_to, finalPath), Toast.LENGTH_LONG).show()
        } catch (exp: Exception) {
            if (showToast) Toast.makeText(context, context.getString(R.string.save_error_reason, exp.message), Toast.LENGTH_LONG).show()
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