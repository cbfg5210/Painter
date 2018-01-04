package com.ue.painter

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), View.OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnAutoDraw.setOnClickListener(this)
        btnFingerColoring.setOnClickListener(this)
        btnGraffiti.setOnClickListener(this)
        btnPixelPaint.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.btnAutoDraw -> ModuleRouter.startAutoDraw(this)
            R.id.btnFingerColoring -> ModuleRouter.startFingerColoring(this)
            R.id.btnGraffiti -> ModuleRouter.startGraffiti(this)
            R.id.btnPixelPaint -> ModuleRouter.startPixelPaint(this)
        }
    }
}
