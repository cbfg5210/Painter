package com.ue.graffiti.helper

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.Paint
import android.support.v4.app.FragmentActivity
import android.view.LayoutInflater
import android.view.View
import com.ue.graffiti.R
import com.ue.graffiti.event.OnSingleResultListener
import com.ue.graffiti.ui.ColorPickerDialog
import com.ue.graffiti.ui.ColorPickerDialog.OnColorPickerListener
import com.ue.graffiti.ui.DrawPictureDialog
import com.ue.graffiti.ui.DrawTextDialog
import com.ue.graffiti.ui.PenDialog
import com.ue.graffiti.widget.CanvasView
import com.ue.library.util.SPUtils
import kotlinx.android.synthetic.main.gr_layout_check_box.view.*
import kotlinx.android.synthetic.main.gr_layout_input.view.*

/**
 * Created by hawk on 2018/1/17.
 */

object DialogHelper {
    fun showColorPickerDialog(activity: FragmentActivity, colorPickerListener: OnColorPickerListener) {
        val fragmentManager = activity.supportFragmentManager
        val tag = ColorPickerDialog::class.java.simpleName
        val fragment = fragmentManager.findFragmentByTag(tag)
        val colorPickerDialog = fragment as? ColorPickerDialog ?: ColorPickerDialog.newInstance()
        if (colorPickerDialog.isAdded) {
            return
        }
        colorPickerDialog.colorPickerListener = colorPickerListener
        colorPickerDialog.show(fragmentManager, tag)
    }

    fun showPenDialog(activity: FragmentActivity, paint: Paint) {
        val fragmentManager = activity.supportFragmentManager
        val tag = PenDialog::class.java.simpleName
        val fragment = fragmentManager.findFragmentByTag(tag)
        val penDialog = fragment as? PenDialog ?: PenDialog.newInstance()
        if (penDialog.isAdded) {
            return
        }
        penDialog.setCurrentPaint(paint)
        penDialog.show(fragmentManager, tag)
    }

    fun showDrawTextDialog(activity: FragmentActivity, cvGraffitiView: CanvasView, drawTextListener: DrawTextDialog.OnDrawTextListener) {
        val fragmentManager = activity.supportFragmentManager
        val tag = DrawTextDialog::class.java.simpleName
        val fragment = fragmentManager.findFragmentByTag(tag)
        val dialog: DrawTextDialog
        if (fragment == null) {
            dialog = DrawTextDialog.newInstance()
            dialog.setDrawTextListener(drawTextListener)
        } else {
            dialog = fragment as DrawTextDialog
        }
        dialog.setGraffitiInfo(cvGraffitiView)
        if (dialog.isAdded) {
            return
        }
        dialog.show(fragmentManager, tag)
    }

    fun showDrawPictureDialog(activity: FragmentActivity, cvGraffitiView: CanvasView, drawPictureListener: DrawPictureDialog.OnDrawPictureListener) {
        val fragmentManager = activity.supportFragmentManager
        val tag = DrawPictureDialog::class.java.simpleName
        val fragment = fragmentManager.findFragmentByTag(tag)
        val dialog = fragment as? DrawPictureDialog
                ?: DrawPictureDialog.newInstance().apply { this.drawPictureListener = drawPictureListener }
        dialog.setGraffitiInfo(cvGraffitiView)
        if (dialog.isAdded) {
            return
        }
        dialog.show(fragmentManager, tag)
    }

    fun showExitDialog(activity: Activity, saveListener: View.OnClickListener) {
        AlertDialog.Builder(activity)
                .setTitle(R.string.gr_exit)
                .setMessage(R.string.gr_exit_tip)
                .setPositiveButton(R.string.gr_exit_save) { _, _ -> saveListener.onClick(null) }
                .setNegativeButton(R.string.gr_cancel, null)
                .setNeutralButton(R.string.gr_exit_not_save) { _, _ -> activity.finish() }
                .create()
                .show()
    }

    fun showInputDialog(context: Context, tip: String, singleResultListener: OnSingleResultListener) {
        showInputDialog(context, tip, null, singleResultListener)
    }

    fun showInputDialog(context: Context, tip: String, defText: String?, singleResultListener: OnSingleResultListener) {
        val contentView = LayoutInflater.from(context).inflate(R.layout.gr_layout_input, null)
        val editTxt = contentView.etInput
        editTxt.setText(defText)
        AlertDialog.Builder(context)
                .setTitle(tip)
                .setView(contentView)
                .setPositiveButton(R.string.gr_ok) { _, _ -> singleResultListener.onResult(editTxt.text.toString()) }
                .setNegativeButton(R.string.gr_cancel, null)
                .create()
                .show()
    }

    fun showClearDialog(context: Context, okListener: DialogInterface.OnClickListener) {
        AlertDialog.Builder(context)
                .setMessage(R.string.gr_ensure_clear)
                .setPositiveButton(R.string.gr_ok, okListener)
                .setNegativeButton(R.string.gr_cancel, null)
                .create()
                .show()
    }

    fun showSensorDrawingDialog(context: Context, tip: String, okListener: DialogInterface.OnClickListener) {
        //实例化确认对话框
        AlertDialog.Builder(context)
                .setMessage(tip)
                .setPositiveButton(R.string.gr_ok, okListener)
                .setNegativeButton(R.string.gr_cancel, null)
                .create()
                .show()
    }

    fun showOnceHintDialog(context: Context, titleRes: Int, hintRes: Int, positiveRes: Int, spKey: String) {
        val showHint = SPUtils.getBoolean(spKey, true)
        if (!showHint) {
            return
        }
        val checkBoxLayout = LayoutInflater.from(context).inflate(R.layout.gr_layout_check_box, null)
        AlertDialog.Builder(context)
                .setTitle(titleRes)
                .setMessage(hintRes)
                .setPositiveButton(positiveRes) { _, _ ->
                    if (checkBoxLayout.cbCheck.isChecked) {
                        SPUtils.putBoolean(spKey, false)
                    }
                }
                .setView(checkBoxLayout)
                .create()
                .show()
    }
}