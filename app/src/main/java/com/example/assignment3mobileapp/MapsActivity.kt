package com.example.assignment3mobileapp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

/*
Class: MapsActivity
Description: The activity that displays a Google Map and handles location-related functionality.
             Implements OnMapReadyCallback to receive the GoogleMap object when the map is ready to be used.
*/
class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap   // The Google Map object //
    private lateinit var fusedLocationClient: FusedLocationProviderClient   // Client to access the device location //

    /*
    Function: onCreate(savedInstanceState: Bundle?)
    Description: Initializes the activity, sets up the map fragment, and initializes the location client
    Parameters: Bundle? savedInstanceState - The previously saved state of the activity, if available
    Return Values: N/A
    */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)   // Set the activity layout //

        // Initialize location client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Obtain the SupportMapFragment and get notified when the map is ready
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment   // Get the map fragment from layout //
        mapFragment.getMapAsync(this)   // Request the GoogleMap object asynchronously //
    }

    /*
    Function: onMapReady(googleMap: GoogleMap)
    Description: Called when the map is ready to be used. Configures the map and displays user's location if permitted.
    Parameters: GoogleMap googleMap - The GoogleMap object
    Return Values: N/A
    */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Enable zoom controls
        mMap.uiSettings.isZoomControlsEnabled = true   // Show zoom controls on the map //

        // Check permission
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            mMap.isMyLocationEnabled = true   // Show the user's location on the map //

            // Get last known location and move camera there
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val currentLatLng = LatLng(location.latitude, location.longitude)   // Create LatLng from location //
                    mMap.addMarker(
                        MarkerOptions()
                            .position(currentLatLng)
                            .title("Your Location")   // Add marker at user's location //
                    )
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))   // Move camera to user's location //
                }
            }
        } else {
            // Default location (perhaps a university campus or something relevant to your app)
            val defaultLocation = LatLng(43.6532, -79.3832) // Toronto   // Default location if permission not granted //
            mMap.addMarker(
                MarkerOptions()
                    .position(defaultLocation)
                    .title("Default Location")   // Add marker at default location //
            )
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 10f))   // Move camera to default location //

            Toast.makeText(
                this,
                "Location permission not granted, showing default location",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}