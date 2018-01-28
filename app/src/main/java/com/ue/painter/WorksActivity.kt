package com.ue.painter

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.StaggeredGridLayoutManager
import android.view.MenuItem
import com.squareup.picasso.Picasso
import com.trello.rxlifecycle2.components.support.RxAppCompatActivity
import com.ue.coloring.feature.main.WorksAdapter
import com.ue.library.util.toast
import com.ue.painter.model.Work
import com.ue.painter.model.WorkShop
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

    private lateinit var adapter: WorksAdapter

    private var isLoadingData = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.co_fragment_works)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        ervWorks.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        ervWorks.addItemDecoration(TimeLineDecoration(this, (resources.displayMetrics.widthPixels * 0.085f).toInt()))

        adapter = WorksAdapter(this)
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
        WorkShop.get().getAllWorks(this, object : WorkShop.OnFetchWorksListener {
            override fun onWorksFetched(works: List<Work>) {
                if (works.isEmpty()) toast(R.string.no_works)
                else {
                    adapter.items.addAll(works)
                    adapter.notifyDataSetChanged()
                }
                isLoadingData = false
            }
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}