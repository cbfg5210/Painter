package com.ue.autodraw

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.RadioGroup
import com.trello.rxlifecycle2.components.support.RxAppCompatActivity
import com.ue.library.event.HomeWatcher
import com.ue.library.event.SimplePermissionListener
import com.ue.library.util.*
import com.ue.library.widget.LoadingDialog
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.au_activity_outline.*
import kotlinx.android.synthetic.main.au_layout_outline_settings.*

class OutlineActivity : RxAppCompatActivity(),
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
    //when ready,true:draw,false:record
    private var readyThenDraw = true
    private lateinit var homeWatcher: HomeWatcher

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.au_activity_outline)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        rgTabs.check(R.id.rbTabObject)

        initRecyclerViews()
        setListeners()

        loadingDialog = LoadingDialog.newInstance()

        val outlineObjPath = SPUtils.getString(SP_OUTLINE_OBJ_PATH)
        if (TextUtils.isEmpty(outlineObjPath)) loadPhoto(R.mipmap.test)
        else loadPhoto(outlineObjPath)

        homeWatcher = HomeWatcher(this).apply {
            homeListener = object : HomeWatcher.OnHomePressedListener {
                override fun onHomePressed() {
                    advOutline.stopDrawing()
                }
            }
            startWatch()
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        DialogUtils.showOnceHintDialog(this,
                R.string.au_module_name,
                R.string.au_auto_draw_tip,
                R.string.au_got_it,
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
                var goOn = true
                recordVideoHelper?.apply {
                    if (isRecording) {
                        goOn = false
                        cancelRecording()
                    }
                }
                if (goOn) toast(R.string.au_cancel_draw)
            }

            override fun onComplete() {
                recordVideoHelper?.apply { if (isRecording) finishRecording() }
            }
        }
    }

    private fun initRecyclerViews() {
        rvBgOptions.apply {
            setHasFixedSize(true)
            adapter = BgAdapter(this@OutlineActivity, getXmlImageArray(R.array.au_bgs))
                    .apply { itemListener = AdapterView.OnItemClickListener { _, _, imgRes, _ -> advOutline.setBgBitmapRes(imgRes) } }
        }

        rvPaintOptions.apply {
            setHasFixedSize(true)
            adapter = PaintAdapter(getXmlImageArray(R.array.au_pens))
                    .apply { itemListener = AdapterView.OnItemClickListener { _, _, imgRes, _ -> advOutline.setPaintBitmapRes(imgRes) } }
        }

        rvPaintColorOptions.apply {
            setHasFixedSize(true)
            adapter = PaintColorAdapter(resources.getIntArray(R.array.au_paintColorOptions))
                    .apply { itemListener = AdapterView.OnItemClickListener { _, _, paintColor, _ -> advOutline.setPaintColor(paintColor) } }
        }
    }

    override fun onClick(v: View) {
        val vid = v.id
        when (vid) {
            R.id.ivObjectView -> pickPhoto()
            R.id.advOutline -> onOutlineClick()
            R.id.tvShareDrawPicture -> onShareDrawPictureClick()
            R.id.tvShareDrawVideo -> onShareDrawVideoClick()
        }
    }

    private fun onShareDrawVideoClick() {
        vgShare.visibility = View.GONE
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            toast(R.string.au_cannot_share_video_version)
            return
        }
        readyThenDraw = false
        if (!advOutline.isCanSave) {
            loadingDialog.showLoading(supportFragmentManager, getString(R.string.au_is_preparing))
            return
        }
        recordDrawVideo()
    }

    private fun onShareDrawPictureClick() {
        vgShare.visibility = View.GONE
        readyThenDraw = true
        if (!advOutline.isCanSave) {
            toast(R.string.au_no_outline_to_save)
            return
        }
        advOutline.saveOutlinePicture(object : FileUtils.OnSaveImageListener {
            override fun onSaved(path: String) {
                IntentUtils.shareImage(this@OutlineActivity, null, null, path)
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
            loadingDialog.showLoading(supportFragmentManager, getString(R.string.au_is_preparing))
            return
        }
        if (advOutline.isDrawing) {
            advOutline.stopDrawing()
            recordVideoHelper?.apply {
                if (isRecording) {
                    cancelRecording()
                    toast(R.string.au_cancel_record_video)
                }
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
                R.string.au_record_draw_video_title,
                R.string.au_record_draw_video_tip,
                R.string.au_got_it,
                View.OnClickListener {
                    recordVideoHelper ?: initRecordVideoHelper()
                    advOutline.resetCanvas(true)
                    recordVideoHelper!!.startRecording()
                },
                SP_RECORD_TIP_VISIBLE)
    }

    private fun initRecordVideoHelper() {
        recordVideoHelper = RecordVideoHelper(this).apply {
            recordVideoListener = object : RecordVideoHelper.RecordVideoListener {
                override fun onStart() {
                    advOutline.startDrawing()
                }

                override fun onCancel() {
                    toast(R.string.au_cancel_record_video)
                }

                override fun onComplete(videoPath: String) {
                    //录制完成后保存临摹结果图片
                    advOutline.saveOutlinePicture(null, false)
                    toast(R.string.au_complete_recording)
                    ShareVideoDialog.newInstance(videoPath).show(supportFragmentManager, "")
                }
            }
        }
    }

    /****/

    override fun onCheckedChanged(group: RadioGroup, checkedId: Int) {
        vgTabContentFlipper.displayedChild = group.indexOfChild(group.findViewById<View>(checkedId))
    }

    override fun onNumberChanged(view: View, number: Int) {
        advOutline.apply {
            if (view.id == R.id.nsLineThickness) setLineThickness(number)
            else setDelaySpeed(number)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.au_menu_auto_draw, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val itemId = item.itemId
        when (itemId) {
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
        ImageLoaderUtils.display(ivObjectView, photo, R.mipmap.test, getString(R.string.au_load_photo_failed),
                object : ImageLoaderUtils.ImageLoaderCallback2 {
                    override fun onBitmapResult(bitmap: Bitmap?) {
                        if (bitmap != null) advOutline.setOutlineObject(bitmap)
                    }
                })
    }

    private fun pickPhoto() {
        PermissionUtils.checkReadWriteStoragePerms(this,
                getString(R.string.au_no_read_storage_perm),
                object : SimplePermissionListener() {
                    override fun onSucceed(requestCode: Int, grantPermissions: MutableList<String>) {
                        startActivityForResult(Intent.createChooser(Intent(Intent.ACTION_GET_CONTENT).setType("image/*"), getString(R.string.au_choose_photo)), REQ_PICK_PHOTO)
                    }
                }
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQ_PICK_PHOTO) {
            if (resultCode == Activity.RESULT_OK && data != null) {
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
        dispose(disposable)
    }
}