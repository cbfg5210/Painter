package com.ue.painter

import android.content.Context
import android.widget.ImageView
import com.ue.library.util.PicassoUtils
import com.youth.banner.loader.ImageLoader

/**
 * Created by hawk on 2018/1/25.
 */
class PicassoImageLoader : ImageLoader() {
    override fun displayImage(context: Context, path: Any, imageView: ImageView) {
        PicassoUtils.displayImage(context, imageView, path)
    }
}