package com.ue.autodraw

import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.widget.AdapterView
import android.widget.RadioGroup
import android.widget.Toast
import com.ue.autodraw.dialog.TabBgDialog
import com.ue.library.util.RxJavaUtils
import io.reactivex.Observable
import io.reactivex.ObservableOnSubscribe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_auto_draw.*

class AutoDrawActivity : AppCompatActivity(), View.OnTouchListener, RadioGroup.OnCheckedChangeListener {

    private var sobelBm: Bitmap? = null
    private var first = true
    private var disposable: Disposable? = null

    companion object {
        private val REQ_SIZE = 150
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auto_draw)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.auto_draw)

        advOutline.setPaintBitmap(AutoDrawUtils.getRatioBitmap(this, R.drawable.paint, 10, 20))
//        advOutline.setBgBitmapRes(R.drawable.fs0)
//        advOutline.setBgBitmap(AutoDrawUtils.getRatioBitmap(this, R.drawable.fs0, REQ_SIZE, REQ_SIZE))

        advOutline.setOnTouchListener(this)
        rgTabs.setOnCheckedChangeListener(this)

        loadBitmapToDraw()
    }

    private fun loadBitmapToDraw() {
        disposable = Observable
                .create(ObservableOnSubscribe<Any> {
                    //将Bitmap压缩处理，防止OOM
                    val bm = AutoDrawUtils.getRatioBitmap(this, R.drawable.bg_4, REQ_SIZE, REQ_SIZE)
                    //Log.e("AutoDrawActivity", "onCreate: bm w=${bm.width},h=${bm.height}")
                    //480x800,648x1152
                    //返回的是处理过的Bitmap
                    sobelBm =
                            if (resources.displayMetrics.widthPixels >= 1080) SobelUtils.sobel(bm, 648, 1152)
                            else SobelUtils.sobel(bm, 480, 800)
                })
                .subscribeOn(Schedulers.single())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe()
    }

    //根据Bitmap信息，获取每个位置的像素点是否需要绘制
    //使用boolean数组而不是int[][]主要是考虑到内存的消耗
    private fun getArray(bitmap: Bitmap): Array<BooleanArray> {
        val b = Array(bitmap.width) { BooleanArray(bitmap.height) }

        for (i in 0 until bitmap.width) {
            for (j in 0 until bitmap.height) {
                b[i][j] = bitmap.getPixel(i, j) != Color.WHITE
            }
        }
        return b
    }

    override fun onTouch(v: View, event: MotionEvent?): Boolean {
        when (v.id) {
            R.id.advOutline -> {
                sobelBm ?: return true
                if (first) {
                    first = false
                    advOutline.beginDraw(getArray(sobelBm!!))
                } else {
                    advOutline.reDraw(getArray(sobelBm!!))
                }
            }
        }
        return true
    }

    override fun onCheckedChanged(group: RadioGroup?, checkedId: Int) {
        when (checkedId) {
            R.id.rbTabBackground -> {
                val bgDialog = TabBgDialog()
                bgDialog.itemListener = AdapterView.OnItemClickListener { _, _, imgRes, _ ->

                }
                bgDialog.show(supportFragmentManager, "")
            }
            R.id.rbTabPaint -> {
            }
            R.id.rbTabLine -> {
            }
            R.id.rbTabSpeed -> {
            }
            R.id.rbTabSave -> {
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_auto_draw, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
            R.id.actionShareDrawPicture -> {
                Toast.makeText(this, "picture", Toast.LENGTH_SHORT).show()
            }
            R.id.actionShareDrawVideo -> {
                Toast.makeText(this, "video", Toast.LENGTH_SHORT).show()
            }
        }
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        RxJavaUtils.dispose(disposable)
    }
}
