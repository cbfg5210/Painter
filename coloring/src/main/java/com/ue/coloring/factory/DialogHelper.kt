package com.ue.coloring.factory

import android.content.Context
import android.support.v7.app.AlertDialog
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.widget.SeekBar
import com.afollestad.materialdialogs.MaterialDialog
import com.ue.coloring.R
import com.ue.coloring.constant.SPKeys
import com.ue.coloring.event.OnAddWordsSuccessListener
import com.ue.coloring.event.OnChangeBorderListener
import com.ue.coloring.widget.ColorPickerSeekBar
import com.ue.library.util.DialogUtils
import com.ue.library.util.dip2px
import com.ue.library.util.toast
import kotlinx.android.synthetic.main.co_view_addborder.view.*
import kotlinx.android.synthetic.main.co_view_addwords.view.*

/**
 * Created by Swifty.Wang on 2015/6/12.
 */
class DialogHelper(private val context: Context) {

    fun showAddWordsDialog(onAddWordsSuccessListener: OnAddWordsSuccessListener) {
        val layout = LayoutInflater.from(context).inflate(R.layout.co_view_addwords, null)

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
                .setTitle(R.string.co_add_text)
                .setPositiveButton(R.string.co_ok) { _, _ ->
                    if (layout.addeditwords.text.toString().trim { it <= ' ' }.isEmpty()) {
                        context.toast(R.string.co_no_words)
                        return@setPositiveButton
                    }
                    onAddWordsSuccessListener.addWordsSuccess(DragTextViewFactory.getInstance().createUserWordTextView(context, layout.addeditwords.text.toString(), layout.addeditwords.currentTextColor, layout.addeditwords.textSize.toInt()))
                }
                .setNegativeButton(R.string.co_cancel, null)
                .setView(layout)
                .create()
                .show()
    }

    fun showRepaintDialog(callback: MaterialDialog.SingleButtonCallback?) {
        DialogUtils.showNormalDialog(context, R.string.co_confirm_repaint, 0, R.string.co_cancel, null, R.string.co_ok, callback)
    }

    fun showAddBorderDialog(listener: OnChangeBorderListener) {
        val layout = LayoutInflater.from(context).inflate(R.layout.co_view_addborder, null)
        var drawableId = 1

        val changeBorderOnclickListener = View.OnClickListener { view ->
            if (view.id == layout.xiangkuang1.id) {
                layout.xiangkuang1.setBackgroundResource(R.drawable.co_sp_bg_maincolor_border)
                drawableId = 1
                layout.xiangkuang2.setBackgroundResource(0)
            } else {
                layout.xiangkuang2.setBackgroundResource(R.drawable.co_sp_bg_maincolor_border)
                drawableId = 2
                layout.xiangkuang1.setBackgroundResource(0)
            }
        }
        layout.xiangkuang1.setOnClickListener(changeBorderOnclickListener)
        layout.xiangkuang2.setOnClickListener(changeBorderOnclickListener)

        AlertDialog.Builder(context)
                .setTitle(R.string.co_add_border)
                .setPositiveButton(R.string.co_ok) { _, _ ->
                    if (drawableId == 1) listener.changeBorder(R.drawable.xiangkuang, context.dip2px(36f), context.dip2px(36f), context.dip2px(21f), context.dip2px(21f))
                    else listener.changeBorder(R.drawable.xiangkuang2, context.dip2px(16f), context.dip2px(16f), context.dip2px(16f), context.dip2px(16f))
                }
                .setNegativeButton(R.string.co_cancel, null)
                .setView(layout)
                .create()
                .show()
    }

    fun showEffectHintDialog(listener: View.OnClickListener?) {
        DialogUtils.showOnceHintDialog(context, R.string.co_after_effect, R.string.co_effect_hint, R.string.co_go_on, listener, SPKeys.SHOW_EFFECT_HINT)
    }

    fun showPickColorHintDialog() {
        DialogUtils.showOnceHintDialog(context, R.string.co_pick_color, R.string.co_pick_color_hint, R.string.co_got_it, null, 0, SPKeys.PickColorDialogEnable)
    }

    fun showGradualHintDialog() {
        DialogUtils.showOnceHintDialog(context, R.string.co_gradual_model, R.string.co_gradual_model_hint, R.string.co_got_it, null, 0, SPKeys.GradualModel)
    }

    fun showEnterHintDialog() {
        DialogUtils.showOnceHintDialog(context, R.string.module_coloring, R.string.co_paint_hint, R.string.co_got_it, null, 0, SPKeys.SHOW_ENTER_HINT)
    }
}