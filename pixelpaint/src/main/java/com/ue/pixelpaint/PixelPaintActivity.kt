package com.ue.pixelpaint

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_pixel_paint.*

class PixelPaintActivity : AppCompatActivity() {
    private var isTranslateEnabled = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pixel_paint)

        pixelView.setTranslateEnabled(isTranslateEnabled)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_pixel_paint, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.actionPixelTranslate) {
            isTranslateEnabled = !isTranslateEnabled
            pixelView.setTranslateEnabled(isTranslateEnabled)
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
