package com.ue.graffiti.util

import android.content.Context
import android.graphics.*
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import com.ue.graffiti.R
import com.ue.graffiti.constant.DrawPelFlags
import com.ue.graffiti.event.OnStepListener
import com.ue.graffiti.model.*
import com.ue.graffiti.touch.CrossFillTouch
import com.ue.library.util.bindUtilDestroy
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.*

/**
 * Created by hawk on 2018/1/24.
 */
/*
* pen utils
* */
/**
 * 线型
 * @param image
 * @return
 */
fun getPaintShapeByImage(image: Int, width: Int, matrix: Matrix? = null): PathEffect {
    var matrix = matrix
    if (matrix == null) {
        matrix = Matrix()
        matrix.setSkew(2f, 2f)
    }
    return when (image) {
        R.drawable.ic_line_solid -> CornerPathEffect(10f)
        R.drawable.ic_line_dashed -> DashPathEffect(floatArrayOf(20f, 20f), 0.5f)
        R.drawable.ic_line_dotted -> DashPathEffect(floatArrayOf(40f, 20f, 10f, 20f), 0.5f)
        R.drawable.ic_line_lighting -> DiscretePathEffect(5f, 9f)
        R.drawable.ic_line_oval -> {
            val path = Path().apply { addOval(RectF(0f, 0f, width.toFloat(), width.toFloat()), Path.Direction.CCW) }
            PathDashPathEffect(path, (width + 10).toFloat(), 0f, PathDashPathEffect.Style.ROTATE)
        }
        R.drawable.ic_line_rect -> {
            val path = Path().apply { addRect(RectF(0f, 0f, width.toFloat(), width.toFloat()), Path.Direction.CCW) }
            PathDashPathEffect(path, (width + 10).toFloat(), 0f, PathDashPathEffect.Style.ROTATE)
        }
        R.drawable.ic_line_brush -> {
            val path = Path().apply {
                addRect(RectF(0f, 0f, width.toFloat(), width.toFloat()), Path.Direction.CCW)
                transform(matrix)
            }
            PathDashPathEffect(path, 2f, 0f, PathDashPathEffect.Style.TRANSLATE)
        }
        R.drawable.ic_line_mark_pen -> {
            val path = Path().apply {
                addArc(RectF(0f, 0f, (width + 4).toFloat(), (width + 4).toFloat()), -90f, 90f)
                addArc(RectF(0f, 0f, (width + 4).toFloat(), (width + 4).toFloat()), 90f, -90f)
            }
            PathDashPathEffect(path, 2f, 0f, PathDashPathEffect.Style.TRANSLATE)
        }
        else -> CornerPathEffect(10f)
    }
}

/**
 * 特效
 *
 * @param image
 * @return
 */
fun getPaintEffectByImage(image: Int): MaskFilter? {
    return when (image) {
        R.drawable.ic_effect_solid -> null
        R.drawable.ic_effect_blur -> BlurMaskFilter(8f, BlurMaskFilter.Blur.NORMAL)
        R.drawable.ic_effect_hollow -> BlurMaskFilter(8f, BlurMaskFilter.Blur.OUTER)
        R.drawable.ic_effect_relievo -> EmbossMaskFilter(floatArrayOf(1f, 1f, 1f), 0.4f, 6f, 3.5f)
        else -> null
    }
}
/*pen utils*/

/**
 * touch utils
 */
// 填充区域重新打印
fun reprintFilledAreas(undoStack: Stack<Step>, bitmap: Bitmap) {
    // 若为填充步骤
    undoStack.filterIsInstance<CrossFillStep>()
            .forEach { fillInWhiteBitmap(it, bitmap) }
}

fun ensureBitmapRecycled(bitmap: Bitmap?) {
    //确保传入位图已经回收
    bitmap?.recycle()
}

fun calPelSavedMatrix(selectedPel: Pel): Matrix {
    val savedMatrix = Matrix()
    // 将Path封装成PathMeasure，方便获取path内的matrix用
    val pathMeasure = PathMeasure(selectedPel.path, true)
    pathMeasure.getMatrix(pathMeasure.length, savedMatrix, PathMeasure.POSITION_MATRIX_FLAG and PathMeasure.TANGENT_MATRIX_FLAG)

    return savedMatrix
}

fun calPelCenterPoint(selectedPel: Pel): PointF {
    val boundRect = Rect()
    selectedPel.region.getBounds(boundRect)
    return PointF(((boundRect.right + boundRect.left) / 2).toFloat(), ((boundRect.bottom + boundRect.top) / 2).toFloat())
}

fun distance(begin: PointF, end: PointF): Float {
    val x = begin.x - end.x
    val y = begin.y - end.y
    return Math.sqrt((x * x + y * y).toDouble()).toFloat()
}
/* touch utils */

/*
* step utils
* */
//进undo栈时对List中图元的更新（子类覆写）
fun toUndoUpdate(context: Context, step: Step, backgroundBitmap: Bitmap, stepListener: OnStepListener?): Disposable {
    return io.reactivex.Observable
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
            .bindUtilDestroy(context)
            .subscribe { stepListener?.onComplete() }
}

//进redo栈时对List中图元的反悔（子类覆写）
fun toRedoUpdate(context: Context, step: Step, backgroundBitmap: Bitmap, copyOfBackgroundBitmap: Bitmap, stepListener: OnStepListener?): Disposable {
    return io.reactivex.Observable
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
            .bindUtilDestroy(context)
            .subscribe { stepListener?.onComplete() }
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
    var scanLine: CrossFillTouch.ScanLine
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
        var scanLine: CrossFillTouch.ScanLine
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
/*step utils*/

/*
* load animation utils
* */
fun loadDrawTextImageAnimations(context: Context?, isVisible: Boolean): Array<Animation>? {
    context ?: return null
    return if (isVisible) arrayOf(
            AnimationUtils.loadAnimation(context, R.anim.gr_top_appear),
            AnimationUtils.loadAnimation(context, R.anim.gr_down_appear))
    else arrayOf(
            AnimationUtils.loadAnimation(context, R.anim.gr_top_disappear),
            AnimationUtils.loadAnimation(context, R.anim.gr_down_disappear))
}

fun loadToggleMenuAnimations(context: Context, isShown: Boolean): Array<Animation> {
    return if (isShown)
        arrayOf(AnimationUtils.loadAnimation(context, R.anim.gr_left_appear),
                AnimationUtils.loadAnimation(context, R.anim.gr_top_appear),
                AnimationUtils.loadAnimation(context, R.anim.gr_right_appear),
                AnimationUtils.loadAnimation(context, R.anim.gr_down_appear))
    else arrayOf(AnimationUtils.loadAnimation(context, R.anim.gr_left_disappear),
            AnimationUtils.loadAnimation(context, R.anim.gr_top_disappear),
            AnimationUtils.loadAnimation(context, R.anim.gr_right_disappear),
            AnimationUtils.loadAnimation(context, R.anim.gr_down_disappear))
}

/*load animation utils*/