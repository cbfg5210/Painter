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
        rvModules.adapter = ModuleAdapter(this)
        //banner,not display if not start()
        banner.setImages(arrayListOf(R.mipmap.ic_launcher))
                .setBannerTitles(arrayListOf(getString(R.string.no_works_display)))
                .setImageLoader(PicassoImageLoader())
                .setBannerStyle(BannerConfig.NUM_INDICATOR_TITLE)
                .start()

        Work.get()
                .getRecentWorks(this, object : Work.OnFetchWorksListener {
                    override fun onWorksFetched(works: ArrayList<WorkItem>) {
                        if (works.isEmpty()) return

                        val images = ArrayList<Any>()
                        val titles = ArrayList<String>()
                        works.apply {
                            forEach {
                                titles.add(it.name)
                                images.add(it.path)
                            }
                        }
                        banner.update(images, titles)
                    }
                })
    }
}