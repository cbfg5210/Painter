package com.ue.graffiti.model;

import java.util.List;

/**
 * Created by hawk on 2018/1/19.
 */

public class Step {
    // 图元链表
    public List<Pel> pelList;
    //最早放入undo的图元
    public Pel curPel;

    public Step(List<Pel> pelList, Pel pel) {
        this.pelList = pelList;
        this.curPel = pel;
    }
}