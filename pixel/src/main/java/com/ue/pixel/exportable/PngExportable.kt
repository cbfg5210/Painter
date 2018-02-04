package com.ue.pixel.exportable

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.util.Log
import com.ue.library.util.bindUtilDestroy
import com.ue.pixel.util.ExportingUtils
import com.ue.pixel.util.Tool
import com.ue.pixel.widget.PxerView
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * Created by BennyKok on 10/17/2016.
 */

class PngExportable : Exportable() {
    override fun runExport(context: Context, name: String, pxerView: PxerView) {
        ExportingUtils.instance.showExportingDialog(context, name, pxerView.picWidth, pxerView.picHeight, object : ExportingUtils.OnExportConfirmedListener {
            override fun onExportConfirmed(fileName: String, width: Int, height: Int) {

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
}