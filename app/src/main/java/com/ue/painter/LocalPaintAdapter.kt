package com.ue.coloring.feature.main

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.squareup.picasso.Picasso
import com.ue.painter.R
import com.ue.painter.model.LocalWork
import kotlinx.android.synthetic.main.co_item_work.view.*
import java.text.SimpleDateFormat

internal class LocalPaintAdapter(var context: Context, localImageListBean: List<LocalWork>?) : RecyclerView.Adapter<LocalPaintAdapter.ViewHolder>() {
    private val imgWidth = (context.resources.displayMetrics.widthPixels * 0.45f).toInt()
    val items = ArrayList<LocalWork>()
    val dateFormatter = SimpleDateFormat("yyyy/MM/dd HH:mm:ss")

    init {
        if (localImageListBean != null) items.addAll(localImageListBean)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(context).inflate(R.layout.co_item_work, parent, false)
        val holder = ViewHolder(v)

//        holder.ivThemeImage.setOnClickListener {
//            val item = items[holder.adapterPosition]
//            PaintActivity.start(context, false, item.imageName, item.imageUrl)
//        }

        holder.ivDeleteWork.setOnClickListener {
//            val item = items[holder.adapterPosition]
//            val result = FileUtils.deleteWork(item.imageName)
//            if (result) {
//                Toast.makeText(context, R.string.co_delete_completed, Toast.LENGTH_SHORT).show()
//                items.removeAt(holder.adapterPosition)
//                notifyItemRemoved(holder.adapterPosition)
//            } else {
//                Toast.makeText(context, R.string.co_delete_paint_failed, Toast.LENGTH_SHORT).show()
//            }
        }

        return holder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]

        Picasso.with(context)
                .load(item.imageUrl)
                .resize(imgWidth, (imgWidth / item.wvHRadio).toInt())
                .tag(WorksFragment.TAG_WORKS)
                .into(holder.ivThemeImage)

        holder.tvLastModify.text = dateFormatter.format(item.lastModTimeStamp)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    internal class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var ivThemeImage = itemView.ivThemeImage!!
        var tvLastModify = itemView.tvLastModify!!
        var ivDeleteWork = itemView.ivDeleteWork!!
    }
}