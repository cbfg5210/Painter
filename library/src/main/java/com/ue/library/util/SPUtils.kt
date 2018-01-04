package com.ue.library.util

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager

class SPUtils private constructor() {

    init {
        throw UnsupportedOperationException()
    }

    companion object {

        private var sharedPreferences: SharedPreferences? = null

        fun init(application: Application) {
            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(application)
        }

        fun putString(key: String, value: String) {
            val editor = sharedPreferences!!.edit()
            editor.putString(key, value)
            editor.apply()
        }

        fun putInt(key: String, value: Int) {
            val editor = sharedPreferences!!.edit()
            editor.putInt(key, value)
            editor.apply()
        }

        fun putBoolean(key: String, value: Boolean) {
            val editor = sharedPreferences!!.edit()
            editor.putBoolean(key, value)
            editor.apply()
        }

        fun putLong(key: String, value: Long) {
            val editor = sharedPreferences!!.edit()
            editor.putLong(key, value)
            editor.apply()
        }

        fun getString(key: String, defaultVaule: String): String? {
            return sharedPreferences!!.getString(key, defaultVaule)
        }

        fun getInt(key: String, defaultValue: Int): Int {
            return sharedPreferences!!.getInt(key, defaultValue)
        }

        fun getBoolean(key: String, defaultValue: Boolean): Boolean {
            return sharedPreferences!!.getBoolean(key, defaultValue)
        }

        fun getLong(key: String, defaultValue: Long): Long {
            return sharedPreferences!!.getLong(key, defaultValue)
        }

        fun remove(key: String) {
            val editor = sharedPreferences!!.edit()
            editor.remove(key)
            editor.apply()
        }

        fun clear(ctx: Context) {
            val editor = sharedPreferences!!.edit()
            editor.clear()
            editor.apply()
        }
    }
}