package com.ue.autodraw

import android.app.Activity
import android.graphics.Point
import android.view.View
import android.widget.AdapterView
import com.ue.adapterdelegate.AdapterDelegate
import com.ue.adapterdelegate.BaseViewHolder
import com.ue.adapterdelegate.DelegationAdapter
import com.ue.adapterdelegate.OnDelegateClickListener
import com.ue.library.util.ImageLoaderUtils
import kotlinx.android.synthetic.main.au_item_bg.view.*

/**
 * Created by hawk on 2018/1/10.
 */
class BgAdapter(activity: Activity, bgs: Array<Int>) : DelegationAdapter<Int>(), OnDelegateClickListener {
    private val displaySize = activity.resources.getDimensionPixelSize(R.dimen.widget_size_50)
    var itemListener: AdapterView.OnItemClickListener? = null

    init {
        items.addAll(bgs)
        addDelegate(BgDelegate(activity).apply { delegateClickListener = this@BgAdapter })
    }

    override fun onClick(view: View, position: Int) {
        itemListener?.onItemClick(null, view, items[position], 0)
    }

    private inner class BgDelegate(activity: Activity) : AdapterDelegate<Int>(activity, R.layout.au_item_bg) {
        override fun onCreateViewHolder(itemView: View): BaseViewHolder<Int> {
            return object : BaseViewHolder<Int>(itemView) {
                private val sivBg = itemView.sivBg

                override fun updateContents(item: Int) {
                    ImageLoaderUtils.display(sivBg, item, Point(displaySize, displaySize))
                }
            }
        }
    }
}