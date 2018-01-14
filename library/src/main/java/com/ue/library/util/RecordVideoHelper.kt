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
import android.util.Log
import android.util.SparseIntArray
import android.view.Surface
import android.widget.Toast
import com.ue.library.R
import java.io.IOException

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
    private var DISPLAY_WIDTH = 720
    private var DISPLAY_HEIGHT = 1280

    private val mMediaRecorder: MediaRecorder
    private val mProjectionManager: MediaProjectionManager
    private var mMediaProjection: MediaProjection? = null
    private lateinit var mMediaProjectionCallback: MediaProjection.Callback
    private lateinit var mVirtualDisplay: VirtualDisplay
    var isRecording = false
    var recordVideoListener: RecordVideoListener? = null

    init {
        val dm = activity.resources.displayMetrics
        DISPLAY_WIDTH = dm.widthPixels
        DISPLAY_HEIGHT = dm.heightPixels
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
                Log.e("RecordVideoHelper", "onStop: Recording Stopped")
                mMediaRecorder.stop()
                mMediaRecorder.reset()
                stopRecording()
            }
        }
        mMediaProjection = mProjectionManager.getMediaProjection(resultCode, data)
        mMediaProjection!!.registerCallback(mMediaProjectionCallback, null)
        initRecorder()

        recordVideo()
    }

    fun startRecording() {
        PermissionUtils.checkPermissions(activity,
                PermissionUtils.REQ_PERM_READ_WRITE_STORAGE,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE),
                activity.getString(R.string.no_recording_perms),
                object : PermissionUtils.SimplePermissionListener {
                    override fun onSucceed(requestCode: Int, grantPermissions: List<String>) {
                        recordVideo()
                    }
                })
    }

    fun stopRecording() {
        Log.e("RecordVideoHelper", "stopRecording: ")
        ActivityUtils.toggleFullScreen(activity, false)
        mMediaRecorder.stop()
        mMediaRecorder.reset()
        mVirtualDisplay.release()
        //mMediaRecorder.release(); //If used: mMediaRecorder object cannot
        // be reused again
        mMediaProjection?.unregisterCallback(mMediaProjectionCallback)
        mMediaProjection?.stop()

        isRecording = false
    }

    private fun recordVideo() {
        if (mMediaProjection == null) {
            activity.startActivityForResult(mProjectionManager.createScreenCaptureIntent(), REQ_RECORD_VIDEO)
            return
        }
        ActivityUtils.toggleFullScreen(activity, true)
        mVirtualDisplay = createVirtualDisplay()
        mMediaRecorder.start()

        isRecording = true
        recordVideoListener?.onStart()
    }

    private fun createVirtualDisplay(): VirtualDisplay {
        return mMediaProjection!!.createVirtualDisplay("MainActivity",
                DISPLAY_WIDTH, DISPLAY_HEIGHT, mScreenDensity,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                mMediaRecorder.surface, null, null
                /*Handler*/)/*Callbacks*/
    }

    private fun initRecorder() {
        try {
            mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE)
            mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            mMediaRecorder.setOutputFile(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/video.mp4")
            mMediaRecorder.setVideoSize(DISPLAY_WIDTH, DISPLAY_HEIGHT)
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
    }
}