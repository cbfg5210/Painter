package com.ue.graffiti.model

import com.ue.graffiti.touch.CrossFillTouch.ScanLine

/**
 * Created by hawk on 2018/1/16.
 */

class CrossFillStep
/**
 * for CrossFillStep
 */
(pelList: List<Pel>, pel: Pel, //初始色（若为白色，则undo的时候恢复成背景色；若为非白色，则undo的时候恢复成该色）
 var initColor: Int, //填充色
 var fillColor: Int, //扫描线链表
 var scanLinesList: List<ScanLine>) : Step(pelList, pel)