package com.ue.library.widget.dialog

import android.annotation.SuppressLint
import android.content.Context
import android.support.annotation.StyleRes
import android.text.TextUtils
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.ue.library.R
import com.ue.library.util.QMUIDisplayHelper
import com.ue.library.util.QMUIResHelper
import kotlinx.android.synthetic.main.qmui_dialog_layout.view.*
import java.util.*

/**
 * 创建 [QMUIDialog] 的 Builder 基类, 不同的 Builder 子类拥有创建不同类型对话框的能力, 具体见子类。
 *
 * 该类产生的 Dialog 分为上中下三个部分:
 *
 *  * 上部分是 title 区域, 支持显示纯文本标题, 通过 [.setTitle] 系列方法设置。
 * 子类也可以通过 override [.onCreateTitle] 方法自定义
 *  * 中间部分的内容由各个子类决定, 子类通过 override [.onCreateContent] 方法自定义。
 *  * 下部分是操作区域, 支持添加操作按钮, 通过 [.addAction] 系列方法添加。
 * 子类也可以通过 override [.onCreateHandlerBar] 方法自定义。
 * 其中操作按钮有内联和块级之分, 也有普通、正向、反向之分, 具体见 [QMUIDialogAction]
 *
 *
 *
 * @author cginechen
 * @date 2015-10-20
 */
abstract class QMUIDialogBuilder<T : QMUIDialogBuilder<T>>(protected var mContext: Context) {
    protected lateinit var mDialog: QMUIDialog
    protected var mInflater = LayoutInflater.from(mContext)
    protected var mTitle: String? = null

    protected lateinit var mRootView: LinearLayout
    protected lateinit var mDialogWrapper: LinearLayout
    lateinit var anchorTopView: View
        private set
    lateinit var anchorBottomView: View
        private set
    protected var mActions: MutableList<QMUIDialogAction> = ArrayList()
    private var mLeftAction: QMUIDialogAction? = null

    var titleView: TextView? = null
        protected set
    protected var mActionContainer: LinearLayout? = null
    private var mContentAreaMaxHeight: Int = 0

    val positiveAction: List<QMUIDialogAction>
        get() {
            return mActions.filter { it.actionProp == QMUIDialogAction.ACTION_PROP_POSITIVE }
        }

    init {
        mContentAreaMaxHeight = (mContext.resources.displayMetrics.heightPixels * 0.75).toInt()
    }

    protected fun getContentAreaMaxHeight(): Int {
        return mContentAreaMaxHeight
    }

    /**
     * 设置内容区域最高的高度
     *
     * @param contentAreaMaxHeight
     */
    fun setContentAreaMaxHeight(contentAreaMaxHeight: Int): T {
        mContentAreaMaxHeight = contentAreaMaxHeight
        return this as T
    }

    /**
     * 设置对话框顶部的标题文字
     */
    fun setTitle(title: String?): T {
        if (!TextUtils.isEmpty(title)) this.mTitle = title + mContext.getString(R.string.qmui_tool_fixellipsize)
        return this as T
    }

    /**
     * 设置对话框顶部的标题文字
     */
    fun setTitle(resId: Int): T {
        return setTitle(mContext.resources.getString(resId))
    }

    //region 添加action

    /**
     * 添加对话框底部的操作按钮
     */
    fun addAction(action: QMUIDialogAction?): T {
        if (action != null) mActions.add(action)
        return this as T
    }

    /**
     * 添加无图标正常类型的操作按钮
     *
     * @param strResId 文案
     * @param listener 点击回调事件
     */
    fun addAction(strResId: Int, listener: QMUIDialogAction.ActionListener): T {
        return addAction(0, strResId, listener)
    }

    /**
     * 添加无图标正常类型的操作按钮
     *
     * @param str      文案
     * @param listener 点击回调事件
     */
    fun addAction(str: String, listener: QMUIDialogAction.ActionListener): T {
        return addAction(0, str, listener)
    }

    /**
     * 添加普通类型的操作按钮
     *
     * @param iconResId 图标
     * @param strResId  文案
     * @param listener  点击回调事件
     */
    fun addAction(iconResId: Int, strResId: Int, listener: QMUIDialogAction.ActionListener): T {
        return addAction(iconResId, strResId, QMUIDialogAction.ACTION_PROP_NEUTRAL, listener)
    }

    /**
     * 添加普通类型的操作按钮
     *
     * @param iconResId 图标
     * @param str       文案
     * @param listener  点击回调事件
     */
    fun addAction(iconResId: Int, str: String, listener: QMUIDialogAction.ActionListener): T {
        return addAction(iconResId, str, QMUIDialogAction.ACTION_PROP_NEUTRAL, listener)
    }

    /**
     * 添加普通类型的操作按钮
     *
     * @param iconRes  图标
     * @param strRes   文案
     * @param prop     属性
     * @param listener 点击回调事件
     */
    fun addAction(iconRes: Int, strRes: Int, @QMUIDialogAction.Companion.Prop prop: Int, listener: QMUIDialogAction.ActionListener): T {
        return addAction(iconRes, mContext.resources.getString(strRes), prop, listener)
    }

    /**
     * 添加普通类型的操作按钮
     *
     * @param iconRes  图标
     * @param str      文案
     * @param prop     属性
     * @param listener 点击回调事件
     */
    fun addAction(iconRes: Int, str: String, @QMUIDialogAction.Companion.Prop prop: Int, listener: QMUIDialogAction.ActionListener): T {
        return addAction(iconRes, str, prop, QMUIDialogAction.ACTION_TYPE_NORMAL, listener)
    }

    /**
     * 添加普通类型的操作按钮
     *
     * @param iconRes  图标
     * @param strRes   文案
     * @param type     类型
     * @param prop     属性
     * @param listener 点击回调事件
     */
    protected fun addAction(iconRes: Int, strRes: Int, @QMUIDialogAction.Companion.Prop prop: Int, @QMUIDialogAction.Companion.Type type: Int, listener: QMUIDialogAction.ActionListener): T {
        return addAction(iconRes, mContext.resources.getString(strRes), prop, type, listener)
    }

    /**
     * 添加普通类型的操作按钮
     *
     * @param iconRes  图标
     * @param str      文案
     * @param type     类型
     * @param prop     属性
     * @param listener 点击回调事件
     */
    protected fun addAction(iconRes: Int, str: String, @QMUIDialogAction.Companion.Prop prop: Int, @QMUIDialogAction.Companion.Type type: Int, listener: QMUIDialogAction.ActionListener): T {
        mActions.add(QMUIDialogAction(mContext, iconRes, str, type, prop, listener))
        return this as T
    }

    fun setLeftAction(str: String, listener: QMUIDialogAction.ActionListener): QMUIDialogAction {
        return setLeftAction(0, str, listener)
    }

    fun setLeftAction(iconRes: Int, str: String, listener: QMUIDialogAction.ActionListener): QMUIDialogAction {
        return setLeftAction(iconRes, str, QMUIDialogAction.ACTION_PROP_NEUTRAL, listener)
    }


    fun setLeftAction(iconRes: Int, str: String, @QMUIDialogAction.Companion.Prop prop: Int, listener: QMUIDialogAction.ActionListener): QMUIDialogAction {
        return QMUIDialogAction(mContext, iconRes, str, QMUIDialogAction.ACTION_TYPE_NORMAL, prop, listener).apply { mLeftAction = this }
    }

    //endregion

    /**
     * 判断对话框是否需要显示title
     *
     * @return 是否有title
     */
    protected fun hasTitle(): Boolean {
        return !TextUtils.isEmpty(mTitle)
    }

    /**
     * 产生一个 Dialog 并显示出来
     */
    fun show(): QMUIDialog {
        return create().apply { show() }
    }

    /**
     * 只产生一个 Dialog, 不显示出来
     *
     * @see .create
     */
    fun create(): QMUIDialog {
        return create(R.style.QMUI_Dialog)
    }

    /**
     * 产生一个Dialog，但不显示出来。
     *
     * @param style Dialog 的样式
     * @see .create
     */
    @SuppressLint("InflateParams")
    fun create(@StyleRes style: Int): QMUIDialog {
        return QMUIDialog(mContext, style).apply {
            mRootView = mInflater.inflate(R.layout.qmui_dialog_layout, null) as LinearLayout
            mDialogWrapper = mRootView.dialog as LinearLayout
            anchorTopView = mRootView.anchor_top
            anchorBottomView = mRootView.anchor_bottom
            // title
            onCreateTitle(this, mDialogWrapper)
            //content
            onCreateContent(this, mDialogWrapper)
            // 操作
            onCreateHandlerBar(this, mDialogWrapper)

            addContentView(mRootView, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
            onAfter(this, mRootView)
            mDialog = this
        }
    }

    /**
     * 创建顶部的标题区域
     */
    protected fun onCreateTitle(dialog: QMUIDialog, parent: ViewGroup) {
        if (hasTitle()) {
            titleView = TextView(mContext).apply {
                setSingleLine(true)
                ellipsize = TextUtils.TruncateAt.END
                text = mTitle
                setTextColor(QMUIResHelper.getAttrColor(mContext, R.attr.qmui_dialog_title_text_color))
                setTextSize(TypedValue.COMPLEX_UNIT_PX, QMUIResHelper.getAttrDimen(mContext, R.attr.qmui_dialog_title_text_size).toFloat())
                layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                setPadding(
                        QMUIResHelper.getAttrDimen(mContext, R.attr.qmui_dialog_padding_horizontal),
                        QMUIResHelper.getAttrDimen(mContext, R.attr.qmui_dialog_title_margin_top),
                        QMUIResHelper.getAttrDimen(mContext, R.attr.qmui_dialog_padding_horizontal),
                        0)
            }
            parent.addView(titleView)
        }
    }

    /**
     * 创建中间的区域
     */
    protected abstract fun onCreateContent(dialog: QMUIDialog, parent: ViewGroup)

    /**
     * 创建底部的操作栏区域
     */
    protected fun onCreateHandlerBar(dialog: QMUIDialog, parent: ViewGroup) {
        val size = mActions.size
        if (size == 0 && mLeftAction == null) return

        mActionContainer = LinearLayout(mContext).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            setPadding(
                    QMUIResHelper.getAttrDimen(mContext, R.attr.qmui_dialog_action_container_margin_horizontal),
                    0,
                    QMUIResHelper.getAttrDimen(mContext, R.attr.qmui_dialog_action_container_margin_horizontal),
                    QMUIResHelper.getAttrDimen(mContext, R.attr.qmui_dialog_action_container_margin_bottom))

            mLeftAction?.apply { addView(this.generateActionView(mContext, dialog, 0, false)) }
            addView(View(mContext).apply { layoutParams = LinearLayout.LayoutParams(0, 0).apply { this.weight = 1f } })

            for (i in 0 until size) {
                addView(mActions[i].generateActionView(mContext, dialog, i, true))
            }

            addOnLayoutChangeListener { v, pLeft, top, pRight, bottom, oldLeft, oldTop, oldRight, oldBottom ->
                val childCount = childCount
                if (childCount <= 0) return@addOnLayoutChangeListener

                getChildAt(childCount - 1).apply {
                    // 如果ActionButton的宽度过宽，则减小padding
                    if (right > pRight - pLeft) {
                        val childPaddingHor = Math.max(0, paddingLeft - QMUIDisplayHelper.dp2px(mContext, 3))
                        for (i in 0 until childCount) {
                            getChildAt(i).setPadding(childPaddingHor, 0, childPaddingHor, 0)
                        }
                    }
                }
            }
        }
        parent.addView(mActionContainer)
    }

    protected open fun onAfter(dialog: QMUIDialog, parent: LinearLayout) {
        //默认情况下，点击anchorView使得dialog消失
        val listener = View.OnClickListener { mDialog.dismiss() }
        anchorBottomView.setOnClickListener(listener)
        anchorTopView.setOnClickListener(listener)
    }
}