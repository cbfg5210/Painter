package com.ue.pixel.util

import android.content.Context
import android.media.MediaScannerConnection
import android.os.Environment
import com.afollestad.materialdialogs.MaterialDialog
import com.ue.library.constant.Constants
import com.ue.library.util.toast
import com.ue.pixel.R
import java.io.File

/**
 * Created by BennyKok on 10/17/2016.
 */
class ExportingUtils private constructor() {

    private var currentProgressDialog: MaterialDialog? = null

    fun dismissAllDialogs() {
        currentProgressDialog?.dismiss()
    }

    fun checkAndCreateProjectDirs() = File(projectPath).apply { if (!exists()) mkdir() }

    fun toastAndFinishExport(context: Context, fileName: String?) {
        if (fileName.isNullOrEmpty()) context.toast(R.string.pi_export_failed)
        else {
            MediaScannerConnection.scanFile(context, arrayOf(fileName), null, null)
            context.toast(R.string.pi_export_successful)
        }
    }

    fun showProgressDialog(context: Context) {
        currentProgressDialog = DialogHelper.showProgressDialog(context)
    }

    companion object {
        val instance = ExportingUtils()

        val projectPath: String
            get() = Environment.getExternalStorageDirectory().path + Constants.PATH_PIXEL
    }
}