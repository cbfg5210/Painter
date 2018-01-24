package com.ue.graffiti.ui

import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import com.ue.graffiti.R
import com.ue.graffiti.constant.SPKeys
import com.ue.graffiti.event.BarSensorListener
import com.ue.graffiti.helper.DialogHelper
import com.ue.graffiti.model.Pel
import com.ue.graffiti.widget.CanvasView
import com.ue.graffiti.widget.TextImageView

/**
 * Created by hawk on 2018/1/15.
 */

class DrawPictureDialog : DialogFragment(), View.OnClickListener {
    private var pelList: List<Pel>? = null
    // 当前重绘位图
    private var savedBitmap: Bitmap? = null
    //重绘画布
    private var savedCanvas: Canvas? = null
    private var drawPictureVi: TextImageView? = null
    private var topToolbar: View? = null
    private var downToolbar: View? = null
    //调色板对话框
    private lateinit var pictureDialog: PictureDialog

    private var mDrawPictureListener: OnDrawPictureListener? = null
    private var canvasWidth: Int = 0
    private var canvasHeight: Int = 0

    fun setDrawPictureListener(drawPictureListener: OnDrawPictureListener) {
        mDrawPictureListener = drawPictureListener
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val contentView = LayoutInflater.from(context).inflate(R.layout.dialog_draw_picture, null)
        drawPictureVi = contentView.findViewById(R.id.drawpicture_canvas)
        drawPictureVi!!.setBarSensorListener(object : BarSensorListener {
            override fun isTopToolbarVisible(): Boolean {
                return topToolbar!!.visibility == View.VISIBLE
            }

            override fun openTools() {
                this@DrawPictureDialog.openTools()
            }

            override fun closeTools() {
                this@DrawPictureDialog.closeTools()
            }
        })

        pictureDialog = PictureDialog.newInstance()
        pictureDialog.setPickPictureListener(object : PictureDialog.OnPickPictureListener {
            override fun onPicturePicked(contentId: Int) {
                //重置插图位置、缩放、旋转信息
                drawPictureVi!!.touch!!.clear()
                //传入插图信息
                drawPictureVi!!.setContentAndCenterPoint(contentId)
                drawPictureVi!!.invalidate()
            }
        })

        topToolbar = contentView.findViewById(R.id.drawpicture_toptoolbar)
        downToolbar = contentView.findViewById(R.id.drawpicture_downtoolbar)

        contentView.findViewById<View>(R.id.drawpicture_refuse).setOnClickListener(this)
        contentView.findViewById<View>(R.id.drawpicture_sure).setOnClickListener(this)
        contentView.findViewById<View>(R.id.drawpicture_select).setOnClickListener(this)

        savedCanvas = Canvas()
        if (savedBitmap != null) {
            savedCanvas!!.setBitmap(savedBitmap) //与画布建立联系
            drawPels()

            drawPictureVi!!.setBitmap(savedBitmap!!, canvasWidth, canvasHeight)
        }
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
        val pelIterator = pelList!!.listIterator()
        while (pelIterator.hasNext()) {
            val pel = pelIterator.next()

            //若是文本图元
            if (pel.text != null) {
                val text = pel.text
                savedCanvas!!.save()
                savedCanvas!!.translate(text!!.transDx, text.transDy)
                savedCanvas!!.scale(text.scale, text.scale, text.centerPoint.x, text.centerPoint.y)
                savedCanvas!!.rotate(text.degree, text.centerPoint.x, text.centerPoint.y)
                savedCanvas!!.drawText(text.content, text.beginPoint.x, text.beginPoint.y, text.paint)
                savedCanvas!!.restore()
            } else if (pel.picture != null) {
                val picture = pel.picture
                savedCanvas!!.save()
                savedCanvas!!.translate(picture!!.transDx, picture.transDy)
                savedCanvas!!.scale(picture.scale, picture.scale, picture.centerPoint.x, picture.centerPoint.y)
                savedCanvas!!.rotate(picture.degree, picture.centerPoint.x, picture.centerPoint.y)
                savedCanvas!!.drawBitmap(picture.createContent(context)!!, picture.beginPoint.x, picture.beginPoint.y, null)
                savedCanvas!!.restore()
            }
            //            else if (!pel.equals(selectedPel))//若非选中的图元
            //                savedCanvas.drawPath(pel.getPath(), pel.getPaint());
        }
    }

    //关闭工具箱
    private fun closeTools() {
        val downDisappearAnim = AnimationUtils.loadAnimation(context, R.anim.downdisappear)
        val topDisappearAnim = AnimationUtils.loadAnimation(context, R.anim.topdisappear)

        downToolbar!!.startAnimation(downDisappearAnim)
        topToolbar!!.startAnimation(topDisappearAnim)

        downToolbar!!.visibility = View.GONE
        topToolbar!!.visibility = View.GONE
    }

    //打开工具箱
    private fun openTools() {
        val downAppearAnim = AnimationUtils.loadAnimation(context, R.anim.downappear)
        val topAppearAnim = AnimationUtils.loadAnimation(context, R.anim.topappear)

        downToolbar!!.startAnimation(downAppearAnim)
        topToolbar!!.startAnimation(topAppearAnim)

        downToolbar!!.visibility = View.VISIBLE
        topToolbar!!.visibility = View.VISIBLE
    }

    override fun onClick(v: View) {
        val viewId = v.id
        when (viewId) {
            R.id.drawpicture_refuse -> dismiss()
            R.id.drawpicture_sure -> onDrawPictureOkBtn(v)
            R.id.drawpicture_select -> pictureDialog.show(childFragmentManager, "")
        }
    }

    fun onDrawPictureOkBtn(v: View) {
        //插入了图
        if (drawPictureVi!!.imageContent != null) {
            val newPel = Pel()
            newPel.picture = drawPictureVi!!.picture

            drawPels()
            if (mDrawPictureListener != null) {
                mDrawPictureListener!!.onPictureDrew(newPel, savedBitmap)
            }
        }
        //结束该活动
        dismiss()
    }

    override fun onResume() {
        super.onResume()
        DialogHelper.showOnceHintDialog(context, R.string.image_gesture_title, R.string.image_gesture_tip, R.string.got_it, SPKeys.SHOW_IMAGE_GESTURE_HINT)
    }

    interface OnDrawPictureListener {
        fun onPictureDrew(newPel: Pel, newBitmap: Bitmap?)
    }

    companion object {

        fun newInstance(): DrawPictureDialog {
            val dialog = DrawPictureDialog()
            dialog.setStyle(DialogFragment.STYLE_NORMAL, R.style.FullScreenDialog)
            return dialog
        }
    }
}