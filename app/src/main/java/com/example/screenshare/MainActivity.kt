package com.example.screenshare

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import com.example.screenshare.service.ScreenShareService

class MainActivity : AppCompatActivity() {
    private lateinit var btnStart: ImageButton
    private lateinit var btnStop: ImageButton
    private lateinit var spinner: Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnStart = findViewById(R.id.btnStart)
        btnStop = findViewById(R.id.btnStop)
        spinner = findViewById(R.id.spinnerResolution)

        btnStart.setOnClickListener {
            val res = spinner.selectedItem.toString()
            val intent = Intent(this, ScreenShareService::class.java)
            intent.putExtra("resolution", res)
            startForegroundService(intent)
        }

        btnStop.setOnClickListener {
            val intent = Intent(this, ScreenShareService::class.java)
            stopService(intent)
        }
    }
}
