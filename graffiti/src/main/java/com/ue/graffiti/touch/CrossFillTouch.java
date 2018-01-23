package com.ue.graffiti.touch;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;

import com.ue.graffiti.R;
import com.ue.graffiti.model.CrossFillStep;
import com.ue.graffiti.model.Pel;
import com.ue.graffiti.util.RxLifecycleUtils;
import com.ue.graffiti.util.TouchUtils;
import com.ue.graffiti.widget.CanvasView;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Stack;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class CrossFillTouch extends Touch {
    private Point originPoint;
    // 当前填充色
    private int fillColor;
    // 虚拟初始颜色
    private int oldColor;
    // 实际初始颜色
    private int initColor;
    private int curColor;
    private int[] pixels;
    // 源粒子栈
    private Stack<Point> pointStack;
    // 白色底的填充信息副本
    private Bitmap whiteBitmap;
    // 在填色时要被同时改变的背景图
    private Bitmap backgroundBitmap;
    private Canvas backgroundCanvas;
    private Bitmap copyOfBackgroundBitmap;

    private int MAX_WIDTH;
    private int MAX_HEIGHT;
    // 填充画笔
    private Paint fillPaint;
    // 位图宽高
    private int width, height;
    // 扫描线链表
    private List<ScanLine> scanLinesList;

    public CrossFillTouch(CanvasView canvasView) {
        super(canvasView);
        originPoint = new Point();
        // 像素堆栈
        pointStack = new Stack<Point>();
        backgroundCanvas = new Canvas();
        fillPaint = new Paint();
        fillPaint.setStrokeWidth(1);

        MAX_WIDTH = canvasView.getCanvasWidth();
        MAX_HEIGHT = canvasView.getCanvasHeight();
    }

    public Bitmap createWhiteBitmap() {
        // 创建缓冲位图
        Bitmap bitmap = Bitmap.createBitmap(MAX_WIDTH, MAX_HEIGHT, Config.ARGB_8888);
        savedCanvas.setBitmap(bitmap);
        TouchUtils.reprintFilledAreas(undoStack, bitmap);
        // 获取pelList对应的迭代器头结点
        ListIterator<Pel> pelIterator = pelList.listIterator();
        while (pelIterator.hasNext()) {
            Pel pel = pelIterator.next();
            if (!pel.equals(selectedPel)) {
                savedCanvas.drawPath(pel.getPath(), pel.getPaint());
            }
        }

        return bitmap;
    }

    @Override
    public void down1() {
        // 落下点没有超出画布
        if (curPoint.x < MAX_WIDTH && curPoint.x > 0 && curPoint.y < MAX_HEIGHT && curPoint.y > 0) {
            // 进度对话框处理填充耗时任务
            isProcessing = true;
            showProgressDialog(getContext().getString(R.string.is_filling_color));
            // 线性填充线链表（链表元素为填充直线的起始坐标点）
            scanLinesList = new LinkedList<ScanLine>();

            doFillAction();
        }
    }

    // 填充操作线程的实现类
    private void doFillAction() {
        Observable observable = Observable
                .create(e -> {
                    fill();
                    e.onNext(1);
                    e.onComplete();
                })
                .subscribeOn(Schedulers.single())
                .observeOn(AndroidSchedulers.mainThread());

        RxLifecycleUtils.bindUtilDestroy(getContext(), observable)
                .subscribe(o -> {
                    undoStack.push(new CrossFillStep(pelList, null, initColor, fillColor, scanLinesList));
                    updateSavedBitmap(true);
                    isProcessing = false;
                    dismissProgressDialog();
                });
    }

    /**
     * 扫描线种子填充
     */
    private void fill() {
        // 将当前有非白色背景的缓冲位图转换成白色背景的
        whiteBitmap = createWhiteBitmap();
        // 获取当前背景图片
        backgroundBitmap = mSimpleTouchListener.getBackgroundBitmap();
        copyOfBackgroundBitmap = mSimpleTouchListener.getCopyOfBackgroundBitmap();
        backgroundCanvas.setBitmap(backgroundBitmap);
        // 获得填充颜色
        fillColor = mSimpleTouchListener.getCurrentPaint().getColor();
        // 该点虚拟初始颜色
        oldColor = whiteBitmap.getPixel((int) curPoint.x, (int) curPoint.y);
        initColor = backgroundBitmap.getPixel((int) curPoint.x, (int) curPoint.y);
        if (initColor == copyOfBackgroundBitmap.getPixel((int) curPoint.x, (int) curPoint.y)) {
            initColor = Color.TRANSPARENT;
        }

        // 算法初始化
        // 清空源粒子栈
        pointStack.clear();
        // 以当前down下坐标作为初始源粒子
        originPoint.set((int) curPoint.x, (int) curPoint.y);
        // 入栈
        pointStack.push(originPoint);

        // 设置填充画笔颜色
        fillPaint.setColor(fillColor);

        width = whiteBitmap.getWidth();
        height = whiteBitmap.getHeight();
        pixels = new int[width * height];
        whiteBitmap.getPixels(pixels, 0, width, 0, 0, width, height);

        Point tmp;
        int x, y, xLeft, xRight, index;
        while (!pointStack.isEmpty()) {
            tmp = pointStack.pop();
            x = tmp.x;
            y = tmp.y;

            while (x > 0 && (curColor = pixels[index = width * y + x]) == oldColor && curColor != fillColor) {
                whiteBitmap.setPixel(x, y, fillColor);
                pixels[index] = fillColor;
                x--;
            }

            xLeft = x + 1;
            x = tmp.x + 1;

            while (x < width && (curColor = pixels[index = width * y + x]) == oldColor && curColor != fillColor) {
                whiteBitmap.setPixel(x, y, fillColor);
                pixels[index] = fillColor;
                x++;
            }
            xRight = x - 1;

            backgroundCanvas.drawLine(xLeft - 1, y, xRight + 2, y, fillPaint);

            ScanLine scanLine = new CrossFillTouch.ScanLine();
            scanLine.from.set(xLeft - 1, y);
            scanLine.to.set(xRight + 2, y);
            scanLinesList.add(scanLine);

            if (y > 0) {
                findNewSeedInline(xLeft, xRight, y - 1, fillPaint);
            }
            if (y + 1 < height) {
                findNewSeedInline(xLeft, xRight, y + 1, fillPaint);
            }
        }
    }

    private void findNewSeedInline(int XLeft, int XRight, int y, Paint paint) {
        Point p;
        Boolean pflag;
        int x = XLeft + 1;
        while (x <= XRight) {
            pflag = false;

            while ((curColor = pixels[width * y + x]) == oldColor && x < XRight && curColor != fillColor) {
                if (!pflag) {
                    pflag = true;
                }
                x++;
            }

            if (pflag) {
                p = ((x == XRight) && (curColor = pixels[width * y + x]) == oldColor && curColor != fillColor) ? new Point(x, y) : new Point(x - 1, y);
                pointStack.push(p);
            }

            // 处理向右跳过内部的无效点（处理区间右端有障碍点的情况）
            int xenter = x;
            while (pixels[width * y + x] != oldColor) {
                if (x >= XRight || x >= width) {
                    break;
                }
                x++;
            }
            if (xenter == x) {
                x++;
            }
        }
    }

    public class ScanLine {
        // 扫描线类
        // 起始点
        public Point from;
        // 终止点
        public Point to;

        ScanLine() {
            from = new Point();
            to = new Point();
        }
    }
}