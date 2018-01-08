package com.ue.autodraw

import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.MotionEvent
import kotlinx.android.synthetic.main.activity_auto_draw.*

class AutoDrawActivity : AppCompatActivity() {
    private lateinit var sobelBm: Bitmap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auto_draw)

        //将Bitmap压缩处理，防止OOM
        val bm = CommenUtils.getRatioBitmap(this, R.drawable.test, 100, 100)
        //返回的是处理过的Bitmap
        sobelBm = SobelUtils.Sobel(bm)

        val paintBm = CommenUtils.getRatioBitmap(this, R.drawable.paint, 10, 20)
        outline.setPaintBm(paintBm)
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

    private var first = true

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (first) {
            first = false
            outline.beginDraw(getArray(sobelBm))
        } else {
            outline.reDraw(getArray(sobelBm))
        }
        return true
    }
}
