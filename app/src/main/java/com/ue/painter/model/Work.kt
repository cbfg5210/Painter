package com.ue.painter.model

import android.content.Context
import android.os.Environment
import com.ue.library.constant.Constants
import com.ue.library.event.SimplePermissionListener
import com.ue.library.util.PermissionUtils
import com.ue.painter.R
import com.ue.painter.util.bindUtilDestroy
import io.reactivex.Observable
import io.reactivex.ObservableOnSubscribe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.io.File
import java.util.*

/**
 * Created by hawk on 2018/1/26.
 */
class Work {
    companion object {
        fun get(): Work {
            return Work()
        }
    }

    fun getRecentWorks(atyOrFrag: Any, listener: OnFetchWorksListener) {
        val context = if (atyOrFrag is android.support.v4.app.Fragment) atyOrFrag.context else atyOrFrag as Context
        PermissionUtils.checkReadWriteStoragePerms(context, context.getString(R.string.load_photo_err_no_perm), object : SimplePermissionListener() {
            override fun onSucceed(requestCode: Int, grantPermissions: MutableList<String>) {
                Observable
                        .create(ObservableOnSubscribe<ArrayList<PictureWorkItem>> { e ->
                            val works = ArrayList<PictureWorkItem>()
                            val externalPath = Environment.getExternalStorageDirectory().path

                            getLatestFile("$externalPath${Constants.PATH_OUTLINE}")?.apply {
                                works.add(PictureWorkItem("${context.getString(R.string.module_outline)}-$name", "file://$absolutePath"))
                            }
                            getLatestFile("$externalPath${Constants.PATH_COLORING}")?.apply {
                                works.add(PictureWorkItem("${context.getString(R.string.module_coloring)}-$name", "file://$absolutePath"))
                            }
                            getLatestFile("$externalPath${Constants.PATH_GRAFFITI}")?.apply {
                                works.add(PictureWorkItem("${context.getString(R.string.module_graffiti)}-$name", "file://$absolutePath"))
                            }
                            getLatestFile("$externalPath${Constants.PATH_PIXEL}")?.apply {
                                works.add(PictureWorkItem("${context.getString(R.string.module_pixel)}-$name", "file://$absolutePath"))
                            }

                            e.onNext(works)
                            e.onComplete()
                        })
                        .subscribeOn(Schedulers.single())
                        .observeOn(AndroidSchedulers.mainThread())
                        .bindUtilDestroy(context)
                        .subscribe { listener.onWorksFetched(it) }
            }
        })
    }

    private fun getLatestFile(dirPath: String): File? {
        val dir = File(dirPath)
        if (!dir.exists() || !dir.isDirectory) {
            return null
        }
        var files = dir.listFiles()
        if (files.isEmpty()) {
            return null
        }
        Arrays.sort(files) { o1, o2 -> if (o1.lastModified() == o2.lastModified()) 0 else if (o1.lastModified() < o2.lastModified()) 1 else -1 }
        files.forEach { if (it.name.endsWith(".png")) return it }
        return null
    }

    interface OnFetchWorksListener {
        fun onWorksFetched(works: ArrayList<PictureWorkItem>)
    }
}