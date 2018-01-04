package com.ue.fingercoloring.feature.paint

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.gson.reflect.TypeToken
import com.ue.adapterdelegate.OnDelegateClickListener
import com.ue.fingercoloring.R
import com.ue.fingercoloring.constant.SPKeys
import com.ue.library.util.GsonHolder
import com.ue.library.util.SPUtils
import kotlinx.android.synthetic.main.item_picked_color.view.*

/**
 * Created by hawk on 2017/12/28.
 */
class PickedColorAdapter(context: Context) : RecyclerView.Adapter<PickedColorAdapter.ViewHolder>() {
    private val colors: MutableList<Int>
    private var lastPickedPos = 0
    private var pickedPos = 0
    var pickColorListener: OnDelegateClickListener? = null

    init {
        val pickedColorsStr = SPUtils.getString(SPKeys.PICKED_COLORS, "")
        if (TextUtils.isEmpty(pickedColorsStr)) {
            val pickedColorsTa = context.resources.obtainTypedArray(R.array.defPickedColors)
            colors = MutableList(pickedColorsTa.length(), { i -> pickedColorsTa.getColor(i, Color.BLACK) })
            pickedColorsTa.recycle()
        } else {
            colors = GsonHolder.gson.fromJson(pickedColorsStr, object : TypeToken<MutableList<Int>>() {}.type)
        }
    }

    fun getPickedColor(): Int {
        return colors[pickedPos]
    }

    fun updateColor(newColor: Int) {
        if (colors[pickedPos] != newColor) {
            colors[pickedPos] = newColor
            notifyItemChanged(pickedPos)
        }
    }

    fun savePickedColors() {
        SPUtils.putString(SPKeys.PICKED_COLORS, GsonHolder.gson.toJson(colors))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_picked_color, parent, false)
        val holder = ViewHolder(itemView)
        holder.ivPickedColor.setOnClickListener { v ->
            if (pickedPos == holder.adapterPosition) {
                return@setOnClickListener
            }
            lastPickedPos = pickedPos
            pickedPos = holder.adapterPosition
            notifyItemChanged(lastPickedPos)
            notifyItemChanged(pickedPos)

            pickColorListener?.onClick(v, colors[pickedPos])
        }
        return holder
    }

    override fun getItemCount(): Int {
        return colors.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.ivPickedColor.setImageDrawable(ColorDrawable(colors[position]))
        holder.ivPickedColor.isSelected = pickedPos == position
    }

    class ViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivPickedColor = itemView.ivPickedColor!!
    }
}