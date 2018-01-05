package com.ue.pixelpaint

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.ue.pixelpaint.gesture.MultiTouchListener
import kotlinx.android.synthetic.main.activity_pixel_paint.*

class PixelPaintActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pixel_paint)

        val listener = MultiTouchListener()
        listener.isRotateEnabled = false

        view.setOnTouchListener(listener)
    }
}
