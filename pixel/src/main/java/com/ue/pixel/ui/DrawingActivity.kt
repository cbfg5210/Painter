package com.ue.pixel.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.os.Environment
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.PopupMenu
import android.support.v7.widget.helper.ItemTouchHelper
import android.text.TextUtils
import android.util.TypedValue
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.folderselector.FileChooserDialog
import com.ue.adapterdelegate.OnDelegateClickListener
import com.ue.library.constant.Constants
import com.ue.library.constant.Modules
import com.ue.library.event.OnTextInputListener
import com.ue.library.util.*
import com.ue.library.widget.colorpicker.ColorPicker
import com.ue.library.widget.colorpicker.SatValView
import com.ue.pixel.R
import com.ue.pixel.event.ItemTouchCallback
import com.ue.pixel.event.OnItemClickListener2
import com.ue.pixel.event.OnProjectInfoListener
import com.ue.pixel.event.SimpleDragCallback
import com.ue.pixel.model.LayerThumbItem1
import com.ue.pixel.shape.EraserShape
import com.ue.pixel.shape.LineShape
import com.ue.pixel.shape.RectShape
import com.ue.pixel.util.DialogHelper
import com.ue.pixel.util.ExportUtils
import com.ue.pixel.util.ExportingUtils
import com.ue.pixel.util.Tool
import com.ue.pixel.widget.PixelCanvasView
import kotlinx.android.synthetic.main.pi_activity_drawing.*
import kotlinx.android.synthetic.main.pi_layout_main.*
import java.io.File
import java.util.*

class DrawingActivity : AppCompatActivity(), FileChooserDialog.FileCallback, ItemTouchCallback, PixelCanvasView.OnDropperCallBack {

    companion object {
        const val UNTITLED = "Untitled"
        private const val LAST_USED_COLOR = "lastUsedColor"
        private const val LAST_OPENED_PROJECT = "lastOpenedProject"
        const val SELECTED_PROJECT_PATH = "selectedProjectPath"
        private const val FILE_NAME_CHANGED = "fileNameChanged"

        val rectShapeFactory = RectShape()
        val lineShapeFactory = LineShape()
        val eraserShapeFactory = EraserShape()
        var currentProjectPath: String = ""
    }

    var isEdited = false
        set(value) {
            field = value
            tvTitle.text = fromHtml("PxerStudio<br><small><small>${projectName}${(if (value) "*" else "")}</small></small>")
        }

    private lateinit var layerAdt: LayerThumbAdapter

    private lateinit var cp: ColorPicker

    private var onlyShowSelected: Boolean = false

    //Picture property
    private var projectName = DrawingActivity.UNTITLED

    private fun setTitle(subtitle: String?, edited: Boolean) {
        tvTitle.text = fromHtml("PxerStudio<br><small><small>${if (subtitle.isNullOrEmpty()) UNTITLED else subtitle}${if (edited) "*" else ""}</small></small>")
        isEdited = edited
    }

    private lateinit var mPreviousMode: PixelCanvasView.Mode

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.pi_activity_drawing)

        setTitle(UNTITLED, false)
        tbToolbar.title = ""
        setSupportActionBar(tbToolbar)
        tvTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)

        val pxerPref = getSharedPreferences(Modules.M_PIXEL, Context.MODE_PRIVATE)
        pvPixelCanvasView.selectedColor = pxerPref.getInt(LAST_USED_COLOR, Color.YELLOW)
        pvPixelCanvasView.setDropperCallBack(this)

        setUpLayersView()
        setupControl()

        currentProjectPath = pxerPref.getString(LAST_OPENED_PROJECT)
        if (!currentProjectPath.isNullOrEmpty()) {
            File(currentProjectPath).apply {
                if (exists()) {
                    pvPixelCanvasView.loadProject(this)
                    setTitle(Tool.stripExtension(name), false)
                }
            }
        }

        if (layerAdt.itemCount == 0) {
            layerAdt.add(LayerThumbItem1(pvPixelCanvasView.pixelCanvasLayers[0].bitmap, true))
        }
        System.gc()
    }

    override fun onColorDropped(newColor: Int) {
        fabColor.setColor(newColor)
        cp.setColor(newColor)

        fabDropper.callOnClick()
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        super.onPostCreate(savedInstanceState)
    }

    fun onProjectTitleClicked(view: View) {
        openProjectManager()
    }

    fun onToggleToolsPanel(view: View) {
        if (cvToolsPanel.visibility == View.INVISIBLE) {
            cvToolsPanel.visibility = View.VISIBLE
            cvToolsPanel.animate()
                    .setDuration(100)
                    .setInterpolator(AccelerateDecelerateInterpolator())
                    .translationX(0f)
            return
        }
        cvToolsPanel.animate()
                .setDuration(100)
                .setInterpolator(AccelerateDecelerateInterpolator())
                .translationX((+cvToolsPanel.width).toFloat())
                .withAnimEndAction(Runnable { cvToolsPanel.visibility = View.INVISIBLE })
    }

    private fun setupControl() {
        cvToolsPanel.post({ cvToolsPanel.translationX = (cvToolsPanel.width).toFloat() })

        val toolAdapter = ToolAdapter(this)
        toolAdapter.itemClickListener = object : OnDelegateClickListener {
            override fun onClick(view: View, image: Int) {
                when (image) {
                    R.drawable.ic_mode_edit_24dp -> pvPixelCanvasView.mode = PixelCanvasView.Mode.Normal
                    R.drawable.ic_fill_24dp -> pvPixelCanvasView.mode = PixelCanvasView.Mode.Fill
                    R.drawable.ic_eraser_24dp -> {
                        pvPixelCanvasView.mode = PixelCanvasView.Mode.ShapeTool
                        pvPixelCanvasView.shapeTool = eraserShapeFactory
                    }
                    R.drawable.ic_line_24dp -> {
                        pvPixelCanvasView.mode = PixelCanvasView.Mode.ShapeTool
                        pvPixelCanvasView.shapeTool = lineShapeFactory
                    }
                    R.drawable.ic_square_24dp -> {
                        pvPixelCanvasView.mode = PixelCanvasView.Mode.ShapeTool
                        pvPixelCanvasView.shapeTool = rectShapeFactory
                    }
                }
            }
        }
        (rvToolsList.layoutManager as LinearLayoutManager).reverseLayout = true
        rvToolsList.adapter = toolAdapter

        fabColor.setColor(pvPixelCanvasView.selectedColor)
        fabColor.colorNormal = pvPixelCanvasView.selectedColor
        fabColor.colorPressed = pvPixelCanvasView.selectedColor
        cp = ColorPicker(this, pvPixelCanvasView.selectedColor, object : SatValView.OnColorChangeListener {
            override fun onColorChanged(newColor: Int) {
                pvPixelCanvasView.selectedColor = newColor
                fabColor.setColor(newColor)
            }
        })
        fabColor.setOnClickListener { view -> cp.show(view) }
        fabUndo.setOnClickListener { pvPixelCanvasView.undo() }
        fabRedo.setOnClickListener { pvPixelCanvasView.redo() }
        fabDropper.setOnClickListener {
            if (pvPixelCanvasView.mode == PixelCanvasView.Mode.Dropper) {
                fabUndo.show(true)
                fabRedo.show(true)
                fabCurrentTool.show(true)

                pvPixelCanvasView.mode = mPreviousMode

                fabDropper.setImageResource(R.drawable.ic_colorize_24dp)
            } else {
                fabUndo.hide(true)
                fabRedo.hide(true)
                fabCurrentTool.hide(true)

                if (cvToolsPanel.visibility == View.VISIBLE) fabCurrentTool.callOnClick()

                mPreviousMode = pvPixelCanvasView.mode
                pvPixelCanvasView.mode = PixelCanvasView.Mode.Dropper

                fabDropper.setImageResource(R.drawable.ic_close_24dp)
            }
        }
    }

    private fun setUpLayersView() {
        val layersBtn = cvAddLayerPanel

        layersBtn.setOnClickListener {
            pvPixelCanvasView.addLayer()

            val currentLayer = pvPixelCanvasView.currentLayer
            layerAdt.add(Math.max(currentLayer, 0), LayerThumbItem1(pvPixelCanvasView.pixelCanvasLayers[currentLayer].bitmap, true))

            rvLayerList.invalidate()
        }

        layerAdt = LayerThumbAdapter(this)

        rvLayerList.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        rvLayerList.adapter = layerAdt

        rvLayerList.itemAnimator = DefaultItemAnimator()
        rvLayerList.itemAnimator.changeDuration = 0
        rvLayerList.itemAnimator.addDuration = 0
        rvLayerList.itemAnimator.removeDuration = 0

        val touchCallback = SimpleDragCallback(this)
        val touchHelper = ItemTouchHelper(touchCallback)
        touchHelper.attachToRecyclerView(rvLayerList)

        layerAdt.itemClickListener = object : OnItemClickListener2<LayerThumbItem1> {
            override fun onItemClick(view: View, item: LayerThumbItem1, position: Int) {
                if (onlyShowSelected) {
                    val layer = pvPixelCanvasView.pixelCanvasLayers[pvPixelCanvasView.currentLayer]
                    layer.visible = false
                    pvPixelCanvasView.invalidate()

                    layerAdt.notifyItemVisible(position, false)
                }
                pvPixelCanvasView.currentLayer = position
                if (onlyShowSelected) {
                    val layer = pvPixelCanvasView.pixelCanvasLayers[pvPixelCanvasView.currentLayer]
                    layer.visible = true
                    pvPixelCanvasView.invalidate()

                    layerAdt.notifyItemVisible(position, true)
                }
                item.pressed()
                if (item.isPressSecondTime) {
                    val popupMenu = PopupMenu(this@DrawingActivity, view)
                    popupMenu.inflate(R.menu.menu_popup_layer)
                    popupMenu.setOnMenuItemClickListener { clickedItem ->
                        this@DrawingActivity.onOptionsItemSelected(clickedItem)
                        false
                    }
                    popupMenu.show()
                }
            }
        }
    }

    override fun itemTouchOnMove(oldPosition: Int, newPosition: Int): Boolean {
        if (!isEdited) isEdited = true

        pvPixelCanvasView.moveLayer(oldPosition, newPosition)

        if (oldPosition < newPosition) {
            for (i in oldPosition + 1..newPosition) {
                Collections.swap(layerAdt.items, i, i - 1)
                layerAdt.notifyItemMoved2(i, i - 1)
            }
        } else {
            for (i in oldPosition - 1 downTo newPosition) {
                Collections.swap(layerAdt.items, i, i + 1)
                layerAdt.notifyItemMoved2(i, i + 1)
            }
        }

        return true
    }

    override fun itemTouchDropped(oldPosition: Int, newPosition: Int) {
        pvPixelCanvasView.currentLayer = newPosition
    }

    fun onLayerUpdate() {
        layerAdt.items.clear()
        for (i in 0 until pvPixelCanvasView.pixelCanvasLayers.size) {
            layerAdt.items.add(LayerThumbItem1(pvPixelCanvasView.pixelCanvasLayers[i].bitmap, true))
        }
        layerAdt.items[0].visible = true
        layerAdt.select(0)
    }

    fun onLayerRefresh() {
        rvLayerList?.invalidate()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_activity_drawing, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.onlyshowselectedlayer -> {
                onlyShowSelected = true
                pvPixelCanvasView.visibilityAllLayer(false)

                val layer2 = pvPixelCanvasView.pixelCanvasLayers[pvPixelCanvasView.currentLayer]
                layer2.visible = true
                pvPixelCanvasView.invalidate()

                layerAdt.notifyItemVisible(pvPixelCanvasView.currentLayer, true)
            }
            R.id.export -> ExportUtils.exportAsPng(this, projectName, pvPixelCanvasView)
            R.id.exportgif -> ExportUtils.exportAsGif(this, projectName, pvPixelCanvasView)
            R.id.save -> save(true)
            R.id.projectm -> openProjectManager()
            R.id.open -> FileChooserDialog.Builder(this)
                    .initialPath(ExportingUtils.projectPath)
                    .extensionsFilter(PixelCanvasView.PIXEL_EXTENSION_NAME)
                    .goUpLabel(".../")
                    .show(this)
            R.id.newp -> createNewProject()
            R.id.resetvp -> pvPixelCanvasView.resetViewPort()
            R.id.hidealllayers -> run {
                if (onlyShowSelected) return@run
                pvPixelCanvasView.visibilityAllLayer(false)

                layerAdt.setAllVisibility(false)
            }
            R.id.showalllayers -> {
                onlyShowSelected = false
                pvPixelCanvasView.visibilityAllLayer(true)

                layerAdt.setAllVisibility(true)
            }
            R.id.gridonoff -> {
                if (pvPixelCanvasView.isShowGrid)
                    item.setIcon(R.drawable.ic_grid_on_24dp)
                else
                    item.setIcon(R.drawable.ic_grid_off_24dp)
                pvPixelCanvasView.isShowGrid = !pvPixelCanvasView.isShowGrid
            }
            R.id.layers -> {
                cvLayersPanel.pivotX = (cvLayersPanel.width / 2).toFloat()
                cvLayersPanel.pivotY = 0f
                if (cvLayersPanel.visibility == View.VISIBLE) {
                    cvLayersPanel.animate()
                            .setDuration(100)
                            .setInterpolator(AccelerateDecelerateInterpolator())
                            .alpha(0f)
                            .scaleX(0.85f)
                            .scaleY(0.85f)
                            .withAnimEndAction(Runnable { cvLayersPanel.visibility = View.INVISIBLE })
                } else {
                    cvLayersPanel.visibility = View.VISIBLE
                    cvLayersPanel.animate()
                            .setDuration(100)
                            .setInterpolator(AccelerateDecelerateInterpolator())
                            .scaleX(1f)
                            .scaleY(1f)
                            .alpha(1f)
                }
            }
            R.id.deletelayer -> run {
                if (pvPixelCanvasView.pixelCanvasLayers.size <= 1) return@run
                DialogHelper.showDeleteLayerDialog(this, MaterialDialog.SingleButtonCallback { _, _ ->
                    if (!isEdited) isEdited = true

                    layerAdt.removeAt(pvPixelCanvasView.currentLayer)
                    pvPixelCanvasView.removeCurrentLayer()
                    layerAdt.select(pvPixelCanvasView.currentLayer)
                })
            }
            R.id.copypastelayer -> {
                pvPixelCanvasView.copyAndPasteCurrentLayer()

                layerAdt.add(Math.max(pvPixelCanvasView.currentLayer, 0), LayerThumbItem1(pvPixelCanvasView.pixelCanvasLayers[pvPixelCanvasView.currentLayer].bitmap, true))
                layerAdt.select(pvPixelCanvasView.currentLayer)
                rvLayerList.invalidate()

            }
            R.id.mergealllayer -> run {
                if (pvPixelCanvasView.pixelCanvasLayers.size <= 1) return@run
                DialogHelper.showMergeAllLayersDialog(this, MaterialDialog.SingleButtonCallback { _, _ ->
                    if (!isEdited) isEdited = true

                    pvPixelCanvasView.mergeAllLayers()
                    layerAdt.items.clear()
                    layerAdt.add(LayerThumbItem1(pvPixelCanvasView.pixelCanvasLayers[pvPixelCanvasView.currentLayer].bitmap, true))
                    layerAdt.select(0)
                })
            }
            R.id.tvisibility -> run {
                if (onlyShowSelected) return@run
                val layer = pvPixelCanvasView.pixelCanvasLayers[pvPixelCanvasView.currentLayer]
                layer.visible = !layer.visible
                pvPixelCanvasView.invalidate()

                layerAdt.notifyItemVisible(pvPixelCanvasView.currentLayer, layer.visible)
            }
            R.id.clearlayer ->
                DialogHelper.showClearLayerDialog(this, MaterialDialog.SingleButtonCallback { _, _ ->
                    pvPixelCanvasView.clearCurrentLayer()
                })
            R.id.mergedown -> run {
                if (pvPixelCanvasView.currentLayer == pvPixelCanvasView.pixelCanvasLayers.size - 1) return@run

                DialogHelper.showMergeDownDialog(this, MaterialDialog.SingleButtonCallback { _, _ ->
                    pvPixelCanvasView.mergeDownLayer()

                    layerAdt.removeAt(pvPixelCanvasView.currentLayer + 1)
                    layerAdt.select(pvPixelCanvasView.currentLayer)
                })
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun openProjectManager() {
        save(false)
        startActivityForResult(Intent(this, ProjectManagerActivity::class.java), 659)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && requestCode == 659 && data != null) {
            val path = data.getStringExtra(SELECTED_PROJECT_PATH)
            if (!TextUtils.isEmpty(path)) {
                currentProjectPath = path
                File(path).apply {
                    if (exists()) {
                        if (pvPixelCanvasView.loadProject(this)) projectName = Tool.stripExtension(name)
                        setTitle(Tool.stripExtension(name), false)
                    }
                }
            } else if (data.getBooleanExtra(FILE_NAME_CHANGED, false)) {
                currentProjectPath = ""
                projectName = ""
                recreate()
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun createNewProject() {
        DialogHelper.showNewProjectDialog(this, object : OnProjectInfoListener {
            override fun onProjectInfo(name: String, width: Int, height: Int) {
                if (name.isEmpty()) {
                    toast(R.string.pi_project_name_null)
                    return
                }
                projectName = name
                setTitle(projectName, true)
                pvPixelCanvasView.createBlankProject(width, height)
            }
        })

        save(false)
    }

    override fun onFileSelection(dialog: FileChooserDialog, file: File) {
        pvPixelCanvasView.loadProject(file)
        setTitle(Tool.stripExtension(file.name), false)
        currentProjectPath = file.path
    }

    override fun onFileChooserDismissed(dialog: FileChooserDialog) {
    }

    override fun onStop() {
        saveState()
        super.onStop()
    }

    private fun saveState() {
        val pxerPref = getSharedPreferences(Modules.M_PIXEL, Context.MODE_PRIVATE)
        pxerPref.edit()
                .putString(LAST_OPENED_PROJECT, currentProjectPath)
                .putInt(LAST_USED_COLOR, pvPixelCanvasView.selectedColor)
                .apply()

        if (!projectName.isNullOrEmpty() || projectName != UNTITLED) save(false)
        else save(true)
    }

    private fun save(force: Boolean): Boolean {
        if (projectName.isEmpty()) {
            if (force) DialogHelper.showSaveProjectDialog(this, object : OnTextInputListener {
                override fun onTextInput(text: String) {
                    projectName = text
                    setTitle(projectName, false)
                    save(true)
                }
            })
            return false
        }
        isEdited = false
        val gson = GsonHolder.gson
        val out = ArrayList<PixelCanvasView.PixelLayer>()
        val pxerLayers = pvPixelCanvasView.pixelCanvasLayers
        for (i in pxerLayers.indices) {
            val pxableLayer = PixelCanvasView.PixelLayer()
            pxableLayer.height = pvPixelCanvasView.picHeight
            pxableLayer.width = pvPixelCanvasView.picWidth
            pxableLayer.visible = pxerLayers[i].visible
            out.add(pxableLayer)
            for (x in 0 until pxerLayers[i].bitmap.width) {
                for (y in 0 until pxerLayers[i].bitmap.height) {
                    val pc = pxerLayers[i].bitmap.getPixel(x, y)
                    if (pc != Color.TRANSPARENT) {
                        out[i].pxers.add(PixelCanvasView.Pixel(x, y, pc))
                    }
                }
            }
        }
        currentProjectPath = Environment.getExternalStorageDirectory().path + Constants.PATH_PIXEL + (projectName + ".pxer")
        setTitle(projectName, false)
        Tool.saveProject(projectName + PixelCanvasView.PIXEL_EXTENSION_NAME, gson.toJson(out))
        return true
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        cp.onConfigChanges()
    }
}