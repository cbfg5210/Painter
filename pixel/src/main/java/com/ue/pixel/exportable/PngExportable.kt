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

class PngExportable : Exportable() {
    override fun runExport(context: Context, name: String, pxerView: PxerView) {
        ExportingUtils.instance.showExportingDialog(context, name, pxerView.picWidth, pxerView.picHeight, object : ExportingUtils.OnExportConfirmedListenser {
            override fun OnExportConfirmed(fileName: String, width: Int, height: Int) {
                val paint = Paint()
                val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bitmap)
                for (i in 0 until pxerView.pxerLayers.size) {
                    if (pxerView.pxerLayers[i].visible){
                        canvas.drawBitmap(pxerView.pxerLayers[i].bitmap, null, Rect(0, 0, width, height), paint)
                    }
                }

                val file = File(ExportingUtils.instance.checkAndCreateProjectDirs(), fileName + ".png")

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