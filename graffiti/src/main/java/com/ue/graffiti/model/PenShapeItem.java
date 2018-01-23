package com.ue.graffiti.model;

import com.ue.adapterdelegate.Item;

/**
 * Created by hawk on 2018/1/17.
 */

public class PenShapeItem implements Item {
    public int flag;
    public int image;
    //多type RecyclerView中有作用
    public int index;
    public String name;
    public boolean isChecked;

    public PenShapeItem(int flag, int image, String name) {
        this.flag = flag;
        this.image = image;
        this.name = name;
    }
}
