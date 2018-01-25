package com.ue.painter

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.youth.banner.BannerConfig
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val images = arrayListOf(R.mipmap.a, R.mipmap.b, R.mipmap.c, R.mipmap.d, R.mipmap.e)
        val titles = arrayListOf("aaaaaaa", "bbbbbbbbbbb", "cccccccccccc", "dddddddddddddd", "eeeeeee")

        //默认是CIRCLE_INDICATOR
        banner.setImages(images)
                .setBannerTitles(titles)
                .setImageLoader(PicassoImageLoader())
                .setBannerStyle(BannerConfig.NUM_INDICATOR_TITLE)
                .start()

        rvModules.setHasFixedSize(true)
        rvModules.adapter = ModuleAdapter()
    }
}
