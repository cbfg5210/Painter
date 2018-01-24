package com.ue.graffiti.model

import com.ue.graffiti.touch.CrossFillTouch.ScanLine

/**
 * Created by hawk on 2018/1/16.
 */
/**
 * for CrossFillStep
 */
//初始色（若为白色，则undo的时候恢复成背景色；若为非白色，则undo的时候恢复成该色）
//填充色
//扫描线链表
class CrossFillStep(pelList: MutableList<Pel>, pel: Pel?, var initColor: Int, var fillColor: Int, var scanLinesList: List<ScanLine>) : Step(pelList, pel)