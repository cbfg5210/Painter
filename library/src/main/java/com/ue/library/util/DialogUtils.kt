package com.ue.library.util

import android.content.Context
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import com.ue.library.R
import kotlinx.android.synthetic.main.layout_check_box.view.*

/**
 * Created by hawk on 2018/1/14.
 */
object DialogUtils {
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