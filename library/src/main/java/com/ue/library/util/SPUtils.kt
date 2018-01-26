package com.ue.library.util

import android.app.Application
import android.content.SharedPreferences
import android.preference.PreferenceManager

class SPUtils private constructor() {

    init {
        throw UnsupportedOperationException()
    }

    companion object {

        private lateinit var sharedPreferences: SharedPreferences

        fun init(application: Application) {
            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(application)
        }

        fun putString(key: String, value: String) = sharedPreferences.edit().putString(key, value).apply()

        fun putInt(key: String, value: Int) = sharedPreferences.edit().putInt(key, value).apply()

        fun putBoolean(key: String, value: Boolean) = sharedPreferences.edit().putBoolean(key, value).apply()

        fun putLong(key: String, value: Long) = sharedPreferences.edit().putLong(key, value).apply()

        fun getString(key: String, defaultValue: String = ""): String = sharedPreferences.getString(key, defaultValue)

        fun getInt(key: String, defaultValue: Int = 0) = sharedPreferences.getInt(key, defaultValue)

        fun getBoolean(key: String, defaultValue: Boolean = true) = sharedPreferences.getBoolean(key, defaultValue)

        fun getLong(key: String, defaultValue: Long = 0) = sharedPreferences.getLong(key, defaultValue)

        fun remove(key: String) = sharedPreferences.edit().remove(key).apply()

        fun clear() = sharedPreferences.edit().clear().apply()
    }
}