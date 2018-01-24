package com.ue.graffiti.widget

import android.content.Context
import android.graphics.*
import android.graphics.Bitmap.Config
import android.graphics.drawable.BitmapDrawable
import android.util.AttributeSet
import android.view.View
import com.ue.graffiti.R
import com.ue.graffiti.constant.SPKeys
import com.ue.graffiti.event.OnMultiTouchListener
import com.ue.graffiti.event.SimpleTouchListener
import com.ue.graffiti.model.Pel
import com.ue.graffiti.model.Step
import com.ue.graffiti.touch.DrawFreehandTouch
import com.ue.graffiti.touch.Touch
import com.ue.graffiti.touch.TransformTouch
import com.ue.graffiti.util.PenUtils
import com.ue.graffiti.util.TouchUtils
import com.ue.library.util.SPUtils
import java.util.*

class CanvasView(context: Context, attrs: AttributeSet) : View(context, attrs), SimpleTouchListener {
    // 图元平移缩放
    // 动画画笔（变换相位用）
    private var phase = 0f
    // 动画效果画笔
    private val animPelPaint: Paint
    // 画画用的画笔
    private val drawPelPaint: Paint
    private val drawTextPaint: Paint
    private val drawPicturePaint: Paint

    private val currentPaint: Paint
    //画布宽
    /**
     * get()方法:获取CanvasView下指定成员
     */
    val canvasWidth: Int
    //画布高
    val canvasHeight: Int
    //undo栈
    val undoStack: Stack<Step>
    //redo栈
    private val redoStack: Stack<Step>
    // 图元链表
    private val pelList: MutableList<Pel>
    // 画布裁剪区域
    val clipRegion: Region
    // 当前被选中的图元
    private var selectedPel: Pel? = null
    // 重绘位图
    var savedBitmap: Bitmap? = null
        private set
    //重绘画布
    private val savedCanvas: Canvas

    private var backgroundBitmap: Bitmap? = null
    //原图片副本，清空或还原时用
    private var copyOfBackgroundBitmap: Bitmap? = null
    var originalBackgroundBitmap: Bitmap? = null
        private set
    //触摸操作
    var touch: Touch? = null
        set(childTouch) {
            field = childTouch
            touch!!.setTouchListener(this, this)
            if (mMultiTouchListener != null) {
                touch!!.setMultiTouchListener(mMultiTouchListener!!)
            }
        }
    private var cacheCanvas: Canvas? = null

    private var isSensorRegistered: Boolean = false
    private var mMultiTouchListener: OnMultiTouchListener? = null

    var paintColor: Int
        get() = currentPaint.color
        set(paintColor) {
            drawPelPaint.color = paintColor
            drawTextPaint.color = paintColor
        }

    fun setMultiTouchListener(multiTouchListener: OnMultiTouchListener) {
        mMultiTouchListener = multiTouchListener
        this.touch?.setMultiTouchListener(multiTouchListener)
    }

    override fun isSensorRegistered(): Boolean {
        return isSensorRegistered
    }

    fun setSensorRegistered(sensorRegistered: Boolean) {
        isSensorRegistered = sensorRegistered
    }

    init {
        //初始化画布宽高为屏幕宽高
        val metrics = resources.displayMetrics
        canvasWidth = metrics.widthPixels
        canvasHeight = metrics.heightPixels
        //初始化undo redo栈
        undoStack = Stack()
        redoStack = Stack()
        // 图元总链表
        pelList = LinkedList()
        savedCanvas = Canvas()
        //获取画布裁剪区域
        clipRegion = Region()
        //初始化为自由手绘操作
        touch = DrawFreehandTouch(this)

        val lastColor = SPUtils.getInt(SPKeys.SP_PAINT_COLOR, resources.getColor(R.color.col_298ecb))
        val lastStrokeWidth = SPUtils.getInt(SPKeys.SP_PAINT_SIZE, 1)
        val lastShapeImage = SPUtils.getInt(SPKeys.SP_PAINT_SHAPE_IMAGE, R.drawable.ic_line_solid)
        val lastEffectImage = SPUtils.getInt(SPKeys.SP_PAINT_EFFECT_IMAGE, R.drawable.ic_effect_solid)

        drawPelPaint = Paint(Paint.DITHER_FLAG).apply {
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            isAntiAlias = true
            isDither = true
            strokeJoin = Paint.Join.ROUND
            color = lastColor
            strokeWidth = lastStrokeWidth.toFloat()
            pathEffect = PenUtils.getPaintShapeByImage(lastShapeImage, 1, null)
            maskFilter = PenUtils.getPaintEffectByImage(lastEffectImage)
        }

        currentPaint = drawPelPaint
        animPelPaint = Paint(drawPelPaint)
        drawPicturePaint = Paint()

        drawTextPaint = Paint().apply {
            color = drawPelPaint.color
            textSize = 50f
        }

        initBitmap()
        updateSavedBitmap()
    }

    override fun getCurrentPaint(): Paint {
        return currentPaint
    }

    private fun initBitmap() {
        clipRegion.set(Rect(0, 0, canvasWidth, canvasHeight))
        val backgroundDrawable = this.resources.getDrawable(R.drawable.bg_canvas0) as BitmapDrawable
        val scaledBitmap = Bitmap.createScaledBitmap(backgroundDrawable.bitmap, canvasWidth, canvasHeight, true)

        TouchUtils.ensureBitmapRecycled(backgroundBitmap)
        backgroundBitmap = scaledBitmap.copy(Config.ARGB_8888, true)
        TouchUtils.ensureBitmapRecycled(scaledBitmap)

        TouchUtils.ensureBitmapRecycled(copyOfBackgroundBitmap)
        copyOfBackgroundBitmap = backgroundBitmap!!.copy(Config.ARGB_8888, true)

        TouchUtils.ensureBitmapRecycled(originalBackgroundBitmap)
        originalBackgroundBitmap = backgroundBitmap!!.copy(Config.ARGB_8888, true)

        cacheCanvas = Canvas()
        cacheCanvas!!.setBitmap(backgroundBitmap)
    }

    //重绘
    override fun onDraw(canvas: Canvas) {
        // 画其余图元
        canvas.drawBitmap(savedBitmap!!, 0f, 0f, Paint())
        selectedPel ?: return

        if (this.touch !is TransformTouch) {
            //画图状态不产生动态画笔效果
            canvas.drawPath(selectedPel!!.path, drawPelPaint)
            return
        }
        //选中状态才产生动态画笔效果
        setAnimPaint()
        canvas.drawPath(selectedPel!!.path, animPelPaint)
        // 画笔动画效果
        invalidate()
    }

    override fun updateSavedBitmap(canvas: Canvas, bitmap: Bitmap?, pelList: List<Pel>, selectedPel: Pel?, isInvalidate: Boolean) {
        var bitmap = bitmap
        //更新重绘背景位图用（当且仅当选择的图元有变化的时候才调用）
        //创建缓冲位图
        TouchUtils.ensureBitmapRecycled(bitmap)
        //由画布背景创建缓冲位图
        bitmap = backgroundBitmap!!.copy(Bitmap.Config.ARGB_8888, true)
        //与画布建立联系
        canvas.setBitmap(bitmap)
        //画除selectedPel外的所有图元
        drawPels(canvas, pelList, selectedPel)

        savedBitmap = bitmap

        if (isInvalidate) {
            invalidate()
        }
    }

    fun updateSavedBitmap() {
        updateSavedBitmap(savedCanvas, savedBitmap, pelList, selectedPel, true)
    }

    private fun drawPels(savedCanvas: Canvas, pelList: List<Pel>, selectedPel: Pel?) {
        // 获取pelList对应的迭代器头结点
        val pelIterator = pelList.listIterator()
        while (pelIterator.hasNext()) {
            val pel = pelIterator.next()
            //若是文本图元
            if (pel.text != null) {
                val text = pel.text!!
                savedCanvas.save()
                savedCanvas.translate(text.transDx, text.transDy)
                savedCanvas.scale(text.scale, text.scale, text.centerPoint.x, text.centerPoint.y)
                savedCanvas.rotate(text.degree, text.centerPoint.x, text.centerPoint.y)
                savedCanvas.drawText(text.content, text.beginPoint.x, text.beginPoint.y, text.paint)
                savedCanvas.restore()
                continue
            }
            if (pel.picture != null) {
                val picture = pel.picture!!
                savedCanvas.save()
                savedCanvas.translate(picture.transDx, picture.transDy)
                savedCanvas.scale(picture.scale, picture.scale, picture.centerPoint.x, picture.centerPoint.y)
                savedCanvas.rotate(picture.degree, picture.centerPoint.x, picture.centerPoint.y)
                savedCanvas.drawBitmap(picture.createContent(context), picture.beginPoint.x, picture.beginPoint.y, drawPicturePaint)
                savedCanvas.restore()
                continue
            }
            if (pel != selectedPel) {
                //若非选中的图元
                savedCanvas.drawPath(pel.path, pel.paint)
            }
        }
    }

    // 动画画笔更新
    private fun setAnimPaint() {
        // 变相位
        phase++

        val p = Path()
        // 路径单元是矩形（也可以为椭圆）
        p.addRect(RectF(0f, 0f, 6f, 3f), Path.Direction.CCW)
        // 设置路径效果
        val effect = PathDashPathEffect(p, 12f, phase, PathDashPathEffect.Style.ROTATE)
        animPelPaint.color = Color.BLACK
        animPelPaint.pathEffect = effect
    }

    fun getPelList(): MutableList<Pel> {
        return pelList
    }

    fun getSelectedPel(): Pel? {
        return selectedPel
    }

    override fun getBackgroundBitmap(): Bitmap {
        return backgroundBitmap!!
    }

    override fun getCopyOfBackgroundBitmap(): Bitmap {
        return copyOfBackgroundBitmap!!
    }

    /*
     * set()方法:设置CanvasView下指定成员
     */
    override fun setSelectedPel(pel: Pel?) {
        selectedPel = pel
    }

    fun setBackgroundBitmap(id: Int) {
        //以已提供选择的背景图片换画布
        val backgroundDrawable = this.resources.getDrawable(id) as BitmapDrawable
        val offeredBitmap = backgroundDrawable.bitmap

        TouchUtils.ensureBitmapRecycled(backgroundBitmap)
        backgroundBitmap = Bitmap.createScaledBitmap(offeredBitmap, canvasWidth, canvasHeight, true)

        TouchUtils.ensureBitmapRecycled(copyOfBackgroundBitmap)
        copyOfBackgroundBitmap = backgroundBitmap!!.copy(Config.ARGB_8888, true)

        TouchUtils.ensureBitmapRecycled(originalBackgroundBitmap)
        originalBackgroundBitmap = backgroundBitmap!!.copy(Config.ARGB_8888, true)

        TouchUtils.reprintFilledAreas(undoStack, backgroundBitmap!!)//填充区域重新打印
        updateSavedBitmap()
    }

    fun setBackgroundBitmap(photo: Bitmap) {
        //以图库或拍照得到的背景图片换画布
        TouchUtils.ensureBitmapRecycled(backgroundBitmap)
        backgroundBitmap = Bitmap.createScaledBitmap(photo, canvasWidth, canvasHeight, true)

        TouchUtils.ensureBitmapRecycled(copyOfBackgroundBitmap)
        copyOfBackgroundBitmap = backgroundBitmap!!.copy(Config.ARGB_8888, true)

        TouchUtils.ensureBitmapRecycled(originalBackgroundBitmap)
        originalBackgroundBitmap = backgroundBitmap!!.copy(Config.ARGB_8888, true)
        //填充区域重新打印
        TouchUtils.reprintFilledAreas(undoStack, backgroundBitmap!!)
        updateSavedBitmap()
    }

    fun setBackgroundBitmap() {
        //清空画布时将之前保存的副本背景作为重绘（去掉填充）
        TouchUtils.ensureBitmapRecycled(backgroundBitmap)
        backgroundBitmap = copyOfBackgroundBitmap!!.copy(Config.ARGB_8888, true)
        //填充区域重新打印
        TouchUtils.reprintFilledAreas(undoStack, backgroundBitmap!!)
        updateSavedBitmap()
    }

    fun clearData() {
        pelList.clear()
        undoStack.clear()
        redoStack.clear()
        //若有选中的图元失去焦点
        setSelectedPel(null)
    }

    fun clearRedoStack() {
        if (!redoStack.empty()) {
            redoStack.clear()
        }
    }

    fun addPel(newPel: Pel) {
        pelList.add(newPel)
    }

    fun pushUndoStack(step: Step) {
        undoStack.push(step)
    }

    fun pushRedoStack(step: Step) {
        redoStack.push(step)
    }

    fun removePel(pel: Pel) {
        pelList.remove(pel)
    }

    fun popUndoStack(): Step? {
        return if (undoStack.empty()) null else undoStack.pop()
    }

    fun popRedoStack(): Step? {
        return if (redoStack.empty()) null else redoStack.pop()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        touch ?: return

        this.touch!!.setTouchListener(null, object : SimpleTouchListener {
            override fun updateSavedBitmap(canvas: Canvas, bitmap: Bitmap?, pelList: List<Pel>, selectedPel: Pel?, isInvalidate: Boolean) {}

            override fun invalidate() {}

            override fun isSensorRegistered(): Boolean {
                return true
            }

            override fun getBackgroundBitmap(): Bitmap {
                return Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888)
            }

            override fun getCopyOfBackgroundBitmap(): Bitmap {
                return Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888)
            }

            override fun setSelectedPel(pel: Pel?) {}

            override fun getCurrentPaint(): Paint {
                return Paint()
            }

            override fun getContext(): Context {
                return getContext().applicationContext
            }
        })
    }
}