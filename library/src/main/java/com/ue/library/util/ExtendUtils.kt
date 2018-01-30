package com.ue.library.util

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.os.Build
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.text.Html
import android.text.Spanned
import android.view.ViewPropertyAnimator
import android.view.WindowManager
import android.widget.Toast
import com.trello.rxlifecycle2.android.ActivityEvent
import com.trello.rxlifecycle2.android.FragmentEvent
import com.trello.rxlifecycle2.components.support.RxAppCompatActivity
import com.trello.rxlifecycle2.components.support.RxFragment
import io.reactivex.Observable
import io.reactivex.disposables.Disposable

/**
 * Created by hawk on 2018/1/24.
 */
fun Context.toast(message: CharSequence, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}

fun Context.toast(messageRes: Int, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, getString(messageRes), duration).show()
}

fun Context.getXmlImageArray(arrayId: Int): Array<Int> {
    val ta = this.resources.obtainTypedArray(arrayId)
    val imageArray = Array(ta.length(), { i -> ta.getResourceId(i, 0) })
    ta.recycle()
    return imageArray
}

fun Context.compatGetColor(colorRes: Int): Int {
    return ContextCompat.getColor(this, colorRes);
}

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

/**
 * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
 */
fun Context.dip2px(dpValue: Float): Int {
    val scale = this.resources.displayMetrics.density
    return (dpValue * scale + 0.5f).toInt()
}

/**
 * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
 */
fun Context.px2dip(pxValue: Float): Int {
    val scale = this.resources.displayMetrics.density
    return (pxValue / scale + 0.5f).toInt()
}

fun AppCompatActivity.toggleFullScreen(isFullScreen: Boolean) {
    this.apply {
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

fun Observable<Any>.bindUtilDestroy(context: Any): Observable<Any> {
    if (context is RxAppCompatActivity) {
        compose(context.bindUntilEvent(ActivityEvent.DESTROY))
    } else if (context is RxFragment) {
        compose(context.bindUntilEvent(FragmentEvent.DESTROY))
    }
    return this
}

fun Observable<Bitmap>.bindUtilDestroy2(context: Any): Observable<Bitmap> {
    if (context is RxAppCompatActivity) {
        compose(context.bindUntilEvent(ActivityEvent.DESTROY))
    } else if (context is RxFragment) {
        compose(context.bindUntilEvent(FragmentEvent.DESTROY))
    }
    return this
}

fun Observable<Long>.bindUtilDestroy3(context: Any): Observable<Long> {
    if (context is RxAppCompatActivity) {
        compose(context.bindUntilEvent(ActivityEvent.DESTROY))
    } else if (context is RxFragment) {
        compose(context.bindUntilEvent(FragmentEvent.DESTROY))
    }
    return this
}

fun dispose(disposable: Disposable?) {
    if (disposable != null && !disposable.isDisposed) {
        disposable.dispose()
    }
}