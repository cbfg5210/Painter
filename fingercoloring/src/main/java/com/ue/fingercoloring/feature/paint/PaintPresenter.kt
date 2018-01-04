package com.ue.fingercoloring.feature.paint

import android.graphics.Bitmap
import android.os.Environment
import android.support.v7.app.AppCompatActivity
import com.ue.fingercoloring.constant.Constants
import io.reactivex.Observable
import io.reactivex.ObservableOnSubscribe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * Created by hawk on 2017/12/24.
 */

class PaintPresenter(private val mPaintActivity: AppCompatActivity) {

    fun saveImageLocally(bmp: Bitmap, paintName: String, listener: OnSaveImageListener) {
        Observable
                .create(ObservableOnSubscribe<String> { e ->
                    val path = Environment.getExternalStorageDirectory().path + Constants.FOLDER_WORKS
                    val dir = File(path)
                    if (!dir.exists()) dir.mkdirs()

                    val file = File(dir, paintName)
                    var out: FileOutputStream? = null
                    try {
                        out = FileOutputStream(file)
                        // bmp is your Bitmap instance
                        bmp.compress(Bitmap.CompressFormat.PNG, 100, out)
                        out.close()
                        e.onNext(path + paintName)
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
                    if (!mPaintActivity.isFinishing){
                        listener.onSaved(s)
                    }
                })
    }

    interface OnSaveImageListener {
        fun onSaved(path: String)
    }
}
