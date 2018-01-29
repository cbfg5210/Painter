package com.ue.library.widget.dialog


import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.support.annotation.IntDef
import android.support.v4.content.ContextCompat
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import com.ue.library.R
import com.ue.library.util.QMUIResHelper
import com.ue.library.util.QMUIViewHelper
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy

/**
 *
 * @author cginechen
 * @date 2015-10-20
 */
class QMUIDialogAction {
    private var mContext: Context
    private var mIconRes: Int = 0
    private var mStr: String? = null
    private var mActionType: Int = 0
    var actionProp: Int = 0
        private set
    private var mOnClickListener: ActionListener? = null
    //FIXME 这个button是在create之后才生成，存在null指针的问题
    var button: Button? = null
        private set

    //region 构造器
    /**
     * 正常类型无图标Action
     * @param context context
     * @param strRes 文案
     * @param onClickListener 点击事件
     */
    constructor(context: Context, strRes: Int, onClickListener: ActionListener) : this(context, 0, strRes, ACTION_TYPE_NORMAL, onClickListener)

    constructor(context: Context, str: String, onClickListener: ActionListener) : this(context, 0, str, ACTION_TYPE_NORMAL, onClickListener) {}

    /**
     * 无图标Action
     * @param context context
     * @param iconRes 图标
     * @param strRes 文案
     * @param onClickListener 点击事件
     */
    constructor(context: Context, iconRes: Int, strRes: Int, onClickListener: ActionListener) : this(context, iconRes, strRes, ACTION_TYPE_NORMAL, onClickListener) {}

    constructor(context: Context, iconRes: Int, str: String, onClickListener: ActionListener) : this(context, iconRes, str, ACTION_TYPE_NORMAL, onClickListener) {}

    /**
     * 无图标Action
     * @param context context
     * @param iconRes 图标
     * @param strRes 文案
     * @param actionType 类型
     * @param onClickListener 点击事件
     */
    constructor(context: Context, iconRes: Int, strRes: Int, @Type actionType: Int, onClickListener: ActionListener) : this(context, iconRes, strRes, actionType, ACTION_PROP_NEUTRAL, onClickListener) {}

    constructor(context: Context, iconRes: Int, str: String, @Type actionType: Int, onClickListener: ActionListener) : this(context, iconRes, str, actionType, ACTION_PROP_NEUTRAL, onClickListener) {}


    /**
     * @param context context
     * @param iconRes 图标
     * @param strRes 文案
     * @param actionType 类型
     * @param actionProp 属性
     * @param onClickListener 点击事件
     */
    constructor(context: Context, iconRes: Int, strRes: Int, @Type actionType: Int, @Prop actionProp: Int, onClickListener: ActionListener) {
        mContext = context
        mIconRes = iconRes
        mStr = mContext.resources.getString(strRes)
        mActionType = actionType
        this.actionProp = actionProp
        mOnClickListener = onClickListener
    }

    constructor(context: Context, iconRes: Int, str: String, @Type actionType: Int, @Prop actionProp: Int, onClickListener: ActionListener) {
        mContext = context
        mIconRes = iconRes
        mStr = str
        mActionType = actionType
        this.actionProp = actionProp
        mOnClickListener = onClickListener
    }
    //endregion


    fun setOnClickListener(onClickListener: ActionListener) {
        mOnClickListener = onClickListener
    }

    fun generateActionView(context: Context, dialog: QMUIDialog, index: Int, hasLeftMargin: Boolean): View {
        if (mActionType == ACTION_TYPE_BLOCK) {
            val actionView = BlockActionView(context, mStr, mIconRes)
            button = actionView.button.apply {
                setTextColor(QMUIResHelper.getAttrColorStateList(mContext,
                        if (actionProp == ACTION_PROP_NEGATIVE) R.attr.qmui_dialog_action_text_negative_color
                        else R.attr.qmui_dialog_action_text_color))
                actionView.setOnClickListener { if (isEnabled) mOnClickListener?.onClick(dialog, index) }
            }
            return actionView
        }

        return QMUIDialogAction.generateSpanActionButton(context, mStr, mIconRes, hasLeftMargin).apply {
            button = this
            setTextColor(QMUIResHelper.getAttrColorStateList(mContext,
                    if (actionProp == ACTION_PROP_NEGATIVE) R.attr.qmui_dialog_action_text_negative_color
                    else R.attr.qmui_dialog_action_text_color))
            setOnClickListener { if (isEnabled) mOnClickListener?.onClick(dialog, index) }
        }
    }

    @SuppressLint("ViewConstructor")
    class BlockActionView(context: Context, text: String?, iconRes: Int) : FrameLayout(context) {
        lateinit var button: Button
            private set

        init {
            init(text, iconRes)
        }

        private fun init(text: String?, iconRes: Int) {
            layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    QMUIResHelper.getAttrDimen(context, R.attr.qmui_dialog_action_block_btn_height))

            QMUIViewHelper.setBackgroundKeepingPadding(this, QMUIResHelper.getAttrDrawable(context, R.attr.qmui_dialog_action_block_btn_bg)!!)
            setPadding(
                    QMUIResHelper.getAttrDimen(context, R.attr.qmui_dialog_padding_horizontal), 0,
                    QMUIResHelper.getAttrDimen(context, R.attr.qmui_dialog_padding_horizontal), 0)

            button = Button(context)
            button.setBackgroundResource(0)
            button.setPadding(0, 0, 0, 0)
            val lp = FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT)
            lp.gravity = Gravity.RIGHT
            button.layoutParams = lp
            button.gravity = Gravity.RIGHT or Gravity.CENTER_VERTICAL
            button.text = text
            if (iconRes != 0) {
                val drawable = ContextCompat.getDrawable(context, iconRes)
                if (drawable != null) {
                    drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
                    button.setCompoundDrawables(drawable, null, null, null)
                    button.compoundDrawablePadding = QMUIResHelper.getAttrDimen(context, R.attr.qmui_dialog_action_drawable_padding)
                }
            }
            button.minHeight = 0
            button.minWidth = 0
            button.minimumWidth = 0
            button.minimumHeight = 0
            button.isClickable = false
            button.isDuplicateParentStateEnabled = true
            button.setTextSize(TypedValue.COMPLEX_UNIT_PX, QMUIResHelper.getAttrDimen(context, R.attr.qmui_dialog_action_button_text_size).toFloat())
            button.setTextColor(QMUIResHelper.getAttrColorStateList(context, R.attr.qmui_dialog_action_text_color))

            addView(button)
        }
    }

    interface ActionListener {
        fun onClick(dialog: QMUIDialog, index: Int)
    }

    companion object {
        //类型
        const val ACTION_TYPE_NORMAL = 0
        const val ACTION_TYPE_BLOCK = 1
        //用于标记positive/negative/neutral
        const val ACTION_PROP_POSITIVE = 0
        const val ACTION_PROP_NEUTRAL = 1
        const val ACTION_PROP_NEGATIVE = 2

        @IntDef(ACTION_TYPE_NORMAL.toLong(), ACTION_TYPE_BLOCK.toLong())
        @Retention(RetentionPolicy.SOURCE)
        annotation class Type

        @IntDef(ACTION_PROP_NEGATIVE.toLong(), ACTION_PROP_NEUTRAL.toLong(), ACTION_PROP_POSITIVE.toLong())
        @Retention(RetentionPolicy.SOURCE)
        annotation class Prop

        /**
         * 生成适用于对话框的按钮
         */
        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
        fun generateSpanActionButton(context: Context, text: String?, iconRes: Int, hasLeftMargin: Boolean): Button {
            val button = Button(context)
            val lp = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    QMUIResHelper.getAttrDimen(context, R.attr.qmui_dialog_action_button_height))
            if (hasLeftMargin) {
                lp.leftMargin = QMUIResHelper.getAttrDimen(context, R.attr.qmui_dialog_action_button_margin_left)
            }
            button.layoutParams = lp
            button.minHeight = QMUIResHelper.getAttrDimen(context, R.attr.qmui_dialog_action_button_height)
            button.minWidth = QMUIResHelper.getAttrDimen(context, R.attr.qmui_dialog_action_button_min_width)
            button.minimumWidth = QMUIResHelper.getAttrDimen(context, R.attr.qmui_dialog_action_button_min_width)
            button.minimumHeight = QMUIResHelper.getAttrDimen(context, R.attr.qmui_dialog_action_button_height)
            button.text = text
            if (iconRes != 0) {
                val drawable = ContextCompat.getDrawable(context, iconRes)
                if (drawable != null) {
                    drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
                    button.setCompoundDrawables(drawable, null, null, null)
                    button.compoundDrawablePadding = QMUIResHelper.getAttrDimen(context, R.attr.qmui_dialog_action_drawable_padding)
                }
            }
            button.gravity = Gravity.CENTER
            button.isClickable = true
            button.setTextSize(TypedValue.COMPLEX_UNIT_PX, QMUIResHelper.getAttrDimen(context, R.attr.qmui_dialog_action_button_text_size).toFloat())
            button.setTextColor(QMUIResHelper.getAttrColorStateList(context, R.attr.qmui_dialog_action_text_color))
            button.background = QMUIResHelper.getAttrDrawable(context, R.attr.qmui_dialog_action_btn_bg)
            val paddingHor = QMUIResHelper.getAttrDimen(context, R.attr.qmui_dialog_action_button_padding_horizontal)
            button.setPadding(paddingHor, 0, paddingHor, 0)
            return button
        }
    }
}