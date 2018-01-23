package com.ue.graffiti.util

import com.trello.rxlifecycle2.android.ActivityEvent
import com.trello.rxlifecycle2.android.FragmentEvent
import com.trello.rxlifecycle2.components.support.RxAppCompatActivity
import com.trello.rxlifecycle2.components.support.RxFragment

import io.reactivex.Observable

/**
 * Created by hawk on 2018/1/22.
 */

object RxLifecycleUtils {
    fun bindUtilDestroy(context: Any, observable: Observable<*>): Observable<*> {
        if (context is RxAppCompatActivity) {
            return observable.compose(context.bindUntilEvent(ActivityEvent.DESTROY))
        }
        return if (context is RxFragment) {
            observable.compose(context.bindUntilEvent(FragmentEvent.DESTROY))
        } else observable
    }
}
