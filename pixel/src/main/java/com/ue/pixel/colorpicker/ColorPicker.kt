package com.ue.pixel.colorpicker

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow

import com.ue.pixel.R
import com.ue.pixel.util.Tool

/**
 * Created by BennyKok on 10/14/2016.
 */

class ColorPicker(c: Context, startColor: Int, listener: SatValView.OnColorChangeListener) {
    private val popupWindow: PopupWindow
    private val satValView: SatValView

    init {
        val contentView = LayoutInflater.from(c).inflate(R.layout.pi_popup_color_picker, null)
        contentView.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        satValView = contentView.findViewById<View>(R.id.svvSatValView) as SatValView
        satValView.withHueBar(contentView.findViewById<View>(R.id.hsbHueSeekBar) as HueSeekBar)
        satValView.withAlphaBar(contentView.findViewById<View>(R.id.asbAlphaSeekBar) as AlphaSeekBar)
        satValView.setListener(listener)
        satValView.setColor(startColor)
        popupWindow = PopupWindow(contentView)
        popupWindow.setBackgroundDrawable(ColorDrawable(Color.parseColor("#424242")))
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            popupWindow.elevation = Tool.convertDpToPixel(8f, c)
        }
        popupWindow.height = Tool.convertDpToPixel(292f, c).toInt()
        popupWindow.width = Tool.convertDpToPixel(216f, c).toInt()
    }

    fun show(anchor: View) {
        if (!popupWindow.isShowing) popupWindow.showAsDropDown(anchor, -popupWindow.width / 2 + anchor.width / 2, 0)
        else popupWindow.dismiss()
    }

    fun setColor(color: Int) {
        satValView.setColor(color)
    }

    fun onConfigChanges() {
        popupWindow.dismiss()
    }
}