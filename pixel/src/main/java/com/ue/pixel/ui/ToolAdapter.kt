package com.ue.pixel.ui

import android.app.Activity
import android.view.View
import android.widget.ImageView
import com.ue.adapterdelegate.AdapterDelegate
import com.ue.adapterdelegate.BaseViewHolder
import com.ue.adapterdelegate.DelegationAdapter
import com.ue.adapterdelegate.OnDelegateClickListener
import com.ue.pixel.R
import com.ue.pixel.model.ToolItem

/**
 * Created by hawk on 2018/1/31.
 */
class ToolAdapter(activity: Activity) : DelegationAdapter<ToolItem>(), OnDelegateClickListener {
    var itemClickListener: OnDelegateClickListener? = null
    private var lastSelection = 0
    private var currentSelection = 0

    init {
        items.add(ToolItem(R.drawable.ic_mode_edit_24dp))
        items.add(ToolItem(R.drawable.ic_eraser_24dp))
        items.add(ToolItem(R.drawable.ic_fill_24dp))
        items.add(ToolItem(R.drawable.ic_line_24dp))
        items.add(ToolItem(R.drawable.ic_square_24dp))

        addDelegate(ToolDelegate(activity).apply { delegateClickListener = this@ToolAdapter })
    }

    override fun onClick(view: View, position: Int) {
        if (position == currentSelection) return
        lastSelection = currentSelection
        currentSelection = position
        notifyItemChanged(lastSelection, 1)
        notifyItemChanged(currentSelection)

        itemClickListener?.onClick(view, items[position].image)
    }

    inner class ToolDelegate(activity: Activity) : AdapterDelegate<ToolItem>(activity, R.layout.item_tool) {
        override fun onCreateViewHolder(itemView: View): BaseViewHolder<ToolItem> {
            return object : BaseViewHolder<ToolItem>(itemView) {
                override fun updateContents(item: ToolItem) {
                    itemView as ImageView
                    itemView.setImageResource(item.image)
                    itemView.alpha = if (currentSelection == adapterPosition) 1f else 0.3f
                }
            }
        }
    }
}