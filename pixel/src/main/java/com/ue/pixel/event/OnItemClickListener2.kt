package com.ue.pixel.event

import android.view.View

/**
 * Created by hawk on 2018/2/5.
 */
interface OnItemClickListener2<T> {
    fun onItemClick(view: View, item: T, position: Int)
}