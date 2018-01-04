package com.ue.fingercoloring.widget

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Window
import android.widget.ProgressBar

class ProgressLoading(context: Context) : Dialog(context) {
    companion object {
        private var dialog: ProgressLoading? = null

        /**
         * press back button can dismiss progressdialog
         *
         * @param context
         * @param cancelable
         */
        fun show(context: Context, cancelable: Boolean) {
            val dialog = ProgressLoading(context)
            dialog.setCanceledOnTouchOutside(false)
            dialog.setCancelable(cancelable)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.window.setDimAmount(0f)
            dialog.setContentView(ProgressBar(context))
            dialog.window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            this.dialog = dialog

            dialog.show()
        }

        fun dismiss() {
            dialog?.dismiss()
        }

        fun setOnDismissListener(listener: DialogInterface.OnDismissListener) {
            dialog?.setOnDismissListener(listener)
        }
    }
}