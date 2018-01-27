package com.ue.painter

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.StaggeredGridLayoutManager
import android.widget.Toast
import com.squareup.picasso.Picasso
import com.trello.rxlifecycle2.components.support.RxAppCompatActivity
import com.ue.coloring.feature.main.LocalPaintAdapter
import com.ue.painter.model.LocalWork
import com.ue.painter.util.FileUtils
import com.ue.painter.util.bindUtilDestroy2
import io.reactivex.Observable
import io.reactivex.ObservableOnSubscribe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.co_fragment_works.*

/**
 * Created by hawk on 2018/1/27.
 */
class WorksActivity : RxAppCompatActivity() {
    companion object {
        val TAG_WORKS = "works"
        fun start(context: Context) {
            context.startActivity(Intent(context, WorksActivity::class.java))
        }
    }

    private lateinit var adapter: LocalPaintAdapter
    private var localWorks: List<LocalWork>? = null

    private var isLoadingData = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.co_fragment_works)

        ervWorks.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        ervWorks.addItemDecoration(TimeLineDecoration(this, (resources.displayMetrics.widthPixels * 0.085f).toInt()))

        adapter = LocalPaintAdapter(this, localWorks)
        ervWorks.adapter = adapter

        ervWorks.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView?, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    Picasso.with(this@WorksActivity).resumeTag(TAG_WORKS)
                } else if (newState == RecyclerView.SCROLL_STATE_SETTLING) {
                    Picasso.with(this@WorksActivity).pauseTag(TAG_WORKS)
                }
            }
        })

        loadLocalWorks()
    }

    private fun loadLocalWorks() {
        if (isLoadingData) return

        isLoadingData = true
        Observable
                .create(ObservableOnSubscribe<List<LocalWork>> { e ->
                    val results = FileUtils.obtainLocalImages()
                    e.onNext(results)
                    e.onComplete()
                })
                .subscribeOn(Schedulers.single())
                .observeOn(AndroidSchedulers.mainThread())
                .bindUtilDestroy2(this)
                .subscribe({ mLocalWorks ->
                    adapter.items.clear()
                    adapter.items.addAll(mLocalWorks)
                    adapter.notifyDataSetChanged()

                    localWorks = mLocalWorks
                    isLoadingData = false
                }, { t ->
                    Toast.makeText(this, getString(R.string.read_data_error, t.message), Toast.LENGTH_SHORT).show()
                })
    }
}