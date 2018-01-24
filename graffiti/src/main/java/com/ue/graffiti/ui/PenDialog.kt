package com.ue.graffiti.ui

import android.content.DialogInterface
import android.graphics.*
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
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
import java.util.*

//调色板对话框
class PenDialog : DialogFragment(), OnSeekBarChangeListener {
    //获取当前绘制画笔
    private var paint: Paint? = null
    // 调整笔触相关控件
    private var peneffectVi: PenEffectView? = null
    private var penwidthSeekBar: SeekBar? = null
    private var penwidthTextVi: TextView? = null
    // 线形按钮
    private var matrix: Matrix? = null

    private var paintStrokeSize: Int = 0
    private var lastShapeIndex: Int = 0
    private var lastEffectIndex: Int = 0
    private var lastShapeImage: Int = 0
    private var lastEffectImage: Int = 0

    private var adapter: PenStyleAdapter? = null

    private val penShapeListener = object : OnDelegateClickListener {
        override fun onClick(view: View, i: Int) {
            val item = adapter!!.items[i] as PenShapeItem
            if (item.flag == FLAG_SHAPE) {
                lastShapeImage = item.image
                lastShapeIndex = item.index
                paint!!.pathEffect = PenUtils.getPaintShapeByImage(lastShapeImage, penwidthSeekBar!!.progress, matrix)
            } else {
                lastEffectImage = item.image
                lastEffectIndex = item.index
                paint!!.maskFilter = PenUtils.getPaintEffectByImage(lastEffectImage)
            }
            // 刷新特效区域
            peneffectVi!!.invalidate()
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

            run {
                var i = 0
                val len = images.size
                while (i < len) {
                    val item = PenShapeItem(FLAG_SHAPE, images[i], names[i])
                    item.index = i
                    item.isChecked = lastShapeIndex == i
                    items.add(item)
                    i++
                }
            }

            items.add(PenCatTitleItem(getString(R.string.special_effect)))
            images = ResourceUtils.getImageArray(context, R.array.penEffectImages)
            names = resources.getStringArray(R.array.penEffectNames)

            var i = 0
            val len = images.size
            while (i < len) {
                val item = PenShapeItem(FLAG_EFFECT, images[i], names[i])
                item.index = i
                item.isChecked = lastEffectIndex == i
                items.add(item)
                i++
            }

            return items
        }

    fun setCurrentPaint(currentPaint: Paint) {
        paint = currentPaint
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val contentView = inflater!!.inflate(R.layout.dialog_pen, null)

        peneffectVi = contentView.findViewById<View>(R.id.pevPenEffect) as PenEffectView
        peneffectVi!!.setPaint(paint!!)

        penwidthSeekBar = contentView.findViewById<View>(R.id.sbPenStroke) as SeekBar
        penwidthTextVi = contentView.findViewById<View>(R.id.tvPenStroke) as TextView

        matrix = Matrix()
        matrix!!.setSkew(2f, 2f)
        // 设置监听
        penwidthSeekBar!!.setOnSeekBarChangeListener(this)

        contentView.findViewById<View>(R.id.btnPickPen).setOnClickListener { v -> dismiss() }

        // 以当前画笔风格初始化特效区域
        paintStrokeSize = paint!!.strokeWidth.toInt()
        penwidthSeekBar!!.progress = paintStrokeSize

        penwidthTextVi!!.text = Integer.toString(paintStrokeSize)
        peneffectVi!!.invalidate()
        /*
        * init effect list
        * */
        adapter = PenStyleAdapter(activity, penList)
        adapter!!.setDelegateClickListener(penShapeListener)

        val layoutManager = GridLayoutManager(context, 4)
        layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return adapter!!.getSpanCount(position)
            }
        }

        val rvPenStyles = contentView.findViewById<RecyclerView>(R.id.rvPenStyles)
        rvPenStyles.setHasFixedSize(true)
        rvPenStyles.layoutManager = layoutManager
        rvPenStyles.adapter = adapter

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
        penwidthTextVi!!.text = Integer.toString(paintStrokeSize)
        //对于PathDashPathEffect特效要特殊处理
        when (lastShapeImage) {
            R.drawable.ic_line_oval -> {
                //椭圆
                val p = Path()
                p.addOval(RectF(0f, 0f, paintStrokeSize.toFloat(), paintStrokeSize.toFloat()), Path.Direction.CCW)
                paint!!.pathEffect = PathDashPathEffect(p, (paintStrokeSize + 10).toFloat(), 0f, PathDashPathEffect.Style.ROTATE)
            }
            R.drawable.ic_line_rect -> {
                //正方形
                val p = Path()
                p.addRect(RectF(0f, 0f, paintStrokeSize.toFloat(), paintStrokeSize.toFloat()), Path.Direction.CCW)
                paint!!.pathEffect = PathDashPathEffect(p, (paintStrokeSize + 10).toFloat(), 0f, PathDashPathEffect.Style.ROTATE)
            }
            R.drawable.ic_line_brush -> {
                //毛笔
                val p = Path()
                p.addRect(RectF(0f, 0f, paintStrokeSize.toFloat(), paintStrokeSize.toFloat()), Path.Direction.CCW)
                p.transform(matrix)
                paint!!.pathEffect = PathDashPathEffect(p, 2f, 0f, PathDashPathEffect.Style.TRANSLATE)
            }
            R.drawable.ic_line_mark_pen -> {
                //马克笔
                val p = Path()
                p.addArc(RectF(0f, 0f, (paintStrokeSize + 4).toFloat(), (paintStrokeSize + 4).toFloat()), -90f, 90f)
                p.addArc(RectF(0f, 0f, (paintStrokeSize + 4).toFloat(), (paintStrokeSize + 4).toFloat()), 90f, -90f)
                paint!!.pathEffect = PathDashPathEffect(p, 2f, 0f, PathDashPathEffect.Style.TRANSLATE)
            }
        }
        //改变粗细
        paint!!.strokeWidth = paintStrokeSize.toFloat()
        // 更新示意view
        peneffectVi!!.invalidate()
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
            val dialog = PenDialog()
            dialog.setStyle(DialogFragment.STYLE_NO_TITLE, R.style.GraffitiDialog)
            return dialog
        }
    }
}