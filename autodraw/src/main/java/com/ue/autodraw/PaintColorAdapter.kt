package com.ue.autodraw

import android.graphics.PorterDuff
import android.support.graphics.drawable.VectorDrawableCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import com.ue.library.util.SPUtils
import kotlinx.android.synthetic.main.item_paint_color.view.*

/**
 * Created by hawk on 2018/1/10.
 */
class PaintColorAdapter(private val bgs: IntArray) : RecyclerView.Adapter<PaintColorAdapter.ViewHolder>() {
    companion object {
        private val SP_PAINT_COLOR_INDEX = "sp_paint_color_index"
    }

    private var itemListener: AdapterView.OnItemClickListener? = null
    private var lastSelectedIndex: Int = 0
    private var selectedIndex: Int

    init {
        selectedIndex = SPUtils.getInt(SP_PAINT_COLOR_INDEX, 0)
        if (selectedIndex >= bgs.size) {
            selectedIndex = 0
        }
    }

    fun setItemListener(itemListener: AdapterView.OnItemClickListener) {
        this.itemListener = itemListener
        itemListener.onItemClick(null, null, bgs[selectedIndex], 0)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_paint_color, parent, false)
        val holder = ViewHolder(itemView)
        itemView.setOnClickListener { v ->
            if (selectedIndex == holder.adapterPosition) {
                return@setOnClickListener
            }
            lastSelectedIndex = selectedIndex
            selectedIndex = holder.adapterPosition
            SPUtils.putInt(SP_PAINT_COLOR_INDEX, selectedIndex)
            notifyItemChanged(lastSelectedIndex)
            notifyItemChanged(selectedIndex)

            itemListener?.onItemClick(null, v, bgs[selectedIndex], 0)
        }
        return holder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val vectorDrawable = holder.sivPaintColor.drawable as VectorDrawableCompat
        vectorDrawable.clearColorFilter()
        vectorDrawable.setColorFilter(bgs[position], PorterDuff.Mode.SRC_ATOP)

        holder.sivPaintColor.isSelected = selectedIndex == position
    }

    override fun getItemCount(): Int {
        return bgs.size
    }

    class ViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val sivPaintColor = itemView.sivPaintColor!!
    }
}