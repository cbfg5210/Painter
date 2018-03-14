package com.ue.coloring.feature.theme

import android.app.Activity
import android.graphics.Point
import android.view.View
import com.google.android.flexbox.FlexboxLayoutManager
import com.ue.adapterdelegate.AdapterDelegate
import com.ue.adapterdelegate.BaseViewHolder
import com.ue.adapterdelegate.DelegationAdapter
import com.ue.adapterdelegate.OnDelegateClickListener
import com.ue.coloring.R
import com.ue.coloring.constant.CoConstants.TAG_COLORING_THEMES
import com.ue.coloring.feature.paint.PaintActivity
import com.ue.coloring.model.ThemeItem
import com.ue.coloring.model.ThemeTitle
import com.ue.library.util.ImageLoaderUtils
import kotlinx.android.synthetic.main.co_item_theme.view.*
import kotlinx.android.synthetic.main.co_item_theme_title.view.*

/**
 * Created by hawk on 2017/12/24.
 */
internal class ThemeItemAdapter(private val activity: Activity, items: List<Any>?) : DelegationAdapter<Any>(), OnDelegateClickListener {
    companion object {
        private const val TYPE_TITLE = -1
    }

    init {
        if (items != null) this.items.addAll(items)

        addDelegate(TitleItemDelegate(activity).apply { delegateClickListener = this@ThemeItemAdapter })
        addDelegate(ThemeItemDelegate(activity).apply { delegateClickListener = this@ThemeItemAdapter })
    }

    override fun onClick(view: View, position: Int) {
        if (position < 0 || position >= itemCount) {
            return
        }
        if (view.id == R.id.ivThemeTerm) {
            val item = items[position] as ThemeItem
            PaintActivity.start(activity, true, item.name, item.imageUrl)
            return
        }

        if (view.tag != TYPE_TITLE) return

        val item = items[position + 1] as ThemeItem
        val visible = !item.visible

        var themeItem: Any
        var lastIndex = 1
        for (i in position + 1 until itemCount) {
            themeItem = items[i]
            if (themeItem !is ThemeItem) break
            lastIndex = i
            themeItem.visible = visible
        }
        //notifyItemRangeChanged(position + 1, i - 1);//会有异常
        if (visible) notifyItemRangeInserted(position + 1, lastIndex)
        else notifyItemRangeRemoved(position + 1, lastIndex)
    }

    private class TitleItemDelegate(activity: Activity) : AdapterDelegate<Any>(activity, R.layout.co_item_theme_title) {

        override fun isForViewType(item: Any) = item is ThemeTitle

        override fun onCreateViewHolder(itemView: View): BaseViewHolder<Any> {
            return object : BaseViewHolder<Any>(itemView) {
                val ivThemeImage = itemView.ivThemeImage
                val ivThemeName = itemView.ivThemeName

                override fun updateContents(item: Any) {
                    item as ThemeTitle
                    itemView.tag = TYPE_TITLE
                    ivThemeName.text = item.name
                    ImageLoaderUtils.display(ivThemeImage.ivThemeImage, item.imgUrl)
                }
            }
        }
    }

    private class ThemeItemDelegate(activity: Activity) : AdapterDelegate<Any>(activity, R.layout.co_item_theme) {
        private val imgSize: Int
        private val margin: Int

        init {
            val screenWidth = activity.resources.displayMetrics.widthPixels
            imgSize = (screenWidth * 0.32f).toInt()
            margin = (screenWidth - imgSize * 3) / 6
        }

        override fun isForViewType(item: Any) = item is ThemeItem

        override fun onCreateViewHolder(itemView: View): BaseViewHolder<Any> {
            return object : BaseViewHolder<Any>(itemView) {
                val ivThemeItem = itemView.ivThemeTerm

                override fun updateContents(item: Any) {
                    item as ThemeItem
                    val lp = itemView.layoutParams

                    if (!item.visible) {
                        lp.height = 0
                        return
                    }

                    if (lp is FlexboxLayoutManager.LayoutParams) {
                        lp.width = imgSize
                        lp.height = imgSize
                        lp.setMargins(margin, margin, margin, margin)
                    }
                    //resize:有效减少内存、加快速度
                    ImageLoaderUtils.display(ivThemeItem, item.imageUrl, Point(imgSize, imgSize), TAG_COLORING_THEMES)
                }
            }
        }
    }
}