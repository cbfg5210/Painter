package com.ue.pixel.ui;

/**
 * Created by hawk on 2018/2/5.
 */

public interface ItemTouchCallback1 {

    /**
     * Called when an item has been dragged
     * This event is called on every item in a dragging chain
     *
     * @param oldPosition start position
     * @param newPosition end position
     * @return true if moved otherwise false
     */
    boolean itemTouchOnMove(int oldPosition, int newPosition);

    /**
     * Called when an item has been dropped
     * This event is only called once when the user stopped dragging the item
     *
     * @param oldPosition start position
     * @param newPosition end position
     */
    void itemTouchDropped(int oldPosition, int newPosition);
}
