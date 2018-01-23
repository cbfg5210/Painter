package com.ue.graffiti.model;

import com.ue.graffiti.touch.CrossFillTouch.ScanLine;

import java.util.List;

/**
 * Created by hawk on 2018/1/16.
 */

public class CrossFillStep extends Step {
    //初始色（若为白色，则undo的时候恢复成背景色；若为非白色，则undo的时候恢复成该色）
    public int initColor;
    //填充色
    public int fillColor;
    //扫描线链表
    public List<ScanLine> scanLinesList;

    /**
     * for CrossFillStep
     */
    public CrossFillStep(List<Pel> pelList, Pel pel, int initC, int fillC, List<ScanLine> scanLL) {
        super(pelList, pel);
        initColor = initC;
        fillColor = fillC;
        scanLinesList = scanLL;
    }
}