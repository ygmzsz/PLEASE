package com.example.assignment3mobileapp

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
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
import androidx.localbroadcastmanager.content.LocalBroadcastManager

class MainActivity : AppCompatActivity() {
    private var longClickButton: Button? = null
    private var statusText: TextView? = null
    private var serviceButton: Button? = null
    private var mapButton: Button? = null
    private var receiver: MyAppReceiver? = null
    private var serviceRunning = false

    private val localReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == "service_update") {
                val data = intent.getStringExtra("update_data")
                statusText?.text = "Latest update: $data"
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize UI elements
        longClickButton = findViewById(R.id.myLongButton)
        statusText = findViewById(R.id.statusText)
        serviceButton = findViewById(R.id.serviceButton)
        mapButton = findViewById(R.id.mapButton)

        // Set up long click listener as before
        longClickButton?.setOnLongClickListener { view ->
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
        serviceButton?.setOnClickListener {
            if (serviceRunning) {
                stopService(Intent(this, MyAppService::class.java))
                serviceButton?.text = "Start Service"
                serviceRunning = false
            } else {
                if (checkNotificationPermission()) {
                    startService(Intent(this, MyAppService::class.java))
                    serviceButton?.text = "Stop Service"
                    serviceRunning = true
                }
            }
        }

        // Map button
        mapButton?.setOnClickListener {
            if (checkLocationPermission()) {
                startActivity(Intent(this, MapsActivity::class.java))
            }
        }

        // Register for local broadcasts
        LocalBroadcastManager.getInstance(this).registerReceiver(
            localReceiver,
            IntentFilter("service_update")
        )
    }

    override fun onResume() {
        super.onResume()
        // Register the broadcast receiver
        receiver = MyAppReceiver.register(this)
    }

    override fun onPause() {
        super.onPause()
        // Unregister the broadcast receiver
        receiver?.let {
            try {
                unregisterReceiver(it)
            } catch (e: Exception) {
                // Receiver might not be registered
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(localReceiver)
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
                    .setTitle("Location Permission Needed")
                    .setMessage("This app needs location permission to show your current location on the map.")
                    .setPositiveButton("OK") { _, _ ->
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
                    startService(Intent(this, MyAppService::class.java))
                    serviceButton?.text = "Stop Service"
                    serviceRunning = true
                } else {
                    Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show()
                }
                return
            }
            PERMISSION_REQUEST_LOCATION -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    startActivity(Intent(this, MapsActivity::class.java))
                } else {
                    Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show()
                }
                return
            }
        }
    }

    companion object {
        private const val PERMISSION_REQUEST_NOTIFICATION = 1001
        private const val PERMISSION_REQUEST_LOCATION = 1002
    }
}