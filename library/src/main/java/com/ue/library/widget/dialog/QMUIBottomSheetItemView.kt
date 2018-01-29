package com.ue.library.widget.dialog

import android.content.Context
import android.support.v7.widget.AppCompatImageView
import android.util.AttributeSet
import android.view.ViewStub
import android.widget.TextView
import com.ue.library.alpha.QMUIAlphaLinearLayout
import kotlinx.android.synthetic.main.qmui_bottom_sheet_grid_item.view.*

/**
 * QMUIBottomSheet 的ItemView
 * @author zander
 * @date 2017-12-05
 */
class QMUIBottomSheetItemView : QMUIAlphaLinearLayout {
    lateinit var appCompatImageView: AppCompatImageView
        private set
    lateinit var subScript: ViewStub
        private set
    lateinit var textView: TextView
        private set

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    override fun onFinishInflate() {
        super.onFinishInflate()
        appCompatImageView = grid_item_image
        subScript = grid_item_subscript
        textView = grid_item_title
    }
}