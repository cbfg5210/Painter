package com.ue.library.alpha

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout

/**
 * 在 pressed 和 disabled 时改变 View 的透明度
 */
open class QMUIAlphaLinearLayout : LinearLayout {

    val alphaViewHelper: QMUIAlphaViewHelper by lazy { QMUIAlphaViewHelper(this) }

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    override fun setPressed(pressed: Boolean) {
        super.setPressed(pressed)
        alphaViewHelper.onPressedChanged(this, pressed)
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        alphaViewHelper.onEnabledChanged(this, enabled)
    }

    /**
     * 设置是否要在 press 时改变透明度
     *
     * @param changeAlphaWhenPress 是否要在 press 时改变透明度
     */
    fun setChangeAlphaWhenPress(changeAlphaWhenPress: Boolean) {
        alphaViewHelper.setChangeAlphaWhenPress(changeAlphaWhenPress)
    }

    /**
     * 设置是否要在 disabled 时改变透明度
     *
     * @param changeAlphaWhenDisable 是否要在 disabled 时改变透明度
     */
    fun setChangeAlphaWhenDisable(changeAlphaWhenDisable: Boolean) {
        alphaViewHelper.setChangeAlphaWhenDisable(changeAlphaWhenDisable)
    }
}