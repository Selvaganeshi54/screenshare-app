package com.example.screenshare

import android.app.Activity
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {
    private val REQUEST_SCREEN_CAPTURE = 1001
    private lateinit var projectionManager: MediaProjectionManager
    private lateinit var spinner: Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        projectionManager = getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager

        val startBtn = findViewById<ImageButton>(R.id.btn_start)
        val stopBtn = findViewById<ImageButton>(R.id.btn_stop)
        spinner = findViewById(R.id.res_spinner)

        val options = listOf("720p", "480p", "360p")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, options)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
        spinner.setSelection(0)

        startBtn.setOnClickListener {
            val captureIntent = projectionManager.createScreenCaptureIntent()
            startActivityForResult(captureIntent, REQUEST_SCREEN_CAPTURE)
        }

        stopBtn.setOnClickListener {
            val stop = Intent(this, ScreenCaptureService::class.java)
            stop.action = ScreenCaptureService.ACTION_STOP
            ContextCompat.startForegroundService(this, stop)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_SCREEN_CAPTURE && resultCode == Activity.RESULT_OK && data != null) {
            val sel = spinner.selectedItem as String
            val intent = Intent(this, ScreenCaptureService::class.java)
            intent.action = ScreenCaptureService.ACTION_START
            intent.putExtra("resultCode", resultCode)
            intent.putExtra("resolution", sel)
            intent.putExtra("data", data)
            ContextCompat.startForegroundService(this, intent)
        }
    }
}
