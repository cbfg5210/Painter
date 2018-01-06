package com.ue.pixelpaint

import android.os.Bundle
import android.support.v7.app.AppCompatActivity

class PixelPaintActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pixel_paint)

//        val listener = MultiTouchListener()
//        listener.isRotateEnabled = false
//        listener.minimumScale = 1F
//        listener.maximumScale = 4F
//
//        view.setOnTouchListener(listener)
    }
}
