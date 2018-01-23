package com.ue.autodraw

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.RadioGroup
import android.widget.Toast
import com.ue.library.event.HomeWatcher
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
        private val SP_RECORD_TIP_VISIBLE = "sp_record_tip_visible"
        private val SP_OUTLINE_TIP_VISIBLE = "sp_outline_tip_visible"
    }

    private var disposable: Disposable? = null
    private lateinit var loadingDialog: LoadingDialog
    private var recordVideoHelper: RecordVideoHelper? = null

    private var readyThenDraw = true//when ready,true:draw,false:record
    private lateinit var homeWatcher: HomeWatcher

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auto_draw)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.auto_draw)

        rgTabs.check(R.id.rbTabObject)

        initRecyclerViews()
        setListeners()

        loadingDialog = LoadingDialog.newInstance()

        val outlineObjPath = SPUtils.getString(SP_OUTLINE_OBJ_PATH, "")
        if (TextUtils.isEmpty(outlineObjPath)) {
            loadPhoto(R.drawable.test)
        } else {
            loadPhoto(outlineObjPath!!)
        }

        homeWatcher = HomeWatcher(this)
        homeWatcher.homeListener = object : HomeWatcher.OnHomePressedListener {
            override fun onHomePressed() {
                advOutline.stopDrawing()
            }
        }
        homeWatcher.startWatch()
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        DialogUtils.showOnceHintDialog(this,
                R.string.auto_draw,
                R.string.auto_draw_tip,
                R.string.got_it,
                null,
                SP_OUTLINE_TIP_VISIBLE)
    }

    private fun setListeners() {
        rgTabs.setOnCheckedChangeListener(this)
        ivObjectView.setOnClickListener(this)
        advOutline.setOnClickListener(this)
        tvShareDrawPicture.setOnClickListener(this)
        tvShareDrawVideo.setOnClickListener(this)
        nsLineThickness.setNumberChangeListener(this)
        nsDelaySpeed.setNumberChangeListener(this)

        advOutline.autoDrawListener = object : AutoDrawView.OnAutoDrawListener {

            override fun onReady() {
                if (!loadingDialog.isAdded) return

                loadingDialog.dismiss()
                if (readyThenDraw) advOutline.startDrawing()
                else recordDrawVideo()
            }

            override fun onStop() {
                if (recordVideoHelper != null && recordVideoHelper!!.isRecording) {
                    recordVideoHelper!!.cancelRecording()
                } else {
                    Toast.makeText(this@AutoDrawActivity, R.string.cancel_draw, Toast.LENGTH_SHORT).show()
                }
            }

            override fun onComplete() {
                if (recordVideoHelper != null && recordVideoHelper!!.isRecording) {
                    recordVideoHelper!!.finishRecording()
                }
            }
        }
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
                R.drawable.svg_pencil, R.drawable.svg_fountain_pen,
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
            R.id.advOutline -> onOutlineClick()
            R.id.tvShareDrawPicture -> onShareDrawPictureClick()
            R.id.tvShareDrawVideo -> onShareDrawVideoClick()
        }
    }

    private fun onShareDrawVideoClick() {
        vgShare.visibility = View.GONE
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            Toast.makeText(this, R.string.cannot_share_video_version, Toast.LENGTH_SHORT).show()
            return
        }
        readyThenDraw = false
        if (!advOutline.isCanSave) {
            loadingDialog.showLoading(supportFragmentManager, getString(R.string.is_preparing))
            return
        }
        recordDrawVideo()
    }

    private fun onShareDrawPictureClick() {
        vgShare.visibility = View.GONE
        readyThenDraw = true
        if (!advOutline.isCanSave) {
            Toast.makeText(this, R.string.no_outline_to_save, Toast.LENGTH_SHORT).show()
            return
        }
        advOutline.saveOutlinePicture(object : FileUtils.OnSaveImageListener {
            override fun onSaved(path: String) {
                IntentUtils.shareImage(this@AutoDrawActivity, null, null, path)
            }
        })
    }

    private fun onOutlineClick() {
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
            if (recordVideoHelper != null && recordVideoHelper!!.isRecording) {
                recordVideoHelper!!.cancelRecording()
                Toast.makeText(this, R.string.cancel_record_video, Toast.LENGTH_SHORT).show()
            }
            return
        }
        advOutline.startDrawing()
    }

    /**
     * 录制视频相关
     */
    private fun recordDrawVideo() {
        DialogUtils.showOnceHintDialog(this,
                R.string.record_draw_video_title,
                R.string.record_draw_video_tip,
                R.string.got_it,
                View.OnClickListener {
                    if (recordVideoHelper == null) initRecordVideoHelper()
                    advOutline.resetCanvas(true)
                    recordVideoHelper!!.startRecording()
                },
                SP_RECORD_TIP_VISIBLE)
    }

    private fun initRecordVideoHelper() {
        recordVideoHelper = RecordVideoHelper(this)
        recordVideoHelper!!.recordVideoListener = object : RecordVideoHelper.RecordVideoListener {
            override fun onStart() {
                advOutline.startDrawing()
            }

            override fun onCancel() {
                Toast.makeText(this@AutoDrawActivity, R.string.cancel_record_video, Toast.LENGTH_SHORT).show()
            }

            override fun onComplete(videoPath: String) {
                Toast.makeText(this@AutoDrawActivity, R.string.complete_recording, Toast.LENGTH_SHORT).show()
                ShareVideoDialog.newInstance(videoPath).show(supportFragmentManager, "")
            }
        }
    }

    /****/

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

    /*
    * 图片相关
    * */
    private fun loadPhoto(photo: Any) {
        readyThenDraw = true
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

    private fun pickPhoto() {
        PermissionUtils.checkReadWriteStoragePerms(this,
                getString(R.string.no_read_storage_perm),
                object : PermissionUtils.SimplePermissionListener {
                    override fun onSucceed(requestCode: Int, grantPermissions: List<String>) {
                        startActivityForResult(Intent.createChooser(Intent(Intent.ACTION_GET_CONTENT).setType("image/*"), getString(R.string.choose_photo)), REQ_PICK_PHOTO)
                    }
                }
        )
    }

    /*******/

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQ_PICK_PHOTO) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                //Log.e("AutoDrawActivity", "onActivityResult: photo path=${data.dataString}")
                SPUtils.putString(SP_OUTLINE_OBJ_PATH, data.dataString)
                loadPhoto(data.dataString)
            }
            return
        }
        recordVideoHelper?.onActivityResult(requestCode, resultCode, data)
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
        recordVideoHelper?.destroyMediaProjection()
        homeWatcher.stopWatch()
        RxJavaUtils.dispose(disposable)
    }
}