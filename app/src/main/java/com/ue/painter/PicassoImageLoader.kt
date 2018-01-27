package com.ue.painter

import android.content.Context
import android.widget.ImageView
import com.ue.library.util.ImageLoaderUtils
import com.youth.banner.loader.ImageLoader

/**
 * Created by hawk on 2018/1/25.
 */
class PicassoImageLoader : ImageLoader() {
    override fun displayImage(context: Context, path: Any, imageView: ImageView) {
        //Log.e("PicassoImageLoader", "displayImage: iv width=${imageView.width},height=${imageView.height},measureW=${imageView.measuredWidth},measureH=${imageView.measuredHeight}")
        //both width and height = 0
        //shape is changed
        //val width = activity.resources.displayMetrics.widthPixels
        //val height = activity.resources.getDimensionPixelSize(R.dimen.widget_size_200)
        //ImageLoaderUtils.display(imageView, path, Point(width, height))
        ImageLoaderUtils.display(imageView, path)
    }
}