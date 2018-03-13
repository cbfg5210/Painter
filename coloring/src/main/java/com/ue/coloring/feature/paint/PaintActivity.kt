package com.ue.coloring.feature.paint

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.afollestad.materialdialogs.MaterialDialog
import com.ue.adapterdelegate.OnDelegateClickListener
import com.ue.coloring.R
import com.ue.coloring.factory.DialogHelper
import com.ue.coloring.widget.ColourImageView
import com.ue.coloring.widget.TipDialog
import com.ue.library.constant.FileTypes
import com.ue.library.util.*
import com.ue.library.widget.colorpicker.ColorPicker
import com.ue.library.widget.colorpicker.SatValView
import kotlinx.android.synthetic.main.co_activity_paint.*


class PaintActivity : AppCompatActivity(), View.OnClickListener {
    private var isFromThemes: Boolean = false
    private var picturePath: String = ""
    private var pictureName: String = ""
    private var savedPicturePath: String = ""
    private var savedBorderPicturePath: String = ""

    private lateinit var mDialogHelper: DialogHelper
    private lateinit var presenter: PaintPresenter
    private lateinit var tipDialog: TipDialog

    private lateinit var pickedColorAdapter: PickedColorAdapter
    private var hasSaved = true

    private lateinit var cpPaletteColorPicker: ColorPicker

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.co_activity_paint)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        isFromThemes = intent.getBooleanExtra(ARG_IS_FROM_THEMES, true)
        pictureName = intent.getStringExtra(ARG_PICTURE_NAME)
        picturePath = intent.getStringExtra(ARG_PICTURE_PATH)

        presenter = PaintPresenter(this)
        tipDialog = TipDialog.newInstance()
        mDialogHelper = DialogHelper(this)

        mDialogHelper.showEnterHintDialog()

        cpPaletteColorPicker = ColorPicker(this, Color.BLACK, object : SatValView.OnColorChangeListener {
            override fun onColorChanged(newColor: Int) {
                changeCurrentColor(newColor)
            }
        })

        initViews()
        loadPicture()
    }

    override fun onDestroy() {
        super.onDestroy()
        civColoring.onRecycleBitmaps()
    }

    private fun loadPicture() {
        tipDialog.showTip(supportFragmentManager, getString(R.string.co_load_picture))

        ImageLoaderUtils.display(civColoring, picturePath, 0, getString(R.string.co_load_picture_failed), object : ImageLoaderUtils.ImageLoaderCallback2 {
            override fun onBitmapResult(bitmap: Bitmap?) {
                tipDialog.dismiss()
                bitmap ?: finish()
            }
        })
    }

    private fun initViews() {
        undo.isEnabled = false
        redo.isEnabled = false
        ivToggleActionBar.isSelected = true

        initBottomColors()

        undo.setOnClickListener(this)
        redo.setOnClickListener(this)
        tvTogglePalette.setOnClickListener(this)
        ivToggleActionBar.setOnClickListener(this)
        tvAfterEffect.setOnClickListener(this)

        tvPickColor.setOnClickListener(this)
        tvGradient.setOnClickListener(this)

        civColoring.setOnRedoUndoListener(object : ColourImageView.OnRedoUndoListener {
            override fun onRedoUndo(undoSize: Int, redoSize: Int) {
                undo.isEnabled = undoSize != 0
                redo.isEnabled = redoSize != 0

                hasSaved = false
            }
        })
    }

    private fun initBottomColors() {
        //paintColors
        val adapter = ColorOptionAdapter(this)

        adapter.setColorSelectedListener(object : OnDelegateClickListener {
            override fun onClick(view: View, color: Int) {
                cpPaletteColorPicker.setColor(color)
                changeCurrentColor(color)
            }
        })
        rvColors.setHasFixedSize(true)
        rvColors.adapter = adapter

        //如果直接paintColors.subList(0, 8)的话，由于Int是对象类型会造成数据影响
        pickedColorAdapter = PickedColorAdapter(this)
        pickedColorAdapter.pickColorListener = object : OnDelegateClickListener {
            override fun onClick(view: View, newColor: Int) {
                changeCurrentColor(newColor)
            }
        }

        rvPickedColors.setHasFixedSize(true)
        rvPickedColors.adapter = pickedColorAdapter

        //初始化选中的颜色,especial for picker palette
        changeCurrentColor(pickedColorAdapter.getPickedColor())
    }

    private fun onPickColorCheckChanged(isChecked: Boolean) {
        tvPickColor.isSelected = isChecked

        if (!isChecked) {
            backToColorModel()
            return
        }

        mDialogHelper.showPickColorHintDialog()
        civColoring.model = ColourImageView.Model.PICKCOLOR
        civColoring.setOnColorPickListener(object : ColourImageView.OnColorPickListener {
            override fun onColorPick(status: Boolean, color: Int) {
                if (status) {
                    changeCurrentColor(color)
                    onPickColorCheckChanged(false)
                } else {
                    toast(R.string.co_pick_color_error)
                }
            }
        })
    }

    private fun onJianBianColorCheckChanged(checked: Boolean) {
        tvGradient.isSelected = checked

        if (checked) {
            mDialogHelper.showGradualHintDialog()
            civColoring.model = ColourImageView.Model.FILLGRADUALCOLOR
            tvGradient.setText(R.string.co_gradual_color)
        } else {
            civColoring.model = ColourImageView.Model.FILLCOLOR
            tvGradient.setText(R.string.co_normal_color)
        }
    }

    override fun onClick(view: View) {
        val viewId = view.id
        when (viewId) {
            R.id.undo -> civColoring.undo()
            R.id.redo -> civColoring.redo()
            R.id.tvAfterEffect -> mDialogHelper.showEffectHintDialog(View.OnClickListener { saveToLocal(FLAG_EFFECT) })
            R.id.tvPickColor -> onPickColorCheckChanged(!tvPickColor.isSelected)
            R.id.tvGradient -> onJianBianColorCheckChanged(!tvGradient.isSelected)

            R.id.tvTogglePalette -> cpPaletteColorPicker.show(tvTogglePalette)

            R.id.ivToggleActionBar -> {
                supportActionBar?.apply {
                    ivToggleActionBar.isSelected = isShowing
                    if (ivToggleActionBar.isSelected) show()
                    else hide()
                }
            }
        }
    }

    private fun backToColorModel() {
        civColoring.model = ColourImageView.Model.FILLCOLOR
        onJianBianColorCheckChanged(false)
    }

    private fun changeCurrentColor(newColor: Int) {
        onPickColorCheckChanged(false)
        pickedColorAdapter.updateColor(newColor)

        cpPaletteColorPicker.setColor(newColor)
        civColoring.setColor(newColor)
    }

    private fun saveToLocal(saveFlag: Int) {
        saveToLocal(saveFlag, null, null)
    }

    private fun saveToLocal(saveFlag: Int, bitmap: Bitmap?, listener: PaintPresenter.OnSaveImageListener?) {
        tipDialog.showTip(supportFragmentManager, getString(R.string.co_saving_image))

        val picName =
                if (isFromThemes) "${pictureName.replace(FileTypes.PNG, "_")}fc${FileTypes.PNG}"
                else pictureName

        val bitmapToSave = bitmap ?: civColoring.getBitmap()!!
        val saveListener = object : PaintPresenter.OnSaveImageListener {
            override fun onSaved(path: String) {
                tipDialog.dismiss()
                if (TextUtils.isEmpty(path)) {
                    toast(R.string.co_save_failed)
                    return
                }
                savedPicturePath = path
                hasSaved = true
                toast(getString(R.string.co_save_success, path))

                if (listener != null) {
                    listener.onSaved(path)
                    return
                }

                if (saveFlag == FLAG_EXIT) finish()
                else if (saveFlag == FLAG_EFFECT) showEffectDialog(path)
                else if (saveFlag == FLAG_SHARE)
                    IntentUtils.shareImage(
                            this@PaintActivity,
                            getString(R.string.module_coloring),
                            getString(R.string.co_share_my_work) + getString(R.string.co_share_content),
                            path)
            }
        }

        presenter.saveImageLocally(bitmapToSave, picName, saveListener)
    }

    private fun showEffectDialog(path: String) {
        val dialog = AfterEffectDialog.newInstance(path)
        dialog.setEffectListener(object : PaintPresenter.OnSaveImageListener {
            override fun onSaved(path: String) {
                savedBorderPicturePath = "file://$path"
                repaint(false, savedBorderPicturePath)
            }
        })
        dialog.show(supportFragmentManager, "")
    }

    private fun repaint(isDelete: Boolean) {
        repaint(isDelete, null)
    }

    private fun repaint(isDelete: Boolean, path: String?) {
        if (isDelete && isFromThemes) {
            if (!TextUtils.isEmpty(savedPicturePath)) {
                if (FileUtils.deleteFile(savedPicturePath)) {
                    savedPicturePath = ""
                    toast(R.string.co_delete_completed)
                } else {
                    toast(R.string.co_delete_paint_failed)
                }
            }
        }
        tipDialog.showTip(supportFragmentManager, getString(R.string.co_load_picture))
        civColoring.clearStack()
        hasSaved = true

        ImageLoaderUtils.display(civColoring, path
                ?: picturePath, 0, getString(R.string.co_load_picture_failed), object : ImageLoaderUtils.ImageLoaderCallback2 {
            override fun onBitmapResult(bitmap: Bitmap?) {
                tipDialog.dismiss()
                bitmap ?: finish()
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.co_menu_paint, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> onBackPressed()
            R.id.menuSave -> saveToLocal(FLAG_SAVE)
            R.id.menuShare -> saveToLocal(FLAG_SHARE)
            R.id.menuDelete -> mDialogHelper.showRepaintDialog(MaterialDialog.SingleButtonCallback { _, _ -> repaint(true) })
        }
        return true
    }

    override fun onPause() {
        super.onPause()
        pickedColorAdapter.savePickedColors()
    }

    override fun onBackPressed() {
        if (hasSaved || !civColoring.isUndoable()) {
            super.onBackPressed()
            return
        }

        DialogUtils.showExitDialogWithCheck(this, getString(R.string.co_save_work), true,
                MaterialDialog.SingleButtonCallback { dialog, _ -> if (dialog.isPromptCheckBoxChecked) saveToLocal(FLAG_EXIT) else finish() })
    }

    companion object {
        private const val ARG_IS_FROM_THEMES = "arg_is_from_themes"
        private const val ARG_PICTURE_NAME = "arg_picture_name"
        private const val ARG_PICTURE_PATH = "arg_picture_path"
        private const val FLAG_SAVE = 0
        private const val FLAG_EXIT = 1
        private const val FLAG_SHARE = 2
        private const val FLAG_EFFECT = 3

        fun start(context: Context, isFromThemes: Boolean, pictureName: String, picturePath: String) {
            val intent = Intent(context, PaintActivity::class.java)
            intent.putExtra(ARG_IS_FROM_THEMES, isFromThemes)
            intent.putExtra(ARG_PICTURE_NAME, pictureName)
            intent.putExtra(ARG_PICTURE_PATH, picturePath)
            context.startActivity(intent)
        }
    }
}