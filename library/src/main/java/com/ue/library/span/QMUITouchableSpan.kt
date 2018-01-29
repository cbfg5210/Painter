package com.ue.library.span

import android.support.annotation.ColorInt
import android.support.v4.view.ViewCompat
import android.text.TextPaint
import android.text.style.ClickableSpan
import android.view.View

import com.ue.library.link.ITouchableSpan

/**
 * 可 Touch 的 Span，在 [.setPressed] 后根据是否 pressed 来触发不同的UI状态
 * 提供设置 span 的文字颜色和背景颜色的功能, 在构造时传入
 */
abstract class QMUITouchableSpan(@param:ColorInt @field:ColorInt val normalTextColor: Int,
                                 @param:ColorInt @field:ColorInt val pressedTextColor: Int,
                                 @param:ColorInt @field:ColorInt val normalBackgroundColor: Int,
                                 @param:ColorInt @field:ColorInt val pressedBackgroundColor: Int) : ClickableSpan(), ITouchableSpan {
    private var mIsPressed: Boolean = false
    private var mIsNeedUnderline = false

    abstract fun onSpanClick(widget: View)

    override fun onClick(widget: View) {
        if (ViewCompat.isAttachedToWindow(widget)) {
            onSpanClick(widget)
        }
    }

    override fun setPressed(isSelected: Boolean) {
        mIsPressed = isSelected
    }

    fun isPressed(): Boolean {
        return mIsPressed
    }

    fun setIsNeedUnderline(isNeedUnderline: Boolean) {
        mIsNeedUnderline = isNeedUnderline
    }

    override fun updateDrawState(ds: TextPaint) {
        ds.color = if (mIsPressed) pressedTextColor else normalTextColor
        ds.bgColor = if (mIsPressed) pressedBackgroundColor else normalBackgroundColor
        ds.isUnderlineText = mIsNeedUnderline
    }
}