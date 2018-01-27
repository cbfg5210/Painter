package com.ue.coloring.feature.main

import android.app.Activity
import android.graphics.Point
import android.view.View
import com.ue.adapterdelegate.AdapterDelegate
import com.ue.adapterdelegate.BaseViewHolder
import com.ue.adapterdelegate.DelegationAdapter
import com.ue.adapterdelegate.OnDelegateClickListener
import com.ue.library.util.ImageLoaderUtils
import com.ue.painter.R
import com.ue.painter.WorksActivity
import com.ue.painter.model.LocalWork
import kotlinx.android.synthetic.main.co_item_work.view.*
import java.text.SimpleDateFormat

internal class LocalPaintAdapter(activity: Activity, localImageListBean: List<LocalWork>?) : DelegationAdapter<LocalWork>(), OnDelegateClickListener {
    private val imgWidth = (activity.resources.displayMetrics.widthPixels * 0.45f).toInt()
    val dateFormatter = SimpleDateFormat("yyyy/MM/dd HH:mm:ss")

    init {
        if (localImageListBean != null) items.addAll(localImageListBean)
        addDelegate(LocalPaintDelegate(activity).apply { delegateClickListener = this@LocalPaintAdapter })
    }

    override fun onClick(view: View, position: Int) {
        if (position < 0 || position > itemCount) return

        val vid = view.id
        when (vid) {
            R.id.ivThemeImage -> {
//                val item = items[holder.adapterPosition]
//                PaintActivity.start(activity, false, item.imageName, item.imageUrl)
            }
            R.id.ivDeleteWork -> {
//                val item = items[holder.adapterPosition]
//                val result = FileUtils.deleteWork(item.imageName)
//                if (result) {
//                    Toast.makeText(activity, R.string.co_delete_completed, Toast.LENGTH_SHORT).show()
//                    items.removeAt(holder.adapterPosition)
//                    notifyItemRemoved(holder.adapterPosition)
//                } else {
//                    Toast.makeText(activity, R.string.co_delete_paint_failed, Toast.LENGTH_SHORT).show()
//                }
            }
        }
    }

    private inner class LocalPaintDelegate(activity: Activity) : AdapterDelegate<LocalWork>(activity, R.layout.co_item_work) {
        override fun onCreateViewHolder(itemView: View): BaseViewHolder<LocalWork> {
            return object : BaseViewHolder<LocalWork>(itemView) {
                private val ivThemeImage = itemView.ivThemeImage
                private val tvLastModify = itemView.tvLastModify
                private val ivDeleteWork = itemView.ivDeleteWork

                override fun updateContents(item: LocalWork) {
                    ImageLoaderUtils.display(ivThemeImage, item.imageUrl, Point(imgWidth, (imgWidth / item.wvHRadio).toInt()), WorksActivity.TAG_WORKS)
                    tvLastModify.text = dateFormatter.format(item.lastModTimeStamp)
                }

                override fun setListeners(clickListener: View.OnClickListener) {
                    ivThemeImage.setOnClickListener(clickListener)
                    ivDeleteWork.setOnClickListener(clickListener)
                }
            }
        }
    }
}