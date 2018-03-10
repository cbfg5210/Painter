package com.ue.library.util

import android.content.Context
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import com.afollestad.materialdialogs.MaterialDialog
import com.ue.library.R
import kotlinx.android.synthetic.main.layout_check_box.view.*

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
//                .content("Sure to exit?")
                .positiveText(R.string.sure)
                .negativeText(R.string.cancel)
                .show()
    }

    fun showExitDialogWithCheck(context: Context, checkTxt: String, defChecked: Boolean, callback: MaterialDialog.SingleButtonCallback) {
        MaterialDialog.Builder(context)
                .content(R.string.sure_exit)
                .checkBoxPrompt(checkTxt, defChecked, null)
                .positiveText(R.string.sure)
                .onPositive(callback)
                .negativeText(R.string.cancel)
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
        val checkBoxLayout = LayoutInflater.from(context).inflate(R.layout.layout_check_box, null)
        val negativeBtnTxt = if (negativeRes == 0) null else context.getString(negativeRes)

        AlertDialog.Builder(context)
                .setTitle(titleRes)
                .setMessage(hintRes)
                .setPositiveButton(positiveRes) { _, _ ->
                    if (checkBoxLayout.cbCheck.isChecked) SPUtils.putBoolean(checkedSpKey, false)
                    positiveListener?.onClick(null)
                }
                .setNegativeButton(negativeBtnTxt, null)
                .setView(checkBoxLayout)
                .create()
                .show()
    }
}