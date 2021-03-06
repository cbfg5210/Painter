package com.ue.library.util

import android.Manifest
import android.content.Context
import com.ue.library.event.SimplePermissionListener
import com.yanzhenjie.permission.AndPermission
import com.yanzhenjie.permission.PermissionListener

/**
 * Created by hawk on 2018/1/8.
 */
object PermissionUtils {
    const val REQ_PERM_READ_WRITE_STORAGE = 11

    fun checkReadWriteStoragePerms(context: Context, failureTip: String, successCallback: SimplePermissionListener) {
        checkPermissions(context,
                REQ_PERM_READ_WRITE_STORAGE,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE),
                failureTip,
                successCallback)
    }

    fun checkPermissions(context: Context, reqCode: Int, perms: Array<String>, failureTip: String, callback: SimplePermissionListener) {
        val callback = object : PermissionListener {
            override fun onSucceed(requestCode: Int, grantPermissions: MutableList<String>) {
                if (reqCode == requestCode) {
                    callback.onSucceed(requestCode, grantPermissions)
                }
            }

            override fun onFailed(requestCode: Int, deniedPermissions: MutableList<String>) {
                context.toast(failureTip)
                callback.onFailed(requestCode, deniedPermissions)
            }
        }
        checkPermissions(context, reqCode, perms, callback)
    }

    private fun checkPermissions(context: Context, reqCode: Int, perms: Array<String>, callback: PermissionListener?) {
        val permList = perms.toList()
        if (AndPermission.hasPermission(context, permList)) {
            callback?.onSucceed(reqCode, permList)
            return
        }
        AndPermission.with(context)
                .requestCode(reqCode)
                .permission(perms)
                .rationale { _, rationale ->
                    AndPermission.rationaleDialog(context, rationale).show()
                }
                .callback(object : PermissionListener {
                    override fun onSucceed(requestCode: Int, grantPermissions: MutableList<String>) {
                        if (requestCode == reqCode) {
                            callback?.onSucceed(requestCode, grantPermissions)
                        }
                    }

                    override fun onFailed(requestCode: Int, deniedPermissions: MutableList<String>) {
                        if (requestCode == reqCode) {
                            callback?.onFailed(requestCode, deniedPermissions)
                        }
                    }
                })
                .start()
    }
}