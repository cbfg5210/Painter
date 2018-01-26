package com.ue.graffiti.touch

import android.graphics.*
import android.graphics.Bitmap.Config
import com.ue.graffiti.R
import com.ue.graffiti.model.CrossFillStep
import com.ue.library.util.bindUtilDestroy
import com.ue.graffiti.util.reprintFilledAreas
import com.ue.graffiti.widget.CanvasView
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.*

class CrossFillTouch(canvasView: CanvasView) : Touch(canvasView) {
    private val originPoint: Point
    // 当前填充色
    private var fillColor = 0
    // 虚拟初始颜色
    private var oldColor = 0
    // 实际初始颜色
    private var initColor = 0
    private var curColor = 0
    private var pixels: IntArray? = null
    // 源粒子栈
    private val pointStack: Stack<Point>
    // 白色底的填充信息副本
    private var whiteBitmap: Bitmap? = null
    // 在填色时要被同时改变的背景图
    private var backgroundBitmap: Bitmap? = null
    private val backgroundCanvas: Canvas
    private var copyOfBackgroundBitmap: Bitmap? = null

    private val MAX_WIDTH: Int
    private val MAX_HEIGHT: Int
    // 填充画笔
    private val fillPaint: Paint
    // 位图宽高
    private var width = 0
    private var height = 0
    // 扫描线链表
    private var scanLinesList: MutableList<ScanLine>? = null

    init {
        originPoint = Point()
        // 像素堆栈
        pointStack = Stack()
        backgroundCanvas = Canvas()
        fillPaint = Paint()
        fillPaint.strokeWidth = 1f

        MAX_WIDTH = canvasView.canvasWidth
        MAX_HEIGHT = canvasView.canvasHeight
    }

    fun createWhiteBitmap(): Bitmap {
        // 创建缓冲位图
        val bitmap = Bitmap.createBitmap(MAX_WIDTH, MAX_HEIGHT, Config.ARGB_8888)
        savedCanvas.setBitmap(bitmap)
        reprintFilledAreas(undoStack, bitmap)
        // 获取pelList对应的迭代器头结点
        val pelIterator = pelList.listIterator()
        while (pelIterator.hasNext()) {
            val pel = pelIterator.next()
            if (pel != selectedPel) {
                savedCanvas.drawPath(pel.path, pel.paint)
            }
        }

        return bitmap
    }

    override fun down1() {
        // 落下点没有超出画布
        if (curPoint.x < MAX_WIDTH && curPoint.x > 0 && curPoint.y < MAX_HEIGHT && curPoint.y > 0) {
            // 进度对话框处理填充耗时任务
            isProcessing = true
            showProgressDialog(context.getString(R.string.gr_is_filling_color))
            // 线性填充线链表（链表元素为填充直线的起始坐标点）
            scanLinesList = LinkedList()

            doFillAction()
        }
    }

    // 填充操作线程的实现类
    private fun doFillAction() {
        Observable
                .create<Any> { e ->
                    fill()
                    e.onNext(1)
                    e.onComplete()
                }
                .subscribeOn(Schedulers.single())
                .observeOn(AndroidSchedulers.mainThread())
                .bindUtilDestroy(context)
                .subscribe {
                    undoStack.push(CrossFillStep(pelList, null, initColor, fillColor, scanLinesList!!))
                    updateSavedBitmap(true)
                    isProcessing = false
                    dismissProgressDialog()
                }
    }

    /**
     * 扫描线种子填充
     */
    private fun fill() {
        // 将当前有非白色背景的缓冲位图转换成白色背景的
        whiteBitmap = createWhiteBitmap()
        // 获取当前背景图片
        backgroundBitmap = mSimpleTouchListener!!.getBackgroundBitmap()
        copyOfBackgroundBitmap = mSimpleTouchListener!!.getCopyOfBackgroundBitmap()
        backgroundCanvas.setBitmap(backgroundBitmap)
        // 获得填充颜色
        fillColor = mSimpleTouchListener!!.getCurrentPaint().color
        // 该点虚拟初始颜色
        oldColor = whiteBitmap!!.getPixel(curPoint.x.toInt(), curPoint.y.toInt())
        initColor = backgroundBitmap!!.getPixel(curPoint.x.toInt(), curPoint.y.toInt())
        if (initColor == copyOfBackgroundBitmap!!.getPixel(curPoint.x.toInt(), curPoint.y.toInt())) {
            initColor = Color.TRANSPARENT
        }

        // 算法初始化
        // 清空源粒子栈
        pointStack.clear()
        // 以当前down下坐标作为初始源粒子
        originPoint.set(curPoint.x.toInt(), curPoint.y.toInt())
        // 入栈
        pointStack.push(originPoint)

        // 设置填充画笔颜色
        fillPaint.color = fillColor

        width = whiteBitmap!!.width
        height = whiteBitmap!!.height
        pixels = IntArray(width * height)
        whiteBitmap!!.getPixels(pixels, 0, width, 0, 0, width, height)

        var tmp: Point
        var x: Int
        var y: Int
        var xLeft: Int
        var xRight: Int
        var index: Int
        while (!pointStack.isEmpty()) {
            tmp = pointStack.pop()
            x = tmp.x
            y = tmp.y

            while (x > 0) {
                index = width * y + x
                curColor = pixels!![index]
                if (curColor != oldColor || curColor == fillColor) {
                    break;
                }
                whiteBitmap!!.setPixel(x, y, fillColor)
                pixels!![index] = fillColor
                x--
            }

            xLeft = x + 1
            x = tmp.x + 1

            while (x < width) {
                index = width * y + x
                curColor = pixels!![index]
                if (curColor != oldColor || curColor == fillColor) {
                    break;
                }
                whiteBitmap!!.setPixel(x, y, fillColor)
                pixels!![index] = fillColor
                x++
            }
            xRight = x - 1

            backgroundCanvas.drawLine((xLeft - 1).toFloat(), y.toFloat(), (xRight + 2).toFloat(), y.toFloat(), fillPaint)

            val scanLine = ScanLine()
            scanLine.from.set(xLeft - 1, y)
            scanLine.to.set(xRight + 2, y)
            scanLinesList!!.add(scanLine)

            if (y > 0) {
                findNewSeedInline(xLeft, xRight, y - 1, fillPaint)
            }
            if (y + 1 < height) {
                findNewSeedInline(xLeft, xRight, y + 1, fillPaint)
            }
        }
    }

    private fun findNewSeedInline(XLeft: Int, XRight: Int, y: Int, paint: Paint) {
        var p: Point
        var pflag: Boolean
        var x = XLeft + 1
        while (x <= XRight) {
            pflag = false

            while (true) {
                curColor = pixels!![width * y + x]
                if (curColor != oldColor || x >= XRight || curColor == fillColor) {
                    break;
                }
                if (!pflag) {
                    pflag = true
                }
                x++
            }

            if (pflag) {
                p = Point(x - 1, y)
                if (x == XRight) {
                    curColor = pixels!![width * y + x]
                    if (curColor == oldColor && curColor != fillColor) {
                        p = Point(x, y)
                    }
                }
                pointStack.push(p)
            }

            // 处理向右跳过内部的无效点（处理区间右端有障碍点的情况）
            val xenter = x
            while (pixels!![width * y + x] != oldColor) {
                if (x >= XRight || x >= width) {
                    break
                }
                x++
            }
            if (xenter == x) {
                x++
            }
        }
    }

    inner class ScanLine internal constructor() {
        // 扫描线类
        // 起始点
        var from: Point = Point()
        // 终止点
        var to: Point = Point()
    }
}