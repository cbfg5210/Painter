package com.ue.library.util

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Point
import android.graphics.drawable.Drawable
import android.util.Size
import android.widget.ImageView
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import com.ue.library.event.SimpleTarget

/**
 * Created by hawk on 2018/1/10.
 */
object ImageLoaderUtils {
    fun display(context: Context, iv: ImageView, imgUrl: String) {
        Picasso.with(context)
                .load(imgUrl)
                .into(iv)
    }

    fun display(context: Context, iv: ImageView, imgSrc: Int) {
        Picasso.with(context)
                .load(imgSrc)
                .into(iv)
    }

    fun display(context: Context, iv: ImageView, imgSrc: Int, size: Point) {
        Picasso.with(context)
                .load(imgSrc)
                .resize(size.x, size.y)
                .into(iv)
    }

    fun display(context: Context, iv: ImageView, imgUrl: String, callback: ImageLoaderCallback) {
        iv.tag = object : SimpleTarget() {
            override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom) {
                if (context is Activity && context.isFinishing) {
                    return
                }
                iv.setImageBitmap(bitmap)
                callback.onBitmapLoaded(bitmap)
            }

            override fun onBitmapFailed(errorDrawable: Drawable?) {
                if (context == null || context is Activity && context.isFinishing) {
                    return
                }
                if (errorDrawable != null) {
                    iv.setImageDrawable(errorDrawable)
                }
                callback.onBitmapFailed()
            }
        }

        Picasso.with(context)
                .load(imgUrl)
                .into(iv.tag as Target)
    }

    interface ImageLoaderCallback {
        fun onBitmapLoaded(bitmap: Bitmap?)
        fun onBitmapFailed()
    }
}