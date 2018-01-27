package com.ue.graffiti.ui


import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.larswerkman.holocolorpicker.ColorPicker
import com.ue.graffiti.R
import com.ue.graffiti.constant.SPKeys
import com.ue.library.util.SPUtils
import kotlinx.android.synthetic.main.gr_dialog_color_picker.view.*

//调色板对话框
class ColorPickerDialog : DialogFragment() {
    //调色板控件相关
    lateinit var picker: ColorPicker
    var colorPickerListener: OnColorPickerListener? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val contentView = inflater.inflate(R.layout.gr_dialog_color_picker, null)
        //找到实例对象
        picker = contentView.cpColorPicker
        //使环形取色器和拖动条建立关系
        picker.apply {
            addOpacityBar(contentView.obTransparencyBar)
            addSaturationBar(contentView.sbSaturation)
            addValueBar(contentView.vbDeepness)
            color = SPUtils.getInt(SPKeys.SP_PAINT_COLOR, resources.getColor(R.color.col_298ecb))
        }

        contentView.ivPickColor.setOnClickListener { v ->
            colorPickerListener?.onColorPicked(picker.color)
            dismiss()
        }

        return contentView
    }

    interface OnColorPickerListener {
        fun onColorPicked(color: Int)
    }

    companion object {
        //如果在onCreateDialog初始化界面的话设置的主题没有效果
        fun newInstance() = ColorPickerDialog().apply { setStyle(DialogFragment.STYLE_NORMAL, R.style.gr_GraffitiDialog) }
    }
}