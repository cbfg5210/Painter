package com.ue.graffiti.ui

import android.app.AlertDialog
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.Toast
import com.trello.rxlifecycle2.android.ActivityEvent
import com.ue.graffiti.R
import com.ue.graffiti.touch.*
import com.ue.graffiti.widget.CanvasView
import io.reactivex.Observable
import io.reactivex.ObservableOnSubscribe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException

/**
 * Created by hawk on 2018/1/16.
 */

class MainPresenter(private val mMainActivity: MainActivity) {
    private var pelsPopupWindow: PopupWindow? = null
    private var canvasBgsPopupWindow: PopupWindow? = null
    //四个方向的显示动画：左、上、右、下
    private var showAnimations: Array<Animation>? = null
    //四个方向的隐藏动画：左、上、右、下
    private var hideAnimations: Array<Animation>? = null

    fun getToggleAnimations(isShown: Boolean): Array<Animation>? {
        if (isShown) {
            if (showAnimations == null) {
                showAnimations = arrayOf(
                        AnimationUtils.loadAnimation(mMainActivity, R.anim.leftappear),
                        AnimationUtils.loadAnimation(mMainActivity, R.anim.topappear),
                        AnimationUtils.loadAnimation(mMainActivity, R.anim.rightappear),
                        AnimationUtils.loadAnimation(mMainActivity, R.anim.downappear)
                )
            }
            return showAnimations
        }
        if (hideAnimations == null) {
            hideAnimations = arrayOf(
                    AnimationUtils.loadAnimation(mMainActivity, R.anim.leftdisappear),
                    AnimationUtils.loadAnimation(mMainActivity, R.anim.topdisappear),
                    AnimationUtils.loadAnimation(mMainActivity, R.anim.rightdisappear),
                    AnimationUtils.loadAnimation(mMainActivity, R.anim.downdisappear)
            )
        }
        return hideAnimations
    }

    fun capturePhoto(REQUEST_CODE_GRAPH: Int) {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(File(Environment.getExternalStorageDirectory(), "temp.jpg")))
        intent.putExtra("outputFormat", Bitmap.CompressFormat.PNG.name)
        intent.putExtra(MediaStore.EXTRA_SCREEN_ORIENTATION, Configuration.ORIENTATION_LANDSCAPE)
        mMainActivity.startActivityForResult(intent, REQUEST_CODE_GRAPH)
    }

    fun showPelsPopupWindow(vgBottomMenu: View) {
        showPopupWindow(canvasBgsPopupWindow, pelsPopupWindow, vgBottomMenu)
    }

    fun showCanvasBgsPopupWindow(vgBottomMenu: View) {
        showPopupWindow(pelsPopupWindow, canvasBgsPopupWindow, vgBottomMenu)
    }

    fun dismissPopupWindows() {
        canvasBgsPopupWindow!!.dismiss()
        pelsPopupWindow!!.dismiss()
    }

    private fun showPopupWindow(dismissPopup: PopupWindow?, showPopup: PopupWindow?, vgBottomMenu: View) {
        dismissPopup!!.dismiss()
        if (showPopup!!.isShowing) {
            showPopup.dismiss()
            return
        }
        showPopup.showAtLocation(vgBottomMenu, Gravity.BOTTOM, 0, vgBottomMenu.height)
    }

    fun initPelsPopupWindow(layoutRes: Int, groupId: Int, cvGraffitiView: CanvasView, pickPelListener: OnPickPelListener): ImageView {
        val layoutView = mMainActivity.layoutInflater.inflate(layoutRes, null)
        pelsPopupWindow = initPopupWindow(layoutView, groupId, View.OnClickListener { v -> pickPelListener.onPelPick(v, getPelTouchByViewId(v.id, cvGraffitiView)) })
        return layoutView.findViewById(R.id.ivFreehand)
    }

    fun getPelTouchByViewId(viewId: Int, cvGraffitiView: CanvasView): Touch? {
        when (viewId) {
            R.id.ivFreehand -> return DrawFreehandTouch(cvGraffitiView)
            R.id.ivRect -> return DrawRectTouch(cvGraffitiView)
            R.id.ivBessel -> return DrawBesselTouch(cvGraffitiView)
            R.id.ivOval -> return DrawOvalTouch(cvGraffitiView)
            R.id.ivLine -> return DrawLineTouch(cvGraffitiView)
            R.id.ivBrokenLine -> return DrawBrokenLineTouch(cvGraffitiView)
            R.id.ivPolygon -> return DrawPolygonTouch(cvGraffitiView)
            R.id.ivKeepDrawing -> return KeepDrawingTouch(cvGraffitiView)
            else -> return null
        }
    }

    fun initCanvasBgsPopupWindow(layoutRes: Int, groupId: Int, clickListener: View.OnClickListener): ImageView {
        val layoutView = mMainActivity.layoutInflater.inflate(layoutRes, null)
        canvasBgsPopupWindow = initPopupWindow(layoutView, groupId, object : View.OnClickListener {
            override fun onClick(v: View) {
                clickListener.onClick(v)

                if (v.id == R.id.btnCanvasBg8) {
                    val intent = Intent(Intent.ACTION_PICK, null)
                    intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, MainActivity.IMAGE_UNSPECIFIED)
                    mMainActivity.startActivityForResult(intent, MainActivity.REQUEST_CODE_PICTURE)
                    return
                }
                if (v.id == R.id.btnCanvasBg9) {
                    capturePhoto(MainActivity.REQUEST_CODE_GRAPH)
                    return
                }
            }
        })
        return layoutView.findViewById(R.id.btnCanvasBg0)
    }

    private fun initPopupWindow(layoutView: View, groupId: Int, clickListener: View.OnClickListener): PopupWindow {
        val viewGroup = layoutView.findViewById<ViewGroup>(groupId)
        var i = 0
        val count = viewGroup.childCount
        while (i < count) {
            viewGroup.getChildAt(i).setOnClickListener(clickListener)
            i++
        }
        return PopupWindow(layoutView, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
    }

    fun getDrawRes(viewId: Int): Int {
        when (viewId) {
            R.id.ivBessel -> return R.drawable.sel_pel_bessel
            R.id.ivBrokenLine -> return R.drawable.sel_pel_broken_line
            R.id.ivFreehand -> return R.drawable.sel_pel_free_hand
            R.id.ivLine -> return R.drawable.sel_pel_line
            R.id.ivOval -> return R.drawable.sel_pel_oval
            R.id.ivPolygon -> return R.drawable.sel_pel_polygon
            R.id.ivRect -> return R.drawable.sel_pel_rect
            R.id.ivKeepDrawing -> return R.drawable.sel_pel_keep_drawing
            else -> return 0
        }
    }

    fun getBgSelectedRes(viewId: Int): Int {
        when (viewId) {
            R.id.btnCanvasBg0 -> return R.drawable.bg_canvas0
            R.id.btnCanvasBg1 -> return R.drawable.bg_canvas1
            R.id.btnCanvasBg2 -> return R.drawable.bg_canvas2
            R.id.btnCanvasBg3 -> return R.drawable.bg_canvas3
            R.id.btnCanvasBg4 -> return R.drawable.bg_canvas4
            R.id.btnCanvasBg5 -> return R.drawable.bg_canvas5
            R.id.btnCanvasBg6 -> return R.drawable.bg_canvas6
            R.id.btnCanvasBg7 -> return R.drawable.bg_canvas7
            else -> return 0
        }
    }

    fun onSaveGraffitiClicked(savedBitmap: Bitmap, workName: String, saveListener: View.OnClickListener?) {
        if (TextUtils.isEmpty(workName)) {
            Toast.makeText(mMainActivity, mMainActivity.getString(R.string.save_error_null), Toast.LENGTH_SHORT).show()
            return
        }
        val path = Environment.getExternalStorageDirectory().path + BASE_PATH
        var file = File(path)
        if (!file.exists()) {
            file.mkdirs()
        }
        val savedPath = "$path/$workName.png"
        file = File(savedPath)
        if (!file.exists()) {
            saveGraffiti(savedBitmap, savedPath, saveListener)
            return
        }
        //询问用户是否覆盖提示框
        AlertDialog.Builder(mMainActivity)
                .setIcon(android.R.drawable.ic_dialog_info)
                .setMessage(R.string.name_conflict)
                .setPositiveButton(R.string.cover) { dialog, which -> saveGraffiti(savedBitmap, savedPath, saveListener) }
                .setNegativeButton(R.string.cancel, null)
                .create()
                .show()
    }

    private fun saveGraffiti(bitmap: Bitmap, savedPath: String, saveListener: View.OnClickListener?) {
        var fileOutputStream: FileOutputStream? = null
        try {
            fileOutputStream = FileOutputStream(savedPath)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream)
            Toast.makeText(mMainActivity, mMainActivity.getString(R.string.save_to, savedPath), Toast.LENGTH_SHORT).show()
            saveListener?.onClick(null)
        } catch (e: Exception) {
            Toast.makeText(mMainActivity, mMainActivity.getString(R.string.save_error, e.message), Toast.LENGTH_SHORT).show()
        } finally {
            if (fileOutputStream == null) {
                return
            }
            try {
                fileOutputStream.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }
    }

    fun loadPictureFromIntent(data: Intent, canvasWidth: Int, canvasHeight: Int): Observable<Bitmap> {
        return Observable
                .create(ObservableOnSubscribe<Bitmap>{ e ->
                    try {
                        val uri = data.data
                        val file = File(uri!!.path)
                        val fis = FileInputStream(file)
                        val pic = BitmapFactory.decodeStream(fis)
                        fis.close()

                        e.onNext(pic)

                    } catch (exp: Exception) {
                        val uri = data.data
                        val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)
                        val cursor = mMainActivity.contentResolver.query(uri!!, filePathColumn, null, null, null)
                        cursor!!.moveToFirst()
                        val columnIndex = cursor.getColumnIndex(filePathColumn[0])
                        val picturePath = cursor.getString(columnIndex)
                        cursor.close()

                        val op = BitmapFactory.Options()
                        op.inJustDecodeBounds = true
                        var pic = BitmapFactory.decodeFile(picturePath, op)
                        val xScale = op.outWidth / canvasWidth
                        val yScale = op.outHeight / canvasHeight
                        op.inSampleSize = if (xScale > yScale) xScale else yScale
                        op.inJustDecodeBounds = false
                        pic = BitmapFactory.decodeFile(picturePath, op)

                        e.onNext(pic)
                    }

                    e.onComplete()
                } as ObservableOnSubscribe<Bitmap>)
                .subscribeOn(Schedulers.single())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(mMainActivity.bindUntilEvent(ActivityEvent.DESTROY))
    }

    fun loadCapturePhoto(): Observable<Bitmap> {
        return Observable
                .create(ObservableOnSubscribe<Bitmap>{ e ->
                    try {
                        //获取图片
                        val file = File(Environment.getExternalStorageDirectory().toString() + "/temp.jpg")
                        val fis = FileInputStream(file)
                        val opts = BitmapFactory.Options()
                        opts.inJustDecodeBounds = false
                        opts.inSampleSize = 2

                        e.onNext(BitmapFactory.decodeStream(fis, null, opts))
                        e.onComplete()

                        file.delete()
                    } catch (exp: Exception) {
                        exp.printStackTrace()
                    }

                    e.onComplete()
                } as ObservableOnSubscribe<Bitmap>)
                .subscribeOn(Schedulers.single())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(mMainActivity.bindUntilEvent(ActivityEvent.DESTROY))
    }

    fun setListenerForChildren(parentId: Int, listener: View.OnClickListener) {
        val viewGroup = mMainActivity.findViewById<ViewGroup>(parentId)
        var child: View
        var i = 0
        val count = viewGroup.childCount
        while (i < count) {
            child = viewGroup.getChildAt(i)
            if (child is ViewGroup) {
                setListenerForChildren(child.getId(), listener)
            } else {
                viewGroup.getChildAt(i).setOnClickListener(listener)
            }
            i++
        }
    }

    interface OnPickPelListener {
        fun onPelPick(v: View, pelTouch: Touch?)
    }

    companion object {
        private val BASE_PATH = "/painter/graffiti"
    }
}
