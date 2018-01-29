package com.ue.library.widget.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.support.annotation.IntDef
import android.support.annotation.LayoutRes
import android.support.v4.content.ContextCompat
import android.text.TextUtils
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.ue.library.R
import com.ue.library.util.QMUIDisplayHelper
import com.ue.library.widget.QMUILoadingView
import com.ue.library.widget.dialog.QMUITipDialog.Builder.IconType
import kotlinx.android.synthetic.main.qmui_tip_dialog_layout.*
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy

/**
 * 提供一个浮层展示在屏幕中间, 一般使用 [QMUITipDialog.Builder] 或 [QMUITipDialog.CustomBuilder] 生成。
 *
 *  * [QMUITipDialog.Builder] 提供了一个图标和一行文字的样式, 其中图标有几种类型可选, 见 [QMUITipDialog.Builder.IconType]
 *  * [QMUITipDialog.CustomBuilder] 支持传入自定义的 layoutResId, 达到自定义 TipDialog 的效果。
 *
 *
 * @author cginechen
 * @date 2016-10-14
 */

class QMUITipDialog constructor(context: Context, themeResId: Int = R.style.QMUI_TipDialog) : Dialog(context, themeResId) {

    init {
        setCanceledOnTouchOutside(false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initDialogWidth()
    }

    private fun initDialogWidth() {
        window?.apply { attributes.width = ViewGroup.LayoutParams.MATCH_PARENT }
    }

    /**
     * 生成默认的 [QMUITipDialog]
     *
     *
     * 提供了一个图标和一行文字的样式, 其中图标有几种类型可选。见 [IconType]
     *
     *
     * @see CustomBuilder
     */
    class Builder(private val mContext: Context) {

        @IconType
        private var mCurrentIconType = ICON_TYPE_NOTHING

        private var mTipWord: CharSequence? = null

        @IntDef(ICON_TYPE_NOTHING.toLong(), ICON_TYPE_LOADING.toLong(), ICON_TYPE_SUCCESS.toLong(), ICON_TYPE_FAIL.toLong(), ICON_TYPE_INFO.toLong())
        @Retention(RetentionPolicy.SOURCE)
        annotation class IconType

        /**
         * 设置 icon 显示的内容
         *
         * @see IconType
         */
        fun setIconType(@IconType iconType: Int): Builder {
            mCurrentIconType = iconType
            return this
        }

        /**
         * 设置显示的文案
         */
        fun setTipWord(tipWord: CharSequence): Builder {
            mTipWord = tipWord
            return this
        }

        fun create(): QMUITipDialog {
            return create(true)
        }

        /**
         * 创建 Dialog, 但没有弹出来, 如果要弹出来, 请调用返回值的 [Dialog.show] 方法
         *
         * @param cancelable 按系统返回键是否可以取消
         * @return 创建的 Dialog
         */

        fun create(cancelable: Boolean = true): QMUITipDialog {
            val dialog = QMUITipDialog(mContext).apply {
                setCancelable(cancelable)
                setContentView(R.layout.qmui_tip_dialog_layout)
            }

            if (mCurrentIconType == ICON_TYPE_LOADING) {
                val loadingView = QMUILoadingView(mContext).apply {
                    setColor(Color.WHITE)
                    setSize(QMUIDisplayHelper.dp2px(mContext, 32))
                    layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                }
                dialog.contentWrap.addView(loadingView)

            } else if (mCurrentIconType == ICON_TYPE_SUCCESS || mCurrentIconType == ICON_TYPE_FAIL || mCurrentIconType == ICON_TYPE_INFO) {
                val imageView = ImageView(mContext).apply {
                    layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                    if (mCurrentIconType == ICON_TYPE_SUCCESS) {
                        setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.qmui_icon_notify_done))
                    } else if (mCurrentIconType == ICON_TYPE_FAIL) {
                        setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.qmui_icon_notify_error))
                    } else {
                        setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.qmui_icon_notify_info))
                    }
                }
                dialog.contentWrap.addView(imageView)
            }

            if (!TextUtils.isEmpty(mTipWord)) {
                val tipView = TextView(mContext).apply {
                    val tipViewLP = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                    if (mCurrentIconType != ICON_TYPE_NOTHING) {
                        tipViewLP.topMargin = QMUIDisplayHelper.dp2px(mContext, 12)
                    }
                    layoutParams = tipViewLP

                    ellipsize = TextUtils.TruncateAt.END
                    gravity = Gravity.CENTER
                    maxLines = 2
                    setTextColor(ContextCompat.getColor(mContext, R.color.qmui_config_color_white))
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
                    text = mTipWord
                }
                dialog.contentWrap.addView(tipView)
            }
            return dialog
        }

        companion object {
            /**
             * 不显示任何icon
             */
            const val ICON_TYPE_NOTHING = 0
            /**
             * 显示 Loading 图标
             */
            const val ICON_TYPE_LOADING = 1
            /**
             * 显示成功图标
             */
            const val ICON_TYPE_SUCCESS = 2
            /**
             * 显示失败图标
             */
            const val ICON_TYPE_FAIL = 3
            /**
             * 显示信息图标
             */
            const val ICON_TYPE_INFO = 4
        }
    }

    /**
     * 传入自定义的布局并使用这个布局生成 TipDialog
     */
    class CustomBuilder(private val mContext: Context) {
        private var mContentLayoutId: Int = 0

        fun setContent(@LayoutRes layoutId: Int): CustomBuilder {
            mContentLayoutId = layoutId
            return this
        }

        /**
         * 创建 Dialog, 但没有弹出来, 如果要弹出来, 请调用返回值的 [Dialog.show] 方法
         *
         * @return 创建的 Dialog
         */
        fun create(): QMUITipDialog {
            val dialog = QMUITipDialog(mContext).apply { setContentView(R.layout.qmui_tip_dialog_layout) }
            LayoutInflater.from(mContext).inflate(mContentLayoutId, dialog.contentWrap, true)
            return dialog
        }
    }
}