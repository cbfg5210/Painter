package com.ue.graffiti.touch;

import android.graphics.PointF;

import com.ue.graffiti.model.Pel;
import com.ue.graffiti.widget.CanvasView;


public class DrawFreehandTouch extends DrawTouch {
    private PointF lastPoint;

    public DrawFreehandTouch(CanvasView canvasView) {
        super(canvasView);
        lastPoint = new PointF();
    }

    @Override
    public void down1() {
        super.down1();
        lastPoint.set(downPoint);

        newPel = new Pel();
        newPel.getPath().moveTo(lastPoint.x, lastPoint.y);
    }

    @Override
    public void move() {
        super.move();
        movePoint.set(curPoint);
        newPel.getPath().quadTo(lastPoint.x, lastPoint.y, (lastPoint.x + movePoint.x) / 2, (lastPoint.y + movePoint.y) / 2);
        lastPoint.set(movePoint);
        mSimpleTouchListener.setSelectedPel(selectedPel = newPel);
    }

    @Override
    public void up() {
        newPel.setClosure(true);
        super.up();
    }
}