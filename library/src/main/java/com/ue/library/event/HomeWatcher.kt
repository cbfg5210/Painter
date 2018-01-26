package com.ue.library.event

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter

/**
 * Created by hawk on 2018/1/14.
 */
class HomeWatcher(private val mContext: Context) {
    private var mReceiver: InnerReceiver? = null

    var homeListener: OnHomePressedListener? = null
        set(value) {
            field = value
            mReceiver = InnerReceiver()
        }

    fun startWatch() {
        if (mReceiver != null) {
            mContext.registerReceiver(mReceiver, IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS))
        }
    }

    fun stopWatch() {
        if (mReceiver != null) {
            mContext.unregisterReceiver(mReceiver)
        }
    }

    internal inner class InnerReceiver : BroadcastReceiver() {
        val SYSTEM_DIALOG_REASON_KEY = "reason"
        //val SYSTEM_DIALOG_REASON_RECENT_APPS = "recentapps"
        val SYSTEM_DIALOG_REASON_HOME_KEY = "homekey"

        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action != Intent.ACTION_CLOSE_SYSTEM_DIALOGS) {
                return
            }
            val reason = intent.getStringExtra(SYSTEM_DIALOG_REASON_KEY) ?: return
            if (reason == SYSTEM_DIALOG_REASON_HOME_KEY) {
                homeListener?.onHomePressed()
            }
        }
    }

    interface OnHomePressedListener {
        fun onHomePressed()
    }
}