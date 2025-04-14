package com.example.assignment3mobileapp

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity

/*
Class: Activity2
Description: A simple activity that demonstrates handling back button presses
             and provides a button to return to the previous activity.
*/
class Activity2 : AppCompatActivity() {
    private var goBack: Button? = null   // Button to return to previous activity //

    /*
    Function: onCreate(savedInstanceState: Bundle?)
    Description: Initializes the activity and sets up UI elements and back press handling
    Parameters: Bundle? savedInstanceState - The previously saved state of the activity, if available
    Return Values: N/A
    */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_2)   // Set the activity layout //

        goBack = findViewById(R.id.goBack2)
        goBack?.setOnClickListener {
            // finish the activity
            finish()   // Return to previous activity when button is clicked //
        }

        val callback = object : OnBackPressedCallback(true) {
            // finish the activity
            override fun handleOnBackPressed() {
                Toast.makeText(this@Activity2, "Back Pressed", Toast.LENGTH_SHORT).show()   // Show toast when back button is pressed //
            }
        }

        onBackPressedDispatcher.addCallback(callback)   // Register the back press callback //
    }
}