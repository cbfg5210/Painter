package com.ue.graffiti.ui

import android.app.Activity
import android.support.v7.widget.RecyclerView
import android.view.View
import com.ue.adapterdelegate.BaseAdapterDelegate
import com.ue.adapterdelegate.DelegationAdapter
import com.ue.adapterdelegate.Item
import com.ue.adapterdelegate.OnDelegateClickListener
import com.ue.graffiti.R
import com.ue.graffiti.base.BaseViewHolder
import com.ue.graffiti.model.PenCatTitleItem
import com.ue.graffiti.model.PenShapeItem
import kotlinx.android.synthetic.main.item_pen_cat_title.view.*
import kotlinx.android.synthetic.main.item_pen_shape.view.*

/**
 * Created by hawk on 2018/1/17.
 */
internal class PenStyleAdapter(activity: Activity, items: MutableList<Item>?) : DelegationAdapter<Item>(), OnDelegateClickListener {
    var delegateClickListener: OnDelegateClickListener? = null
    private var lastShapeIndex = 0
    private var lastEffectIndex = 0

    fun getSpanCount(position: Int): Int {
        return if (items[position] is PenCatTitleItem) 4 else 1
    }

    init {
        if (items != null) {
            for (i in items.indices) {
                val item = items[i] as? PenShapeItem ?: continue
                if (!item.isChecked) {
                    continue
                }
                if (item.flag == PenDialog.FLAG_SHAPE) {
                    lastShapeIndex = i
                } else {
                    lastEffectIndex = i
                }
            }
            this.items.addAll(items)
        }

        val delegate = PenItemDelegate(activity)
        delegate.onDelegateClickListener = this
        this.addDelegate(delegate)

        addDelegate(PenTitleItemDelegate(activity))
    }

    override fun onClick(view: View, position: Int) {
        if (position < 0 || position >= itemCount) {
            return
        }

        val item = items[position] as PenShapeItem
        if (item.isChecked) {
            return
        }
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

    private class PenTitleItemDelegate(activity: Activity) : BaseAdapterDelegate<Item>(activity, R.layout.item_pen_cat_title) {

        override fun onCreateViewHolder(itemView: View): RecyclerView.ViewHolder {
            return ViewHolder(itemView)
        }

        override fun isForViewType(item: Item): Boolean {
            return item is PenCatTitleItem
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, item: Item, payloads: List<Any>) {
            (holder as BaseViewHolder).update(item)
        }

        internal inner class ViewHolder(itemView: View) : BaseViewHolder(itemView) {
            var tvPenCatTitle = itemView.tvPenCatTitle!!

            override fun update(item: Any) {
                item as PenCatTitleItem
                tvPenCatTitle.text = item.title
            }
        }
    }

    private class PenItemDelegate(activity: Activity) : BaseAdapterDelegate<Item>(activity, R.layout.item_pen_shape) {

        override fun onCreateViewHolder(itemView: View): RecyclerView.ViewHolder {
            return ViewHolder(itemView)
        }

        override fun isForViewType(item: Item): Boolean {
            return item is PenShapeItem
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, item: Item, payloads: List<Any>) {
            (holder as BaseViewHolder).update(item)
        }

        internal inner class ViewHolder(itemView: View) : BaseViewHolder(itemView) {
            var rbShapeName = itemView.rbShapeName!!

            override fun update(item: Any) {
                item as PenShapeItem
                rbShapeName.text = item.name
                rbShapeName.setCompoundDrawablesWithIntrinsicBounds(0, item.image, 0, 0)
                rbShapeName.isChecked = item.isChecked
            }
        }
    }
}