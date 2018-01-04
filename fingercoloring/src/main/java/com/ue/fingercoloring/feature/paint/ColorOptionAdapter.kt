package com.ue.fingercoloring.feature.paint

import android.content.Context
import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ue.adapterdelegate.OnDelegateClickListener
import com.ue.fingercoloring.R
import kotlinx.android.synthetic.main.item_color_option.view.*

/**
 * Created by hawk on 2017/12/28.
 */
class ColorOptionAdapter(context: Context) : RecyclerView.Adapter<ColorOptionAdapter.ViewHolder>() {
    private val colors: MutableList<Int>
    private var listener: OnDelegateClickListener? = null
    private var lastClickedPos = -1

    fun setColorSelectedListener(listener: OnDelegateClickListener) {
        this.listener = listener
    }

    init {
        val paintColorsTa = context.resources.obtainTypedArray(R.array.paintColors)
        colors = MutableList(paintColorsTa.length(), { i -> paintColorsTa.getColor(i, Color.BLACK) })
        paintColorsTa.recycle()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.vPaintColor.setBackgroundColor(colors[position])
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_color_option, parent, false)
        val holder = ViewHolder(itemView)
        itemView.setOnClickListener { v ->
            if (lastClickedPos != holder.adapterPosition) {
                lastClickedPos = holder.adapterPosition
                listener?.onClick(v, colors[lastClickedPos])
            }
        }
        return holder
    }

    override fun getItemCount(): Int {
        return colors.size
    }

    class ViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val vPaintColor = itemView.vPaintColor!!
    }
}