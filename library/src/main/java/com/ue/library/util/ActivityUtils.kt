package com.ue.library.util

import android.support.v7.app.AppCompatActivity
import android.view.WindowManager

/**
 * Created by hawk on 2018/1/14.
 */
object ActivityUtils {
     fun toggleFullScreen(activity: AppCompatActivity, isFullScreen: Boolean) {
        if (isFullScreen) {
            val params = activity.window.attributes
            params.flags = params.flags or WindowManager.LayoutParams.FLAG_FULLSCREEN
            activity.window.attributes = params
            activity.window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
            activity.supportActionBar?.hide()
        } else {
            val params = activity.window.attributes
            params.flags = params.flags and WindowManager.LayoutParams.FLAG_FULLSCREEN.inv()
            activity.window.attributes = params
            activity.window.clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
            activity.supportActionBar?.show()
        }
    }
}