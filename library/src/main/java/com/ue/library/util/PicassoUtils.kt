package com.ue.library.util

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.widget.ImageView

import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import com.ue.library.event.SimpleTarget

/**
 * Created by hawk on 2017/12/24.
 */

object PicassoUtils {
    fun displayImage(context: Context, iv: ImageView, imageUrl: String) {
        Picasso.with(context)
                .load(imageUrl)
                .into(iv)
    }

    fun displayImage(context: Context, iv: ImageView, imageUrl: String, picassoListener: Target) {
        iv.tag = object : SimpleTarget() {
            override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom) {
                if (context is Activity && context.isFinishing) {
                    return
                }
                iv.setImageBitmap(bitmap)
                picassoListener.onBitmapLoaded(bitmap, from)
            }

            override fun onBitmapFailed(errorDrawable: Drawable?) {
                if (context == null || context is Activity && context.isFinishing) {
                    return
                }
                if (errorDrawable != null) iv.setImageDrawable(errorDrawable)
                picassoListener.onBitmapFailed(errorDrawable)
            }
        }

        Picasso.with(context)
                .load(imageUrl)
                .into(iv.tag as Target)
    }
}
