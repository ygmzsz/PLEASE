package com.example.assignment3mobileapp

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MyAppService : Service() {
    companion object {
        const val CHANNEL_ID = "MyAppServiceChannel"
        const val NOTIFICATION_ID = 1001
        const val UPDATE_ACTION = "com.example.assignment3mobileapp.ACTION_UPDATE"
        const val UPDATE_EXTRA = "update_data"
    }

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.Default + serviceJob)

    override fun onCreate() {
        super.onCreate()
        try {
            createNotificationChannel()
            startForeground(NOTIFICATION_ID, createNotification("Service started"))

            // Start periodic updates in a coroutine
            serviceScope.launch {
                var count = 0
                while (true) {
                    try {
                        delay(10000) // Update every 10 seconds
                        count++

                        // Update notification
                        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                        notificationManager.notify(NOTIFICATION_ID, createNotification("Update #$count"))

                        // Broadcast update with the correct action
                        val intent = Intent(UPDATE_ACTION)
                        intent.setPackage(packageName) // Set package to ensure delivery to your app
                        intent.putExtra(UPDATE_EXTRA, "Service update #$count at ${System.currentTimeMillis()}")
                        sendBroadcast(intent)
                    } catch (e: Exception) {
                        Log.e("MyAppService", "Error in service loop: ${e.message}", e)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("MyAppService", "Error creating service: ${e.message}", e)
            stopSelf()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Restart if killed
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        // Cancel all coroutines when service is destroyed
        serviceScope.cancel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "My App Service"
            val descriptionText = "Channel for My App Service"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(content: String): android.app.Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("My App Service")
            .setContentText(content)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(pendingIntent)
            .build()
    }
}