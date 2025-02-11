package com.qrcodescanner.barcodereader.qrgenerator.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log


class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val sharedPref = context.getSharedPreferences("NotificationTimePrefs", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()


        // Get the current layout counter, defaulting to 1 if not set
        var currentCounter = sharedPref.getInt("layoutCounter", 1)
        Log.e("currentlayout", "Value of layout is $currentCounter")

// Increment the counter (reset to 1 after 5) relo
        currentCounter = if (currentCounter == 5) {
            Log.e("currentlayout", "Resetting layout counter to 1")
            1 // Reset to 1 after 5
        } else {
            currentCounter + 1 // Otherwise increment by 1
        }



        // Save the updated counter to SharedPreferences
        editor.putInt("layoutCounter", currentCounter)
        editor.apply()

        // Trigger the lockscreen widget notification (using your existing manager)
        AppNotificationManager.sendLockscreenWidgetNotification(context)

        // Optionally: Schedule the next alarm for 24 hours from now
        scheduleNextAlarm(context)

        // Optional: Log or send some feedback about the new layout and counter
        Log.d("AlarmReceiver", "Scheduled layout $currentCounter after 24 hours")
    }

    private fun scheduleNextAlarm(context: Context) {
        // Get the saved time from SharedPreferences (e.g., from the time the user selected)
        val sharedPref = context.getSharedPreferences("NotificationTimePrefs", Context.MODE_PRIVATE)
        val hour = sharedPref.getInt("hour", 0)
        val minute = sharedPref.getInt("minute", 0)

        // Schedule the next alarm 24 hours from now
        AppAlarmManager.scheduleLockscreenWidget(
            context,
            AppAlarmManager.lockscreenWidgetRequestCode,
            hour,
            minute
        )
    }
}
