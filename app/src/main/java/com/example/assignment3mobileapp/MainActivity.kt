package com.example.assignment3mobileapp
import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
/*
File          : MainActivity.kot
Project       : PROG3150 - Assignment 3
Programmer    : Dionisio Estupin III and Ygnacio Maza Sanchez
File Version  : 2025-04-14
*/

/*
Class: MainActivity
Description: The main entry point of the application that handles UI interactions, permissions,
             services, and navigation between activities.
*/
class MainActivity : AppCompatActivity() {
    private lateinit var longClickButton: Button
    private lateinit var statusText: TextView
    private lateinit var serviceButton: Button
    private lateinit var mapButton: Button
    private var receiver: MyAppReceiver? = null
    private var localReceiver: BroadcastReceiver? = null
    private var serviceRunning = false

    private val SERVICE_UPDATE_ACTION = "com.example.assignment3mobileapp.SERVICE_UPDATE"

    /*
    Function: onCreate(savedInstanceState: Bundle?)
    Description: Initializes the activity, sets up UI elements and their event listeners
    Parameters: Bundle? savedInstanceState - The previously saved state of the activity, if available
    Return Values: N/A
    */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)   // Set the activity layout //

        // Initialize UI elements
        longClickButton = findViewById(R.id.myLongButton)
        statusText = findViewById(R.id.statusText)
        serviceButton = findViewById(R.id.serviceButton)
        mapButton = findViewById(R.id.mapButton)

        // Set up long click listener
        longClickButton.setOnLongClickListener {
            val popupMenu = PopupMenu(this@MainActivity, longClickButton)
            popupMenu.menuInflater.inflate(R.menu.mymenu, popupMenu.menu)   // Inflate the menu resource //

            popupMenu.setOnMenuItemClickListener { item ->
                when (item.itemId) {   // Handle menu item selections //
                    R.id.mymenu_activity2 -> {
                        startActivity(Intent(this, Activity2::class.java))   // Navigate to Activity2 //
                        true
                    }
                    R.id.mymenu_activity3 -> {
                        startActivity(Intent(this, Activity3::class.java))   // Navigate to Activity3 //
                        true
                    }
                    else -> false
                }
            }

            popupMenu.show()   // Display the popup menu //
            true
        }

        // Service control button
        serviceButton.setOnClickListener {
            if (serviceRunning) {   // Check if service is already running //
                try {
                    stopService(Intent(this, MyAppService::class.java))   // Stop the running service //
                    serviceButton.text = getString(R.string.start_service)
                    serviceRunning = false
                    statusText.text = getString(R.string.service_stopped)
                } catch (e: Exception) {
                    Log.e("MainActivity", "Error stopping service: ${e.message}")   // Log any errors //
                }
            } else {
                if (checkNotificationPermission()) {   // Check for notification permission //
                    try {
                        val serviceIntent = Intent(this, MyAppService::class.java)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            startForegroundService(serviceIntent)   // Start as foreground service on Android O+ //
                        } else {
                            startService(serviceIntent)   // Start service for older Android versions //
                        }
                        serviceButton.text = getString(R.string.stop_service)
                        serviceRunning = true
                        statusText.text = getString(R.string.service_running)
                    } catch (e: Exception) {
                        Log.e("MainActivity", "Error starting service: ${e.message}")   // Log any errors //
                        Toast.makeText(this, "Error starting service: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

        // Map button
        mapButton.setOnClickListener {
            if (checkLocationPermission()) {   // Check for location permission //
                startActivity(Intent(this, MapsActivity::class.java))   // Open the maps activity //
            }
        }

        // Create local receiver for service updates
        createLocalReceiver()
    }

    /*
    Function: createLocalReceiver()
    Description: Creates a broadcast receiver to handle updates from the service
    Parameters: None
    Return Values: N/A
    */
    private fun createLocalReceiver() {
        localReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (intent.action == SERVICE_UPDATE_ACTION) {   // Check if it's our update action //
                    val data = intent.getStringExtra("update_data")
                    statusText.text = getString(R.string.latest_update, data)   // Update the status text //
                }
            }
        }
    }

    /*
    Function: onResume()
    Description: Called when the activity becomes visible to the user. Registers broadcast receivers.
    Parameters: None
    Return Values: N/A
    */
    override fun onResume() {
        super.onResume()
        // Register the broadcast receivers
        receiver = MyAppReceiver.register(this)   // Register system broadcast receiver //

        // Register local receiver with the proper flag for Android 13+
        try {
            val filter = IntentFilter(SERVICE_UPDATE_ACTION)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                registerReceiver(localReceiver, filter)   // Register receiver for Android 13+ //
            } else {
                registerReceiver(localReceiver, filter)   // Register receiver for older versions //
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Error registering local receiver: ${e.message}")   // Log any errors //
        }
    }

    /*
    Function: onPause()
    Description: Called when the activity is no longer visible to the user. Unregisters broadcast receivers.
    Parameters: None
    Return Values: N/A
    */
    override fun onPause() {
        super.onPause()
        // Unregister the receivers
        try {
            receiver?.let {
                unregisterReceiver(it)   // Unregister system receiver if not null //
                receiver = null
            }
        } catch (e: Exception) {
            Log.w("MainActivity", "Error unregistering system receiver: ${e.message}")   // Log any errors //
        }

        try {
            localReceiver?.let {
                unregisterReceiver(it)   // Unregister local receiver if not null //
            }
        } catch (e: Exception) {
            Log.w("MainActivity", "Error unregistering local receiver: ${e.message}")   // Log any errors //
        }
    }

    /*
    Function: onCreateOptionsMenu(menu: Menu)
    Description: Initialize the contents of the Activity's options menu
    Parameters: Menu menu - The options menu in which to place items
    Return Values: Boolean - Return true for the menu to be displayed
    */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.mymenu, menu)   // Inflate the menu resource //
        return true
    }

    /*
    Function: onOptionsItemSelected(item: MenuItem)
    Description: Called when an item in the options menu is selected
    Parameters: MenuItem item - The menu item that was selected
    Return Values: Boolean - Return true to indicate the item selection was handled
    */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {   // Handle menu item selections //
            R.id.mymenu_activity2 -> {
                startActivity(Intent(this, Activity2::class.java))   // Navigate to Activity2 //
                true
            }
            R.id.mymenu_activity3 -> {
                startActivity(Intent(this, Activity3::class.java))   // Navigate to Activity3 //
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    /*
    Function: checkNotificationPermission()
    Description: Checks if the app has notification permission and requests it if not granted
    Parameters: None
    Return Values: Boolean - Returns true if permission is granted, false otherwise
    */
    private fun checkNotificationPermission(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {   // Check if device is running Android 13+ //
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(   // Request notification permission //
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    PERMISSION_REQUEST_NOTIFICATION
                )
                return false
            }
        }
        return true   // Permission already granted or not needed for this Android version //
    }

    /*
    Function: checkLocationPermission()
    Description: Checks if the app has location permission and requests it if not granted
    Parameters: None
    Return Values: Boolean - Returns true if permission is granted, false otherwise
    */
    private fun checkLocationPermission(): Boolean {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Show explanation dialog if needed
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            ) {
                AlertDialog.Builder(this)   // Create explanation dialog //
                    .setTitle(getString(R.string.location_permission_title))
                    .setMessage(getString(R.string.location_permission_message))
                    .setPositiveButton(getString(R.string.ok)) { _, _ ->
                        // Request permission
                        ActivityCompat.requestPermissions(
                            this,
                            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                            PERMISSION_REQUEST_LOCATION
                        )
                    }
                    .create()
                    .show()
            } else {
                // No explanation needed, request permission
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    PERMISSION_REQUEST_LOCATION
                )
            }
            return false
        }
        return true   // Permission already granted //
    }

    /*
    Function: onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray)
    Description: Handles the result of a permission request
    Parameters: Int requestCode - The request code passed in requestPermissions
                Array<String> permissions - The requested permissions (never null)
                IntArray grantResults - The grant results (PERMISSION_GRANTED or PERMISSION_DENIED)
    Return Values: N/A
    */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSION_REQUEST_NOTIFICATION -> {   // Handle notification permission result //
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    val serviceIntent = Intent(this, MyAppService::class.java)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        startForegroundService(serviceIntent)   // Start as foreground service on Android O+ //
                    } else {
                        startService(serviceIntent)   // Start service for older Android versions //
                    }
                    serviceButton.text = getString(R.string.stop_service)
                    serviceRunning = true
                } else {
                    Toast.makeText(this, getString(R.string.notification_permission_denied), Toast.LENGTH_SHORT).show()
                }
            }
            PERMISSION_REQUEST_LOCATION -> {   // Handle location permission result //
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    startActivity(Intent(this, MapsActivity::class.java))   // Open the maps activity //
                } else {
                    Toast.makeText(this, getString(R.string.location_permission_denied), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    companion object {
        private const val PERMISSION_REQUEST_NOTIFICATION = 1001   // Request code for notification permission //
        private const val PERMISSION_REQUEST_LOCATION = 1002   // Request code for location permission //
    }
}