package com.ue.pixel.util

import android.content.Context
import android.media.MediaScannerConnection
import android.os.Environment
import com.afollestad.materialdialogs.MaterialDialog
import com.ue.library.constant.Constants
import com.ue.library.util.toast
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
        currentProgressDialog = DialogHelper.showProgressDialog(context)
    }

    interface OnExportConfirmedListener {
        fun onExportConfirmed(fileName: String, width: Int, height: Int)
    }

    companion object {
        val instance = ExportingUtils()

        val projectPath: String
            get() = Environment.getExternalStorageDirectory().path + Constants.PATH_PIXEL
    }
}