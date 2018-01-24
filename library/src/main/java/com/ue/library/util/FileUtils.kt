package com.ue.library.util

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.graphics.Bitmap
import com.ue.library.R
import io.reactivex.Observable
import io.reactivex.ObservableOnSubscribe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * Created by hawk on 2018/1/10.
 */
object FileUtils {
    fun saveImageLocally(context: Context, bmp: Bitmap, path: String, imgName: String, listener: OnSaveImageListener) {
        Observable
                .create(ObservableOnSubscribe<String> { e ->
                    val dir = File(path)
                    if (dir.exists() && !dir.isDirectory) {
                        dir.delete()
                    }
                    if (!dir.exists()) dir.mkdirs()

                    val file = File(dir, imgName)
                    if (file.exists()) {
                        //询问用户是否覆盖提示框
                        AlertDialog.Builder(context)
                                .setMessage(R.string.name_conflict)
                                .setPositiveButton(R.string.cover) { _, _ -> e.onNext(saveImage(file, bmp, path, imgName)) }
                                .setNegativeButton(R.string.cancel, null)
                                .create()
                                .show()
                    } else {
                        e.onNext(saveImage(file, bmp, path, imgName))
                    }
                })
                .subscribeOn(Schedulers.single())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ s ->
                    if (context is Activity && !context.isFinishing) {
                        listener.onSaved(s)
                    }
                })
    }

    private fun saveImage(file: File, bmp: Bitmap, path: String, imgName: String): String {
        var out: FileOutputStream? = null
        var finalPath = ""
        try {
            out = FileOutputStream(file)
            bmp.compress(Bitmap.CompressFormat.PNG, 100, out)
            finalPath = path + imgName
            out.close()
        } catch (exp: Exception) {
        } finally {
            if (out != null) {
                try {
                    out?.close()
                } catch (e1: IOException) {
                }
            }
        }
        return finalPath
    }

    interface OnSaveImageListener {
        fun onSaved(path: String)
    }
}