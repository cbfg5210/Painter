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
import com.ue.graffiti.util.getPaintEffectByImage
import com.ue.graffiti.util.getPaintShapeByImage
import com.ue.graffiti.util.getXmlImageArray
import com.ue.graffiti.widget.PenEffectView
import com.ue.library.util.SPUtils
import kotlinx.android.synthetic.main.gr_dialog_pen.view.*

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
                paint.pathEffect = getPaintShapeByImage(lastShapeImage, sbPenStroke.progress, matrix)
            } else {
                lastEffectImage = item.image
                lastEffectIndex = item.index
                paint.maskFilter = getPaintEffectByImage(lastEffectImage)
            }
            // 刷新特效区域
            pevPenEffect.invalidate()
        }
    }

    private val penList: MutableList<Item>
        get() {
            lastShapeIndex = SPUtils.getInt(SPKeys.SP_PAINT_SHAPE_INDEX, 0)
            lastEffectIndex = SPUtils.getInt(SPKeys.SP_PAINT_EFFECT_INDEX, 0)

            return ArrayList<Item>().apply {
                add(PenCatTitleItem(getString(R.string.gr_line_shape)))
                addAll(getPenShapes(FLAG_SHAPE, R.array.gr_penShapeImages, R.array.gr_penShapeNames, lastShapeIndex))
                add(PenCatTitleItem(getString(R.string.gr_special_effect)))
                addAll(getPenShapes(FLAG_EFFECT, R.array.gr_penEffectImages, R.array.gr_penEffectNames, lastEffectIndex))
            }
        }

    private fun getPenShapes(flag: Int, imageArrayId: Int, nameArrayId: Int, lastIndex: Int): ArrayList<Item> {
        val images = context.getXmlImageArray(imageArrayId)
        val names = resources.getStringArray(nameArrayId)

        return images.indices
                .mapTo(ArrayList<Item>(images.size)) {
                    PenShapeItem(flag, images[it], names[it])
                            .apply {
                                index = it
                                isChecked = lastIndex == it
                            }
                }
    }

    fun setCurrentPaint(currentPaint: Paint) {
        paint = currentPaint
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val contentView = inflater.inflate(R.layout.gr_dialog_pen, null)

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
        gAdapter.delegateClickListener = penShapeListener

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
        paint.apply {
            when (lastShapeImage) {
            //椭圆
                R.drawable.ic_line_oval -> {
                    val p = Path().apply {
                        addOval(RectF(0f, 0f, paintStrokeSize.toFloat(), paintStrokeSize.toFloat()), Path.Direction.CCW)
                    }
                    pathEffect = PathDashPathEffect(p, (paintStrokeSize + 10).toFloat(), 0f, PathDashPathEffect.Style.ROTATE)
                }
            //正方形
                R.drawable.ic_line_rect -> {
                    val p = Path().apply {
                        addRect(RectF(0f, 0f, paintStrokeSize.toFloat(), paintStrokeSize.toFloat()), Path.Direction.CCW)
                    }
                    pathEffect = PathDashPathEffect(p, (paintStrokeSize + 10).toFloat(), 0f, PathDashPathEffect.Style.ROTATE)
                }
            //毛笔
                R.drawable.ic_line_brush -> {
                    val p = Path().apply {
                        addRect(RectF(0f, 0f, paintStrokeSize.toFloat(), paintStrokeSize.toFloat()), Path.Direction.CCW)
                        transform(matrix)
                    }
                    pathEffect = PathDashPathEffect(p, 2f, 0f, PathDashPathEffect.Style.TRANSLATE)
                }
            //马克笔
                R.drawable.ic_line_mark_pen -> {
                    val p = Path().apply {
                        addArc(RectF(0f, 0f, (paintStrokeSize + 4).toFloat(), (paintStrokeSize + 4).toFloat()), -90f, 90f)
                        addArc(RectF(0f, 0f, (paintStrokeSize + 4).toFloat(), (paintStrokeSize + 4).toFloat()), 90f, -90f)
                    }
                    pathEffect = PathDashPathEffect(p, 2f, 0f, PathDashPathEffect.Style.TRANSLATE)
                }
            }
            //改变粗细
            strokeWidth = paintStrokeSize.toFloat()
        }
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
            return PenDialog().apply { setStyle(DialogFragment.STYLE_NO_TITLE, R.style.gr_GraffitiDialog) }
        }
    }
}