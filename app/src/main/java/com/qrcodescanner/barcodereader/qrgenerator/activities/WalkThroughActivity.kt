package com.qrcodescanner.barcodereader.qrgenerator.activities

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import androidx.viewpager2.widget.ViewPager2
import com.qrcodescanner.barcodereader.qrgenerator.R
import com.qrcodescanner.barcodereader.qrgenerator.adapters.WalkThroughAdapter
import com.qrcodescanner.barcodereader.qrgenerator.ads.CustomFirebaseEvents
import com.qrcodescanner.barcodereader.qrgenerator.ads.NetworkCheck
import com.qrcodescanner.barcodereader.qrgenerator.utils.PrefHelper
import com.qrcodescanner.barcodereader.qrgenerator.utils.native_onb_2_f

class WalkThroughActivity : AppCompatActivity() {
    private lateinit var viewPager: ViewPager2
    var prefHelper: PrefHelper? = null
    private var previousPosition: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_walk_through)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
            val controller = window.insetsController
            if (controller != null) {
                // Hide only the navigation bar
                controller.hide(WindowInsets.Type.navigationBars())
                controller.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            // Fallback for devices with Android 9 and below
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        }

        prefHelper = PrefHelper(this)
        val noOfFragment = if (NetworkCheck.isNetworkAvailable(this) &&
            (getSharedPreferences("RemoteConfig", MODE_PRIVATE).getBoolean(native_onb_2_f, true) ||
                    getSharedPreferences("RemoteConfig", MODE_PRIVATE).getBoolean(native_onb_2_f, true))) {
                4
            } else {
                3
            }

        viewPager = findViewById(R.id.viewPager)
        if (prefHelper != null) {
            viewPager.adapter = WalkThroughAdapter(fragmentActivity = this, prefHelper!!, noOfFragment)
        } else {
            prefHelper = PrefHelper(this)
            viewPager.adapter = WalkThroughAdapter(fragmentActivity = this, prefHelper!!, noOfFragment)
        }
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)

                if (previousPosition != -1) {
                    when (previousPosition) {
                        0 -> if (position == 1) {
                            CustomFirebaseEvents.logEvent(context = this@WalkThroughActivity, screenName = "", trigger = "", eventName = "onboard1_scr_swipe_next")
                        }
                        1 -> if (position == 2) {
                            CustomFirebaseEvents.logEvent(context = this@WalkThroughActivity, screenName = "", trigger = "", eventName = "onboard2_scr_swipe_next")
                        } else if (position == 0) {
                            CustomFirebaseEvents.logEvent(context = this@WalkThroughActivity, screenName = "", trigger = "", eventName = "onboard2_scr_swipe_back")
                        }
                        2 -> if (position == 1) {
                            CustomFirebaseEvents.logEvent(context = this@WalkThroughActivity, screenName = "", trigger = "", eventName = "onboard3_scr_swipe_back")
                        }
                    }
                }
                previousPosition = position
            }
        })
    }
}