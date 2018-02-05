package com.ue.pixel.ui

import android.app.Activity
import android.content.Intent
import android.view.View
import com.ue.adapterdelegate.AdapterDelegate
import com.ue.adapterdelegate.BaseViewHolder
import com.ue.adapterdelegate.DelegationAdapter
import com.ue.adapterdelegate.OnDelegateClickListener
import com.ue.pixel.R
import com.ue.pixel.model.ProjectItem
import kotlinx.android.synthetic.main.pi_item_project.view.*

/**
 * Created by hawk on 2018/1/30.
 */
class ProjectAdapter(private val activity: Activity, files: List<ProjectItem>?) : DelegationAdapter<ProjectItem>(), OnDelegateClickListener {
    init {
        if (files != null) items.addAll(files)
        addDelegate(ProjectDelegate(activity).apply { delegateClickListener = this@ProjectAdapter })
    }

    override fun onClick(view: View, position: Int) {
        if (position < 0 || position >= itemCount) return

        val item = items[position]
        val newIntent = Intent().putExtra(DrawingActivity.SELECTED_PROJECT_PATH, item.path)
        activity.setResult(Activity.RESULT_OK, newIntent)
        activity.finish()
    }

    class ProjectDelegate(activity: Activity) : AdapterDelegate<ProjectItem>(activity, R.layout.pi_item_project) {
        override fun onCreateViewHolder(itemView: View): BaseViewHolder<ProjectItem> {
            return object : BaseViewHolder<ProjectItem>(itemView) {
                private val tvProjectTitle = itemView.tvProjectTitle
                private val tvProjectPath = itemView.tvProjectPath

                override fun updateContents(item: ProjectItem) {
                    tvProjectTitle.text = item.name
                    tvProjectPath.text = item.path
                }
            }
        }
    }
}