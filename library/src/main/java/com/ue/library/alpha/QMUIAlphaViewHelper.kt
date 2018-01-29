package com.ue.library.alpha

import android.view.View
import com.ue.library.R
import com.ue.library.util.QMUIResHelper

class QMUIAlphaViewHelper(private val mTarget: View) {

    /**
     * 设置是否要在 press 时改变透明度
     */
    private var mChangeAlphaWhenPress = true

    /**
     * 设置是否要在 disabled 时改变透明度
     */
    private var mChangeAlphaWhenDisable = true

    private val mNormalAlpha = 1f
    private var mPressedAlpha = .5f
    private var mDisabledAlpha = .5f

    init {
        mPressedAlpha = QMUIResHelper.getAttrFloatValue(mTarget.context, R.attr.qmui_alpha_pressed)
        mDisabledAlpha = QMUIResHelper.getAttrFloatValue(mTarget.context, R.attr.qmui_alpha_disabled)
    }

    fun onPressedChanged(target: View, pressed: Boolean) {
        if (mTarget.isEnabled) {
            mTarget.alpha = if (mChangeAlphaWhenPress && pressed && target.isClickable) mPressedAlpha else mNormalAlpha
        } else if (mChangeAlphaWhenDisable) {
            target.alpha = mDisabledAlpha
        }
    }

    fun onEnabledChanged(target: View, enabled: Boolean) {
        val alphaForIsEnable: Float
        if (mChangeAlphaWhenDisable) {
            alphaForIsEnable = if (enabled) mNormalAlpha else mDisabledAlpha
        } else {
            alphaForIsEnable = mNormalAlpha
        }
        target.alpha = alphaForIsEnable
    }

    /**
     * 设置是否要在 press 时改变透明度
     *
     * @param changeAlphaWhenPress 是否要在 press 时改变透明度
     */
    fun setChangeAlphaWhenPress(changeAlphaWhenPress: Boolean) {
        mChangeAlphaWhenPress = changeAlphaWhenPress
    }

    /**
     * 设置是否要在 disabled 时改变透明度
     *
     * @param changeAlphaWhenDisable 是否要在 disabled 时改变透明度
     */
    fun setChangeAlphaWhenDisable(changeAlphaWhenDisable: Boolean) {
        mChangeAlphaWhenDisable = changeAlphaWhenDisable
        onEnabledChanged(mTarget, mTarget.isEnabled)
    }
}