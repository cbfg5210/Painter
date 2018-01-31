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
import java.util.*

/**
 * Created by BennyKok on 10/17/2016.
 */

class FolderExportable : Exportable() {
    override fun runExport(context: Context, name: String, pxerView: PxerView) {
        ExportingUtils.instance.showExportingDialog(context, name, pxerView.picWidth, pxerView.picHeight, object : ExportingUtils.OnExportConfirmedListenser {
            override fun OnExportConfirmed(fileName: String, width: Int, height: Int) {
                val paint = Paint()
                val canvas = Canvas()

                val pngs = ArrayList<File>()
                val bitmaps = ArrayList<Bitmap>()

                for (i in 0 until pxerView.pxerLayers.size) {
                    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                    canvas.setBitmap(bitmap)
                    canvas.drawBitmap(pxerView.pxerLayers[i].bitmap, null, Rect(0, 0, width, height), paint)
                    val file = File(ExportingUtils.instance.checkAndCreateProjectDirs(fileName), fileName + "_Frame_" + (i + 1).toString() + ".png")
                    pngs.add(file)
                    bitmaps.add(bitmap)
                }

                ExportingUtils.instance.showProgressDialog(context)

                object : AsyncTask<Void, Int, Void>() {
                    override fun doInBackground(vararg params: Void): Void? {
                        try {
                            for (i in pngs.indices) {
                                publishProgress(i)
                                pngs[i].createNewFile()
                                val out = FileOutputStream(pngs[i])
                                bitmaps[i].compress(Bitmap.CompressFormat.PNG, 100, out)
                                out.flush()
                                out.close()
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }

                        return null
                    }

                    override fun onProgressUpdate(vararg values: Int?) {
                        ExportingUtils.instance.currentProgressDialog?.setTitle("Working on frame ${values[0]!! + 1}")
                        super.onProgressUpdate(*values)
                    }

                    override fun onPostExecute(aVoid: Void) {
                        ExportingUtils.instance.dismissAllDialogs()
                        ExportingUtils.instance.toastAndFinishExport(context, null)
                        ExportingUtils.instance.scanAlotsOfFile(context, pngs)
                        Tool.freeMemory()
                        super.onPostExecute(aVoid)
                    }
                }.execute()
            }
        })
    }
}