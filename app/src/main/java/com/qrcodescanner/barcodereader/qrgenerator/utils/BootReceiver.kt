package com.qrcodescanner.barcodereader.qrgenerator.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.qrcodescanner.barcodereader.qrgenerator.notification.AppAlarmManager

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // Reschedule alarm after boot
//            AppAlarmManager.scheduleLockscreenWidgets(context)
        }
    }
}