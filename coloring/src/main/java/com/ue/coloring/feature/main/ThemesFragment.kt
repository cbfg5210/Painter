package com.ue.coloring.feature.main

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.squareup.picasso.Picasso
import com.ue.coloring.constant.Constants
import com.ue.coloring.model.ThemeItem
import com.ue.coloring.model.ThemeTitle
import com.ue.fingercoloring.R
import kotlinx.android.synthetic.main.co_fragment_themes.view.*
import java.io.IOException
import java.util.*

/**
 * Created by Swifty.Wang on 2015/8/14.
 */
class ThemesFragment : Fragment() {
    private lateinit var adapter: ThemeItemAdapter

    companion object {
        val TAG_THEMES = "themes"

        fun newInstance(): ThemesFragment {
            return ThemesFragment()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.co_fragment_themes, container, false)

        adapter = ThemeItemAdapter(activity, loadData())
        rootView.theme_list.adapter = adapter

        rootView.theme_list.layoutManager = FlexboxLayoutManager(context).apply { flexDirection = FlexDirection.ROW }

        rootView.theme_list.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView?, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) Picasso.with(context).resumeTag(TAG_THEMES)
                else if (newState == RecyclerView.SCROLL_STATE_SETTLING) Picasso.with(context).pauseTag(TAG_THEMES)
            }
        })
        return rootView
    }

    private fun loadData(): ArrayList<Any> {
        val items = ArrayList<Any>()
        items.add(ThemeTitle(Constants.ASSETS + "secretgarden.jpg", getString(R.string.co_secret_garden)))
        try {
            val images = context.assets.list("SecretGarden")
            val prefix = "${Constants.ASSETS}SecretGarden/"
            val len = images.size
            (0 until len).mapTo(items) { ThemeItem(images[it], "$prefix${images[it]}", false) }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return items
    }
}
