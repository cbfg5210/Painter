package com.ue.graffiti.util;

import com.trello.rxlifecycle2.android.ActivityEvent;
import com.trello.rxlifecycle2.android.FragmentEvent;
import com.trello.rxlifecycle2.components.support.RxAppCompatActivity;
import com.trello.rxlifecycle2.components.support.RxFragment;

import io.reactivex.Observable;

/**
 * Created by hawk on 2018/1/22.
 */

public class RxLifecycleUtils {
    public static Observable bindUtilDestroy(Object context, Observable observable) {
        if (context instanceof RxAppCompatActivity) {
            RxAppCompatActivity rxAppCompatActivity = (RxAppCompatActivity) context;
            return observable.compose(rxAppCompatActivity.bindUntilEvent(ActivityEvent.DESTROY));
        }
        if (context instanceof RxFragment) {
            RxFragment rxFragment = (RxFragment) context;
            return observable.compose(rxFragment.bindUntilEvent(FragmentEvent.DESTROY));
        }
        return observable;
    }
}
