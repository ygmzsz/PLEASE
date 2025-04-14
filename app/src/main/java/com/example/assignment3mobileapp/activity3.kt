package com.example.assignment3mobileapp

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

/*
Class: Activity3
Description: A simple activity that provides a button to return to the previous activity.
*/
class Activity3 : AppCompatActivity() {
    private var goBack: Button? = null   // Button to return to previous activity //

    /*
    Function: onCreate(savedInstanceState: Bundle?)
    Description: Initializes the activity and sets up the back button
    Parameters: Bundle? savedInstanceState - The previously saved state of the activity, if available
    Return Values: N/A
    */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_3)   // Set the activity layout //

        goBack = findViewById(R.id.goBack3)
        goBack?.setOnClickListener {
            // finish the activity
            finish()   // Return to previous activity when button is clicked //
        }
    }
}