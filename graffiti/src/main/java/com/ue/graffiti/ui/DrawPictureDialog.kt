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
import com.ue.graffiti.helper.DialogHelper
import com.ue.graffiti.model.Pel
import com.ue.graffiti.util.loadDrawTextImageAnimations
import com.ue.graffiti.widget.CanvasView
import com.ue.graffiti.widget.TextImageView
import kotlinx.android.synthetic.main.gr_dialog_draw_picture.view.*

/**
 * Created by hawk on 2018/1/15.
 */

class DrawPictureDialog : DialogFragment(), View.OnClickListener {
    private lateinit var pelList: List<Pel>
    // 当前重绘位图
    private lateinit var savedBitmap: Bitmap
    //重绘画布
    private lateinit var savedCanvas: Canvas
    private lateinit var tivCanvas: TextImageView
    private lateinit var vgTopToolbar: View
    private lateinit var vgDownToolbar: View
    //调色板对话框
    private lateinit var pictureDialog: PictureDialog

    var drawPictureListener: OnDrawPictureListener? = null
    private var canvasWidth = 0
    private var canvasHeight = 0

    //显示动画：上、下
    private val showAnimations: Array<Animation> by lazy { loadDrawTextImageAnimations(context, true) }
    //隐藏动画：上、下
    private val hideAnimations: Array<Animation> by lazy { loadDrawTextImageAnimations(context, false) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val contentView = inflater.inflate(R.layout.gr_dialog_draw_picture, null)
        tivCanvas = contentView.tivCanvas
        tivCanvas.barSensorListener = object : BarSensorListener {
            override fun isTopToolbarVisible(): Boolean {
                return vgTopToolbar.visibility == View.VISIBLE
            }

            override fun openTools() {
                toggleMenuVisibility(true)
            }

            override fun closeTools() {
                toggleMenuVisibility(false)
            }
        }

        pictureDialog = PictureDialog.newInstance()
        pictureDialog.pickPictureListener = object : PictureDialog.OnPickPictureListener {
            override fun onPicturePicked(contentId: Int) {
                //重置插图位置、缩放、旋转信息
                tivCanvas.touch?.clear()
                //传入插图信息
                tivCanvas.setContentAndCenterPoint(contentId)
                tivCanvas.invalidate()
            }
        }

        vgTopToolbar = contentView.vgTopToolbar
        vgDownToolbar = contentView.tvSelectPicture

        contentView.ivCancel.setOnClickListener(this)
        contentView.rbImage.setOnClickListener(this)
        contentView.tvSelectPicture.setOnClickListener(this)

        savedCanvas = Canvas()
        savedCanvas.setBitmap(savedBitmap) //与画布建立联系
        drawPels()
        tivCanvas.setBitmap(savedBitmap, canvasWidth, canvasHeight)
        return contentView
    }

    fun setGraffitiInfo(cvGraffitiView: CanvasView) {
        this.canvasWidth = cvGraffitiView.canvasWidth
        this.canvasHeight = cvGraffitiView.canvasHeight
        this.savedBitmap = cvGraffitiView.savedBitmap!!.copy(Bitmap.Config.ARGB_8888, true)//由画布背景创建缓冲位图
        this.pelList = cvGraffitiView.getPelList()
    }

    fun drawPels() {
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

    private fun toggleMenuVisibility(isVisible: Boolean) {
        val visibility = if (isVisible) View.VISIBLE else View.GONE
        val animations = getToggleAnimations(isVisible)

        vgTopToolbar.startAnimation(animations[0])
        vgDownToolbar.startAnimation(animations[1])

        vgDownToolbar.visibility = visibility
        vgTopToolbar.visibility = visibility
    }

    private fun getToggleAnimations(isVisible: Boolean): Array<Animation> {
        return if (isVisible) showAnimations else hideAnimations
    }

    override fun onClick(v: View) {
        val viewId = v.id
        when (viewId) {
            R.id.ivCancel -> dismiss()
            R.id.tvSelectPicture -> pictureDialog.show(childFragmentManager, "")
            R.id.rbImage -> {
                //插入了图
                if (tivCanvas.imageContent != null) {
                    val newPel = Pel().apply { picture = tivCanvas.picture }
                    drawPels()
                    drawPictureListener?.onPictureDrew(newPel, savedBitmap)
                }
                //结束该活动
                dismiss()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        DialogHelper.showOnceHintDialog(context, R.string.gr_image_gesture_title, R.string.gr_image_gesture_tip, R.string.gr_got_it, SPKeys.SHOW_IMAGE_GESTURE_HINT)
    }

    interface OnDrawPictureListener {
        fun onPictureDrew(newPel: Pel, newBitmap: Bitmap?)
    }

    companion object {
        fun newInstance() = DrawPictureDialog().apply { setStyle(DialogFragment.STYLE_NORMAL, R.style.FullScreenDialog) }
    }
}