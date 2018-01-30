package com.ue.autodraw

import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import com.ue.library.util.IntentUtils

/**
 * Created by hawk on 2018/1/14.
 */
class ShareVideoDialog : DialogFragment() {
    private var videoPath = ""
    private var shareDialog: AlertDialog? = null

    companion object {
        private val ARG_VIDEO_PATH = "arg_video_path"

        fun newInstance(videoPath: String): ShareVideoDialog {
            val dialog = ShareVideoDialog()
            val arguments = Bundle()
            arguments.putString(ARG_VIDEO_PATH, videoPath)
            dialog.arguments = arguments
            return dialog
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        videoPath = arguments.getString(ARG_VIDEO_PATH)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val shareDialog = AlertDialog.Builder(context)
                .setTitle(R.string.au_share_draw_video)
                .setMessage(R.string.au_share_draw_video_tip)
                .setPositiveButton(R.string.au_share, null)
                .setNegativeButton(R.string.au_preview, null)
                .setNeutralButton(R.string.au_cancel, null)
                .create()
        shareDialog.setCanceledOnTouchOutside(false)
        shareDialog.setCancelable(false)

        this.shareDialog = shareDialog

        return shareDialog
    }

    override fun onResume() {
        super.onResume()
        shareDialog!!.getButton(AlertDialog.BUTTON_POSITIVE)?.setOnClickListener { IntentUtils.shareVideo(context, videoPath) }
        shareDialog?.getButton(AlertDialog.BUTTON_NEGATIVE)?.setOnClickListener { IntentUtils.playVideo(context, videoPath) }
    }
}