package com.ue.fingercoloring.widget

/*
 * Copyright 2013 Piotr Adamus
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.graphics.Bitmap.Config
import android.graphics.Paint.Join
import android.graphics.Paint.Style
import android.graphics.Shader.TileMode
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class ColorPicker : View {

    /**
     * Customizable display parameters (in percents)
     */
    private val paramOuterPadding = 2 // outer padding of the whole color picker view
    private val paramInnerPadding = 5 // distance between value slider wheel and inner color wheel
    private val paramValueSliderWidth = 10 // width of the value slider
    private val paramArrowPointerSize = 4 // size of the arrow pointer; set to 0 to hide the pointer

    private var colorWheelPaint: Paint? = null
    private var valueSliderPaint: Paint? = null

    //private Paint colorViewPaint;

    private var colorPointerPaint: Paint? = null
    private var colorPointerCoords: RectF? = null

    private var valuePointerPaint: Paint? = null
    private var valuePointerArrowPaint: Paint? = null

    private var outerWheelRect: RectF? = null
    private var innerWheelRect: RectF? = null

    private var colorViewPath: Path? = null
    private var valueSliderPath: Path? = null
    private var arrowPointerPath: Path? = null

    private var colorWheelBitmap: Bitmap? = null

    private var valueSliderWidth: Int = 0
    private var innerPadding: Int = 0
    private var outerPadding: Int = 0

    private var arrowPointerSize: Int = 0
    private var outerWheelRadius: Int = 0
    private var innerWheelRadius: Int = 0
    private var colorWheelRadius: Int = 0

    private var gradientRotationMatrix: Matrix? = null

    /**
     * Currently selected color
     */
    private var colorHSV: FloatArray = floatArrayOf(0f, 0f, 1f)

    private var onChangedListener: OnColorChangedListener? = null

    var color: Int
        get() = Color.HSVToColor(colorHSV)
        set(color) {
            Color.colorToHSV(color, colorHSV)
            invalidate()
        }

    fun setOnChangedListener(onChangedListener: OnColorChangedListener) {
        this.onChangedListener = onChangedListener
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    constructor(context: Context) : super(context) {
        init()
    }

    private fun init() {

        colorPointerPaint = Paint()
        colorPointerPaint!!.style = Style.STROKE
        colorPointerPaint!!.strokeWidth = 2f
        colorPointerPaint!!.setARGB(255, 0, 0, 0)

        valuePointerPaint = Paint()
        valuePointerPaint!!.style = Style.STROKE
        valuePointerPaint!!.strokeWidth = 2f

        valuePointerArrowPaint = Paint()

        colorWheelPaint = Paint()
        colorWheelPaint!!.isAntiAlias = true
        colorWheelPaint!!.isDither = true

        valueSliderPaint = Paint()
        valueSliderPaint!!.isAntiAlias = true
        valueSliderPaint!!.isDither = true

        //        colorViewPaint = new Paint();
        //        colorViewPaint.setAntiAlias(true);

        colorViewPath = Path()
        valueSliderPath = Path()
        arrowPointerPath = Path()

        outerWheelRect = RectF()
        innerWheelRect = RectF()

        colorPointerCoords = RectF()

    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthSize = View.MeasureSpec.getSize(widthMeasureSpec)
        val heightSize = View.MeasureSpec.getSize(heightMeasureSpec)
        val size = Math.min(widthSize, heightSize)
        setMeasuredDimension(size, size)
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {

        val centerX = width / 2
        val centerY = height / 2

        // drawing color wheel

        canvas.drawBitmap(colorWheelBitmap!!, (centerX - colorWheelRadius).toFloat(), (centerY - colorWheelRadius).toFloat(), null)

        // drawing color view

        //        colorViewPaint.setColor(Color.HSVToColor(colorHSV));
        //        canvas.drawPath(colorViewPath, colorViewPaint);

        // drawing value slider

        val hsv = floatArrayOf(colorHSV[0], colorHSV[1], 1f)

        val sweepGradient = SweepGradient(centerX.toFloat(), centerY.toFloat(), intArrayOf(Color.BLACK, Color.HSVToColor(hsv), Color.WHITE), null)
        sweepGradient.setLocalMatrix(gradientRotationMatrix)
        valueSliderPaint!!.shader = sweepGradient

        canvas.drawPath(valueSliderPath!!, valueSliderPaint!!)

        // drawing color wheel pointer

        val hueAngle = Math.toRadians(colorHSV[0].toDouble()).toFloat()
        val colorPointX = (-Math.cos(hueAngle.toDouble()) * colorHSV[1].toDouble() * colorWheelRadius.toDouble()).toInt() + centerX
        val colorPointY = (-Math.sin(hueAngle.toDouble()) * colorHSV[1].toDouble() * colorWheelRadius.toDouble()).toInt() + centerY

        val pointerRadius = 0.075f * colorWheelRadius
        val pointerX = (colorPointX - pointerRadius / 2).toInt()
        val pointerY = (colorPointY - pointerRadius / 2).toInt()

        colorPointerCoords!!.set(pointerX.toFloat(), pointerY.toFloat(), pointerX + pointerRadius, pointerY + pointerRadius)
        canvas.drawOval(colorPointerCoords!!, colorPointerPaint!!)

        // drawing value pointer

        valuePointerPaint!!.color = Color.HSVToColor(floatArrayOf(0f, 0f, 1f - colorHSV[2]))

        val valueAngle = (colorHSV[2] - 0.5f) * Math.PI
        val valueAngleX = Math.cos(valueAngle).toFloat()
        val valueAngleY = Math.sin(valueAngle).toFloat()

        canvas.drawLine(valueAngleX * innerWheelRadius + centerX, valueAngleY * innerWheelRadius + centerY, valueAngleX * outerWheelRadius + centerX,
                valueAngleY * outerWheelRadius + centerY, valuePointerPaint!!)

        // drawing pointer arrow

        if (arrowPointerSize > 0) {
            drawPointerArrow(canvas)
        }

    }

    private fun drawPointerArrow(canvas: Canvas) {

        val centerX = width / 2
        val centerY = height / 2

        val tipAngle = (colorHSV[2] - 0.5f) * Math.PI
        val leftAngle = tipAngle + Math.PI / 96
        val rightAngle = tipAngle - Math.PI / 96

        val tipAngleX = Math.cos(tipAngle) * outerWheelRadius
        val tipAngleY = Math.sin(tipAngle) * outerWheelRadius
        val leftAngleX = Math.cos(leftAngle) * (outerWheelRadius + arrowPointerSize)
        val leftAngleY = Math.sin(leftAngle) * (outerWheelRadius + arrowPointerSize)
        val rightAngleX = Math.cos(rightAngle) * (outerWheelRadius + arrowPointerSize)
        val rightAngleY = Math.sin(rightAngle) * (outerWheelRadius + arrowPointerSize)

        arrowPointerPath!!.reset()
        arrowPointerPath!!.moveTo(tipAngleX.toFloat() + centerX, tipAngleY.toFloat() + centerY)
        arrowPointerPath!!.lineTo(leftAngleX.toFloat() + centerX, leftAngleY.toFloat() + centerY)
        arrowPointerPath!!.lineTo(rightAngleX.toFloat() + centerX, rightAngleY.toFloat() + centerY)
        arrowPointerPath!!.lineTo(tipAngleX.toFloat() + centerX, tipAngleY.toFloat() + centerY)

        valuePointerArrowPaint!!.color = Color.HSVToColor(colorHSV)
        valuePointerArrowPaint!!.style = Style.FILL
        canvas.drawPath(arrowPointerPath!!, valuePointerArrowPaint!!)

        valuePointerArrowPaint!!.style = Style.STROKE
        valuePointerArrowPaint!!.strokeJoin = Join.ROUND
        valuePointerArrowPaint!!.color = Color.BLACK
        canvas.drawPath(arrowPointerPath!!, valuePointerArrowPaint!!)

    }

    override fun onSizeChanged(width: Int, height: Int, oldw: Int, oldh: Int) {

        val centerX = width / 2
        val centerY = height / 2

        innerPadding = (paramInnerPadding * width / 100).toInt()
        outerPadding = (paramOuterPadding * width / 100).toInt()
        arrowPointerSize = (paramArrowPointerSize * width / 100).toInt()
        valueSliderWidth = (paramValueSliderWidth * width / 100).toInt()

        outerWheelRadius = width / 2 - outerPadding - arrowPointerSize
        innerWheelRadius = outerWheelRadius - valueSliderWidth
        colorWheelRadius = innerWheelRadius - innerPadding

        outerWheelRect!!.set((centerX - outerWheelRadius).toFloat(), (centerY - outerWheelRadius).toFloat(), (centerX + outerWheelRadius).toFloat(), (centerY + outerWheelRadius).toFloat())
        innerWheelRect!!.set((centerX - innerWheelRadius).toFloat(), (centerY - innerWheelRadius).toFloat(), (centerX + innerWheelRadius).toFloat(), (centerY + innerWheelRadius).toFloat())

        colorWheelBitmap = createColorWheelBitmap(colorWheelRadius * 2, colorWheelRadius * 2)

        gradientRotationMatrix = Matrix()
        gradientRotationMatrix!!.preRotate(270f, (width / 2).toFloat(), (height / 2).toFloat())

        colorViewPath!!.arcTo(outerWheelRect, 270f, -180f)
        colorViewPath!!.arcTo(innerWheelRect, 90f, 180f)

        valueSliderPath!!.arcTo(outerWheelRect, 288f, 162f)
        valueSliderPath!!.arcTo(innerWheelRect, 90f, -162f)

    }

    private fun createColorWheelBitmap(width: Int, height: Int): Bitmap {

        val bitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888)

        val colorCount = 12
        val colorAngleStep = 360 / 12
        val colors = IntArray(colorCount + 1)
        val hsv = floatArrayOf(0f, 1f, 1f)
        for (i in colors.indices) {
            hsv[0] = ((i * colorAngleStep + 180) % 360).toFloat()
            colors[i] = Color.HSVToColor(hsv)
        }
        colors[colorCount] = colors[0]

        val sweepGradient = SweepGradient((width / 2).toFloat(), (height / 2).toFloat(), colors, null)
        val radialGradient = RadialGradient((width / 2).toFloat(), (height / 2).toFloat(), colorWheelRadius.toFloat(), -0x1, 0x00FFFFFF, TileMode.CLAMP)
        val composeShader = ComposeShader(sweepGradient, radialGradient, PorterDuff.Mode.SRC_OVER)

        colorWheelPaint!!.shader = composeShader

        val canvas = Canvas(bitmap)
        canvas.drawCircle((width / 2).toFloat(), (height / 2).toFloat(), colorWheelRadius.toFloat(), colorWheelPaint!!)

        return bitmap

    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val action = event.action
        when (action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {

                val x = event.x.toInt()
                val y = event.y.toInt()
                val cx = x - width / 2
                val cy = y - height / 2
                val d = Math.sqrt((cx * cx + cy * cy).toDouble())

                if (d <= colorWheelRadius) {

                    colorHSV[0] = (Math.toDegrees(Math.atan2(cy.toDouble(), cx.toDouble())) + 180f).toFloat()
                    colorHSV[1] = Math.max(0f, Math.min(1f, (d / colorWheelRadius).toFloat()))
                    if (onChangedListener != null) {
                        onChangedListener!!.colorChangedListener(color)
                    }
                    invalidate()

                } else if (x >= width / 2 && d >= innerWheelRadius) {
                    val light = Math.max(0.0, Math.min(1.0, Math.atan2(cy.toDouble(), cx.toDouble()) / Math.PI + 0.5f)).toFloat()
                    colorHSV[2] = if (light > 0.1) light else 0.1.toFloat()
                    if (onChangedListener != null) {
                        onChangedListener!!.colorChangedListener(color)
                    }
                    invalidate()
                }
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    override fun onSaveInstanceState(): Parcelable? {
        val state = Bundle()
        state.putFloatArray("color", colorHSV)
        state.putParcelable("super", super.onSaveInstanceState())
        return state
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        if (state is Bundle) {
            colorHSV = state.getFloatArray("color")
            super.onRestoreInstanceState(state.getParcelable("super"))
        } else {
            super.onRestoreInstanceState(state)
        }
    }

    interface OnColorChangedListener {
        fun colorChangedListener(color: Int)
    }
}