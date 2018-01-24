package com.ue.library.util

import android.app.AlertDialog
import android.content.Context
import android.graphics.Bitmap
import android.widget.Toast
import com.ue.library.R
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * Created by hawk on 2018/1/10.
 */
object FileUtils {

    fun saveImageLocally(context: Context, bmp: Bitmap, path: String, workName: String, listener: OnSaveImageListener? = null) {
        PermissionUtils.checkReadWriteStoragePerms(context,
                context.getString(R.string.save_error_no_perm),
                object : PermissionUtils.SimplePermissionListener {
                    override fun onSucceed(requestCode: Int, grantPermissions: List<String>) {
                        goSaveImage(context, bmp, path, "$workName.png", listener)
                    }
                })
    }

    private fun goSaveImage(context: Context, bmp: Bitmap, path: String, imgName: String, listener: OnSaveImageListener? = null) {
        val dir = File(path)
        if (dir.exists() && !dir.isDirectory) {
            dir.delete()
        }
        if (!dir.exists()) dir.mkdirs()

        val file = File(dir, imgName)
        if (!file.exists()) {
            saveImage(context, file, bmp, path, imgName, listener)
            return
        }
        //询问用户是否覆盖提示框
        AlertDialog.Builder(context)
                .setMessage(R.string.name_conflict)
                .setPositiveButton(R.string.cover) { _, _ -> saveImage(context, file, bmp, path, imgName, listener) }
                .setNegativeButton(R.string.cancel, null)
                .create()
                .show()
    }

    private fun saveImage(context: Context, file: File, bmp: Bitmap, path: String, imgName: String, listener: OnSaveImageListener?) {
        var out: FileOutputStream? = null
        var finalPath = ""
        try {
            out = FileOutputStream(file)
            bmp.compress(Bitmap.CompressFormat.PNG, 100, out)
            finalPath = path + imgName
            Toast.makeText(context, context.getString(R.string.save_to, finalPath), Toast.LENGTH_LONG).show()
        } catch (exp: Exception) {
            Toast.makeText(context, context.getString(R.string.save_error_reason, exp.message), Toast.LENGTH_LONG).show()
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