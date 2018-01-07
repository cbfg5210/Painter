package com.ue.pixelpaint

import android.content.Context
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import com.ue.pixelpaint.gesture.Vector2D


/**
 * Created by hawk on 2018/1/4.
 */
class PixelView : View, View.OnTouchListener {
    var lineNum = 16
    private var linePaint: Paint = Paint()
    private var squareSize = 0F
    private var tempSize = 0F
    private var canvasSize = 0F

    private var thirdOneRatio = 0F
    private var thirdTwoRatio = 0F

    private val INVALID_POINTER_ID = -1
    var isTranslateEnabled = true
    var isScaleEnabled = true
    var minimumScale = 1f
    var maximumScale = 10f
    private var mActivePointerId = INVALID_POINTER_ID
    private var mPrevX = 0f
    private var mPrevY = 0f
    private var mScaleGestureDetector: NScaleGestureDetector

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle) {
//        thirdOneRatio = resources.getDimension(R.dimen.widget_size_1) / 3
//        thirdTwoRatio = resources.getDimension(R.dimen.widget_size_2) / 3
        mScaleGestureDetector = NScaleGestureDetector(context, ScaleGestureListener())
        setOnTouchListener(this)
    }

/*    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec)
        Log.e("PixelView", "onMeasure: w=$measuredWidth,h=$measuredHeight")
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        render(canvas)
    }

    private fun render(canvas: Canvas) {
        canvas.drawColor(Color.WHITE)

        squareSize = measuredWidth * scaleX / lineNum
        canvasSize = squareSize * lineNum
        Log.e("PixelView", "render: squareSize=$squareSize,canvasSize=$canvasSize,w=$measuredWidth,h=$measuredHeight")

        linePaint.color = Color.parseColor("#010101")
        linePaint.strokeWidth = thirdOneRatio

        for (i in 1 until lineNum) {
            tempSize = squareSize * i
            //horizontal line
            canvas.drawLine(0F, tempSize, canvasSize, tempSize, linePaint)
            //vertical line
            canvas.drawLine(tempSize, 0F, tempSize, canvasSize, linePaint)
        }

        linePaint.color = Color.BLACK
        linePaint.strokeWidth = thirdTwoRatio

        var i = 8
        while (i < lineNum) {
            tempSize = squareSize * i
            //horizontal line
            canvas.drawLine(0F, tempSize, canvasSize, tempSize, linePaint)
            //vertical line
            canvas.drawLine(tempSize, 0F, tempSize, canvasSize, linePaint)

            i += 8
        }
    }*/

    private fun move(view: View, info: TransformInfo) {
        computeRenderOffset(view, info.pivotX, info.pivotY)
        adjustTranslation(view, info.deltaX, info.deltaY)

        // Assume that scaling still maintains aspect ratio.
        var scale = view.scaleX * info.deltaScale
        scale = Math.max(info.minimumScale, Math.min(info.maximumScale, scale))
        view.scaleX = scale
        view.scaleY = scale
    }

    private fun adjustTranslation(view: View, deltaX: Float, deltaY: Float) {
        val deltaVector = floatArrayOf(deltaX, deltaY)
        view.matrix.mapVectors(deltaVector)
        view.translationX = view.translationX + deltaVector[0]
        view.translationY = view.translationY + deltaVector[1]
    }

    private fun computeRenderOffset(view: View, pivotX: Float, pivotY: Float) {
        if (view.pivotX == pivotX && view.pivotY == pivotY) {
            return
        }

        val prevPoint = floatArrayOf(0.0f, 0.0f)
        view.matrix.mapPoints(prevPoint)

        view.pivotX = pivotX
        view.pivotY = pivotY

        val currPoint = floatArrayOf(0.0f, 0.0f)
        view.matrix.mapPoints(currPoint)

        val offsetX = currPoint[0] - prevPoint[0]
        val offsetY = currPoint[1] - prevPoint[1]

        view.translationX = view.translationX - offsetX
        view.translationY = view.translationY - offsetY
    }

    override fun onTouch(view: View, event: MotionEvent): Boolean {
        mScaleGestureDetector.onTouchEvent(view, event)

        if (!isTranslateEnabled) {
            return true
        }

        val action = event.action
        when (action and event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                mPrevX = event.x
                mPrevY = event.y

                // Save the ID of this pointer.
                mActivePointerId = event.getPointerId(0)
            }

            MotionEvent.ACTION_MOVE -> {
                // Find the index of the active pointer and fetch its position.
                val pointerIndex = event.findPointerIndex(mActivePointerId)
                if (pointerIndex != -1) {
                    val currX = event.getX(pointerIndex)
                    val currY = event.getY(pointerIndex)

                    // Only move if the ScaleGestureDetector isn't processing a
                    // gesture.
                    if (!mScaleGestureDetector.isInProgress) {
                        adjustTranslation(view, currX - mPrevX, currY - mPrevY)
                    }
                }
            }

            MotionEvent.ACTION_CANCEL -> mActivePointerId = INVALID_POINTER_ID

            MotionEvent.ACTION_UP -> mActivePointerId = INVALID_POINTER_ID

            MotionEvent.ACTION_POINTER_UP -> {
                // Extract the index of the pointer that left the touch sensor.
                val pointerIndex = action and MotionEvent.ACTION_POINTER_INDEX_MASK shr MotionEvent.ACTION_POINTER_INDEX_SHIFT
                val pointerId = event.getPointerId(pointerIndex)
                if (pointerId == mActivePointerId) {
                    // This was our active pointer going up. Choose a new
                    // active pointer and adjust accordingly.
                    val newPointerIndex = if (pointerIndex == 0) 1 else 0
                    mPrevX = event.getX(newPointerIndex)
                    mPrevY = event.getY(newPointerIndex)
                    mActivePointerId = event.getPointerId(newPointerIndex)
                }
            }
        }

        return true
    }

    private inner class ScaleGestureListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {

        private var mPivotX = 0f
        private var mPivotY = 0f
        private val mPrevSpanVector = Vector2D()

        override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
            mPivotX = detector.focusX
            mPivotY = detector.focusY
            detector as NScaleGestureDetector
            mPrevSpanVector.set(detector.currentSpanVector)
            return true
        }

        override fun onScale(detector: ScaleGestureDetector): Boolean {
            val info = TransformInfo()
            info.deltaScale = if (isScaleEnabled) detector.scaleFactor else 1.0f
            info.deltaX = if (isTranslateEnabled) detector.focusX - mPivotX else 0.0f
            info.deltaY = if (isTranslateEnabled) detector.focusY - mPivotY else 0.0f
            info.pivotX = mPivotX
            info.pivotY = mPivotY
            info.minimumScale = minimumScale
            info.maximumScale = maximumScale

            move(this@PixelView, info)
            return false
        }
    }

    private inner class TransformInfo {
        var deltaX = 0f
        var deltaY = 0f
        var deltaScale = 0f
        var pivotX = 0f
        var pivotY = 0f
        var minimumScale = 0f
        var maximumScale = 0f
    }

    internal class NScaleGestureDetector(context: Context, private val mListener: android.view.ScaleGestureDetector.OnScaleGestureListener) : ScaleGestureDetector(context, mListener) {
        private var mGestureInProgress: Boolean = false

        private var mPrevEvent: MotionEvent? = null
        private var mCurrEvent: MotionEvent? = null

        val currentSpanVector: Vector2D
        private var mFocusX: Float = 0.toFloat()
        private var mFocusY: Float = 0.toFloat()
        private var mPrevFingerDiffX: Float = 0.toFloat()
        private var mPrevFingerDiffY: Float = 0.toFloat()
        private var mCurrFingerDiffX: Float = 0.toFloat()
        private var mCurrFingerDiffY: Float = 0.toFloat()
        private var mCurrLen: Float = 0.toFloat()
        private var mPrevLen: Float = 0.toFloat()
        private var mScaleFactor: Float = 0.toFloat()
        private var mCurrPressure: Float = 0.toFloat()
        private var mPrevPressure: Float = 0.toFloat()

        private var mInvalidGesture: Boolean = false

        // Pointer IDs currently responsible for the two fingers controlling the gesture
        private var mActiveId0: Int = 0
        private var mActiveId1: Int = 0
        private var mActive0MostRecent: Boolean = false

        init {
            currentSpanVector = Vector2D()
        }

        fun onTouchEvent(view: View, event: MotionEvent): Boolean {
            val action = event.actionMasked

            if (action == MotionEvent.ACTION_DOWN) {
                reset() // Start fresh
            }

            var handled = true
            if (mInvalidGesture) {
                handled = false
            } else if (!mGestureInProgress) {
                when (action) {
                    MotionEvent.ACTION_DOWN -> {
                        mActiveId0 = event.getPointerId(0)
                        mActive0MostRecent = true
                    }

                    MotionEvent.ACTION_UP -> reset()

                    MotionEvent.ACTION_POINTER_DOWN -> {
                        // We have a new multi-finger gesture
                        if (mPrevEvent != null) mPrevEvent!!.recycle()
                        mPrevEvent = MotionEvent.obtain(event)

                        val index1 = event.actionIndex
                        var index0 = event.findPointerIndex(mActiveId0)
                        mActiveId1 = event.getPointerId(index1)
                        if (index0 < 0 || index0 == index1) {
                            // Probably someone sending us a broken event stream.
                            index0 = findNewActiveIndex(event, mActiveId1, -1)
                            mActiveId0 = event.getPointerId(index0)
                        }
                        mActive0MostRecent = false

                        setContext(event)

                        mGestureInProgress = mListener.onScaleBegin(this)
                    }
                }
            } else {
                // Transform gesture in progress - attempt to handle it
                when (action) {
                    MotionEvent.ACTION_POINTER_DOWN -> {
                        // End the old gesture and begin a new one with the most recent two fingers.
                        mListener.onScaleEnd(this)
                        val oldActive0 = mActiveId0
                        val oldActive1 = mActiveId1
                        reset()

                        mPrevEvent = MotionEvent.obtain(event)
                        mActiveId0 = if (mActive0MostRecent) oldActive0 else oldActive1
                        mActiveId1 = event.getPointerId(event.actionIndex)
                        mActive0MostRecent = false

                        var index0 = event.findPointerIndex(mActiveId0)
                        if (index0 < 0 || mActiveId0 == mActiveId1) {
                            // Probably someone sending us a broken event stream.
                            index0 = findNewActiveIndex(event, mActiveId1, -1)
                            mActiveId0 = event.getPointerId(index0)
                        }

                        setContext(event)

                        mGestureInProgress = mListener.onScaleBegin(this)
                    }

                    MotionEvent.ACTION_POINTER_UP -> {
                        val pointerCount = event.pointerCount
                        val actionIndex = event.actionIndex
                        val actionId = event.getPointerId(actionIndex)

                        var gestureEnded = false
                        if (pointerCount > 2) {
                            if (actionId == mActiveId0) {
                                val newIndex = findNewActiveIndex(event, mActiveId1, actionIndex)
                                if (newIndex >= 0) {
                                    mListener.onScaleEnd(this)
                                    mActiveId0 = event.getPointerId(newIndex)
                                    mActive0MostRecent = true
                                    mPrevEvent = MotionEvent.obtain(event)
                                    setContext(event)
                                    mGestureInProgress = mListener.onScaleBegin(this)
                                } else {
                                    gestureEnded = true
                                }
                            } else if (actionId == mActiveId1) {
                                val newIndex = findNewActiveIndex(event, mActiveId0, actionIndex)
                                if (newIndex >= 0) {
                                    mListener.onScaleEnd(this)
                                    mActiveId1 = event.getPointerId(newIndex)
                                    mActive0MostRecent = false
                                    mPrevEvent = MotionEvent.obtain(event)
                                    setContext(event)
                                    mGestureInProgress = mListener.onScaleBegin(this)
                                } else {
                                    gestureEnded = true
                                }
                            }
                            mPrevEvent!!.recycle()
                            mPrevEvent = MotionEvent.obtain(event)
                            setContext(event)
                        } else {
                            gestureEnded = true
                        }

                        if (gestureEnded) {
                            // Gesture ended
                            setContext(event)

                            // Set focus point to the remaining finger
                            val activeId = if (actionId == mActiveId0) mActiveId1 else mActiveId0
                            val index = event.findPointerIndex(activeId)
                            mFocusX = event.getX(index)
                            mFocusY = event.getY(index)

                            mListener.onScaleEnd(this)
                            reset()
                            mActiveId0 = activeId
                            mActive0MostRecent = true
                        }
                    }

                    MotionEvent.ACTION_CANCEL -> {
                        mListener.onScaleEnd(this)
                        reset()
                    }

                    MotionEvent.ACTION_UP -> reset()

                    MotionEvent.ACTION_MOVE -> {
                        setContext(event)

                        // Only accept the event if our relative pressure is within
                        // a certain limit - this can help filter shaky data as a
                        // finger is lifted.
                        if (mCurrPressure / mPrevPressure > PRESSURE_THRESHOLD) {
                            val updatePrevious = mListener.onScale(this)

                            if (updatePrevious) {
                                mPrevEvent!!.recycle()
                                mPrevEvent = MotionEvent.obtain(event)
                            }
                        }
                    }
                }
            }

            return handled
        }

        private fun findNewActiveIndex(ev: MotionEvent, otherActiveId: Int, removedPointerIndex: Int): Int {
            val pointerCount = ev.pointerCount

            // It's ok if this isn't found and returns -1, it simply won't match.
            val otherActiveIndex = ev.findPointerIndex(otherActiveId)

            // Pick a new id and update tracking state.
            for (i in 0 until pointerCount) {
                if (i != removedPointerIndex && i != otherActiveIndex) {
                    return i
                }
            }
            return -1
        }

        private fun setContext(curr: MotionEvent) {
            if (mCurrEvent != null) {
                mCurrEvent!!.recycle()
            }
            mCurrEvent = MotionEvent.obtain(curr)

            mCurrLen = -1f
            mPrevLen = -1f
            mScaleFactor = -1f
            currentSpanVector.set(0.0f, 0.0f)

            val prev = mPrevEvent

            val prevIndex0 = prev!!.findPointerIndex(mActiveId0)
            val prevIndex1 = prev.findPointerIndex(mActiveId1)
            val currIndex0 = curr.findPointerIndex(mActiveId0)
            val currIndex1 = curr.findPointerIndex(mActiveId1)

            if (prevIndex0 < 0 || prevIndex1 < 0 || currIndex0 < 0 || currIndex1 < 0) {
                mInvalidGesture = true
                Log.e(TAG, "Invalid MotionEvent stream detected.", Throwable())
                if (mGestureInProgress) {
                    mListener.onScaleEnd(this)
                }
                return
            }

            val px0 = prev.getX(prevIndex0)
            val py0 = prev.getY(prevIndex0)
            val px1 = prev.getX(prevIndex1)
            val py1 = prev.getY(prevIndex1)
            val cx0 = curr.getX(currIndex0)
            val cy0 = curr.getY(currIndex0)
            val cx1 = curr.getX(currIndex1)
            val cy1 = curr.getY(currIndex1)

            val pvx = px1 - px0
            val pvy = py1 - py0
            val cvx = cx1 - cx0
            val cvy = cy1 - cy0

            currentSpanVector.set(cvx, cvy)

            mPrevFingerDiffX = pvx
            mPrevFingerDiffY = pvy
            mCurrFingerDiffX = cvx
            mCurrFingerDiffY = cvy

            mFocusX = cx0 + cvx * 0.5f
            mFocusY = cy0 + cvy * 0.5f
            mCurrPressure = curr.getPressure(currIndex0) + curr.getPressure(currIndex1)
            mPrevPressure = prev.getPressure(prevIndex0) + prev.getPressure(prevIndex1)
        }

        private fun reset() {
            if (mPrevEvent != null) {
                mPrevEvent!!.recycle()
                mPrevEvent = null
            }
            if (mCurrEvent != null) {
                mCurrEvent!!.recycle()
                mCurrEvent = null
            }
            mGestureInProgress = false
            mActiveId0 = -1
            mActiveId1 = -1
            mInvalidGesture = false
        }

        /**
         * Returns `true` if a two-finger scale gesture is in progress.
         *
         * @return `true` if a scale gesture is in progress, `false` otherwise.
         */
        override fun isInProgress(): Boolean {
            return mGestureInProgress
        }

        /**
         * Get the X coordinate of the current gesture's focal point.
         * If a gesture is in progress, the focal point is directly between
         * the two pointers forming the gesture.
         * If a gesture is ending, the focal point is the location of the
         * remaining pointer on the screen.
         * If [.isInProgress] would return false, the result of this
         * function is undefined.
         *
         * @return X coordinate of the focal point in pixels.
         */
        override fun getFocusX(): Float {
            return mFocusX
        }

        /**
         * Get the Y coordinate of the current gesture's focal point.
         * If a gesture is in progress, the focal point is directly between
         * the two pointers forming the gesture.
         * If a gesture is ending, the focal point is the location of the
         * remaining pointer on the screen.
         * If [.isInProgress] would return false, the result of this
         * function is undefined.
         *
         * @return Y coordinate of the focal point in pixels.
         */
        override fun getFocusY(): Float {
            return mFocusY
        }

        /**
         * Return the current distance between the two pointers forming the
         * gesture in progress.
         *
         * @return Distance between pointers in pixels.
         */
        override fun getCurrentSpan(): Float {
            if (mCurrLen == -1f) {
                val cvx = mCurrFingerDiffX
                val cvy = mCurrFingerDiffY
                mCurrLen = Math.sqrt((cvx * cvx + cvy * cvy).toDouble()).toFloat()
            }
            return mCurrLen
        }

        /**
         * Return the previous distance between the two pointers forming the
         * gesture in progress.
         *
         * @return Previous distance between pointers in pixels.
         */
        override fun getPreviousSpan(): Float {
            if (mPrevLen == -1f) {
                val pvx = mPrevFingerDiffX
                val pvy = mPrevFingerDiffY
                mPrevLen = Math.sqrt((pvx * pvx + pvy * pvy).toDouble()).toFloat()
            }
            return mPrevLen
        }

        /**
         * Return the scaling factor from the previous scale event to the current
         * event. This value is defined as
         * ([.getCurrentSpan] / [.getPreviousSpan]).
         *
         * @return The current scaling factor.
         */
        override fun getScaleFactor(): Float {
            if (mScaleFactor == -1f) {
                mScaleFactor = currentSpan / previousSpan
            }
            return mScaleFactor
        }

        companion object {
            private val TAG = "ScaleGestureDetector"

            /**
             * This value is the threshold ratio between our previous combined pressure
             * and the current combined pressure. We will only fire an onScale event if
             * the computed ratio between the current and previous event pressures is
             * greater than this value. When pressure decreases rapidly between events
             * the position values can often be imprecise, as it usually indicates
             * that the user is in the process of lifting a pointer off of the device.
             * Its value was tuned experimentally.
             */
            private val PRESSURE_THRESHOLD = 0.67f
        }
    }
}