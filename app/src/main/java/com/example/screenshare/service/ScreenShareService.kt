package com.example.screenshare.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import org.webrtc.*

class ScreenShareService : Service() {
    private val CHANNEL_ID = "ScreenShareChannel"
    private var peerConnectionFactory: PeerConnectionFactory? = null
    private var videoCapturer: VideoCapturer? = null
    private var videoSource: VideoSource? = null
    private var videoTrack: VideoTrack? = null
    private var eglBase: EglBase? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(1, createNotification())
        eglBase = EglBase.create()
        initializePeerConnectionFactory()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val resolution = intent?.getStringExtra("resolution") ?: "720p"
        startScreenCapture(resolution)
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        videoCapturer?.stopCapture()
        videoSource?.dispose()
        videoTrack?.dispose()
        peerConnectionFactory?.dispose()
        eglBase?.release()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Screen Sharing",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Screen sharing active")
            .setContentText("Your screen is being shared.")
            .setSmallIcon(android.R.drawable.ic_menu_camera)
            .setOngoing(true)
            .build()
    }

    private fun initializePeerConnectionFactory() {
        val options = PeerConnectionFactory.InitializationOptions.builder(this)
            .setEnableInternalTracer(false)
            .createInitializationOptions()
        PeerConnectionFactory.initialize(options)
        val encoderFactory = HardwareVideoEncoderFactory(
            eglBase?.eglBaseContext, true, true
        )
        val decoderFactory = HardwareVideoDecoderFactory(eglBase?.eglBaseContext)
        peerConnectionFactory = PeerConnectionFactory.builder()
            .setVideoEncoderFactory(encoderFactory)
            .setVideoDecoderFactory(decoderFactory)
            .createPeerConnectionFactory()
    }

    private fun startScreenCapture(resolution: String) {
        val (width, height) = when (resolution) {
            "720p" -> 1280 to 720
            "480p" -> 854 to 480
            "360p" -> 640 to 360
            else -> 1280 to 720
        }
        val mgr = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        // TODO: Request MediaProjection permission from MainActivity and pass data here
        // Placeholder: videoCapturer = ScreenCapturerAndroid(...)
        // For demo, skip actual capture setup
        videoSource = peerConnectionFactory?.createVideoSource(false)
        // Hardcap FPS to 15
        val constraints = MediaConstraints().apply {
            mandatory.add(MediaConstraints.KeyValuePair("maxFrameRate", "15"))
            mandatory.add(MediaConstraints.KeyValuePair("minFrameRate", "15"))
            mandatory.add(MediaConstraints.KeyValuePair("maxWidth", width.toString()))
            mandatory.add(MediaConstraints.KeyValuePair("maxHeight", height.toString()))
        }
        // videoCapturer?.initialize(...)
        // videoCapturer?.startCapture(width, height, 15)
        videoTrack = peerConnectionFactory?.createVideoTrack("ARDAMSv0", videoSource)
        // TODO: Add track to PeerConnection and signaling logic
    }
}
