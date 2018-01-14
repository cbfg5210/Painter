package com.ue.library.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.ue.library.R
import java.io.File

/**
 * Created by hawk on 2018/1/13.
 */
object IntentUtils {
    fun shareImage(context: Context, subject: String?, text: String?, path: String) {
        val file = File(path)
        val uri = Uri.fromFile(file)
        val intent = Intent()
        intent.action = Intent.ACTION_SEND
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_SUBJECT, subject)
        intent.putExtra(Intent.EXTRA_TEXT, text)
        intent.putExtra(Intent.EXTRA_STREAM, uri)
        context.startActivity(Intent.createChooser(intent, context.getString(R.string.share_to)))
    }

    fun playVideo(context: Context, videoPath: String) {
        val videoIntent = Intent(Intent.ACTION_VIEW)
        videoIntent.setDataAndType(Uri.parse("file:///$videoPath"), "video/*");
        videoIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(videoIntent)
    }

    fun playVideo(context: Context, videoUril: Uri) {
        val videoIntent = Intent(Intent.ACTION_VIEW)
        videoIntent.setDataAndType(videoUril, "video/*");
        videoIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(videoIntent)
    }

    fun shareVideo(context: Context, videoPath: String) {
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.putExtra(Intent.EXTRA_STREAM, videoPath)
        shareIntent.type = "video/*";
        context.startActivity(Intent.createChooser(shareIntent, context.getString(R.string.share_to)));
    }
}