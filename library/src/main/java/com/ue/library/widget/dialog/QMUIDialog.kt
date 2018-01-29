package com.ue.library.widget.dialog


import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.Rect
import android.os.Bundle
import android.support.annotation.LayoutRes
import android.text.InputType
import android.text.TextUtils
import android.text.method.TransformationMethod
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*
import com.ue.library.R
import com.ue.library.util.QMUIDisplayHelper
import com.ue.library.util.QMUIResHelper
import com.ue.library.util.QMUIViewHelper
import com.ue.library.widget.QMUIWrapContentScrollView
import com.ue.library.widget.textview.QMUISpanTouchFixTextView
import java.util.*

/**
 * QMUIDialog 对话框一般由 [QMUIDialogBuilder] 及其子类创建, 不同的 Builder 可以创建不同类型的对话框,
 * 例如消息类型的对话框、菜单项对话框等等。
 *
 * @author cginechen
 * @date 2015-10-20
 * @see QMUIDialogBuilder
 */
class QMUIDialog constructor(context: Context, styleRes: Int = R.style.QMUI_Dialog) : Dialog(context, styleRes) {

    init {
        init()
    }

    private fun init() {
        setCancelable(true)
        setCanceledOnTouchOutside(true)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initDialogWidth()
    }

    private fun initDialogWidth() {
        window?.apply { attributes.width = ViewGroup.LayoutParams.MATCH_PARENT }
    }

    /**
     * 消息类型的对话框 Builder。通过它可以生成一个带标题、文本消息、按钮的对话框。
     */
    class MessageDialogBuilder(context: Context) : QMUIDialogBuilder<MessageDialogBuilder>(context) {
        protected var mMessage: CharSequence? = null
        private val mScrollContainer: QMUIWrapContentScrollView
        private val textView = QMUISpanTouchFixTextView(mContext)

        init {
            textView.setTextColor(QMUIResHelper.getAttrColor(mContext, R.attr.qmui_config_color_gray_4))
            textView.setLineSpacing(QMUIDisplayHelper.dpToPx(mContext, 2).toFloat(), 1.0f)
            textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, QMUIResHelper.getAttrDimen(mContext, R.attr.qmui_dialog_content_message_text_size).toFloat())

            mScrollContainer = QMUIWrapContentScrollView(mContext)
            mScrollContainer.addView(textView)
        }

        /**
         * 设置对话框的消息文本
         */
        fun setMessage(message: CharSequence): MessageDialogBuilder {
            this.mMessage = message
            return this
        }

        /**
         * 设置对话框的消息文本
         */
        fun setMessage(resId: Int): MessageDialogBuilder {
            return setMessage(mContext.resources.getString(resId))
        }

        override fun onCreateContent(dialog: QMUIDialog, parent: ViewGroup) {
            if (!TextUtils.isEmpty(mMessage)) {
                mScrollContainer.setMaxHeight(getContentAreaMaxHeight())
                textView.text = mMessage
                textView.setPadding(
                        QMUIResHelper.getAttrDimen(mContext, R.attr.qmui_dialog_padding_horizontal),
                        QMUIResHelper.getAttrDimen(mContext, if (hasTitle()) R.attr.qmui_dialog_content_padding_top else R.attr.qmui_dialog_content_padding_top_when_no_title),
                        QMUIResHelper.getAttrDimen(mContext, R.attr.qmui_dialog_padding_horizontal),
                        QMUIResHelper.getAttrDimen(mContext, R.attr.qmui_dialog_content_padding_bottom)
                )
                parent.addView(mScrollContainer)
            }
        }
    }

    /**
     * 带 CheckBox 的消息确认框 Builder
     */
    class CheckBoxMessageDialogBuilder(context: Context) : QMUIDialogBuilder<CheckBoxMessageDialogBuilder>(context) {
        private val mScrollContainer = QMUIWrapContentScrollView(mContext)
        private val textView = QMUISpanTouchFixTextView(mContext).apply {
            setTextColor(QMUIResHelper.getAttrColor(mContext, R.attr.qmui_config_color_gray_4))
            setLineSpacing(QMUIDisplayHelper.dpToPx(mContext, 2).toFloat(), 1.0f)
            setTextSize(TypedValue.COMPLEX_UNIT_PX, QMUIResHelper.getAttrDimen(mContext, R.attr.qmui_dialog_content_message_text_size).toFloat())
            mScrollContainer.addView(this)
        }

        private var mMessage: String? = null
        private var mIsChecked = false
        private val mCheckMarkDrawable = QMUIResHelper.getAttrDrawable(context, R.attr.qmui_s_checkbox)

        /**
         * 设置对话框的消息文本
         */
        fun setMessage(message: String): CheckBoxMessageDialogBuilder {
            this.mMessage = message
            return this
        }

        /**
         * 设置对话框的消息文本
         */
        fun setMessage(resid: Int): CheckBoxMessageDialogBuilder {
            return setMessage(mContext.resources.getString(resid))
        }

        /**
         * CheckBox 是否处于勾选状态
         */
        fun isChecked(): Boolean {
            return mIsChecked
        }

        /**
         * 设置 CheckBox 的勾选状态
         */
        fun setChecked(checked: Boolean): CheckBoxMessageDialogBuilder {
            if (mIsChecked != checked) {
                mIsChecked = checked
                textView.isSelected = checked
            }
            return this
        }

        override fun onCreateContent(dialog: QMUIDialog, parent: ViewGroup) {
            if (!TextUtils.isEmpty(mMessage)) {
                mScrollContainer.setMaxHeight(getContentAreaMaxHeight())
                textView.text = mMessage
                textView.setPadding(
                        QMUIResHelper.getAttrDimen(mContext, R.attr.qmui_dialog_padding_horizontal),
                        QMUIResHelper.getAttrDimen(mContext, if (hasTitle()) R.attr.qmui_dialog_confirm_content_padding_top else R.attr.qmui_dialog_content_padding_top_when_no_title),
                        QMUIResHelper.getAttrDimen(mContext, R.attr.qmui_dialog_padding_horizontal),
                        QMUIResHelper.getAttrDimen(mContext, R.attr.qmui_dialog_confirm_content_padding_bottom)
                )
                mCheckMarkDrawable?.apply {
                    setBounds(0, 0, intrinsicWidth, intrinsicHeight)
                    textView.setCompoundDrawables(this, null, null, null)
                }
                textView.compoundDrawablePadding = QMUIDisplayHelper.dpToPx(mContext, 12)
                textView.setOnClickListener { setChecked(!mIsChecked) }
                textView.isSelected = mIsChecked
                parent.addView(mScrollContainer)
            }
        }

    }

    /**
     * 带输入框的对话框 Builder
     */
    class EditTextDialogBuilder(context: Context) : QMUIDialogBuilder<EditTextDialogBuilder>(context) {
        private var mPlaceholder: String? = null
        private var mTransformationMethod: TransformationMethod? = null
        private lateinit var mMainLayout: RelativeLayout
        var editText: EditText
            private set
        var rightImageView: ImageView
            private set
        private var mInputType = InputType.TYPE_CLASS_TEXT

        init {
            editText = EditText(mContext)
            editText.setHintTextColor(QMUIResHelper.getAttrColor(mContext, R.attr.qmui_config_color_gray_3))
            editText.setTextColor(QMUIResHelper.getAttrColor(mContext, R.attr.qmui_config_color_black))
            editText.setTextSize(TypedValue.COMPLEX_UNIT_PX, QMUIResHelper.getAttrDimen(mContext, R.attr.qmui_dialog_content_message_text_size).toFloat())
            editText.isFocusable = true
            editText.isFocusableInTouchMode = true
            editText.imeOptions = EditorInfo.IME_ACTION_GO
            editText.gravity = Gravity.CENTER_VERTICAL
            editText.id = R.id.qmui_dialog_edit_input

            rightImageView = ImageView(mContext)
            rightImageView.id = R.id.qmui_dialog_edit_right_icon
            rightImageView.visibility = View.GONE
        }

        /**
         * 设置输入框的 placeholder
         */
        fun setPlaceholder(placeholder: String): EditTextDialogBuilder {
            this.mPlaceholder = placeholder
            return this
        }

        /**
         * 设置输入框的 placeholder
         */
        fun setPlaceholder(resId: Int): EditTextDialogBuilder {
            return setPlaceholder(mContext.resources.getString(resId))
        }

        /**
         * 设置 EditText 的 transformationMethod
         */
        fun setTransformationMethod(transformationMethod: TransformationMethod): EditTextDialogBuilder {
            mTransformationMethod = transformationMethod
            return this
        }

        /**
         * 设置 EditText 的 inputType
         */
        fun setInputType(inputType: Int): EditTextDialogBuilder {
            mInputType = inputType
            return this
        }

        override fun onCreateContent(dialog: QMUIDialog, parent: ViewGroup) {
            mMainLayout = RelativeLayout(mContext)
            mMainLayout.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                topMargin = QMUIResHelper.getAttrDimen(mContext, if (hasTitle()) R.attr.qmui_dialog_edit_content_padding_top else R.attr.qmui_dialog_content_padding_top_when_no_title)
                leftMargin = QMUIResHelper.getAttrDimen(mContext, R.attr.qmui_dialog_padding_horizontal)
                rightMargin = QMUIResHelper.getAttrDimen(mContext, R.attr.qmui_dialog_padding_horizontal)
                bottomMargin = QMUIResHelper.getAttrDimen(mContext, R.attr.qmui_dialog_edit_content_padding_bottom)
            }
            mMainLayout.setBackgroundResource(R.drawable.qmui_edittext_bg_border_bottom)

            if (mTransformationMethod == null) editText.inputType = mInputType
            else editText.transformationMethod = mTransformationMethod

            editText.setBackgroundResource(0)
            editText.setPadding(0, 0, 0, QMUIDisplayHelper.dpToPx(mContext, 5))

            if (mPlaceholder != null) editText.hint = mPlaceholder

            mMainLayout.addView(editText, createEditTextLayoutParams())
            mMainLayout.addView(rightImageView, createRightIconLayoutParams())

            parent.addView(mMainLayout)
        }

        protected fun createEditTextLayoutParams(): RelativeLayout.LayoutParams {
            val editLp = RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            editLp.addRule(RelativeLayout.LEFT_OF, rightImageView.id)
            editLp.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE)
            return editLp
        }

        protected fun createRightIconLayoutParams(): RelativeLayout.LayoutParams {
            return RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE)
                addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE)
                leftMargin = QMUIDisplayHelper.dpToPx(mContext, 5)
            }
        }

        override fun onAfter(dialog: QMUIDialog, parent: LinearLayout) {
            super.onAfter(dialog, parent)
            dialog.setOnDismissListener { (mContext.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(editText.windowToken, 0) }
            editText.postDelayed({
                editText.requestFocus()
                (mContext.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).showSoftInput(editText, 0)
            }, 300)
        }
    }

    open class MenuBaseDialogBuilder<T : QMUIDialogBuilder<T>>(context: Context) : QMUIDialogBuilder<T>(context) {
        protected var mMenuItemViews = ArrayList<QMUIDialogMenuItemView>()
        protected lateinit var mMenuItemContainer: LinearLayout
        protected var mMenuItemLp = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                QMUIResHelper.getAttrDimen(mContext, R.attr.qmui_dialog_content_list_item_height))

        init {
            mMenuItemLp.gravity = Gravity.CENTER_VERTICAL
        }

        fun clear() {
            mMenuItemViews.clear()
        }

        fun addItem(itemView: QMUIDialogMenuItemView, listener: DialogInterface.OnClickListener?): T {
            itemView.menuIndex = mMenuItemViews.size
            itemView.setListener(object : QMUIDialogMenuItemView.MenuItemViewListener {
                override fun onClick(index: Int) {
                    onItemClick(index)
                    listener?.onClick(mDialog, index)
                }
            })
            mMenuItemViews.add(itemView)
            return this as T
        }

        protected open fun onItemClick(index: Int) {
        }

        override fun onCreateContent(dialog: QMUIDialog, parent: ViewGroup) {
            mMenuItemContainer = LinearLayout(mContext)
            val layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT)
            mMenuItemContainer.setPadding(
                    0, QMUIResHelper.getAttrDimen(mContext, R.attr.qmui_dialog_content_padding_top_when_list),
                    0, QMUIResHelper.getAttrDimen(mContext, if (mActions.size > 0) R.attr.qmui_dialog_content_padding_bottom else R.attr.qmui_dialog_content_padding_bottom_when_no_action)
            )
            mMenuItemContainer.layoutParams = layoutParams
            mMenuItemContainer.orientation = LinearLayout.VERTICAL
            if (mMenuItemViews.size == 1) {
                mMenuItemContainer.setPadding(0, 0, 0, 0)
                if (hasTitle()) {
                    QMUIViewHelper.setPaddingTop(mMenuItemContainer, QMUIResHelper.getAttrDimen(mContext, R.attr.qmui_dialog_content_padding_top_when_list))
                }
                if (mActions.size > 0) {
                    QMUIViewHelper.setPaddingBottom(mMenuItemContainer, QMUIResHelper.getAttrDimen(mContext, R.attr.qmui_dialog_content_padding_bottom))
                }
            }
            for (itemView in mMenuItemViews) {
                mMenuItemContainer.addView(itemView, mMenuItemLp)
            }
            val scrollView = object : ScrollView(mContext) {
                override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
                    var heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(getContentAreaMaxHeight(), View.MeasureSpec.AT_MOST)
                    super.onMeasure(widthMeasureSpec, heightMeasureSpec)
                }
            }
            scrollView.addView(mMenuItemContainer)
            parent.addView(scrollView)
        }
    }

    /**
     * 菜单类型的对话框 Builder
     */
    class MenuDialogBuilder(context: Context) : MenuBaseDialogBuilder<MenuDialogBuilder>(context) {

        /**
         * 添加多个菜单项
         *
         * @param items    所有菜单项的文字
         * @param listener 菜单项的点击事件
         */
        fun addItems(items: Array<CharSequence>, listener: DialogInterface.OnClickListener): MenuDialogBuilder {
            for (item in items) {
                addItem(QMUIDialogMenuItemView.TextItemView(mContext, item), listener)
            }
            return this
        }

        /**
         * 添加单个菜单项
         *
         * @param item     菜单项的文字
         * @param listener 菜单项的点击事件
         */
        fun addItem(item: CharSequence, listener: DialogInterface.OnClickListener): MenuDialogBuilder {
            addItem(QMUIDialogMenuItemView.TextItemView(mContext, item), listener)
            return this
        }

    }

    /**
     * 单选类型的对话框 Builder
     */
    class CheckableDialogBuilder(context: Context) : MenuBaseDialogBuilder<CheckableDialogBuilder>(context) {
        /**
         * 当前被选中的菜单项的下标, 负数表示没选中任何项
         */
        private var mCheckedIndex = -1

        /**
         * 获取当前选中的菜单项的下标
         *
         * @return 负数表示没选中任何项
         */
        fun getCheckedIndex(): Int {
            return mCheckedIndex
        }

        /**
         * 设置选中的菜单项的下班
         */
        fun setCheckedIndex(checkedIndex: Int): CheckableDialogBuilder {
            mCheckedIndex = checkedIndex
            return this
        }

        override fun onCreateContent(dialog: QMUIDialog, parent: ViewGroup) {
            super.onCreateContent(dialog, parent)
            mMenuItemViews[mCheckedIndex].isChecked = (mCheckedIndex > -1 && mCheckedIndex < mMenuItemViews.size)
        }

        override fun onItemClick(index: Int) {
            for (i in mMenuItemViews.indices) {
                mMenuItemViews[i].apply {
                    if (i == index) {
                        isChecked = true
                        mCheckedIndex = index
                    } else {
                        isChecked = false
                    }
                }
            }
        }

        /**
         * 添加菜单项
         *
         * @param items    所有菜单项的文字
         * @param listener 菜单项的点击事件,可以在点击事件里调用 [.setCheckedIndex] 来设置选中某些菜单项
         */
        fun addItems(items: Array<CharSequence>, listener: DialogInterface.OnClickListener): CheckableDialogBuilder {
            for (item in items) {
                addItem(QMUIDialogMenuItemView.MarkItemView(mContext, item), listener)
            }
            return this
        }
    }

    /**
     * 多选类型的对话框 Builder
     */
    class MultiCheckableDialogBuilder(context: Context) : MenuBaseDialogBuilder<MultiCheckableDialogBuilder>(context) {

        /**
         * 该 int 的每一位标识菜单的每一项是否被选中 (1为选中,0位不选中)
         */
        private var mCheckedItems: Int = 0

        /**
         * @return 被选中的菜单项的下标 **注意: 如果选中的是1，3项(以0开始)，因为 (2<<1) + (2<<3) = 20**
         */
        val checkedItemRecord: Int
            get() {
                var output = 0
                val length = mMenuItemViews.size

                for (i in 0 until length) {
                    mMenuItemViews[i].apply { if (isChecked) output += 2 shl menuIndex }
                }
                mCheckedItems = output
                return output
            }

        /**
         * @return 被选中的菜单项的下标数组。如果选中的是1，3项(以0开始)，则返回[1,3]
         */
        val checkedItemIndexes: IntArray
            get() {
                val array = ArrayList<Int>()
                val length = mMenuItemViews.size

                for (i in 0 until length) {
                    mMenuItemViews[i].apply { if (isChecked) array.add(menuIndex) }
                }
                val output = IntArray(array.size)
                for (i in array.indices) {
                    output[i] = array[i]
                }
                return output
            }

        /**
         * 设置被选中的菜单项的下标
         *
         * @param checkedItems **注意: 该 int 参数的每一位标识菜单项的每一项是否被选中**
         *
         * 如 20 表示选中下标为 1、3 的菜单项, 因为 (2<<1) + (2<<3) = 20
         */
        fun setCheckedItems(checkedItems: Int): MultiCheckableDialogBuilder {
            mCheckedItems = checkedItems
            return this
        }

        /**
         * 设置被选中的菜单项的下标
         *
         * @param checkedIndexes 被选中的菜单项的下标组成的数组,如 [1,3] 表示选中下标为 1、3 的菜单项
         */
        fun setCheckedItems(checkedIndexes: IntArray): MultiCheckableDialogBuilder {
            var checkedItemRecord = 0
            for (checkedIndexe in checkedIndexes) {
                checkedItemRecord += 2 shl checkedIndexe
            }
            return setCheckedItems(checkedItemRecord)
        }

        /**
         * 添加菜单项
         *
         * @param items    所有菜单项的文字
         * @param listener 菜单项的点击事件,可以在点击事件里调用 [.setCheckedItems]} 来设置选中某些菜单项
         */
        fun addItems(items: Array<CharSequence>, listener: DialogInterface.OnClickListener): MultiCheckableDialogBuilder {
            for (item in items) {
                addItem(QMUIDialogMenuItemView.CheckItemView(mContext, true, item), listener)
            }
            return this
        }

        override fun onCreateContent(dialog: QMUIDialog, parent: ViewGroup) {
            super.onCreateContent(dialog, parent)
            for (i in mMenuItemViews.indices) {
                mMenuItemViews[i].apply {
                    val v = 2 shl i
                    isChecked = v and mCheckedItems == v
                }
            }
        }

        override fun onItemClick(index: Int) {
            val itemView = mMenuItemViews[index]
            itemView.isChecked = !itemView.isChecked
        }

        protected fun existCheckedItem(): Boolean {
            return checkedItemRecord <= 0
        }
    }

    /**
     * 自定义对话框内容区域的 Builder
     */
    class CustomDialogBuilder(context: Context) : QMUIDialogBuilder<CustomDialogBuilder>(context) {

        private var mLayoutId: Int = 0

        /**
         * 设置内容区域的 layoutResId
         */
        fun setLayout(@LayoutRes layoutResId: Int): CustomDialogBuilder {
            mLayoutId = layoutResId
            return this
        }

        override fun onCreateContent(dialog: QMUIDialog, parent: ViewGroup) {
            parent.addView(LayoutInflater.from(mContext).inflate(mLayoutId, parent, false))
        }
    }

    /**
     * 随键盘升降自动调整 Dialog 高度的 Builder
     */
    abstract class AutoResizeDialogBuilder(context: Context) : QMUIDialogBuilder<AutoResizeDialogBuilder>(context) {

        private lateinit var mScrollerView: ScrollView

        private var mAnchorHeight = 0
        private var mScreenHeight = 0
        private var mScrollHeight = 0

        override fun onCreateContent(dialog: QMUIDialog, parent: ViewGroup) {
            mScrollerView = ScrollView(mContext).apply {
                layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, onGetScrollHeight())
                addView(onBuildContent(dialog, this))
                parent.addView(this)
            }
        }

        override fun onAfter(dialog: QMUIDialog, parent: LinearLayout) {
            super.onAfter(dialog, parent)
            bindEvent()
        }

        abstract fun onBuildContent(dialog: QMUIDialog, parent: ScrollView): View

        fun onGetScrollHeight(): Int {
            return ViewGroup.LayoutParams.WRAP_CONTENT
        }

        private fun bindEvent() {
            anchorTopView.setOnClickListener { mDialog.dismiss() }
            anchorBottomView.setOnClickListener { mDialog.dismiss() }
            mRootView.viewTreeObserver.addOnGlobalLayoutListener {
                val mDecor = mDialog.window.decorView
                val r = Rect()
                mDecor.getWindowVisibleDisplayFrame(r)
                mScreenHeight = mContext.resources.displayMetrics.heightPixels
                val anchorShouldHeight = mScreenHeight - r.bottom

                if (anchorShouldHeight != mAnchorHeight) {
                    mAnchorHeight = anchorShouldHeight
                    val lp = anchorBottomView.layoutParams as LinearLayout.LayoutParams
                    lp.height = mAnchorHeight
                    anchorBottomView.layoutParams = lp
                    val slp = mScrollerView.layoutParams as LinearLayout.LayoutParams
                    if (onGetScrollHeight() == ViewGroup.LayoutParams.WRAP_CONTENT) {
                        mScrollHeight = Math.max(mScrollHeight, mScrollerView.measuredHeight)
                    } else {
                        mScrollHeight = onGetScrollHeight()
                    }
                    if (mAnchorHeight == 0) {
                        slp.height = mScrollHeight
                    } else {
                        mScrollerView.getChildAt(0).requestFocus()
                        slp.height = mScrollHeight - mAnchorHeight
                    }
                    mScrollerView.layoutParams = slp
                }
                else {
                    //如果内容过高,anchorShouldHeight=0,但实际下半部分会被截断,因此需要保护
                    //由于高度超过后,actionContainer并不会去测量和布局,所以这里拿不到action的高度,因此用比例估算一个值
                    val lp = mDialogWrapper.layoutParams as LinearLayout.LayoutParams
                    val dialogLayoutMaxHeight = mScreenHeight - lp.bottomMargin - lp.topMargin - r.top
                    val scrollLayoutHeight = mScrollerView.measuredHeight
                    if (scrollLayoutHeight > dialogLayoutMaxHeight * 0.8) {
                        mScrollHeight = (dialogLayoutMaxHeight * 0.8).toInt()
                        val slp = mScrollerView.layoutParams as LinearLayout.LayoutParams
                        slp.height = mScrollHeight
                        mScrollerView.layoutParams = slp
                    }
                }
            }
        }
    }
}
