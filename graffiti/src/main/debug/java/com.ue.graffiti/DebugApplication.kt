package com.ue.graffiti

import android.app.Application
import android.support.v7.app.AppCompatDelegate
import com.ue.library.util.SPUtils

/**
 * Created by hawk on 2018/1/23.
 */

class DebugApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        SPUtils.init(this)
    }
}
