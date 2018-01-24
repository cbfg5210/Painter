package com.ue.graffiti.util

import android.content.Context
import android.widget.Toast
import com.trello.rxlifecycle2.android.ActivityEvent
import com.trello.rxlifecycle2.android.FragmentEvent
import com.trello.rxlifecycle2.components.support.RxAppCompatActivity
import com.trello.rxlifecycle2.components.support.RxFragment
import io.reactivex.Observable

/**
 * Created by hawk on 2018/1/24.
 */
fun Context.toast(message: CharSequence, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
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

fun Observable<Any>.bindUtilDestroy(context: Any): Observable<Any> {
    if (context is RxAppCompatActivity) {
        return this.compose(context.bindUntilEvent(ActivityEvent.DESTROY))
    }
    return if (context is RxFragment) {
        this.compose(context.bindUntilEvent(FragmentEvent.DESTROY))
    } else this
}