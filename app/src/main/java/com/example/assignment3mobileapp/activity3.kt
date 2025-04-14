package com.example.assignment3mobileapp

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class Activity3 : AppCompatActivity() {
    private var goBack: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_3)

        goBack = findViewById(R.id.goBack3)
        goBack?.setOnClickListener {
            // finish the activity
            finish()
        }
    }
}