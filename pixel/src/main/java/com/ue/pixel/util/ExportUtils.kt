package com.ue.pixel.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.util.Log
import com.ue.library.util.bindUtilDestroy
import com.ue.pixel.event.OnProjectInfoListener
import com.ue.pixel.gifencoder.AnimatedGifEncoder
import com.ue.pixel.widget.PxerView
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * Created by hawk on 2018/2/5.
 */
object ExportUtils {
    fun exportAsPng(context: Context, name: String, pxerView: PxerView) {
        DialogHelper.showExportingDialog(context, name, pxerView.picWidth, pxerView.picHeight, object : OnProjectInfoListener {
            override fun onProjectInfo(fileName: String, width: Int, height: Int) {

                ExportingUtils.instance.showProgressDialog(context)
                Observable
                        .create<Any> {
                            val paint = Paint()
                            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                            val canvas = Canvas(bitmap)
                            (0 until pxerView.pxerLayers.size)
                                    .filter { pxerView.pxerLayers[it].visible }
                                    .forEach { canvas.drawBitmap(pxerView.pxerLayers[it].bitmap, null, Rect(0, 0, width, height), paint) }

                            val file = File(ExportingUtils.instance.checkAndCreateProjectDirs(), fileName + ".png")
                            var out: FileOutputStream? = null
                            try {
                                file.createNewFile()
                                out = FileOutputStream(file)
                                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                                out.flush()

                                it.onNext(file.path)
                            } catch (e: Exception) {
                                Log.e("PngExportable", "onExportConfirmed: error:${e.message}")
                                it.onNext("")
                            } finally {
                                if (out != null) {
                                    try {
                                        out.close()
                                    } catch (e: IOException) {
                                        Log.e("PngExportable", "onExportConfirmed: error:${e.message}")
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

    fun exportAsGif(context: Context, name: String, pxerView: PxerView) {
        DialogHelper.showExportingDialog(context, name, pxerView.picWidth, pxerView.picHeight, object : OnProjectInfoListener {
            override fun onProjectInfo(name: String, width: Int, height: Int) {
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

                            val file = File(ExportingUtils.instance.checkAndCreateProjectDirs(), name + ".gif")
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