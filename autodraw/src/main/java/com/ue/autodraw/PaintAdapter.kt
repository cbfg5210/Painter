package com.ue.autodraw

import android.app.Activity
import android.view.View
import android.widget.AdapterView
import com.ue.adapterdelegate.AdapterDelegate
import com.ue.adapterdelegate.BaseViewHolder
import com.ue.adapterdelegate.DelegationAdapter
import com.ue.adapterdelegate.OnDelegateClickListener
import com.ue.library.util.SPUtils
import kotlinx.android.synthetic.main.au_item_paint.view.*

/**
 * Created by hawk on 2018/1/10.
 */
class PaintAdapter(activity: Activity, bgs: Array<Int>) : DelegationAdapter<Int>(), OnDelegateClickListener {
    companion object {
        private val SP_PAINT_INDEX = "sp_paint_index"
    }

    private var lastSelectedIndex = 0
    private var selectedIndex = SPUtils.getInt(SP_PAINT_INDEX)

    var itemListener: AdapterView.OnItemClickListener? = null
        set(value) {
            field = value
            field?.onItemClick(null, null, items[selectedIndex], 0)
        }

    init {
        items.addAll(bgs)
        addDelegate(PaintDelegate(activity).apply { delegateClickListener = this@PaintAdapter })
    }

    override fun onClick(view: View, position: Int) {
        if (selectedIndex == position) return

        lastSelectedIndex = selectedIndex
        selectedIndex = position
        SPUtils.putInt(SP_PAINT_INDEX, selectedIndex)
        notifyItemChanged(lastSelectedIndex)
        notifyItemChanged(selectedIndex)

        itemListener?.onItemClick(null, view, items[position], 0)
    }

    private inner class PaintDelegate(activity: Activity) : AdapterDelegate<Int>(activity, R.layout.au_item_paint) {
        override fun onCreateViewHolder(itemView: View): BaseViewHolder<Int> {
            return object : BaseViewHolder<Int>(itemView) {
                private val sivPaint = itemView.sivPaint

                override fun updateContents(item: Int) {
                    sivPaint.isSelected = selectedIndex == adapterPosition
                    sivPaint.setImageResource(item)
                }
            }
        }
    }
}