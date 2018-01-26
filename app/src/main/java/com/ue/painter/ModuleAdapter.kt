package com.ue.painter

import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ue.library.util.PicassoUtils
import com.ue.painter.model.ModuleItem
import kotlinx.android.synthetic.main.item_module.view.*

/**
 * Created by hawk on 2018/1/25.
 */
class ModuleAdapter() : RecyclerView.Adapter<ModuleAdapter.ViewHolder>() {
    companion object {
        private val OUTLINE = 0
        private val COLORING = 1
        private val GRAFFITI = 2
        private val PIXEL = 3
    }

    private val modules = arrayOf(
            ModuleItem(OUTLINE, R.mipmap.ic_launcher, "aa", "aaaaa"),
            ModuleItem(COLORING, R.mipmap.ic_launcher, "bb", "aaaaa"),
            ModuleItem(GRAFFITI, R.mipmap.ic_launcher, "cc", "aaaaa"),
            ModuleItem(PIXEL, R.mipmap.ic_launcher, "dd", "aaaaa"))

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
        private val vgModule = itemView.vgModule!!
        private val ivModuleImage = itemView.ivModuleImage!!
        private val tvModuleName = itemView.tvModuleName!!
        private val tvModuleSlogan = itemView.tvModuleSlogan!!

        fun update(module: ModuleItem) {
            PicassoUtils.displayImage(itemView.context, ivModuleImage, module.image)
            tvModuleName.text = module.name
            tvModuleSlogan.text = module.slogan
            val bgColor = if (adapterPosition % 3 == 0) Color.LTGRAY else Color.GREEN
            vgModule.setBackgroundColor(bgColor)
        }
    }
}