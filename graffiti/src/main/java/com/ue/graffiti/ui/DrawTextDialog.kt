package com.ue.graffiti.ui

import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import com.ue.graffiti.R
import com.ue.graffiti.constant.SPKeys
import com.ue.graffiti.event.BarSensorListener
import com.ue.graffiti.event.OnSingleResultListener
import com.ue.graffiti.helper.DialogHelper
import com.ue.graffiti.model.Pel
import com.ue.graffiti.util.loadDrawTextImageAnimations
import com.ue.graffiti.widget.CanvasView
import com.ue.graffiti.widget.TextImageView
import kotlinx.android.synthetic.main.dialog_draw_text.view.*

/**
 * Created by hawk on 2018/1/15.
 */

class DrawTextDialog : DialogFragment(), View.OnClickListener {
    private lateinit var pelList: List<Pel>
    // 当前重绘位图
    private lateinit var savedBitmap: Bitmap
    private lateinit var savedCanvas: Canvas

    private lateinit var tivCanvas: TextImageView
    private lateinit var vgDrawTextTopMenu: View
    private lateinit var vgDrawTextBottomMenu: View

    private var mDrawTextListener: OnDrawTextListener? = null

    private var mCanvasWidth = 0
    private var mCanvasHeight = 0
    private var paintColor = 0
    //显示动画：上、下
    private var showAnimations: Array<Animation>? = null
    //隐藏动画：上、下
    private var hideAnimations: Array<Animation>? = null

    fun setDrawTextListener(drawTextListener: OnDrawTextListener) {
        mDrawTextListener = drawTextListener
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val contentView = LayoutInflater.from(context).inflate(R.layout.dialog_draw_text, null)
        tivCanvas = contentView.tivCanvas
        tivCanvas.barSensorListener = object : BarSensorListener {
            override fun isTopToolbarVisible(): Boolean {
                return vgDrawTextTopMenu.visibility == View.VISIBLE
            }

            override fun openTools() {
                toggleMenuVisibility(true)
            }

            override fun closeTools() {
                toggleMenuVisibility(false)
            }
        }

        vgDrawTextTopMenu = contentView.vgDrawTextTopMenu
        vgDrawTextBottomMenu = contentView.vgDrawTextBottomMenu

        contentView.btnCancel.setOnClickListener(this)
        contentView.btnInsertText.setOnClickListener(this)
        contentView.btnTextContent.setOnClickListener(this)
        contentView.btnTextColor.setOnClickListener(this)

        savedCanvas = Canvas()
        //与画布建立联系
        savedCanvas.setBitmap(savedBitmap)
        drawPels()
        tivCanvas.setBitmap(savedBitmap, mCanvasWidth, mCanvasHeight, paintColor)

        return contentView
    }

    fun setGraffitiInfo(cvGraffitiView: CanvasView) {
        this.mCanvasWidth = cvGraffitiView.canvasWidth
        this.mCanvasHeight = cvGraffitiView.canvasHeight
        this.paintColor = cvGraffitiView.paintColor
        //由画布背景创建缓冲位图
        this.savedBitmap = cvGraffitiView.savedBitmap!!.copy(Bitmap.Config.ARGB_8888, true)
        this.pelList = cvGraffitiView.getPelList()
    }

    private fun drawPels() {
        // 获取pelList对应的迭代器头结点
        val pelIterator = pelList.listIterator()
        while (pelIterator.hasNext()) {
            val pel = pelIterator.next()
            //若是文本图元
            if (pel.text != null) {
                val text = pel.text
                savedCanvas.save()
                savedCanvas.translate(text!!.transDx, text.transDy)
                savedCanvas.scale(text.scale, text.scale, text.centerPoint.x, text.centerPoint.y)
                savedCanvas.rotate(text.degree, text.centerPoint.x, text.centerPoint.y)
                savedCanvas.drawText(text.content, text.beginPoint.x, text.beginPoint.y, text.paint)
                savedCanvas.restore()
            } else if (pel.picture != null) {
                val picture = pel.picture
                savedCanvas.save()
                savedCanvas.translate(picture!!.transDx, picture.transDy)
                savedCanvas.scale(picture.scale, picture.scale, picture.centerPoint.x, picture.centerPoint.y)
                savedCanvas.rotate(picture.degree, picture.centerPoint.x, picture.centerPoint.y)
                savedCanvas.drawBitmap(picture.createContent(context)!!, picture.beginPoint.x, picture.beginPoint.y, null)
                savedCanvas.restore()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        demandContent()
        DialogHelper.showOnceHintDialog(context, R.string.text_gesture_title, R.string.text_gesture_tip, R.string.got_it, SPKeys.SHOW_TEXT_GESTURE_HINT)
    }

    private fun demandContent() {
        DialogHelper.showInputDialog(context, getString(R.string.input_text), getString(R.string.finger_graffiti), object : OnSingleResultListener {
            override fun onResult(result: Any) {
                tivCanvas.setTextContent(result as String)
                tivCanvas.invalidate()
            }
        })
    }

    private fun toggleMenuVisibility(isVisible: Boolean) {
        val visibility = if (isVisible) View.VISIBLE else View.GONE
        val animations = getToggleAnimations(isVisible)

        vgDrawTextTopMenu.startAnimation(animations[0])
        vgDrawTextBottomMenu.startAnimation(animations[1])

        vgDrawTextBottomMenu.visibility = visibility
        vgDrawTextTopMenu.visibility = visibility
    }

    private fun getToggleAnimations(isVisible: Boolean): Array<Animation> {
        return if (isVisible) {
            if (showAnimations == null) {
                showAnimations = loadDrawTextImageAnimations(context, isVisible)
            }
            showAnimations!!
        } else {
            if (hideAnimations == null) {
                hideAnimations = loadDrawTextImageAnimations(context, isVisible)
            }
            hideAnimations!!
        }
    }

    override fun onClick(v: View) {
        val viewId = v.id
        when (viewId) {
            R.id.btnCancel -> dismiss()
            R.id.btnTextContent -> demandContent()
            R.id.btnInsertText -> {
                //构造该次的文本对象,并装入图元对象
                drawPels()
                mDrawTextListener?.onTextDrew(Pel().apply { text = tivCanvas.getText(paintColor) }, savedBitmap)
                //结束该活动
                dismiss()
            }
            R.id.btnTextColor -> DialogHelper.showColorPickerDialog(activity, object : ColorPickerDialog.OnColorPickerListener {
                override fun onColorPicked(color: Int) {
                    paintColor = color
                    tivCanvas.setTextColor(color)
                }
            })
        }
    }

    interface OnDrawTextListener {
        fun onTextDrew(newPel: Pel, newBitmap: Bitmap?)
    }

    companion object {
        fun newInstance(): DrawTextDialog {
            return DrawTextDialog().apply { setStyle(DialogFragment.STYLE_NORMAL, R.style.FullScreenDialog) }
        }
    }
}