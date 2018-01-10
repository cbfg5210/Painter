package com.ue.autodraw.dialog

import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.widget.AdapterView
import android.widget.Toast
import com.ue.autodraw.R
import kotlinx.android.synthetic.main.dialog_tab_bg.view.*

/**
 * Created by hawk on 2018/1/10.
 */
class TabBgDialog : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val rootView = LayoutInflater.from(context).inflate(R.layout.dialog_tab_bg, null)
        val adapter = BgAdapter(context, intArrayOf(
                R.drawable.fs0, R.drawable.fs1,
                R.drawable.fs2, R.drawable.fs3,
                R.drawable.fs4, R.drawable.fs5,
                R.drawable.fs6, R.drawable.fs7,
                R.drawable.fs8, R.drawable.fs9, R.drawable.fs10))
        adapter.itemListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            Toast.makeText(context, "bg item$position", Toast.LENGTH_SHORT).show()
        }
        rootView.rvBgOptions.setHasFixedSize(true)
        rootView.rvBgOptions.adapter = adapter

        return AlertDialog.Builder(context)
                .setTitle(R.string.background)
                .setView(rootView)
                .create()
    }
}