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
import com.ue.graffiti.util.ensureBitmapRecycled

class TextImageView : View {
    private var savedBitmap: Bitmap? = null
    lateinit var touch: TextImageTouch
    //文字坐标
    private lateinit var textPoint: PointF
    //文字中心
    private lateinit var centerPoint: PointF

    private var contentId = 0
    var imageContent: Bitmap? = null

    private lateinit var drawTextPaint: Paint
    private var textContent = ""

    var barSensorListener: BarSensorListener? = null
    //0:text,1:image
    private val type: Int

    val picture: Picture
        get() = Picture(contentId,
                touch.dx, touch.dy, touch.scale, touch.degree,
                PointF(centerPoint.x, centerPoint.y),
                PointF(textPoint.x, textPoint.y))

    constructor(context: Context) : this(context, null, 0)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        val ta = context.obtainStyledAttributes(attrs, R.styleable.gr_TextImageView)
        type = ta.getInt(R.styleable.gr_TextImageView_gr_type, 0)
        ta.recycle()
        if (type == 0) {
            //text
            drawTextPaint = Paint().apply {
                textSize = 50f
                typeface = Typeface.DEFAULT_BOLD
            }
        } else {
            //image
            textPoint = PointF()
            centerPoint = PointF()
        }
    }

    fun setTextColor(color: Int) {
        drawTextPaint.color = color
        invalidate()
    }

    fun setBitmap(savedBitmap: Bitmap, canvasWidth: Int, canvasHeight: Int, paintColor: Int) {
        this.savedBitmap = Bitmap.createScaledBitmap(savedBitmap, canvasWidth, canvasHeight, true)

        touch = TextImageTouch(true, canvasWidth, canvasHeight)
        touch.barSensorListener = barSensorListener

        drawTextPaint.color = paintColor

        textPoint = PointF()
        textPoint.set(touch.textPoint)
        centerPoint = PointF()
        centerPoint.set(touch.centerPoint)
    }

    fun setBitmap(savedBitmap: Bitmap, canvasWidth: Int, canvasHeight: Int) {
        this.savedBitmap = Bitmap.createScaledBitmap(savedBitmap, canvasWidth, canvasHeight, true)

        textPoint.set(canvasWidth / 2.5f, canvasHeight / 2.5f)
        centerPoint.set(textPoint)

        touch = TextImageTouch(false, canvasWidth, canvasHeight)
        touch.barSensorListener = barSensorListener
    }

    fun getText(paintColor: Int): Text {
        return Text(textContent,
                touch.dx, touch.dy, touch.scale, touch.degree,
                PointF(touch.centerPoint.x, touch.centerPoint.y),
                PointF(touch.textPoint.x, touch.textPoint.y), paintColor)
    }

    //触摸事件
    override fun onTouchEvent(event: MotionEvent): Boolean {
        touch.setCurrentPoint(PointF(event.getX(0), event.getY(0)))
        touch.setSecondPoint(if (event.pointerCount > 1) PointF(event.getX(1), event.getY(1)) else PointF(1f, 1f))

        val actionMasked = event.actionMasked
        when (actionMasked) {
        // 第一只手指按下
            MotionEvent.ACTION_DOWN -> {
                if (barSensorListener != null && barSensorListener!!.isTopToolbarVisible()) {
                    barSensorListener!!.closeTools()
                    touch.setDis(java.lang.Float.MAX_VALUE)
                }
                touch.down1()
            }
            MotionEvent.ACTION_POINTER_DOWN ->
                // 第二个手指按下
                touch.down2()
            MotionEvent.ACTION_MOVE -> touch.move()
        // 第一只手指抬起
            MotionEvent.ACTION_UP,
                //第二只手抬起
            MotionEvent.ACTION_POINTER_UP -> touch.up()
        }
        invalidate()

        return true
    }

    //重绘
    override fun onDraw(canvas: Canvas) {
        // 画其余图元
        canvas.drawBitmap(savedBitmap, 0f, 0f, Paint())
        canvas.translate(touch.dx, touch.dy)
        canvas.scale(touch.scale, touch.scale, centerPoint.x, centerPoint.y)
        canvas.rotate(touch.degree, centerPoint.x, centerPoint.y)

        if (type == 0) {
            //text
            canvas.drawText(textContent, textPoint.x, textPoint.y, drawTextPaint)
            return
        }
        if (imageContent != null) {
            //image
            canvas.drawBitmap(imageContent, textPoint.x, textPoint.y, null)
        }
    }

    fun setTextContent(textContent: String) {
        this.textContent = textContent
    }

    fun setContentAndCenterPoint(contentId: Int) {
        this.contentId = contentId
        ensureBitmapRecycled(imageContent)
        this.imageContent = BitmapFactory.decodeResource(context.resources, contentId)
        centerPoint.set(textPoint.x + imageContent!!.width / 2, textPoint.y + imageContent!!.height / 2)
    }
}