package com.ue.pixel.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Environment
import android.text.InputType
import android.util.DisplayMetrics
import android.util.Log
import com.afollestad.materialdialogs.GravityEnum
import com.afollestad.materialdialogs.MaterialDialog
import com.ue.library.constant.Constants
import com.ue.pixel.R
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStreamWriter

/**
 * Created by BennyKok on 10/6/2016.
 */
object Tool {
    val myType = Typeface.create("sans-serif-light", Typeface.NORMAL)

    fun drawableToBitmap(drawable: Drawable): Bitmap {
        if (drawable is BitmapDrawable && drawable.bitmap != null) {
            return drawable.bitmap
        }

        val bitmap: Bitmap
        if (drawable.intrinsicWidth <= 0 || drawable.intrinsicHeight <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888) // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        }

        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    fun saveProject(name: String, data: String) {
        val dirs = File(Environment.getExternalStorageDirectory().path + Constants.PATH_PIXEL)
        if (!dirs.exists()) {
            dirs.mkdirs()
        }
        var outputStreamWriter: OutputStreamWriter? = null
        try {
            outputStreamWriter = OutputStreamWriter(FileOutputStream(File(dirs, name)))
            outputStreamWriter.write(data)
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            outputStreamWriter?.apply {
                try {
                    close()
                } catch (exp: IOException) {
                    Log.e("Tool", "saveProject: error:msg=${exp.message}")
                }
            }
        }
    }

    fun stripExtension(str: String): String {
        val pos = str.lastIndexOf(".")
        return if (pos == -1) str else str.substring(0, pos)
    }

    fun convertDpToPixel(dp: Float, context: Context): Float {
        val metrics = context.resources.displayMetrics
        return dp * (metrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
    }

    fun freeMemory() {
        System.runFinalization()
        Runtime.getRuntime().gc()
        System.gc()
    }

    fun trimLongString(str: String): String {
        return if (str.length <= 25) str
        else "...${str.substring(str.length - 21, str.length)}"
    }

    fun prompt(c: Context): MaterialDialog.Builder {
        return MaterialDialog.Builder(c)
                .negativeText(R.string.cancel)
                .titleGravity(GravityEnum.CENTER)
                .typeface(Tool.myType, Tool.myType)
                .positiveColor(Color.RED)
    }

    fun promptTextInput(c: Context, title: String): MaterialDialog.Builder {
        return MaterialDialog.Builder(c)
                .negativeText(R.string.cancel)
                .positiveText(R.string.sure)
                .title(title)
                .inputType(InputType.TYPE_CLASS_TEXT)
                .inputRange(0, 20)
                .titleGravity(GravityEnum.CENTER)
                .typeface(Tool.myType, Tool.myType)
                .positiveColor(Color.GREEN)
    }
}