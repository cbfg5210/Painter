package com.ue.graffiti.ui

import android.app.Activity
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.RadioButton
import android.widget.TextView

import com.ue.adapterdelegate.BaseAdapterDelegate
import com.ue.adapterdelegate.DelegationAdapter
import com.ue.adapterdelegate.Item
import com.ue.adapterdelegate.OnDelegateClickListener
import com.ue.graffiti.R
import com.ue.graffiti.base.BaseViewHolder
import com.ue.graffiti.model.PenCatTitleItem
import com.ue.graffiti.model.PenShapeItem

/**
 * Created by hawk on 2018/1/17.
 */
internal class PenStyleAdapter(activity: Activity, items: MutableList<Item>?) : DelegationAdapter<Item>(), OnDelegateClickListener {

    private var mDelegateClickListener: OnDelegateClickListener? = null
    private var lastShapeIndex: Int = 0
    private var lastEffectIndex: Int = 0

    fun setDelegateClickListener(delegateClickListener: OnDelegateClickListener) {
        mDelegateClickListener = delegateClickListener
    }

    fun getSpanCount(position: Int): Int {
        return if (items[position] is PenCatTitleItem) 4 else 1
    }

    init {
        if (items != null) {
            var i = 0
            val len = items.size
            while (i < len) {
                val item = items[i]
                if (item !is PenShapeItem) {
                    i++
                    continue
                }
                if (!item.isChecked) {
                    i++
                    continue
                }
                if (item.flag == PenDialog.FLAG_SHAPE) {
                    lastShapeIndex = i
                } else {
                    lastEffectIndex = i
                }
                i++
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

        if (mDelegateClickListener != null) {
            mDelegateClickListener!!.onClick(view, position)
        }
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
            var tvPenCatTitle: TextView

            init {
                tvPenCatTitle = itemView.findViewById(R.id.tvPenCatTitle)
            }

            override fun update(aItem: Any) {
                val item = aItem as PenCatTitleItem
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
            var rbShapeName: RadioButton

            init {
                rbShapeName = itemView.findViewById(R.id.rbShapeName)
            }

            override fun update(aItem: Any) {
                val item = aItem as PenShapeItem
                rbShapeName.text = item.name
                rbShapeName.setCompoundDrawablesWithIntrinsicBounds(0, item.image, 0, 0)
                rbShapeName.isChecked = item.isChecked
            }
        }
    }
}