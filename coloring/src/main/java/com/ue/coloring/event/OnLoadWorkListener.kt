package com.ue.coloring.event

import com.ue.coloring.model.LocalWork

/**
 * Created by Swifty.Wang on 2015/9/1.
 */
interface OnLoadWorkListener {
    fun loadUserPaintFinished(list: List<LocalWork>)
}
