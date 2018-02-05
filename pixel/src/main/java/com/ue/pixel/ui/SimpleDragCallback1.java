package com.ue.pixel.ui;

import android.support.annotation.IntDef;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;

import com.mikepenz.fastadapter.IDraggable;
import com.ue.pixel.R;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by hawk on 2018/2/5.
 */

public class SimpleDragCallback1 extends ItemTouchHelper.SimpleCallback {

    //our callback
    private ItemTouchCallback1 mCallbackItemTouch; // interface

    private int mFrom = RecyclerView.NO_POSITION;
    private int mTo = RecyclerView.NO_POSITION;

    private int mDirections = UP_DOWN;

    public static final int ALL = ItemTouchHelper.UP | ItemTouchHelper.DOWN | ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;
    public static final int UP_DOWN = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
    public static final int LEFT_RIGHT = ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;

    @IntDef({ALL, UP_DOWN, LEFT_RIGHT})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Directions {
    }

    public SimpleDragCallback1() {
        super(UP_DOWN, 0);
    }

    public SimpleDragCallback1(@SimpleDragCallback1.Directions int directions) {
        super(directions, 0);
        this.mDirections = directions;
    }

    public SimpleDragCallback1(@SimpleDragCallback1.Directions int directions, ItemTouchCallback1 itemTouchCallback) {
        super(directions, 0);
        this.mDirections = directions;
        this.mCallbackItemTouch = itemTouchCallback;
    }

    public SimpleDragCallback1(ItemTouchCallback1 itemTouchCallback) {
        super(UP_DOWN, 0);
        this.mCallbackItemTouch = itemTouchCallback;
    }

    @Override
    public boolean isLongPressDragEnabled() {
        return true;
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        // remember the from/to positions
        if (target.itemView.getTag(R.id.fastadapter_item) instanceof IDraggable) {
            if (((IDraggable) target.itemView.getTag(R.id.fastadapter_item)).isDraggable()) {
                if (mFrom == RecyclerView.NO_POSITION) {
                    mFrom = viewHolder.getAdapterPosition();
                }
                mTo = target.getAdapterPosition();
            }
        }
        return mCallbackItemTouch.itemTouchOnMove(viewHolder.getAdapterPosition(), target.getAdapterPosition()); // information to the interface
    }

    @Override
    public int getDragDirs(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
//        if (viewHolder.itemView.getTag(R.id.fastadapter_item) instanceof IDraggable) {
//            if (((IDraggable) viewHolder.itemView.getTag(R.id.fastadapter_item)).isDraggable()) {
//                return super.getDragDirs(recyclerView, viewHolder);
//            } else {
//                return 0;
//            }
//        } else {
            return mDirections;
//        }
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        // swiped disabled
    }

    @Override
    public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        super.clearView(recyclerView, viewHolder);
        if (mFrom != RecyclerView.NO_POSITION && mTo != RecyclerView.NO_POSITION) {
            if (mCallbackItemTouch != null) {
                mCallbackItemTouch.itemTouchDropped(mFrom, mTo);
            }
        }
        // reset the from/to positions
        mFrom = mTo = RecyclerView.NO_POSITION;
    }
}