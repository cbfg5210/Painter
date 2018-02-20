package com.ue.graffiti.ui

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.*
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.text.TextUtils
import android.view.MotionEvent
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.Toast
import com.trello.rxlifecycle2.components.support.RxAppCompatActivity
import com.ue.graffiti.R
import com.ue.graffiti.constant.DrawPelFlags
import com.ue.graffiti.constant.SPKeys
import com.ue.graffiti.event.OnSingleResultListener
import com.ue.graffiti.event.OnStepListener
import com.ue.graffiti.helper.DialogHelper
import com.ue.graffiti.model.*
import com.ue.graffiti.touch.*
import com.ue.graffiti.util.calPelCenterPoint
import com.ue.graffiti.util.calPelSavedMatrix
import com.ue.graffiti.util.toRedoUpdate
import com.ue.graffiti.util.toUndoUpdate
import com.ue.library.util.FileUtils
import com.ue.library.util.IntentUtils
import com.ue.library.util.SPUtils
import com.ue.library.util.toast
import com.ue.library.widget.colorpicker.ColorPicker
import com.ue.library.widget.colorpicker.SatValView
import kotlinx.android.synthetic.main.gr_activity_graffiti.*
import kotlinx.android.synthetic.main.gr_layout_bottom_menu.*
import kotlinx.android.synthetic.main.gr_layout_right_menu.*
import kotlinx.android.synthetic.main.gr_layout_top_menu.*

class GraffitiActivity : RxAppCompatActivity(), View.OnClickListener {
    private lateinit var curToolVi: View
    private lateinit var curPelVi: ImageView
    private lateinit var curCanvasBgVi: ImageView
    private lateinit var whiteCanvasBgVi: ImageView

    private val lastPoint = PointF()
    private var newPel: Pel? = null
    private lateinit var sensorManager: SensorManager
    private var responseCount = 0

    private lateinit var mMainPresenter: MainPresenter

    private val transMatrix = Matrix()
    private val savedMatrix = Matrix()
    private var savedPel: Pel? = null
    private var step: Step? = null
    private val centerPoint = PointF()
    private var lastEditActionId = 0

    private var graffitiName = ""

    private lateinit var cpPaletteColorPicker: ColorPicker

    //单手操作传感器监听者
    private val singleHandSensorEventListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            if (curToolVi.id != R.id.rbDraw) {
                return
            }
            //单手加速度作图
            if (responseCount % 2 == 0) {
                val dx = -event.values[0]
                val dy = event.values[1]
                val nowPoint = PointF(lastPoint.x + dx, lastPoint.y + dy)

                newPel!!.path.quadTo(lastPoint.x, lastPoint.y, (lastPoint.x + nowPoint.x) / 2, (lastPoint.y + nowPoint.y) / 2)
                lastPoint.set(nowPoint)

                cvGraffitiView.setSelectedPel(newPel)
                cvGraffitiView.invalidate()
            }
            responseCount++
        }

        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.gr_activity_graffiti)

        mMainPresenter = MainPresenter(this)
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        cpPaletteColorPicker = ColorPicker(this, Color.BLACK, object : SatValView.OnColorChangeListener {
            override fun onColorChanged(newColor: Int) {
                SPUtils.putInt(SPKeys.SP_PAINT_COLOR, newColor)
                tvColor.setTextColor(newColor)
                cvGraffitiView.paintColor = newColor
            }
        })

        initViews()

        DialogHelper.showOnceHintDialog(this, R.string.gr_draw_gesture_title, R.string.gr_draw_gesture_tip, R.string.gr_got_it, SPKeys.SHOW_DRAW_GESTURE_HINT)
    }

    private fun initViews() {
        rgDrawOptions.check(R.id.rbDraw)
        ivToggleOptions.isSelected = true
        curToolVi = rbDraw

        val lastColor = SPUtils.getInt(SPKeys.SP_PAINT_COLOR, resources.getColor(R.color.col_298ecb))
        tvColor.setTextColor(lastColor)
        cvGraffitiView.paintColor = lastColor

        whiteCanvasBgVi = mMainPresenter.initCanvasBgsPopupWindow(R.layout.gr_popup_canvas_bgs, R.id.vgCanvasBgs, object : View.OnClickListener {
            override fun onClick(v: View?) {
                updateCanvasBgAndIcons(v as ImageView)
            }
        })

        curCanvasBgVi = whiteCanvasBgVi
        curPelVi = mMainPresenter.initPelsPopupWindow(R.layout.gr_popup_pels, R.id.vgPels, cvGraffitiView, object : MainPresenter.OnPickPelListener {
            override fun onPelPick(v: View, pelTouch: Touch?) {
                mMainPresenter.dismissPopupWindows()
                updatePelBarIcons(v as ImageView)
                registerKeepDrawingSensor(pelTouch)
                cvGraffitiView.touch = pelTouch
            }
        })

        setListeners()
    }

    private fun setListeners() {
        setListenerForViews(arrayOf(
                tvColor, tvPen, tvClear, tvSave, tvShare,
                ivUndo, ivRedo,
                rbDraw, rbEdit, rbFill, rbBg, rbText, rbImage),
                this)
        setListenerForViews(arrayOf(
                ivDelete, ivCopy, ivRotate, ivZoomIn, ivZoomOut, ivFill, ivToggleOptions),
                View.OnClickListener { v -> onSwitchEditMenuAction(v) })
    }

    private fun setListenerForViews(views: Array<View>, listener: View.OnClickListener) {
        for (i in views.indices) {
            views[i].setOnClickListener(listener)
        }
    }

    private fun openTools() {
        if (vgRightMenu.visibility == View.VISIBLE) {
            vgRightMenu.visibility = View.GONE
        }
        toggleMenuVisibility(true)
    }

    private fun closeTools() {
        mMainPresenter.dismissPopupWindows()
        cvGraffitiView.clearRedoStack()

        toggleMenuVisibility(false)
    }

    private fun onOpenPelBarBtn(v: View) {
        ensurePelFinished()
        mMainPresenter.showPelsPopupWindow(vgBottomMenu)

        curToolVi = v

        val pelTouch = mMainPresenter.getPelTouchByViewId(curPelVi.id, cvGraffitiView)
        registerKeepDrawingSensor(pelTouch)

        cvGraffitiView.touch = pelTouch
    }

    private fun registerKeepDrawingSensor(pelTouch: Touch?) {
        if (pelTouch !is KeepDrawingTouch) {
            return
        }
        pelTouch.keepDrawingTouchListener = object : KeepDrawingTouch.KeepDrawingTouchListener {
            override fun onDownPoint(downPoint: PointF) {
                lastPoint.set(downPoint)
            }

            override fun registerKeepDrawingSensor() {
                this@GraffitiActivity.registerKeepDrawingSensor()
            }
        }
    }

    private fun toggleMenuVisibility(isVisible: Boolean) {
        val visibility = if (isVisible) View.VISIBLE else View.GONE
        val animations = mMainPresenter.getToggleAnimations(isVisible)

        vgTopMenu.startAnimation(animations[1])
        vgBottomMenu.startAnimation(animations[3])

        vgBottomMenu.visibility = visibility
        vgTopMenu.visibility = visibility
    }

    private fun onOpenTransBarBtn(v: View) {
        mMainPresenter.dismissPopupWindows()
        curToolVi = v
        closeTools()

        if (v.id == R.id.rbEdit) {
            val leftAppearAnim = AnimationUtils.loadAnimation(this, R.anim.gr_left_appear)
            vgRightMenu.visibility = View.VISIBLE
            ivToggleOptions.visibility = View.VISIBLE
            vgRightMenu.startAnimation(leftAppearAnim)
        }

        toggleSensor(false)

        cvGraffitiView.touch = TransformTouch(cvGraffitiView)
    }

    private fun onOpenDrawTextBtn(v: View) {
        DialogHelper.showDrawTextDialog(this, cvGraffitiView, object : DrawTextDialog.OnDrawTextListener {
            override fun onTextDrew(newPel: Pel, newBitmap: Bitmap?) {
                //添加至文本总链表
                cvGraffitiView.addPel(newPel)
                //记录栈中信息
                cvGraffitiView.pushUndoStack(DrawPelStep(DrawPelFlags.DRAW, cvGraffitiView.getPelList(), newPel))
                //更新画布
                cvGraffitiView.updateSavedBitmap()
            }
        })
    }

    private fun onOpenDrawPictureBtn(v: View) {
        DialogHelper.showDrawPictureDialog(this, cvGraffitiView, object : DrawPictureDialog.OnDrawPictureListener {
            override fun onPictureDrew(newPel: Pel, newBitmap: Bitmap?) {
                //添加至文本总链表
                cvGraffitiView.addPel(newPel)
                //记录栈中信息
                cvGraffitiView.pushUndoStack(DrawPelStep(DrawPelFlags.DRAW, cvGraffitiView.getPelList(), newPel))
                //更新画布
                cvGraffitiView.updateSavedBitmap()
            }
        })
    }

    private fun onCopyPelClick(selectedPel: Pel) {
        val pel = selectedPel.clone()
        pel.path.offset(10f, 10f)
        pel.region.setPath(pel.path, cvGraffitiView.clipRegion)

        cvGraffitiView.addPel(pel)
        cvGraffitiView.pushUndoStack(DrawPelStep(DrawPelFlags.COPY, cvGraffitiView.getPelList(), pel))

        cvGraffitiView.setSelectedPel(null)
        cvGraffitiView.updateSavedBitmap()
    }

    private fun onDeletePelClick(selectedPel: Pel) {
        cvGraffitiView.pushUndoStack(DrawPelStep(DrawPelFlags.DELETE, cvGraffitiView.getPelList(), selectedPel))
        cvGraffitiView.removePel(selectedPel)

        cvGraffitiView.setSelectedPel(null)
        cvGraffitiView.updateSavedBitmap()
    }

    private fun updatePelBarIcons(v: ImageView) {
        curPelVi.setImageDrawable(null)
        v.setImageResource(R.drawable.bg_highlight_frame)
        curPelVi = v

        val fatherDrawable = resources.getDrawable(mMainPresenter.getDrawRes(v.id))
        rbDraw.setCompoundDrawablesWithIntrinsicBounds(null, fatherDrawable, null, null)
    }

    private fun updateCanvasBgAndIcons(v: View) {
        curCanvasBgVi.setImageDrawable(null)
        curCanvasBgVi = v as ImageView
        curCanvasBgVi.setImageResource(R.drawable.bg_highlight_frame)

        val backgroundDrawable = mMainPresenter.getBgSelectedRes(v.id)
        if (backgroundDrawable != 0) {
            cvGraffitiView.setBackgroundBitmap(backgroundDrawable)
        }
    }

    //确保未画完的图元能够真正敲定
    private fun ensurePelFinished() {
        cvGraffitiView.getSelectedPel() ?: return
        val touch = cvGraffitiView.touch
        if (touch is DrawBesselTouch) {
            touch.control = true
            touch.up()
            return
        }
        if (touch is DrawBrokenLineTouch) {
            touch.hasFinished = true
            touch.up()
            return
        }
        if (touch is DrawPolygonTouch) {
            touch.curPoint.set(touch.beginPoint)
            touch.up()
            return
        }
        cvGraffitiView.setSelectedPel(null)
        cvGraffitiView.updateSavedBitmap()
    }

    private fun completeKeepDrawing() {
        toggleSensor(false)

        newPel!!.apply {
            region.setPath(path, cvGraffitiView.clipRegion)
            paint.set(cvGraffitiView.getCurrentPaint())

            cvGraffitiView.addPel(this)
            cvGraffitiView.pushUndoStack(DrawPelStep(DrawPelFlags.DRAW, cvGraffitiView.getPelList(), this))
            cvGraffitiView.setSelectedPel(null)
            cvGraffitiView.updateSavedBitmap()
        }
    }

    private fun registerKeepDrawingSensor() {
        newPel = Pel().apply { closure = true }
        lastPoint.set(cvGraffitiView.touch!!.curPoint)
        newPel!!.path.moveTo(lastPoint.x, lastPoint.y)

        toggleSensor(true)
    }

    private fun toggleSensor(open: Boolean) {
        sensorManager.apply {
            if (open) registerListener(singleHandSensorEventListener, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_GAME)
            else unregisterListener(singleHandSensorEventListener)
        }
        cvGraffitiView.setSensorRegistered(open)
    }

    private fun onUndoBtn(v: View) {
        val step = cvGraffitiView.popUndoStack() ?: return

        cvGraffitiView.touch!!.setProcessing(true, getString(R.string.gr_undoing))
        toRedoUpdate(this, step, cvGraffitiView.getBackgroundBitmap(), cvGraffitiView.getCopyOfBackgroundBitmap(), object : OnStepListener {
            override fun onComplete() {
                if (step is TransformPelStep) {
                    cvGraffitiView.setSelectedPel(null)
                }
                //重绘位图
                cvGraffitiView.updateSavedBitmap()
                cvGraffitiView.pushRedoStack(step)

                cvGraffitiView.touch!!.setProcessing(false, null)
            }
        })
    }

    private fun onRedoBtn(v: View) {
        val step = cvGraffitiView.popRedoStack() ?: return
        cvGraffitiView.touch!!.setProcessing(true, getString(R.string.gr_redoing))
        toUndoUpdate(this, step, cvGraffitiView.getBackgroundBitmap(), object : OnStepListener {
            override fun onComplete() {
                //重绘位图
                cvGraffitiView.updateSavedBitmap()
                cvGraffitiView.touch!!.setProcessing(false, null)
            }
        })
        cvGraffitiView.pushUndoStack(step)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        if (resultCode == REQUEST_CODE_NONE) {
            return
        }
        // 触发拍照模式的“确定”、“取消”、“返键”按钮后
        if (requestCode == REQUEST_CODE_GRAPH) {
            cvGraffitiView.touch!!.setProcessing(true, getString(R.string.gr_loading))
            mMainPresenter.loadCapturePhoto()
                    .subscribe { bitmap ->
                        cvGraffitiView.touch!!.setProcessing(false, null)
                        cvGraffitiView.setBackgroundBitmap(bitmap)
                    }
            return
        }
        // 触发图库模式的“选择”、“返键”按钮后
        if (requestCode == REQUEST_CODE_PICTURE) {
            cvGraffitiView.touch!!.setProcessing(true, getString(R.string.gr_loading))
            mMainPresenter.loadPictureFromIntent(data, cvGraffitiView.canvasWidth, cvGraffitiView.canvasHeight)
                    .subscribe { bitmap ->
                        cvGraffitiView.touch!!.setProcessing(false, null)
                        cvGraffitiView.setBackgroundBitmap(bitmap)
                    }
            return
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onBackPressed() {
        DialogHelper.showExitDialog(this, View.OnClickListener {
            saveGraffiti(object : FileUtils.OnSaveImageListener {
                override fun onSaved(path: String) {
                    finish()
                }
            })
        })
    }

    private fun saveGraffiti(saveListener: FileUtils.OnSaveImageListener? = null) {
        if (!TextUtils.isEmpty(graffitiName)) {
            mMainPresenter.onSaveGraffitiClicked(cvGraffitiView.savedBitmap!!, graffitiName, saveListener)
            return
        }
        DialogHelper.showInputDialog(this, getString(R.string.gr_input_graffiti_name), object : OnSingleResultListener {
            override fun onResult(result: Any) {
                graffitiName = result as String
                mMainPresenter.onSaveGraffitiClicked(cvGraffitiView.savedBitmap!!, graffitiName, saveListener)
            }
        })
    }

    override fun onClick(v: View) {
        if (!prepareToggleSwitchMenuAction(v)) {
            return
        }
        val viewId = v.id
        when (viewId) {
        /*
        * top menu listener
        * */
            R.id.tvPen -> DialogHelper.showPenDialog(this@GraffitiActivity, cvGraffitiView.getCurrentPaint())
            R.id.tvSave -> saveGraffiti()
            R.id.tvColor -> cpPaletteColorPicker.show(tvColor)
            R.id.tvClear -> DialogHelper.showClearDialog(this, DialogInterface.OnClickListener { _, _ ->
                //清空内部所有数据
                cvGraffitiView.clearData()
                //画布背景图标复位
                updateCanvasBgAndIcons(whiteCanvasBgVi)
                //清除填充过颜色的地方
                cvGraffitiView.setBackgroundBitmap()
            })
            R.id.tvShare -> saveGraffiti(object : FileUtils.OnSaveImageListener {
                override fun onSaved(path: String) {
                    IntentUtils.shareImage(
                            this@GraffitiActivity,
                            getString(R.string.gr_module_name),
                            getString(R.string.gr_share_work_app_module_link, getString(R.string.gr_module_name), getString(R.string.gr_module_name), getString(R.string.gr_download_link)),
                            path)
                }
            })
        /*
        * bottom menu listener
        * */
            R.id.rbDraw -> onOpenPelBarBtn(v)
            R.id.rbEdit -> onOpenTransBarBtn(v)
            R.id.rbFill -> {
                curToolVi = v
                cvGraffitiView.touch = CrossFillTouch(cvGraffitiView)
            }
            R.id.rbBg -> mMainPresenter.showCanvasBgsPopupWindow(vgBottomMenu)
            R.id.rbText -> onOpenDrawTextBtn(v)
            R.id.rbImage -> onOpenDrawPictureBtn(v)
        /**
         * other listener:undo,redo,extend
         */
            R.id.ivUndo -> onUndoBtn(v)
            R.id.ivRedo -> onRedoBtn(v)
        }
    }

    private fun onSwitchEditMenuAction(view: View) {
        if (!prepareToggleSwitchMenuAction(view)) {
            return
        }
        val viewId = view.id
        if (viewId == R.id.ivToggleOptions) {
            onOpenTransChildren()
            return
        }
        val selectedPel = cvGraffitiView.getSelectedPel()
        if (selectedPel == null) {
            toast(R.string.gr_select_pel_first, Toast.LENGTH_LONG)
            return
        }
        when (viewId) {
            R.id.ivDelete -> onDeletePelClick(selectedPel)
            R.id.ivCopy -> onCopyPelClick(selectedPel)
            R.id.ivRotate, R.id.ivZoomIn, R.id.ivZoomOut -> onSwitchRotateZoom(viewId, selectedPel)
            R.id.ivFill -> onFillPelClick(selectedPel)
        }
    }

    private fun onSwitchRotateZoom(viewId: Int, selectedPel: Pel) {
        savedPel = Pel().apply { path.set(selectedPel.path) }
        savedMatrix.set(calPelSavedMatrix(savedPel!!))

        if (lastEditActionId == 0) {
            lastEditActionId = viewId
        }
        if (lastEditActionId != viewId) {
            step = TransformPelStep(cvGraffitiView.getPelList(), cvGraffitiView.clipRegion, selectedPel)
            selectedPel.region.setPath(selectedPel.path, cvGraffitiView.clipRegion)
            (step as TransformPelStep).toUndoMatrix = transMatrix
            cvGraffitiView.pushUndoStack(step!!)
            cvGraffitiView.updateSavedBitmap()

            lastEditActionId = viewId
        }

        when (viewId) {
            R.id.ivRotate -> {
                centerPoint.set(calPelCenterPoint(selectedPel))
                transMatrix.set(savedMatrix)
                transMatrix.setRotate(10f, centerPoint.x, centerPoint.y)
            }
            R.id.ivZoomIn -> {
                centerPoint.set(calPelCenterPoint(selectedPel))
                transMatrix.set(savedMatrix)
                transMatrix.postScale(1.1f, 1.1f, centerPoint.x, centerPoint.y)
            }
            R.id.ivZoomOut -> {
                centerPoint.set(calPelCenterPoint(selectedPel))
                transMatrix.set(savedMatrix)
                transMatrix.postScale(0.9f, 0.9f, centerPoint.x, centerPoint.y)
            }
        }
        selectedPel.path.set(savedPel!!.path)
        selectedPel.path.transform(transMatrix)

        cvGraffitiView.invalidate()
    }

    private fun onFillPelClick(selectedPel: Pel) {
        val oldPaint = Paint(selectedPel.paint)
        selectedPel.paint.set(cvGraffitiView.getCurrentPaint())
        selectedPel.paint.style = if (selectedPel.closure) Paint.Style.FILL else Paint.Style.STROKE

        val newPaint = Paint(selectedPel.paint)
        cvGraffitiView.pushUndoStack(FillPelStep(cvGraffitiView.getPelList(), selectedPel, oldPaint, newPaint))

        cvGraffitiView.setSelectedPel(null)
        cvGraffitiView.updateSavedBitmap()
    }

    private fun onOpenTransChildren() {
        val isEditOptionsVisible = vgEditOptions.visibility == View.GONE
        ivToggleOptions.isSelected = isEditOptionsVisible
        vgEditOptions.visibility = if (isEditOptionsVisible) View.VISIBLE else View.GONE
    }

    /**
     * 在显示/隐藏/切换菜单项前的处理
     *
     * @return false:prepare failed;true:prepare ok
     */
    private fun prepareToggleSwitchMenuAction(view: View): Boolean {
        if (cvGraffitiView.touch!!.isProcessing) {
            toast(getString(R.string.gr_task_processing))
            return false
        }
        if (cvGraffitiView.isSensorRegistered()) {
            //结束sensor绘图
            completeKeepDrawing()
        }
        val viewId = view.id
        if (viewId != R.id.rbDraw && viewId != R.id.rbBg) {
            //点击的不是弹窗按钮则隐藏弹窗
            mMainPresenter.dismissPopupWindows()
        }
        return true
    }

    private var lastMultiTouchTime = 0L

    override fun onTouchEvent(event: MotionEvent): Boolean {
        /*
        * 如果是多指触摸的话，显示/隐藏工具栏
        * */
        if (event.pointerCount > 2) {
            if (event.eventTime - lastMultiTouchTime < 1000) return true

            lastMultiTouchTime = event.eventTime
            if (vgTopMenu.visibility == View.VISIBLE) {
                closeTools()
                return true
            }
            ensurePelFinished()
            openTools()
            return true
        }
        return super.onTouchEvent(event)
    }

    companion object {
        private const val REQUEST_CODE_NONE = 0
        const val REQUEST_CODE_GRAPH = 1//拍照
        const val REQUEST_CODE_PICTURE = 2 //缩放
        const val IMAGE_UNSPECIFIED = "image/*"
    }
}