package com.ue.pixel.util

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.support.constraint.ConstraintLayout
import android.text.InputType
import android.view.LayoutInflater
import android.widget.SeekBar
import com.afollestad.materialdialogs.GravityEnum
import com.afollestad.materialdialogs.MaterialDialog
import com.ue.library.event.OnTextInputListener
import com.ue.library.util.toast
import com.ue.pixel.R
import com.ue.pixel.event.OnProjectInfoListener
import kotlinx.android.synthetic.main.pi_dialog_export.view.*
import kotlinx.android.synthetic.main.pi_dialog_new_project.view.*

/**
 * Created by hawk on 2018/2/6.
 */
object DialogHelper {
    val myType = Typeface.create("sans-serif-light", Typeface.NORMAL)

    fun prompt(c: Context): MaterialDialog.Builder {
        return MaterialDialog.Builder(c)
                .negativeText(R.string.cancel)
                .titleGravity(GravityEnum.CENTER)
                .typeface(myType, myType)
                .positiveColor(Color.RED)
    }

    fun promptTextInput(c: Context, title: String): MaterialDialog.Builder {
        return MaterialDialog.Builder(c)
                .negativeText(R.string.cancel)
                .positiveText(R.string.sure)
                .title(title)
                .inputType(InputType.TYPE_CLASS_TEXT)
                .inputRange(0, 20)
                .titleGravity(GravityEnum.CENTER)
                .typeface(myType, myType)
                .positiveColor(Color.GREEN)
    }

    fun showSaveProjectDialog(context: Context, callback: OnTextInputListener) {
        MaterialDialog.Builder(context)
                .titleGravity(GravityEnum.CENTER)
                .typeface(DialogHelper.myType, DialogHelper.myType)
                .inputRange(0, 20)
                .title(R.string.pi_save_project)
                .input(context.getString(R.string.pi_name), null, false) { _, _ -> }
                .inputType(InputType.TYPE_CLASS_TEXT)
                .positiveText(R.string.save)
                .onPositive { dialog, _ ->
                    callback.onTextInput(dialog.inputEditText!!.text.toString())
                }
                .show()
    }

    fun showDeleteLayerDialog(context: Context, callback: MaterialDialog.SingleButtonCallback) {
        prompt(context)
                .title(R.string.pi_delete_layer)
                .content(R.string.pi_tip_delete_layer)
                .positiveText(R.string.delete)
                .onPositive(callback)
                .show()
    }

    fun showMergeAllLayersDialog(context: Context, callback: MaterialDialog.SingleButtonCallback) {
        prompt(context)
                .title(R.string.pi_merge_all_layers)
                .content(R.string.pi_tip_merge_all_layers)
                .positiveText(R.string.pi_merge)
                .onPositive(callback)
                .show()
    }

    fun showClearLayerDialog(context: Context, callback: MaterialDialog.SingleButtonCallback) {
        prompt(context)
                .title(R.string.pi_clear_current_layer)
                .content(R.string.pi_tip_clear_current_layer)
                .positiveText(R.string.pi_clear)
                .onPositive(callback)
                .show()
    }

    fun showMergeDownDialog(context: Context, callback: MaterialDialog.SingleButtonCallback) {
        prompt(context)
                .title(R.string.pi_merge_down_layer)
                .content(R.string.pi_tip_merge_down_layer)
                .positiveText(R.string.pi_merge)
                .onPositive(callback)
                .show()
    }

    fun showNewProjectDialog(context: Context, projectInfoListener: OnProjectInfoListener) {
        val l = LayoutInflater.from(context).inflate(R.layout.pi_dialog_new_project, null) as ConstraintLayout
        val etNewPixelName = l.etNewPixelName
        val sbWidthBar = l.sbWidthBar
        val tvWidthLab = l.tvWidthLab
        val sbHeightBar = l.sbHeightBar
        val tvHeightLab = l.tvHeightLab

        sbWidthBar.max = 127
        sbWidthBar.progress = 39
        tvWidthLab.text = "Width : 40"
        sbWidthBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                tvWidthLab.text = "Width : ${(i + 1)}"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        sbHeightBar.max = 127
        sbHeightBar.progress = 39
        tvHeightLab.text = "Height : 40"
        sbHeightBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                tvHeightLab.text = "Height : ${i + 1}"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        MaterialDialog.Builder(context)
                .titleGravity(GravityEnum.CENTER)
                .typeface(myType, myType)
                .customView(l, false)
                .title(R.string.pi_new_project)
                .positiveText(R.string.pi_create)
                .negativeText(R.string.cancel)
                .onPositive({ _, _ ->
                    projectInfoListener.onProjectInfo(etNewPixelName.text.toString().trim(), sbWidthBar.progress + 1, sbHeightBar.progress + 1)
                })
                .show()
    }

    fun showProgressDialog(context: Context): MaterialDialog {
        return MaterialDialog.Builder(context)
                .titleGravity(GravityEnum.CENTER)
                .typeface(DialogHelper.myType, DialogHelper.myType)
                .cancelable(false)
                .canceledOnTouchOutside(false)
                .title("Painting...")
                .progress(true, 0)
                .progressIndeterminateStyle(true)
                .show()
    }

    fun showExportingDialog(context: Context, projectName: String, picWidth: Int, picHeight: Int, listener: OnProjectInfoListener) {
        DialogHelper.showExportingDialog(context, -1, projectName, picWidth, picHeight, listener)
    }

    fun showExportingDialog(context: Context, maxSize: Int, projectName: String, picWidth: Int, picHeight: Int, listener: OnProjectInfoListener) {
        val l = LayoutInflater.from(context).inflate(R.layout.pi_dialog_export, null) as ConstraintLayout
        val editText = l.etExportPixelName
        val seekBar = l.sbSizeBar
        val textView = l.tvSizeLab

        editText.setText(projectName)

        if (maxSize == -1) seekBar.max = 4096 - picWidth
        else seekBar.max = maxSize - picWidth

        textView.text = "Size : $picWidth x $picHeight"
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                textView.text = "Size : ${i + picWidth} x ${i + picHeight}"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        MaterialDialog.Builder(context)
                .titleGravity(GravityEnum.CENTER)
                .typeface(DialogHelper.myType, DialogHelper.myType)
                .customView(l, false)
                .title("Export")
                .positiveText("Export")
                .negativeText("Cancel")
                .onPositive(MaterialDialog.SingleButtonCallback { _, _ ->
                    if (editText.text.toString().isEmpty()) {
                        context.toast("The file name cannot be empty!")
                        return@SingleButtonCallback
                    }
                    listener.onProjectInfo(editText.text.toString(), seekBar.progress + picWidth, seekBar.progress + picHeight)
                })
                .show()
    }
}