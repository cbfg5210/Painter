package com.ue.coloring.feature.theme

import android.app.Activity
import android.graphics.Point
import android.util.Log
import android.view.View
import com.ue.adapterdelegate.AdapterDelegate
import com.ue.adapterdelegate.BaseViewHolder
import com.ue.adapterdelegate.DelegationAdapter
import com.ue.adapterdelegate.OnDelegateClickListener
import com.ue.library.util.ImageLoaderUtils
import com.ue.painter.R
import com.ue.painter.WorksActivity
import com.ue.painter.model.VideoWork
import com.ue.painter.model.Work
import kotlinx.android.synthetic.main.item_work.view.*
import java.text.SimpleDateFormat

internal class WorksAdapter(activity: Activity, works: List<Work>? = null) : DelegationAdapter<Work>(), OnDelegateClickListener {
    private val imgWidth = (activity.resources.displayMetrics.widthPixels * 0.45f).toInt()
    val dateFormatter = SimpleDateFormat("yyyy/MM/dd HH:mm:ss")

    init {
        if (works != null) items.addAll(works)
        addDelegate(PictureWorkDelegate(activity).apply { delegateClickListener = this@WorksAdapter })
        addDelegate(VideoWorkDelegate(activity).apply { delegateClickListener = this@WorksAdapter })
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

    private inner class VideoWorkDelegate(activity: Activity) : AdapterDelegate<Work>(activity, R.layout.item_work) {
        override fun onCreateViewHolder(itemView: View): BaseViewHolder<Work> {
            return object : BaseViewHolder<Work>(itemView) {
                private val ivThemeImage = itemView.ivThemeImage
                private val tvLastModify = itemView.tvLastModify
                private val ivDeleteWork = itemView.ivDeleteWork

                override fun updateContents(item: Work) {
                    item as VideoWork
                    tvLastModify.text = dateFormatter.format(item.lastModTime)
                    item.picPath?.apply {
                        ImageLoaderUtils.display(ivThemeImage, this, Point(imgWidth, (imgWidth / item.wvHRadio).toInt()), WorksActivity.TAG_WORKS)
                    }
                }

                override fun setListeners(clickListener: View.OnClickListener) {
                    ivThemeImage.setOnClickListener(clickListener)
                    ivDeleteWork.setOnClickListener(clickListener)
                }
            }
        }
    }

    private inner class PictureWorkDelegate(activity: Activity) : AdapterDelegate<Work>(activity, R.layout.item_work) {
        override fun onCreateViewHolder(itemView: View): BaseViewHolder<Work> {
            return object : BaseViewHolder<Work>(itemView) {
                private val ivThemeImage = itemView.ivThemeImage
                private val tvLastModify = itemView.tvLastModify
                private val ivDeleteWork = itemView.ivDeleteWork

                override fun updateContents(item: Work) {
                    Log.e("PictureWorkDelegate", "updateContents: image=${item.path}")
                    ImageLoaderUtils.display(ivThemeImage, item.path, Point(imgWidth, (imgWidth / item.wvHRadio).toInt()), WorksActivity.TAG_WORKS)
                    tvLastModify.text = dateFormatter.format(item.lastModTime)
                }

                override fun setListeners(clickListener: View.OnClickListener) {
                    ivThemeImage.setOnClickListener(clickListener)
                    ivDeleteWork.setOnClickListener(clickListener)
                }
            }
        }
    }
}