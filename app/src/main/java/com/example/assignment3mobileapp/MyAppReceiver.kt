package com.example.assignment3mobileapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.util.Log
import android.widget.Toast

/*
Class: MyAppReceiver
Description: A broadcast receiver that handles various system events and custom broadcasts from the app's service.
             It shows toast messages and forwards updates to the MainActivity.
*/
class MyAppReceiver : BroadcastReceiver() {

    companion object {
        private const val SERVICE_UPDATE_ACTION = "com.example.assignment3mobileapp.SERVICE_UPDATE"   // Action for local broadcasts //

        /*
        Function: register(context: Context)
        Description: Registers the broadcast receiver with the specified context
        Parameters: Context context - The context to register the receiver with
        Return Values: MyAppReceiver - The registered receiver instance
        */
        fun register(context: Context): MyAppReceiver {
            val receiver = MyAppReceiver()
            val filter = IntentFilter().apply {
                addAction(MyAppService.UPDATE_ACTION)   // Listen for service updates //
                addAction(Intent.ACTION_BATTERY_LOW)   // Listen for low battery events //
                addAction(Intent.ACTION_POWER_CONNECTED)   // Listen for power connection events //
                addAction(Intent.ACTION_POWER_DISCONNECTED)   // Listen for power disconnection events //
            }

            try {
                // Use the appropriate flag based on Android version
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    context.registerReceiver(receiver, filter, Context.RECEIVER_EXPORTED)   // Register with export flag on Android 13+ //
                } else {
                    context.registerReceiver(receiver, filter)   // Register without flag on older Android versions //
                }
                return receiver
            } catch (e: Exception) {
                Log.e("MyAppReceiver", "Error registering receiver: ${e.message}")   // Log any errors //
                throw e
            }
        }
    }

    /*
    Function: onReceive(context: Context, intent: Intent)
    Description: Called when the registered broadcasts are received
    Parameters: Context context - The context in which the receiver is running
                Intent intent - The intent being received
    Return Values: N/A
    */
    override fun onReceive(context: Context, intent: Intent) {
        try {
            when (intent.action) {
                MyAppService.UPDATE_ACTION -> {   // Handle service update broadcasts //
                    val data = intent.getStringExtra(MyAppService.UPDATE_EXTRA) ?: "No data"   // Get the update data //
                    Toast.makeText(context, "Service update: $data", Toast.LENGTH_SHORT).show()   // Show toast with update //

                    // Send a broadcast to the MainActivity using explicit intent
                    val localIntent = Intent(SERVICE_UPDATE_ACTION)
                    localIntent.setPackage(context.packageName)   // Target this app only //
                    localIntent.putExtra("update_data", data)   // Pass the update data //
                    context.sendBroadcast(localIntent)   // Send the broadcast //
                }
                Intent.ACTION_BATTERY_LOW -> {   // Handle low battery event //
                    Toast.makeText(context, "Battery is low!", Toast.LENGTH_SHORT).show()
                }
                Intent.ACTION_POWER_CONNECTED -> {   // Handle power connected event //
                    Toast.makeText(context, "Power connected", Toast.LENGTH_SHORT).show()
                }
                Intent.ACTION_POWER_DISCONNECTED -> {   // Handle power disconnected event //
                    Toast.makeText(context, "Power disconnected", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            Log.e("MyAppReceiver", "Error in onReceive: ${e.message}")   // Log any errors //
        }
    }
}