package com.ue.graffiti.ui

import android.content.DialogInterface
import android.graphics.*
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.widget.GridLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import com.ue.adapterdelegate.Item
import com.ue.adapterdelegate.OnDelegateClickListener
import com.ue.graffiti.R
import com.ue.graffiti.constant.SPKeys
import com.ue.graffiti.model.PenCatTitleItem
import com.ue.graffiti.model.PenShapeItem
import com.ue.graffiti.util.PenUtils
import com.ue.graffiti.util.ResourceUtils
import com.ue.graffiti.widget.PenEffectView
import com.ue.library.util.SPUtils
import kotlinx.android.synthetic.main.dialog_pen.view.*
import java.util.*

//调色板对话框
class PenDialog : DialogFragment(), OnSeekBarChangeListener {
    //获取当前绘制画笔
    private lateinit var paint: Paint
    // 调整笔触相关控件
    private lateinit var pevPenEffect: PenEffectView
    private lateinit var sbPenStroke: SeekBar
    private lateinit var tvPenStroke: TextView
    // 线形按钮
    private lateinit var matrix: Matrix

    private var paintStrokeSize = 0
    private var lastShapeIndex = 0
    private var lastEffectIndex = 0
    private var lastShapeImage = 0
    private var lastEffectImage = 0

    private lateinit var gAdapter: PenStyleAdapter

    private val penShapeListener = object : OnDelegateClickListener {
        override fun onClick(view: View, i: Int) {
            val item = gAdapter.items[i] as PenShapeItem
            if (item.flag == FLAG_SHAPE) {
                lastShapeImage = item.image
                lastShapeIndex = item.index
                paint.pathEffect = PenUtils.getPaintShapeByImage(lastShapeImage, sbPenStroke.progress, matrix)
            } else {
                lastEffectImage = item.image
                lastEffectIndex = item.index
                paint.maskFilter = PenUtils.getPaintEffectByImage(lastEffectImage)
            }
            // 刷新特效区域
            pevPenEffect.invalidate()
        }
    }

    private val penList: MutableList<Item>
        get() {
            val items = ArrayList<Item>()
            var images = ResourceUtils.getImageArray(context, R.array.penShapeImages)
            var names = resources.getStringArray(R.array.penShapeNames)

            lastShapeIndex = SPUtils.getInt(SPKeys.SP_PAINT_SHAPE_INDEX, 0)
            lastEffectIndex = SPUtils.getInt(SPKeys.SP_PAINT_EFFECT_INDEX, 0)

            items.add(PenCatTitleItem(getString(R.string.line_shape)))
            images.indices.mapTo(items) {
                PenShapeItem(FLAG_SHAPE, images[it], names[it])
                        .apply {
                            index = it
                            isChecked = lastShapeIndex == it
                        }
            }

            items.add(PenCatTitleItem(getString(R.string.special_effect)))
            images = ResourceUtils.getImageArray(context, R.array.penEffectImages)
            names = resources.getStringArray(R.array.penEffectNames)

            images.indices.mapTo(items) {
                PenShapeItem(FLAG_EFFECT, images[it], names[it])
                        .apply {
                            index = it
                            isChecked = lastEffectIndex == it
                        }
            }

            return items
        }

    fun setCurrentPaint(currentPaint: Paint) {
        paint = currentPaint
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val contentView = inflater.inflate(R.layout.dialog_pen, null)

        pevPenEffect = contentView.pevPenEffect
        pevPenEffect.setPaint(paint)

        sbPenStroke = contentView.sbPenStroke
        tvPenStroke = contentView.tvPenStroke

        matrix = Matrix()
        matrix.setSkew(2f, 2f)
        // 设置监听
        sbPenStroke.setOnSeekBarChangeListener(this)

        contentView.btnPickPen.setOnClickListener { dismiss() }

        // 以当前画笔风格初始化特效区域
        paintStrokeSize = paint.strokeWidth.toInt()
        sbPenStroke.progress = paintStrokeSize

        tvPenStroke.text = Integer.toString(paintStrokeSize)
        pevPenEffect.invalidate()
        /*
        * init effect list
        * */
        gAdapter = PenStyleAdapter(activity, penList)
        gAdapter.setDelegateClickListener(penShapeListener)

        contentView.rvPenStyles.apply {
            setHasFixedSize(true)
            adapter = gAdapter

            layoutManager = GridLayoutManager(context, 4).apply {
                spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                    override fun getSpanSize(position: Int): Int {
                        return gAdapter.getSpanCount(position)
                    }
                }
            }
        }

        return contentView
    }

    // 拖动的时候时刻更新粗细文本
    override fun onProgressChanged(seekBar: SeekBar, curWidth: Int, fromUser: Boolean) {
        // 移到0的时候自动转换成1
        paintStrokeSize = curWidth
        if (paintStrokeSize == 0) {
            seekBar.progress = 1
            paintStrokeSize = 1
        }
        //更新粗细文本
        tvPenStroke.text = Integer.toString(paintStrokeSize)
        //对于PathDashPathEffect特效要特殊处理
        when (lastShapeImage) {
            R.drawable.ic_line_oval -> {
                //椭圆
                val p = Path()
                p.addOval(RectF(0f, 0f, paintStrokeSize.toFloat(), paintStrokeSize.toFloat()), Path.Direction.CCW)
                paint.pathEffect = PathDashPathEffect(p, (paintStrokeSize + 10).toFloat(), 0f, PathDashPathEffect.Style.ROTATE)
            }
            R.drawable.ic_line_rect -> {
                //正方形
                val p = Path()
                p.addRect(RectF(0f, 0f, paintStrokeSize.toFloat(), paintStrokeSize.toFloat()), Path.Direction.CCW)
                paint.pathEffect = PathDashPathEffect(p, (paintStrokeSize + 10).toFloat(), 0f, PathDashPathEffect.Style.ROTATE)
            }
            R.drawable.ic_line_brush -> {
                //毛笔
                val p = Path()
                p.addRect(RectF(0f, 0f, paintStrokeSize.toFloat(), paintStrokeSize.toFloat()), Path.Direction.CCW)
                p.transform(matrix)
                paint.pathEffect = PathDashPathEffect(p, 2f, 0f, PathDashPathEffect.Style.TRANSLATE)
            }
            R.drawable.ic_line_mark_pen -> {
                //马克笔
                val p = Path()
                p.addArc(RectF(0f, 0f, (paintStrokeSize + 4).toFloat(), (paintStrokeSize + 4).toFloat()), -90f, 90f)
                p.addArc(RectF(0f, 0f, (paintStrokeSize + 4).toFloat(), (paintStrokeSize + 4).toFloat()), 90f, -90f)
                paint.pathEffect = PathDashPathEffect(p, 2f, 0f, PathDashPathEffect.Style.TRANSLATE)
            }
        }
        //改变粗细
        paint.strokeWidth = paintStrokeSize.toFloat()
        // 更新示意view
        pevPenEffect.invalidate()
    }

    override fun onStartTrackingTouch(seekBar: SeekBar) {}

    // 放开拖动条后 重绘特效示意区域
    override fun onStopTrackingTouch(seekBar: SeekBar) {}

    override fun onDismiss(dialog: DialogInterface?) {
        super.onDismiss(dialog)
        SPUtils.putInt(SPKeys.SP_PAINT_SIZE, paintStrokeSize)
        SPUtils.putInt(SPKeys.SP_PAINT_SHAPE_INDEX, lastShapeIndex)
        SPUtils.putInt(SPKeys.SP_PAINT_EFFECT_INDEX, lastEffectIndex)
        SPUtils.putInt(SPKeys.SP_PAINT_SHAPE_IMAGE, lastShapeImage)
        SPUtils.putInt(SPKeys.SP_PAINT_EFFECT_IMAGE, lastEffectImage)
    }

    companion object {
        val FLAG_SHAPE = 0
        val FLAG_EFFECT = 1

        fun newInstance(): PenDialog {
            return PenDialog().apply { setStyle(DialogFragment.STYLE_NO_TITLE, R.style.GraffitiDialog) }
        }
    }
}