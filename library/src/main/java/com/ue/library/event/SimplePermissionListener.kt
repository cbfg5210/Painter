package com.ue.library.event

import com.yanzhenjie.permission.PermissionListener

/**
 * Created by hawk on 2018/1/26.
 */
abstract class SimplePermissionListener : PermissionListener {
    override fun onSucceed(requestCode: Int, grantPermissions: MutableList<String>) {
    }

    override fun onFailed(requestCode: Int, deniedPermissions: MutableList<String>) {
    }
}