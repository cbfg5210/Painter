package com.ue.graffiti.touch;

import android.graphics.PointF;

import com.ue.graffiti.constant.DrawPelFlags;
import com.ue.graffiti.model.DrawPelStep;
import com.ue.graffiti.model.Pel;
import com.ue.graffiti.widget.CanvasView;

//画图元触摸类
public class DrawTouch extends Touch {
    protected PointF downPoint;
    protected PointF movePoint;
    protected Pel newPel;

    protected DrawTouch(CanvasView canvasView) {
        super(canvasView);
        downPoint = new PointF();
        movePoint = new PointF();
    }

    @Override
    public void down1() {
        super.down1();
        downPoint.set(curPoint);
    }

    @Override
    public void up() {
        //敲定该图元的路径，区域，画笔,名称
        newPel.region.setPath(newPel.path, clipRegion);
        newPel.paint.set(mSimpleTouchListener.getCurrentPaint());
        //1.将新画好的图元存入图元链表中
        pelList.add(newPel);
        //2.包装好当前步骤 内的操作
        //将该“步”压入undo栈
        undoStack.push(new DrawPelStep(DrawPelFlags.DRAW,pelList, newPel));
        //3.更新重绘位图
        //刚才画的图元失去焦点
        mSimpleTouchListener.setSelectedPel(selectedPel = null);
        //重绘位图
        updateSavedBitmap(true);
    }
}
