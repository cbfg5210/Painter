package com.ue.fingercoloring.event

import com.ue.fingercoloring.model.LocalWork

/**
 * Created by Swifty.Wang on 2015/9/1.
 */
interface OnLoadWorkListener {
    fun loadUserPaintFinished(list: List<LocalWork>)
}
