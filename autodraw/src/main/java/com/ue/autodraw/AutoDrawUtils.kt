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

//        Log.e("AutoDrawUtils", "getRatioBitmap: opt w=${newOpts.outWidth},h=${newOpts.outHeight},sampleSize=${newOpts.inSampleSize},div=${newOpts.outWidth / newOpts.inSampleSize}")

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

        val bmpGrayScale = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        val c = Canvas(bmpGrayScale)
        val cm = ColorMatrix()
        cm.setSaturation(0f)
        val paint = Paint()
        paint.colorFilter = ColorMatrixColorFilter(cm)
        c.drawBitmap(bmpOriginal, 0f, 0f, paint)
        return bmpGrayScale
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
        if (bm.height <= reqHeight && bm.width <= reqWidth) return bm

        val matrix = Matrix()
        val scale = Math.min(reqWidth.toFloat() / bm.width, reqHeight.toFloat() / bm.height)
        //scale=0.8f的时候绘制1080x1920图片比较合适
        matrix.postScale(scale, scale)
        val result = Bitmap.createBitmap(bm, 0, 0, bm.width, bm.height, matrix, true)
        bm.recycle()
        return result
    }
}
