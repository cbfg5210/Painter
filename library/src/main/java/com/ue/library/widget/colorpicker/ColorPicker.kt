package com.ue.pixel.colorpicker

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import com.ue.library.R
import kotlinx.android.synthetic.main.popup_color_picker.view.*

/**
 * Created by BennyKok on 10/14/2016.
 */

class ColorPicker(c: Context, startColor: Int, listener: SatValView.OnColorChangeListener) {
    private val popupWindow: PopupWindow
    private val satValView: SatValView

    init {
        val contentView = LayoutInflater.from(c).inflate(R.layout.popup_color_picker, null)
        contentView.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        satValView = contentView.svvSatValView
        satValView.withHueBar(contentView.hsbHueSeekBar)
        satValView.withAlphaBar(contentView.asbAlphaSeekBar)
        satValView.setListener(listener)
        satValView.setColor(startColor)
        popupWindow = PopupWindow(contentView)
        popupWindow.setBackgroundDrawable(ColorDrawable(Color.parseColor("#424242")))
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            popupWindow.elevation = convertDpToPixel(8f, c)
        }
        popupWindow.height = convertDpToPixel(292f, c).toInt()
        popupWindow.width = convertDpToPixel(216f, c).toInt()
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

    private fun convertDpToPixel(dp: Float, context: Context): Float {
        val metrics = context.resources.displayMetrics
        return dp * (metrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
    }
}