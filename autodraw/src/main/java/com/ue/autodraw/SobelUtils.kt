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
    fun sobel(mBitmap: Bitmap, reqWidth: Int, reqHeight: Int): Bitmap {
        //480x800,648x1152
        var bitmap = AutoDrawUtils.compress(mBitmap, reqWidth, reqHeight)
        //Log.e("SobelUtils", "sobel: compress totalTime=${System.currentTimeMillis()-startTime}")//11
        val temp = AutoDrawUtils.toGrayScale(bitmap)
        //Log.e("SobelUtils", "sobel: toGrayScale totalTime=${System.currentTimeMillis()-startTime}")//18

        var startTime = System.currentTimeMillis()

        val w = temp.width
        val h = temp.height

        val mMap = IntArray(w * h)
        val tMap = DoubleArray(w * h)
        val cMap = IntArray(w * h)

        temp.getPixels(mMap, 0, w, 0, 0, w, h)

        var max = java.lang.Double.MIN_VALUE
        for (i in 0 until w) {
            for (j in 0 until h) {
                val gx = GX(i, j, temp)
                val gy = GY(i, j, temp)
                tMap[j * w + i] = Math.sqrt(gx * gx + gy * gy)
                if (max < tMap[j * w + i]) {
                    max = tMap[j * w + i]
                }
            }
        }

        val top = max * 0.06
        for (i in 0 until w) {
            for (j in 0 until h) {
                cMap[j * w + i] = if (tMap[j * w + i] > top) mMap[j * w + i] else Color.WHITE
            }
        }

        //Log.e("SobelUtils", "sobel: totalTime=${System.currentTimeMillis() - startTime}")//14174

        return Bitmap.createBitmap(cMap, temp.width, temp.height, Config.ARGB_8888)
    }

    /**
     * 获取横向的
     *
     * @param x      第x行
     * @param y      第y列
     * @param bitmap
     * @return
     */
    private fun GX(x: Int, y: Int, bitmap: Bitmap): Double {
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
    private fun GY(x: Int, y: Int, bitmap: Bitmap): Double {
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
    private fun getPixel(x: Int, y: Int, bitmap: Bitmap): Double {
        return if (x < 0 || x >= bitmap.width || y < 0 || y >= bitmap.height) 0.0 else bitmap.getPixel(x, y).toDouble()
    }
}
