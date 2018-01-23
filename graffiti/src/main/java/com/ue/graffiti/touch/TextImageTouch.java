package com.ue.graffiti.touch;

import android.graphics.PointF;

import com.ue.graffiti.constant.GestureFlags;
import com.ue.graffiti.event.BarSensorListener;
import com.ue.graffiti.util.TouchUtils;

public class TextImageTouch extends Touch {
    // 当前操作类型
    private int mode = GestureFlags.INSTANCE.getNONE();
    //平移偏移量
    private float dx, dy, oridx, oridy;
    // 缩放时两指最初放上时的距离
    private float scale, oriscale;
    //旋转量
    private float degree, oridegree;
    private PointF centerPoint, textPoint;

    private PointF frontPoint1, frontPoint2;

    private PointF downPoint;
    // 缩放时两指最初放上时的距离
    private float oriDist;
    //整个触摸过程在x和y方向上的偏移总量
    private float dis;

    private BarSensorListener mBarSensorListener;

    public void setBarSensorListener(BarSensorListener barSensorListener) {
        mBarSensorListener = barSensorListener;
    }

    public TextImageTouch(boolean isText, int canvasWidth, int canvasHeight) {
        super(null);
        downPoint = new PointF();
        frontPoint1 = new PointF();
        frontPoint2 = new PointF();

        dx = dy = oridx = oridy = 0;
        scale = oriscale = 1;
        degree = oridegree = 0;

        if (!isText) {
            return;
        }
        //计算文字坐标、文字宽高、文字中心
        textPoint = new PointF(canvasWidth / 2.5f, canvasHeight / 2.5f);
        centerPoint = new PointF();
        centerPoint.set(new PointF(textPoint.x, textPoint.y));
    }

    // 第一只手指按下
    @Override
    public void down1() {
        // 获取down事件的发生位置
        frontPoint1.set(curPoint);
        downPoint.set(curPoint);
        mode = GestureFlags.INSTANCE.getDRAG();
    }

    // 第二只手指按下
    @Override
    public void down2() {
        oriDist = TouchUtils.distance(curPoint, secPoint);
        if (oriDist > GestureFlags.INSTANCE.getMIN_ZOOM()) {
            // 距离小于50px才算是缩放
            mode = GestureFlags.INSTANCE.getZOOM();
        }
    }

    // 手指移动
    @Override
    public void move() {
        float dis1 = Math.abs(curPoint.x - frontPoint1.x) + Math.abs(curPoint.y - frontPoint1.y);
        float dis2 = 0;

        if (secPoint != null) {
            dis2 = Math.abs(secPoint.x - frontPoint2.x) + Math.abs(secPoint.y - frontPoint2.y);
            frontPoint2.set(secPoint);
        }
        dis += dis1 + dis2;

        frontPoint1.set(curPoint);

        if (mode == GestureFlags.INSTANCE.getDRAG()) {
            // 平移操作
            //计算距离
            dx = oridx + (curPoint.x - downPoint.x);
            dy = oridy + (curPoint.y - downPoint.y);
            return;
        }
        if (mode == GestureFlags.INSTANCE.getZOOM()) {
            // 缩放操作
            float newDist = TouchUtils.distance(curPoint, secPoint);
            //两指的垂直间距
            if (Math.abs(curPoint.y - secPoint.y) >= GestureFlags.INSTANCE.getMAX_DY()) {
                //判断是否需要转变为旋转模式
                //延续准备操作
                mode = GestureFlags.INSTANCE.getROTATE();
                downPoint.set(curPoint);
            } else if (newDist > GestureFlags.INSTANCE.getMIN_ZOOM()) {
                //<100仍然是正常缩放
                scale = oriscale * (newDist / oriDist);
            }
            return;
        }
        if (mode == GestureFlags.INSTANCE.getROTATE()) {
            // 旋转操作
            //两指的垂直间距
            if (Math.abs(curPoint.y - secPoint.y) < GestureFlags.INSTANCE.getMAX_DY()) {
                //判断是否需要转变为缩放模式
                mode = GestureFlags.INSTANCE.getZOOM();
                oriDist = TouchUtils.distance(curPoint, secPoint);
            } else {
                //>100仍然是正常旋转
                degree = (oridegree % 360) + degree();
            }
            return;
        }
    }

    // 手指抬起
    @Override
    public void up() {
        //改变文字的坐标
        if (dis < 10f) {
            dis = 0;
            if (mBarSensorListener != null) {
                mBarSensorListener.openTools();
            }
            return;
        }
        dis = 0;

        oridx = dx;
        oridy = dy;
        oriscale = scale;
        oridegree = degree;
        mode = GestureFlags.INSTANCE.getNONE();
    }

    // 旋转角度的计算
    private float degree() {
        // 获得两次down下时的距离
        float x = curPoint.x - downPoint.x;
        float y = curPoint.y - downPoint.y;

        float arc = (float) Math.sqrt(x * x + y * y);//弧长
        float radius = TouchUtils.distance(curPoint, secPoint) / 2;//半径

        float degrees = (arc / radius) * (180 / 3.14f);

        return degrees;
    }

    public void setCurPoint(PointF point) {
        curPoint.set(point);
    }

    public void setSecPoint(PointF point) {
        secPoint.set(point);
    }

    public float getDx() {
        return dx;
    }

    public float getDy() {
        return dy;
    }

    public float getScale() {
        return scale;
    }

    public float getDegree() {
        return degree;
    }

    public PointF getCenterPoint() {
        return centerPoint;
    }

    public PointF getTextPoint() {
        return textPoint;
    }

    public void setDis(float dis) {
        this.dis = dis;
    }

    public void clear() {
        dx = dy = oridx = oridy = 0;
        scale = oriscale = 1;
        degree = oridegree = 0;
    }
}