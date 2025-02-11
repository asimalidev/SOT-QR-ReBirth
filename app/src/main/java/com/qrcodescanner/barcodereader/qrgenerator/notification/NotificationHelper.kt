package com.qrcodescanner.barcodereader.qrgenerator.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.window.SplashScreen
import androidx.core.app.NotificationCompat
import com.qrcodescanner.barcodereader.qrgenerator.R
import com.qrcodescanner.barcodereader.qrgenerator.activities.FirstOpenActivity
import com.qrcodescanner.barcodereader.qrgenerator.activities.SplashQRActivity


class NotificationHelper(private val context: Context) {

    private val channelId = "high_priority_channel"
    private val notificationId = 1234

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "High Priority Notifications"
            val descriptionText = "Notifications that pop up as a message"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
            Log.d("NotificationHelper", "High-priority notification channel created: $channelId")
        }
    }

    fun sendNotification(title: String, content: String) {
        Log.d(
            "NotificationHelper",
            "Preparing to send notification: Title='$title', Content='$content'"
        )

        val resultIntent = Intent(context, FirstOpenActivity::class.java)
      // Create the TaskStackBuilder.
        val resultPendingIntent: PendingIntent? = TaskStackBuilder.create(context).run {
            // Add the intent, which inflates the back stack.
            addNextIntentWithParentStack(resultIntent)
            // Get the PendingIntent containing the entire back stack.
            getPendingIntent(
                0,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }

        val bigTextStyle = NotificationCompat.BigTextStyle().bigText(content)

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.app_icon)  // Ensure this is a valid drawable
            .setContentTitle(title)
            .setContentText(content)
            .setStyle(bigTextStyle)
            .setPriority(NotificationCompat.PRIORITY_HIGH)  // High priority for popups
            .setDefaults(NotificationCompat.DEFAULT_ALL)  // Include vibration and sound
            .setFullScreenIntent(resultPendingIntent, true)  // Show as a popup if possible
            .setContentIntent(resultPendingIntent)
            .setAutoCancel(true)
            .build()

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, notification)
        Log.d("NotificationHelper", "Notification sent: Title='$title'")
    }
}




