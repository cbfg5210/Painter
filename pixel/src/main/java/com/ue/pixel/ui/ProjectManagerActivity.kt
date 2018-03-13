package com.ue.pixel.ui

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.ue.pixel.R
import com.ue.pixel.model.ProjectItem
import com.ue.pixel.util.ExportingUtils
import kotlinx.android.synthetic.main.pi_activity_project_manager.*
import java.io.File
import java.io.FileFilter
import java.util.*

class ProjectManagerActivity : AppCompatActivity() {
    private var projectFiles = ArrayList<File>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.pi_activity_project_manager)

        setSupportActionBar(tbToolbar)
        rvProjectList.adapter = ProjectAdapter(this, getProjects())
        projectFiles.clear()
    }

    private fun getProjects(): List<ProjectItem>? {
        val parent = File(ExportingUtils.projectPath)

        if (!parent.exists()) return null
        val temp = parent.listFiles(FileFilter { it.name.endsWith(".pxer") }) ?: return null

        val projects = ArrayList<ProjectItem>()
        temp.forEach {
            projectFiles.add(it)
            projects.add(ProjectItem(it.name.substring(0, it.name.lastIndexOf('.')), it.path))
        }
        if (projects.size > 0) tvNoProjectFound.visibility = View.GONE

        return projects

//            fa.withOnLongClickListener { v, _, _, position ->
//                val pm = PopupMenu(v.context, v)
//                pm.inflate(R.menu.menu_popup_project)
//                pm.setOnMenuItemClickListener { item ->
//                    when (item.itemId) {
//                        R.id.rename -> Tool.promptTextInput(this@ProjectManagerActivity, getString(R.string.rename)).input(null, projectFiles[position].name, false) { _, input ->
//                            var mInput = input.toString()
//                            if (!mInput.endsWith(".pxer")) mInput += ".pxer"
//
//                            val fromFile = File(projectFiles[position].path)
//                            val newFile = File(projectFiles[position].parent, mInput)
//
//                            if (fromFile.renameTo(newFile)) {
//                                projectFiles[position] = newFile
//                                ia.set(position, Item(newFile.name, newFile.path))
//                                fa.notifyAdapterItemChanged(position)
//
//                                val newIntent = Intent()
//                                newIntent.putExtra("fileNameChanged", true)
//
//                                setResult(Activity.RESULT_OK, newIntent)
//                            }
//                        }.show()
//                        R.id.delete -> Tool.prompt(this@ProjectManagerActivity).title(R.string.deleteproject).content(R.string.deleteprojectwarning).positiveText(R.string.delete).onPositive { _, _ ->
//                            if (projectFiles[position].delete()) {
//                                ia.remove(position)
//                                projectFiles.removeAt(position)
//
//                                if (projectFiles.size < 1) noProjectFound.visibility = View.VISIBLE
//
//                                val newIntent = Intent()
//                                newIntent.putExtra("fileNameChanged", true)
//
//                                setResult(Activity.RESULT_OK, newIntent)
//
//                                toast(getString(R.string.projectdeleted))
//                            } else
//                                toast(getString(R.string.unabletodeleteproject))
//                        }.show()
//                    }
//                    true
//                }
//                pm.show()
//                true
//            }
    }
}