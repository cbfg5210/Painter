package com.ue.graffiti.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.ue.graffiti.constant.DrawPelFlags
import com.ue.graffiti.event.OnStepListener
import com.ue.graffiti.model.*
import com.ue.graffiti.touch.CrossFillTouch.ScanLine
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

/**
 * Created by hawk on 2018/1/16.
 */
object StepUtils {
    //进undo栈时对List中图元的更新（子类覆写）
    fun toUndoUpdate(context: Context, step: Step, backgroundBitmap: Bitmap, stepListener: OnStepListener?): Disposable {
        val observable = Observable
                .create<Any> { e ->
                    step.apply {
                        when (this) {
                            is CrossFillStep -> undoCrossFillStep(this, backgroundBitmap)
                            is FillPelStep -> curPel!!.paint.set(newPaint)
                        //删除链表对应索引位置图元-//更新图元链表数据
                            is DrawPelStep -> if (flag == DrawPelFlags.DELETE) pelList.removeAt(location) else pelList.add(location, step.curPel!!)
                            is TransformPelStep -> {
                                curPel!!.path.transform(toUndoMatrix)
                                curPel!!.region.setPath(curPel!!.path, clipRegion)
                            }
                        }
                    }
                    e.onNext(1)
                    e.onComplete()
                }
                .subscribeOn(Schedulers.single())
                .observeOn(AndroidSchedulers.mainThread())

        return RxLifecycleUtils.bindUtilDestroy(context, observable)
                .subscribe { o ->
                    stepListener?.onComplete()
                }
    }

    //进redo栈时对List中图元的反悔（子类覆写）
    fun toRedoUpdate(context: Context, step: Step, backgroundBitmap: Bitmap, copyOfBackgroundBitmap: Bitmap, stepListener: OnStepListener?): Disposable {
        val observable = Observable
                .create<Any> { e ->
                    step.apply {
                        when (this) {
                        //进度对话框处理填充耗时任务
                            is CrossFillStep -> redoFillStep(this, backgroundBitmap, copyOfBackgroundBitmap)
                            is FillPelStep -> curPel!!.paint.set(oldPaint)
                        //更新图元链表数据-//删除链表对应索引位置图元
                            is DrawPelStep -> if (flag == DrawPelFlags.DELETE) pelList.add(location, curPel!!) else pelList.removeAt(location)
                            is TransformPelStep -> {
                                curPel!!.path.set(savedPel.path)
                                curPel!!.region.setPath(curPel!!.path, clipRegion)
                            }
                        }
                    }
                    e.onNext(1)
                    e.onComplete()
                }
                .subscribeOn(Schedulers.single())
                .observeOn(AndroidSchedulers.mainThread())

        return RxLifecycleUtils.bindUtilDestroy(context, observable)
                .subscribe { o ->
                    stepListener?.onComplete()
                }
    }

    /*
    * cross fill
    * */
    //重做填充线程
    private fun undoCrossFillStep(step: CrossFillStep, backgroundBitmap: Bitmap) {
        //设置重做填充颜色
        val backgroundCanvas = Canvas().apply { setBitmap(backgroundBitmap) }
        val paint = Paint().apply { color = step.fillColor }
        // 获取pelList对应的迭代器头结点
        val scanLineIterator = step.scanLinesList.listIterator()
        var scanLine: ScanLine
        while (scanLineIterator.hasNext()) {
            scanLine = scanLineIterator.next()
            backgroundCanvas.drawLine(scanLine.from.x.toFloat(), scanLine.from.y.toFloat(), scanLine.to.x.toFloat(), scanLine.to.y.toFloat(), paint)
        }
    }

    private fun redoFillStep(step: CrossFillStep, backgroundBitmap: Bitmap, copyOfBackgroundBitmap: Bitmap) {
        //扫描线种子填充
        //设置填充还原色
        // 获取pelList对应的迭代器头结点
        val scanLineIterator = step.scanLinesList.listIterator()
        if (step.initColor == Color.TRANSPARENT) {
            //背景色填充
            var scanLine: ScanLine
            while (scanLineIterator.hasNext()) {
                scanLine = scanLineIterator.next()
                var x = scanLine.from.x
                val y = scanLine.from.y
                while (x < scanLine.to.x) {
                    if (x < copyOfBackgroundBitmap.width) {
                        backgroundBitmap.setPixel(x, y, copyOfBackgroundBitmap.getPixel(x, y))
                    }
                    x++
                }
            }
            return
        }
        //用上一次的颜色填充
        val backgroundCanvas = Canvas().apply { setBitmap(backgroundBitmap) }
        val paint = Paint().apply { color = step.initColor }
        while (scanLineIterator.hasNext()) {
            val scanLine = scanLineIterator.next()
            backgroundCanvas.drawLine(scanLine.from.x.toFloat(), scanLine.from.y.toFloat(), scanLine.to.x.toFloat(), scanLine.to.y.toFloat(), paint)
        }
    }

    //填充到白底位图上
    fun fillInWhiteBitmap(step: CrossFillStep, bitmap: Bitmap) {
        //设置重做填充颜色
        //构造画布
        val whiteCanvas = Canvas().apply { setBitmap(bitmap) }
        val paint = Paint().apply { color = step.fillColor }
        // 获取pelList对应的迭代器头结点
        val scanLineIterator = step.scanLinesList.listIterator()
        while (scanLineIterator.hasNext()) {
            val scanLine = scanLineIterator.next()
            whiteCanvas.drawLine(scanLine.from.x.toFloat(), scanLine.from.y.toFloat(), scanLine.to.x.toFloat(), scanLine.to.y.toFloat(), paint)
        }
    }
}