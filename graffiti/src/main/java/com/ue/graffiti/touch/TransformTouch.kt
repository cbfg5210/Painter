package com.ue.graffiti.touch

import android.graphics.Matrix
import android.graphics.PointF
import com.ue.graffiti.constant.GestureFlags
import com.ue.graffiti.model.Pel
import com.ue.graffiti.model.TransformPelStep
import com.ue.graffiti.util.TouchUtils
import com.ue.graffiti.widget.CanvasView

//变换触摸类
class TransformTouch(canvasView: CanvasView) : Touch(canvasView) {
    //选中图元的最初因子
    private val savedMatrix: Matrix
    //变换因子（平移、缩放、旋转）
    private val transMatrix: Matrix
    //按下，移动，两指中点
    private val downPoint: PointF
    //缩放、旋转中心
    private val centerPoint: PointF
    //重绘图元
    private val savedPel: Pel
    // 当前操作类型
    private var mode = GestureFlags.NONE
    // 缩放时两指最初放上时的距离
    private var oriDist=0f
    //平移偏移量
    private var dx=0f
    private var dy=0f

    private var step: TransformPelStep? = null

    init {
        savedMatrix = Matrix()
        transMatrix = Matrix()

        downPoint = PointF()
        centerPoint = PointF()

        savedPel = Pel()
    }

    // 第一只手指按下
    override fun down1() {
        // 获取down事件的发生位置
        downPoint.set(curPoint)
        // 判断是否相交
        var minDisPel: Pel? = null
        var minHorizontalDis = java.lang.Float.MAX_VALUE
        var minVerticalDis = java.lang.Float.MAX_VALUE
        // 获取pelList对应的迭代器头结点
        val pelIterator = pelList.listIterator()
        while (pelIterator.hasNext()) {
            val pel = pelIterator.next()
            val rect = pel.region.bounds

            val leftDis = Math.abs(rect.left - downPoint.x)
            val rightDis = Math.abs(rect.right - downPoint.x)
            val horizontalDis = leftDis + rightDis

            val topDis = Math.abs(rect.top - downPoint.y)
            val bottomDis = Math.abs(rect.bottom - downPoint.y)
            val verticalDis = topDis + bottomDis

            if (horizontalDis < minHorizontalDis || verticalDis < minVerticalDis) {
                if (leftDis + rightDis < rect.width() + 5) {
                    if (topDis + bottomDis < rect.height() + 5) {
                        minDisPel = pel
                        minHorizontalDis = leftDis + rightDis
                        minVerticalDis = topDis + bottomDis
                    }
                }
            }
        }
        if (minDisPel == null) {
            //超过阈值未选中
            //同步CanvasView中当前选中的图元
            selectedPel = null
            mSimpleTouchListener!!.setSelectedPel(selectedPel)

            updateSavedBitmap(true)
            return
        }
        // 圆域扩展到最大是否有选中任何图元
        // 敲定该图元
        selectedPel = minDisPel
        mSimpleTouchListener!!.setSelectedPel(selectedPel!!)
        //计算选中图元的中心点
        centerPoint.set(TouchUtils.calPelCenterPoint(selectedPel!!))
        // 获取选中图元的初始matrix
        savedMatrix.set(TouchUtils.calPelSavedMatrix(selectedPel!!))
        //由已知信息构造该步骤
        //设置该步骤对应图元
        step = TransformPelStep(pelList, clipRegion, selectedPel!!)
        // 原始选中图元所在位置记忆到零时图元中去
        savedPel.path.set(selectedPel!!.path)

        updateSavedBitmap(true)

        mode = GestureFlags.DRAG
    }

    // 第二只手指按下
    public override fun down2() {
        oriDist = TouchUtils.distance(curPoint, secPoint)
        if (oriDist > GestureFlags.MIN_ZOOM && selectedPel != null) {
            // 距离小于50px才算是缩放
            takeOverSelectedPel()
            mode = GestureFlags.ZOOM
        }
    }

    // 手指移动
    override fun move() {
        if (selectedPel == null) {
            return
        }
        // 获取move事件的发生位置
        // 前提是要选中了图元
        if (mode == GestureFlags.DRAG) {
            // 平移操作
            dx = curPoint.x - downPoint.x//计算距离
            dy = curPoint.y - downPoint.y

            // 对选中图元施行平移变换
            transMatrix.set(savedMatrix)
            // 作用于平移变换因子
            transMatrix.postTranslate(dx, dy)

            selectedPel!!.path.set(savedPel.path)
            // 作用于图元
            selectedPel!!.path.transform(transMatrix)
            // 更新平移后路径所在区域
            selectedPel!!.region.setPath(selectedPel!!.path, clipRegion)
            return
        }
        if (mode == GestureFlags.ZOOM) {
            // 缩放操作
            val newDist = TouchUtils.distance(curPoint, secPoint)
            //两指的垂直间距
            val dy = Math.abs(curPoint.y - secPoint.y)
            if (dy >= GestureFlags.MAX_DY) {
                //判断是否需要转变为旋转模式
                //延续准备操作
                mode = GestureFlags.ROTATE
                takeOverSelectedPel()
                savedPel.path.set(selectedPel!!.path)
                downPoint.set(curPoint)
                return
            }
            if (newDist > GestureFlags.MIN_ZOOM) {
                //<100仍然是正常缩放
                val scale = newDist / oriDist

                transMatrix.set(savedMatrix)
                // 作用于缩放变换因子
                transMatrix.postScale(scale, scale, centerPoint.x, centerPoint.y)

                selectedPel!!.path.set(savedPel.path)
                // 作用于图元
                selectedPel!!.path.transform(transMatrix)
                // 更新平移后路径所在区域
                selectedPel!!.region.setPath(selectedPel!!.path, clipRegion)
            }
            return
        }
        if (mode == GestureFlags.ROTATE) {
            // 旋转操作
            //两指的垂直间距
            val dy = Math.abs(curPoint.y - secPoint.y)
            if (dy < GestureFlags.MAX_DY) {
                //判断是否需要转变为缩放模式
                mode = GestureFlags.ZOOM
                takeOverSelectedPel()
                savedPel.path.set(selectedPel!!.path)
                oriDist = TouchUtils.distance(curPoint, secPoint)
                return
            }
            //>100仍然是正常旋转
            transMatrix.set(savedMatrix)
            transMatrix.setRotate(degree(), centerPoint.x, centerPoint.y)

            selectedPel!!.path.set(savedPel.path)
            // 作用于图元
            selectedPel!!.path.transform(transMatrix)
            // 更新平移后路径所在区域
            selectedPel!!.region.setPath(selectedPel!!.path, clipRegion)
            return
        }
    }

    // 手指抬起
    override fun up() {
        //为判断是否属于“选中（即秒抬）”情况
        val disx = Math.abs(curPoint.x - downPoint.x)
        val disy = Math.abs(curPoint.y - downPoint.y)

        if ((disx > 2f || disy > 2f) && step != null) {
            //移动距离至少要满足大于2f
            //敲定当前对应步骤
            savedMatrix.set(transMatrix)
            //设置进行该次步骤后的变换因子
            step!!.toUndoMatrix = transMatrix
            //将该“步”压入undo栈
            undoStack.push(step)

            // 敲定此次操作的最终区域
            if (selectedPel != null) {
                //初始位置也同步更新
                savedPel.path.set(selectedPel!!.path)
            }
        }
        mode = GestureFlags.NONE
    }

    // 旋转角度的计算
    private fun degree(): Float {
        // 获得两次down下时的距离
        val x = curPoint.x - downPoint.x
        val y = curPoint.y - downPoint.y
        //弧长
        val arc = Math.sqrt((x * x + y * y).toDouble()).toFloat()
        //半径
        val radius = TouchUtils.distance(curPoint, secPoint) / 2

        return arc / radius * (180 / 3.14f)
    }

    private fun takeOverSelectedPel() {
        //接手变换到一般要进行其它不同变换操作的图元（如平移到某处后马上又缩放，如缩放到某处后马上又旋转）
        //起始变换因子为刚才的变换后因子
        savedMatrix.set(transMatrix)
        //重新计算图元中心点
        centerPoint.set(TouchUtils.calPelCenterPoint(selectedPel!!))
    }
}