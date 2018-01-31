package com.ue.pixel.exportable

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.os.AsyncTask
import com.ue.pixel.util.ExportingUtils
import com.ue.pixel.util.Tool
import com.ue.pixel.widget.PxerView
import java.io.File
import java.io.FileOutputStream

/**
 * Created by BennyKok on 10/17/2016.
 */

class AtlasExportable : Exportable() {
    override fun runExport(context: Context, name: String, pxerView: PxerView) {
        ExportingUtils.instance.showExportingDialog(context, 2048, name, pxerView.picWidth, pxerView.picHeight, object : ExportingUtils.OnExportConfirmedListenser {
            override fun OnExportConfirmed(fileName: String, width: Int, height: Int) {
                val paint = Paint()
                val canvas = Canvas()

                val atlasWidth = Math.ceil((pxerView.pxerLayers.size.toFloat() / Math.sqrt(pxerView.pxerLayers.size.toFloat().toDouble()).toFloat()).toDouble()).toInt()
                val atlasHeight = Math.ceil((pxerView.pxerLayers.size.toFloat() / atlasWidth.toFloat()).toDouble()).toInt()

                val bitmap = Bitmap.createBitmap(width * atlasWidth, height * atlasHeight, Bitmap.Config.ARGB_8888)
                canvas.setBitmap(bitmap)

                var counter = 0
                for (y in 0 until atlasHeight) {
                    for (x in 0 until atlasWidth) {
                        if (pxerView.pxerLayers.size > counter) {
                            canvas.drawBitmap(pxerView.pxerLayers[counter].bitmap, null, Rect(width * x, height * y, width * (x + 1), height * (y + 1)), paint)
                        }
                        counter++
                    }
                }

                val file = File(ExportingUtils.instance.checkAndCreateProjectDirs(), fileName + "_Atlas" + ".png")

                ExportingUtils.instance.showProgressDialog(context)

                object : AsyncTask<Void, Void, Void>() {
                    override fun doInBackground(vararg params: Void): Void? {
                        try {
                            file.createNewFile()
                            val out = FileOutputStream(file)
                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                            out.flush()
                            out.close()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                        return null
                    }

                    override fun onPostExecute(aVoid: Void) {
                        ExportingUtils.instance.dismissAllDialogs()
                        ExportingUtils.instance.toastAndFinishExport(context, file.toString())
                        Tool.freeMemory()
                        super.onPostExecute(aVoid)
                    }
                }.execute()
            }
        })
    }
}