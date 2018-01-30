package com.ue.pixel

import android.view.MotionEvent
import android.view.View

/**
 * Created by hawk on 2018/1/7.
 */
class GestureTouchListener : View.OnTouchListener {

    private var mPrevEvent: MotionEvent? = null
    private var mCurrEvent: MotionEvent? = null

    private var mPivotX = 0f
    private var mPivotY = 0f
    private var mFocusX = 0f
    private var mFocusY = 0f
    private var mPrevFingerDiffX = 0f
    private var mPrevFingerDiffY = 0f
    private var mCurrFingerDiffX = 0f
    private var mCurrFingerDiffY = 0f
    private var mCurrLen = 0f
    private var mPrevLen = 0f
    private var mScaleFactor = 0f
    private var mCurrPressure = 0f
    private var mPrevPressure = 0f
    // Pointer IDs currently responsible for the two fingers controlling the gesture
    private var mActiveId0 = 0
    private var mActiveId1 = 0

    private var mGestureInProgress = false
    private var mInvalidGesture = false
    private var mActive0MostRecent = false

    var isTranslateEnabled = true
    var isScaleEnabled = true
    var minimumScale = 1f
    var maximumScale = 10f
    private var mActivePointerId = INVALID_POINTER_ID
    private var mPrevX = 0f
    private var mPrevY = 0f

    companion object {
        private val PRESSURE_THRESHOLD = 0.67f
        private val INVALID_POINTER_ID = -1
    }

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        handleEventForScale(v, event)
        if (isTranslateEnabled) {
            handleEventForTranslate(v, event)
        }
        return true
    }

    private fun handleEventForScale(view: View, event: MotionEvent): Boolean {
        val action = event.actionMasked

        if (action == MotionEvent.ACTION_DOWN) {
            reset() // Start fresh
        }

        if (mInvalidGesture) {
            return false
        }

        if (!mGestureInProgress) {
            when (action) {
                MotionEvent.ACTION_DOWN -> {
                    mActiveId0 = event.getPointerId(0)
                    mActive0MostRecent = true
                }

                MotionEvent.ACTION_UP -> {
                    reset()
                }

                MotionEvent.ACTION_POINTER_DOWN -> {
                    // We have a new multi-finger gesture
                    mPrevEvent?.recycle()
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

                    onScaleBegin()
                }
            }
            return true
        }

        // Transform gesture in progress - attempt to handle it
        when (action) {
            MotionEvent.ACTION_POINTER_DOWN -> {
                // End the old gesture and begin a new one with the most recent two fingers.
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

                onScaleBegin()
            }

            MotionEvent.ACTION_POINTER_UP -> {
                handlePointerUpEvent(event)
            }

            MotionEvent.ACTION_CANCEL,
            MotionEvent.ACTION_UP -> {
                reset()
            }

            MotionEvent.ACTION_MOVE -> {
                setContext(event)
                // Only accept the event if our relative pressure is within
                // a certain limit - this can help filter shaky data as a
                // finger is lifted.
                if (mCurrPressure / mPrevPressure > PRESSURE_THRESHOLD) {
                    if (onScale(view)) {
                        mPrevEvent?.recycle()
                        mPrevEvent = MotionEvent.obtain(event)
                    }
                }
            }
        }

        return true
    }

    private fun handlePointerUpEvent(event: MotionEvent) {
        val actionId = event.getPointerId(event.actionIndex)
        var gestureEnded = event.pointerCount <= 2

        if (!gestureEnded) {
            if (actionId != mActiveId0) {
                val newIndex = findNewActiveIndex(event, mActiveId1, event.actionIndex)
                if (newIndex >= 0) {
                    mActiveId0 = event.getPointerId(newIndex)
                    mActive0MostRecent = true
                    mPrevEvent = MotionEvent.obtain(event)
                    setContext(event)
                    onScaleBegin()
                } else {
                    gestureEnded = true
                }
            } else if (actionId == mActiveId1) {
                val newIndex = findNewActiveIndex(event, mActiveId0, event.actionIndex)
                if (newIndex >= 0) {
                    mActiveId1 = event.getPointerId(newIndex)
                    mActive0MostRecent = false
                    mPrevEvent = MotionEvent.obtain(event)
                    setContext(event)
                    onScaleBegin()
                } else {
                    gestureEnded = true
                }
            }
            mPrevEvent?.recycle()
            mPrevEvent = MotionEvent.obtain(event)
            setContext(event)
        }

        if (gestureEnded) {
            // Gesture ended
            setContext(event)
            // Set focus point to the remaining finger
            val activeId = if (actionId == mActiveId0) mActiveId1 else mActiveId0
            val index = event.findPointerIndex(activeId)
            mFocusX = event.getX(index)
            mFocusY = event.getY(index)

            reset()
            mActiveId0 = activeId
            mActive0MostRecent = true
        }
    }

    private fun handleEventForTranslate(view: View, event: MotionEvent) {
        when (event.action and event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                mPrevX = event.x
                mPrevY = event.y
                // Save the ID of this pointer.
                mActivePointerId = event.getPointerId(0)
            }

            MotionEvent.ACTION_MOVE -> {
                // Find the index of the active pointer and fetch its position.
                val pointerIndex = event.findPointerIndex(mActivePointerId)
                if (pointerIndex != -1 && !mGestureInProgress) {
                    // Only move if the ScaleGestureDetector isn't processing a gesture.
                    adjustTranslation(view, event.getX(pointerIndex) - mPrevX, event.getY(pointerIndex) - mPrevY)
                }
            }

            MotionEvent.ACTION_CANCEL,
            MotionEvent.ACTION_UP -> {
                mActivePointerId = INVALID_POINTER_ID
            }

            MotionEvent.ACTION_POINTER_UP -> {
                // Extract the index of the pointer that left the touch sensor.
                val pointerIndex = event.action and MotionEvent.ACTION_POINTER_INDEX_MASK shr MotionEvent.ACTION_POINTER_INDEX_SHIFT
                if (event.getPointerId(pointerIndex) == mActivePointerId) {
                    // This was our active pointer going up. Choose a new
                    // active pointer and adjust accordingly.
                    val newPointerIndex = if (pointerIndex == 0) 1 else 0
                    mPrevX = event.getX(newPointerIndex)
                    mPrevY = event.getY(newPointerIndex)
                    mActivePointerId = event.getPointerId(newPointerIndex)
                }
            }
        }
    }

    private fun move(view: View, info: TransformInfo) {
        computeRenderOffset(view, info.pivotX, info.pivotY)
        adjustTranslation(view, info.deltaX, info.deltaY)
        // Assume that scaling still maintains aspect ratio.
        val scale = Math.max(minimumScale, Math.min(maximumScale, view.scaleX * info.deltaScale))
        view.scaleX = scale
        view.scaleY = scale
    }

    private fun adjustTranslation(view: View, deltaX: Float, deltaY: Float) {
        val deltaVector = floatArrayOf(deltaX, deltaY)
        view.matrix.mapVectors(deltaVector)
        view.translationX += deltaVector[0]
        view.translationY += deltaVector[1]
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

        view.translationX -= offsetX
        view.translationY -= offsetY
    }

    private fun onScaleBegin(): Boolean {
        mPivotX = mFocusX
        mPivotY = mFocusY
        mGestureInProgress = true
        return true
    }

    private fun onScale(view: View): Boolean {
        val info = TransformInfo()
        info.deltaScale = if (isScaleEnabled) getScaleFactor() else 1.0f
        info.deltaX = if (isTranslateEnabled) mFocusX - mPivotX else 0.0f
        info.deltaY = if (isTranslateEnabled) mFocusY - mPivotY else 0.0f
        info.pivotX = mPivotX
        info.pivotY = mPivotY

        move(view, info)
        return false
    }

    private fun findNewActiveIndex(ev: MotionEvent, otherActiveId: Int, removedPointerIndex: Int): Int {
        // It's ok if this isn't found and returns -1, it simply won't match.
        val otherActiveIndex = ev.findPointerIndex(otherActiveId)
        // Pick a new id and update tracking state.
        for (i in 0 until ev.pointerCount) {
            if (i != removedPointerIndex && i != otherActiveIndex) {
                return i
            }
        }
        return -1
    }

    private fun setContext(curr: MotionEvent) {
        mCurrEvent?.recycle()
        mCurrEvent = MotionEvent.obtain(curr)

        mCurrLen = -1f
        mPrevLen = -1f
        mScaleFactor = -1f

        val prev = mPrevEvent!!

        val prevIndex0 = prev.findPointerIndex(mActiveId0)
        val prevIndex1 = prev.findPointerIndex(mActiveId1)
        val currIndex0 = curr.findPointerIndex(mActiveId0)
        val currIndex1 = curr.findPointerIndex(mActiveId1)

        if (prevIndex0 < 0 || prevIndex1 < 0 || currIndex0 < 0 || currIndex1 < 0) {
            mInvalidGesture = true
            return
        }

        val cx0 = curr.getX(currIndex0)
        val cy0 = curr.getY(currIndex0)

        mPrevFingerDiffX = prev.getX(prevIndex1) - prev.getX(prevIndex0)
        mPrevFingerDiffY = prev.getY(prevIndex1) - prev.getY(prevIndex0)
        mCurrFingerDiffX = curr.getX(currIndex1) - cx0
        mCurrFingerDiffY = curr.getY(currIndex1) - cy0

        mFocusX = cx0 + mCurrFingerDiffX * 0.5f
        mFocusY = cy0 + mCurrFingerDiffY * 0.5f
        mCurrPressure = curr.getPressure(currIndex0) + curr.getPressure(currIndex1)
        mPrevPressure = prev.getPressure(prevIndex0) + prev.getPressure(prevIndex1)
    }

    private fun reset() {
        mPrevEvent?.recycle()
        mPrevEvent = null

        mCurrEvent?.recycle()
        mCurrEvent = null

        mGestureInProgress = false
        mActiveId0 = -1
        mActiveId1 = -1
        mInvalidGesture = false
    }

    /**
     * Return the current distance between the two pointers forming the
     * gesture in progress.
     *
     * @return Distance between pointers in pixels.
     */
    private fun getCurrentSpan(): Float {
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
    private fun getPreviousSpan(): Float {
        if (mPrevLen == -1f) {
            mPrevLen = Math.sqrt((mPrevFingerDiffX * mPrevFingerDiffX + mPrevFingerDiffY * mPrevFingerDiffY).toDouble()).toFloat()
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
    private fun getScaleFactor(): Float {
        if (mScaleFactor == -1f) {
            mScaleFactor = getCurrentSpan() / getPreviousSpan()
        }
        return mScaleFactor
    }

    private inner class TransformInfo {
        var deltaX = 0f
        var deltaY = 0f
        var deltaScale = 0f
        var pivotX = 0f
        var pivotY = 0f
    }
}