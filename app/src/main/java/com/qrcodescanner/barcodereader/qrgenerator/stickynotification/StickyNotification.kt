package com.qrcodescanner.barcodereader.qrgenerator.stickynotification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.widget.RemoteViews
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.qrcodescanner.barcodereader.qrgenerator.R
import com.qrcodescanner.barcodereader.qrgenerator.activities.FirstOpenActivity


class StickyNotification {
    companion object {
        private const val channelId = "sticky_notification_channel"
        private const val notificationId = 1

        fun showNotification(context: Context) {
            // Check for POST_NOTIFICATIONS permission (for Android 13 and above)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    if (context is android.app.Activity) {
                        ActivityCompat.requestPermissions(
                            context,
                            arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                            1001
                        )
                    }
                    return
                }
            }
            // Create notification channel if necessary
            createNotificationChannel(context)

            // Create unique intents for each action
            val qrScanIntent = Intent(context, FirstOpenActivity::class.java).apply { putExtra("action", "scan_qr") }
            val createQrIntent = Intent(context, FirstOpenActivity::class.java).apply { putExtra("action", "create_qr") }
            val translateIntent = Intent(context, FirstOpenActivity::class.java).apply { putExtra("action", "translate_image") }
            val homeIntent = Intent(context, FirstOpenActivity::class.java).apply { putExtra("action", "home") }

            // Create unique PendingIntents
            val qrScanPendingIntent = PendingIntent.getActivity(context, 0, qrScanIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            val createQrPendingIntent = PendingIntent.getActivity(context, 1, createQrIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            val translatePendingIntent = PendingIntent.getActivity(context, 2, translateIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            val homePendingIntent = PendingIntent.getActivity(context, 3, homeIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)



            // Create custom views for both compact and expanded layouts
            val compactView = RemoteViews(context.packageName, R.layout.compact_notification)
            compactView.setOnClickPendingIntent(R.id.btn_scan_qr_container, qrScanPendingIntent)
            compactView.setOnClickPendingIntent(R.id.btn_create_qr_container, createQrPendingIntent)
            compactView.setOnClickPendingIntent(R.id.btn_translate_image_container, translatePendingIntent)
            compactView.setOnClickPendingIntent(R.id.btn_home_container, homePendingIntent)

            val expandedView = RemoteViews(context.packageName, R.layout.notification_layout)
            expandedView.setOnClickPendingIntent(R.id.btn_scan_qr_container, qrScanPendingIntent)
            expandedView.setOnClickPendingIntent(R.id.btn_create_qr_container, createQrPendingIntent)
            expandedView.setOnClickPendingIntent(R.id.btn_translate_image_container, translatePendingIntent)
            expandedView.setOnClickPendingIntent(R.id.btn_home_container, homePendingIntent)

            // Build the notification
            val builder = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.app_icon)
                .setStyle(NotificationCompat.DecoratedCustomViewStyle())
                .setCustomContentView(compactView)  // Set the compact view for collapsed notification
                .setCustomBigContentView(expandedView)  // Set the expanded view for expanded notification
                .setOngoing(true)  // Make it sticky
                .setAutoCancel(false)  // Prevent it from being dismissed automatically
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

            // Issue the notification
            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.notify(notificationId, builder.build())
        }

        // Create notification channel for devices running Android O and above
        private fun createNotificationChannel(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val name = "Sticky Notification"
                val description = "Notification for quick access"
                val importance = NotificationManager.IMPORTANCE_HIGH
                val channel = NotificationChannel(channelId, name, importance).apply {
                    this.description = description
                }
                val notificationManager = context.getSystemService(NotificationManager::class.java)
                notificationManager?.createNotificationChannel(channel)
            }
        }
    }
}














