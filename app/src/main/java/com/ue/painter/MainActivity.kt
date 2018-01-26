package com.ue.painter

import android.os.Bundle
import com.trello.rxlifecycle2.components.support.RxAppCompatActivity
import com.ue.painter.model.Work
import com.ue.painter.model.WorkItem
import com.youth.banner.BannerConfig
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : RxAppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        rvModules.setHasFixedSize(true)
        rvModules.adapter = ModuleAdapter()

        Work.get()
                .getRecentWorks(this)
                .subscribe { works ->
                    val images = ArrayList<Any>()
                    val titles = ArrayList<String>()
                    works.apply {
                        if (isEmpty()) add(WorkItem("充场", R.mipmap.ic_launcher))
                        forEach {
                            titles.add(it.name)
                            images.add(it.path)
                        }
                    }
                    //默认是CIRCLE_INDICATOR
                    banner.setImages(images)
                            .setBannerTitles(titles)
                            .setImageLoader(PicassoImageLoader())
                            .setBannerStyle(BannerConfig.NUM_INDICATOR_TITLE)
                            .start()
                }
    }
}