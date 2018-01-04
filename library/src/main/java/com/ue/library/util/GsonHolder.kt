package com.ue.library.util

/**
 * Created by hawk on 2017/4/9.
 */

import android.os.Build

import com.google.gson.Gson
import com.google.gson.GsonBuilder

import java.lang.reflect.Modifier

class GsonHolder private constructor() {

    companion object {
        val gson: Gson =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) Gson()
                else GsonBuilder().excludeFieldsWithModifiers(Modifier.FINAL, Modifier.TRANSIENT, Modifier.STATIC).create()

    }
}