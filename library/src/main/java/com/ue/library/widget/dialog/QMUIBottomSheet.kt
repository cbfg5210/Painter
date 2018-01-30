package com.ue.library.widget.dialog

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.annotation.IntDef
import android.support.v4.content.ContextCompat
import android.support.v7.content.res.AppCompatResources
import android.text.TextUtils
import android.util.Log
import android.util.SparseArray
import android.view.*
import android.view.animation.*
import android.widget.*
import com.ue.library.R
import com.ue.library.util.QMUIResHelper
import kotlinx.android.synthetic.main.qmui_bottom_sheet_grid.view.*
import kotlinx.android.synthetic.main.qmui_bottom_sheet_grid_item.view.*
import kotlinx.android.synthetic.main.qmui_bottom_sheet_list.view.*
import kotlinx.android.synthetic.main.qmui_bottom_sheet_list_item.view.*
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.util.*

/**
 * QMUIBottomSheet 在 [Dialog] 的基础上重新定制了 [.show] 和 [.hide] 时的动画效果, 使 [Dialog] 在界面底部升起和降下。
 *
 *
 * 提供了以下两种面板样式:
 *
 *  * 列表样式, 使用 [QMUIBottomSheet.BottomListSheetBuilder] 生成。
 *  * 宫格类型, 使用 [QMUIBottomSheet.BottomGridSheetBuilder] 生成。
 *
 *
 */
class QMUIBottomSheet(context: Context) : Dialog(context, R.style.QMUI_BottomSheet) {
    // 持有 ContentView，为了做动画
    private var mContentView: View? = null
    private var mIsAnimating = false

    private var mOnBottomSheetShowListener: OnBottomSheetShowListener? = null

    fun setOnBottomSheetShowListener(onBottomSheetShowListener: OnBottomSheetShowListener) {
        mOnBottomSheetShowListener = onBottomSheetShowListener
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window?.apply {
            decorView.setPadding(0, 0, 0, 0)
            // 在底部，宽度撑满
            attributes.height = ViewGroup.LayoutParams.WRAP_CONTENT
            attributes.gravity = Gravity.BOTTOM or Gravity.CENTER

            val metrics = context.resources.displayMetrics
            val screenWidth = metrics.widthPixels
            val screenHeight = metrics.heightPixels
            attributes.width = if (screenWidth < screenHeight) screenWidth else screenHeight
        }
        setCanceledOnTouchOutside(true)
    }

    override fun setContentView(layoutResID: Int) {
        mContentView = LayoutInflater.from(context).inflate(layoutResID, null)
        super.setContentView(mContentView)
    }

    override fun setContentView(view: View, params: ViewGroup.LayoutParams?) {
        mContentView = view
        super.setContentView(view, params)
    }

    fun getContentView(): View? {
        return mContentView
    }

    override fun setContentView(view: View) {
        mContentView = view
        super.setContentView(view)
    }

    /**
     * BottomSheet升起动画
     */
    private fun animateUp() {
        mContentView?.apply {
            val set = AnimationSet(true).apply {
                addAnimation(TranslateAnimation(
                        Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 0f,
                        Animation.RELATIVE_TO_SELF, 1f, Animation.RELATIVE_TO_SELF, 0f))
                addAnimation(AlphaAnimation(0f, 1f))
                interpolator = DecelerateInterpolator()
                duration = mAnimationDuration.toLong()
                fillAfter = true
            }
            startAnimation(set)
        }
    }

    /**
     * BottomSheet降下动画
     */
    private fun animateDown() {
        mContentView?.apply {
            val set = AnimationSet(true).apply {
                addAnimation(TranslateAnimation(
                        Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 0f,
                        Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 1f))
                addAnimation(AlphaAnimation(1f, 0f))
                interpolator = DecelerateInterpolator()
                duration = mAnimationDuration.toLong()
                fillAfter = true
                setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationStart(animation: Animation) {
                        mIsAnimating = true
                    }

                    override fun onAnimationEnd(animation: Animation) {
                        mIsAnimating = false
                        /**
                         * Bugfix： Attempting to destroy the window while drawing!
                         */
                        post {
                            // java.lang.IllegalArgumentException: View=com.android.internal.policy.PhoneWindow$DecorView{22dbf5b V.E...... R......D 0,0-1080,1083} not attached to window manager
                            // 在dismiss的时候可能已经detach了，简单try-catch一下
                            try {
                                super@QMUIBottomSheet.dismiss()
                            } catch (e: Exception) {
                                Log.e("QMUIBottomSheet", "onAnimationEnd: dismiss error\n${e.message}")
                            }
                        }
                    }

                    override fun onAnimationRepeat(animation: Animation) {
                    }
                })
            }
            startAnimation(set)
        }
    }

    override fun show() {
        super.show()
        animateUp()
        mOnBottomSheetShowListener?.onShow()
    }

    override fun dismiss() {
        if (!mIsAnimating) animateDown()
    }

    interface OnBottomSheetShowListener {
        fun onShow()
    }

    /**
     * 生成列表类型的 [QMUIBottomSheet] 对话框。
     */
    class BottomListSheetBuilder
    /**
     * @param needRightMark 是否需要在被选中的 Item 右侧显示一个勾(使用 [.setCheckedIndex] 设置选中的 Item)
     */
    //是否需要rightMark,标识当前项
    constructor(private val mContext: Context, private val mNeedRightMark: Boolean = false) {
        private lateinit var mDialog: QMUIBottomSheet
        private val mItems: MutableList<BottomSheetListItemData>
        private lateinit var mAdapter: BaseAdapter
        private val mHeaderViews: MutableList<View>
        private lateinit var mContainerView: ListView
        private var mCheckedIndex: Int = 0
        private var mTitle: String? = null
        private var mTitleTv: TextView? = null
        private var mOnSheetItemClickListener: OnSheetItemClickListener? = null
        private var mOnBottomDialogDismissListener: DialogInterface.OnDismissListener? = null

        /**
         * 注意:这里只考虑List的高度,如果有title或者headerView,不计入考虑中
         */
        protected val listMaxHeight: Int
            get() = (mContext.resources.displayMetrics.heightPixels * 0.5).toInt()

        protected val contentViewLayoutId: Int
            get() = R.layout.qmui_bottom_sheet_list

        init {
            mItems = ArrayList()
            mHeaderViews = ArrayList()
        }

        /**
         * 设置要被选中的 Item 的下标。
         *
         *
         * 注意:仅当 [.mNeedRightMark] 为 true 时才有效。
         */
        fun setCheckedIndex(checkedIndex: Int): BottomListSheetBuilder {
            mCheckedIndex = checkedIndex
            return this
        }

        /**
         * @param textAndTag Item 的文字内容，同时会把内容设置为 tag。
         */
        fun addItem(textAndTag: String): BottomListSheetBuilder {
            mItems.add(BottomSheetListItemData(textAndTag, textAndTag))
            return this
        }

        /**
         * @param image      icon Item 的 icon。
         * @param textAndTag Item 的文字内容，同时会把内容设置为 tag。
         */
        fun addItem(image: Drawable, textAndTag: String): BottomListSheetBuilder {
            mItems.add(BottomSheetListItemData(image, textAndTag, textAndTag))
            return this
        }

        /**
         * @param text Item 的文字内容。
         * @param tag  item 的 tag。
         */
        fun addItem(text: String, tag: String): BottomListSheetBuilder {
            mItems.add(BottomSheetListItemData(text, tag))
            return this
        }

        /**
         * @param imageRes Item 的图标 Resource。
         * @param text     Item 的文字内容。
         * @param tag      Item 的 tag。
         */
        fun addItem(imageRes: Int, text: String, tag: String): BottomListSheetBuilder {
            val drawable = if (imageRes != 0) ContextCompat.getDrawable(mContext, imageRes) else null
            mItems.add(BottomSheetListItemData(drawable, text, tag))
            return this
        }

        /**
         * @param imageRes    Item 的图标 Resource。
         * @param text        Item 的文字内容。
         * @param tag         Item 的 tag。
         * @param hasRedPoint 是否显示红点。
         */
        fun addItem(imageRes: Int, text: String, tag: String, hasRedPoint: Boolean): BottomListSheetBuilder {
            val drawable = if (imageRes != 0) ContextCompat.getDrawable(mContext, imageRes) else null
            mItems.add(BottomSheetListItemData(drawable, text, tag, hasRedPoint))
            return this
        }

        /**
         * @param imageRes    Item 的图标 Resource。
         * @param text        Item 的文字内容。
         * @param tag         Item 的 tag。
         * @param hasRedPoint 是否显示红点。
         * @param disabled    是否显示禁用态。
         */
        fun addItem(imageRes: Int, text: String, tag: String, hasRedPoint: Boolean, disabled: Boolean): BottomListSheetBuilder {
            val drawable = if (imageRes != 0) ContextCompat.getDrawable(mContext, imageRes) else null
            mItems.add(BottomSheetListItemData(drawable, text, tag, hasRedPoint, disabled))
            return this
        }

        fun setOnSheetItemClickListener(onSheetItemClickListener: OnSheetItemClickListener): BottomListSheetBuilder {
            mOnSheetItemClickListener = onSheetItemClickListener
            return this
        }

        fun setOnBottomDialogDismissListener(listener: DialogInterface.OnDismissListener): BottomListSheetBuilder {
            mOnBottomDialogDismissListener = listener
            return this
        }

        fun addHeaderView(view: View?): BottomListSheetBuilder {
            if (view != null) mHeaderViews.add(view)
            return this
        }

        fun setTitle(title: String): BottomListSheetBuilder {
            mTitle = title
            return this
        }

        fun setTitle(resId: Int): BottomListSheetBuilder {
            mTitle = mContext.resources.getString(resId)
            return this
        }

        fun build(): QMUIBottomSheet {
            return QMUIBottomSheet(mContext).apply {
                setContentView(buildViews(), ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
                if (mOnBottomDialogDismissListener != null) setOnDismissListener(mOnBottomDialogDismissListener)
                mDialog = this
            }
        }

        private fun buildViews(): View {
            return View.inflate(mContext, contentViewLayoutId, null).apply {
                tvProjectTitle as TextView
                mTitleTv = tvProjectTitle

                listview as ListView
                mContainerView = listview

                if (!TextUtils.isEmpty(mTitle)) {
                    tvProjectTitle.visibility = View.VISIBLE
                    tvProjectTitle.text = mTitle
                } else {
                    tvProjectTitle.visibility = View.GONE
                }
                if (mHeaderViews.size > 0) {
                    for (headerView in mHeaderViews) {
                        listview.addHeaderView(headerView)
                    }
                }
                if (needToScroll()) {
                    listview.layoutParams.height = listMaxHeight
                    mDialog.setOnBottomSheetShowListener(object : OnBottomSheetShowListener {
                        override fun onShow() {
                            listview.setSelection(mCheckedIndex)
                        }
                    })
                }
                mAdapter = ListAdapter()
                listview.adapter = mAdapter
            }
        }

        private fun needToScroll(): Boolean {
            val itemHeight = QMUIResHelper.getAttrDimen(mContext, R.attr.qmui_bottom_sheet_list_item_height)
            var totalHeight = mItems.size * itemHeight
            if (mHeaderViews.size > 0) {
                for (view in mHeaderViews) {
                    if (view.measuredHeight == 0) {
                        view.measure(0, 0)
                    }
                    totalHeight += view.measuredHeight
                }
            }
            if (mTitleTv != null && !TextUtils.isEmpty(mTitle)) {
                totalHeight += QMUIResHelper.getAttrDimen(mContext, R.attr.qmui_bottom_sheet_title_height)
            }
            return totalHeight > listMaxHeight
        }

        fun notifyDataSetChanged() {
            mAdapter.notifyDataSetChanged()
            if (needToScroll()) {
                mContainerView.layoutParams.height = listMaxHeight
                mContainerView.setSelection(mCheckedIndex)
            }
        }

        interface OnSheetItemClickListener {
            fun onClick(dialog: QMUIBottomSheet, itemView: View, position: Int, tag: String)
        }

        private class BottomSheetListItemData {
            internal var image: Drawable? = null
            internal var text: String
            internal var tag = ""
            internal var hasRedPoint = false
            internal var isDisabled = false

            constructor(text: String, tag: String) {
                this.text = text
                this.tag = tag
            }

            constructor(image: Drawable?, text: String, tag: String) {
                this.image = image
                this.text = text
                this.tag = tag
            }

            constructor(image: Drawable?, text: String, tag: String, hasRedPoint: Boolean) {
                this.image = image
                this.text = text
                this.tag = tag
                this.hasRedPoint = hasRedPoint
            }

            constructor(image: Drawable?, text: String, tag: String, hasRedPoint: Boolean, isDisabled: Boolean) {
                this.image = image
                this.text = text
                this.tag = tag
                this.hasRedPoint = hasRedPoint
                this.isDisabled = isDisabled
            }
        }

        private class ViewHolder {
            internal lateinit var imageView: ImageView
            internal lateinit var textView: TextView
            internal lateinit var markView: View
            internal lateinit var redPoint: View
        }

        private inner class ListAdapter : BaseAdapter() {

            override fun getCount(): Int {
                return mItems.size
            }

            override fun getItem(position: Int): BottomSheetListItemData {
                return mItems[position]
            }

            override fun getItemId(position: Int): Long {
                return 0
            }

            override fun getView(position: Int, mConvertView: View?, parent: ViewGroup): View {
                val convertView: View
                val data = getItem(position)
                val holder: ViewHolder

                if (mConvertView == null) {
                    convertView = LayoutInflater.from(mContext).inflate(R.layout.qmui_bottom_sheet_list_item, parent, false)
                    holder = ViewHolder().apply {
                        imageView = convertView.bottom_dialog_list_item_img
                        textView = convertView.bottom_dialog_list_item_title
                        markView = convertView.bottom_dialog_list_item_mark_view_stub
                        redPoint = convertView.bottom_dialog_list_item_point

                        convertView.tag = this
                    }
                } else {
                    convertView = mConvertView
                    holder = convertView.tag as ViewHolder
                }

                if (data.image == null) holder.imageView.visibility = View.GONE
                else {
                    holder.imageView.visibility = View.VISIBLE
                    holder.imageView.setImageDrawable(data.image)
                }

                holder.textView.text = data.text
                holder.redPoint.visibility = if (data.hasRedPoint) View.VISIBLE else View.GONE

                holder.textView.isEnabled = !data.isDisabled
                convertView.isEnabled = !data.isDisabled

                if (mNeedRightMark) holder.markView.visibility = if (mCheckedIndex == position) View.VISIBLE else View.GONE
                 else holder.markView.visibility = View.GONE

                convertView.setOnClickListener { v ->
                    if (data.hasRedPoint) {
                        data.hasRedPoint = false
                        holder.redPoint.visibility = View.GONE
                    }
                    if (mNeedRightMark) {
                        setCheckedIndex(position)
                        notifyDataSetChanged()
                    }
                    mOnSheetItemClickListener?.onClick(mDialog, v, position, data.tag)
                }
                return convertView
            }
        }

    }

    /**
     * 生成宫格类型的 [QMUIBottomSheet] 对话框。
     */
    class BottomGridSheetBuilder(private val mContext: Context) : View.OnClickListener {
        private lateinit var mDialog: QMUIBottomSheet
        private val mFirstLineViews = SparseArray<View>()
        private val mSecondLineViews = SparseArray<View>()
        private var mMiniItemWidth = -1
        private var mOnSheetItemClickListener: OnSheetItemClickListener? = null
        private var mItemTextTypeFace: Typeface? = null
        private var mBottomButton: TextView? = null
        private var mBottomButtonTypeFace: Typeface? = null
        private var mIsShowButton = true
        private var mButtonText: CharSequence? = null
        private var mButtonClickListener: View.OnClickListener? = null

        protected val contentViewLayoutId: Int
            get() = R.layout.qmui_bottom_sheet_grid

        fun addItem(imageRes: Int, textAndTag: CharSequence, @Style style: Int): BottomGridSheetBuilder {
            return addItem(imageRes, textAndTag, textAndTag, style, 0)
        }

        fun setIsShowButton(isShowButton: Boolean): BottomGridSheetBuilder {
            mIsShowButton = isShowButton
            return this
        }

        fun setButtonText(buttonText: CharSequence): BottomGridSheetBuilder {
            mButtonText = buttonText
            return this
        }

        fun setButtonClickListener(buttonClickListener: View.OnClickListener): BottomGridSheetBuilder {
            mButtonClickListener = buttonClickListener
            return this
        }

        fun setItemTextTypeFace(itemTextTypeFace: Typeface): BottomGridSheetBuilder {
            mItemTextTypeFace = itemTextTypeFace
            return this
        }

        fun setBottomButtonTypeFace(bottomButtonTypeFace: Typeface): BottomGridSheetBuilder {
            mBottomButtonTypeFace = bottomButtonTypeFace
            return this
        }

        fun addItem(imageRes: Int, text: CharSequence, tag: Any, @Style style: Int, subscriptRes: Int = 0): BottomGridSheetBuilder {
            val itemView = createItemView(AppCompatResources.getDrawable(mContext, imageRes), text, tag, subscriptRes)
            return addItem(itemView, style)
        }

        fun addItem(view: View, @Style style: Int): BottomGridSheetBuilder {
            if (style == FIRST_LINE) mFirstLineViews.append(mFirstLineViews.size(), view)
            else if (style == SECOND_LINE) mSecondLineViews.append(mSecondLineViews.size(), view)
            return this
        }

        fun createItemView(drawable: Drawable?, text: CharSequence, tag: Any, subscriptRes: Int): QMUIBottomSheetItemView {
            val itemView = LayoutInflater.from(mContext).inflate(R.layout.qmui_bottom_sheet_grid_item, null, false) as QMUIBottomSheetItemView
            itemView.grid_item_title.apply {
                if (mItemTextTypeFace != null) typeface = mItemTextTypeFace
                this.text = text
            }

            itemView.tag = tag
            itemView.setOnClickListener(this)
            itemView.grid_item_image.setImageDrawable(drawable)

            if (subscriptRes != 0) {
                val stub = (itemView.grid_item_subscript as ViewStub).inflate() as ImageView
                stub.setImageResource(subscriptRes)
            }
            return itemView
        }

        fun setItemVisibility(tag: Any, visibility: Int) {
            var size = mFirstLineViews.size()
            var foundView: View? = (0 until size)
                    .mapNotNull { mFirstLineViews.get(it) }
                    .lastOrNull { it.tag == tag }

            size = mSecondLineViews.size()
            (0 until size)
                    .mapNotNull { mSecondLineViews.get(it) }
                    .filter { it.tag == tag }
                    .forEach { foundView = it }

            foundView?.visibility = visibility
        }

        fun setOnSheetItemClickListener(onSheetItemClickListener: OnSheetItemClickListener): BottomGridSheetBuilder {
            mOnSheetItemClickListener = onSheetItemClickListener
            return this
        }

        override fun onClick(v: View) {
            mOnSheetItemClickListener?.onClick(mDialog, v)
        }

        fun build(): QMUIBottomSheet {
            return QMUIBottomSheet(mContext).apply {
                setContentView(buildViews(), ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
                mDialog = this
            }
        }

        private fun buildViews(): View {
            val baseLinearLayout = View.inflate(mContext, contentViewLayoutId, null) as LinearLayout
            val firstLine = baseLinearLayout.bottom_sheet_first_linear_layout
            val secondLine = baseLinearLayout.bottom_sheet_second_linear_layout
            mBottomButton = baseLinearLayout.bottom_sheet_button

            val maxItemCountEachLine = Math.max(mFirstLineViews.size(), mSecondLineViews.size())

            val metrics = mContext.resources.displayMetrics
            val screenWidth = metrics.widthPixels
            val screenHeight = metrics.heightPixels
            val width = if (screenWidth < screenHeight) screenWidth else screenHeight
            val itemWidth = calculateItemWidth(width, maxItemCountEachLine, firstLine.paddingLeft, firstLine.paddingRight)

            addViewsInSection(mFirstLineViews, firstLine, itemWidth)
            addViewsInSection(mSecondLineViews, secondLine, itemWidth)

            val hasFirstLine = mFirstLineViews.size() > 0
            val hasSecondLine = mSecondLineViews.size() > 0
            if (!hasFirstLine) {
                firstLine.visibility = View.GONE
            }
            if (!hasSecondLine) {
                if (hasFirstLine) firstLine.setPadding(firstLine.paddingLeft, firstLine.paddingTop, firstLine.paddingRight, 0)
                secondLine.visibility = View.GONE
            }

            // button 在用户自定义了contentView的情况下可能不存在
            mBottomButton?.apply {
                if (mIsShowButton) {
                    visibility = View.VISIBLE
                    val dimen = QMUIResHelper.getAttrDimen(mContext, R.attr.qmui_bottom_sheet_grid_padding_vertical)
                    baseLinearLayout.setPadding(0, dimen, 0, 0)
                } else {
                    visibility = View.GONE
                }
                if (mBottomButtonTypeFace != null) typeface = mBottomButtonTypeFace
                if (mButtonText != null) text = mButtonText

                if (mButtonClickListener != null) setOnClickListener(mButtonClickListener)
                else setOnClickListener { mDialog.dismiss() }
            }
            return baseLinearLayout
        }

        /**
         * 拿个数最多的一行，去决策item的平铺/拉伸策略
         *
         * @return item 宽度
         */
        private fun calculateItemWidth(width: Int, maxItemCountInEachLine: Int, paddingLeft: Int, paddingRight: Int): Int {
            if (mMiniItemWidth == -1) {
                mMiniItemWidth = QMUIResHelper.getAttrDimen(mContext, R.attr.qmui_bottom_sheet_grid_item_mini_width)
            }

            val parentSpacing = width - paddingLeft - paddingRight
            var itemWidth = mMiniItemWidth
            // 看是否需要把 Item 拉伸平分 parentSpacing
            if (maxItemCountInEachLine >= 3
                    && parentSpacing - maxItemCountInEachLine * itemWidth > 0
                    && parentSpacing - maxItemCountInEachLine * itemWidth < itemWidth) {
                val count = parentSpacing / itemWidth
                itemWidth = parentSpacing / count
            }
            // 看是否需要露出半个在屏幕边缘
            if (itemWidth * maxItemCountInEachLine > parentSpacing) {
                val count = (width - paddingLeft) / itemWidth
                itemWidth = ((width - paddingLeft) / (count + .5f)).toInt()
            }
            return itemWidth
        }

        private fun addViewsInSection(items: SparseArray<View>, parent: LinearLayout, itemWidth: Int) {

            for (i in 0 until items.size()) {
                val itemView = items.get(i)
                setItemWidth(itemView, itemWidth)
                parent.addView(itemView)
            }
        }

        private fun setItemWidth(itemView: View, itemWidth: Int) {
            val itemLp: LinearLayout.LayoutParams
            if (itemView.layoutParams != null) {
                itemLp = itemView.layoutParams as LinearLayout.LayoutParams
                itemLp.width = itemWidth
            } else {
                itemLp = LinearLayout.LayoutParams(itemWidth, ViewGroup.LayoutParams.WRAP_CONTENT)
                itemView.layoutParams = itemLp
            }
            itemLp.gravity = Gravity.TOP
        }

        interface OnSheetItemClickListener {
            fun onClick(dialog: QMUIBottomSheet, itemView: View)
        }

        @Retention(RetentionPolicy.SOURCE)
        @IntDef(FIRST_LINE.toLong(), SECOND_LINE.toLong())
        annotation class Style

        companion object {
            /**
             * item 出现在第一行
             */
            const val FIRST_LINE = 0
            /**
             * item 出现在第二行
             */
            const val SECOND_LINE = 1
        }
    }

    companion object {
        // 动画时长
        private const val mAnimationDuration = 200
    }
}