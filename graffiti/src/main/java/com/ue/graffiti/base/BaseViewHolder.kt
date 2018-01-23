package com.ue.graffiti.base

import android.support.v7.widget.RecyclerView
import android.view.View

/**
 * Created by hawk on 2018/1/17.
 */

abstract class BaseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    abstract fun update(item: Any)
}
