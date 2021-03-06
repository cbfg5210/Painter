package com.ue.pixel.widget

import android.content.Context
import android.graphics.*
import android.os.SystemClock
import android.support.v4.graphics.ColorUtils
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import com.google.gson.stream.JsonReader
import com.ue.library.util.GsonHolder
import com.ue.pixel.shape.BaseShape
import com.ue.pixel.ui.DrawingActivity
import com.ue.pixel.util.DialogHelper
import com.ue.pixel.util.Tool
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader
import java.util.*

/**
 * Created by BennyKok on 10/3/2016.
 */
class PixelCanvasView : View, ScaleGestureDetector.OnScaleGestureListener, GestureDetector.OnGestureListener {
    val pixelCanvasLayers = ArrayList<PixelCanvasLayer>()
    //Drawing property
    private lateinit var pxerPaint: Paint
    var selectedColor = Color.YELLOW
    var mode = Mode.Normal
    lateinit var shapeTool: BaseShape
    var currentLayer = 0
        set(currentLayer) {
            field = currentLayer
            invalidate()
        }
    var isShowGrid = false
        set(showGrid) {
            field = showGrid
            invalidate()
        }
    private var isUnrecordedChanges = false

    private lateinit var borderPaint: Paint
    var picWidth = 0
        private set
    var picHeight = 0
        private set
    private var pixelSize = 0f
    private lateinit var picBoundary: RectF
    private val picRect = Rect()
    private val grid = Path()
    private lateinit var bgbitmap: Bitmap
    val previewCanvas = Canvas()
    lateinit var preview: Bitmap
        private set
    //Control property
    private lateinit var points: Array<Point?>
    private var downY = 0
    private var downX = 0
    private var downInPic = false
    private val drawMatrix = Matrix()
    private lateinit var mScaleDetector: ScaleGestureDetector
    private lateinit var mGestureDetector: GestureDetector
    private var mScaleFactor = 1f
    private var prePressedTime = -1L
    //History property
    private val history = ArrayList<ArrayList<PixelHistory>>()
    private val redoHistory = ArrayList<ArrayList<PixelHistory>>()
    private val historyIndex = ArrayList<Int>()
    val currentHistory = ArrayList<Pixel>()
    //Callback
    private var dropperCallBack: OnDropperCallBack? = null

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    fun setDropperCallBack(dropperCallBack: OnDropperCallBack) {
        this.dropperCallBack = dropperCallBack
    }

    fun copyAndPasteCurrentLayer() {
        val bitmap = pixelCanvasLayers[currentLayer].bitmap.copy(Bitmap.Config.ARGB_8888, true)
        pixelCanvasLayers.add(Math.max(currentLayer, 0), PixelCanvasLayer(bitmap))

        history.add(Math.max(currentLayer, 0), ArrayList())
        redoHistory.add(Math.max(currentLayer, 0), ArrayList())
        historyIndex.add(Math.max(currentLayer, 0), 0)
    }

    fun addLayer() {
        val bitmap = Bitmap.createBitmap(picWidth, picHeight, Bitmap.Config.ARGB_8888)
        bitmap.eraseColor(Color.TRANSPARENT)
        pixelCanvasLayers.add(Math.max(currentLayer, 0), PixelCanvasLayer(bitmap))

        history.add(Math.max(currentLayer, 0), ArrayList())
        redoHistory.add(Math.max(currentLayer, 0), ArrayList())
        historyIndex.add(Math.max(currentLayer, 0), 0)
    }

    fun removeCurrentLayer() {
        pixelCanvasLayers.removeAt(currentLayer)

        history.removeAt(currentLayer)
        redoHistory.removeAt(currentLayer)
        historyIndex.removeAt(currentLayer)

        currentLayer = Math.max(0, currentLayer - 1)
        invalidate()
    }

    fun moveLayer(from: Int, to: Int) {
        Collections.swap(pixelCanvasLayers, from, to)
        Collections.swap(history, from, to)
        Collections.swap(redoHistory, from, to)
        Collections.swap(historyIndex, from, to)
        invalidate()
    }

    fun clearCurrentLayer() {
        pixelCanvasLayers[currentLayer].bitmap.eraseColor(Color.TRANSPARENT)
    }

    fun mergeDownLayer() {
        preview.eraseColor(Color.TRANSPARENT)
        previewCanvas.setBitmap(preview)

        previewCanvas.drawBitmap(pixelCanvasLayers[currentLayer + 1].bitmap, 0f, 0f, null)
        previewCanvas.drawBitmap(pixelCanvasLayers[currentLayer].bitmap, 0f, 0f, null)

        pixelCanvasLayers.removeAt(currentLayer + 1)
        history.removeAt(currentLayer + 1)
        redoHistory.removeAt(currentLayer + 1)
        historyIndex.removeAt(currentLayer + 1)

        pixelCanvasLayers[currentLayer] = PixelCanvasLayer(Bitmap.createBitmap(preview))
        history[currentLayer] = ArrayList()
        redoHistory[currentLayer] = ArrayList()
        historyIndex[currentLayer] = 0

        invalidate()
    }

    fun visibilityAllLayer(visible: Boolean) {
        for (i in pixelCanvasLayers.indices) {
            pixelCanvasLayers[i].visible = visible
        }
        invalidate()
    }

    fun mergeAllLayers() {
        preview.eraseColor(Color.TRANSPARENT)
        previewCanvas.setBitmap(preview)
        for (i in pixelCanvasLayers.indices) {
            previewCanvas.drawBitmap(pixelCanvasLayers[pixelCanvasLayers.size - i - 1].bitmap, 0f, 0f, null)
        }
        pixelCanvasLayers.clear()
        history.clear()
        redoHistory.clear()
        historyIndex.clear()

        pixelCanvasLayers.add(PixelCanvasLayer(Bitmap.createBitmap(preview)))
        history.add(ArrayList())
        redoHistory.add(ArrayList())
        historyIndex.add(0)

        currentLayer = 0

        invalidate()
    }

    fun createBlankProject(picWidth: Int, picHeight: Int) {
        this.picWidth = picWidth
        this.picHeight = picHeight

        points = arrayOfNulls(picWidth * picHeight)
        for (i in 0 until picWidth) {
            for (j in 0 until picHeight) {
                points[i * picHeight + j] = Point(i, j)
            }
        }

        val bitmap = Bitmap.createBitmap(picWidth, picHeight, Bitmap.Config.ARGB_8888)
        bitmap.eraseColor(Color.TRANSPARENT)
        pixelCanvasLayers.clear()
        pixelCanvasLayers.add(PixelCanvasLayer(bitmap))
        onLayerUpdate()

        mScaleFactor = 1f
        drawMatrix.reset()
        initPxerInfo()

        history.clear()
        redoHistory.clear()
        historyIndex.clear()

        history.add(ArrayList())
        redoHistory.add(ArrayList())
        historyIndex.add(0)

        currentLayer = 0

        reCalBackground()

        Tool.freeMemory()
    }

    fun loadProject(file: File): Boolean {
        val gson = GsonHolder.gson
        val out = ArrayList<PixelLayer>()
        try {
            val reader = JsonReader(InputStreamReader(FileInputStream(File(file.path))))
            reader.beginArray()
            while (reader.hasNext()) {
                val layer = gson.fromJson<PixelLayer>(reader, PixelLayer::class.java)
                out.add(layer)
            }
            reader.endArray()
            reader.close()
        } catch (e: Exception) {
            DialogHelper.showLoadProjectErrorDialog(context)
            return false
        }

        this.picWidth = out[0].width
        this.picHeight = out[0].height

        points = arrayOfNulls(picWidth * picHeight)
        for (i in 0 until picWidth) {
            for (j in 0 until picHeight) {
                points[i * picHeight + j] = Point(i, j)
            }
        }

        history.clear()
        redoHistory.clear()
        historyIndex.clear()


        pixelCanvasLayers.clear()
        for (i in out.indices) {
            val bitmap = Bitmap.createBitmap(picWidth, picHeight, Bitmap.Config.ARGB_8888)

            history.add(ArrayList())
            redoHistory.add(ArrayList())
            historyIndex.add(0)

            val layer = PixelCanvasLayer(bitmap)
            layer.visible = out[i].visible
            pixelCanvasLayers.add(layer)
            for (x in out[i].pxers.indices) {
                val p = out[i].pxers[x]
                pixelCanvasLayers[i].bitmap.setPixel(p.x, p.y, p.color)
            }
        }
        onLayerUpdate()

        mScaleFactor = 1f
        drawMatrix.reset()
        initPxerInfo()

        currentLayer = 0

        reCalBackground()
        invalidate()

        Tool.freeMemory()
        return true
    }

    fun undo() {
        if (historyIndex[currentLayer] <= 0) return

        historyIndex[currentLayer] = historyIndex[currentLayer] - 1
        for (i in history[currentLayer][historyIndex[currentLayer]].pixels.indices) {
            val pxer = history[currentLayer][historyIndex[currentLayer]].pixels[i]
            currentHistory.add(Pixel(pxer.x, pxer.y, pixelCanvasLayers[currentLayer].bitmap.getPixel(pxer.x, pxer.y)))

            val coord = history[currentLayer][historyIndex[currentLayer]].pixels[i]
            pixelCanvasLayers[currentLayer].bitmap.setPixel(coord.x, coord.y, coord.color)
        }
        redoHistory[currentLayer].add(PixelHistory(cloneList(currentHistory)))
        currentHistory.clear()

        history[currentLayer].removeAt(history[currentLayer].size - 1)
        invalidate()
    }

    fun redo() {
        if (redoHistory[currentLayer].size <= 0) return

        for (i in redoHistory[currentLayer][redoHistory[currentLayer].size - 1].pixels.indices) {
            var pxer = redoHistory[currentLayer][redoHistory[currentLayer].size - 1].pixels[i]
            currentHistory.add(Pixel(pxer.x, pxer.y, pixelCanvasLayers[currentLayer].bitmap.getPixel(pxer.x, pxer.y)))

            pxer = redoHistory[currentLayer][redoHistory[currentLayer].size - 1].pixels[i]
            pixelCanvasLayers[currentLayer].bitmap.setPixel(pxer.x, pxer.y, pxer.color)
        }
        historyIndex[currentLayer] = historyIndex[currentLayer] + 1

        history[currentLayer].add(PixelHistory(cloneList(currentHistory)))
        currentHistory.clear()

        redoHistory[currentLayer].removeAt(redoHistory[currentLayer].size - 1)
        invalidate()
    }

    fun resetViewPort() {
        scaleAtFirst()
    }

    private fun init() {
        mScaleDetector = ScaleGestureDetector(context, this)
        mGestureDetector = GestureDetector(context, this)

        setWillNotDraw(false)

        borderPaint = Paint()
        borderPaint.isAntiAlias = true
        borderPaint.style = Paint.Style.STROKE
        borderPaint.strokeWidth = 1f
        borderPaint.color = Color.DKGRAY

        pxerPaint = Paint()
        pxerPaint.isAntiAlias = true

        picBoundary = RectF(0f, 0f, 0f, 0f)

        //Create a 40 x 40 project
        this.picWidth = 40
        this.picHeight = 40

        points = arrayOfNulls(picWidth * picHeight)
        for (i in 0 until picWidth) {
            for (j in 0 until picHeight) {
                points[i * picHeight + j] = Point(i, j)
            }
        }

        val bitmap = Bitmap.createBitmap(picWidth, picHeight, Bitmap.Config.ARGB_8888)
        bitmap.eraseColor(Color.TRANSPARENT)
        pixelCanvasLayers.clear()
        pixelCanvasLayers.add(PixelCanvasLayer(bitmap))

        history.add(ArrayList())
        redoHistory.add(ArrayList())
        historyIndex.add(0)

        reCalBackground()
        resetViewPort()

        //Avoid unknown flicking issue if the user scale the canvas immediately
        val downTime = SystemClock.uptimeMillis()
        val eventTime = downTime + 100
        val x = 0.0f
        val y = 0.0f
        val metaState = 0
        val motionEvent = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_UP, x, y, metaState)
        mGestureDetector.onTouchEvent(motionEvent)
    }

    private fun reCalBackground() {
        preview = Bitmap.createBitmap(picWidth, picHeight, Bitmap.Config.ARGB_8888)

        bgbitmap = Bitmap.createBitmap(picWidth * 2, picHeight * 2, Bitmap.Config.ARGB_8888)
        bgbitmap.eraseColor(ColorUtils.setAlphaComponent(Color.WHITE, 200))

        for (i in 0 until picWidth) {
            for (j in 0 until picHeight * 2) {
                if (j % 2 != 0) bgbitmap.setPixel(i * 2 + 1, j, Color.argb(200, 220, 220, 220))
                else bgbitmap.setPixel(i * 2, j, Color.argb(200, 220, 220, 220))
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) mGestureDetector.onTouchEvent(event)

        mScaleDetector.onTouchEvent(event)

        if (event.action == MotionEvent.ACTION_UP) {
            downInPic = false

            if (mode == Mode.ShapeTool) shapeTool.onDrawEnd(this)

            if (mode != Mode.Fill && mode != Mode.Dropper && mode != Mode.ShapeTool) {
                finishAddHistory()
            }
        }

        if (event.pointerCount > 1) {
            prePressedTime = -1L
            mGestureDetector.onTouchEvent(event)

            return true
        }
        //Get the position
        val mX = event.x
        val mY = event.y
        val raw = FloatArray(9)
        drawMatrix.getValues(raw)
        val scaledWidth = picBoundary.width() * mScaleFactor
        val scaledHeight = picBoundary.height() * mScaleFactor
        picRect.set(raw[Matrix.MTRANS_X].toInt(), raw[Matrix.MTRANS_Y].toInt(), (raw[Matrix.MTRANS_X] + scaledWidth).toInt(), (raw[Matrix.MTRANS_Y] + scaledHeight).toInt())
        if (!picRect.contains(mX.toInt(), mY.toInt())) {
            return true
        }
        val x = ((mX - picRect.left) / scaledWidth * picWidth).toInt()
        val y = ((mY - picRect.top) / scaledHeight * picHeight).toInt()
        //We got x and y

        if (event.action == MotionEvent.ACTION_MOVE) {
            if (prePressedTime != -1L && System.currentTimeMillis() - prePressedTime <= pressDelay) return true
            if (prePressedTime == -1L) return true
        }

        if (!isValid(x, y)) return true

        if (event.action == MotionEvent.ACTION_DOWN) {
            downY = y
            downX = x
            downInPic = true
            prePressedTime = System.currentTimeMillis()
        }

        if (mode == Mode.ShapeTool && downX != -1 && event.action != MotionEvent.ACTION_UP && event.action != MotionEvent.ACTION_DOWN) {
            if (!shapeTool.hasEnded()) shapeTool.onDraw(this, downX, downY, x, y)
            return true
        }

        val pixel: Pixel
        val bitmapToDraw = pixelCanvasLayers[currentLayer].bitmap
        if (event.action != MotionEvent.ACTION_UP) {
            pixel = Pixel(x, y, bitmapToDraw.getPixel(x, y))
            if (!currentHistory.contains(pixel)) currentHistory.add(pixel)
        }

        when (mode) {
            PixelCanvasView.Mode.Normal -> run {
                if (event.action == MotionEvent.ACTION_DOWN || event.action == MotionEvent.ACTION_UP) return@run

                bitmapToDraw.setPixel(x, y, ColorUtils.compositeColors(selectedColor, bitmapToDraw.getPixel(x, y)))
                setUnrecordedChanges(true)
            }
            PixelCanvasView.Mode.Dropper -> run {
                if (event.action == MotionEvent.ACTION_DOWN) return@run

                if (x == downX && downY == y) {
                    for (i in pixelCanvasLayers.indices) {
                        val pixel = pixelCanvasLayers[i].bitmap.getPixel(x, y)
                        if (pixel != Color.TRANSPARENT) {
                            selectedColor = pixelCanvasLayers[i].bitmap.getPixel(x, y)
                            dropperCallBack?.onColorDropped(selectedColor)
                            break
                        }
                        if (i == pixelCanvasLayers.size - 1) {
                            dropperCallBack?.onColorDropped(Color.TRANSPARENT)
                        }
                    }
                }
            }
            PixelCanvasView.Mode.Fill ->
                //The fill tool is brought to us with aid by some open source project online :( I forgot the name
                if (event.action == MotionEvent.ACTION_UP && x == downX && downY == y) {
                    Tool.freeMemory()

                    val targetColor = bitmapToDraw.getPixel(x, y)
                    val toExplore = LinkedList<Point>()
                    val explored = HashSet<Point>()
                    toExplore.add(Point(x, y))

                    while (!toExplore.isEmpty()) {
                        val p = toExplore.remove()
                        //Color it
                        currentHistory.add(Pixel(p.x, p.y, targetColor))
                        bitmapToDraw.setPixel(p.x, p.y, ColorUtils.compositeColors(selectedColor, bitmapToDraw.getPixel(p.x, p.y)))
                        //
                        var cp: Point
                        if (isValid(p.x, p.y - 1)) {
                            cp = points[p.x * picHeight + p.y - 1]!!
                            if (!explored.contains(cp)) {
                                if (bitmapToDraw.getPixel(cp.x, cp.y) == targetColor) toExplore.add(cp)
                                explored.add(cp)
                            }
                        }

                        if (isValid(p.x, p.y + 1)) {
                            cp = points[p.x * picHeight + p.y + 1]!!
                            if (!explored.contains(cp)) {
                                if (bitmapToDraw.getPixel(cp.x, cp.y) == targetColor) toExplore.add(cp)
                                explored.add(cp)
                            }
                        }

                        if (isValid(p.x - 1, p.y)) {
                            cp = points[(p.x - 1) * picHeight + p.y]!!
                            if (!explored.contains(cp)) {
                                if (bitmapToDraw.getPixel(cp.x, cp.y) == targetColor) toExplore.add(cp)
                                explored.add(cp)
                            }
                        }

                        if (isValid(p.x + 1, p.y)) {
                            cp = points[(p.x + 1) * picHeight + p.y]!!
                            if (!explored.contains(cp)) {
                                if (bitmapToDraw.getPixel(cp.x, cp.y) == targetColor) toExplore.add(cp)
                                explored.add(cp)
                            }
                        }
                    }
                    setUnrecordedChanges(true)
                    finishAddHistory()
                }
        }
        invalidate()
        return true
    }

    fun finishAddHistory() {
        if (currentHistory.size > 0 && isUnrecordedChanges) {
            isUnrecordedChanges = false
            redoHistory[currentLayer].clear()
            historyIndex[currentLayer] = historyIndex[currentLayer] + 1
            history[currentLayer].add(PixelHistory(cloneList(currentHistory)))
            currentHistory.clear()
        }
    }

    private fun isValid(x: Int, y: Int): Boolean {
        return x >= 0 && x <= picWidth - 1 && y >= 0 && y <= picHeight - 1
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawColor(Color.DKGRAY)
        canvas.save()
        canvas.concat(drawMatrix)
        canvas.drawBitmap(bgbitmap, null, picBoundary, pxerPaint)
        for (i in pixelCanvasLayers.size - 1 downTo -1 + 1) {
            if (pixelCanvasLayers[i].visible) {
                canvas.drawBitmap(pixelCanvasLayers[i].bitmap, null, picBoundary, pxerPaint)
            }
        }
        if (isShowGrid) canvas.drawPath(grid, borderPaint)
        canvas.restore()
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        initPxerInfo()
    }

    private fun initPxerInfo() {
        val length = Math.min(height, width)
        pixelSize = (length / 40).toFloat()
        picBoundary.set(0f, 0f, pixelSize * picWidth, pixelSize * picHeight)
        scaleAtFirst()

        grid.reset()
        for (x in 0 until picWidth + 1) {
            val posx = picBoundary.left + pixelSize * x
            grid.moveTo(posx, picBoundary.top)
            grid.lineTo(posx, picBoundary.bottom)
        }
        for (y in 0 until picHeight + 1) {
            val posy = picBoundary.top + pixelSize * y
            grid.moveTo(picBoundary.left, posy)
            grid.lineTo(picBoundary.right, posy)
        }
    }

    override fun onDown(motionEvent: MotionEvent): Boolean {
        return false
    }

    override fun onShowPress(motionEvent: MotionEvent) {
    }

    override fun onSingleTapUp(motionEvent: MotionEvent): Boolean {
        return false
    }

    override fun onScroll(motionEvent: MotionEvent, motionEvent1: MotionEvent, v: Float, v1: Float): Boolean {
        drawMatrix.postTranslate(-v, -v1)
        invalidate()
        return true
    }

    override fun onLongPress(motionEvent: MotionEvent) {
    }

    override fun onFling(motionEvent: MotionEvent, motionEvent1: MotionEvent, v: Float, v1: Float): Boolean {
        return false
    }

    override fun onScale(scaleGestureDetector: ScaleGestureDetector): Boolean {
        val scale = scaleGestureDetector.scaleFactor

        mScaleFactor *= scale
        val transformationMatrix = Matrix()
        val focusX = scaleGestureDetector.focusX
        val focusY = scaleGestureDetector.focusY

        transformationMatrix.postTranslate(-focusX, -focusY)
        transformationMatrix.postScale(scaleGestureDetector.scaleFactor, scaleGestureDetector.scaleFactor)

        transformationMatrix.postTranslate(focusX, focusY)
        drawMatrix.postConcat(transformationMatrix)

        invalidate()
        return true
    }

    private fun scaleAtFirst() {
        //int width = Math.max(getWidth(),getHeight()),height = Math.min(getWidth(),getHeight());
        mScaleFactor = 1f
        drawMatrix.reset()

        val scale = 0.98f

        mScaleFactor = scale
        val transformationMatrix = Matrix()
        transformationMatrix.postTranslate((width - picBoundary.width()) / 2, (height - picBoundary.height()) / 3)

        val focusX = (width / 2).toFloat()
        val focusY = (height / 2).toFloat()

        transformationMatrix.postTranslate(-focusX, -focusY)
        transformationMatrix.postScale(scale, scale)

        transformationMatrix.postTranslate(focusX, focusY)
        drawMatrix.postConcat(transformationMatrix)

        invalidate()
    }

    private fun onLayerUpdate() {
        (context as DrawingActivity).onLayerUpdate()
    }

    override fun invalidate() {
        (context as DrawingActivity).onLayerRefresh()
        super.invalidate()
    }

    override fun onScaleBegin(scaleGestureDetector: ScaleGestureDetector): Boolean {
        return true
    }

    override fun onScaleEnd(scaleGestureDetector: ScaleGestureDetector) {
    }

    fun setUnrecordedChanges(unrecordedChanges: Boolean) {
        isUnrecordedChanges = unrecordedChanges

        val drawingActivity = context as DrawingActivity
        if (!drawingActivity.isEdited) drawingActivity.isEdited = isUnrecordedChanges
    }

    enum class Mode {
        Normal, Eraser, Fill, Dropper, ShapeTool
    }

    interface OnDropperCallBack {
        fun onColorDropped(newColor: Int)
    }

    class PixelCanvasLayer {
        var bitmap: Bitmap
        var visible = true

        constructor(bitmap: Bitmap) {
            this.bitmap = bitmap
        }
    }

    class PixelLayer {
        var width = 0
        var height = 0
        var visible = false
        var pxers = ArrayList<Pixel>()
    }

    class Pixel(var x: Int, var y: Int, var color: Int) {

        fun clone(): Pixel {
            return Pixel(x, y, color)
        }

        override fun equals(obj: Any?): Boolean {
            return (obj as Pixel).x == this.x && obj.y == this.y
        }
    }

    class PixelHistory(var pixels: ArrayList<Pixel>)

    companion object {
        const val PIXEL_EXTENSION_NAME = ".pxer"
        private const val pressDelay = 60L

        fun cloneList(list: List<Pixel>): ArrayList<Pixel> {
            val clone = ArrayList<Pixel>(list.size)
            list.mapTo(clone) { it.clone() }
            return clone
        }
    }
}