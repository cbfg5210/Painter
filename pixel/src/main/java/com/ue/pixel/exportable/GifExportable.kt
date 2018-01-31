package com.ue.pixel.exportable

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.os.AsyncTask
import com.ue.pixel.gifencoder.AnimatedGifEncoder
import com.ue.pixel.util.ExportingUtils
import com.ue.pixel.util.Tool
import com.ue.pixel.widget.PxerView
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

/**
 * Created by BennyKok on 10/17/2016.
 */

class GifExportable : Exportable() {
    override fun runExport(context: Context, name: String, pxerView: PxerView) {
        ExportingUtils.instance.showExportingDialog(context, name, pxerView.picWidth, pxerView.picHeight, object : ExportingUtils.OnExportConfirmedListenser {
            override fun OnExportConfirmed(fileName: String, width: Int, height: Int) {
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
                val finalgif = bos.toByteArray()
                //Finish giffing

                val file = File(ExportingUtils.instance.checkAndCreateProjectDirs(), fileName + ".gif")

                ExportingUtils.instance.showProgressDialog(context)

                object : AsyncTask<Void, Void, Void>() {
                    override fun doInBackground(vararg params: Void): Void? {
                        try {
                            file.createNewFile()
                            val out = FileOutputStream(file)
                            out.write(finalgif)
                            out.flush()
                            out.close()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }

                        return null
                    }

                    override fun onPostExecute(aVoid: Void?) {
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