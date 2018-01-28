package com.ue.painter.model

import android.content.Context
import android.graphics.BitmapFactory
import android.os.Environment
import com.ue.library.constant.Constants
import com.ue.library.constant.FileTypes
import com.ue.library.constant.Modules
import com.ue.library.event.SimplePermissionListener
import com.ue.library.util.PermissionUtils
import com.ue.painter.R
import com.ue.painter.util.bindUtilDestroy
import com.ue.painter.util.bindUtilDestroy3
import io.reactivex.Observable
import io.reactivex.ObservableOnSubscribe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.io.File
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

/**
 * Created by hawk on 2018/1/26.
 */
class WorkShop {
    companion object {
        fun get(): WorkShop {
            return WorkShop()
        }
    }

    fun getAllWorks(atyOrFrag: Any, listener: OnFetchWorksListener) {
        val context = if (atyOrFrag is android.support.v4.app.Fragment) atyOrFrag.context else atyOrFrag as Context
        PermissionUtils.checkReadWriteStoragePerms(context, context.getString(R.string.load_photo_err_no_perm), object : SimplePermissionListener() {
            override fun onSucceed(requestCode: Int, grantPermissions: MutableList<String>) {
                Observable
                        .create(ObservableOnSubscribe<MutableList<Work>> { e ->
                            val works: MutableList<Work> = ArrayList<Work>()
                            val externalPath = Environment.getExternalStorageDirectory().path

                            getOutlineWorks("$externalPath${Constants.PATH_OUTLINE}")?.apply { works.addAll(this) }
                            getPicWorks(Modules.COLORING, "$externalPath${Constants.PATH_COLORING}")?.apply { works.addAll(this) }
                            getPicWorks(Modules.GRAFFITI, "$externalPath${Constants.PATH_GRAFFITI}")?.apply { works.addAll(this) }
                            getPicWorks(Modules.PIXEL, "$externalPath${Constants.PATH_PIXEL}")?.apply { works.addAll(this) }

                            works.sortByDescending { it.lastModTime }

                            e.onNext(works)
                            e.onComplete()
                        })
                        .subscribeOn(Schedulers.single())
                        .observeOn(AndroidSchedulers.mainThread())
                        .bindUtilDestroy3(context)
                        .subscribe { listener.onWorksFetched(it) }
            }
        })
    }

    private fun getOutlineWorks(dirPath: String): MutableList<Work>? {
        val dir = File(dirPath)
        if (!dir.exists() || !dir.isDirectory) return null
        val files = dir.listFiles() ?: return null
        if (files.isEmpty()) return null

        val picMap = HashMap<String, File>()
        val mp4Map = HashMap<String, File>()
        var rawName: String
        files.forEach {
            rawName = FileTypes.getRawName(it.name)
            if (rawName.equals(FileTypes.PNG, true)) picMap[rawName] = it
            else if (rawName.equals(FileTypes.MP4, true)) mp4Map[rawName] = it
        }

        val list = ArrayList<Work>()

        var iterator = mp4Map.entries.iterator()
        var entry: MutableMap.MutableEntry<String, File>
        while (iterator.hasNext()) {
            entry = iterator.next()
            list.add(VideoWork(Modules.OUTLINE, entry.key, entry.value.absolutePath, entry.value.lastModified()).apply {
                val path = picMap[entry.key]?.absolutePath
                if (path != null) {
                    picPath = "file://$path"
                    wvHRadio = getPicWvHRatio(entry.value)
                }
            })
            picMap.remove(entry.key)
        }

        iterator = picMap.entries.iterator()
        while (iterator.hasNext()) {
            entry = iterator.next()
            list.add(PictureWork(Modules.OUTLINE, entry.key, "file://${entry.value.absolutePath}", entry.value.lastModified()).apply { wvHRadio = getPicWvHRatio(entry.value) })
        }

        return list
    }

    private fun getPicWorks(cat: Int, dirPath: String): Array<PictureWork>? {
        val dir = File(dirPath)
        if (!dir.exists() || !dir.isDirectory) return null
        val files = dir.listFiles() ?: return null
        if (files.isEmpty()) return null

        var file: File
        return Array<PictureWork>(files.size, { i ->
            file = files[i]
            PictureWork(cat, "${FileTypes.getRawName(file.name)}", "file://${file.absolutePath}", file.lastModified()).apply { wvHRadio = getPicWvHRatio(file) }
        })
    }

    fun getRecentWorks(atyOrFrag: Any, listener: OnFetchPicWorksListener) {
        val context = if (atyOrFrag is android.support.v4.app.Fragment) atyOrFrag.context else atyOrFrag as Context
        PermissionUtils.checkReadWriteStoragePerms(context, context.getString(R.string.load_photo_err_no_perm), object : SimplePermissionListener() {
            override fun onSucceed(requestCode: Int, grantPermissions: MutableList<String>) {
                Observable
                        .create(ObservableOnSubscribe<ArrayList<PictureWork>> { e ->
                            val works = ArrayList<PictureWork>()
                            val externalPath = Environment.getExternalStorageDirectory().path

                            val targetPaths = arrayOf(
                                    "$externalPath${Constants.PATH_OUTLINE}", "$externalPath${Constants.PATH_COLORING}",
                                    "$externalPath${Constants.PATH_GRAFFITI}", "$externalPath${Constants.PATH_PIXEL}")
                            val flags = intArrayOf(Modules.OUTLINE, Modules.COLORING, Modules.GRAFFITI, Modules.PIXEL)
                            val modules = arrayOf(
                                    context.getString(R.string.module_outline), context.getString(R.string.module_coloring),
                                    context.getString(R.string.module_graffiti), context.getString(R.string.module_pixel))

                            for (i in targetPaths.indices) {
                                getLatestFile(targetPaths[i])?.apply {
                                    works.add(PictureWork(flags[i], "${modules[i]}-${FileTypes.getRawName(name)}", "file://$absolutePath"))
                                }
                            }

                            e.onNext(works)
                            e.onComplete()
                        })
                        .subscribeOn(Schedulers.single())
                        .observeOn(AndroidSchedulers.mainThread())
                        .bindUtilDestroy(context)
                        .subscribe { listener.onPicWorksFetched(it) }
            }
        })
    }

    private fun getLatestFile(dirPath: String): File? {
        val dir = File(dirPath)
        if (!dir.exists() || !dir.isDirectory) {
            return null
        }
        var files = dir.listFiles() ?: return null
        if (files.isEmpty()) return null

        Arrays.sort(files) { o1, o2 -> if (o1.lastModified() == o2.lastModified()) 0 else if (o1.lastModified() < o2.lastModified()) 1 else -1 }
        files.forEach { if (it.name.endsWith(FileTypes.PNG, true)) return it }
        return null
    }

    private fun getPicWvHRatio(file: File): Float {
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeFile(file.path, options)
        val imageHeight = options.outHeight.toFloat()
        val imageWidth = options.outWidth.toFloat()
        return imageWidth / imageHeight
    }

    interface OnFetchWorksListener {
        fun onWorksFetched(works: List<Work>)
    }

    interface OnFetchPicWorksListener {
        fun onPicWorksFetched(works: ArrayList<PictureWork>)
    }
}