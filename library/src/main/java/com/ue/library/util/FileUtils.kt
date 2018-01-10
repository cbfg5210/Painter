package com.ue.library.util

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
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
                    if (!dir.exists()) dir.mkdirs()

                    val file = File(dir, imgName)
                    var out: FileOutputStream? = null
                    try {
                        out = FileOutputStream(file)
                        // bmp is your Bitmap instance
                        bmp.compress(Bitmap.CompressFormat.PNG, 100, out)
                        out.close()
                        e.onNext(path + imgName)
                    } catch (exp: Exception) {
                        e.onNext("")
                    } finally {
                        try {
                            out?.close()
                        } catch (e1: IOException) {
                        }
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

    interface OnSaveImageListener {
        fun onSaved(path: String)
    }
}