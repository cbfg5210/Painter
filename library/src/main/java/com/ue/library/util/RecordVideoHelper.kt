package com.ue.library.util

import android.Manifest
import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.MediaRecorder
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Environment
import android.support.v7.app.AppCompatActivity
import android.util.SparseIntArray
import android.view.Surface
import android.widget.Toast
import com.ue.library.R
import com.ue.library.constant.Constants
import com.ue.library.event.SimplePermissionListener
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.concurrent.TimeUnit

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
class RecordVideoHelper(private val activity: AppCompatActivity) {

    companion object {
        private val REQ_RECORD_VIDEO = 1000
        private val ORIENTATIONS = SparseIntArray()

        init {
            ORIENTATIONS.append(Surface.ROTATION_0, 90)
            ORIENTATIONS.append(Surface.ROTATION_90, 0)
            ORIENTATIONS.append(Surface.ROTATION_180, 270)
            ORIENTATIONS.append(Surface.ROTATION_270, 180)
        }
    }

    private var mScreenDensity = 0
    private var displayWidth = 720
    private var displayHeight = 1280

    private val mMediaRecorder: MediaRecorder
    private val mProjectionManager: MediaProjectionManager
    private var mMediaProjection: MediaProjection? = null
    private lateinit var mMediaProjectionCallback: MediaProjection.Callback
    private lateinit var mVirtualDisplay: VirtualDisplay

    var isRecording = false
    var recordVideoListener: RecordVideoListener? = null
    private var videoPath = ""

    init {
        val dm = activity.resources.displayMetrics
        displayWidth = dm.widthPixels
        displayHeight = dm.heightPixels
        mScreenDensity = dm.densityDpi

        mMediaRecorder = MediaRecorder()
        mProjectionManager = activity.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode != REQ_RECORD_VIDEO) {
            return
        }
        if (resultCode != Activity.RESULT_OK) {
            Toast.makeText(activity, activity.getString(R.string.no_recording_perms), Toast.LENGTH_SHORT).show()
            return
        }
        mMediaProjectionCallback = object : MediaProjection.Callback() {
            override fun onStop() {
                mMediaRecorder.stop()
                mMediaRecorder.reset()
                cancelRecording()
            }
        }
        mMediaProjection = mProjectionManager.getMediaProjection(resultCode, data)
                .apply {
                    registerCallback(mMediaProjectionCallback, null)
                }

        recordVideo()
    }

    fun startRecording() {
        videoPath = "${Environment.getExternalStorageDirectory().path}${Constants.PATH_OUTLINE}${SimpleDateFormat("yyyyMMddHHmmss").format(System.currentTimeMillis())}.mp4"
        PermissionUtils.checkPermissions(activity,
                PermissionUtils.REQ_PERM_READ_WRITE_STORAGE,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE),
                activity.getString(R.string.record_failed_no_perm),
                object : SimplePermissionListener() {
                    override fun onSucceed(requestCode: Int, grantPermissions: MutableList<String>) {
                        recordVideo()
                    }
                })
    }

    fun cancelRecording() {
        if (!isRecording) return
        stopRecoding()
        recordVideoListener?.onCancel()
    }

    fun finishRecording() {
        if (!isRecording) return
        stopRecoding()
        recordVideoListener?.onComplete(videoPath)
        activity.toggleFullScreen(false)
    }

    private fun stopRecoding() {
        mMediaRecorder.stop()
        mMediaRecorder.reset()
        mVirtualDisplay.release()

        isRecording = false
    }

    fun destroyMediaProjection() {
        //If used: mMediaRecorder object cannot be reused again
        mMediaRecorder.release()
        mMediaProjection?.apply {
            unregisterCallback(mMediaProjectionCallback)
            stop()
        }
    }

    private fun createVirtualDisplay(): VirtualDisplay {
        return mMediaProjection!!.createVirtualDisplay("recordDrawVideo",
                displayWidth, displayHeight, mScreenDensity,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                mMediaRecorder.surface, null, null)/*Callbacks*/
    }

    private fun recordVideo() {
        if (mMediaProjection == null) {
            activity.startActivityForResult(mProjectionManager.createScreenCaptureIntent(), REQ_RECORD_VIDEO)
            return
        }
        activity.toggleFullScreen(true)
        //全屏过程会有一段时间，避免录入全屏过程及对绘制轮廓的影响
        Observable.timer(300, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.single())
                .observeOn(AndroidSchedulers.mainThread())
                .bindUtilDestroy3(activity)
                .subscribe {
                    initRecorder()
                    mVirtualDisplay = createVirtualDisplay()
                    mMediaRecorder.start()

                    recordVideoListener?.onStart()

                    isRecording = true
                }
    }

    private fun initRecorder() {
        try {
            mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE)
            mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            mMediaRecorder.setOutputFile(videoPath)
            mMediaRecorder.setVideoSize(displayWidth, displayHeight)
            mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264)
            mMediaRecorder.setVideoEncodingBitRate(512 * 1000)
            mMediaRecorder.setVideoFrameRate(30)
            val rotation = activity.windowManager.defaultDisplay.rotation
            val orientation = ORIENTATIONS.get(rotation + 90)
            mMediaRecorder.setOrientationHint(orientation)
            mMediaRecorder.prepare()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    interface RecordVideoListener {
        fun onStart()
        fun onCancel()
        fun onComplete(videoPath: String)
    }
}