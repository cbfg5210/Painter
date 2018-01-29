package com.ue.library.link

import android.text.Selection
import android.text.Spannable
import android.view.MotionEvent
import android.widget.TextView
import com.ue.library.widget.textview.ISpanTouchFix

/**
 * @author cginechen
 * @date 2017-03-20
 */

class QMUILinkTouchDecorHelper {
    private var mPressedSpan: ITouchableSpan? = null

    fun onTouchEvent(textView: TextView, spannable: Spannable, event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                mPressedSpan = getPressedSpan(textView, spannable, event)?.apply {
                    setPressed(true)
                    Selection.setSelection(spannable, spannable.getSpanStart(mPressedSpan), spannable.getSpanEnd(mPressedSpan))
                }
                if (textView is ISpanTouchFix) {
                    textView.setTouchSpanHit(mPressedSpan != null)
                }
                return mPressedSpan != null
            }
            MotionEvent.ACTION_MOVE -> {
                val touchedSpan = getPressedSpan(textView, spannable, event)
                mPressedSpan?.apply {
                    if (touchedSpan != this) {
                        setPressed(false)
                        mPressedSpan = null
                        Selection.removeSelection(spannable)
                    }
                }
                if (textView is ISpanTouchFix) {
                    textView.setTouchSpanHit(mPressedSpan != null)
                }
                return mPressedSpan != null
            }
            MotionEvent.ACTION_UP -> {
                var touchSpanHint = false
                mPressedSpan?.apply {
                    touchSpanHint = true
                    setPressed(false)
                    onClick(textView)
                }
                mPressedSpan = null
                Selection.removeSelection(spannable)
                if (textView is ISpanTouchFix) {
                    textView.setTouchSpanHit(touchSpanHint)
                }
                return touchSpanHint
            }
            else -> {
                mPressedSpan?.apply { setPressed(false) }
                if (textView is ISpanTouchFix) {
                    textView.setTouchSpanHit(false)
                }
                Selection.removeSelection(spannable)
                return false
            }
        }
    }

    private fun getPressedSpan(textView: TextView, spannable: Spannable, event: MotionEvent): ITouchableSpan? {
        var x = event.x.toInt()
        var y = event.y.toInt()

        x -= textView.totalPaddingLeft
        y -= textView.totalPaddingTop

        x += textView.scrollX
        y += textView.scrollY

        val layout = textView.layout
        val line = layout.getLineForVertical(y)
        var off = layout.getOffsetForHorizontal(line, x.toFloat())
        // 实际上没点到任何内容
        if (x < layout.getLineLeft(line) || x > layout.getLineRight(line)) off = -1

        val link = spannable.getSpans(off, off, ITouchableSpan::class.java)
        return if (link.isNotEmpty()) link[0] else null
    }
}