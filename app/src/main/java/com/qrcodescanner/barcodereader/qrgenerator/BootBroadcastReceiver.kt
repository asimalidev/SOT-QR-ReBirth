package com.qrcodescanner.barcodereader.qrgenerator

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.qrcodescanner.barcodereader.qrgenerator.activities.HomeActivity
import java.util.concurrent.TimeUnit

class BootBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || intent.action == Intent.ACTION_MY_PACKAGE_REPLACED) {
            // You do not need to launch HomeActivity here, just schedule the notifications
            val sharedPrefs = context.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
            val lastOpenedTime = sharedPrefs.getLong("last_opened_time", 0)

            // Check if it's been more than 8 hours since the last launch
            if (System.currentTimeMillis() - lastOpenedTime > TimeUnit.HOURS.toMillis(8)) {
                // Schedule notifications
                val activity = context as HomeActivity
                activity.scheduleSequentialNotifications()
            }
        }
    }
}

