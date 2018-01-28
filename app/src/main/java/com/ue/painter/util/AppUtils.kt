package com.ue.painter.util

import com.trello.rxlifecycle2.android.ActivityEvent
import com.trello.rxlifecycle2.android.FragmentEvent
import com.trello.rxlifecycle2.components.support.RxAppCompatActivity
import com.trello.rxlifecycle2.components.support.RxFragment
import com.ue.painter.model.PictureWork
import com.ue.painter.model.Work
import io.reactivex.Observable
import java.util.*

/**
 * Created by hawk on 2018/1/26.
 */
fun Observable<ArrayList<PictureWork>>.bindUtilDestroy(context: Any): Observable<ArrayList<PictureWork>> {
    if (context is RxAppCompatActivity) compose(context.bindUntilEvent(ActivityEvent.DESTROY))
    else if (context is RxFragment) compose(context.bindUntilEvent(FragmentEvent.DESTROY))
    return this
}

fun Observable<MutableList<Work>>.bindUtilDestroy3(context: Any): Observable<MutableList<Work>> {
    if (context is RxAppCompatActivity) compose(context.bindUntilEvent(ActivityEvent.DESTROY))
    else if (context is RxFragment) compose(context.bindUntilEvent(FragmentEvent.DESTROY))
    return this
}