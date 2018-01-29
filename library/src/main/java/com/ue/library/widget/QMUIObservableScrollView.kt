package com.ue.library.widget

import android.content.Context
import android.util.AttributeSet
import android.widget.ScrollView
import java.util.*

/**
 * 可以监听滚动事件的 [ScrollView]，并能在滚动回调中获取每次滚动前后的偏移量。
 *
 *
 * 由于 [ScrollView] 没有类似于 addOnScrollChangedListener 的方法可以监听滚动事件，所以需要通过重写 [android.view.View.onScrollChanged]，来触发滚动监听
 *
 * @author chantchen
 * @date 2015-08-25
 */
open class QMUIObservableScrollView : ScrollView {

    private val mOnScrollChangedListeners: MutableList<OnScrollChangedListener> by lazy { ArrayList<OnScrollChangedListener>() }

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    fun addOnScrollChangedListener(onScrollChangedListener: OnScrollChangedListener) {
        if (mOnScrollChangedListeners.contains(onScrollChangedListener)) return
        mOnScrollChangedListeners.add(onScrollChangedListener)
    }

    fun removeOnScrollChangedListener(onScrollChangedListener: OnScrollChangedListener) {
        mOnScrollChangedListeners.remove(onScrollChangedListener)
    }

    override fun onScrollChanged(l: Int, t: Int, oldl: Int, oldt: Int) {
        super.onScrollChanged(l, t, oldl, oldt)
        if (!mOnScrollChangedListeners.isEmpty()) {
            for (listener in mOnScrollChangedListeners) {
                listener.onScrollChanged(this, l, t, oldl, oldt)
            }
        }
    }

    interface OnScrollChangedListener {
        fun onScrollChanged(scrollView: QMUIObservableScrollView, l: Int, t: Int, oldl: Int, oldt: Int)
    }
}