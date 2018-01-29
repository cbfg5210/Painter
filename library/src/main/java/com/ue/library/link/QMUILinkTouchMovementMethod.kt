package com.ue.library.link

import android.text.Spannable
import android.text.method.LinkMovementMethod
import android.text.method.Touch
import android.view.MotionEvent
import android.widget.TextView

/**
 * 配合 [QMUILinkTouchDecorHelper] 使用
 *
 * @author cginechen
 * @date 2017-03-20
 */

class QMUILinkTouchMovementMethod : LinkMovementMethod() {

    override fun onTouchEvent(widget: TextView, buffer: Spannable, event: MotionEvent): Boolean {
        return sHelper.onTouchEvent(widget, buffer, event) || Touch.onTouchEvent(widget, buffer, event)
    }

    companion object {
        private val sHelper = QMUILinkTouchDecorHelper()
        val instance: QMUILinkTouchMovementMethod by lazy { QMUILinkTouchMovementMethod() }
    }
}