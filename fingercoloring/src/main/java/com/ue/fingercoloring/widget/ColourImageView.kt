package com.ue.fingercoloring.widget

import android.content.Context
import android.content.DialogInterface
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.support.v7.widget.AppCompatImageView
import android.util.AttributeSet
import com.ue.fingercoloring.constant.SPKeys
import com.ue.fingercoloring.event.OnDrawLineListener
import com.ue.fingercoloring.util.SizedStack
import com.ue.library.util.RxJavaUtils
import com.ue.library.util.SPUtils
import io.reactivex.Observable
import io.reactivex.ObservableOnSubscribe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.*


class ColourImageView : AppCompatImageView {
    private val mStacks = Stack<Point>()
    private var undopoints: Stack<Point>
    private var redopoints: Stack<Point>
    private var bmstackundo: Stack<Bitmap>
    private var bmstackredo: Stack<Bitmap>

    private var onRedoUndoListener: OnRedoUndoListener? = null
    private var onColorPickListener: OnColorPickListener? = null
    private var onDrawLineListener: OnDrawLineListener? = null

    private var mBitmap: Bitmap? = null
    var model = Model.FILLCOLOR
    private var attacher: MPhotoViewAttacher? = null

    private var mColor = -0xff432c
    private var stackSize: Int = 0

    private var mDisposable: Disposable? = null

    enum class Model {
        FILLCOLOR,
        FILLGRADUALCOLOR,
        PICKCOLOR,
        DRAW_LINE
    }

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle)

    init {
        stackSize = SPUtils.getInt(SPKeys.STACK_MAX_SIZE, 10)

        bmstackundo = SizedStack(stackSize)
        bmstackredo = SizedStack(stackSize)
        undopoints = Stack()
        redopoints = Stack()

        attacher = MPhotoViewAttacher(this)
    }

    override fun setImageBitmap(bm: Bitmap?) {
        super.setImageBitmap(bm)

        mBitmap = bm!!.copy(bm.config, true)
        attacher?.update()
    }

    fun pickColor(x: Int, y: Int) {
        var color = 0
        var status: Boolean
        try {
            status = !isBorderColor(mBitmap!!.getPixel(x, y)) && mBitmap!!.getPixel(x, y) != Color.TRANSPARENT
        } catch (e: Exception) {
            status = false
        }

        if (status) color = mBitmap!!.getPixel(x, y)

        onColorPickListener?.onColorPick(status, color)
    }

    fun isUndoable(): Boolean {
        return bmstackundo.size != 0
    }

    fun clearPoints() {
        undopoints.clear()
        redopoints.clear()
    }

    /**
     * @param x
     * @param y
     */
    fun fillColorToSameArea(x: Int, y: Int) {
        var isOk: Boolean
        try {
            isOk = mBitmap!!.getPixel(x, y) != mColor && !isBorderColor(mBitmap!!.getPixel(x, y)) && mBitmap!!.getPixel(x, y) != Color.TRANSPARENT
        } catch (e: Exception) {
            isOk = false
        }

        if (isOk) {
            ProgressLoading.show(context, true)
            ProgressLoading.setOnDismissListener(DialogInterface.OnDismissListener {
                RxJavaUtils.dispose(mDisposable)
            })

            doFillColorAction(x, y)
        }
    }

    private fun doFillColorAction(x: Int, y: Int) {
        RxJavaUtils.dispose(mDisposable)
        mDisposable = Observable
                .create(ObservableOnSubscribe<Bitmap> { e ->
                    val bm = mBitmap!!
                    try {
                        pushUndoStack(bm.copy(bm.config, true))
                        val pixel = bm.getPixel(x, y)
                        val w = bm.width
                        val h = bm.height
                        //?????bitmap?????????
                        val pixels = IntArray(w * h)
                        bm.getPixels(pixels, 0, w, 0, 0, w, h)
                        //???
                        fillColor(pixels, w, h, pixel, mColor, x, y)
                        //????????bitmap
                        bm.setPixels(pixels, 0, w, 0, 0, w, h)
                    } catch (exp: Exception) {
                        bmstackundo.pop()
                    }

                    e.onNext(bm)
                    e.onComplete()
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { bm ->
                    ProgressLoading.dismiss()
                    setImageDrawable(BitmapDrawable(resources, bm))
                    onRedoUndoListener?.onRedoUndo(bmstackundo.size, bmstackredo.size)
                }
    }

    private fun isBorderColor(color: Int): Boolean {
        return Color.red(color) < 0x10 && Color.green(color) < 0x10 && Color.blue(color) < 0x10
    }

    private fun pushUndoStack(bm: Bitmap) {
        bmstackundo.push(bm)
        bmstackredo.clear()
    }

    /**
     * @param pixels   像素数组
     * @param w        宽度
     * @param h        高度
     * @param pixel    当前点的颜色
     * @param newColor 填充色
     * @param i        横坐标
     * @param j        纵坐标
     */
    private fun fillColor(pixels: IntArray, w: Int, h: Int, pixel: Int, newColor: Int, i: Int, j: Int) {
        mStacks.clear()
        mStacks.push(Point(i, j))
        while (!mStacks.isEmpty()) {
            if (mDisposable == null || mDisposable!!.isDisposed) {
                break
            }

            val seed = mStacks.pop()
            //L.e("seed = " + seed.x + " , seed = " + seed.y);
            var count = fillLineLeft(pixels, pixel, w, h, newColor, seed.x, seed.y, i, j)
            val left = seed.x - count + 1
            count = fillLineRight(pixels, pixel, w, h, newColor, seed.x + 1, seed.y, i, j)
            val right = seed.x + count

            if (seed.y - 1 >= 0)
                findSeedInNewLine(pixels, pixel, w, h, seed.y - 1, left, right)
            if (seed.y + 1 < h)
                findSeedInNewLine(pixels, pixel, w, h, seed.y + 1, left, right)
        }
    }

    /**
     * @param pixels
     * @param pixel
     * @param w
     * @param h
     * @param i
     * @param left
     * @param right
     */
    private fun findSeedInNewLine(pixels: IntArray, pixel: Int, w: Int, h: Int, i: Int, left: Int, right: Int) {
        val begin = i * w + left
        var end = i * w + right
        var hasSeed = false
        var rx: Int

        while (end >= begin) {
            if (needFillPixel(pixels, pixel, end)) {
                if (!hasSeed) {
                    rx = end % w
                    mStacks.push(Point(rx, i))
                    hasSeed = true
                }
            } else {
                hasSeed = false
            }
            end--
        }
    }


    /**
     * @return
     */
    private fun fillLineLeft(pixels: IntArray, pixel: Int, w: Int, h: Int, newColor: Int, x: Int, y: Int, orginalX: Int, orginalY: Int): Int {
        var x = x
        var count = 0
        while (x >= 0) {
            val index = y * w + x

            if (!needFillPixel(pixels, pixel, index)) break

            if (model == Model.FILLCOLOR) {
                pixels[index] = newColor
            } else if (model == Model.FILLGRADUALCOLOR) {
                val colorHSV = floatArrayOf(0f, 0f, 1f)
                Color.colorToHSV(newColor, colorHSV)
                val dis = Math.sqrt(((x - orginalX) * (x - orginalX) + (y - orginalY) * (y - orginalY)).toDouble()).toFloat()
                colorHSV[1] = if (colorHSV[1] - dis * 0.006 < 0.2) 0.2f else colorHSV[1] - dis * 0.006f
                pixels[index] = Color.HSVToColor(colorHSV)
            }
            count++
            x--
        }

        return count
    }

    private fun fillLineRight(pixels: IntArray, pixel: Int, w: Int, h: Int, newColor: Int, x: Int, y: Int, orginalX: Int, orginalY: Int): Int {
        var x = x
        var count = 0

        while (x < w) {
            val index = y * w + x

            if (!needFillPixel(pixels, pixel, index)) break

            if (model == Model.FILLCOLOR) {
                pixels[index] = newColor
            } else if (model == Model.FILLGRADUALCOLOR) {
                val colorHSV = floatArrayOf(0f, 0f, 1f)
                Color.colorToHSV(newColor, colorHSV)
                val dis = Math.sqrt(((x - orginalX) * (x - orginalX) + (y - orginalY) * (y - orginalY)).toDouble()).toFloat()
                colorHSV[1] = if (colorHSV[1] - dis * 0.006 < 0.2) 0.2f else colorHSV[1] - dis * 0.006f
                pixels[index] = Color.HSVToColor(colorHSV)
            }
            count++
            x++
        }

        return count
    }

    private fun needFillPixel(pixels: IntArray, pixel: Int, index: Int): Boolean {
        return pixels[index] == pixel
    }

    fun update() {
        if (drawable != null)
            setMeasuredDimension(measuredWidth, drawable.intrinsicHeight * measuredWidth / drawable.intrinsicWidth)
    }

    fun setColor(color: Int) {
        mColor = color
    }

    /**
     * @return true: has element can undo;
     */
    fun undo(): Boolean {
        if (bmstackundo.empty()) return false

        try {
            bmstackredo.push(mBitmap!!.copy(mBitmap!!.config, true))
            mBitmap = bmstackundo.pop()
            setImageDrawable(BitmapDrawable(resources, mBitmap))
            onRedoUndoListener?.onRedoUndo(bmstackundo.size, bmstackredo.size)

            if (!undopoints.empty()) {
                redopoints.push(undopoints.pop())
            }
            return !bmstackundo.empty()

        } catch (e: Exception) {
        }

        return false
    }

    /**
     * @return true: has element ,can redo;
     */
    fun redo(): Boolean {
        if (bmstackredo.empty()) return false

        try {
            bmstackundo.push(mBitmap!!.copy(mBitmap!!.config, true))
            mBitmap = bmstackredo.pop()
            setImageDrawable(BitmapDrawable(resources, mBitmap))
            onRedoUndoListener?.onRedoUndo(bmstackundo.size, bmstackredo.size)

            if (!redopoints.empty()) {
                undopoints.push(redopoints.pop())
            }
            return !bmstackredo.empty()
        } catch (e: Exception) {
        }

        return false
    }

    fun setOnRedoUndoListener(onRedoUndoListener: OnRedoUndoListener) {
        this.onRedoUndoListener = onRedoUndoListener
    }

    //clear stack and the current ivThemeImage
    fun clearStack() {
        bmstackredo.clear()
        bmstackundo.clear()
        onRedoUndoListener?.onRedoUndo(bmstackundo.size, bmstackredo.size)
        mBitmap = null
    }

    fun setOnColorPickListener(onColorPickListener: OnColorPickListener) {
        this.onColorPickListener = onColorPickListener
    }

    fun getBitmap(): Bitmap? {
        return mBitmap
    }

    /**
     * call only for activity destroyed
     */
    fun onRecycleBitmaps() {
        while (!bmstackundo.empty()) {
            bmstackundo.pop().recycle()
            bmstackundo.clear()
        }

        while (!bmstackredo.empty()) {
            bmstackredo.pop().recycle()
            bmstackredo.clear()
        }

        mBitmap?.recycle()
    }

    fun drawLine(x: Int, y: Int) {
        if (!undopoints.empty()) {
            drawBlackLine(undopoints.peek().x, undopoints.peek().y, x, y)
            undopoints.push(Point(x, y))
            onDrawLineListener?.OnGivenNextPointListener(x, y)
        } else {
            undopoints.push(Point(x, y))
            onDrawLineListener?.OnGivenFirstPointListener(x, y)
        }
    }

    private fun drawBlackLine(startX: Int, startY: Int, endX: Int, endY: Int) {
        var startX = startX
        var startY = startY
        var endX = endX
        var endY = endY
        try {
            val bm = mBitmap
            //format points
            startX = if (startX >= bm!!.width) bm.width - 1 else startX
            startX = if (startX < 0) 0 else startX
            startY = if (startY >= bm.height) bm.height - 1 else startY
            startY = if (startY < 0) 0 else startY
            endX = if (endX >= bm.width) bm.width - 1 else endX
            endX = if (endX < 0) 0 else endX
            endY = if (endY >= bm.height) bm.height - 1 else endY
            endY = if (endY < 0) 0 else endY
            //test points
            bm.getPixel(endX, endY)
            bm.getPixel(startX, startY)
            pushUndoStack(bm.copy(bm.config, true))
            doingDrawLine(bm, startX, startY, endX, endY)
            setImageDrawable(BitmapDrawable(resources, bm))

            onRedoUndoListener?.onRedoUndo(bmstackundo.size, bmstackredo.size)
            onDrawLineListener?.OnDrawFinishedListener(true, startX, startY, endX, endY)
        } catch (e: Exception) {
            bmstackundo.pop()
            onDrawLineListener?.OnDrawFinishedListener(false, startX, startY, endX, endY)
        }

    }

    private fun doingDrawLine(bm: Bitmap, startX: Int, startY: Int, endX: Int, endY: Int) {
        val canvas = Canvas(bm)
        val paint = Paint()
        paint.color = -0x1000000
        paint.strokeWidth = 2f
        canvas.drawLine(startX.toFloat(), startY.toFloat(), endX.toFloat(), endY.toFloat(), paint)
    }

    fun setOnDrawLineListener(onDrawLineListener: OnDrawLineListener) {
        this.onDrawLineListener = onDrawLineListener
    }

    interface OnColorPickListener {
        fun onColorPick(status: Boolean, color: Int)
    }

    interface OnRedoUndoListener {
        fun onRedoUndo(undoSize: Int, redoSize: Int)
    }
}