package com.ue.autodraw

import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.MotionEvent
import android.view.View
import kotlinx.android.synthetic.main.activity_auto_draw.*

class AutoDrawActivity : AppCompatActivity(), View.OnTouchListener {

    private lateinit var sobelBm: Bitmap
    private var first = true

    companion object {
        private val REQ_SIZE = 150
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auto_draw)

        //将Bitmap压缩处理，防止OOM
        val bm = AutoDrawUtils.getRatioBitmap(this, R.drawable.bg_4, REQ_SIZE, REQ_SIZE)
        //Log.e("AutoDrawActivity", "onCreate: bm w=${bm.width},h=${bm.height}")

        //480x800,648x1152
        var reqWidth: Int
        var reqHeight: Int
        if (resources.displayMetrics.widthPixels >= 1080) {
            reqWidth = 648
            reqHeight = 1152
        } else {
            reqWidth = 480
            reqHeight = 800
        }
        //Log.e("AutoDrawActivity", "onCreate: reqWidth=$reqWidth,reqHeight=$reqHeight")
        //返回的是处理过的Bitmap
        sobelBm = SobelUtils.sobel(bm, reqWidth, reqHeight)

        val paintBm = AutoDrawUtils.getRatioBitmap(this, R.drawable.paint, 10, 20)
        outline.setPaintBm(paintBm)

        dd.setOnTouchListener(this)
        outline.setOnTouchListener(this)
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
            R.id.dd -> dd.visibility = View.GONE
            R.id.outline -> {
                if (first) {
                    first = false
                    outline.beginDraw(getArray(sobelBm))
                } else {
                    outline.reDraw(getArray(sobelBm))
                }
            }
        }
        return true
    }
}
