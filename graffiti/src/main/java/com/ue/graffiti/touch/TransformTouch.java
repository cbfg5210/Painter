package com.ue.graffiti.touch;

import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.Rect;

import com.ue.graffiti.constant.GestureFlags;
import com.ue.graffiti.model.Pel;
import com.ue.graffiti.model.TransformPelStep;
import com.ue.graffiti.util.TouchUtils;
import com.ue.graffiti.widget.CanvasView;

import java.util.ListIterator;

//变换触摸类
public class TransformTouch extends Touch {
    //选中图元的最初因子
    private Matrix savedMatrix;
    //变换因子（平移、缩放、旋转）
    private Matrix transMatrix;
    //按下，移动，两指中点
    private PointF downPoint;
    //缩放、旋转中心
    private PointF centerPoint;
    //重绘图元
    private Pel savedPel;
    // 当前操作类型
    private int mode = GestureFlags.INSTANCE.getNONE();
    // 缩放时两指最初放上时的距离
    private float oriDist;
    //平移偏移量
    private float dx, dy;

    private TransformPelStep step;

    public TransformTouch(CanvasView canvasView) {
        super(canvasView);

        savedMatrix = new Matrix();
        transMatrix = new Matrix();

        downPoint = new PointF();
        centerPoint = new PointF();

        savedPel = new Pel();
    }

    // 第一只手指按下
    @Override
    public void down1() {
        // 获取down事件的发生位置
        downPoint.set(curPoint);
        // 判断是否相交
        Pel minDisPel = null;
        float minHorizontalDis = Float.MAX_VALUE;
        float minVerticalDis = Float.MAX_VALUE;
        // 获取pelList对应的迭代器头结点
        ListIterator<Pel> pelIterator = pelList.listIterator();
        while (pelIterator.hasNext()) {
            Pel pel = pelIterator.next();
            Rect rect = (pel.getRegion()).getBounds();

            float leftDis = Math.abs(rect.left - downPoint.x);
            float rightDis = Math.abs(rect.right - downPoint.x);
            float horizontalDis = leftDis + rightDis;

            float topDis = Math.abs(rect.top - downPoint.y);
            float bottomDis = Math.abs(rect.bottom - downPoint.y);
            float verticalDis = topDis + bottomDis;

            if (horizontalDis < minHorizontalDis || verticalDis < minVerticalDis) {
                if (leftDis + rightDis < rect.width() + 5) {
                    if (topDis + bottomDis < rect.height() + 5) {
                        minDisPel = pel;
                        minHorizontalDis = leftDis + rightDis;
                        minVerticalDis = topDis + bottomDis;
                    }
                }
            }
        }
        if (minDisPel == null) {
            //超过阈值未选中
            //同步CanvasView中当前选中的图元
            mSimpleTouchListener.setSelectedPel(selectedPel = null);

            updateSavedBitmap(true);
            return;
        }
        // 圆域扩展到最大是否有选中任何图元
        // 敲定该图元
        mSimpleTouchListener.setSelectedPel(selectedPel = minDisPel);
        //计算选中图元的中心点
        centerPoint.set(TouchUtils.calPelCenterPoint(selectedPel));
        // 获取选中图元的初始matrix
        savedMatrix.set(TouchUtils.calPelSavedMatrix(selectedPel));
        //由已知信息构造该步骤
        //设置该步骤对应图元
        step = new TransformPelStep(pelList, clipRegion, selectedPel);
        // 原始选中图元所在位置记忆到零时图元中去
        savedPel.getPath().set(selectedPel.getPath());

        updateSavedBitmap(true);

        mode = GestureFlags.INSTANCE.getDRAG();
    }

    // 第二只手指按下
    @Override
    public void down2() {
        oriDist = TouchUtils.distance(curPoint, secPoint);
        if (oriDist > GestureFlags.INSTANCE.getMIN_ZOOM() && selectedPel != null) {
            // 距离小于50px才算是缩放
            takeOverSelectedPel();
            mode = GestureFlags.INSTANCE.getZOOM();
        }
    }

    // 手指移动
    @Override
    public void move() {
        if (selectedPel == null) {
            return;
        }
        // 获取move事件的发生位置
        // 前提是要选中了图元
        if (mode == GestureFlags.INSTANCE.getDRAG()) {
            // 平移操作
            dx = curPoint.x - downPoint.x;//计算距离
            dy = curPoint.y - downPoint.y;

            // 对选中图元施行平移变换
            transMatrix.set(savedMatrix);
            // 作用于平移变换因子
            transMatrix.postTranslate(dx, dy);

            selectedPel.getPath().set(savedPel.getPath());
            // 作用于图元
            selectedPel.getPath().transform(transMatrix);
            // 更新平移后路径所在区域
            selectedPel.getRegion().setPath(selectedPel.getPath(), clipRegion);
            return;
        }
        if (mode == GestureFlags.INSTANCE.getZOOM()) {
            // 缩放操作
            float newDist = TouchUtils.distance(curPoint, secPoint);
            //两指的垂直间距
            float dy = Math.abs(curPoint.y - secPoint.y);
            if (dy >= GestureFlags.INSTANCE.getMAX_DY()) {
                //判断是否需要转变为旋转模式
                //延续准备操作
                mode = GestureFlags.INSTANCE.getROTATE();
                takeOverSelectedPel();
                savedPel.getPath().set(selectedPel.getPath());
                downPoint.set(curPoint);
                return;
            }
            if (newDist > GestureFlags.INSTANCE.getMIN_ZOOM()) {
                //<100仍然是正常缩放
                float scale = newDist / oriDist;

                transMatrix.set(savedMatrix);
                // 作用于缩放变换因子
                transMatrix.postScale(scale, scale, centerPoint.x, centerPoint.y);

                selectedPel.getPath().set(savedPel.getPath());
                // 作用于图元
                selectedPel.getPath().transform(transMatrix);
                // 更新平移后路径所在区域
                selectedPel.getRegion().setPath(selectedPel.getPath(), clipRegion);
            }
            return;
        }
        if (mode == GestureFlags.INSTANCE.getROTATE()) {
            // 旋转操作
            //两指的垂直间距
            float dy = Math.abs(curPoint.y - secPoint.y);
            if (dy < GestureFlags.INSTANCE.getMAX_DY()) {
                //判断是否需要转变为缩放模式
                mode = GestureFlags.INSTANCE.getZOOM();
                takeOverSelectedPel();
                savedPel.getPath().set(selectedPel.getPath());
                oriDist = TouchUtils.distance(curPoint, secPoint);
                return;
            }
            //>100仍然是正常旋转
            transMatrix.set(savedMatrix);
            transMatrix.setRotate(degree(), centerPoint.x, centerPoint.y);

            selectedPel.getPath().set(savedPel.getPath());
            // 作用于图元
            selectedPel.getPath().transform(transMatrix);
            // 更新平移后路径所在区域
            selectedPel.getRegion().setPath(selectedPel.getPath(), clipRegion);
            return;
        }
    }

    // 手指抬起
    @Override
    public void up() {
        //为判断是否属于“选中（即秒抬）”情况
        float disx = Math.abs(curPoint.x - downPoint.x);
        float disy = Math.abs(curPoint.y - downPoint.y);

        if ((disx > 2f || disy > 2f) && step != null) {
            //移动距离至少要满足大于2f
            //敲定当前对应步骤
            savedMatrix.set(transMatrix);
            //设置进行该次步骤后的变换因子
            step.setToUndoMatrix(transMatrix);
            //将该“步”压入undo栈
            undoStack.push(step);

            // 敲定此次操作的最终区域
            if (selectedPel != null) {
                //初始位置也同步更新
                savedPel.getPath().set(selectedPel.getPath());
            }
        }
        mode = GestureFlags.INSTANCE.getNONE();
    }

    // 旋转角度的计算
    private float degree() {
        // 获得两次down下时的距离
        float x = curPoint.x - downPoint.x;
        float y = curPoint.y - downPoint.y;
        //弧长
        float arc = (float) Math.sqrt(x * x + y * y);
        //半径
        float radius = TouchUtils.distance(curPoint, secPoint) / 2;

        float degrees = (arc / radius) * (180 / 3.14f);

        return degrees;
    }

    private void takeOverSelectedPel() {
        //接手变换到一般要进行其它不同变换操作的图元（如平移到某处后马上又缩放，如缩放到某处后马上又旋转）
        //起始变换因子为刚才的变换后因子
        savedMatrix.set(transMatrix);
        //重新计算图元中心点
        centerPoint.set(TouchUtils.calPelCenterPoint(selectedPel));
    }
}