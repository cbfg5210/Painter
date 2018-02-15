package com.ue.painter

import android.app.Activity
import android.support.v4.content.ContextCompat
import android.view.View
import com.ue.adapterdelegate.AdapterDelegate
import com.ue.adapterdelegate.BaseViewHolder
import com.ue.adapterdelegate.DelegationAdapter
import com.ue.adapterdelegate.OnDelegateClickListener
import com.ue.painter.model.ModuleItem
import kotlinx.android.synthetic.main.item_module.view.*

/**
 * Created by hawk on 2018/1/25.
 */
class ModuleAdapter(activity: Activity) : DelegationAdapter<ModuleItem>() {
    companion object {
        private const val OUTLINE = 0
        private const val COLORING = 1
        private const val GRAFFITI = 2
        private const val PIXEL = 3
    }

    init {
        items.addAll(arrayListOf(
                ModuleItem(OUTLINE, R.drawable.svg_outline, activity.getString(R.string.module_outline), activity.getString(R.string.module_slogan_outline)),
                ModuleItem(COLORING, R.drawable.svg_coloring, activity.getString(R.string.module_coloring), activity.getString(R.string.module_slogan_coloring)),
                ModuleItem(GRAFFITI, R.drawable.svg_graffi, activity.getString(R.string.module_graffiti), activity.getString(R.string.module_slogan_graffiti)),
                ModuleItem(PIXEL, R.drawable.svg_pixel, activity.getString(R.string.module_pixel), activity.getString(R.string.module_slogan_pixel))))

        val delegate = ModuleDelegate(activity)
        delegate.delegateClickListener = object : OnDelegateClickListener {
            override fun onClick(v: View, position: Int) {
                val flag = items[position].flag
                when (flag) {
//                        OUTLINE -> v.activity.startActivity(Intent(v.activity, AutoDrawActivity::class.java))
//                        COLORING -> v.activity.startActivity(Intent(v.activity, MainListActivity::class.java))
//                        GRAFFITI -> v.activity.startActivity(Intent(v.activity, GraffitiActivity::class.java))
//                        PIXEL -> Intent()
                }
            }
        }
        addDelegate(delegate)
    }

    private class ModuleDelegate(activity: Activity) : AdapterDelegate<ModuleItem>(activity, R.layout.item_module) {
        override fun onCreateViewHolder(itemView: View): BaseViewHolder<ModuleItem> {
            return object : BaseViewHolder<ModuleItem>(itemView) {
                private val vgModule = itemView.vgModule
                private val ivModuleImage = itemView.ivModuleImage
                private val tvModuleName = itemView.tvModuleName
                private val tvModuleSlogan = itemView.tvModuleSlogan

                override fun updateContents(module: ModuleItem) {
                    ivModuleImage.setImageResource(module.image)
                    tvModuleName.text = module.name
                    tvModuleSlogan.text = module.slogan
                    val bgColor = ContextCompat.getColor(vgModule.context, if (adapterPosition % 3 == 0) R.color.white else R.color.col_f7f7f7)
                    vgModule.setBackgroundColor(bgColor)
                }
            }
        }
    }
}