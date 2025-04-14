package com.example.assignment3mobileapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.widget.Toast
import androidx.localbroadcastmanager.content.LocalBroadcastManager

class MyAppReceiver : BroadcastReceiver() {

    companion object {
        // Register broadcast receiver dynamically
        fun register(context: Context): MyAppReceiver {
            val receiver = MyAppReceiver()
            val filter = IntentFilter().apply {
                addAction(MyAppService.UPDATE_ACTION)
                addAction(Intent.ACTION_BATTERY_LOW)
                addAction(Intent.ACTION_POWER_CONNECTED)
                addAction(Intent.ACTION_POWER_DISCONNECTED)
            }
            context.registerReceiver(receiver, filter)
            return receiver
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            MyAppService.UPDATE_ACTION -> {
                val data = intent.getStringExtra(MyAppService.UPDATE_EXTRA) ?: "No data"
                Toast.makeText(context, "Service update: $data", Toast.LENGTH_SHORT).show()

                // Let's also notify MainActivity if it's active
                val localIntent = Intent("service_update")
                localIntent.putExtra("update_data", data)
                LocalBroadcastManager.getInstance(context).sendBroadcast(localIntent)
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
    }
}