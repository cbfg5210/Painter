package com.ue.library.util

import android.animation.*
import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.graphics.*
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.support.annotation.ColorInt
import android.view.*
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.DecelerateInterpolator
import android.view.animation.TranslateAnimation
import android.widget.ImageView
import android.widget.ListView
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

/**
 * @author cginechen
 * @date 2016-03-17
 */
object QMUIViewHelper {

    // copy from View.generateViewId for API <= 16
    private val sNextGeneratedId = AtomicInteger(1)


    private val APPCOMPAT_CHECK_ATTRS = intArrayOf(android.support.v7.appcompat.R.attr.colorPrimary)

    /**
     * 判断是否需要对 LineSpacingExtra 进行额外的兼容处理
     * 安卓 5.0 以下版本中，LineSpacingExtra 在最后一行也会产生作用，因此会多出一个 LineSpacingExtra 的空白，可以通过该方法判断后进行兼容处理
     * if (QMUIViewHelper.getISLastLineSpacingExtraError()) {
     * textView.bottomMargin = -3dp;
     * } else {
     * textView.bottomMargin = 0;
     * }
     */
    val isLastLineSpacingExtraError: Boolean
        get() = Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP

    fun checkAppCompatTheme(context: Context) {
        val a = context.obtainStyledAttributes(APPCOMPAT_CHECK_ATTRS)
        val failed = !a.hasValue(0)
        a.recycle()
        if (failed) {
            throw IllegalArgumentException("You need to use a Theme.AppCompat theme " + "(or descendant) with the design library.")
        }
    }

    /**
     * 获取activity的根view
     */
    fun getActivityRoot(activity: Activity): View {
        return (activity.findViewById<View>(Window.ID_ANDROID_CONTENT) as ViewGroup).getChildAt(0)
    }

    /**
     * 触发window的insets的广播，使得view的fitSystemWindows得以生效
     */
    fun requestApplyInsets(window: Window) {
        if (Build.VERSION.SDK_INT in 19..20) {
            window.decorView.requestFitSystemWindows()
        } else if (Build.VERSION.SDK_INT >= 21) {
            window.decorView.requestApplyInsets()
        }
    }

    /**
     * 扩展点击区域的范围
     *
     * @param view       需要扩展的元素，此元素必需要有父级元素
     * @param expendSize 需要扩展的尺寸（以sp为单位的）
     */
    fun expendTouchArea(view: View?, expendSize: Int) {
        view?.apply {
            val parentView = parent as View
            parentView.post {
                val rect = Rect()
                getHitRect(rect) //如果太早执行本函数，会获取rect失败，因为此时UI界面尚未开始绘制，无法获得正确的坐标
                rect.left -= expendSize
                rect.top -= expendSize
                rect.right += expendSize
                rect.bottom += expendSize
                parentView.touchDelegate = TouchDelegate(rect, view)
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    fun setBackgroundKeepingPadding(view: View, drawable: Drawable) {
        val padding = intArrayOf(view.paddingLeft, view.paddingTop, view.paddingRight, view.paddingBottom)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            view.background = drawable
        } else {
            view.setBackgroundDrawable(drawable)
        }
        view.setPadding(padding[0], padding[1], padding[2], padding[3])
    }

    fun setBackgroundKeepingPadding(view: View, backgroundResId: Int) {
        setBackgroundKeepingPadding(view, view.resources.getDrawable(backgroundResId))
    }

    fun setBackgroundColorKeepPadding(view: View, @ColorInt color: Int) {
        val padding = intArrayOf(view.paddingLeft, view.paddingTop, view.paddingRight, view.paddingBottom)
        view.setBackgroundColor(color)
        view.setPadding(padding[0], padding[1], padding[2], padding[3])
    }

    /**
     * 对 View 的做背景闪动的动画
     */
    fun playBackgroundBlinkAnimation(v: View?, @ColorInt bgColor: Int) {
        v?.apply { playViewBackgroundAnimation(this, bgColor, intArrayOf(0, 255, 0), 300) }
    }

    /**
     * 对 View 做背景色变化的动作
     *
     * @param v            做背景色变化的View
     * @param bgColor      背景色
     * @param alphaArray   背景色变化的alpha数组，如 int[]{255,0} 表示从纯色变化到透明
     * @param stepDuration 每一步变化的时长
     * @param endAction    动画结束后的回调
     */
    @JvmOverloads
    fun playViewBackgroundAnimation(v: View, @ColorInt bgColor: Int, alphaArray: IntArray, stepDuration: Int, endAction: Runnable? = null) {
        val animationCount = alphaArray.size - 1

        val bgDrawable = ColorDrawable(bgColor)
        val oldBgDrawable = v.background
        setBackgroundKeepingPadding(v, bgDrawable)

        val animatorList = ArrayList<Animator>()
        for (i in 0 until animationCount) {
            val animator = ObjectAnimator.ofInt(v.background, "alpha", alphaArray[i], alphaArray[i + 1])
            animatorList.add(animator)
        }

        val animatorSet = AnimatorSet()
        animatorSet.duration = stepDuration.toLong()
        animatorSet.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {}

            override fun onAnimationEnd(animation: Animator) {
                setBackgroundKeepingPadding(v, oldBgDrawable)
                endAction?.run()
            }

            override fun onAnimationCancel(animation: Animator) {}

            override fun onAnimationRepeat(animation: Animator) {}
        })
        animatorSet.playSequentially(animatorList)
        animatorSet.start()
    }

    /**
     * 对 View 做背景色变化的动作
     *
     * @param v            做背景色变化的View
     * @param startColor   动画开始时 View 的背景色
     * @param endColor     动画结束时 View 的背景色
     * @param duration     动画总时长
     * @param repeatCount  动画重复次数
     * @param setAnimTagId 将动画设置tag给view,若为0则不设置
     * @param endAction    动画结束后的回调
     */
    @JvmOverloads
    fun playViewBackgroundAnimation(v: View, @ColorInt startColor: Int, @ColorInt endColor: Int, duration: Long, repeatCount: Int = 0, setAnimTagId: Int = 0, endAction: Runnable? = null) {
        val oldBgDrawable = v.background // 存储旧的背景
        QMUIViewHelper.setBackgroundColorKeepPadding(v, startColor)
        val anim = ValueAnimator()
        anim.setIntValues(startColor, endColor)
        anim.duration = duration / (repeatCount + 1)
        anim.repeatCount = repeatCount
        anim.repeatMode = ValueAnimator.REVERSE
        anim.setEvaluator(ArgbEvaluator())
        anim.addUpdateListener { animation -> QMUIViewHelper.setBackgroundColorKeepPadding(v, animation.animatedValue as Int) }
        if (setAnimTagId != 0) {
            v.setTag(setAnimTagId, anim)
        }
        anim.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {

            }

            override fun onAnimationEnd(animation: Animator) {
                setBackgroundKeepingPadding(v, oldBgDrawable)
                endAction?.run()
            }

            override fun onAnimationCancel(animation: Animator) {

            }

            override fun onAnimationRepeat(animation: Animator) {

            }
        })
        anim.start()
    }

    fun generateViewId(): Int {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return View.generateViewId()
        }
        while (true) {
            val result = sNextGeneratedId.get()
            // aapt-generated IDs have the high byte nonzero; clamp to the range under that.
            var newValue = result + 1
            if (newValue > 0x00FFFFFF) newValue = 1 // Roll over to 1, not 0.
            if (sNextGeneratedId.compareAndSet(result, newValue)) {
                return result
            }
        }
    }

    /**
     *
     * 对 View 做透明度变化的进场动画。
     *
     * 相关方法 [.fadeOut]
     *
     * @param view            做动画的 View
     * @param duration        动画时长(毫秒)
     * @param listener        动画回调
     * @param isNeedAnimation 是否需要动画
     */
    fun fadeIn(view: View?, duration: Int, listener: Animation.AnimationListener?, isNeedAnimation: Boolean): AlphaAnimation? {
        view ?: return null

        if (!isNeedAnimation) {
            //not need animation
            view.alpha = 1f
            view.visibility = View.VISIBLE
            return null
        }
        //need animation
        view.visibility = View.VISIBLE
        val alpha = AlphaAnimation(0f, 1f)
        alpha.interpolator = DecelerateInterpolator()
        alpha.duration = duration.toLong()
        alpha.fillAfter = true
        if (listener != null) {
            alpha.setAnimationListener(listener)
        }
        view.startAnimation(alpha)
        return alpha
    }

    /**
     *
     * 对 View 做透明度变化的退场动画
     *
     * 相关方法 [.fadeIn]
     *
     * @param view            做动画的 View
     * @param duration        动画时长(毫秒)
     * @param listener        动画回调
     * @param isNeedAnimation 是否需要动画
     */
    fun fadeOut(view: View?, duration: Int, listener: Animation.AnimationListener?, isNeedAnimation: Boolean): AlphaAnimation? {
        view ?: return null
        if (!isNeedAnimation) {
            //not need animation
            view.visibility = View.GONE
            return null
        }
        //need animation
        val alpha = AlphaAnimation(1f, 0f)
        alpha.interpolator = DecelerateInterpolator()
        alpha.duration = duration.toLong()
        alpha.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {
                listener?.onAnimationStart(animation)
            }

            override fun onAnimationEnd(animation: Animation) {
                view.visibility = View.GONE
                listener?.onAnimationEnd(animation)
            }

            override fun onAnimationRepeat(animation: Animation) {
                listener?.onAnimationRepeat(animation)
            }
        })
        view.startAnimation(alpha)
        return alpha
    }

    fun clearValueAnimator(animator: ValueAnimator?) {
        animator?.apply {
            removeAllListeners()
            removeAllUpdateListeners()
            if (Build.VERSION.SDK_INT >= 19) pause()
            cancel()
        }
    }

    fun calcViewScreenLocation(view: View): Rect {
        val location = IntArray(2)
        // 获取控件在屏幕中的位置，返回的数组分别为控件左顶点的 x、y 的值
        view.getLocationOnScreen(location)
        return Rect(location[0], location[1], location[0] + view.width, location[1] + view.height)
    }

    /**
     *
     * 对 View 做上下位移的进场动画
     *
     * 相关方法 [.slideOut]
     *
     * @param view            做动画的 View
     * @param duration        动画时长(毫秒)
     * @param listener        动画回调
     * @param isNeedAnimation 是否需要动画
     * @param direction       进场动画的方向
     * @return 动画对应的 Animator 对象, 注意无动画时返回 null
     */
    fun slideIn(view: View?, duration: Int, listener: Animation.AnimationListener?, isNeedAnimation: Boolean, direction: QMUIDirection): TranslateAnimation? {
        view ?: return null

        if (!isNeedAnimation) {
            //not need animation
            view.clearAnimation()
            view.visibility = View.VISIBLE
            return null
        }
        //need animation
        var translate: TranslateAnimation? = null
        when (direction) {
            QMUIDirection.LEFT_TO_RIGHT -> translate = TranslateAnimation(
                    Animation.RELATIVE_TO_SELF, -1f, Animation.RELATIVE_TO_SELF, 0f,
                    Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 0f)
            QMUIDirection.TOP_TO_BOTTOM -> translate = TranslateAnimation(
                    Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 0f,
                    Animation.RELATIVE_TO_SELF, -1f, Animation.RELATIVE_TO_SELF, 0f)
            QMUIDirection.RIGHT_TO_LEFT -> translate = TranslateAnimation(
                    Animation.RELATIVE_TO_SELF, 1f, Animation.RELATIVE_TO_SELF, 0f,
                    Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 0f)
            QMUIDirection.BOTTOM_TO_TOP -> translate = TranslateAnimation(
                    Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 0f,
                    Animation.RELATIVE_TO_SELF, 1f, Animation.RELATIVE_TO_SELF, 0f)
        }
        translate.interpolator = DecelerateInterpolator()
        translate.duration = duration.toLong()
        translate.fillAfter = true
        if (listener != null) {
            translate.setAnimationListener(listener)
        }
        view.visibility = View.VISIBLE
        view.startAnimation(translate)
        return translate
    }

    /**
     *
     * 对 View 做上下位移的退场动画
     *
     * 相关方法 [.slideIn]
     *
     * @param view            做动画的 View
     * @param duration        动画时长(毫秒)
     * @param listener        动画回调
     * @param isNeedAnimation 是否需要动画
     * @param direction       进场动画的方向
     * @return 动画对应的 Animator 对象, 注意无动画时返回 null
     */
    fun slideOut(view: View?, duration: Int, listener: Animation.AnimationListener?, isNeedAnimation: Boolean, direction: QMUIDirection): TranslateAnimation? {
        view ?: return null

        //not need animation
        if (!isNeedAnimation) {
            view.clearAnimation()
            view.visibility = View.GONE
            return null
        }
        //need animation
        var translate: TranslateAnimation? = null
        when (direction) {
            QMUIDirection.LEFT_TO_RIGHT -> translate = TranslateAnimation(
                    Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 1f,
                    Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 0f)
            QMUIDirection.TOP_TO_BOTTOM -> translate = TranslateAnimation(
                    Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 0f,
                    Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 1f)
            QMUIDirection.RIGHT_TO_LEFT -> translate = TranslateAnimation(
                    Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, -1f,
                    Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 0f)
            QMUIDirection.BOTTOM_TO_TOP -> translate = TranslateAnimation(
                    Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 0f,
                    Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, -1f)
        }
        translate.interpolator = DecelerateInterpolator()
        translate.duration = duration.toLong()
        translate.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {
                listener?.onAnimationStart(animation)
            }

            override fun onAnimationEnd(animation: Animation) {
                view.visibility = View.GONE
                listener?.onAnimationEnd(animation)
            }

            override fun onAnimationRepeat(animation: Animation) {
                listener?.onAnimationRepeat(animation)
            }
        })
        view.startAnimation(translate)
        return translate
    }

    /**
     * 对 View 设置 paddingLeft
     *
     * @param view  需要被设置的 View
     * @param value 设置的值
     */
    fun setPaddingLeft(view: View, value: Int) {
        view.setPadding(value, view.paddingTop, view.paddingRight, view.paddingBottom)
    }

    /**
     * 对 View 设置 paddingTop
     *
     * @param view  需要被设置的 View
     * @param value 设置的值
     */
    fun setPaddingTop(view: View, value: Int) {
        view.setPadding(view.paddingLeft, value, view.paddingRight, view.paddingBottom)
    }

    /**
     * 对 View 设置 paddingRight
     *
     * @param view  需要被设置的 View
     * @param value 设置的值
     */
    fun setPaddingRight(view: View, value: Int) {
        view.setPadding(view.paddingLeft, view.paddingTop, value, view.paddingBottom)
    }

    /**
     * 对 View 设置 paddingBottom
     *
     * @param view  需要被设置的 View
     * @param value 设置的值
     */
    fun setPaddingBottom(view: View, value: Int) {
        view.setPadding(view.paddingLeft, view.paddingTop, view.paddingRight, value)
    }

    /**
     * 把 ViewStub inflate 之后在其中根据 id 找 View
     *
     * @param parentView     包含 ViewStub 的 View
     * @param viewStubId     要从哪个 ViewStub 来 inflate
     * @param inflatedViewId 最终要找到的 View 的 id
     * @return id 为 inflatedViewId 的 View
     */
    fun findViewFromViewStub(parentView: View?, viewStubId: Int, inflatedViewId: Int): View? {
        parentView ?: return null

        var view: View? = parentView.findViewById(inflatedViewId)
        if (view == null) {
            val vs = parentView.findViewById<View>(viewStubId) as? ViewStub ?: return null
            view = vs.inflate()
            if (view != null) view = view.findViewById(inflatedViewId)
        }
        return view
    }

    /**
     * inflate ViewStub 并返回对应的 View。
     */
    fun findViewFromViewStub(parentView: View?, viewStubId: Int, inflatedViewId: Int, inflateLayoutResId: Int): View? {
        parentView ?: return null

        var view: View? = parentView.findViewById(inflatedViewId)
        if (view == null) {
            val vs = parentView.findViewById<View>(viewStubId) as? ViewStub ?: return null
            if (vs.layoutResource < 1 && inflateLayoutResId > 0) {
                vs.layoutResource = inflateLayoutResId
            }
            view = vs.inflate()
            if (view != null) view = view.findViewById(inflatedViewId)
        }
        return view
    }


    fun setImageViewTintColor(imageView: ImageView, @ColorInt tintColor: Int): ColorFilter {
        val colorFilter = LightingColorFilter(Color.argb(255, 0, 0, 0), tintColor)
        imageView.colorFilter = colorFilter
        return colorFilter
    }

    /**
     * 判断 ListView 是否已经滚动到底部。
     *
     * @param listView 需要被判断的 ListView。
     * @return ListView 已经滚动到底部则返回 true，否则返回 false。
     */
    fun isListViewAlreadyAtBottom(listView: ListView): Boolean {
        if (listView.adapter == null || listView.height == 0) return false

        if (listView.lastVisiblePosition == listView.adapter.count - 1) {
            val lastItemView = listView.getChildAt(listView.childCount - 1)
            if (lastItemView != null && lastItemView.bottom == listView.height) return true
        }
        return false
    }


    /**
     * Retrieve the transformed bounding rect of an arbitrary descendant view.
     * This does not need to be a direct child.
     *
     * @param descendant descendant view to reference
     * @param out        rect to set to the bounds of the descendant view
     */
    fun getDescendantRect(parent: ViewGroup, descendant: View, out: Rect) {
        out.set(0, 0, descendant.width, descendant.height)
        ViewGroupHelper.offsetDescendantRect(parent, descendant, out)
    }


    private object ViewGroupHelper {
        private val sMatrix = ThreadLocal<Matrix>()
        private val sRectF = ThreadLocal<RectF>()

        fun offsetDescendantRect(group: ViewGroup, child: View, rect: Rect) {
            var m = sMatrix.get()
            if (m == null) {
                m = Matrix()
                sMatrix.set(m)
            } else {
                m.reset()
            }

            offsetDescendantMatrix(group, child, m)

            var rectF = sRectF.get()
            if (rectF == null) {
                rectF = RectF()
                sRectF.set(rectF)
            }
            rectF.set(rect)
            m.mapRect(rectF)
            rect.set((rectF.left + 0.5f).toInt(), (rectF.top + 0.5f).toInt(),
                    (rectF.right + 0.5f).toInt(), (rectF.bottom + 0.5f).toInt())
        }

        internal fun offsetDescendantMatrix(target: ViewParent, view: View, m: Matrix) {
            val parent = view.parent
            if (parent is View && parent !== target) {
                offsetDescendantMatrix(target, parent, m)
                m.preTranslate((-parent.scrollX).toFloat(), (-parent.scrollY).toFloat())
            }

            m.preTranslate(view.left.toFloat(), view.top.toFloat())

            if (!view.matrix.isIdentity) {
                m.preConcat(view.matrix)
            }
        }
    }
}