package com.ue.autodraw

import android.content.Context
import android.graphics.*

/**
 * 共用的工具
 *
 * @author leaf
 */
object AutoDrawUtils {

    fun getRatioBitmap(context: Context, imgId: Int, reqWidth: Int, reqHeight: Int): Bitmap {
        val newOpts = BitmapFactory.Options()
        newOpts.inJustDecodeBounds = true
        BitmapFactory.decodeResource(context.resources, imgId, newOpts)
        newOpts.inSampleSize = calculateInSampleSize(newOpts, reqWidth, reqHeight)

        newOpts.inJustDecodeBounds = false
        return BitmapFactory.decodeResource(context.resources, imgId, newOpts)
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    /**
     * 转化成灰度图
     *
     * @param bmpOriginal
     * @return
     */
    fun toGrayScale(bmpOriginal: Bitmap): Bitmap {
        val height = bmpOriginal.height
        val width = bmpOriginal.width

        val bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        val c = Canvas(bmpGrayscale)
        val cm = ColorMatrix()
        cm.setSaturation(0f)
        val paint = Paint()
        paint.colorFilter = ColorMatrixColorFilter(cm)
        c.drawBitmap(bmpOriginal, 0f, 0f, paint)
        return bmpGrayscale
    }

    /**
     * Bitmap压缩
     *
     * @param bm
     * @param height
     * @param width
     * @return
     */
    fun compress(bm: Bitmap, reqWidth: Int, reqHeight: Int): Bitmap {
        val width = bm.width
        val height = bm.height

        if (height > reqHeight || width > reqWidth) {
            val scaleWidth = reqWidth.toFloat() / width
            val scaleHeight = reqHeight.toFloat() / height
            val scale = if (scaleWidth < scaleHeight) scaleWidth else scaleHeight

            val matrix = Matrix()
            matrix.postScale(scale, scale)
            val result = Bitmap.createBitmap(bm, 0, 0, bm.width, bm.height, matrix, true)
            bm.recycle()
            return result
        }
        return bm
    }
}
