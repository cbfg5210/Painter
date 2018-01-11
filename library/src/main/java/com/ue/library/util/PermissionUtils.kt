package com.ue.library.util

import android.content.Context
import com.yanzhenjie.permission.AndPermission
import com.yanzhenjie.permission.PermissionListener

/**
 * Created by hawk on 2018/1/8.
 */
object PermissionUtils {
    fun checkPermission(context: Context, reqCode: Int, perms: Array<String>, callback: PermissionListener?) {
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