package com.ue.library.util

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Point
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.widget.ImageView
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import com.ue.library.event.SimpleTarget

/**
 * Created by hawk on 2018/1/10.
 */
object ImageLoaderUtils {
    fun display(iv: ImageView, image: Any) {
        if (image is Int) Picasso.with(iv.context).load(image).into(iv)
        else if (image is String) Picasso.with(iv.context).load(image).into(iv)
    }

    fun display(iv: ImageView, image: Any, size: Point) {
        if (image is Int) Picasso.with(iv.context).load(image).resize(size.x, size.y).into(iv)
        else if (image is String) Picasso.with(iv.context).load(image).resize(size.x, size.y).into(iv)
    }

    fun display(iv: ImageView, image: Any, errorDrawableRes: Int, callback: ImageLoaderCallback) {
        iv.tag = object : SimpleTarget() {
            override fun onBitmapLoaded(bitmap: Bitmap, from: Picasso.LoadedFrom) {
                val context = iv.context
                if (context is Activity && context.isFinishing) {
                    return
                }
                iv.setImageBitmap(bitmap)
                callback.onBitmapLoaded(bitmap)
            }

            override fun onBitmapFailed(errorDrawable: Drawable?) {
                val context = iv.context
                if (context is Activity && context.isFinishing) {
                    return
                }
                if (errorDrawable != null) {
                    iv.setImageDrawable(errorDrawable)
                }
                callback.onBitmapFailed((errorDrawable as? BitmapDrawable)?.bitmap)
            }
        }
        if (image is String) Picasso.with(iv.context).load(image).error(errorDrawableRes).into(iv.tag as Target)
        else if (image is Int) Picasso.with(iv.context).load(image).error(errorDrawableRes).into(iv.tag as Target)
    }

    fun display(iv: ImageView, image: Any, errorDrawableRes: Int, errorTip: String, callback: ImageLoaderCallback2) {
        iv.tag = object : SimpleTarget() {
            override fun onBitmapLoaded(bitmap: Bitmap, from: Picasso.LoadedFrom) {
                val context = iv.context
                if (context is Activity && context.isFinishing) {
                    return
                }
                iv.setImageBitmap(bitmap)
                callback.onBitmapResult(bitmap)
            }

            override fun onBitmapFailed(errorDrawable: Drawable?) {
                val context = iv.context
                if (context is Activity && context.isFinishing) {
                    return
                }
                if (errorDrawable == null) {
                    context.toast(errorTip)
                    callback.onBitmapResult(null)
                } else {
                    iv.setImageDrawable(errorDrawable)
                    callback.onBitmapResult((errorDrawable as? BitmapDrawable)?.bitmap)
                }
            }
        }
        if (image is String) Picasso.with(iv.context).load(image).error(errorDrawableRes).into(iv.tag as Target)
        else if (image is Int) Picasso.with(iv.context).load(image).error(errorDrawableRes).into(iv.tag as Target)
    }

    interface ImageLoaderCallback {
        fun onBitmapLoaded(bitmap: Bitmap)
        fun onBitmapFailed(errorBitmap: Bitmap?)
    }

    interface ImageLoaderCallback2 {
        fun onBitmapResult(bitmap: Bitmap?)
    }
}