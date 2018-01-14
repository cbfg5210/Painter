package com.ue.library.event

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter


/**
 * Created by hawk on 2018/1/14.
 */
class HomeWatcher(private val mContext: Context) {
    private var mRecevier: InnerRecevier? = null

    var homeListener: OnHomePressedListener? = null
        set(value) {
            field = value
            mRecevier = InnerRecevier()
        }

    fun startWatch() {
        if (mRecevier != null) {
            mContext.registerReceiver(mRecevier, IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS))
        }
    }

    fun stopWatch() {
        if (mRecevier != null) {
            mContext.unregisterReceiver(mRecevier)
        }
    }

    internal inner class InnerRecevier : BroadcastReceiver() {
        val SYSTEM_DIALOG_REASON_KEY = "reason"
        val SYSTEM_DIALOG_REASON_RECENT_APPS = "recentapps"
        val SYSTEM_DIALOG_REASON_HOME_KEY = "homekey"

        override fun onReceive(context: Context, intent: Intent) {
            if (!intent.action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
                return
            }

            val reason = intent.getStringExtra(SYSTEM_DIALOG_REASON_KEY)
            reason ?: return

            if (reason == SYSTEM_DIALOG_REASON_HOME_KEY) {
                homeListener?.onHomePressed()
            }
//            else if (reason == SYSTEM_DIALOG_REASON_RECENT_APPS) {
//                homeListener?.onHomeLongPressed()
//            }
        }
    }

    interface OnHomePressedListener {
        fun onHomePressed()
//        fun onHomeLongPressed()
    }
}