package com.ue.coloring.feature.theme

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.view.MenuItem
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.squareup.picasso.Picasso
import com.ue.coloring.R
import com.ue.coloring.constant.Constants
import com.ue.coloring.constant.Constants.TAG_COLORING_THEMES
import com.ue.coloring.model.ThemeItem
import com.ue.coloring.model.ThemeTitle
import com.ue.library.event.SimplePermissionListener
import com.ue.library.util.PermissionUtils
import kotlinx.android.synthetic.main.co_activity_coloring_themes.*
import java.io.IOException
import java.util.*

/**
 *  检查外部存储权限，如果没有授予权限则退出
 */
class ColoringThemesActivity : AppCompatActivity() {

    companion object {
        fun start(context: Context) {
            context.startActivity(Intent(context, ColoringThemesActivity::class.java))
        }
    }

    private lateinit var adapter: ThemeItemAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.co_activity_coloring_themes)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        checkExternalPermissions()
    }

    private fun initViews() {
        adapter = ThemeItemAdapter(this, loadData())
        rvThemes.adapter = adapter

        rvThemes.layoutManager = FlexboxLayoutManager(this).apply { flexDirection = FlexDirection.ROW }

        rvThemes.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView?, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) Picasso.with(this@ColoringThemesActivity).resumeTag(TAG_COLORING_THEMES)
                else if (newState == RecyclerView.SCROLL_STATE_SETTLING) Picasso.with(this@ColoringThemesActivity).pauseTag(TAG_COLORING_THEMES)
            }
        })
    }

    private fun loadData(): ArrayList<Any> {
        val items = ArrayList<Any>()
        items.add(ThemeTitle(Constants.THEME_DEFAULT_IMAGE, getString(R.string.co_secret_garden)))
        try {
            val images = assets.list(Constants.THEME_DEFAULT)
            val len = images.size
            (0 until len).mapTo(items) { ThemeItem(images[it], "${Constants.THEME_DEFAULT_PREFIX}${images[it]}", false) }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return items
    }

    private fun checkExternalPermissions() {
        PermissionUtils.checkReadWriteStoragePerms(this, getString(R.string.co_no_external_permission), object : SimplePermissionListener() {
            override fun onSucceed(requestCode: Int, grantPermissions: MutableList<String>) {
                super.onSucceed(requestCode, grantPermissions)
                initViews()
            }

            override fun onFailed(requestCode: Int, deniedPermissions: MutableList<String>) {
                super.onFailed(requestCode, deniedPermissions)
                finish()
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