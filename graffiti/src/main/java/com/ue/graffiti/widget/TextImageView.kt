package com.ue.graffiti.widget

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.ue.graffiti.R
import com.ue.graffiti.event.BarSensorListener
import com.ue.graffiti.model.Picture
import com.ue.graffiti.model.Text
import com.ue.graffiti.touch.TextImageTouch
import com.ue.graffiti.util.TouchUtils

class TextImageView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : View(context, attrs, defStyleAttr) {
    private var savedBitmap: Bitmap? = null
    var touch: TextImageTouch? = null
        private set
    private var textPoint: PointF? = null//文字坐标
    private var centerPoint: PointF? = null//文字中心

    private var contentId: Int = 0
    var imageContent: Bitmap? = null
        private set

    private var drawTextPaint: Paint? = null
    private var textContent = ""

    private var mBarSensorListener: BarSensorListener? = null
    //0:text,1:image
    private val type: Int

    val picture: Picture
        get() = Picture(contentId,
                touch!!.dx, touch!!.dy, touch!!.scale, touch!!.degree,
                PointF(centerPoint!!.x, centerPoint!!.y),
                PointF(textPoint!!.x, textPoint!!.y))

    init {

        val ta = context.obtainStyledAttributes(attrs, R.styleable.TextImageView)
        type = ta.getInt(R.styleable.TextImageView_type, 0)
        if (type == 0) {
            //text
            drawTextPaint = Paint()
            drawTextPaint!!.textSize = 50f
            drawTextPaint!!.typeface = Typeface.DEFAULT_BOLD
        } else {
            //image
            textPoint = PointF()
            centerPoint = PointF()
        }
        ta.recycle()
    }

    fun setBarSensorListener(barSensorListener: BarSensorListener) {
        mBarSensorListener = barSensorListener
    }

    fun setTextColor(color: Int) {
        drawTextPaint!!.color = color
        invalidate()
    }

    fun setBitmap(savedBitmap: Bitmap, canvasWidth: Int, canvasHeight: Int, paintColor: Int) {
        this.savedBitmap = Bitmap.createScaledBitmap(savedBitmap, canvasWidth, canvasHeight, true)

        touch = TextImageTouch(true, canvasWidth, canvasHeight)
        if (mBarSensorListener != null) {
            touch!!.setBarSensorListener(mBarSensorListener!!)
        }

        drawTextPaint!!.color = paintColor

        textPoint = PointF()
        textPoint!!.set(touch!!.textPoint)
        centerPoint = PointF()
        centerPoint!!.set(touch!!.centerPoint)
    }

    fun setBitmap(savedBitmap: Bitmap, canvasWidth: Int, canvasHeight: Int) {
        this.savedBitmap = Bitmap.createScaledBitmap(savedBitmap, canvasWidth, canvasHeight, true)

        textPoint!!.set(canvasWidth / 2.5f, canvasHeight / 2.5f)
        centerPoint!!.set(textPoint)

        touch = TextImageTouch(false, canvasWidth, canvasHeight)
        if (mBarSensorListener != null) {
            touch!!.setBarSensorListener(mBarSensorListener!!)
        }
    }

    fun getText(paintColor: Int): Text {
        return Text(textContent,
                touch!!.dx, touch!!.dy, touch!!.scale, touch!!.degree,
                PointF(touch!!.centerPoint.x, touch!!.centerPoint.y),
                PointF(touch!!.textPoint.x, touch!!.textPoint.y), paintColor)
    }

    //触摸事件
    override fun onTouchEvent(event: MotionEvent): Boolean {
        touch!!.setCurrentPoint(PointF(event.getX(0), event.getY(0)))
        touch!!.setSecondPoint(if (event.pointerCount > 1) PointF(event.getX(1), event.getY(1)) else PointF(1f, 1f))

        val actionMasked = event.actionMasked
        when (actionMasked) {
        // 第一只手指按下
            MotionEvent.ACTION_DOWN -> {
                if (mBarSensorListener != null && mBarSensorListener!!.isTopToolbarVisible()) {
                    mBarSensorListener!!.closeTools()
                    touch!!.setDis(java.lang.Float.MAX_VALUE)
                }
                touch!!.down1()
            }
            MotionEvent.ACTION_POINTER_DOWN ->
                // 第二个手指按下
                touch!!.down2()
            MotionEvent.ACTION_MOVE -> touch!!.move()
        // 第一只手指抬起
            MotionEvent.ACTION_UP,
                //第二只手抬起
            MotionEvent.ACTION_POINTER_UP -> touch!!.up()
        }
        invalidate()

        return true
    }

    //重绘
    override fun onDraw(canvas: Canvas) {
        canvas.drawBitmap(savedBitmap!!, 0f, 0f, Paint())// 画其余图元
        canvas.translate(touch!!.dx, touch!!.dy)
        canvas.scale(touch!!.scale, touch!!.scale, centerPoint!!.x, centerPoint!!.y)
        canvas.rotate(touch!!.degree, centerPoint!!.x, centerPoint!!.y)

        if (type == 0) {
            //text
            canvas.drawText(textContent, textPoint!!.x, textPoint!!.y, drawTextPaint!!)
            return
        }
        //image
        if (imageContent != null) {
            canvas.drawBitmap(imageContent!!, textPoint!!.x, textPoint!!.y, null)
        }
    }

    fun setTextContent(textContent: String) {
        this.textContent = textContent
    }

    fun setContentAndCenterPoint(contentId: Int) {
        this.contentId = contentId
        TouchUtils.ensureBitmapRecycled(imageContent)
        this.imageContent = BitmapFactory.decodeResource(context.resources, contentId)
        centerPoint!!.set(textPoint!!.x + imageContent!!.width / 2, textPoint!!.y + imageContent!!.height / 2)
    }
}