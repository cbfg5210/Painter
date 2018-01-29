package com.ue.library.util

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.support.v4.content.ContextCompat
import android.util.TypedValue

/**
 *
 * @author cginechen
 * @date 2016-09-22
 */
object QMUIResHelper {

    fun getAttrFloatValue(context: Context, attrRes: Int): Float {
        val typedValue = TypedValue()
        context.theme.resolveAttribute(attrRes, typedValue, true)
        return typedValue.float
    }

    fun getAttrColor(context: Context, attrRes: Int): Int {
        val typedValue = TypedValue()
        context.theme.resolveAttribute(attrRes, typedValue, true)
        return typedValue.data
    }

    fun getAttrColorStateList(context: Context, attrRes: Int): ColorStateList {
        val typedValue = TypedValue()
        context.theme.resolveAttribute(attrRes, typedValue, true)
        return ContextCompat.getColorStateList(context, typedValue.resourceId)
    }

    fun getAttrDrawable(context: Context, attrRes: Int): Drawable? {
        val attrs = intArrayOf(attrRes)
        val ta = context.obtainStyledAttributes(attrs)
        val drawable = ta.getDrawable(0)
        ta.recycle()
        return drawable
    }

    fun getAttrDimen(context: Context, attrRes: Int): Int {
        val typedValue = TypedValue()
        context.theme.resolveAttribute(attrRes, typedValue, true)
        return TypedValue.complexToDimensionPixelSize(typedValue.data, context.resources.displayMetrics)
    }
}
