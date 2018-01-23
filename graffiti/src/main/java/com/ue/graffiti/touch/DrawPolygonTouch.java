package com.ue.graffiti.touch;

import android.graphics.Path;
import android.graphics.PointF;

import com.ue.graffiti.model.Pel;
import com.ue.graffiti.util.TouchUtils;
import com.ue.graffiti.widget.CanvasView;

public class DrawPolygonTouch extends DrawTouch {
    private boolean firstDown = true;
    private Path lastPath;

    private final float MAX_CIRCLE = 50;

    public DrawPolygonTouch(CanvasView canvasView) {
        super(canvasView);
        lastPath = new Path();
    }

    @Override
    public void down1() {
        super.down1();

        if (!firstDown) {
            return;
        }
        // 画折线的第一笔
        beginPoint.set(downPoint);

        newPel = new Pel();
        newPel.path.moveTo(beginPoint.x, beginPoint.y);
        lastPath.set(newPel.path);

        firstDown = false;
    }

    @Override
    public void move() {
        super.move();

        movePoint.set(curPoint);

        newPel.path.set(lastPath);
        newPel.path.lineTo(movePoint.x, movePoint.y);

        mSimpleTouchListener.setSelectedPel(selectedPel = newPel);
    }

    @Override
    public void up() {
        PointF endPoint = new PointF();
        endPoint.set(curPoint);

        if (TouchUtils.distance(beginPoint, endPoint) <= MAX_CIRCLE) {
            newPel.path.set(lastPath);
            newPel.path.close();
            newPel.closure = true;
            super.up();

            firstDown = true;
        }
        lastPath.set(newPel.path);
    }
}
