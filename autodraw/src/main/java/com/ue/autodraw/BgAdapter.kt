package com.ue.autodraw

import android.content.Context
import android.graphics.Point
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import com.ue.library.util.ImageLoaderUtils
import kotlinx.android.synthetic.main.au_item_bg.view.*

/**
 * Created by hawk on 2018/1/10.
 */
class BgAdapter(context: Context, private val bgs: IntArray) : RecyclerView.Adapter<BgAdapter.ViewHolder>() {
    private val displaySize = context.resources.getDimensionPixelSize(R.dimen.widget_size_50)
    var itemListener: AdapterView.OnItemClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.au_item_bg, parent, false)
        val holder = ViewHolder(itemView)
        itemView.setOnClickListener { itemListener?.onItemClick(null, it, bgs[holder.adapterPosition], 0) }
        return holder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        ImageLoaderUtils.display(holder.sivBg, bgs[position], Point(displaySize, displaySize))
    }

    override fun getItemCount(): Int {
        return bgs.size
    }

    class ViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val sivBg = itemView.sivBg
    }
}