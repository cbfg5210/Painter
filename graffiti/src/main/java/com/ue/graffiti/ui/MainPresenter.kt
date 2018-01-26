package com.ue.graffiti.ui

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
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupWindow
import com.trello.rxlifecycle2.android.ActivityEvent
import com.ue.graffiti.R
import com.ue.graffiti.touch.*
import com.ue.graffiti.util.loadToggleMenuAnimations
import com.ue.library.util.toast
import com.ue.graffiti.widget.CanvasView
import com.ue.library.constant.Constants
import com.ue.library.util.FileUtils
import io.reactivex.Observable
import io.reactivex.ObservableOnSubscribe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.io.File
import java.io.FileInputStream

/**
 * Created by hawk on 2018/1/16.
 */

class MainPresenter(private val mGraffitiActivity: GraffitiActivity) {
    private var pelsPopupWindow: PopupWindow? = null
    private var canvasBgsPopupWindow: PopupWindow? = null
    //四个方向的显示动画：左、上、右、下
    private var showAnimations: Array<Animation>? = null
    //四个方向的隐藏动画：左、上、右、下
    private var hideAnimations: Array<Animation>? = null

    fun getToggleAnimations(isShown: Boolean): Array<Animation> {
        return if (isShown) {
            if (showAnimations == null) {
                showAnimations = loadToggleMenuAnimations(mGraffitiActivity, isShown)
            }
            showAnimations!!
        } else {
            if (hideAnimations == null) {
                hideAnimations = loadToggleMenuAnimations(mGraffitiActivity, isShown)
            }
            hideAnimations!!
        }
    }

    fun capturePhoto(REQUEST_CODE_GRAPH: Int) {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(File(Environment.getExternalStorageDirectory(), "temp.jpg")))
        intent.putExtra("outputFormat", Bitmap.CompressFormat.PNG.name)
        intent.putExtra(MediaStore.EXTRA_SCREEN_ORIENTATION, Configuration.ORIENTATION_LANDSCAPE)
        mGraffitiActivity.startActivityForResult(intent, REQUEST_CODE_GRAPH)
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
        val layoutView = mGraffitiActivity.layoutInflater.inflate(layoutRes, null)
        pelsPopupWindow = initPopupWindow(layoutView, groupId, View.OnClickListener { v -> pickPelListener.onPelPick(v, getPelTouchByViewId(v.id, cvGraffitiView)) })
        return layoutView.findViewById(R.id.ivFreehand)
    }

    fun getPelTouchByViewId(viewId: Int, cvGraffitiView: CanvasView): Touch? {
        return when (viewId) {
            R.id.ivFreehand -> DrawFreehandTouch(cvGraffitiView)
            R.id.ivRect -> DrawRectTouch(cvGraffitiView)
            R.id.ivBessel -> DrawBesselTouch(cvGraffitiView)
            R.id.ivOval -> DrawOvalTouch(cvGraffitiView)
            R.id.ivLine -> DrawLineTouch(cvGraffitiView)
            R.id.ivBrokenLine -> DrawBrokenLineTouch(cvGraffitiView)
            R.id.ivPolygon -> DrawPolygonTouch(cvGraffitiView)
            R.id.ivKeepDrawing -> KeepDrawingTouch(cvGraffitiView)
            else -> null
        }
    }

    fun initCanvasBgsPopupWindow(layoutRes: Int, groupId: Int, clickListener: View.OnClickListener): ImageView {
        val layoutView = mGraffitiActivity.layoutInflater.inflate(layoutRes, null)
        canvasBgsPopupWindow = initPopupWindow(layoutView, groupId, View.OnClickListener { v ->
            clickListener.onClick(v)
            if (v.id == R.id.btnCanvasBg8) {
                val intent = Intent(Intent.ACTION_PICK, null)
                intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, GraffitiActivity.IMAGE_UNSPECIFIED)
                mGraffitiActivity.startActivityForResult(intent, GraffitiActivity.REQUEST_CODE_PICTURE)
                return@OnClickListener
            }
            if (v.id == R.id.btnCanvasBg9) {
                capturePhoto(GraffitiActivity.REQUEST_CODE_GRAPH)
                return@OnClickListener
            }
        })
        return layoutView.findViewById(R.id.btnCanvasBg0)
    }

    private fun initPopupWindow(layoutView: View, groupId: Int, clickListener: View.OnClickListener): PopupWindow {
        val viewGroup = layoutView.findViewById<ViewGroup>(groupId)
        for (i in 0 until viewGroup.childCount) {
            viewGroup.getChildAt(i).setOnClickListener(clickListener)
        }
        return PopupWindow(layoutView, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
    }

    fun getDrawRes(viewId: Int): Int {
        return when (viewId) {
            R.id.ivBessel -> R.drawable.gr_sel_pel_bessel
            R.id.ivBrokenLine -> R.drawable.gr_sel_pel_broken_line
            R.id.ivFreehand -> R.drawable.gr_sel_pel_free_hand
            R.id.ivLine -> R.drawable.gr_sel_pel_line
            R.id.ivOval -> R.drawable.gr_sel_pel_oval
            R.id.ivPolygon -> R.drawable.gr_sel_pel_polygon
            R.id.ivRect -> R.drawable.gr_sel_pel_rect
            R.id.ivKeepDrawing -> R.drawable.gr_sel_pel_keep_drawing
            else -> 0
        }
    }

    fun getBgSelectedRes(viewId: Int): Int {
        return when (viewId) {
            R.id.btnCanvasBg0 -> R.drawable.bg_canvas0
            R.id.btnCanvasBg1 -> R.drawable.bg_canvas1
            R.id.btnCanvasBg2 -> R.drawable.bg_canvas2
            R.id.btnCanvasBg3 -> R.drawable.bg_canvas3
            R.id.btnCanvasBg4 -> R.drawable.bg_canvas4
            R.id.btnCanvasBg5 -> R.drawable.bg_canvas5
            R.id.btnCanvasBg6 -> R.drawable.bg_canvas6
            R.id.btnCanvasBg7 -> R.drawable.bg_canvas7
            else -> 0
        }
    }

    fun onSaveGraffitiClicked(savedBitmap: Bitmap, workName: String, saveListener: FileUtils.OnSaveImageListener?) {
        if (TextUtils.isEmpty(workName)) {
            mGraffitiActivity.toast(R.string.gr_save_error_null)
            return
        }
        val path = Environment.getExternalStorageDirectory().path + Constants.PATH_GRAFFITI
        FileUtils.saveImageLocally(mGraffitiActivity, savedBitmap, path, workName, saveListener)
    }

    fun loadPictureFromIntent(data: Intent, canvasWidth: Int, canvasHeight: Int): Observable<Bitmap> {
        return Observable
                .create(ObservableOnSubscribe<Bitmap> { e ->
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
                        val cursor = mGraffitiActivity.contentResolver.query(uri!!, filePathColumn, null, null, null)
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
                })
                .subscribeOn(Schedulers.single())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(mGraffitiActivity.bindUntilEvent(ActivityEvent.DESTROY))
    }

    fun loadCapturePhoto(): Observable<Bitmap> {
        return Observable
                .create(ObservableOnSubscribe<Bitmap> { e ->
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
                })
                .subscribeOn(Schedulers.single())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(mGraffitiActivity.bindUntilEvent(ActivityEvent.DESTROY))
    }

    interface OnPickPelListener {
        fun onPelPick(v: View, pelTouch: Touch?)
    }
}
