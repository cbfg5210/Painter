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
import com.ue.adapterdelegate.Item
import com.ue.fingercoloring.R
import com.ue.coloring.constant.Constants
import com.ue.coloring.model.ThemeItem
import com.ue.coloring.model.ThemeTitle
import kotlinx.android.synthetic.main.co_fragment_themes.view.*
import java.io.IOException
import java.util.*

/**
 * Created by Swifty.Wang on 2015/8/14.
 */
class ThemesFragment : Fragment() {
    private var items: List<Item>? = null
    private lateinit var adapter: ThemeItemAdapter

    companion object {
        val TAG_THEMES = "themes"

        fun newInstance(): ThemesFragment {
            return ThemesFragment()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.co_fragment_themes, container, false)

        adapter = ThemeItemAdapter(activity, items)
        rootView.theme_list.adapter = adapter

        val layoutManager = FlexboxLayoutManager(context)
        layoutManager.flexDirection = FlexDirection.ROW
        rootView.theme_list.layoutManager = layoutManager

        rootView.theme_list.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView?, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    Picasso.with(context).resumeTag(TAG_THEMES)
                } else if (newState == RecyclerView.SCROLL_STATE_SETTLING) {
                    Picasso.with(context).pauseTag(TAG_THEMES)
                }
            }
        })
        return rootView
    }

    private fun loadData() {
        val itemList = ArrayList<Item>()
        itemList.add(ThemeTitle(Constants.ASSETS + "secretgarden.jpg", getString(R.string.co_secret_garden)))
        try {
            val images = Arrays.asList(*context.assets.list("SecretGarden"))

            val prefix = Constants.ASSETS + "SecretGarden/"
            var i = 0
            val len = images.size
            while (i < len) {
                itemList.add(ThemeItem(images[i], prefix + images[i]))
                i++
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }

        adapter.items.clear()
        adapter.items.addAll(itemList)
        adapter.notifyDataSetChanged()

        items = itemList
    }

    /*private void loadTestData() {
        List<Item> itemList = new ArrayList<>();
        itemList.add(new ThemeTitle(Constants.ASSETS + "secretgarden.jpg", getString(R.string.secretGarden)));
        try {
            List<String> images = Arrays.asList(getContext().getAssets().list("SecretGarden"));

            String prefix = Constants.ASSETS + "SecretGarden/";
            for (int i = 0, len = images.size(); i < len; i++) {
                itemList.add(new ThemeItem(images.get(i), prefix + images.get(i)));
            }

            itemList.add(new ThemeTitle(Constants.ASSETS + "secretgarden.jpg", getString(R.string.secretGarden)));
            for (int i = 0, len = images.size(); i < len; i++) {
                itemList.add(new ThemeItem(images.get(i), prefix + images.get(i)));
            }

            itemList.add(new ThemeTitle(Constants.ASSETS + "secretgarden.jpg", getString(R.string.secretGarden)));
            for (int i = 0, len = images.size(); i < len; i++) {
                itemList.add(new ThemeItem(images.get(i), prefix + images.get(i)));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        adapter.getItems().clear();
        adapter.getItems().addAll(itemList);
        adapter.notifyDataSetChanged();

        items = itemList;
    }*/

    override fun onResume() {
        super.onResume()
        items ?: loadData()//loadTestData();
    }
}
