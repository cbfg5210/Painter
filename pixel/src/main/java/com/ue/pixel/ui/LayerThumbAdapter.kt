package com.ue.pixel.ui

import android.app.Activity
import android.view.View
import com.ue.adapterdelegate.AdapterDelegate
import com.ue.adapterdelegate.BaseViewHolder
import com.ue.adapterdelegate.DelegationAdapter
import com.ue.adapterdelegate.OnDelegateClickListener
import com.ue.pixel.R
import com.ue.pixel.event.OnItemClickListener2
import com.ue.pixel.model.LayerThumbItem1
import com.ue.pixel.widget.FastBitmapView

/**
 * Created by hawk on 2018/2/5.
 */
class LayerThumbAdapter(activity: Activity) : DelegationAdapter<LayerThumbItem1>(), OnDelegateClickListener {
    var itemClickListener: OnItemClickListener2<LayerThumbItem1>? = null
    private var lastSelection = 0
    private var currentSelection = 0

    init {
        addDelegate(LayerThumbDelegate(activity).apply { delegateClickListener = this@LayerThumbAdapter })
    }

    fun add(thumbItem1: LayerThumbItem1) {
        items.add(thumbItem1)
        notifyItemInserted(items.size - 1)
    }

    fun add(index: Int, thumbItem1: LayerThumbItem1) {
        items.add(index, thumbItem1)
        notifyItemInserted(index)
    }

    fun setAllVisibility(visible: Boolean) {
        items.forEach { it.visible = visible }
        notifyDataSetChanged()
    }

    fun select(position: Int) {
        lastSelection = currentSelection
        currentSelection = position
        items[currentSelection].pressed()
        items[lastSelection].pressedTime = 0

        notifyItemChanged(lastSelection, 1)
        notifyItemChanged(currentSelection, 1)
    }

    fun removeAt(position: Int) {
        items.removeAt(position)
        if (currentSelection == position || currentSelection >= itemCount) {
            currentSelection = 0
        }
        if (lastSelection == position || lastSelection >= itemCount) {
            lastSelection = 0
        }
        notifyItemRemoved(position)
        notifyItemChanged(lastSelection, 1)
        notifyItemChanged(currentSelection, 1)
    }

    override fun onClick(view: View, position: Int) {
        itemClickListener?.onItemClick(view, items[position], position)

        if (position != currentSelection) {
            lastSelection = currentSelection
            currentSelection = position
            notifyItemChanged(lastSelection, 1)
            notifyItemChanged(currentSelection, 1)

            items[lastSelection].pressedTime = 0
        }
    }

    fun notifyItemVisible(position: Int, visible: Boolean) {
        items[position].visible = visible
        notifyItemChanged(position, 1)
    }

    fun notifyItemMoved2(from: Int, to: Int) {
        if (currentSelection == from) {
            lastSelection = currentSelection
            currentSelection = to
        }
        notifyItemMoved(from, to)
    }

    private inner class LayerThumbDelegate(activity: Activity) : AdapterDelegate<LayerThumbItem1>(activity, R.layout.pi_item_layer_thumb) {
        override fun onCreateViewHolder(itemView: View): BaseViewHolder<LayerThumbItem1> {
            return object : BaseViewHolder<LayerThumbItem1>(itemView) {
                override fun updateContents(item: LayerThumbItem1) {
                    itemView as FastBitmapView

                    itemView.isSelected = currentSelection == adapterPosition
                    itemView.setVisible(item.visible)
                    itemView.bitmap = item.bitmap
                }
            }
        }
    }
}