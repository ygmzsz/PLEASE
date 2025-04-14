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

/*
Class: MyAppService
Description: A foreground service that runs continuously, even when the app is in the background.
             It sends periodic updates via broadcasts and shows a persistent notification.
*/
class MyAppService : Service() {
    companion object {
        const val CHANNEL_ID = "MyAppServiceChannel"   // Notification channel ID //
        const val NOTIFICATION_ID = 1001   // Unique ID for the notification //
        const val UPDATE_ACTION = "com.example.assignment3mobileapp.ACTION_UPDATE"   // Broadcast action for updates //
        const val UPDATE_EXTRA = "update_data"   // Key for extra data in the broadcast intent //
    }

    private val serviceJob = SupervisorJob()   // Parent job for all coroutines in this service //
    private val serviceScope = CoroutineScope(Dispatchers.Default + serviceJob)   // Coroutine scope for the service //

    /*
    Function: onCreate()
    Description: Called when the service is first created. Sets up notification channel and starts the service work.
    Parameters: None
    Return Values: N/A
    */
    override fun onCreate() {
        super.onCreate()
        try {
            createNotificationChannel()   // Create the notification channel //
            startForeground(NOTIFICATION_ID, createNotification("Service started"))   // Make the service foreground //

            // Start periodic updates in a coroutine
            serviceScope.launch {
                var count = 0
                while (true) {
                    try {
                        delay(10000) // Update every 10 seconds   // Pause the coroutine for 10 seconds //
                        count++

                        // Update notification
                        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                        notificationManager.notify(NOTIFICATION_ID, createNotification("Update #$count"))   // Update the notification text //

                        // Broadcast update with the correct action
                        val intent = Intent(UPDATE_ACTION)
                        intent.setPackage(packageName) // Set package to ensure delivery to your app   // Target this app only //
                        intent.putExtra(UPDATE_EXTRA, "Service update #$count at ${System.currentTimeMillis()}")   // Add update data //
                        sendBroadcast(intent)   // Send the broadcast to registered receivers //
                    } catch (e: Exception) {
                        Log.e("MyAppService", "Error in service loop: ${e.message}", e)   // Log any errors //
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("MyAppService", "Error creating service: ${e.message}", e)   // Log any errors //
            stopSelf()   // Stop the service if there's an error //
        }
    }

    /*
    Function: onStartCommand(intent: Intent?, flags: Int, startId: Int)
    Description: Called when the service is started or restarted
    Parameters: Intent? intent - The Intent supplied to startService(Intent), may be null
                Int flags - Additional data about this start request
                Int startId - A unique integer representing this specific request to start
    Return Values: Int - The return value indicates what semantics the system should use for the service's current started state
    */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Restart if killed
        return START_STICKY   // Request restart if the service is killed //
    }

    /*
    Function: onBind(intent: Intent?)
    Description: Called when a client binds to the service with bindService()
    Parameters: Intent? intent - The Intent that was used to bind to this service
    Return Values: IBinder? - Return an IBinder through which clients can call on to the service
    */
    override fun onBind(intent: Intent?): IBinder? {
        return null   // This service doesn't support binding //
    }

    /*
    Function: onDestroy()
    Description: Called when the service is no longer used and is being destroyed
    Parameters: None
    Return Values: N/A
    */
    override fun onDestroy() {
        super.onDestroy()
        // Cancel all coroutines when service is destroyed
        serviceScope.cancel()   // Cancel all running coroutines to clean up //
    }

    /*
    Function: createNotificationChannel()
    Description: Creates a notification channel for Android O and above
    Parameters: None
    Return Values: N/A
    */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {   // Check if running on Android 8.0 (Oreo) or higher //
            val name = "My App Service"
            val descriptionText = "Channel for My App Service"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)   // Register the channel with the system //
        }
    }

    /*
    Function: createNotification(content: String)
    Description: Creates a notification with the given content text
    Parameters: String content - The text to display in the notification
    Return Values: android.app.Notification - The created notification
    */
    private fun createNotification(content: String): android.app.Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_IMMUTABLE   // Create pending intent to open the app when notification is tapped //
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("My App Service")
            .setContentText(content)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(pendingIntent)
            .build()   // Build and return the notification //
    }
}