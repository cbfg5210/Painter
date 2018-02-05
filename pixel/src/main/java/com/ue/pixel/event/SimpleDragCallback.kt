package com.ue.pixel.event

import android.support.annotation.IntDef
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper

import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy

/**
 * Created by hawk on 2018/2/5.
 */

class SimpleDragCallback : ItemTouchHelper.SimpleCallback {
    @IntDef(ALL.toLong(), UP_DOWN.toLong(), LEFT_RIGHT.toLong())
    @Retention(RetentionPolicy.SOURCE)
    annotation class Directions

    //our callback
    private var mCallbackItemTouch: ItemTouchCallback? = null

    private var mFrom = RecyclerView.NO_POSITION
    private var mTo = RecyclerView.NO_POSITION

    private var mDirections = UP_DOWN

    constructor() : super(UP_DOWN, 0)

    constructor(@SimpleDragCallback.Directions directions: Int) : super(directions, 0) {
        this.mDirections = directions
    }

    constructor(@SimpleDragCallback.Directions directions: Int, itemTouchCallback: ItemTouchCallback) : super(directions, 0) {
        this.mDirections = directions
        this.mCallbackItemTouch = itemTouchCallback
    }

    constructor(itemTouchCallback: ItemTouchCallback) : super(UP_DOWN, 0) {
        this.mCallbackItemTouch = itemTouchCallback
    }

    override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
        if (mFrom == RecyclerView.NO_POSITION) mFrom = viewHolder.adapterPosition
        mTo = target.adapterPosition
        mCallbackItemTouch?.itemTouchOnMove(viewHolder.adapterPosition, target.adapterPosition)
        return true
    }

    override fun getDragDirs(recyclerView: RecyclerView?, viewHolder: RecyclerView.ViewHolder?): Int {
        return mDirections
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
    }

    override fun clearView(recyclerView: RecyclerView?, viewHolder: RecyclerView.ViewHolder) {
        super.clearView(recyclerView, viewHolder)
        if (mFrom != RecyclerView.NO_POSITION && mTo != RecyclerView.NO_POSITION) {
            mCallbackItemTouch?.itemTouchDropped(mFrom, mTo)
        }
        // reset the from/to positions
        mTo = RecyclerView.NO_POSITION
        mFrom = mTo
    }

    companion object {
        const val ALL = ItemTouchHelper.UP or ItemTouchHelper.DOWN or ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        const val UP_DOWN = ItemTouchHelper.UP or ItemTouchHelper.DOWN
        const val LEFT_RIGHT = ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
    }
}