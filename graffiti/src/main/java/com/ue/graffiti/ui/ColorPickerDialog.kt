package com.ue.graffiti.ui


import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.larswerkman.holocolorpicker.ColorPicker
import com.larswerkman.holocolorpicker.OpacityBar
import com.larswerkman.holocolorpicker.SaturationBar
import com.larswerkman.holocolorpicker.ValueBar
import com.ue.graffiti.R
import com.ue.graffiti.constant.SPKeys
import com.ue.library.util.SPUtils

//调色板对话框
class ColorPickerDialog : DialogFragment() {
    //调色板控件相关
    lateinit var picker: ColorPicker
    private var mColorPickerListener: OnColorPickerListener? = null

    fun setColorPickerListener(colorPickerListener: OnColorPickerListener) {
        mColorPickerListener = colorPickerListener
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val contentView = LayoutInflater.from(context).inflate(R.layout.dialog_color_picker, null)
        //找到实例对象
        picker = contentView.findViewById<View>(R.id.cpColorPicker) as ColorPicker
        val opacityBar = contentView.findViewById<View>(R.id.obTransparencyBar) as OpacityBar
        val saturationBar = contentView.findViewById<View>(R.id.sbSaturation) as SaturationBar
        val valueBar = contentView.findViewById<View>(R.id.vbDeepness) as ValueBar
        //使环形取色器和拖动条建立关系
        picker.addOpacityBar(opacityBar)
        picker.addSaturationBar(saturationBar)
        picker.addValueBar(valueBar)

        val lastColor = SPUtils.getInt(SPKeys.SP_PAINT_COLOR, resources.getColor(R.color.col_298ecb))
        picker.color = lastColor

        contentView.findViewById<View>(R.id.ivPickColor).setOnClickListener { v ->
            if (mColorPickerListener != null) {
                mColorPickerListener!!.onColorPicked(picker.color)
            }
            dismiss()
        }

        return contentView
    }

    interface OnColorPickerListener {
        fun onColorPicked(color: Int)
    }

    companion object {

        fun newInstance(): ColorPickerDialog {
            val dialog = ColorPickerDialog()
            //如果在onCreateDialog初始化界面的话设置的主题没有效果
            dialog.setStyle(DialogFragment.STYLE_NORMAL, R.style.GraffitiDialog)
            return dialog
        }
    }
}