package com.ue.graffiti.ui

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.PointF
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageView
import android.widget.RadioGroup
import android.widget.Toast
import com.trello.rxlifecycle2.components.support.RxAppCompatActivity
import com.ue.graffiti.R
import com.ue.graffiti.constant.DrawPelFlags
import com.ue.graffiti.constant.SPKeys
import com.ue.graffiti.event.OnMultiTouchListener
import com.ue.graffiti.event.OnSingleResultListener
import com.ue.graffiti.event.OnStepListener
import com.ue.graffiti.helper.DialogHelper
import com.ue.graffiti.model.*
import com.ue.graffiti.touch.*
import com.ue.graffiti.util.calPelCenterPoint
import com.ue.graffiti.util.calPelSavedMatrix
import com.ue.graffiti.util.toRedoUpdate
import com.ue.graffiti.util.toUndoUpdate
import com.ue.library.util.SPUtils
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : RxAppCompatActivity(), View.OnClickListener {
    private lateinit var vgTopMenu: View
    private lateinit var vgBottomMenu: RadioGroup
    private lateinit var btnUndo: View
    private lateinit var btnRedo: View
    private lateinit var btnDraw: Button
    private lateinit var vgRightMenu: View
    private lateinit var ivToggleOptions: View
    private lateinit var vgEditOptions: View

    private lateinit var btnColor: Button

    private var curToolVi: View? = null
    private var curPelVi: ImageView? = null
    private var curCanvasBgVi: ImageView? = null
    private var whiteCanvasBgVi: ImageView? = null

    private val lastPoint = PointF()
    private var newPel: Pel? = null
    private var sensorManager: SensorManager? = null
    private var responseCount: Int = 0

    private lateinit var mMainPresenter: MainPresenter

    private val transMatrix = Matrix()
    private val savedMatrix = Matrix()
    private var savedPel: Pel? = null
    private var step: Step? = null
    private val centerPoint = PointF()
    private var lastEditActionId: Int = 0

    //单手操作传感器监听者
    private val singleHandSensorEventListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            if (curToolVi!!.id != R.id.btnDraw) {
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
        setContentView(R.layout.activity_main)

        mMainPresenter = MainPresenter(this)
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        initViews()

        DialogHelper.showOnceHintDialog(this, R.string.draw_gesture_title, R.string.draw_gesture_tip, R.string.got_it, SPKeys.SHOW_DRAW_GESTURE_HINT)
    }

    private fun initViews() {
        btnDraw = findViewById(R.id.btnDraw)
        vgTopMenu = findViewById(R.id.vgTopMenu)
        vgBottomMenu = findViewById(R.id.vgBottomMenu)
        btnUndo = findViewById(R.id.ivUndo)
        btnRedo = findViewById(R.id.ivRedo)
        vgRightMenu = findViewById(R.id.vgRightMenu)
        ivToggleOptions = findViewById(R.id.ivToggleOptions)
        vgEditOptions = findViewById(R.id.vgEditOptions)
        btnColor = findViewById(R.id.btnColor)

        vgBottomMenu.check(R.id.btnDraw)
        ivToggleOptions.isSelected = true
        curToolVi = btnDraw

        val lastColor = SPUtils.getInt(SPKeys.SP_PAINT_COLOR, resources.getColor(R.color.col_298ecb))
        btnColor!!.setTextColor(lastColor)
        cvGraffitiView.paintColor = lastColor

        whiteCanvasBgVi = mMainPresenter.initCanvasBgsPopupWindow(R.layout.popup_canvas_bgs, R.id.vgCanvasBgs, object : View.OnClickListener {
            override fun onClick(v: View?) {
                updateCanvasBgAndIcons(v as ImageView)
            }
        })

        curCanvasBgVi = whiteCanvasBgVi
        curPelVi = mMainPresenter.initPelsPopupWindow(R.layout.popup_pels, R.id.vgPels, cvGraffitiView, object : MainPresenter.OnPickPelListener {
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
        btnUndo.setOnClickListener(this)
        btnRedo.setOnClickListener(this)

        mMainPresenter.setListenerForChildren(R.id.vgTopMenu, this)
        mMainPresenter.setListenerForChildren(R.id.vgBottomMenu, this)
        mMainPresenter.setListenerForChildren(R.id.vgRightMenu, View.OnClickListener { v -> onSwitchEditMenuAction(v) })

        cvGraffitiView.setMultiTouchListener(object : OnMultiTouchListener {
            override fun onMultiTouch() {
                if (vgTopMenu.visibility == View.VISIBLE) {
                    closeTools()
                    return
                }
                ensurePelFinished()
                openTools()
            }
        })
    }

    private fun openTools() {
        if (vgRightMenu!!.visibility == View.VISIBLE) {
            vgRightMenu!!.visibility = View.GONE
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

        val pelTouch = mMainPresenter.getPelTouchByViewId(curPelVi!!.id, cvGraffitiView)
        registerKeepDrawingSensor(pelTouch)

        cvGraffitiView.touch = pelTouch
    }

    private fun registerKeepDrawingSensor(pelTouch: Touch?) {
        if (pelTouch !is KeepDrawingTouch) {
            return
        }
        pelTouch.setKeepDrawingListener(object : KeepDrawingTouch.KeepDrawingTouchListener {
            override fun onDownPoint(downPoint: PointF) {
                lastPoint.set(downPoint)
            }

            override fun registerKeepDrawingSensor() {
                this@MainActivity.registerKeepDrawingSensor()
            }
        })
    }

    private fun toggleMenuVisibility(isVisible: Boolean) {
        val visibility = if (isVisible) View.VISIBLE else View.GONE
        val animations = mMainPresenter.getToggleAnimations(isVisible)!!

        btnRedo.startAnimation(animations[0])
        vgTopMenu.startAnimation(animations[1])
        btnUndo.startAnimation(animations[2])
        vgBottomMenu.startAnimation(animations[3])

        vgBottomMenu.visibility = visibility
        vgTopMenu.visibility = visibility
        btnUndo.visibility = visibility
        btnRedo.visibility = visibility
    }

    private fun onOpenTransBarBtn(v: View) {
        mMainPresenter.dismissPopupWindows()
        curToolVi = v
        closeTools()

        if (curToolVi!!.id == R.id.btnEdit) {
            val leftAppearAnim = AnimationUtils.loadAnimation(this, R.anim.leftappear)
            vgRightMenu!!.visibility = View.VISIBLE
            ivToggleOptions.visibility = View.VISIBLE
            vgRightMenu!!.startAnimation(leftAppearAnim)
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
        curPelVi!!.setImageDrawable(null)
        v.setImageResource(R.drawable.bg_highlight_frame)
        curPelVi = v

        val fatherDrawable = resources.getDrawable(mMainPresenter.getDrawRes(v.id))
        btnDraw!!.setCompoundDrawablesWithIntrinsicBounds(null, fatherDrawable, null, null)
    }

    private fun updateCanvasBgAndIcons(v: View?) {
        curCanvasBgVi!!.setImageDrawable(null)
        curCanvasBgVi = v as ImageView?
        curCanvasBgVi!!.setImageResource(R.drawable.bg_highlight_frame)

        val backgroundDrawable = mMainPresenter.getBgSelectedRes(v!!.id)
        if (backgroundDrawable != 0) {
            cvGraffitiView.setBackgroundBitmap(backgroundDrawable)
        }
    }

    //确保未画完的图元能够真正敲定
    private fun ensurePelFinished() {
        val selectedPel = cvGraffitiView.getSelectedPel() ?: return
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

        newPel!!.region.setPath(newPel!!.path, cvGraffitiView.clipRegion)
        newPel!!.paint.set(cvGraffitiView.getCurrentPaint())

        cvGraffitiView.addPel(newPel!!)

        cvGraffitiView.pushUndoStack(DrawPelStep(DrawPelFlags.DRAW, cvGraffitiView.getPelList(), newPel!!))

        cvGraffitiView.setSelectedPel(null)
        cvGraffitiView.updateSavedBitmap()
    }

    private fun registerKeepDrawingSensor() {
        newPel = Pel()
        newPel!!.closure = true
        lastPoint.set(cvGraffitiView.touch!!.curPoint)
        newPel!!.path.moveTo(lastPoint.x, lastPoint.y)

        toggleSensor(true)
    }

    private fun toggleSensor(open: Boolean) {
        if (open) {
            val sensor = sensorManager!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            sensorManager!!.registerListener(singleHandSensorEventListener, sensor, SensorManager.SENSOR_DELAY_GAME)
        } else {
            sensorManager!!.unregisterListener(singleHandSensorEventListener)
        }
        cvGraffitiView.setSensorRegistered(open)
    }

    private fun onUndoBtn(v: View) {
        val step = cvGraffitiView.popUndoStack() ?: return

        cvGraffitiView.touch!!.setProcessing(true, getString(R.string.undoing))
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
        cvGraffitiView.touch!!.setProcessing(true, getString(R.string.redoing))
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
            cvGraffitiView.touch!!.setProcessing(true, getString(R.string.loading))
            mMainPresenter.loadCapturePhoto()
                    .subscribe { bitmap ->
                        cvGraffitiView.touch!!.setProcessing(false, null)
                        cvGraffitiView.setBackgroundBitmap(bitmap)
                    }
            return
        }
        // 触发图库模式的“选择”、“返键”按钮后
        if (requestCode == REQUEST_CODE_PICTURE) {
            cvGraffitiView.touch!!.setProcessing(true, getString(R.string.loading))
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
            DialogHelper.showInputDialog(this@MainActivity, getString(R.string.input_graffiti_name), object : OnSingleResultListener {
                override fun onResult(result: Any) {
                    mMainPresenter.onSaveGraffitiClicked(cvGraffitiView.savedBitmap!!, result as String, View.OnClickListener { finish() })
                }
            })
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
            R.id.btnColor -> DialogHelper.showColorPickerDialog(this@MainActivity, object : ColorPickerDialog.OnColorPickerListener {
                override fun onColorPicked(color: Int) {
                    SPUtils.putInt(SPKeys.SP_PAINT_COLOR, color)
                    btnColor!!.setTextColor(color)
                    cvGraffitiView.paintColor = color
                }
            })
            R.id.btnPen -> DialogHelper.showPenDialog(this@MainActivity, cvGraffitiView.getCurrentPaint())
            R.id.btnClear -> DialogHelper.showClearDialog(this, object : DialogInterface.OnClickListener {
                override fun onClick(dialog: DialogInterface?, which: Int) {
                    //清空内部所有数据
                    cvGraffitiView.clearData()
                    //画布背景图标复位
                    updateCanvasBgAndIcons(whiteCanvasBgVi)
                    //清除填充过颜色的地方
                    cvGraffitiView.setBackgroundBitmap()
                }
            })
            R.id.btnSave -> DialogHelper.showInputDialog(this, getString(R.string.input_graffiti_name), object : OnSingleResultListener {
                override fun onResult(result: Any) {
                    mMainPresenter.onSaveGraffitiClicked(cvGraffitiView.savedBitmap!!, result as String, null)
                }
            })
        /*
            * bottom menu listener
            * */
            R.id.btnDraw -> onOpenPelBarBtn(v)
            R.id.btnEdit -> onOpenTransBarBtn(v)
            R.id.btnFill -> {
                curToolVi = v
                cvGraffitiView.touch = CrossFillTouch(cvGraffitiView)
            }
            R.id.btnBg -> mMainPresenter.showCanvasBgsPopupWindow(vgBottomMenu)
            R.id.btnText -> onOpenDrawTextBtn(v)
            R.id.btnInsertPicture -> onOpenDrawPictureBtn(v)
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
            Toast.makeText(this@MainActivity, R.string.select_pel_first, Toast.LENGTH_LONG).show()
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
        savedPel = Pel()
        savedPel!!.path.set(selectedPel.path)
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
        val isEditOptionsVisible = vgEditOptions!!.visibility == View.GONE
        ivToggleOptions.isSelected = isEditOptionsVisible
        vgEditOptions!!.visibility = if (isEditOptionsVisible) View.VISIBLE else View.GONE
    }

    /**
     * 在显示/隐藏/切换菜单项前的处理
     *
     * @return false:prepare failed;true:prepare ok
     */
    private fun prepareToggleSwitchMenuAction(view: View): Boolean {
        if (cvGraffitiView.touch!!.isProcessing) {
            Toast.makeText(this, getString(R.string.task_processing), Toast.LENGTH_SHORT).show()
            return false
        }
        if (cvGraffitiView.isSensorRegistered()) {
            //结束sensor绘图
            completeKeepDrawing()
        }
        val viewId = view.id
        if (viewId != R.id.btnDraw && viewId != R.id.btnBg) {
            //点击的不是弹窗按钮则隐藏弹窗
            mMainPresenter.dismissPopupWindows()
        }
        return true
    }

    companion object {
        private val REQUEST_CODE_NONE = 0
        val REQUEST_CODE_GRAPH = 1//拍照
        val REQUEST_CODE_PICTURE = 2 //缩放
        val IMAGE_UNSPECIFIED = "image/*"
    }
}