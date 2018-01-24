package com.ue.graffiti.touch;

import android.graphics.PointF;

import com.ue.graffiti.model.Pel;
import com.ue.graffiti.widget.CanvasView;


public class DrawBesselTouch extends DrawTouch {
    private PointF beginPoint, endPoint;

    public DrawBesselTouch(CanvasView canvasView) {
        super(canvasView);

        beginPoint = new PointF();
        endPoint = new PointF();
    }

    @Override
    public void down1() {
        super.down1();

        if (!control) {
            //非拉伸曲线操作表明是新图元的开端
            beginPoint.set(getDownPoint()); //记录起点
            setNewPel(new Pel());
        }
    }

    @Override
    public void move() {
        super.move();

        getMovePoint().set(curPoint);
        getNewPel().getPath().reset();

        if (!control) {
            //非拉伸贝塞尔曲线操作
            getNewPel().getPath().moveTo(beginPoint.x, beginPoint.y);
            getNewPel().getPath().cubicTo(beginPoint.x, beginPoint.y, beginPoint.x, beginPoint.y, getMovePoint().x, getMovePoint().y);
        } else {
            getNewPel().getPath().moveTo(beginPoint.x, beginPoint.y);
            getNewPel().getPath().cubicTo(beginPoint.x, beginPoint.y, getMovePoint().x, getMovePoint().y, endPoint.x, endPoint.y);
        }

        mSimpleTouchListener.setSelectedPel(selectedPel = getNewPel());
    }

    @Override
    public void up() {
        PointF upPoint = new PointF();
        upPoint.set(curPoint);

        if (!control) {
            //非拉伸贝塞尔曲线操作则记录落脚点
            endPoint.set(upPoint);//记录落脚点
            control = true;
        } else {
            getNewPel().setClosure(false);
            super.up(); //最终敲定

            control = false;
        }
    }
}