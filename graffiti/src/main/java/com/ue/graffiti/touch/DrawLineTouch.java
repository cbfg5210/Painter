package com.ue.graffiti.touch;

import com.ue.graffiti.model.Pel;
import com.ue.graffiti.widget.CanvasView;

public class DrawLineTouch extends DrawTouch {

    public DrawLineTouch(CanvasView canvasView) {
        super(canvasView);
    }

    @Override
    public void move() {
        super.move();

        newPel = new Pel();
        movePoint.set(curPoint);

        newPel.path.moveTo(downPoint.x, downPoint.y);
        newPel.path.lineTo(movePoint.x, movePoint.y);
        newPel.path.lineTo(movePoint.x, movePoint.y + 1);

        mSimpleTouchListener.setSelectedPel(selectedPel = newPel);
    }

    @Override
    public void up() {
        newPel.closure = false;
        super.up();
    }
}
