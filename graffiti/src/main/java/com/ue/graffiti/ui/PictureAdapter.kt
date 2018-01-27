package com.ue.graffiti.ui

import android.app.Activity
import android.view.View
import com.ue.adapterdelegate.AdapterDelegate
import com.ue.adapterdelegate.BaseViewHolder
import com.ue.adapterdelegate.DelegationAdapter
import com.ue.adapterdelegate.OnDelegateClickListener
import com.ue.graffiti.R
import com.ue.graffiti.model.PictureItem
import kotlinx.android.synthetic.main.gr_item_picture.view.*

/**
 * Created by hawk on 2018/1/16.
 */

internal class PictureAdapter(activity: Activity, mPictureItems: List<PictureItem>?) : DelegationAdapter<PictureItem>(), OnDelegateClickListener {

    var itemClickListener: OnPictureItemListener? = null

    init {
        if (mPictureItems != null) items.addAll(mPictureItems)
        addDelegate(PictureDelegate(activity).apply { delegateClickListener = this@PictureAdapter })
    }

    override fun onClick(view: View, position: Int) {
        itemClickListener?.onItemClick(position, items[position])
    }

    private class PictureDelegate(activity: Activity) : AdapterDelegate<PictureItem>(activity, R.layout.gr_item_picture) {
        override fun onCreateViewHolder(itemView: View): BaseViewHolder<PictureItem> {
            return object : BaseViewHolder<PictureItem>(itemView) {
                private val tvPicture = itemView.tvPicture

                override fun updateContents(item: PictureItem) {
                    tvPicture.text = item.name
                    tvPicture.setCompoundDrawablesWithIntrinsicBounds(0, item.res, 0, 0)
                }
            }
        }

    }

    /*override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
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
    */

    interface OnPictureItemListener {
        fun onItemClick(position: Int, pictureItem: PictureItem)
    }
}