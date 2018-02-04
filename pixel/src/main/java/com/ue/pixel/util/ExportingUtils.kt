package com.ue.pixel.util

import android.content.Context
import android.media.MediaScannerConnection
import android.os.Environment
import android.support.constraint.ConstraintLayout
import android.view.LayoutInflater
import android.widget.SeekBar
import com.afollestad.materialdialogs.GravityEnum
import com.afollestad.materialdialogs.MaterialDialog
import com.ue.library.constant.Constants
import com.ue.library.util.toast
import com.ue.pixel.R
import kotlinx.android.synthetic.main.dialog_activity_drawing.view.*
import java.io.File

/**
 * Created by BennyKok on 10/17/2016.
 */
class ExportingUtils private constructor() {

    var currentProgressDialog: MaterialDialog? = null

    fun dismissAllDialogs() {
        currentProgressDialog?.dismiss()
    }

    fun checkAndCreateProjectDirs(): File {
        val path = Environment.getExternalStorageDirectory().path + Constants.PATH_PIXEL
        val dirs = File(path)
        if (!dirs.exists()) {
            dirs.mkdirs()
        }
        return dirs
    }

    fun checkAndCreateProjectDirs(extraFolder: String?): File {
        if (extraFolder == null || extraFolder.isEmpty()) return checkAndCreateProjectDirs()
        val path = Environment.getExternalStorageDirectory().path + Constants.PATH_PIXEL + extraFolder
        val dirs = File(path)
        if (!dirs.exists()) {
            dirs.mkdirs()
        }
        return dirs
    }

    fun toastAndFinishExport(context: Context, fileName: String?) {
        if (fileName.isNullOrEmpty()) context.toast("Exported failed")
        else {
            MediaScannerConnection.scanFile(context, arrayOf(fileName), null, null)
            context.toast("Exported successfully")
        }
    }

    fun scanAlotsOfFile(context: Context, files: List<File>) {
        val paths = arrayOfNulls<String>(files.size)
        for (i in files.indices) {
            paths[i] = files[i].toString()
        }
        MediaScannerConnection.scanFile(context, paths, null, null)
    }

    fun showProgressDialog(context: Context) {
        currentProgressDialog = MaterialDialog.Builder(context)
                .titleGravity(GravityEnum.CENTER)
                .typeface(Tool.myType, Tool.myType)
                .cancelable(false)
                .canceledOnTouchOutside(false)
                .title("Painting...")
                .progress(true, 0)
                .progressIndeterminateStyle(true)
                .show()
    }

    fun showExportingDialog(context: Context, projectName: String, picWidth: Int, picHeight: Int, listener: OnExportConfirmedListener) {
        showExportingDialog(context, -1, projectName, picWidth, picHeight, listener)
    }

    fun showExportingDialog(context: Context, maxSize: Int, projectName: String, picWidth: Int, picHeight: Int, listener: OnExportConfirmedListener) {
        val l = LayoutInflater.from(context).inflate(R.layout.dialog_activity_drawing, null) as ConstraintLayout
        val editText = l.et1
        val seekBar = l.sb
        val textView = l.tv2

        editText.setText(projectName)

        if (maxSize == -1) seekBar.max = 4096 - picWidth
        else seekBar.max = maxSize - picWidth

        textView.text = "Size : $picWidth x $picHeight"
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                textView.text = "Size : ${i + picWidth} x ${i + picHeight}"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}

            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        MaterialDialog.Builder(context)
                .titleGravity(GravityEnum.CENTER)
                .typeface(Tool.myType, Tool.myType)
                .customView(l, false)
                .title("Export")
                .positiveText("Export")
                .negativeText("Cancel")
                .onPositive(MaterialDialog.SingleButtonCallback { dialog, which ->
                    if (editText.text.toString().isEmpty()) {
                        context.toast("The file name cannot be empty!")
                        return@SingleButtonCallback
                    }
                    listener.onExportConfirmed(editText.text.toString(), seekBar.progress + picWidth, seekBar.progress + picHeight)
                })
                .show()
    }

    interface OnExportConfirmedListener {
        fun onExportConfirmed(fileName: String, width: Int, height: Int)
    }

    companion object {
        val instance = ExportingUtils()

        val exportPath: String
            get() = Environment.getExternalStorageDirectory().path + Constants.PATH_PIXEL

        val projectPath: String
            get() = Environment.getExternalStorageDirectory().path + Constants.PATH_PIXEL
    }
}