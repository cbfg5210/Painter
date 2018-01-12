package com.ue.autodraw

import android.content.Context
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import com.ue.library.util.SPUtils
import kotlinx.android.synthetic.main.view_number_selector.view.*

/**
 * Created by hawk on 2018/1/11.
 */
class NumberSelectorView : LinearLayout, View.OnClickListener {
    private var numberListener: OnNumberChangeListener? = null
    private var stepLen = 0
    private var spKey: String? = null
    private var numberValue = 0

    constructor(context: Context?) : this(context, null)
    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        if (attrs != null) {
            val ta = resources.obtainAttributes(attrs, R.styleable.NumberSelectorView)
            stepLen = ta.getInt(R.styleable.NumberSelectorView_stepLen, 0)
            spKey = ta.getString(R.styleable.NumberSelectorView_spKey)
            ta.recycle()
        }
        if (!TextUtils.isEmpty(spKey)) {
            numberValue = SPUtils.getInt(spKey!!, 0)
        }
        View.inflate(context, R.layout.view_number_selector, this)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        if (!TextUtils.isEmpty(spKey)) {
            SPUtils.putInt(spKey!!, numberValue)
        }
    }

    fun setNumberChangeListener(numberListener: OnNumberChangeListener) {
        this.numberListener = numberListener
        numberListener.onNumberChanged(this, numberValue)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        ivSub.setOnClickListener(this)
        ivPlus.setOnClickListener(this)

        etNumber.setText(numberValue.toString())
        etNumber.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                numberValue = if (TextUtils.isEmpty(s)) 0 else s.toString().toInt()
                numberListener?.onNumberChanged(this@NumberSelectorView, numberValue)
            }
        })
    }

    override fun onClick(v: View) {
        etNumber.setText((if (v.id == R.id.ivPlus) numberValue + stepLen else numberValue - stepLen).toString())
    }

    interface OnNumberChangeListener {
        fun onNumberChanged(view: View, number: Int)
    }
}