package com.ue.graffiti.touch;

import android.graphics.Path;
import android.graphics.RectF;

import com.ue.graffiti.model.Pel;
import com.ue.graffiti.widget.CanvasView;

public class DrawOvalTouch extends DrawTouch {

    public DrawOvalTouch(CanvasView canvasView) {
        super(canvasView);
    }

    @Override
    public void move() {
        super.move();

        newPel = new Pel();
        movePoint.set(curPoint);
        newPel.getPath().addOval(new RectF(downPoint.x, downPoint.y, movePoint.x, movePoint.y), Path.Direction.CCW);
        mSimpleTouchListener.setSelectedPel(selectedPel = newPel);
    }

    @Override
    public void up() {
        newPel.setClosure(true);
        super.up();
    }
}
