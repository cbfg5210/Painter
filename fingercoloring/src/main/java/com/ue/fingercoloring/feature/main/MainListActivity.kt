package com.ue.fingercoloring.feature.main

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentStatePagerAdapter
import android.support.v7.app.AppCompatActivity

import android.view.MenuItem
import android.widget.Toast
import com.ue.fingercoloring.R
import com.yanzhenjie.permission.AndPermission
import com.yanzhenjie.permission.PermissionListener
import kotlinx.android.synthetic.main.activity_main_list.*

/**
 *  检查外部存储权限，如果没有授予权限则退出
 */
class MainListActivity : AppCompatActivity() {

    companion object {
        private val REQ_PERMISSION = 10
        fun start(context: Context) {
            context.startActivity(Intent(context, MainListActivity::class.java))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_list)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setTitle(R.string.finger_coloring)

        rgTabs.check(R.id.rbTabWorks)
        checkExternalPermissions()
    }

    private fun checkExternalPermissions() {
        AndPermission.with(this)
                .requestCode(REQ_PERMISSION)
                .permission(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .rationale { _, rationale -> AndPermission.rationaleDialog(this, rationale).show() }
                .callback(object : PermissionListener {
                    override fun onSucceed(requestCode: Int, grantPermissions: MutableList<String>) {
                        if (requestCode == REQ_PERMISSION) {
                            initViews()
                        }
                    }

                    override fun onFailed(requestCode: Int, deniedPermissions: MutableList<String>) {
                        if (requestCode == REQ_PERMISSION) {
                            Toast.makeText(this@MainListActivity, R.string.no_external_permission, Toast.LENGTH_SHORT).show()
                            finish()
                        }
                    }
                })
                .start()
    }

    private fun initViews() {
        val titles = arrayOf(
                getString(R.string.themelist),
                getString(R.string.my_works)
        )
        val fragments = arrayOf(
                ThemesFragment.newInstance(),
                WorksFragment.newInstance()
        )

        viewpager.adapter = object : FragmentStatePagerAdapter(supportFragmentManager) {
            override fun getItem(position: Int): Fragment {
                return fragments[position]
            }

            override fun getPageTitle(position: Int): CharSequence {
                return titles[position]
            }

            override fun getCount(): Int {
                return titles.size
            }
        }

        rgTabs.setOnCheckedChangeListener { _, checkedId ->
            viewpager.currentItem = if (checkedId == R.id.rbTabWorks) 1 else 0
        }
        rgTabs.check(R.id.rbTabThemes)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
