package com.ue.autodraw

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.RadioGroup
import android.widget.Toast
import com.ue.library.util.*
import com.ue.library.widget.LoadingDialog
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_auto_draw.*
import kotlinx.android.synthetic.main.layout_auto_draw_settings.*

class AutoDrawActivity : AppCompatActivity(),
        View.OnClickListener,
        RadioGroup.OnCheckedChangeListener,
        NumberSelectorView.OnNumberChangeListener {

    companion object {
        private val REQ_PICK_PHOTO = 2
        private val SP_OUTLINE_OBJ_PATH = "sp_outline_obj_path"
    }

    private var disposable: Disposable? = null
    private lateinit var loadingDialog: LoadingDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auto_draw)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.auto_draw)

        rgTabs.check(R.id.rbTabObject)

        initRecyclerViews()

        rgTabs.setOnCheckedChangeListener(this)
        ivObjectView.setOnClickListener(this)
        advOutline.setOnClickListener(this)
        tvShareDrawPicture.setOnClickListener(this)
        tvShareDrawVideo.setOnClickListener(this)
        nsLineThickness.setNumberChangeListener(this)
        nsDelaySpeed.setNumberChangeListener(this)

        advOutline.autoDrawListener = object : AutoDrawView.OnAutoDrawListener {
            override fun onReady() {
                if (loadingDialog.isAdded) {
                    advOutline.startDrawing()
                }
                loadingDialog.dismiss()
            }

            override fun onStop() {
                Toast.makeText(this@AutoDrawActivity, R.string.cancel_draw, Toast.LENGTH_SHORT).show()
            }
        }

        loadingDialog = LoadingDialog.newInstance()

        val outlineObjPath = SPUtils.getString(SP_OUTLINE_OBJ_PATH, "")
        if (TextUtils.isEmpty(outlineObjPath)) {
            loadPhoto(R.drawable.test)
        } else {
            loadPhoto(outlineObjPath!!)
        }
    }

    private fun loadPhoto(photo: Any) {
        loadingDialog.showLoading(supportFragmentManager, getString(R.string.is_preparing))

        ImageLoaderUtils.display(this, ivObjectView, photo, R.drawable.test,
                object : ImageLoaderUtils.ImageLoaderCallback {
                    override fun onBitmapLoaded(bitmap: Bitmap) {
                        advOutline.setOutlineObject(bitmap)
                    }

                    override fun onBitmapFailed(errorBitmap: Bitmap?) {
                        if (errorBitmap != null) {
                            advOutline.setOutlineObject(errorBitmap)
                        }
                    }
                })
    }

    private fun initRecyclerViews() {
        val bgAdapter = BgAdapter(this, intArrayOf(
                R.drawable.fs0, R.drawable.fs1,
                R.drawable.fs2, R.drawable.fs3,
                R.drawable.fs4, R.drawable.fs5,
                R.drawable.fs6, R.drawable.fs7,
                R.drawable.fs8, R.drawable.fs9, R.drawable.fs10))
        bgAdapter.itemListener = AdapterView.OnItemClickListener { _, _, imgRes, _ ->
            advOutline.setBgBitmapRes(imgRes)
        }
        rvBgOptions.setHasFixedSize(true)
        rvBgOptions.adapter = bgAdapter

        val paintAdapter = PaintAdapter(intArrayOf(
                R.drawable.svg_pencil, R.drawable.svg_pen,
                R.drawable.svg_blush, R.drawable.svg_feather))
        paintAdapter.itemListener = AdapterView.OnItemClickListener { _, _, imgRes, _ ->
            advOutline.setPaintBitmapRes(imgRes)
        }
        rvPaintOptions.setHasFixedSize(true)
        rvPaintOptions.adapter = paintAdapter

        val paintColorAdapter = PaintColorAdapter(resources.getIntArray(R.array.PaintColorOptions))
        paintColorAdapter.itemListener = AdapterView.OnItemClickListener { _, _, paintColor, _ ->
            advOutline.setPaintColor(paintColor)
        }
        rvPaintColorOptions.setHasFixedSize(true)
        rvPaintColorOptions.adapter = paintColorAdapter
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.ivObjectView -> pickPhoto()
            R.id.advOutline -> {
                if (vgShare.visibility == View.VISIBLE) {
                    vgShare.visibility = View.GONE
                    return
                }
                if (vgDrawSettings.visibility == View.VISIBLE) {
                    vgDrawSettings.visibility = View.GONE
                    return
                }

                if (!advOutline.isReadyToDraw) {
                    loadingDialog.showLoading(supportFragmentManager, getString(R.string.is_preparing))
                    return
                }
                if (advOutline.isDrawing) {
                    advOutline.stopDrawing()
                    return
                }
                advOutline.startDrawing()
            }
            R.id.tvShareDrawPicture -> {
                vgShare.visibility = View.GONE
                if (!advOutline.isCanSave) {
                    Toast.makeText(this, R.string.no_outline_to_save, Toast.LENGTH_SHORT).show()
                    return
                }
                advOutline.saveOutlinePicture(object : FileUtils.OnSaveImageListener {
                    override fun onSaved(path: String) {
                        IntentUtils.shareImage(this@AutoDrawActivity, null, null, path, getString(R.string.share_to))
                    }
                })
            }
            R.id.tvShareDrawVideo -> {
                vgShare.visibility = View.GONE
                Toast.makeText(this, "share video", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCheckedChanged(group: RadioGroup, checkedId: Int) {
        vgTabContentFlipper.displayedChild = group.indexOfChild(group.findViewById<View>(checkedId))
    }

    override fun onNumberChanged(view: View, number: Int) {
        if (view.id == R.id.nsLineThickness) {
            advOutline.setLineThickness(number)
        } else {
            advOutline.setDelaySpeed(number)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_auto_draw, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> onBackPressed()
            R.id.actionSettings -> {
                advOutline.stopDrawing()
                if (vgShare.visibility == View.VISIBLE) vgShare.visibility = View.GONE
                vgDrawSettings.visibility = if (vgDrawSettings.visibility == View.VISIBLE) View.GONE else View.VISIBLE
            }
            R.id.actionShare -> {
                advOutline.stopDrawing()
                if (vgDrawSettings.visibility == View.VISIBLE) vgDrawSettings.visibility = View.GONE
                vgShare.visibility = if (vgShare.visibility == View.VISIBLE) View.GONE else View.VISIBLE
            }
        }
        return true
    }

    private fun pickPhoto() {
        PermissionUtils.checkReadWriteStoragePerms(this,
                getString(R.string.no_read_storage_perm),
                object : PermissionUtils.SimplePermissionListener {
                    override fun onSucceed(requestCode: Int, grantPermissions: List<String>) {
                        startActivityForResult(Intent.createChooser(Intent(Intent.ACTION_GET_CONTENT).setType("image*//*"), getString(R.string.choose_photo)), REQ_PICK_PHOTO)
                    }
                }
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQ_PICK_PHOTO && resultCode == Activity.RESULT_OK && data != null) {
            //Log.e("AutoDrawActivity", "onActivityResult: photo path=${data.dataString}")
            SPUtils.putString(SP_OUTLINE_OBJ_PATH, data.dataString)
            loadPhoto(data.dataString)
        }
    }

    override fun onBackPressed() {
        if (vgShare.visibility == View.VISIBLE) {
            vgShare.visibility = View.GONE
            return
        }
        if (vgDrawSettings.visibility == View.VISIBLE) {
            vgDrawSettings.visibility = View.GONE
            return
        }
        if (advOutline.isDrawing) {
            advOutline.stopDrawing()
            return
        }
        super.onBackPressed()
    }

    override fun onDestroy() {
        super.onDestroy()
        RxJavaUtils.dispose(disposable)
    }
}
