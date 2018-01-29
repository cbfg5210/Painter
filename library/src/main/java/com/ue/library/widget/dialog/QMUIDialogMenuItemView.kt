package com.ue.library.widget.dialog

import android.annotation.SuppressLint
import android.content.Context
import android.text.TextUtils
import android.util.TypedValue
import android.view.Gravity
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView

import com.ue.library.R
import com.ue.library.util.QMUIResHelper
import com.ue.library.util.QMUIViewHelper


/**
 * 菜单类型的对话框的item
 *
 * @author chantchen
 * @date 2016-1-20
 */

open class QMUIDialogMenuItemView(context: Context) : RelativeLayout(context) {
    var menuIndex = -1
    private var mListener: MenuItemViewListener? = null
    var isChecked = false
        set(checked) {
            field = checked
            notifyCheckChange(isChecked)
        }

    init {
        QMUIViewHelper.setBackgroundKeepingPadding(this, QMUIResHelper.getAttrDrawable(context, R.attr.qmui_dialog_content_list_item_bg)!!)
        setPadding(
                QMUIResHelper.getAttrDimen(context, R.attr.qmui_dialog_padding_horizontal), 0,
                QMUIResHelper.getAttrDimen(context, R.attr.qmui_dialog_padding_horizontal), 0)
    }

    protected open fun notifyCheckChange(isChecked: Boolean) {

    }

    fun setListener(listener: MenuItemViewListener) {
        if (!isClickable) isClickable = true
        mListener = listener
    }

    override fun performClick(): Boolean {
        mListener?.onClick(menuIndex)
        return super.performClick()
    }

    interface MenuItemViewListener {
        fun onClick(index: Int)
    }

    class TextItemView : QMUIDialogMenuItemView {
        protected lateinit var mTextView: TextView

        constructor(context: Context) : super(context) {
            init()
        }

        constructor(context: Context, text: CharSequence) : super(context) {
            init()
            setText(text)
        }

        private fun init() {
            mTextView = createItemTextView(context)
            addView(mTextView, RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT))
        }

        fun setText(text: CharSequence) {
            mTextView.text = text
        }

        fun setTextColor(color: Int) {
            mTextView.setTextColor(color)
        }
    }

    class MarkItemView(private val mContext: Context) : QMUIDialogMenuItemView(mContext) {
        private val mTextView: TextView
        private val mCheckedView: ImageView

        init {
            mCheckedView = ImageView(mContext).apply {
                setImageResource(R.drawable.qmui_s_dialog_check_mark)
                id = QMUIViewHelper.generateViewId()
            }
            val checkLp = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT).apply {
                addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE)
                addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE)
                leftMargin = QMUIResHelper.getAttrDimen(mContext, R.attr.qmui_dialog_menu_item_check_icon_margin_horizontal)
            }
            addView(mCheckedView, checkLp)

            mTextView = createItemTextView(mContext)
            val tvLp = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT).apply {
                addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE)
                addRule(RelativeLayout.LEFT_OF, mCheckedView.id)
            }
            addView(mTextView, tvLp)
        }

        constructor(context: Context, text: CharSequence) : this(context) {
            setText(text)
        }

        private fun setText(text: CharSequence) {
            mTextView.text = text
        }

        override fun notifyCheckChange(isChecked: Boolean) {
            mCheckedView.isSelected = isChecked
        }
    }

    @SuppressLint("ViewConstructor")
    class CheckItemView(private val mContext: Context, right: Boolean) : QMUIDialogMenuItemView(mContext) {
        private val mTextView = createItemTextView(mContext)
        private val mCheckedView = ImageView(mContext)

        init {
            mCheckedView.setImageDrawable(QMUIResHelper.getAttrDrawable(mContext, R.attr.qmui_s_checkbox))
            mCheckedView.id = QMUIViewHelper.generateViewId()

            val checkLp = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT).apply {
                addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE)
                if (right) {
                    addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE)
                    leftMargin = QMUIResHelper.getAttrDimen(mContext, R.attr.qmui_dialog_menu_item_check_icon_margin_horizontal)
                } else {
                    addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE)
                    rightMargin = QMUIResHelper.getAttrDimen(mContext, R.attr.qmui_dialog_menu_item_check_icon_margin_horizontal)
                }
            }
            addView(mCheckedView, checkLp)

            val tvLp = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT)
            tvLp.addRule(if (right) RelativeLayout.LEFT_OF else RelativeLayout.RIGHT_OF, mCheckedView.id)

            addView(mTextView, tvLp)
        }

        constructor(context: Context, right: Boolean, text: CharSequence) : this(context, right) {
            setText(text)
        }

        fun setText(text: CharSequence) {
            mTextView.text = text
        }

        override fun notifyCheckChange(isChecked: Boolean) {
            mCheckedView.isSelected = isChecked
        }
    }

    companion object {
        fun createItemTextView(context: Context) =
                TextView(context).apply {
                    setTextColor(QMUIResHelper.getAttrColor(context, R.attr.qmui_dialog_menu_item_text_color))
                    gravity = Gravity.CENTER_VERTICAL or Gravity.LEFT
                    setTextSize(TypedValue.COMPLEX_UNIT_PX, QMUIResHelper.getAttrDimen(context, R.attr.qmui_dialog_content_list_item_text_size).toFloat())
                    setSingleLine(true)
                    ellipsize = TextUtils.TruncateAt.MIDDLE
                    isDuplicateParentStateEnabled = false
                }
    }
}