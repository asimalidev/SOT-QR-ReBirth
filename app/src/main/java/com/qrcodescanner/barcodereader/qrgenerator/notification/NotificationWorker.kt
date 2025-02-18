package com.qrcodescanner.barcodereader.qrgenerator.notification

import android.content.Context
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.work.Worker
import androidx.work.WorkerParameters

class NotificationWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

    override fun doWork(): Result {
        val title = inputData.getString("title") ?: "Notification"
        val content = inputData.getString("content") ?: "Check out our app for more!"

        Log.d("NotificationWorker", "Worker started with title='$title', content='$content'")

        if (!isAppInForeground()) {
            Log.d("NotificationWorker", "App is in background. Sending notification.")
            val notificationHelper = NotificationHelper(applicationContext)
            notificationHelper.sendNotification(title, content)
        } else {
            Log.d("NotificationWorker", "App is in foreground. Skipping notification.")
        }

        return Result.success()
    }

    private fun isAppInForeground(): Boolean {
        val isForeground = ProcessLifecycleOwner.get().lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)
        Log.d("NotificationWorker", "App foreground state: $isForeground")
        return isForeground
    }
}







