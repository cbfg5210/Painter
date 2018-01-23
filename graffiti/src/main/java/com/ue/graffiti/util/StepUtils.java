package com.ue.graffiti.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import com.ue.graffiti.constant.DrawPelFlags;
import com.ue.graffiti.event.OnStepListener;
import com.ue.graffiti.model.CrossFillStep;
import com.ue.graffiti.model.DrawPelStep;
import com.ue.graffiti.model.FillPelStep;
import com.ue.graffiti.model.Step;
import com.ue.graffiti.model.TransformPelStep;
import com.ue.graffiti.touch.CrossFillTouch.ScanLine;

import java.util.ListIterator;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by hawk on 2018/1/16.
 */

public class StepUtils {

    //进undo栈时对List中图元的更新（子类覆写）
    public static Disposable toUndoUpdate(Context context, Step step, Bitmap backgroundBitmap, OnStepListener stepListener) {
        Observable observable = Observable
                .create(e -> {
                    if (step instanceof CrossFillStep) {
                        undoCrossFillStep((CrossFillStep) step, backgroundBitmap);
                    } else if (step instanceof FillPelStep) {
                        step.curPel.paint.set(((FillPelStep) step).newPaint);
                    } else if (step instanceof DrawPelStep) {
                        DrawPelStep drawPelStep = (DrawPelStep) step;
                        if (drawPelStep.flag == DrawPelFlags.INSTANCE.getDELETE()) {
                            //删除链表对应索引位置图元
                            step.pelList.remove(drawPelStep.location);
                        } else {
                            //更新图元链表数据
                            step.pelList.add(drawPelStep.location, step.curPel);
                        }
                    } else if (step instanceof TransformPelStep) {
                        TransformPelStep transformPelStep = (TransformPelStep) step;
                        step.curPel.path.transform(transformPelStep.toUndoMatrix);
                        step.curPel.region.setPath(step.curPel.path, transformPelStep.clipRegion);
                    }
                    e.onNext(1);
                    e.onComplete();
                })
                .subscribeOn(Schedulers.single())
                .observeOn(AndroidSchedulers.mainThread());

        return RxLifecycleUtils.bindUtilDestroy(context, observable)
                .subscribe(o -> {
                    if (stepListener != null) {
                        stepListener.onComplete();
                    }
                });
    }

    //进redo栈时对List中图元的反悔（子类覆写）
    public static Disposable toRedoUpdate(Context context, Step step, Bitmap backgroundBitmap, Bitmap copyOfBackgroundBitmap, OnStepListener stepListener) {
        Observable observable = Observable
                .create(e -> {
                    if (step instanceof CrossFillStep) {
                        //进度对话框处理填充耗时任务
                        redoFillStep((CrossFillStep) step, backgroundBitmap, copyOfBackgroundBitmap);
                    } else if (step instanceof FillPelStep) {
                        step.curPel.paint.set(((FillPelStep) step).oldPaint);
                    } else if (step instanceof DrawPelStep) {
                        DrawPelStep drawPelStep = (DrawPelStep) step;
                        if (drawPelStep.flag == DrawPelFlags.INSTANCE.getDELETE()) {
                            //更新图元链表数据
                            step.pelList.add(drawPelStep.location, step.curPel);
                        } else {
                            //删除链表对应索引位置图元
                            step.pelList.remove(drawPelStep.location);
                        }
                    } else if (step instanceof TransformPelStep) {
                        TransformPelStep transformPelStep = (TransformPelStep) step;
                        step.curPel.path.set(transformPelStep.savedPel.path);
                        step.curPel.region.setPath(step.curPel.path, transformPelStep.clipRegion);
                    }
                    e.onNext(1);
                    e.onComplete();
                })
                .subscribeOn(Schedulers.single())
                .observeOn(AndroidSchedulers.mainThread());

        return RxLifecycleUtils.bindUtilDestroy(context, observable)
                .subscribe(o -> {
                    if (stepListener != null) {
                        stepListener.onComplete();
                    }
                });
    }

    /*
    * cross fill
    * */
    //重做填充线程
    private static void undoCrossFillStep(final CrossFillStep step, final Bitmap backgroundBitmap) {
        //设置重做填充颜色
        Canvas backgroundCanvas = new Canvas();
        backgroundCanvas.setBitmap(backgroundBitmap);
        Paint paint = new Paint();
        paint.setColor(step.fillColor);
        // 获取pelList对应的迭代器头结点
        ListIterator<ScanLine> scanLineIterator = step.scanLinesList.listIterator();
        ScanLine scanLine;
        while (scanLineIterator.hasNext()) {
            scanLine = scanLineIterator.next();
            backgroundCanvas.drawLine(scanLine.from.x, scanLine.from.y, scanLine.to.x, scanLine.to.y, paint);
        }
    }

    private static void redoFillStep(final CrossFillStep step, final Bitmap backgroundBitmap, final Bitmap copyOfBackgroundBitmap) {
        //扫描线种子填充
        //设置填充还原色
        // 获取pelList对应的迭代器头结点
        ListIterator<ScanLine> scanLineIterator = step.scanLinesList.listIterator();
        if (step.initColor == Color.TRANSPARENT) {
            //背景色填充
            ScanLine scanLine;
            while (scanLineIterator.hasNext()) {
                scanLine = scanLineIterator.next();
                for (int x = scanLine.from.x, y = scanLine.from.y; x < scanLine.to.x; x++) {
                    if (x < copyOfBackgroundBitmap.getWidth()) {
                        backgroundBitmap.setPixel(x, y, copyOfBackgroundBitmap.getPixel(x, y));
                    }
                }
            }
            return;
        }
        //用上一次的颜色填充
        Canvas backgroundCanvas = new Canvas();
        backgroundCanvas.setBitmap(backgroundBitmap);
        Paint paint = new Paint();
        paint.setColor(step.initColor);
        while (scanLineIterator.hasNext()) {
            ScanLine scanLine = scanLineIterator.next();
            backgroundCanvas.drawLine(scanLine.from.x, scanLine.from.y, scanLine.to.x, scanLine.to.y, paint);
        }
    }

    //填充到白底位图上
    public static void fillInWhiteBitmap(CrossFillStep step, Bitmap bitmap) {
        //设置重做填充颜色
        Canvas whiteCanvas = new Canvas();//构造画布
        whiteCanvas.setBitmap(bitmap);
        Paint paint = new Paint();
        paint.setColor(step.fillColor);

        ListIterator<ScanLine> scanlineIterator = step.scanLinesList.listIterator();// 获取pelList对应的迭代器头结点
        while (scanlineIterator.hasNext()) {
            ScanLine scanLine = scanlineIterator.next();
            whiteCanvas.drawLine(scanLine.from.x, scanLine.from.y, scanLine.to.x, scanLine.to.y, paint);
        }
    }
}
