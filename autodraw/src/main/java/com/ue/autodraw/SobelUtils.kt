package com.ue.autodraw

import android.graphics.Bitmap
import android.graphics.Bitmap.Config
import android.graphics.Color

object SobelUtils {
    /**
     * Sobel算法
     *
     * @param bitmap
     * @return
     */
    fun Sobel(mBitmap: Bitmap): Bitmap {
        var bitmap = AutoDrawUtils.compress(mBitmap, 480, 800)
        val temp = AutoDrawUtils.toGrayScale(bitmap)
        val w = temp.width
        val h = temp.height

        val mmap = IntArray(w * h)
        val tmap = DoubleArray(w * h)
        val cmap = IntArray(w * h)

        temp.getPixels(mmap, 0, temp.width, 0, 0, temp.width, temp.height)

        var max = java.lang.Double.MIN_VALUE
        for (i in 0 until w) {
            for (j in 0 until h) {
                val gx = GX(i, j, temp)
                val gy = GY(i, j, temp)
                tmap[j * w + i] = Math.sqrt(gx * gx + gy * gy)
                if (max < tmap[j * w + i]) {
                    max = tmap[j * w + i]
                }
            }
        }

        val top = max * 0.06
        for (i in 0 until w) {
            for (j in 0 until h) {
                cmap[j * w + i] = if (tmap[j * w + i] > top) mmap[j * w + i] else Color.WHITE
            }
        }
        return Bitmap.createBitmap(cmap, temp.width, temp.height, Config.ARGB_8888)
    }

    /**
     * 获取横向的
     *
     * @param x      第x行
     * @param y      第y列
     * @param bitmap
     * @return
     */
    fun GX(x: Int, y: Int, bitmap: Bitmap): Double {
        return -1 * getPixel(x - 1, y - 1, bitmap) + 1 * getPixel(x + 1, y - 1, bitmap) + -Math.sqrt(2.0) * getPixel(x - 1, y, bitmap) + Math.sqrt(2.0) * getPixel(x + 1, y, bitmap) + -1 * getPixel(x - 1, y + 1, bitmap) + 1 * getPixel(x + 1, y + 1, bitmap)
    }

    /**
     * 获取纵向的
     *
     * @param x      第x行
     * @param y      第y列
     * @param bitmap
     * @return
     */
    fun GY(x: Int, y: Int, bitmap: Bitmap): Double {
        return 1 * getPixel(x - 1, y - 1, bitmap) + Math.sqrt(2.0) * getPixel(x, y - 1, bitmap) + 1 * getPixel(x + 1, y - 1, bitmap) + -1 * getPixel(x - 1, y + 1, bitmap) + -Math.sqrt(2.0) * getPixel(x, y + 1, bitmap) + -1 * getPixel(x + 1, y + 1, bitmap)
    }

    /**
     * 获取第x行第y列的色度
     *
     * @param x      第x行
     * @param y      第y列
     * @param bitmap
     * @return
     */
    fun getPixel(x: Int, y: Int, bitmap: Bitmap): Double {
        return if (x < 0 || x >= bitmap.width || y < 0 || y >= bitmap.height) 0.0 else bitmap.getPixel(x, y).toDouble()
    }
}
