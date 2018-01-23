package com.ue.graffiti.touch;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.graphics.Region;
import android.view.MotionEvent;
import android.view.View;

import com.ue.graffiti.R;
import com.ue.graffiti.event.OnMultiTouchListener;
import com.ue.graffiti.event.SimpleTouchListener;
import com.ue.graffiti.model.Pel;
import com.ue.graffiti.model.Step;
import com.ue.graffiti.widget.CanvasView;

import java.util.List;
import java.util.Stack;

//触摸类
public class Touch implements View.OnTouchListener {
    //获取undo
    protected Stack<Step> undoStack;
    // 图元链表// 屏幕宽高
    protected List<Pel> pelList;
    // 画布裁剪区域
    protected Region clipRegion;
    // 当前选中图元
    protected Pel selectedPel;
    //重绘画布
    protected Canvas savedCanvas;
    // 当前重绘位图
    protected Bitmap savedBitmap;
    //当前第一只手指事件坐标
    public PointF curPoint;
    //当前第二只手指事件坐标
    protected PointF secPoint;
    //特殊处理用
    //贝塞尔曲线切换时敲定
    public boolean control;
    //多边形时敲定
    public PointF beginPoint;

    protected SimpleTouchListener mSimpleTouchListener;
    protected boolean isProcessing;
    private ProgressDialog progressDialog;

    private OnMultiTouchListener mMultiTouchListener;

    public void setMultiTouchListener(OnMultiTouchListener multiTouchListener) {
        mMultiTouchListener = multiTouchListener;
    }

    public Touch(CanvasView canvasView) {
        savedCanvas = new Canvas();
        curPoint = new PointF();
        secPoint = new PointF();
        beginPoint = new PointF();

        if (canvasView == null) {
            return;
        }
        selectedPel = canvasView.getSelectedPel();
        //获取undo
        undoStack = canvasView.getUndoStack();
        // 画布裁剪区域
        clipRegion = canvasView.getClipRegion();
        // 图元链表// 屏幕宽高
        pelList = canvasView.getPelList();
    }

    public void setProcessing(boolean processing, String tip) {
        isProcessing = processing;
        if (isProcessing) {
            showProgressDialog(tip);
            return;
        }
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    public boolean isProcessing() {
        return isProcessing;
    }

    protected void showProgressDialog(String progressTip) {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(getContext());
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.setMessage(progressTip);
        if (!progressDialog.isShowing()) {
            progressDialog.show();
        }
    }

    protected void dismissProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    public void setTouchListener(CanvasView canvasView, SimpleTouchListener simpleTouchListener) {
        mSimpleTouchListener = simpleTouchListener;
        if (canvasView != null) {
            canvasView.setOnTouchListener(this);
        }
    }

    protected Context getContext() {
        return mSimpleTouchListener.getContext();
    }

    // 第一只手指按下
    public void down1() {
    }

    // 第二只手指按下
    protected void down2() {
    }

    // 手指移动
    public void move() {
    }

    // 手指抬起
    public void up() {
    }

    private void setCurPoint(PointF point) {
        curPoint.set(point);
    }

    private void setSecPoint(PointF point) {
        secPoint.set(point);
    }

    protected void updateSavedBitmap(boolean isInvalidate) {
        mSimpleTouchListener.updateSavedBitmap(savedCanvas, savedBitmap, pelList, selectedPel, isInvalidate);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (isProcessing) {
            showProgressDialog(v.getContext().getString(R.string.is_processing));
            return true;
        }
        //非传感器模式才响应屏幕
        if (mSimpleTouchListener.isSensorRegistered()) {
            return true;
        }
        //第一只手指坐标
        setCurPoint(new PointF(event.getX(0), event.getY(0)));
        //第二只手指坐标
        setSecPoint(event.getPointerCount() > 1 ? new PointF(event.getX(1), event.getY(1)) : new PointF(1, 1));

        int actionMasked = event.getActionMasked();
        switch (actionMasked) {
            // 第一只手指按下
            case MotionEvent.ACTION_DOWN:
                down1();
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                if (event.getPointerCount() > 2 && mMultiTouchListener != null) {
                    mMultiTouchListener.onMultiTouch();
                    return true;
                }
                // 第二个手指按下
                down2();
                break;
            case MotionEvent.ACTION_MOVE:
                move();
                break;
            // 第一只手指抬起
            case MotionEvent.ACTION_UP:
                //第二只手抬起
            case MotionEvent.ACTION_POINTER_UP:
                up();
                break;
        }
        mSimpleTouchListener.invalidate();
        return true;
    }
}