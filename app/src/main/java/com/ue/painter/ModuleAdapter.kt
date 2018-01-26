package com.ue.painter

import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ue.painter.model.ModuleItem
import kotlinx.android.synthetic.main.item_module.view.*

/**
 * Created by hawk on 2018/1/25.
 */
class ModuleAdapter(context: Context) : RecyclerView.Adapter<ModuleAdapter.ViewHolder>() {
    companion object {
        private val OUTLINE = 0
        private val COLORING = 1
        private val GRAFFITI = 2
        private val PIXEL = 3
    }

    private val modules = arrayOf(
            ModuleItem(OUTLINE, R.drawable.svg_outline, context.getString(R.string.module_outline), context.getString(R.string.module_slogan_outline)),
            ModuleItem(COLORING, R.drawable.svg_coloring, context.getString(R.string.module_coloring), context.getString(R.string.module_slogan_coloring)),
            ModuleItem(GRAFFITI, R.drawable.svg_graffi, context.getString(R.string.module_graffiti), context.getString(R.string.module_slogan_graffiti)),
            ModuleItem(PIXEL, R.drawable.svg_pixel, context.getString(R.string.module_pixel), context.getString(R.string.module_slogan_pixel)))

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_module, parent, false)
        val holder = ViewHolder(itemView)
        itemView.setOnClickListener({ v ->
            val flag = modules[holder.adapterPosition].flag
            when (flag) {
//                OUTLINE -> v.context.startActivity(Intent(parent.context, AutoDrawActivity::class.java))
//                COLORING -> v.context.startActivity(Intent(parent.context, MainListActivity::class.java))
//                GRAFFITI -> v.context.startActivity(Intent(parent.context, GraffitiActivity::class.java))
//                PIXEL -> Intent()
            }
        })
        return holder
    }

    override fun getItemCount(): Int {
        return modules.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.update(modules[position])
    }

    class ViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val vgModule = itemView.vgModule
        private val ivModuleImage = itemView.ivModuleImage
        private val tvModuleName = itemView.tvModuleName
        private val tvModuleSlogan = itemView.tvModuleSlogan

        fun update(module: ModuleItem) {
            ivModuleImage.setImageResource(module.image)
            tvModuleName.text = module.name
            tvModuleSlogan.text = module.slogan
            val bgColor = ContextCompat.getColor(vgModule.context, if (adapterPosition % 3 == 0) R.color.white else R.color.col_f7f7f7)
            vgModule.setBackgroundColor(bgColor)
        }
    }
}