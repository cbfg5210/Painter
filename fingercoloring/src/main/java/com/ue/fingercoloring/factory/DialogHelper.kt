package com.ue.fingercoloring.factory

import android.content.Context
import android.support.v7.app.AlertDialog
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.widget.SeekBar
import android.widget.Toast
import com.ue.fingercoloring.R
import com.ue.fingercoloring.constant.SPKeys
import com.ue.fingercoloring.event.OnAddWordsSuccessListener
import com.ue.fingercoloring.event.OnChangeBorderListener
import com.ue.fingercoloring.widget.ColorPickerSeekBar
import com.ue.library.util.DensityUtil
import com.ue.library.util.SPUtils
import kotlinx.android.synthetic.main.layout_check_box.view.*
import kotlinx.android.synthetic.main.view_addborder.view.*
import kotlinx.android.synthetic.main.view_addwords.view.*

/**
 * Created by Swifty.Wang on 2015/6/12.
 */
class DialogHelper(private val context: Context) {

    fun showAddWordsDialog(onAddWordsSuccessListener: OnAddWordsSuccessListener) {
        val layout = LayoutInflater.from(context).inflate(R.layout.view_addwords, null)

        layout.radiogroup.setOnCheckedChangeListener { _, i ->
            layout.addeditwords.setTextSize(TypedValue.COMPLEX_UNIT_SP,
                    when (i) {
                        R.id.middle -> DragTextViewFactory.getInstance().MiddleTextSize.toFloat()
                        R.id.large -> DragTextViewFactory.getInstance().BigTextSize.toFloat()
                        R.id.huge -> DragTextViewFactory.getInstance().HugeSize.toFloat()
                        else -> DragTextViewFactory.getInstance().SmallTextSize.toFloat()
                    })
        }

        layout.cpPaletteColorPicker.setOnColorSeekbarChangeListener(object : ColorPickerSeekBar.SimpleColorSeekBarChangeListener() {
            override fun onColorChanged(seekBar: SeekBar, color: Int, b: Boolean) {
                layout.addeditwords.setTextColor(color)
            }
        })

        AlertDialog.Builder(context)
                .setTitle(R.string.addtext)
                .setPositiveButton(R.string.ok) { _, _ ->
                    if (layout.addeditwords.text.toString().trim { it <= ' ' }.isEmpty()) {
                        Toast.makeText(context, context.getString(R.string.nowords), Toast.LENGTH_SHORT).show()
                        return@setPositiveButton
                    }
                    onAddWordsSuccessListener.addWordsSuccess(DragTextViewFactory.getInstance().createUserWordTextView(context, layout.addeditwords.text.toString(), layout.addeditwords.currentTextColor, layout.addeditwords.textSize.toInt()))
                }
                .setNegativeButton(R.string.cancel, null)
                .setView(layout)
                .create()
                .show()
    }

    fun showRepaintDialog(confirm: View.OnClickListener?) {
        AlertDialog.Builder(context)
                .setTitle(R.string.confirmRepaint)
                .setPositiveButton(R.string.cancel, null)
                .setNegativeButton(R.string.ok) { _, _ -> confirm?.onClick(null) }
                .create()
                .show()
    }

    fun showAddBorderDialog(listener: OnChangeBorderListener) {
        val layout = LayoutInflater.from(context).inflate(R.layout.view_addborder, null)
        var drawableId = 1

        val changeBorderOnclickListener = View.OnClickListener { view ->
            if (view.id == layout.xiangkuang1.id) {
                layout.xiangkuang1.setBackgroundResource(R.drawable.sp_bg_maincolor_border)
                drawableId = 1
                layout.xiangkuang2.setBackgroundResource(0)
            } else {
                layout.xiangkuang2.setBackgroundResource(R.drawable.sp_bg_maincolor_border)
                drawableId = 2
                layout.xiangkuang1.setBackgroundResource(0)
            }
        }
        layout.xiangkuang1.setOnClickListener(changeBorderOnclickListener)
        layout.xiangkuang2.setOnClickListener(changeBorderOnclickListener)

        AlertDialog.Builder(context)
                .setTitle(R.string.addborder)
                .setPositiveButton(R.string.ok) { _, _ ->
                    if (drawableId == 1)
                        listener.changeBorder(R.drawable.xiangkuang, DensityUtil.dip2px(context, 36f), DensityUtil.dip2px(context, 36f), DensityUtil.dip2px(context, 21f), DensityUtil.dip2px(context, 21f))
                    else
                        listener.changeBorder(R.drawable.xiangkuang2, DensityUtil.dip2px(context, 16f), DensityUtil.dip2px(context, 16f), DensityUtil.dip2px(context, 16f), DensityUtil.dip2px(context, 16f))
                }
                .setNegativeButton(R.string.cancel, null)
                .setView(layout)
                .create()
                .show()
    }

    private fun showOnceHintDialog(titleRes: Int, hintRes: Int, positiveRes: Int, positiveListener: View.OnClickListener?, negativeRes: Int, checkedSpKey: String) {
        val showHint = SPUtils.getBoolean(checkedSpKey, true)
        if (!showHint) {
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

    fun showEffectHintDialog(listener: View.OnClickListener?) {
        showOnceHintDialog(R.string.after_effect, R.string.effect_hint, R.string.go_on, listener, R.string.cancel, SPKeys.SHOW_EFFECT_HINT)
    }

    fun showPickColorHintDialog() {
        showOnceHintDialog(R.string.pickcolor, R.string.pickcolorhint, R.string.got_it, null, 0, SPKeys.PickColorDialogEnable)
    }

    fun showGradualHintDialog() {
        showOnceHintDialog(R.string.gradualModel, R.string.gradualModelHint, R.string.got_it, null, 0, SPKeys.GradualModel)
    }

    fun showEnterHintDialog() {
        showOnceHintDialog(R.string.finger_coloring, R.string.paintHint, R.string.got_it, null, 0, SPKeys.SHOW_ENTER_HINT)
    }

    fun showExitPaintDialog(saveListener: View.OnClickListener?, quitListener: View.OnClickListener?) {
        AlertDialog.Builder(context)
                .setTitle(R.string.is_exit)
                .setMessage(R.string.quitorsave)
                .setPositiveButton(R.string.cancel, null)
                .setNegativeButton(R.string.save_exit) { _, _ -> saveListener?.onClick(null) }
                .setNeutralButton(R.string.quit_exit) { _, _ -> quitListener?.onClick(null) }
                .create()
                .show()
    }
}
