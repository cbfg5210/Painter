package com.ue.library.widget

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v4.app.FragmentManager
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import com.ue.library.R
import kotlinx.android.synthetic.main.dialog_loading.view.*

/**
 * Created by hawk on 2018/1/13.
 */
class LoadingDialog() : DialogFragment() {

    companion object {
        fun newInstance(): LoadingDialog {
            val dialog = LoadingDialog()
            dialog.setStyle(DialogFragment.STYLE_NO_TITLE, 0)
            return dialog
        }
    }

    private lateinit var rootView: View
    private var loadingTip = ""
        set(value) {
            field = value
            if (isVisible) {
                rootView.tvLoadingTip.text = value
            }
        }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        rootView = LayoutInflater.from(context).inflate(R.layout.dialog_loading, null)
        rootView.tvLoadingTip.text = loadingTip

        val dialog = AlertDialog.Builder(context)
                .setView(rootView)
                .create()
        dialog.window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        return dialog
    }

    fun showLoading(manager: FragmentManager?, loadingTip: String) {
        this.loadingTip = loadingTip
        show(manager, "")
        if (isVisible) {
            rootView.tvLoadingTip.text = loadingTip
        }
    }
}