package com.ue.pixel.util

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.SharedPreferences
import android.os.Build
import android.text.Html
import android.text.Spanned
import android.view.ViewPropertyAnimator


/**
 * Created by hawk on 2018/1/30.
 */
fun fromHtml(source: String): Spanned {
    return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) Html.fromHtml(source)
    else Html.fromHtml(source, Html.FROM_HTML_MODE_LEGACY)
}

fun ViewPropertyAnimator.withAnimEndAction(endRunnable: Runnable): ViewPropertyAnimator {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) withEndAction(endRunnable)
    else setListener(object : AnimatorListenerAdapter() {
        override fun onAnimationEnd(animation: Animator) {
            endRunnable.run()
        }
    })
    return this
}

fun SharedPreferences.getString(key: String) = this.getString(key, "")