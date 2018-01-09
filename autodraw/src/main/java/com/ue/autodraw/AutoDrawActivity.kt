package com.ue.autodraw

import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.MotionEvent
import android.view.View
import kotlinx.android.synthetic.main.activity_auto_draw.*

class AutoDrawActivity : AppCompatActivity(), View.OnTouchListener {

    private lateinit var sobelBm: Bitmap
    private var first = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auto_draw)

        //将Bitmap压缩处理，防止OOM
        val bm = AutoDrawUtils.getRatioBitmap(this, R.drawable.test, 150, 150)
        Log.e("AutoDrawActivity", "onCreate: bm w=${bm.width},h=${bm.height},outline w=${outline.measuredWidth},h=${outline.measuredHeight}")
        //返回的是处理过的Bitmap
        sobelBm = SobelUtils.Sobel(bm)

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
