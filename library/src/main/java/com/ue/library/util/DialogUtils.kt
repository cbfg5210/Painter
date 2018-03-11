package com.ue.library.util

import android.content.Context
import android.view.View
import com.afollestad.materialdialogs.MaterialDialog
import com.ue.library.R

/**
 * Created by hawk on 2018/1/14.
 */
object DialogUtils {

    fun getProgressDialog(context: Context, processTxt: String): MaterialDialog {
        return MaterialDialog.Builder(context)
                .progress(true, 100)
                .content(processTxt)
                .show()
    }

    fun showExitDialog(context: Context) {
        MaterialDialog.Builder(context)
                .title(R.string.sure_exit)
                .positiveText(R.string.cancel)
                .negativeText(R.string.sure)
                .show()
    }

    fun showExitDialogWithCheck(context: Context, checkTxt: String, defChecked: Boolean, callback: MaterialDialog.SingleButtonCallback) {
        MaterialDialog.Builder(context)
                .content(R.string.sure_exit)
                .checkBoxPrompt(checkTxt, defChecked, null)
                .positiveText(R.string.cancel)
                .negativeText(R.string.sure)
                .onNegative(callback)
                .show()
    }

    fun showOnceHintDialog(context: Context, titleRes: Int, hintRes: Int, positiveRes: Int, positiveListener: View.OnClickListener?, checkedSpKey: String) {
        showOnceHintDialog(context, titleRes, hintRes, positiveRes, positiveListener, 0, checkedSpKey)
    }

    fun showOnceHintDialog(context: Context,
                           titleRes: Int,
                           hintRes: Int,
                           positiveRes: Int,
                           positiveListener: View.OnClickListener?,
                           negativeRes: Int,
                           checkedSpKey: String) {

        if (!SPUtils.getBoolean(checkedSpKey, true)) {
            positiveListener?.onClick(null)
            return
        }

        val dialogBuilder = MaterialDialog.Builder(context)
                .title(titleRes)
                .content(hintRes)
                .checkBoxPrompt(context.getString(R.string.dont_show_next_time), false, null)
                .positiveText(positiveRes)
                .onPositive { dialog, _ ->
                    if (dialog.isPromptCheckBoxChecked) SPUtils.putBoolean(checkedSpKey, false)
                    positiveListener?.onClick(null)
                }

        if (negativeRes != 0) dialogBuilder.negativeText(context.getString(negativeRes))

        dialogBuilder.show()
    }

    fun showNormalDialog(context: Context,
                         titleRes: Int,
                         hintRes: Int,
                         positiveRes: Int,
                         positiveListener: MaterialDialog.SingleButtonCallback?,
                         negativeRes: Int,
                         negativeListener: MaterialDialog.SingleButtonCallback?) {

        val dialogBuilder = MaterialDialog.Builder(context)
        if (titleRes != 0) dialogBuilder.title(titleRes)
        if (hintRes != 0) dialogBuilder.content(hintRes)
        if (positiveRes != 0) {
            dialogBuilder.positiveText(positiveRes)
            if (positiveListener != null) dialogBuilder.onPositive(positiveListener)
        }
        if (negativeRes != 0) {
            dialogBuilder.negativeText(negativeRes)
            if (negativeListener != null) dialogBuilder.onNegative(negativeListener)
        }

        dialogBuilder.show()
    }
}