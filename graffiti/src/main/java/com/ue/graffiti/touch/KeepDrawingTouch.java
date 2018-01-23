package com.ue.graffiti.touch;

import android.graphics.PointF;
import android.graphics.drawable.BitmapDrawable;
import android.widget.Toast;

import com.ue.graffiti.R;
import com.ue.graffiti.helper.DialogHelper;
import com.ue.graffiti.widget.CanvasView;

public class KeepDrawingTouch extends Touch {
    private PointF downPoint;
    private KeepDrawingTouchListener mKeepDrawingTouchListener;

    public void setKeepDrawingListener(KeepDrawingTouchListener keepDrawingTouchListener) {
        mKeepDrawingTouchListener = keepDrawingTouchListener;
    }

    public KeepDrawingTouch(CanvasView canvasView) {
        super(canvasView);
        downPoint = new PointF();
    }

    // 第一只手指按下
    @Override
    public void down1() {
        downPoint.set(curPoint);
        updateSavedBitmap(false);
        //调取起始点标志图片
        BitmapDrawable startFlag = (BitmapDrawable) getContext().getResources().getDrawable(R.drawable.img_startflag);
        savedCanvas.drawBitmap(startFlag.getBitmap(), downPoint.x, downPoint.y, null);

        String tip = "您确定以(" + (int) downPoint.x + "," + (int) downPoint.y + ")为起始点并开始重力绘图？";
        DialogHelper.showSensorDrawingDialog(getContext(), tip, (dialog, which) -> {
            if (mKeepDrawingTouchListener != null) {
                mKeepDrawingTouchListener.onDownPoint(downPoint);
                mKeepDrawingTouchListener.registerKeepDrawingSensor();
            }
            Toast.makeText(getContext(), "摆动手机画图吧", Toast.LENGTH_SHORT).show();
        });
    }

    public interface KeepDrawingTouchListener {
        void onDownPoint(PointF downPoint);

        void registerKeepDrawingSensor();
    }
}