package com.ue.fingercoloring.feature.paint

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.squareup.picasso.Picasso
import com.ue.adapterdelegate.OnDelegateClickListener
import com.ue.fingercoloring.R
import com.ue.fingercoloring.factory.DialogHelper
import com.ue.fingercoloring.util.FileUtils
import com.ue.fingercoloring.widget.ColorPicker
import com.ue.fingercoloring.widget.ColourImageView
import com.ue.fingercoloring.widget.TipDialog
import com.ue.library.event.SimpleTarget
import com.ue.library.util.PicassoUtils
import com.ue.library.util.ShareImageUtil
import kotlinx.android.synthetic.main.activity_paint.*


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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_paint)

        val intent = intent
        isFromThemes = intent.getBooleanExtra(ARG_IS_FROM_THEMES, true)
        pictureName = intent.getStringExtra(ARG_PICTURE_NAME)
        picturePath = intent.getStringExtra(ARG_PICTURE_PATH)

        presenter = PaintPresenter(this)
        tipDialog = TipDialog.newInstance()
        mDialogHelper = DialogHelper(this)

        setTitle(R.string.finger_coloring)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        mDialogHelper.showEnterHintDialog()

        initViews()
        loadPicture()
    }

    override fun onDestroy() {
        super.onDestroy()
        civColoring.onRecycleBitmaps()
    }

    private fun loadPicture() {
        tipDialog.showTip(supportFragmentManager, getString(R.string.loadpicture))
        PicassoUtils.displayImage(this, civColoring, picturePath, object : SimpleTarget() {
            override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom) {
                tipDialog.dismiss()
            }

            override fun onBitmapFailed(errorDrawable: Drawable?) {
                tipDialog.dismiss()
                Toast.makeText(this@PaintActivity, getString(R.string.loadpicturefailed), Toast.LENGTH_SHORT).show()
                finish()
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

        cpPaletteColorPicker.setOnChangedListener(object : ColorPicker.OnColorChangedListener {
            override fun colorChangedListener(color: Int) {
                changeCurrentColor(color)
            }
        })

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
                cpPaletteColorPicker.color = color
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
                    Toast.makeText(this@PaintActivity, getString(R.string.pickcolorerror), Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun onJianBianColorCheckChanged(checked: Boolean) {
        tvGradient.isSelected = checked

        if (checked) {
            mDialogHelper.showGradualHintDialog()
            civColoring.model = ColourImageView.Model.FILLGRADUALCOLOR
            tvGradient.setText(R.string.jianbian_color)
        } else {
            civColoring.model = ColourImageView.Model.FILLCOLOR
            tvGradient.setText(R.string.normal_color)
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

            R.id.tvTogglePalette ->
                cpPaletteColorPicker.visibility =
                        if (cpPaletteColorPicker.visibility == View.VISIBLE) View.GONE
                        else View.VISIBLE

            R.id.ivToggleActionBar -> {
                supportActionBar ?: return
                ivToggleActionBar.isSelected = !supportActionBar!!.isShowing
                if (ivToggleActionBar.isSelected) supportActionBar!!.show()
                else supportActionBar!!.hide()
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

        cpPaletteColorPicker.color = newColor
        civColoring.setColor(newColor)
    }

    private fun saveToLocal(saveFlag: Int) {
        saveToLocal(saveFlag, null, null)
    }

    private fun saveToLocal(saveFlag: Int, bitmap: Bitmap?, listener: PaintPresenter.OnSaveImageListener?) {
        tipDialog.showTip(supportFragmentManager, getString(R.string.savingimage))

        val picName =
                if (isFromThemes) pictureName.replace(".png", "_") + "fc.png"
                else pictureName

        val bitmapToSave = bitmap ?: civColoring.getBitmap()!!
        val saveListener = object : PaintPresenter.OnSaveImageListener {
            override fun onSaved(path: String) {
                tipDialog.dismiss()
                if (TextUtils.isEmpty(path)) {
                    Toast.makeText(this@PaintActivity, getString(R.string.saveFailed), Toast.LENGTH_SHORT).show()
                    return
                }
                savedPicturePath = path
                hasSaved = true
                Toast.makeText(this@PaintActivity, getString(R.string.saveSuccess) + path, Toast.LENGTH_SHORT).show()

                if (listener != null) {
                    listener.onSaved(path)
                    return
                }

                if (saveFlag == FLAG_EXIT)
                    finish()
                else if (saveFlag == FLAG_SHARE)
                    ShareImageUtil.shareImg(
                            this@PaintActivity,
                            getString(R.string.app_name),
                            getString(R.string.sharemywork) + getString(R.string.sharecontent),
                            path,
                            getString(R.string.pleaseselect))
                else if (saveFlag == FLAG_EFFECT)
                    showEffectDialog(path)
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
                    Toast.makeText(this, getString(R.string.delete_completed), Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, getString(R.string.deletePaintFailed), Toast.LENGTH_SHORT).show()
                }
            }
        }
        tipDialog.showTip(supportFragmentManager, getString(R.string.loadpicture))
        civColoring.clearStack()
        hasSaved = true

        PicassoUtils.displayImage(this, civColoring, path ?: picturePath, object : SimpleTarget() {
            override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom) {
                tipDialog.dismiss()
            }

            override fun onBitmapFailed(errorDrawable: Drawable?) {
                tipDialog.dismiss()
                Toast.makeText(this@PaintActivity, getString(R.string.loadpicturefailed), Toast.LENGTH_SHORT).show()
                finish()
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_paint, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> onBackPressed()
            R.id.menuSave -> saveToLocal(FLAG_SAVE)
            R.id.menuShare -> saveToLocal(FLAG_SHARE)
            R.id.menuDelete -> {
                mDialogHelper.showRepaintDialog(View.OnClickListener {
                    repaint(true)
                })
            }
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
        mDialogHelper.showExitPaintDialog(
                View.OnClickListener { saveToLocal(FLAG_EXIT) },
                View.OnClickListener { finish() })
    }

    companion object {
        private val ARG_IS_FROM_THEMES = "arg_is_from_themes"
        private val ARG_PICTURE_NAME = "arg_picture_name"
        private val ARG_PICTURE_PATH = "arg_picture_path"
        private val FLAG_SAVE = 0
        private val FLAG_EXIT = 1
        private val FLAG_SHARE = 2
        private val FLAG_EFFECT = 3

        fun start(context: Context, isFromThemes: Boolean, pictureName: String, picturePath: String) {
            val intent = Intent(context, PaintActivity::class.java)
            intent.putExtra(ARG_IS_FROM_THEMES, isFromThemes)
            intent.putExtra(ARG_PICTURE_NAME, pictureName)
            intent.putExtra(ARG_PICTURE_PATH, picturePath)
            context.startActivity(intent)
        }
    }
}