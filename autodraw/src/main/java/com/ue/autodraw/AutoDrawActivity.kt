package com.ue.autodraw

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
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
import com.yanzhenjie.permission.PermissionListener
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_auto_draw.*
import kotlinx.android.synthetic.main.layout_auto_draw_settings.*

class AutoDrawActivity : AppCompatActivity(), View.OnClickListener, RadioGroup.OnCheckedChangeListener, NumberSelectorView.OnNumberChangeListener {

    private var disposable: Disposable? = null

    companion object {
        private val REQ_SIZE = 150
        private val REQ_PERM_EXTERNAL = 1
        private val REQ_PICK_PHOTO = 2
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auto_draw)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.auto_draw)

        initRecyclerViews()

        rgTabs.check(R.id.rbTabObject)
        rgTabs.setOnCheckedChangeListener(this)
        ivObjectView.setOnClickListener(this)
        advOutline.setOnClickListener(this)
        nsLineThickness.setNumberChangeListener(this)
        nsDelaySpeed.setNumberChangeListener(this)

        advOutline.loadBitmapThenDraw(R.drawable.test)
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
        paintColorAdapter.setItemListener(AdapterView.OnItemClickListener { _, _, paintColor, _ ->
            advOutline.setPaintColor(paintColor)
        })
        rvPaintColorOptions.setHasFixedSize(true)
        rvPaintColorOptions.adapter = paintColorAdapter
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.ivObjectView -> pickPhoto()
            R.id.advOutline -> {
                if (vgDrawSettings.visibility == View.VISIBLE) {
                    vgDrawSettings.visibility = View.GONE
                    return
                }
                advOutline.redraw()
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
            android.R.id.home -> {
                if (vgDrawSettings.visibility == View.VISIBLE) {
                    vgDrawSettings.visibility = View.GONE
                    return true
                }
                finish()
            }
            R.id.actionSettings -> vgDrawSettings.visibility = if (vgDrawSettings.visibility == View.VISIBLE) View.GONE else View.VISIBLE
//            R.id.actionShareDrawPicture -> {
//                Toast.makeText(this, "picture", Toast.LENGTH_SHORT).show()
//            }
//            R.id.actionShareDrawVideo -> {
//                Toast.makeText(this, "video", Toast.LENGTH_SHORT).show()
//            }
        }
        return true
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
            ImageLoaderUtils.display(this,
                    ivObjectView,
                    data.dataString,
                    object : ImageLoaderUtils.ImageLoaderCallback {
                        override fun onBitmapLoaded(bitmap: Bitmap) {
                            Log.e("AutoDrawActivity", "onBitmapLoaded: ok")
                            advOutline.resetBitmapThenDraw(bitmap)
                        }

                        override fun onBitmapFailed() {
                            Toast.makeText(this@AutoDrawActivity, R.string.load_photo_failed, Toast.LENGTH_SHORT).show()
                        }
                    })
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        RxJavaUtils.dispose(disposable)
    }
}
