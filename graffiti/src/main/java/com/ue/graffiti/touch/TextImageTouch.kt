package com.ue.graffiti.touch

import android.graphics.PointF
import com.ue.graffiti.constant.GestureFlags
import com.ue.graffiti.event.BarSensorListener
import com.ue.graffiti.util.distance

class TextImageTouch(isText: Boolean, canvasWidth: Int, canvasHeight: Int) : Touch(null) {
    // 当前操作类型
    private var mode = GestureFlags.NONE
    //平移偏移量
    var dx=0f
        private set
    var dy=0f
        private set
    private var oridx=0f
    private var oridy=0f
    // 缩放时两指最初放上时的距离
    var scale=0f
        private set
    private var oriscale=0f
    //旋转量
    var degree=0f
        private set
    private var oridegree=0f
    lateinit var centerPoint: PointF
    lateinit var textPoint: PointF

    private val frontPoint1: PointF
    private val frontPoint2: PointF

    private val downPoint: PointF
    // 缩放时两指最初放上时的距离
    private var oriDist=0f
    //整个触摸过程在x和y方向上的偏移总量
    private var dis=0f

    private var mBarSensorListener: BarSensorListener? = null

    fun setBarSensorListener(barSensorListener: BarSensorListener) {
        mBarSensorListener = barSensorListener
    }

    init {
        downPoint = PointF()
        frontPoint1 = PointF()
        frontPoint2 = PointF()

        oridy = 0f
        oridx = oridy
        dy = oridx
        dx = dy
        oriscale = 1f
        scale = oriscale
        oridegree = 0f
        degree = oridegree

        if (isText) {
            //计算文字坐标、文字宽高、文字中心
            textPoint = PointF(canvasWidth / 2.5f, canvasHeight / 2.5f)
            centerPoint = PointF()
            centerPoint.set(PointF(textPoint.x, textPoint.y))
        }
    }

    // 第一只手指按下
    override fun down1() {
        // 获取down事件的发生位置
        frontPoint1.set(curPoint)
        downPoint.set(curPoint)
        mode = GestureFlags.DRAG
    }

    // 第二只手指按下
    public override fun down2() {
        oriDist = distance(curPoint, secPoint)
        if (oriDist > GestureFlags.MIN_ZOOM) {
            // 距离小于50px才算是缩放
            mode = GestureFlags.ZOOM
        }
    }

    // 手指移动
    override fun move() {
        val dis1 = Math.abs(curPoint.x - frontPoint1.x) + Math.abs(curPoint.y - frontPoint1.y)
        var dis2 = 0f

        if (secPoint != null) {
            dis2 = Math.abs(secPoint.x - frontPoint2.x) + Math.abs(secPoint.y - frontPoint2.y)
            frontPoint2.set(secPoint)
        }
        dis += dis1 + dis2

        frontPoint1.set(curPoint)

        if (mode == GestureFlags.DRAG) {
            // 平移操作
            //计算距离
            dx = oridx + (curPoint.x - downPoint.x)
            dy = oridy + (curPoint.y - downPoint.y)
            return
        }
        if (mode == GestureFlags.ZOOM) {
            // 缩放操作
            val newDist = distance(curPoint, secPoint)
            //两指的垂直间距
            if (Math.abs(curPoint.y - secPoint.y) >= GestureFlags.MAX_DY) {
                //判断是否需要转变为旋转模式
                //延续准备操作
                mode = GestureFlags.ROTATE
                downPoint.set(curPoint)
            } else if (newDist > GestureFlags.MIN_ZOOM) {
                //<100仍然是正常缩放
                scale = oriscale * (newDist / oriDist)
            }
            return
        }
        if (mode == GestureFlags.ROTATE) {
            // 旋转操作
            //两指的垂直间距
            if (Math.abs(curPoint.y - secPoint.y) < GestureFlags.MAX_DY) {
                //判断是否需要转变为缩放模式
                mode = GestureFlags.ZOOM
                oriDist = distance(curPoint, secPoint)
            } else {
                //>100仍然是正常旋转
                degree = oridegree % 360 + degree()
            }
            return
        }
    }

    // 手指抬起
    override fun up() {
        //改变文字的坐标
        if (dis < 10f) {
            dis = 0f
            if (mBarSensorListener != null) {
                mBarSensorListener!!.openTools()
            }
            return
        }
        dis = 0f

        oridx = dx
        oridy = dy
        oriscale = scale
        oridegree = degree
        mode = GestureFlags.NONE
    }

    // 旋转角度的计算
    private fun degree(): Float {
        // 获得两次down下时的距离
        val x = curPoint.x - downPoint.x
        val y = curPoint.y - downPoint.y

        val arc = Math.sqrt((x * x + y * y).toDouble()).toFloat()//弧长
        val radius = distance(curPoint, secPoint) / 2//半径

        return arc / radius * (180 / 3.14f)
    }

    fun setCurrentPoint(point: PointF) {
        curPoint.set(point)
    }

    fun setSecondPoint(point: PointF) {
        secPoint.set(point)
    }

    fun setDis(dis: Float) {
        this.dis = dis
    }

    fun clear() {
        oridy = 0f
        oridx = oridy
        dy = oridx
        dx = dy
        oriscale = 1f
        scale = oriscale
        oridegree = 0f
        degree = oridegree
    }
}