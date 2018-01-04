package com.ue.fingercoloring.feature.main

import android.app.Activity
import android.support.v7.widget.RecyclerView
import android.view.View
import com.google.android.flexbox.FlexboxLayoutManager
import com.squareup.picasso.Picasso
import com.ue.adapterdelegate.BaseAdapterDelegate
import com.ue.adapterdelegate.DelegationAdapter
import com.ue.adapterdelegate.Item
import com.ue.adapterdelegate.OnDelegateClickListener
import com.ue.fingercoloring.R
import com.ue.fingercoloring.feature.paint.PaintActivity
import com.ue.fingercoloring.model.ThemeItem
import com.ue.fingercoloring.model.ThemeTitle
import com.ue.library.util.PicassoUtils
import kotlinx.android.synthetic.main.item_theme.view.*
import kotlinx.android.synthetic.main.item_theme_title.view.*

/**
 * Created by hawk on 2017/12/24.
 */
internal class ThemeItemAdapter(private val activity: Activity, items: List<Item>?) : DelegationAdapter<Item>(), OnDelegateClickListener {

    init {
        if (items != null) this.items.addAll(items)

        val titleItemDelegate = TitleItemDelegate(activity)
        titleItemDelegate.onDelegateClickListener = this
        this.addDelegate(titleItemDelegate)

        val themeItemDelegate = ThemeItemDelegate(activity)
        themeItemDelegate.onDelegateClickListener = this
        this.addDelegate(themeItemDelegate)
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
        if (view.tag == null || view.tag !is Int) {
            return
        }
        val tag = view.tag as Int
        if (tag == -1) {
            val item = items[position + 1] as ThemeItem
            val visible = !item.visible

            var i = 1
            while (position + i < itemCount && items[position + i] !is ThemeTitle)
                (items[position + i++] as ThemeItem).visible = visible

            //notifyItemRangeChanged(position + 1, i - 1);//会有异常
            if (visible)
                notifyItemRangeInserted(position + 1, i - 1)
            else
                notifyItemRangeRemoved(position + 1, i - 1)
        }
    }

    private class TitleItemDelegate(activity: Activity) : BaseAdapterDelegate<Item>(activity, R.layout.item_theme_title) {

        override fun onCreateViewHolder(itemView: View): RecyclerView.ViewHolder {
            itemView.tag = TYPE_TITLE
            return ViewHolder(itemView)
        }

        override fun isForViewType(item: Item): Boolean {
            return item is ThemeTitle
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, item: Item, payloads: List<Any>) {
            val vHolder = holder as ViewHolder
            val title = item as ThemeTitle
            val context = holder.itemView.context

            vHolder.ivThemeName.text = title.name
            PicassoUtils.displayImage(context, vHolder.ivThemeImage, title.imgUrl)
        }

        private class ViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val ivThemeImage = itemView.ivThemeImage!!
            val ivThemeName = itemView.ivThemeName!!
        }
    }

    private class ThemeItemDelegate(activity: Activity) : BaseAdapterDelegate<Item>(activity, R.layout.item_theme) {
        private val imgSize: Int
        private val margin: Int

        init {
            val screenWidth = activity.resources.displayMetrics.widthPixels
            imgSize = (screenWidth * 0.32f).toInt()
            margin = (screenWidth - imgSize * 3) / 6
        }

        override fun onCreateViewHolder(itemView: View): RecyclerView.ViewHolder {
            return ViewHolder(itemView)
        }

        override fun isForViewType(item: Item): Boolean {
            return item is ThemeItem
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, item: Item, payloads: List<Any>) {
            val vHolder = holder as ViewHolder
            val themeTerm = item as ThemeItem
            val lp = holder.itemView.layoutParams

            if (!themeTerm.visible) {
                lp.height = 0
                return
            }

            if (lp is FlexboxLayoutManager.LayoutParams) {
                lp.width = imgSize
                lp.height = imgSize
                lp.setMargins(margin, margin, margin, margin)
            }

            Picasso.with(holder.itemView.context)
                    .load(themeTerm.imageUrl)
                    .resize(imgSize, imgSize)//有效减少内存、加快速度
                    .tag(ThemesFragment.TAG_THEMES)
                    .into(vHolder.ivThemeItem)
        }

        private class ViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val ivThemeItem = itemView.ivThemeTerm!!
        }
    }

    companion object {
        private val TYPE_TITLE = -1
    }
}