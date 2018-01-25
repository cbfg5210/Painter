package com.ue.autodraw

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import com.ue.library.util.SPUtils
import kotlinx.android.synthetic.main.au_item_paint.view.*

/**
 * Created by hawk on 2018/1/10.
 */
class PaintAdapter(private val bgs: IntArray) : RecyclerView.Adapter<PaintAdapter.ViewHolder>() {
    companion object {
        private val SP_PAINT_INDEX = "sp_paint_index"
    }

    private var lastSelectedIndex = 0
    private var selectedIndex = SPUtils.getInt(SP_PAINT_INDEX, 0)

    var itemListener: AdapterView.OnItemClickListener? = null
        set(value) {
            field = value
            field?.onItemClick(null, null, bgs[selectedIndex], 0)
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.au_item_paint, parent, false)
        val holder = ViewHolder(itemView)
        itemView.setOnClickListener { v ->
            if (selectedIndex == holder.adapterPosition) {
                return@setOnClickListener
            }
            lastSelectedIndex = selectedIndex
            selectedIndex = holder.adapterPosition
            SPUtils.putInt(SP_PAINT_INDEX, selectedIndex)
            notifyItemChanged(lastSelectedIndex)
            notifyItemChanged(selectedIndex)

            itemListener?.onItemClick(null, v, bgs[holder.adapterPosition], 0)
        }
        return holder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.sivPaint.apply {
            isSelected = selectedIndex == position
            setImageResource(bgs[position])
        }
    }

    override fun getItemCount(): Int {
        return bgs.size
    }

    class ViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val sivPaint = itemView.sivPaint!!
    }
}