package com.ue.fingercoloring.widget

import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v4.app.FragmentManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import com.ue.fingercoloring.R

/**
 * Created by hawk on 2017/12/26.
 */

class TipDialog : DialogFragment() {
    private var tvTipTxt: TextView? = null
    private var tip: String? = null

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = LayoutInflater.from(context).inflate(R.layout.dialog_tip, null)
        tvTipTxt = rootView.findViewById(R.id.tvTipTxt)
        return rootView
    }

    override fun onResume() {
        super.onResume()
        if (tvTipTxt != null) {
            tvTipTxt!!.text = tip
        }
    }

    fun showTip(fragmentManager: FragmentManager, tip: String) {
        this.tip = tip
        if (isVisible) {
            if (tvTipTxt != null) {
                tvTipTxt!!.text = tip
            }
        } else {
            show(fragmentManager, "")
        }
    }

    companion object {

        fun newInstance(): TipDialog {
            val dialog = TipDialog()
            dialog.setStyle(DialogFragment.STYLE_NO_TITLE, R.style.TipDialogTheme)
            return dialog
        }
    }
}
