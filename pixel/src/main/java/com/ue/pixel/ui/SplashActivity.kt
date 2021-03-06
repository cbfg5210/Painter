package com.ue.pixel.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.view.animation.AccelerateDecelerateInterpolator
import com.ue.library.util.toast
import kotlinx.android.synthetic.main.pi_activity_splash.*

class SplashActivity : AppCompatActivity() {

    private lateinit var handler: Handler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.ue.pixel.R.layout.pi_activity_splash)

        ivAppImage.animate()
                .alpha(1f)
                .scaleY(1.1f)
                .scaleX(1.1f)
                .setDuration(2000L)
                .interpolator = AccelerateDecelerateInterpolator()
        tvAppName.animate()
                .alpha(1f)
                .scaleY(1.1f)
                .scaleX(1.1f)
                .setDuration(2000L)
                .interpolator = AccelerateDecelerateInterpolator()

        handler = Handler()
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
            handler.postDelayed({
                startActivity(Intent(this@SplashActivity, DrawingActivity::class.java))
                finish()
            }, 2000L)
        else
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE), 0x456)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == 0x456) {
            for (i in grantResults.indices) {
                if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    toast("Sorry this application require storage permission for saving your project")
                    handler.postDelayed({ recreate() }, 1000)
                    return
                }
            }
            handler.postDelayed({
                startActivity(Intent(this@SplashActivity, DrawingActivity::class.java))
                finish()
            }, 2000L)
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}