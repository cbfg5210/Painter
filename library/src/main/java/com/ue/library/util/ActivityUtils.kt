package com.ue.library.util

import android.support.v7.app.AppCompatActivity
import android.view.WindowManager

/**
 * Created by hawk on 2018/1/14.
 */
object ActivityUtils {
    fun toggleFullScreen(activity: AppCompatActivity, isFullScreen: Boolean) {
        activity.apply {
            window.apply {
                if (isFullScreen) {
                    attributes.flags = attributes.flags or WindowManager.LayoutParams.FLAG_FULLSCREEN
                    addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
                    supportActionBar?.hide()
                } else {
                    attributes.flags = attributes.flags and WindowManager.LayoutParams.FLAG_FULLSCREEN.inv()
                    clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
                    supportActionBar?.show()
                }
            }
        }
    }
}