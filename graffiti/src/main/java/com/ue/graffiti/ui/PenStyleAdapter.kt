package com.ue.graffiti.ui

import android.app.Activity
import android.view.View
import com.ue.adapterdelegate.AdapterDelegate
import com.ue.adapterdelegate.BaseViewHolder
import com.ue.adapterdelegate.DelegationAdapter
import com.ue.adapterdelegate.OnDelegateClickListener
import com.ue.graffiti.R
import com.ue.graffiti.model.PenCatTitleItem
import com.ue.graffiti.model.PenShapeItem
import kotlinx.android.synthetic.main.gr_item_pen_cat_title.view.*
import kotlinx.android.synthetic.main.gr_item_pen_shape.view.*

/**
 * Created by hawk on 2018/1/17.
 */
internal class PenStyleAdapter(activity: Activity, mItems: MutableList<Any>?) : DelegationAdapter<Any>(), OnDelegateClickListener {
    var delegateClickListener: OnDelegateClickListener? = null
    private var lastShapeIndex = 0
    private var lastEffectIndex = 0

    fun getSpanCount(position: Int): Int {
        return if (items[position] is PenCatTitleItem) 4 else 1
    }

    init {
        mItems?.apply {
            for (i in mItems.indices) {
                val item = mItems[i] as? PenShapeItem ?: continue
                if (item.isChecked) {
                    if (item.flag == PenDialog.FLAG_SHAPE) lastShapeIndex = i
                    else lastEffectIndex = i
                }
            }
            items.addAll(this)
        }
        addDelegate(PenTitleItemDelegate(activity))
        addDelegate(PenItemDelegate(activity).apply { delegateClickListener = this@PenStyleAdapter })
    }

    override fun onClick(view: View, position: Int) {
        val item = items[position] as PenShapeItem
        if (item.isChecked) return

        if (item.flag == PenDialog.FLAG_SHAPE) {
            (items[lastShapeIndex] as PenShapeItem).isChecked = false
            notifyItemChanged(lastShapeIndex, 0)
            lastShapeIndex = position
        } else {
            (items[lastEffectIndex] as PenShapeItem).isChecked = false
            notifyItemChanged(lastEffectIndex, 0)
            lastEffectIndex = position
        }
        item.isChecked = true
        notifyItemChanged(position, 0)

        delegateClickListener?.onClick(view, position)
    }

    private class PenTitleItemDelegate(activity: Activity) : AdapterDelegate<Any>(activity, R.layout.gr_item_pen_cat_title) {

        override fun isForViewType(item: Any) = item is PenCatTitleItem

        override fun onCreateViewHolder(itemView: View): BaseViewHolder<Any> {
            return object : BaseViewHolder<Any>(itemView) {
                private val tvPenCatTitle = itemView.tvPenCatTitle

                override fun updateContents(item: Any) {
                    item as PenCatTitleItem
                    tvPenCatTitle.text = item.title
                }
            }
        }
    }

    private class PenItemDelegate(activity: Activity) : AdapterDelegate<Any>(activity, R.layout.gr_item_pen_shape) {
        override fun isForViewType(item: Any) = item is PenShapeItem

        override fun onCreateViewHolder(itemView: View): BaseViewHolder<Any> {
            return object : BaseViewHolder<Any>(itemView) {
                private val rbShapeName = itemView.rbShapeName

                override fun updateContents(item: Any) {
                    item as PenShapeItem
                    rbShapeName.text = item.name
                    rbShapeName.setCompoundDrawablesWithIntrinsicBounds(0, item.image, 0, 0)
                    rbShapeName.isChecked = item.isChecked
                }
            }
        }
    }
}