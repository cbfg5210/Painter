package com.ue.library.util

import android.content.Context
import android.graphics.Bitmap
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

fun Context.getXmlImageArray(arrayId: Int): IntArray {
    val ta = this.resources.obtainTypedArray(arrayId)
    val imageArray = IntArray(ta.length())
    for (i in imageArray.indices) {
        imageArray[i] = ta.getResourceId(i, 0)
    }
    ta.recycle()
    return imageArray
}

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