package com.example.assignment3mobileapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.util.Log
import android.widget.Toast

class MyAppReceiver : BroadcastReceiver() {

    companion object {
        private const val SERVICE_UPDATE_ACTION = "com.example.assignment3mobileapp.SERVICE_UPDATE"

        // Register broadcast receiver dynamically
        fun register(context: Context): MyAppReceiver {
            val receiver = MyAppReceiver()
            val filter = IntentFilter().apply {
                addAction(MyAppService.UPDATE_ACTION)
                addAction(Intent.ACTION_BATTERY_LOW)
                addAction(Intent.ACTION_POWER_CONNECTED)
                addAction(Intent.ACTION_POWER_DISCONNECTED)
            }

            try {
                // Use the appropriate flag based on Android version
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    context.registerReceiver(receiver, filter, Context.RECEIVER_EXPORTED)
                } else {
                    context.registerReceiver(receiver, filter)
                }
                return receiver
            } catch (e: Exception) {
                Log.e("MyAppReceiver", "Error registering receiver: ${e.message}")
                throw e
            }
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        try {
            when (intent.action) {
                MyAppService.UPDATE_ACTION -> {
                    val data = intent.getStringExtra(MyAppService.UPDATE_EXTRA) ?: "No data"
                    Toast.makeText(context, "Service update: $data", Toast.LENGTH_SHORT).show()

                    // Send a broadcast to the MainActivity using explicit intent
                    val localIntent = Intent(SERVICE_UPDATE_ACTION)
                    localIntent.setPackage(context.packageName)
                    localIntent.putExtra("update_data", data)

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        // Fix the type mismatch issue - pass null for receiverPermission
                        context.sendBroadcast(localIntent)
                    } else {
                        context.sendBroadcast(localIntent)
                    }
                }
                Intent.ACTION_BATTERY_LOW -> {
                    Toast.makeText(context, "Battery is low!", Toast.LENGTH_SHORT).show()
                }
                Intent.ACTION_POWER_CONNECTED -> {
                    Toast.makeText(context, "Power connected", Toast.LENGTH_SHORT).show()
                }
                Intent.ACTION_POWER_DISCONNECTED -> {
                    Toast.makeText(context, "Power disconnected", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            Log.e("MyAppReceiver", "Error in onReceive: ${e.message}")
        }
    }
}