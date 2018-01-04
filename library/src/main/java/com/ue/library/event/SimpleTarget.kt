package com.ue.library.event

import android.graphics.Bitmap
import android.graphics.drawable.Drawable

import com.squareup.picasso.Picasso
import com.squareup.picasso.Target

/**
 * Created by hawk on 2017/12/26.
 */

open class SimpleTarget : Target {
    override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom) {}

    override fun onBitmapFailed(errorDrawable: Drawable?) {}

    override fun onPrepareLoad(placeHolderDrawable: Drawable?) {}
}
