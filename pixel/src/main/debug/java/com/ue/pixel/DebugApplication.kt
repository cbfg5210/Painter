package com.ue.pixel

import android.app.Application
import com.ue.library.util.SPUtils

/**
 * Created by hawk on 2018/1/30.
 */
class DebugApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        SPUtils.init(this)
    }
}