package com.ue.coloring.model

import com.ue.library.util.GsonHolder

/**
 * Created by Swifty.Wang on 2015/9/1.
 */
class LocalWork(val imageName: String, val imageUrl: String, var lastModDate: String, var lastModTimeStamp: Long, wvHRadio: Float) {
    var wvHRadio=0f
        private set

    init {
        this.wvHRadio = wvHRadio
    }

    override fun toString():String{
        return GsonHolder.gson.toJson(this)
    }
}
