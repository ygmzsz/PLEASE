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

class MainActivity : AppCompatActivity() {
    private lateinit var longClickButton: Button
    private lateinit var statusText: TextView
    private lateinit var serviceButton: Button
    private lateinit var mapButton: Button
    private var receiver: MyAppReceiver? = null
    private var localReceiver: BroadcastReceiver? = null
    private var serviceRunning = false

    private val SERVICE_UPDATE_ACTION = "com.example.assignment3mobileapp.SERVICE_UPDATE"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize UI elements
        longClickButton = findViewById(R.id.myLongButton)
        statusText = findViewById(R.id.statusText)
        serviceButton = findViewById(R.id.serviceButton)
        mapButton = findViewById(R.id.mapButton)

        // Set up long click listener
        longClickButton.setOnLongClickListener {
            val popupMenu = PopupMenu(this@MainActivity, longClickButton)
            popupMenu.menuInflater.inflate(R.menu.mymenu, popupMenu.menu)

            popupMenu.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.mymenu_activity2 -> {
                        startActivity(Intent(this, Activity2::class.java))
                        true
                    }
                    R.id.mymenu_activity3 -> {
                        startActivity(Intent(this, Activity3::class.java))
                        true
                    }
                    else -> false
                }
            }

            popupMenu.show()
            true
        }

        // Service control button
        serviceButton.setOnClickListener {
            if (serviceRunning) {
                try {
                    stopService(Intent(this, MyAppService::class.java))
                    serviceButton.text = getString(R.string.start_service)
                    serviceRunning = false
                    statusText.text = getString(R.string.service_stopped)
                } catch (e: Exception) {
                    Log.e("MainActivity", "Error stopping service: ${e.message}")
                }
            } else {
                if (checkNotificationPermission()) {
                    try {
                        val serviceIntent = Intent(this, MyAppService::class.java)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            startForegroundService(serviceIntent)
                        } else {
                            startService(serviceIntent)
                        }
                        serviceButton.text = getString(R.string.stop_service)
                        serviceRunning = true
                        statusText.text = getString(R.string.service_running)
                    } catch (e: Exception) {
                        Log.e("MainActivity", "Error starting service: ${e.message}")
                        Toast.makeText(this, "Error starting service: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

        // Map button
        mapButton.setOnClickListener {
            if (checkLocationPermission()) {
                startActivity(Intent(this, MapsActivity::class.java))
            }
        }

        // Create local receiver for service updates
        createLocalReceiver()
    }

    private fun createLocalReceiver() {
        localReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (intent.action == SERVICE_UPDATE_ACTION) {
                    val data = intent.getStringExtra("update_data")
                    statusText.text = getString(R.string.latest_update, data)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Register the broadcast receivers
        receiver = MyAppReceiver.register(this)

        // Register local receiver with the proper flag for Android 13+
        try {
            val filter = IntentFilter(SERVICE_UPDATE_ACTION)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                registerReceiver(localReceiver, filter)
            } else {
                registerReceiver(localReceiver, filter)
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Error registering local receiver: ${e.message}")
        }
    }

    override fun onPause() {
        super.onPause()
        // Unregister the receivers
        try {
            receiver?.let {
                unregisterReceiver(it)
                receiver = null
            }
        } catch (e: Exception) {
            Log.w("MainActivity", "Error unregistering system receiver: ${e.message}")
        }

        try {
            localReceiver?.let {
                unregisterReceiver(it)
            }
        } catch (e: Exception) {
            Log.w("MainActivity", "Error unregistering local receiver: ${e.message}")
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.mymenu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.mymenu_activity2 -> {
                startActivity(Intent(this, Activity2::class.java))
                true
            }
            R.id.mymenu_activity3 -> {
                startActivity(Intent(this, Activity3::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun checkNotificationPermission(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    PERMISSION_REQUEST_NOTIFICATION
                )
                return false
            }
        }
        return true
    }

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
                AlertDialog.Builder(this)
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
        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSION_REQUEST_NOTIFICATION -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    val serviceIntent = Intent(this, MyAppService::class.java)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        startForegroundService(serviceIntent)
                    } else {
                        startService(serviceIntent)
                    }
                    serviceButton.text = getString(R.string.stop_service)
                    serviceRunning = true
                } else {
                    Toast.makeText(this, getString(R.string.notification_permission_denied), Toast.LENGTH_SHORT).show()
                }
            }
            PERMISSION_REQUEST_LOCATION -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    startActivity(Intent(this, MapsActivity::class.java))
                } else {
                    Toast.makeText(this, getString(R.string.location_permission_denied), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    companion object {
        private const val PERMISSION_REQUEST_NOTIFICATION = 1001
        private const val PERMISSION_REQUEST_LOCATION = 1002
    }
}