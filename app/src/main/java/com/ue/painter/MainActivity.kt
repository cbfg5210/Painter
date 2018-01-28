package com.ue.painter

import android.os.Bundle
import com.trello.rxlifecycle2.components.support.RxAppCompatActivity
import com.ue.painter.model.PictureWork
import com.ue.painter.model.WorkShop
import com.youth.banner.BannerConfig
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : RxAppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        rvModules.setHasFixedSize(true)
        rvModules.adapter = ModuleAdapter(this)
        //banner,not display if not start()
        banner.setImages(arrayListOf(R.mipmap.ic_launcher))
                .setBannerTitles(arrayListOf(getString(R.string.no_works_display)))
                .setImageLoader(PicassoImageLoader())
                .setBannerStyle(BannerConfig.NUM_INDICATOR_TITLE)
                .setOnBannerListener { WorksActivity.start(this) }
                .start()

        WorkShop.get()
                .getRecentWorks(this, object : WorkShop.OnFetchPicWorksListener {
                    override fun onPicWorksFetched(works: ArrayList<PictureWork>) {
                        if (works.isEmpty()) return

                        val images = ArrayList<Any>()
                        val titles = ArrayList<String>()
                        works.forEach {
                            titles.add(it.name)
                            images.add(it.path)
                        }
                        banner.update(images, titles)
                    }
                })
    }
}