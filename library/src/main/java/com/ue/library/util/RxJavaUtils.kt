package com.ue.library.util

import io.reactivex.disposables.Disposable

/**
 * Created by hawk on 2017/12/27.
 */
class RxJavaUtils {
    companion object {
        fun dispose(disposable: Disposable?) {
            if (disposable != null && !disposable.isDisposed) {
                disposable.dispose()
            }
        }
    }
}