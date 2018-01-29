package com.ue.library.link

import android.view.View

/**
 * @author cginechen
 * @date 2017-03-20
 */

interface ITouchableSpan {
    fun setPressed(pressed: Boolean)
    fun onClick(widget: View)
}
