package com.ue.fingercoloring.factory

import android.content.Context
import android.graphics.Typeface
import android.text.Editable
import android.util.TypedValue
import android.widget.FrameLayout

import com.ue.fingercoloring.widget.DragedTextView

/**
 * Created by macpro001 on 20/8/15.
 */
class DragTextViewFactory private constructor() {
    var HugeSize = 30
    var BigTextSize = 24
    var MiddleTextSize = 18
    var SmallTextSize = 12

    /**
     * @param context
     * @param words
     * @param color
     * @param size    sp
     * @return
     */
    fun createUserWordTextView(context: Context, words: String, color: Int, size: Int): DragedTextView {
        val dragedTextView = DragedTextView(context)
        dragedTextView.layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT)
        dragedTextView.text = words
        dragedTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, size.toFloat())
        dragedTextView.setTextColor(color)
        dragedTextView.isClickable = true
        dragedTextView.setTypeface(null, Typeface.BOLD)
        return dragedTextView
    }

    fun createUserWordTextView(context: Context, words: Editable): DragedTextView {
        val dragedTextView = DragedTextView(context)
        dragedTextView.layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT)
        dragedTextView.text = words
        dragedTextView.isClickable = true
        dragedTextView.setTypeface(null, Typeface.BOLD)
        return dragedTextView
    }

    companion object {
        private var ourInstance: DragTextViewFactory? = null

        fun getInstance(): DragTextViewFactory {
            ourInstance = ourInstance ?: DragTextViewFactory()
            return ourInstance!!
        }
    }
}
