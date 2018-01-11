package com.ue.autodraw

import android.content.Context
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import kotlinx.android.synthetic.main.view_number_selector.view.*

/**
 * Created by hawk on 2018/1/11.
 */
class NumberSelectorView : LinearLayout, View.OnClickListener {
    private var numberListener: OnNumberChangeListener? = null

    constructor(context: Context?) : this(context, null)
    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    fun setNumberChangeListener(numberListener: OnNumberChangeListener? = null) {
        this.numberListener = numberListener
    }

    init {
        View.inflate(context, R.layout.view_number_selector, this)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        ivSub.setOnClickListener(this)
        ivPlus.setOnClickListener(this)

        etNumber.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                numberListener?.onNumberChanged(this@NumberSelectorView, if (TextUtils.isEmpty(etNumber.text)) 0 else etNumber.text.toString().toInt())
            }
        })
    }

    override fun onClick(v: View) {
        val number = if (TextUtils.isEmpty(etNumber.text)) 0 else etNumber.text.toString().toInt()
        if (v.id == R.id.ivPlus) {
            etNumber.setText((number + 1).toString())
            return
        }
        if (number > 0) {
            etNumber.setText((number - 1).toString())
        }
    }

    interface OnNumberChangeListener {
        fun onNumberChanged(view: View, number: Int)
    }
}