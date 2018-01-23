package com.ue.graffiti.model;

import java.util.List;

/**
 * Created by hawk on 2018/1/19.
 */

public class DrawPelStep extends Step {
    //图元所在链表位置
    public int location;
    //0:draw,1:copy,2:delete
    public int flag;

    public DrawPelStep(int flag, List<Pel> pelList, Pel pel) {
        super(pelList, pel);
        this.flag = flag;
        location = pelList.indexOf(pel);
    }
}
