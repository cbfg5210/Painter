package com.ue.fingercoloring.feature.main

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.StaggeredGridLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.squareup.picasso.Picasso
import com.ue.fingercoloring.R
import com.ue.fingercoloring.model.LocalWork
import com.ue.fingercoloring.util.FileUtils
import com.ue.fingercoloring.widget.TimeLineDecoration
import com.ue.library.util.RxJavaUtils
import io.reactivex.Observable
import io.reactivex.ObservableOnSubscribe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_works.view.*
import kotlinx.android.synthetic.main.layout_empty.view.*

class WorksFragment : Fragment() {
    private lateinit var adapter: LocalPaintAdapter
    private var localWorks: List<LocalWork>? = null
    private var disposable: Disposable? = null

    //--start
    //is loading data
    private var isLoadingData = false
    //status--end

    companion object {
        val TAG_WORKS = "works"
        fun newInstance(): WorksFragment {
            return WorksFragment()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_works, container, false)

        rootView.ervWorks.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        rootView.ervWorks.addItemDecoration(TimeLineDecoration(context, (resources.displayMetrics.widthPixels * 0.085f).toInt()))

        adapter = LocalPaintAdapter(activity, localWorks)
        rootView.ervWorks.adapter = adapter
        rootView.ervWorks.setEmptyView(rootView.vgEmptyPanel)

        rootView.ervWorks.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView?, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    Picasso.with(context).resumeTag(TAG_WORKS)
                } else if (newState == RecyclerView.SCROLL_STATE_SETTLING) {
                    Picasso.with(context).pauseTag(TAG_WORKS)
                }
            }
        })

        return rootView
    }

    private fun loadLocalWorks() {
        if (isLoadingData) return

        isLoadingData = true
        disposable = Observable
                .create(ObservableOnSubscribe<List<LocalWork>> { e ->
                    val results = FileUtils.obtainLocalImages()
                    e.onNext(results)
                    e.onComplete()
                })
                .subscribeOn(Schedulers.single())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ mLocalWorks ->
                    adapter.items.clear()
                    adapter.items.addAll(mLocalWorks)
                    adapter.notifyDataSetChanged()

                    localWorks = mLocalWorks
                    isLoadingData = false
                }, { t ->
                    Toast.makeText(context, getString(R.string.read_data_error, t.message), Toast.LENGTH_SHORT).show()
                })
    }

    override fun onResume() {
        super.onResume()
        //每次都重新获取数据
        loadLocalWorks()
    }

    override fun onDestroy() {
        super.onDestroy()
        RxJavaUtils.dispose(disposable)
    }
}
