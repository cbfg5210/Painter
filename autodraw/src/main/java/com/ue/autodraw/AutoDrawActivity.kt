package com.ue.autodraw

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.RadioGroup
import android.widget.Toast
import com.ue.library.util.ImageLoaderUtils
import com.ue.library.util.PermissionUtils
import com.ue.library.util.RxJavaUtils
import com.ue.library.util.SPUtils
import com.yanzhenjie.permission.PermissionListener
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_auto_draw.*
import kotlinx.android.synthetic.main.layout_auto_draw_settings.*

class AutoDrawActivity : AppCompatActivity(),
        View.OnClickListener,
        RadioGroup.OnCheckedChangeListener,
        NumberSelectorView.OnNumberChangeListener {

    private var disposable: Disposable? = null

    companion object {
        private val REQ_PERM_EXTERNAL = 1
        private val REQ_PICK_PHOTO = 2
        private val SP_OUTLINE_OBJ_PATH = "sp_outline_obj_path"
    }

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
            override fun onPrepare() {
                Toast.makeText(this@AutoDrawActivity, "绘制前准备", Toast.LENGTH_SHORT).show()
            }

            override fun onStart() {
                Toast.makeText(this@AutoDrawActivity, "开始绘制", Toast.LENGTH_SHORT).show()
            }

            override fun onComplete() {
                Toast.makeText(this@AutoDrawActivity, "分享图片或视频", Toast.LENGTH_SHORT).show()
            }
        }

        val outlineObjPath = SPUtils.getString(SP_OUTLINE_OBJ_PATH, "")
        if (TextUtils.isEmpty(outlineObjPath)) {
            loadPhoto(R.drawable.test)
        } else {
            loadPhoto(outlineObjPath!!)
        }
    }

    private fun loadPhoto(photo: Any) {
        ImageLoaderUtils.display(this, ivObjectView, photo, R.drawable.test,
                object : ImageLoaderUtils.ImageLoaderCallback {
                    override fun onBitmapLoaded(bitmap: Bitmap) {
                        advOutline.resetBitmapThenDraw(bitmap)
                    }

                    override fun onBitmapFailed(errorBitmap: Bitmap?) {
                        if (errorBitmap != null) {
                            advOutline.resetBitmapThenDraw(errorBitmap)
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
        if (v.id == R.id.ivObjectView) {
            pickPhoto()
            return
        }
        if (v.id == R.id.advOutline) {
            if (vgDrawSettings.visibility == View.VISIBLE) {
                vgDrawSettings.visibility = View.GONE
                return
            }
            advOutline.redraw()
            return
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
                if (vgShare.visibility == View.VISIBLE) vgShare.visibility = View.GONE
                vgDrawSettings.visibility = if (vgDrawSettings.visibility == View.VISIBLE) View.GONE else View.VISIBLE
            }
            R.id.actionShare -> {
                if (vgDrawSettings.visibility == View.VISIBLE) vgDrawSettings.visibility = View.GONE
                vgShare.visibility = if (vgShare.visibility == View.VISIBLE) View.GONE else View.VISIBLE
            }
            R.id.tvShareDrawPicture -> Toast.makeText(this, "share picture", Toast.LENGTH_SHORT).show()
            R.id.tvShareDrawVideo -> Toast.makeText(this, "share video", Toast.LENGTH_SHORT).show()
        }
        return true
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
        super.onBackPressed()
    }

    private fun pickPhoto() {
        PermissionUtils.checkPermission(this,
                REQ_PERM_EXTERNAL,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE),
                object : PermissionListener {
                    override fun onSucceed(requestCode: Int, grantPermissions: MutableList<String>) {
                        startActivityForResult(Intent.createChooser(Intent(Intent.ACTION_GET_CONTENT).setType("image/*"), getString(R.string.choose_photo)), REQ_PICK_PHOTO)
                    }

                    override fun onFailed(requestCode: Int, deniedPermissions: MutableList<String>) {
                        Toast.makeText(this@AutoDrawActivity, R.string.no_read_storage_perm, Toast.LENGTH_SHORT).show()
                    }
                })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQ_PICK_PHOTO && resultCode == Activity.RESULT_OK && data != null) {
            Log.e("AutoDrawActivity", "onActivityResult: photo path=${data.dataString}")
            SPUtils.putString(SP_OUTLINE_OBJ_PATH, data.dataString)
            loadPhoto(data.dataString)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        RxJavaUtils.dispose(disposable)
    }
}
