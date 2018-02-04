package com.ue.pixel.exportable

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.util.Log
import com.ue.library.util.bindUtilDestroy
import com.ue.pixel.gifencoder.AnimatedGifEncoder
import com.ue.pixel.util.ExportingUtils
import com.ue.pixel.util.Tool
import com.ue.pixel.widget.PxerView
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * Created by BennyKok on 10/17/2016.
 */

class GifExportable : Exportable() {
    override fun runExport(context: Context, name: String, pxerView: PxerView) {
        ExportingUtils.instance.showExportingDialog(context, name, pxerView.picWidth, pxerView.picHeight, object : ExportingUtils.OnExportConfirmedListener {
            override fun onExportConfirmed(fileName: String, width: Int, height: Int) {
                ExportingUtils.instance.showProgressDialog(context)
                Observable
                        .create<Any> {
                            val paint = Paint()
                            val canvas = Canvas()
                            //Make gif
                            val bos = ByteArrayOutputStream()
                            val encoder = AnimatedGifEncoder()
                            encoder.start(bos)
                            for (i in 0 until pxerView.pxerLayers.size) {
                                val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                                canvas.setBitmap(bitmap)
                                canvas.drawBitmap(pxerView.pxerLayers[i].bitmap, null, Rect(0, 0, width, height), paint)
                                encoder.addFrame(bitmap)
                            }
                            encoder.finish()
                            val finalGif = bos.toByteArray()

                            val file = File(ExportingUtils.instance.checkAndCreateProjectDirs(), fileName + ".gif")
                            var out: FileOutputStream? = null

                            try {
                                file.createNewFile()
                                out = FileOutputStream(file)
                                out.write(finalGif)
                                out.flush()

                                it.onNext(file.path)
                            } catch (e: Exception) {
                                Log.e("GifExportable", "onExportConfirmed: error:${e.message}")
                                it.onNext("")
                            } finally {
                                if (out != null) {
                                    try {
                                        out.close()
                                    } catch (e: IOException) {
                                        Log.e("GifExportable", "onExportConfirmed: error:${e.message}")
                                    }
                                }
                            }
                            it.onComplete()
                        }
                        .subscribeOn(Schedulers.single())
                        .observeOn(AndroidSchedulers.mainThread())
                        .bindUtilDestroy(context)
                        .subscribe {
                            ExportingUtils.instance.dismissAllDialogs()
                            ExportingUtils.instance.toastAndFinishExport(context, it as String)
                            Tool.freeMemory()
                        }
            }
        })
    }
}