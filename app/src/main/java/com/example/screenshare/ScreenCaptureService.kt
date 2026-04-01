package com.example.screenshare

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import org.webrtc.*

class ScreenCaptureService : Service() {
    companion object {
        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"
        const val NOTIF_CHANNEL = "screenshare_channel"
        const val NOTIF_ID = 1337
    }

    private var peerConnectionFactory: PeerConnectionFactory? = null
    private var eglBase: EglBase? = null
    private var videoCapturer: VideoCapturer? = null
    private var videoSource: VideoSource? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val resultCode = intent.getIntExtra("resultCode", -1)
                val data = intent.getParcelableExtra<Intent>("data")
                val resolution = intent.getStringExtra("resolution") ?: "720p"
                if (resultCode != -1 && data != null) {
                    startForeground(NOTIF_ID, buildNotification())
                    startCapture(resultCode, data, resolution)
                } else {
                    stopSelf()
                }
            }
            ACTION_STOP -> {
                stopCapture()
                stopForeground(true)
                stopSelf()
            }
        }
        return START_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val chan = NotificationChannel(NOTIF_CHANNEL, "Screen sharing", NotificationManager.IMPORTANCE_LOW)
            nm.createNotificationChannel(chan)
        }
    }

    private fun buildNotification(): Notification {
        val pi = PendingIntent.getActivity(this, 0, Intent(this, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE)
        return NotificationCompat.Builder(this, NOTIF_CHANNEL)
            .setContentTitle("Screen sharing")
            .setContentText("Sharing your screen")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(pi)
            .setOngoing(true)
            .build()
    }

    private fun startCapture(resultCode: Int, data: Intent, resolution: String) {
        // Initialize WebRTC
        val options = PeerConnectionFactory.InitializationOptions.builder(this).createInitializationOptions()
        PeerConnectionFactory.initialize(options)

        eglBase = EglBase.create()
        val encoderFactory = HardwareVideoEncoderFactory(eglBase!!.eglBaseContext, true, true)
        val decoderFactory = HardwareVideoDecoderFactory(eglBase!!.eglBaseContext)

        val factoryBuilder = PeerConnectionFactory.builder()
            .setVideoEncoderFactory(encoderFactory)
            .setVideoDecoderFactory(decoderFactory)

        peerConnectionFactory = factoryBuilder.createPeerConnectionFactory()

        // Create video source and capturer
        videoSource = peerConnectionFactory?.createVideoSource(true)
        val surfaceHelper = SurfaceTextureHelper.create("ScreenCaptureThread", eglBase!!.eglBaseContext)

        videoCapturer = ScreenCapturerAndroid(data, object : MediaProjection.Callback() {})
        videoCapturer?.initialize(surfaceHelper, this, videoSource!!.capturerObserver)

        val (w, h) = when (resolution) {
            "480p" -> Pair(854, 480)
            "360p" -> Pair(640, 360)
            else -> Pair(1280, 720)
        }

        // Hardcap to exactly 15 fps
        videoCapturer?.startCapture(w, h, 15)

        val videoTrack = peerConnectionFactory?.createVideoTrack("ARDAMSv0", videoSource)
        videoTrack?.setEnabled(true)

        // No signaling here; the app ensures capture runs and hardware encoders are used
    }

    private fun stopCapture() {
        try {
            videoCapturer?.stopCapture()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
        videoCapturer?.dispose()
        videoSource?.dispose()
        peerConnectionFactory?.dispose()
        eglBase?.release()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopCapture()
    }
}
