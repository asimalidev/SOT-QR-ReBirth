package com.qrcodescanner.barcodereader.qrgenerator.activities

import android.app.AlarmManager
import android.app.KeyguardManager
import android.app.PendingIntent
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.qrcodescanner.barcodereader.qrgenerator.R
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.qrcodescanner.barcodereader.qrgenerator.notification.AlarmReceiver
import java.text.SimpleDateFormat
import java.util.*


class KeyguardDismissActivity : AppCompatActivity() {
    private lateinit var timeTextView: TextView
    private lateinit var dateTextView: TextView
    private var windowManager: WindowManager? = null
    private var layoutCounter = 1
    private lateinit var handler:Handler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        showOnLockscreen()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON
            )
        }

        setContentView(R.layout.notification_custom)
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        timeTextView = findViewById(R.id.tvTime)
        dateTextView = findViewById(R.id.tvDate)

        // Start updating the time and date
        updateDateTime()

        // Load the layout counter from SharedPreferences
        val sharedPref = getSharedPreferences("NotificationTimePrefs", Context.MODE_PRIVATE)
        layoutCounter = sharedPref.getInt("layoutCounter", 1)

        // Immediately set the default layout
        changeLayout(layoutCounter) // Use the loaded layout counter
    }

        private fun showOnLockscreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
        } else {
            window.addFlags(WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON)
        }
    }

    private fun updateDateTime() {
        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault()) // 12-hour format
        val dateFormat =
            SimpleDateFormat("EEE, MMM d, yyyy", Locale.getDefault()) // e.g., Fri, Jan 17, 2025

        handler = Handler(Looper.getMainLooper())
        handler.post(object : Runnable {
            override fun run() {
                val currentTime = Calendar.getInstance().time
                timeTextView.text = timeFormat.format(currentTime)
                dateTextView.text = dateFormat.format(currentTime)

                // Update every second
                handler.postDelayed(this, 1000)
            }
        })
    }

    private fun changeLayout(layoutId: Int) {
        val layoutToLoad: Int = when (layoutId) {
            1 -> R.layout.layout_notification_1
            2 -> R.layout.layout_notification_3
            3 -> R.layout.layout_notification_4
            4 -> R.layout.layout_notification_5
            5 -> R.layout.layout_notification_6
            else -> R.layout.layout_notification_1 // Default layout if invalid layoutId
        }

        // Find the container (linearLayout5) where we want to inflate the new layout
        val layoutContainer: ViewGroup = findViewById(R.id.linearLayout5)

        // Remove all existing views to ensure the layout is refreshed
        layoutContainer.removeAllViews()

        // Inflate the selected layout into the container
        layoutInflater.inflate(layoutToLoad, layoutContainer, true)

        // Set up button click listener based on layout
        val btnCheck: LinearLayout = layoutContainer.findViewById(R.id.llmain)
        btnCheck.setOnClickListener {
            val actionIntent = when (layoutId) {
                1 -> Intent(this, FirstOpenActivity::class.java).apply { putExtra("action", "create_qr") }
                2 -> Intent(this, FirstOpenActivity::class.java).apply { putExtra("action", "scan_qr") }
                3 -> Intent(this, FirstOpenActivity::class.java).apply { putExtra("action", "translate_image") }
                4 -> Intent(this, FirstOpenActivity::class.java).apply { putExtra("action", "doc_scan") }
                5 -> Intent(this, FirstOpenActivity::class.java).apply { putExtra("action", "barcode_scan") }
                else -> Intent(this, FirstOpenActivity::class.java).apply { putExtra("action", "create_qr") }
            }
            startActivity(actionIntent)
        }
    }

    override fun onResume() {
        super.onResume()
        // Reload the layout counter from SharedPreferences whenever the activity is resumed
        val sharedPref = getSharedPreferences("NotificationTimePrefs", Context.MODE_PRIVATE)
        layoutCounter = sharedPref.getInt("layoutCounter", 1)
        changeLayout(layoutCounter)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null) // Stop updates when the activity is destroyed
    }
}










