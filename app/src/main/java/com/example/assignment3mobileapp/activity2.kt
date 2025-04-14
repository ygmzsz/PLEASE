package com.example.assignment3mobileapp

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity

class Activity2 : AppCompatActivity() {
    private var goBack: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_2)

        goBack = findViewById(R.id.goBack2)
        goBack?.setOnClickListener {
            // finish the activity
            finish()
        }

        val callback = object : OnBackPressedCallback(true) {
            // finish the activity
            override fun handleOnBackPressed() {
                Toast.makeText(this@Activity2, "Back Pressed", Toast.LENGTH_SHORT).show()
            }
        }

        onBackPressedDispatcher.addCallback(callback)
    }
}