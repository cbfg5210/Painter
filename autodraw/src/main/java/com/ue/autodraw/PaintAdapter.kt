package com.ue.autodraw

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import kotlinx.android.synthetic.main.item_paint.view.*

/**
 * Created by hawk on 2018/1/10.
 */
class PaintAdapter(private val bgs: IntArray) : RecyclerView.Adapter<PaintAdapter.ViewHolder>() {
    var itemListener: AdapterView.OnItemClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_paint, parent, false)
        val holder = ViewHolder(itemView)
        itemView.setOnClickListener { v ->
            itemListener?.onItemClick(null, v, bgs[holder.adapterPosition], 0)
        }
        return holder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
//        ImageLoaderUtils.display(holder.itemView.context, holder.sivPaint, bgs[position])
        holder.sivPaint.setImageResource(bgs[position])
    }

    override fun getItemCount(): Int {
        return bgs.size
    }

    class ViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val sivPaint = itemView.sivPaint!!
    }
}