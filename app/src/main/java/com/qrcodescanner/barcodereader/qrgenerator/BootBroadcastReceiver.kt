package com.qrcodescanner.barcodereader.qrgenerator

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.qrcodescanner.barcodereader.qrgenerator.notification.NotificationWorker
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class BootBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || intent.action == Intent.ACTION_MY_PACKAGE_REPLACED) {
            val sharedPrefs = context.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
            val lastOpenedTime = sharedPrefs.getLong("last_opened_time", 0)
            if (System.currentTimeMillis() - lastOpenedTime > TimeUnit.HOURS.toMillis(8)) {
                scheduleSequentialNotifications(context)
            }
        }
    }

    private fun scheduleSequentialNotifications(context: Context) {
        Log.e("BootBroadcastReceiver", "Notification scheduling process started")
        val sharedPreferences = context.getSharedPreferences("NotificationPrefs", Context.MODE_PRIVATE)
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val notificationsSentToday = sharedPreferences.getInt(today, 0)
        val notificationData = listOf(
            Pair(
                context.getString(R.string.create_your_perfect_qr_code_today),
                context.getString(R.string.transform_any_url_text_or_data_into_a_unique_qr_code)
            ),
            Pair(
                context.getString(R.string.scan_save_and_go),
                context.getString(R.string.scan_barcodes_and_qr_codes_instantly)
            ),
            Pair(
                context.getString(R.string.unlock_the_world_with_live_translation),
                context.getString(R.string.point_your_camera_at_any_text_to_instantly_translate_it)
            ),
            Pair(
                context.getString(R.string.your_digital_scanner_is_ready),
                context.getString(R.string.snap_scan_and_save_your_documents)
            )
        )

        var previousWorkRequest: OneTimeWorkRequest? = null
        var notificationsScheduled = 0

        notificationData.forEachIndexed { index, data ->
            val workRequest = OneTimeWorkRequest.Builder(NotificationWorker::class.java)
                .setInputData(workDataOf("title" to data.first, "content" to data.second))
                .setInitialDelay(8, TimeUnit.HOURS)
                .build()

            if (previousWorkRequest != null) {
                WorkManager.getInstance(context).beginWith(previousWorkRequest!!).then(workRequest)
                    .enqueue()
            } else {
                WorkManager.getInstance(context).enqueue(workRequest)
            }

            previousWorkRequest = workRequest
            notificationsScheduled++

            Log.e("BootBroadcastReceiver","Notification scheduled: ${data.first}, Index: ${index + 1}, Trigger delay: ${(index + 1) * 2} minutes")
        }

        sharedPreferences.edit()
            .putInt(today, notificationsSentToday + notificationsScheduled)
            .apply()

        Log.e("BootBroadcastReceiver", "Sequential notifications scheduled successfully.")
    }
}

