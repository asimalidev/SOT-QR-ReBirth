package com.qrcodescanner.barcodereader.qrgenerator.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.qrcodescanner.barcodereader.qrgenerator.R

//class NotificationReceiver : BroadcastReceiver() {
//    @RequiresApi(Build.VERSION_CODES.O)
//    override fun onReceive(context: Context, intent: Intent) {
//        // Check if permission is granted on Android 13 and above
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
//                // If permission is not granted, do not show the notification
//                return
//            }
//        }
//
//        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//
//        // Create notification channel if necessary
//        val notificationChannel = NotificationChannel("default", "Default", NotificationManager.IMPORTANCE_DEFAULT)
//        notificationManager.createNotificationChannel(notificationChannel)
//
//        // Create the notification
//        val notification = NotificationCompat.Builder(context, "default")
//            .setContentTitle("Scheduled Notification")
//            .setContentText("This is your notification!")
//            .setSmallIcon(R.drawable.ic_notification)
//            .setAutoCancel(true)
//            .build()
//
//        // Show the notification
//        notificationManager.notify(1, notification)
//    }
//}

class NotificationReceiver : BroadcastReceiver() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onReceive(context: Context, intent: Intent) {
        // Check if permission is granted on Android 13 and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                return  // If permission is not granted, do not show the notification
            }
        }

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create notification channel if necessary
        val notificationChannel = NotificationChannel("default", "Default", NotificationManager.IMPORTANCE_DEFAULT)
        notificationManager.createNotificationChannel(notificationChannel)

        // Create the notification with visibility set to show on the lock screen
        val notification = NotificationCompat.Builder(context, "default")
            .setContentTitle("Scheduled Notification")
            .setContentText("This is your notification!")
            .setSmallIcon(R.drawable.ic_notification)
            .setAutoCancel(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)  // Show on lock screen
            .setPriority(NotificationCompat.PRIORITY_HIGH)  // High priority for lock screen visibility
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)  // Helps identify it as a message-type notification
            .build()

        // Show the notification
        notificationManager.notify(1, notification)
    }
}



