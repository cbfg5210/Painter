package com.ue.library.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import java.io.File

/**
 * Created by hawk on 2018/1/13.
 */
object IntentUtils {
    fun shareImage(context: Context, subject: String?, text: String?, path: String, chooserTxt: String) {
        val file = File(path)
        val uri = Uri.fromFile(file)
        val intent = Intent()
        intent.action = Intent.ACTION_SEND
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_SUBJECT, subject)
        intent.putExtra(Intent.EXTRA_TEXT, text)
        intent.putExtra(Intent.EXTRA_STREAM, uri)
        context.startActivity(Intent.createChooser(intent, chooserTxt))
    }
}