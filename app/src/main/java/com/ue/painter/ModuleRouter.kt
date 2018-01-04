package com.ue.painter

import android.content.Context
import android.content.Intent
import com.ue.autodraw.AutoDrawActivity
import com.ue.fingercoloring.FingerColoringActivity
import com.ue.graffiti.GraffitiActivity
import com.ue.pixelpaint.PixelPaintActivity

/**
 * Created by hawk on 2018/1/4.
 */
class ModuleRouter {
    companion object {
        fun startAutoDraw(context: Context) {
            context.startActivity(Intent(context, AutoDrawActivity::class.java))
        }

        fun startFingerColoring(context: Context) {
            context.startActivity(Intent(context, FingerColoringActivity::class.java))
        }

        fun startGraffiti(context: Context) {
            context.startActivity(Intent(context, GraffitiActivity::class.java))
        }

        fun startPixelPaint(context: Context) {
            context.startActivity(Intent(context, PixelPaintActivity::class.java))
        }
    }
}