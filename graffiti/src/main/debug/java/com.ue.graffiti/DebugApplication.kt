package com.ue.graffiti

import android.app.Application
import com.ue.library.util.SPUtils

/**
 * Created by hawk on 2018/1/23.
 */

class DebugApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        SPUtils.init(this)
    }
}
