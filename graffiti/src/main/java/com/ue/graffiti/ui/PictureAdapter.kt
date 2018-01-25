package com.ue.graffiti.ui

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import com.ue.graffiti.R
import com.ue.graffiti.model.PictureItem
import kotlinx.android.synthetic.main.gr_item_picture.view.*

/**
 * Created by hawk on 2018/1/16.
 */

internal class PictureAdapter(private val mPictureItems: List<PictureItem>?) : RecyclerView.Adapter<PictureAdapter.ViewHolder>() {
    var itemClickListener: OnPictureItemListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.gr_item_picture, parent, false)
        val holder = ViewHolder(itemView)
        itemView.setOnClickListener {
            itemClickListener?.onItemClick(holder.adapterPosition, mPictureItems!![holder.adapterPosition])
        }
        return holder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = mPictureItems!![position]
        holder.tvPicture.text = item.name
        holder.tvPicture.setCompoundDrawablesWithIntrinsicBounds(0, item.res, 0, 0)
    }

    override fun getItemCount(): Int {
        return mPictureItems?.size ?: 0
    }

    internal class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvPicture: TextView = itemView.tvPicture!!
    }

    interface OnPictureItemListener {
        fun onItemClick(position: Int, pictureItem: PictureItem)
    }
}