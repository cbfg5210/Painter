package com.ue.painter.util

import com.trello.rxlifecycle2.android.ActivityEvent
import com.trello.rxlifecycle2.android.FragmentEvent
import com.trello.rxlifecycle2.components.support.RxAppCompatActivity
import com.trello.rxlifecycle2.components.support.RxFragment
import com.ue.painter.model.LocalWork
import com.ue.painter.model.PictureWorkItem
import io.reactivex.Observable
import java.util.*

/**
 * Created by hawk on 2018/1/26.
 */
fun Observable<ArrayList<PictureWorkItem>>.bindUtilDestroy(context: Any): Observable<ArrayList<PictureWorkItem>> {
    if (context is RxAppCompatActivity) {
        compose(context.bindUntilEvent(ActivityEvent.DESTROY))
    } else if (context is RxFragment) {
        compose(context.bindUntilEvent(FragmentEvent.DESTROY))
    }
    return this
}

fun Observable<List<LocalWork>>.bindUtilDestroy2(context: Any): Observable<List<LocalWork>> {
    if (context is RxAppCompatActivity) {
        compose(context.bindUntilEvent(ActivityEvent.DESTROY))
    } else if (context is RxFragment) {
        compose(context.bindUntilEvent(FragmentEvent.DESTROY))
    }
    return this
}